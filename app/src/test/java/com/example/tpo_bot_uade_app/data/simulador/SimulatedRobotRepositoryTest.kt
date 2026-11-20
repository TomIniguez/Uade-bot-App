package com.example.tpo_bot_uade_app.data.simulador

import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.EstadoConexion
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import com.example.tpo_bot_uade_app.domain.model.EventoRobot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

// runTest usa un reloj virtual: los delay() no esperan de verdad, asi el test tarda milisegundos.
@OptIn(ExperimentalCoroutinesApi::class)
class SimulatedRobotRepositoryTest {

    private fun TestScope.repositorio() =
        SimulatedRobotRepository(backgroundScope, Random(7)) { currentTime }

    @Test
    fun conectar_robotFueraDeLinea_falla() = runTest {
        val repo = repositorio()
        assertFalse(repo.conectar("go2-03"))
        assertNull(repo.telemetria.value)
    }

    @Test
    fun conectar_publicaTelemetria() = runTest {
        val repo = repositorio()
        assertTrue(repo.conectar("go2-01"))
        val t = repo.telemetria.value!!
        assertEquals("go2-01", t.robotId)
        assertEquals(86, t.bateriaPct)
        assertEquals(EstadoOperativo.EN_BASE, t.estado)
    }

    @Test
    fun ordenNormal_confirmadaDespuesDeLaLatencia() = runTest {
        val repo = repositorio()
        repo.conectar("go2-01")
        val antes = currentTime
        val r = repo.enviar("cmd-1", Comando.IniciarMision("M3"))
        val demora = currentTime - antes
        assertEquals(EstadoComando.CONFIRMADA, r.estado)
        assertEquals("cmd-1", r.comandoId)
        assertTrue("Demoro $demora ms", demora in CasoDeUso.LATENCIA_MIN_MS..CasoDeUso.LATENCIA_MAX_MS)
        assertEquals(EstadoOperativo.EN_MISION, repo.telemetria.value?.estado)
    }

    @Test
    fun rechazoForzado_llegaRechazadaConMotivo() = runTest {
        val repo = repositorio()
        repo.conectar("go2-01")
        repo.rechazarProximoComando()
        val r = repo.enviar("cmd-1", Comando.IniciarMision("M3"))
        assertEquals(EstadoComando.RECHAZADA, r.estado)
        assertEquals("Rechazo forzado desde el panel de pruebas", r.motivo)
        assertEquals(EstadoOperativo.EN_BASE, repo.telemetria.value?.estado)
    }

    @Test
    fun sinConexion_sinRespuestaALos3Segundos_yLaOrdenNoSeEjecuta() = runTest {
        val repo = repositorio()
        repo.conectar("go2-01")
        repo.enviar("cmd-1", Comando.IniciarMision("M1"))
        repo.forzarDesconexion(true)
        assertEquals(EstadoConexion.SIN_CONEXION, repo.telemetria.value?.conexion)

        val antes = currentTime
        val r = repo.enviar("cmd-2", Comando.PausarMision)
        assertEquals(EstadoComando.SIN_RESPUESTA, r.estado)
        assertEquals(CasoDeUso.TIMEOUT_COMANDO_MS, currentTime - antes)

        repo.forzarDesconexion(false)
        // La orden nunca llego: el robot sigue en mision.
        assertEquals(EstadoOperativo.EN_MISION, repo.telemetria.value?.estado)
    }

    @Test
    fun sinConexion_laTelemetriaNoSeActualiza() = runTest {
        val repo = repositorio()
        repo.conectar("go2-01")
        repo.forzarDesconexion(true)
        val congelada = repo.telemetria.value!!.timestampMs
        advanceTimeBy(5_000)
        assertEquals(congelada, repo.telemetria.value!!.timestampMs)

        repo.forzarDesconexion(false)
        assertTrue(repo.telemetria.value!!.timestampMs > congelada)
        assertEquals(EstadoConexion.CONECTADO, repo.telemetria.value?.conexion)
    }

    @Test
    fun eventos_conexionPerdidaYRecuperada() = runTest {
        val repo = repositorio()
        val recibidos = mutableListOf<EventoRobot>()
        backgroundScope.launch { repo.eventos.collect { recibidos += it } }
        runCurrent()
        repo.conectar("go2-01")
        repo.forzarDesconexion(true)
        repo.forzarDesconexion(false)
        runCurrent()
        assertTrue(recibidos.contains(EventoRobot.ConexionPerdida))
        assertTrue(recibidos.contains(EventoRobot.ConexionRecuperada))
        assertTrue(recibidos.any { it is EventoRobot.AlertaEmitida })
    }

    @Test
    fun velocidadX4_cuatroTicksPorSegundo() = runTest {
        val repo = repositorio()
        repo.conectar("go2-02") // en base se carga 1 % por tick
        val inicial = repo.telemetria.value!!.bateriaPct
        repo.fijarVelocidad(4)
        advanceTimeBy(2_100)
        // primer tick a 1000 ms (ya estaba esperando con x1), despues cada 250 ms
        assertTrue(repo.telemetria.value!!.bateriaPct - inicial >= 5)
    }
}
