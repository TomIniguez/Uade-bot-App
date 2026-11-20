package com.example.tpo_bot_uade_app.ui.robot

import com.example.tpo_bot_uade_app.data.simulador.SimulatedRobotRepository
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
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
    private fun probar(robotId: String = "go2-01", prueba: suspend TestScope.(RobotViewModel) -> Unit) = runTest(despachador) {
        val repo = SimulatedRobotRepository(backgroundScope, Random(1)) { currentTime }
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
}
