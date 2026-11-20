package com.example.tpo_bot_uade_app.ui.robot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tpo_bot_uade_app.domain.ContextoComando
import com.example.tpo_bot_uade_app.domain.Habilitacion
import com.example.tpo_bot_uade_app.domain.ReglasComando
import com.example.tpo_bot_uade_app.domain.RobotRepository
import com.example.tpo_bot_uade_app.domain.Umbrales
import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.EstadoConexion
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import com.example.tpo_bot_uade_app.domain.model.RegistroComando
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.domain.model.TipoComando
import com.example.tpo_bot_uade_app.domain.model.tipo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Ordenes que piden confirmacion antes de enviarse (tabla 5.5 del plan).
private val CON_CONFIRMACION = setOf(
    TipoComando.INICIAR_MISION,
    TipoComando.CANCELAR_MISION,
    TipoComando.REGRESAR_A_PUNTO_SEGURO
)

private const val MAX_RECIENTES = 5

// Guarda el estado de las pantallas y sobrevive a la rotacion.
// Solo conoce la interfaz RobotRepository: no sabe si del otro lado hay un simulador o un robot real.
class RobotViewModel(
    private val repo: RobotRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RobotUiState(robots = repo.robots, misiones = repo.misiones, plano = repo.plano)
    )
    val uiState: StateFlow<RobotUiState> = _uiState.asStateFlow()

    private var contadorComandos = 0

    // Late una vez por segundo para que "hace N s" avance aunque no llegue telemetria nueva.
    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(1000)
        }
    }

    init {
        viewModelScope.launch {
            combine(repo.telemetria, repo.alertas, ticker) { telemetria, alertas, _ -> telemetria to alertas }
                .collect { (telemetria, alertas) -> recalcular(telemetria, alertas) }
        }
    }

    // Unica entrada publica para la UI.
    fun onEvento(evento: EventoUi) {
        when (evento) {
            is EventoUi.SeleccionarRobot -> conectar(evento.robotId)
            EventoUi.Desconectar -> _uiState.update { it.copy(dialogo = DialogoUi.ConfirmarDesconexion) }
            is EventoUi.PedirComando -> pedirComando(evento.comando, evento.yaConfirmado)
            EventoUi.ConfirmarDialogo -> confirmarDialogo()
            EventoUi.CerrarDialogo -> _uiState.update { it.copy(dialogo = null) }
        }
    }

    // ---------- Conexion ----------

    private fun conectar(robotId: String) {
        if (_uiState.value.conectando != null) return
        _uiState.update { it.copy(conectando = robotId) }
        viewModelScope.launch {
            val ok = repo.conectar(robotId)
            _uiState.update {
                it.copy(
                    conectando = null,
                    robotConectado = if (ok) repo.robots.first { r -> r.id == robotId } else null
                )
            }
        }
    }

    private fun desconectar() {
        viewModelScope.launch {
            repo.desconectar()
            _uiState.update { it.copy(robotConectado = null, ultimoComando = null, comandosRecientes = emptyList()) }
        }
    }

    // ---------- Ordenes ----------

    private fun pedirComando(comando: Comando, yaConfirmado: Boolean) {
        if (!habilitado(comando)) return // la UI ya lo muestra deshabilitado; esto es una segunda barrera
        if (comando.tipo in CON_CONFIRMACION && !yaConfirmado) {
            _uiState.update { it.copy(dialogo = DialogoUi.Confirmar(comando)) }
        } else {
            enviar(comando)
        }
    }

    private fun confirmarDialogo() {
        val dialogo = _uiState.value.dialogo
        _uiState.update { it.copy(dialogo = null) }
        when (dialogo) {
            is DialogoUi.Confirmar -> if (habilitado(dialogo.comando)) enviar(dialogo.comando)
            DialogoUi.ConfirmarDesconexion -> desconectar()
            null -> Unit
        }
    }

    private fun habilitado(comando: Comando): Boolean {
        val estado = _uiState.value
        val contexto = contexto(estado.telemetria, estado.datosAntiguos, estado.alertas)
        val h: Habilitacion = when (comando) {
            is Comando.ReconocerAlerta -> estado.alertas.firstOrNull { it.id == comando.alertaId }
                ?.let { ReglasComando.reconocerAlerta(it, contexto) } ?: Habilitacion(false)
            is Comando.ResolverIncidente -> estado.alertas.firstOrNull { it.id == comando.alertaId }
                ?.let { ReglasComando.resolverIncidente(it, comando.decision, contexto) } ?: Habilitacion(false)
            else -> ReglasComando.evaluar(comando.tipo, contexto)
        }
        return h.habilitado
    }

    // 1) Se muestra PENDIENTE enseguida. 2) Se espera la respuesta del robot.
    // 3) Se muestra CONFIRMADA, RECHAZADA o SIN_RESPUESTA. Confirmada = el robot acepto la orden;
    //    que se haya ejecutado se ve despues, en la telemetria.
    private fun enviar(comando: Comando) {
        contadorComandos++
        val registro = RegistroComando(
            comandoId = "cmd-%06d".format(contadorComandos),
            tipo = comando.tipo,
            estado = EstadoComando.PENDIENTE,
            motivo = null,
            enviadoMs = reloj()
        )
        _uiState.update {
            it.copy(ultimoComando = registro, comandosRecientes = (listOf(registro) + it.comandosRecientes).take(MAX_RECIENTES))
        }
        recalcularHabilitacion()

        viewModelScope.launch {
            val respuesta = repo.enviar(registro.comandoId, comando)
            val final = registro.copy(estado = respuesta.estado, motivo = respuesta.motivo)
            _uiState.update { estado ->
                estado.copy(
                    ultimoComando = if (estado.ultimoComando?.comandoId == final.comandoId) final else estado.ultimoComando,
                    comandosRecientes = estado.comandosRecientes.map { if (it.comandoId == final.comandoId) final else it }
                )
            }
            recalcularHabilitacion()
        }
    }

    // ---------- Estado derivado ----------

    private fun hayComandoPendiente() =
        _uiState.value.comandosRecientes.any { it.estado == EstadoComando.PENDIENTE }

    private fun contexto(telemetria: Telemetria?, datosAntiguos: Boolean, alertas: List<Alerta>) =
        ContextoComando(telemetria, datosAntiguos, hayComandoPendiente(), alertas)

    private fun recalcularHabilitacion() {
        val e = _uiState.value
        _uiState.update { it.copy(habilitacion = ReglasComando.todas(contexto(e.telemetria, e.datosAntiguos, e.alertas))) }
    }

    // Calcula lo "derivado": si los datos estan viejos y que botones se pueden usar.
    private fun recalcular(telemetria: Telemetria?, alertas: List<Alerta>) {
        val edadMs = if (telemetria == null) 0L else (reloj() - telemetria.timestampMs).coerceAtLeast(0L)
        val antiguos = telemetria != null &&
            (telemetria.conexion == EstadoConexion.SIN_CONEXION || edadMs > Umbrales.DATOS_ANTIGUOS_MS)
        _uiState.update {
            it.copy(
                telemetria = telemetria,
                alertas = alertas,
                // Hay incidente mientras el robot espera decision y la alerta no fue resuelta.
                incidenteActivo = if (telemetria?.estado == EstadoOperativo.ESPERANDO_DECISION)
                    alertas.lastOrNull { it.requiereDecision && !it.reconocida } else null,
                datosAntiguos = antiguos,
                segundosDesdeActualizacion = (edadMs / 1000).toInt(),
                habilitacion = ReglasComando.todas(contexto(telemetria, antiguos, alertas))
            )
        }
    }
}
