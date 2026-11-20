package com.example.tpo_bot_uade_app.data.simulador

import com.example.tpo_bot_uade_app.domain.ControlSimulacion
import com.example.tpo_bot_uade_app.domain.RobotRepository
import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.DefinicionMision
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.EstadoConexion
import com.example.tpo_bot_uade_app.domain.model.EventoRobot
import com.example.tpo_bot_uade_app.domain.model.Plano
import com.example.tpo_bot_uade_app.domain.model.RespuestaComando
import com.example.tpo_bot_uade_app.domain.model.RobotInfo
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random

// Hace de "robot remoto" para la app: implementa RobotRepository (lo que ve el ViewModel)
// y ControlSimulacion (lo que usa el panel de pruebas).
//
// Por dentro tiene un SimuladorRobot y una corrutina que le da un tick por segundo
// (mas rapido si se elige x2 o x4). Simula lo que pasaria con un robot real:
// cada orden tarda en responder (latencia) y, si no hay enlace, nunca llega la respuesta.
class SimulatedRobotRepository(
    private val scope: CoroutineScope,
    private val random: Random = Random.Default,
    private val reloj: () -> Long = System::currentTimeMillis
) : RobotRepository, ControlSimulacion {

    override val robots: List<RobotInfo> = CasoDeUso.ROBOTS
    override val misiones: List<DefinicionMision> = CasoDeUso.MISIONES
    override val plano: Plano = CasoDeUso.PLANO

    private val _telemetria = MutableStateFlow<Telemetria?>(null)
    override val telemetria: StateFlow<Telemetria?> = _telemetria.asStateFlow()

    private val _alertas = MutableStateFlow<List<Alerta>>(emptyList())
    override val alertas: StateFlow<List<Alerta>> = _alertas.asStateFlow()

    private val _eventos = MutableSharedFlow<EventoRobot>(extraBufferCapacity = 64)
    override val eventos: SharedFlow<EventoRobot> = _eventos.asSharedFlow()

    // El simulador se toca desde la corrutina del tick y desde enviar(). El "candado" evita
    // que las dos cosas lo modifiquen al mismo tiempo.
    private val candado = Any()
    private var simulador: SimuladorRobot? = null
    private var robotActual: RobotInfo? = null
    private var tickJob: Job? = null

    @Volatile private var conectado = false
    @Volatile private var desconexionForzada = false
    @Volatile private var factorVelocidad = 1

    // =====================================================================
    // RobotRepository
    // =====================================================================

    override suspend fun conectar(robotId: String): Boolean {
        val robot = robots.firstOrNull { it.id == robotId } ?: return false
        if (!robot.disponible) return false
        delay(CasoDeUso.DEMORA_CONEXION_MS)
        synchronized(candado) {
            // Un solo simulador: si se elige otro robot, arranca de cero con los datos de ese robot.
            if (robotActual?.id != robot.id || simulador == null) {
                simulador = SimuladorRobot(robot, random, reloj)
                robotActual = robot
            }
        }
        conectado = true
        arrancarTick()
        publicar()
        return true
    }

    override suspend fun desconectar() {
        // El robot sigue haciendo lo suyo; la app simplemente deja de escucharlo.
        conectado = false
        _telemetria.value = null
        _alertas.value = emptyList()
    }

    override suspend fun enviar(comandoId: String, comando: Comando): RespuestaComando {
        val respuesta = withTimeoutOrNull(CasoDeUso.TIMEOUT_COMANDO_MS) {
            // Sin enlace el mensaje no llega: se espera hasta que venza el timeout.
            if (!hayEnlace()) awaitCancellation()
            delay(random.nextLong(CasoDeUso.LATENCIA_MIN_MS, CasoDeUso.LATENCIA_MAX_MS + 1))
            // Si el enlace se corto mientras viajaba la orden, tampoco llega.
            if (!hayEnlace()) awaitCancellation()
            val sim = simulador ?: awaitCancellation()
            synchronized(candado) { sim.procesar(comandoId, comando) }
        }
        publicar() // la telemetria nueva confirma (o no) que la orden se ejecuto
        val final = respuesta ?: RespuestaComando(comandoId, EstadoComando.SIN_RESPUESTA, null, reloj())
        _eventos.tryEmit(EventoRobot.ComandoResuelto(comando, final))
        return final
    }

    // =====================================================================
    // ControlSimulacion (panel de pruebas)
    // =====================================================================

    override fun forzarDesconexion(activa: Boolean) {
        if (activa == desconexionForzada) return
        desconexionForzada = activa
        synchronized(candado) { simulador?.fijarEnlace(!activa) }
        if (activa) {
            // La app deja de recibir datos: se queda con el ultimo valor, marcado sin conexion.
            _telemetria.value = _telemetria.value?.copy(conexion = EstadoConexion.SIN_CONEXION, senalPct = 0)
            _eventos.tryEmit(EventoRobot.ConexionPerdida)
        } else {
            _eventos.tryEmit(EventoRobot.ConexionRecuperada)
            publicar()
        }
    }

    override fun rechazarProximoComando() = conSimulador { it.rechazarProximoComando() }

    override fun fijarBateria(pct: Int) = conSimulador { it.fijarBateria(pct) }

    override fun inyectarObstaculo() = conSimulador { it.inyectarObstaculo() }

    override fun inyectarAnomalia() = conSimulador { it.inyectarAnomalia() }

    override fun fijarVelocidad(factor: Int) {
        factorVelocidad = if (factor in listOf(1, 2, 4)) factor else 1
    }

    override fun reiniciar() {
        val robot = robotActual ?: return
        desconexionForzada = false
        factorVelocidad = 1
        synchronized(candado) { simulador = SimuladorRobot(robot, random, reloj) }
        publicar()
    }

    // =====================================================================
    // Interno
    // =====================================================================

    private fun hayEnlace() = conectado && !desconexionForzada

    private fun conSimulador(accion: (SimuladorRobot) -> Unit) {
        synchronized(candado) { simulador?.let(accion) }
        publicar()
    }

    private fun arrancarTick() {
        if (tickJob?.isActive == true) return
        tickJob = scope.launch {
            while (isActive) {
                delay(1000L / factorVelocidad)
                synchronized(candado) { simulador?.tick() }
                publicar()
            }
        }
    }

    // Manda a la app el estado actual del robot, solo si hay enlace.
    // Los eventos que ocurrieron sin enlace quedan guardados en el simulador hasta que vuelva.
    private fun publicar() {
        if (!hayEnlace()) return
        val (tele, alertas, eventos) = synchronized(candado) {
            val sim = simulador ?: return
            Triple(sim.telemetria(), sim.alertas, sim.tomarEventos())
        }
        _telemetria.value = tele
        _alertas.value = alertas
        eventos.forEach { _eventos.tryEmit(it) }
    }
}
