package com.example.tpo_bot_uade_app.ui.robot

import com.example.tpo_bot_uade_app.data.simulador.SimulatedRobotRepository
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.DecisionIncidente
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import com.example.tpo_bot_uade_app.domain.model.TipoAlerta
import com.example.tpo_bot_uade_app.domain.model.TipoComando
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

// El ViewModel usa viewModelScope, que corre en Dispatchers.Main. En los tests no hay
// "hilo principal" de Android, asi que lo reemplazamos por uno de prueba con reloj virtual.
@OptIn(ExperimentalCoroutinesApi::class)
class RobotViewModelTest {

    private val despachador = StandardTestDispatcher()

    @Before
    fun antes() = Dispatchers.setMain(despachador)

    @After
    fun despues() = Dispatchers.resetMain()

    // Crea el ViewModel conectado a un robot, corre la prueba y al final cancela sus corrutinas
    // (el "reloj" de 1 s del ViewModel no termina nunca; si no se cancela, runTest se queda esperando).
    // El repositorio simulado tambien es el ControlSimulacion (para forzar fallas en los tests).
    private lateinit var repo: SimulatedRobotRepository

    private fun probar(robotId: String = "go2-01", prueba: suspend TestScope.(RobotViewModel) -> Unit) = runTest(despachador) {
        repo = SimulatedRobotRepository(backgroundScope, Random(1)) { currentTime }
        val vm = RobotViewModel(repo) { currentTime }
        vm.onEvento(EventoUi.SeleccionarRobot(robotId))
        advanceTimeBy(1_000) // demora de conexion
        runCurrent()
        try {
            prueba(vm)
        } finally {
            vm.viewModelScope.cancel()
        }
    }

    @Test
    fun iniciar_pideConfirmacion_yNoEnviaHastaConfirmar() = probar { vm ->
        vm.onEvento(EventoUi.PedirComando(Comando.IniciarMision("M2")))
        assertEquals(DialogoUi.Confirmar(Comando.IniciarMision("M2")), vm.uiState.value.dialogo)
        assertNull(vm.uiState.value.ultimoComando)

        vm.onEvento(EventoUi.CerrarDialogo)
        assertNull(vm.uiState.value.dialogo)
        assertNull(vm.uiState.value.ultimoComando)
    }

    @Test
    fun ordenConfirmada_pasaDePendienteAConfirmada() = probar { vm ->
        vm.onEvento(EventoUi.PedirComando(Comando.IniciarMision("M2")))
        vm.onEvento(EventoUi.ConfirmarDialogo)

        assertEquals(EstadoComando.PENDIENTE, vm.uiState.value.ultimoComando?.estado)
        assertTrue(vm.uiState.value.estaPendiente(TipoComando.INICIAR_MISION))

        advanceTimeBy(1_000) // latencia maxima: 900 ms
        runCurrent()
        assertEquals(EstadoComando.CONFIRMADA, vm.uiState.value.ultimoComando?.estado)
        assertEquals(EstadoOperativo.EN_MISION, vm.uiState.value.telemetria?.estado)
    }

    @Test
    fun conOrdenPendiente_soloDetencionHabilitada() = probar { vm ->
        vm.onEvento(EventoUi.PedirComando(Comando.IniciarMision("M1")))
        vm.onEvento(EventoUi.ConfirmarDialogo)
        advanceTimeBy(1_000)
        runCurrent()

        vm.onEvento(EventoUi.PedirComando(Comando.PausarMision)) // sin confirmacion
        val h = vm.uiState.value.habilitacion
        assertFalse(h.getValue(TipoComando.REGRESAR_A_PUNTO_SEGURO).habilitado)
        assertTrue(h.getValue(TipoComando.DETENCION_OPERATIVA).habilitado)

        // Un segundo pedido mientras hay uno pendiente se ignora.
        vm.onEvento(EventoUi.PedirComando(Comando.CancelarMision))
        assertNull(vm.uiState.value.dialogo)
    }

    @Test
    fun bateriaBaja_iniciarDeshabilitado() = probar("go2-02") { vm -> // 24 %
        assertFalse(vm.uiState.value.habilitacionDe(TipoComando.INICIAR_MISION).habilitado)
        vm.onEvento(EventoUi.PedirComando(Comando.IniciarMision("M3")))
        assertNull(vm.uiState.value.dialogo) // ni siquiera abre el dialogo
    }

    @Test
    fun desconectar_pideConfirmacion() = probar { vm ->
        vm.onEvento(EventoUi.Desconectar)
        assertEquals(DialogoUi.ConfirmarDesconexion, vm.uiState.value.dialogo)
        vm.onEvento(EventoUi.ConfirmarDialogo)
        runCurrent()
        assertNull(vm.uiState.value.robotConectado)
    }

    @Test
    fun rechazoForzado_seVeRechazadaConMotivo() = probar { vm ->
        repo.rechazarProximoComando()
        vm.onEvento(EventoUi.PedirComando(Comando.IniciarMision("M3")))
        vm.onEvento(EventoUi.ConfirmarDialogo)
        advanceTimeBy(1_000)
        runCurrent()
        val ultimo = vm.uiState.value.ultimoComando!!
        assertEquals(EstadoComando.RECHAZADA, ultimo.estado)
        assertEquals("Rechazo forzado desde el panel de pruebas", ultimo.motivo)
        assertEquals(EstadoOperativo.EN_BASE, vm.uiState.value.telemetria?.estado)
    }

    @Test
    fun obstaculo_abreIncidente_yOmitirLoCierra() = probar { vm ->
        vm.onEvento(EventoUi.PedirComando(Comando.IniciarMision("M2")))
        vm.onEvento(EventoUi.ConfirmarDialogo)
        advanceTimeBy(1_000)
        runCurrent()
        repo.inyectarObstaculo()
        runCurrent()
        val incidente = vm.uiState.value.incidenteActivo!!

        // Desde el dialogo de incidente la decision sale sin pedir otra confirmacion.
        vm.onEvento(EventoUi.PedirComando(Comando.ResolverIncidente(incidente.id, DecisionIncidente.OMITIR_PUNTO), yaConfirmado = true))
        assertNull(vm.uiState.value.dialogo)
        advanceTimeBy(1_000)
        runCurrent()
        assertNull(vm.uiState.value.incidenteActivo)
        assertEquals(EstadoOperativo.EN_MISION, vm.uiState.value.telemetria?.estado)
        assertEquals(listOf("P1"), vm.uiState.value.telemetria?.mision?.puntosOmitidos)
    }

    // Escenario 3 completo: corte de enlace, orden sin respuesta, pausa automatica y recuperacion.
    @Test
    fun desconexion_pausarSinRespuesta_yAlVolverAlertas() = probar { vm ->
        vm.onEvento(EventoUi.PedirComando(Comando.IniciarMision("M1")))
        vm.onEvento(EventoUi.ConfirmarDialogo)
        advanceTimeBy(1_000)
        runCurrent()

        repo.forzarDesconexion(true)
        advanceTimeBy(4_000)
        runCurrent()
        assertTrue(vm.uiState.value.datosAntiguos)
        assertTrue(vm.uiState.value.segundosDesdeActualizacion >= 4)

        // Pausar se puede intentar sin conexion, pero la respuesta nunca llega.
        vm.onEvento(EventoUi.PedirComando(Comando.PausarMision))
        assertEquals(EstadoComando.PENDIENTE, vm.uiState.value.ultimoComando?.estado)
        advanceTimeBy(3_100)
        runCurrent()
        assertEquals(EstadoComando.SIN_RESPUESTA, vm.uiState.value.ultimoComando?.estado)

        advanceTimeBy(20_000) // mas de 20 ticks sin enlace: el robot se pausa solo
        repo.forzarDesconexion(false)
        runCurrent()
        val estado = vm.uiState.value
        assertFalse(estado.datosAntiguos)
        assertTrue(estado.conexionRecuperada)
        assertEquals(EstadoOperativo.PAUSADO, estado.telemetria?.estado)
        val tipos = estado.alertas.map { it.tipo }
        assertTrue(TipoAlerta.CONEXION_RECUPERADA in tipos)
        assertTrue(TipoAlerta.PAUSA_AUTOMATICA in tipos)
    }
}
