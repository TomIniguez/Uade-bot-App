package com.example.tpo_bot_uade_app.data.simulador

import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.DecisionIncidente
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.EstadoMision
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import com.example.tpo_bot_uade_app.domain.model.EventoRobot
import com.example.tpo_bot_uade_app.domain.model.RespuestaComando
import com.example.tpo_bot_uade_app.domain.model.TipoAlerta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

// Prueba el simulador "a mano": nosotros llamamos a tick(), sin corrutinas ni esperas reales.
class SimuladorRobotTest {

    private var reloj = 0L
    private var contador = 0

    private fun simulador(robotId: String = "go2-01") =
        SimuladorRobot(CasoDeUso.ROBOTS.first { it.id == robotId }, Random(42)) { reloj }

    private fun SimuladorRobot.enviar(comando: Comando): RespuestaComando =
        procesar("cmd-${++contador}", comando)

    // Hace ticks hasta que se cumpla la condicion (con un tope para no colgarse).
    private fun SimuladorRobot.tickHasta(maximo: Int = 1000, condicion: (SimuladorRobot) -> Boolean) {
        repeat(maximo) {
            if (condicion(this)) return
            reloj += 1000
            tick()
        }
        throw AssertionError("La condicion no se cumplio en $maximo ticks. Estado: $estado")
    }

    private fun SimuladorRobot.misionFinalizada() =
        tomarEventos().filterIsInstance<EventoRobot.MisionFinalizada>().single().registro

    private fun SimuladorRobot.incidenteId() = alertas.last { it.requiereDecision && !it.reconocida }.id

    // ---------- Mision normal ----------

    @Test
    fun misionM3_completa_terminaEnBaseYCompletada() {
        val sim = simulador()
        assertEquals(EstadoComando.CONFIRMADA, sim.enviar(Comando.IniciarMision("M3")).estado)
        assertEquals(EstadoOperativo.EN_MISION, sim.estado)

        sim.tickHasta { it.estado == EstadoOperativo.REGRESANDO }
        assertEquals(100, sim.telemetria().mision?.progresoPct)
        assertEquals(EstadoMision.COMPLETADA, sim.telemetria().mision?.estado)

        sim.tickHasta { it.estado == EstadoOperativo.EN_BASE }
        val registro = sim.misionFinalizada()
        assertEquals(EstadoMision.COMPLETADA, registro.resultado)
        assertEquals(1, registro.puntosInspeccionados)
        assertEquals(1, registro.totalPuntos)
        assertEquals(EstadoMision.COMPLETADA, sim.telemetria().mision?.estado)
    }

    @Test
    fun misionM2_inspeccionaTresPuntos_yGastaBateriaRazonable() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M2"))
        var ticks = 0
        sim.tickHasta { ticks++; it.estado == EstadoOperativo.EN_BASE }
        val registro = sim.misionFinalizada()
        assertEquals(3, registro.puntosInspeccionados)
        assertTrue("Duro $ticks ticks", ticks in 80..100)
        assertTrue("Gasto ${registro.bateriaInicio - registro.bateriaFin} %", registro.bateriaInicio - registro.bateriaFin in 10..14)
    }

    @Test
    fun iniciar_rechazadoPorBateria_conMotivo() {
        val sim = simulador("go2-02") // 24 %
        val r = sim.enviar(Comando.IniciarMision("M1"))
        assertEquals(EstadoComando.RECHAZADA, r.estado)
        assertEquals("Batería insuficiente para iniciar la misión (24 %, mínimo 30 %)", r.motivo)
        assertEquals(EstadoOperativo.EN_BASE, sim.estado)
    }

    @Test
    fun ordenQueNoCorresponde_rechazadaConMotivo() {
        val r = simulador().enviar(Comando.PausarMision)
        assertEquals(EstadoComando.RECHAZADA, r.estado)
        assertEquals("No hay una misión en curso para pausar", r.motivo)
    }

    // ---------- Pausa, cancelacion, detencion ----------

    @Test
    fun pausaYReanudacion() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M3"))
        repeat(5) { sim.tick() }
        assertEquals(EstadoComando.CONFIRMADA, sim.enviar(Comando.PausarMision).estado)
        val posicion = sim.telemetria().posicion
        repeat(5) { sim.tick() }
        assertEquals(EstadoOperativo.PAUSADO, sim.estado)
        assertEquals(posicion, sim.telemetria().posicion)
        assertEquals(EstadoMision.PAUSADA, sim.telemetria().mision?.estado)
        assertEquals(0f, sim.telemetria().velocidadMs)

        assertEquals(EstadoComando.CONFIRMADA, sim.enviar(Comando.ReanudarMision).estado)
        sim.tickHasta { it.estado == EstadoOperativo.EN_BASE }
        assertEquals(EstadoMision.COMPLETADA, sim.misionFinalizada().resultado)
    }

    @Test
    fun cancelacion_vuelveSoloALaBase() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M2"))
        repeat(10) { sim.tick() }
        assertEquals(EstadoComando.CONFIRMADA, sim.enviar(Comando.CancelarMision).estado)
        assertEquals(EstadoOperativo.REGRESANDO, sim.estado)
        assertEquals(EstadoMision.CANCELADA, sim.telemetria().mision?.estado)
        sim.tickHasta { it.estado == EstadoOperativo.EN_BASE }
        assertEquals(EstadoMision.CANCELADA, sim.misionFinalizada().resultado)
    }

    @Test
    fun detencion_soloSeSaleRegresando() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M2"))
        repeat(5) { sim.tick() }
        assertEquals(EstadoComando.CONFIRMADA, sim.enviar(Comando.DetencionOperativa).estado)
        assertEquals(EstadoOperativo.DETENIDO, sim.estado)
        assertEquals(EstadoMision.ABORTADA, sim.telemetria().mision?.estado)

        assertEquals(EstadoComando.RECHAZADA, sim.enviar(Comando.ReanudarMision).estado)
        assertEquals(EstadoComando.RECHAZADA, sim.enviar(Comando.CancelarMision).estado)
        assertEquals(EstadoComando.CONFIRMADA, sim.enviar(Comando.RegresarAPuntoSeguro).estado)
        sim.tickHasta { it.estado == EstadoOperativo.EN_BASE }
        assertEquals(EstadoMision.ABORTADA, sim.misionFinalizada().resultado)
    }

    // ---------- Bateria ----------

    @Test
    fun bateriaBaja_alertaUnaSolaVez() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M1"))
        sim.fijarBateria(21)
        repeat(30) { sim.tick() }
        assertEquals(1, sim.alertas.count { it.tipo == TipoAlerta.BATERIA_BAJA })
        assertEquals(EstadoOperativo.EN_MISION, sim.estado) // la baja solo avisa, no actua sola
    }

    @Test
    fun bateriaCritica_regresoAutomatico_misionAbortada() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M1"))
        sim.fijarBateria(11)
        sim.tickHasta { it.estado == EstadoOperativo.REGRESANDO }
        assertNotNull(sim.alertas.firstOrNull { it.tipo == TipoAlerta.BATERIA_CRITICA })
        assertEquals(EstadoMision.ABORTADA, sim.telemetria().mision?.estado)
        sim.tickHasta { it.estado == EstadoOperativo.EN_BASE }
        assertEquals(EstadoMision.ABORTADA, sim.misionFinalizada().resultado)
    }

    @Test
    fun enBase_laBateriaSeCarga() {
        val sim = simulador("go2-02") // 24 %
        assertEquals(EstadoComando.RECHAZADA, sim.enviar(Comando.IniciarMision("M3")).estado)
        repeat(120) { sim.tick() } // 120 x 0,05 % = 6 %
        assertEquals(30, sim.telemetria().bateriaPct)
        assertEquals(EstadoComando.CONFIRMADA, sim.enviar(Comando.IniciarMision("M3")).estado)
    }

    // ---------- Incidentes ----------

    @Test
    fun obstaculo_reintentar_sigueYCompletaTodo() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M2"))
        repeat(3) { sim.tick() }
        assertTrue(sim.inyectarObstaculo())
        assertEquals(EstadoOperativo.ESPERANDO_DECISION, sim.estado)
        val alerta = sim.alertas.last()
        assertEquals(TipoAlerta.OBSTACULO, alerta.tipo)
        assertEquals("Obstáculo en el recorrido hacia Recepción", alerta.mensaje)

        // Mientras espera la decision, el robot no se mueve.
        val posicion = sim.telemetria().posicion
        repeat(5) { sim.tick() }
        assertEquals(posicion, sim.telemetria().posicion)

        val r = sim.enviar(Comando.ResolverIncidente(alerta.id, DecisionIncidente.REINTENTAR))
        assertEquals(EstadoComando.CONFIRMADA, r.estado)
        assertEquals(EstadoOperativo.EN_MISION, sim.estado)
        repeat(3) { sim.tick() }
        assertEquals(posicion, sim.telemetria().posicion) // espera 3 ticks antes de seguir
        sim.tickHasta { it.estado == EstadoOperativo.EN_BASE }
        assertEquals(3, sim.misionFinalizada().puntosInspeccionados)
        assertTrue(sim.alertas.first { it.id == alerta.id }.reconocida)
    }

    @Test
    fun obstaculo_omitirPunto_quedaSinInspeccionar() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M2"))
        sim.tick()
        sim.inyectarObstaculo()
        sim.enviar(Comando.ResolverIncidente(sim.incidenteId(), DecisionIncidente.OMITIR_PUNTO))
        assertEquals(setOf("P1"), sim.puntosOmitidos)
        sim.tickHasta { it.estado == EstadoOperativo.EN_BASE }
        val registro = sim.misionFinalizada()
        assertEquals(EstadoMision.COMPLETADA, registro.resultado)
        assertEquals(2, registro.puntosInspeccionados)
        assertEquals(3, registro.totalPuntos)
    }

    @Test
    fun obstaculo_decisionDeAnomalia_rechazada() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M2"))
        sim.inyectarObstaculo()
        val r = sim.enviar(Comando.ResolverIncidente(sim.incidenteId(), DecisionIncidente.CONTINUAR))
        assertEquals(EstadoComando.RECHAZADA, r.estado)
        assertEquals(EstadoOperativo.ESPERANDO_DECISION, sim.estado)
    }

    @Test
    fun obstaculo_fueraDeMision_noHaceNada() {
        val sim = simulador()
        assertFalse(sim.inyectarObstaculo())
        assertFalse(sim.inyectarAnomalia())
        assertEquals(EstadoOperativo.EN_BASE, sim.estado)
    }

    @Test
    fun anomalia_reinspeccionar_sumaUnaAnomalia() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M3"))
        assertTrue(sim.inyectarAnomalia())
        sim.tickHasta { it.estado == EstadoOperativo.ESPERANDO_DECISION }
        val t = sim.telemetria()
        assertEquals(58f, t.servicio.temperaturaC)
        assertEquals("Anomalía térmica en Sala eléctrica: 58,0 °C", sim.alertas.last().mensaje)

        sim.enviar(Comando.ResolverIncidente(sim.incidenteId(), DecisionIncidente.REINSPECCIONAR))
        sim.tick()
        assertEquals(58f, sim.telemetria().servicio.temperaturaC) // la lectura se mantiene
        sim.tickHasta { it.estado == EstadoOperativo.EN_BASE }
        val registro = sim.misionFinalizada()
        assertEquals(1, registro.anomalias)
        assertEquals(1, registro.puntosInspeccionados)
    }

    @Test
    fun anomalia_continuar_sumaUnaAnomaliaYSigue() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M3"))
        sim.inyectarAnomalia()
        sim.tickHasta { it.estado == EstadoOperativo.ESPERANDO_DECISION }
        sim.enviar(Comando.ResolverIncidente(sim.incidenteId(), DecisionIncidente.CONTINUAR))
        assertEquals(1, sim.telemetria().servicio.anomaliasDetectadas)
        assertEquals(EstadoOperativo.REGRESANDO, sim.estado) // era el unico punto
    }

    @Test
    fun cancelarDuranteIncidente_cierraElIncidente() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M2"))
        sim.inyectarObstaculo()
        assertEquals(EstadoComando.CONFIRMADA, sim.enviar(Comando.CancelarMision).estado)
        assertTrue(sim.alertas.all { it.reconocida })
        assertEquals(EstadoOperativo.REGRESANDO, sim.estado)
    }

    // ---------- Alertas ----------

    @Test
    fun reconocerAlerta_comun_siIncidente_no() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M2"))
        sim.fijarBateria(20)
        val baja = sim.alertas.single()
        assertEquals(EstadoComando.CONFIRMADA, sim.enviar(Comando.ReconocerAlerta(baja.id)).estado)
        assertEquals(0, sim.telemetria().alertasActivas)

        sim.inyectarObstaculo()
        val r = sim.enviar(Comando.ReconocerAlerta(sim.incidenteId()))
        assertEquals(EstadoComando.RECHAZADA, r.estado)
    }

    // ---------- Enlace y fallas forzadas ----------

    @Test
    fun sinEnlace20Ticks_pausaAutomatica_yAlertasAlVolver() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M1"))
        sim.fijarEnlace(false)
        repeat(19) { sim.tick() }
        assertEquals(EstadoOperativo.EN_MISION, sim.estado)
        sim.tick()
        assertEquals(EstadoOperativo.PAUSADO, sim.estado)

        sim.fijarEnlace(true)
        val tipos = sim.alertas.map { it.tipo }
        assertEquals(listOf(TipoAlerta.CONEXION_RECUPERADA, TipoAlerta.PAUSA_AUTOMATICA), tipos)
    }

    @Test
    fun cortoDeEnlaceBreve_soloConexionRecuperada() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M1"))
        sim.fijarEnlace(false)
        repeat(5) { sim.tick() }
        sim.fijarEnlace(true)
        assertEquals(listOf(TipoAlerta.CONEXION_RECUPERADA), sim.alertas.map { it.tipo })
        assertEquals(EstadoOperativo.EN_MISION, sim.estado)
    }

    @Test
    fun rechazoForzado_soloLaProximaOrden() {
        val sim = simulador()
        sim.rechazarProximoComando()
        val r = sim.enviar(Comando.IniciarMision("M3"))
        assertEquals(EstadoComando.RECHAZADA, r.estado)
        assertEquals("Rechazo forzado desde el panel de pruebas", r.motivo)
        assertEquals(EstadoComando.CONFIRMADA, sim.enviar(Comando.IniciarMision("M3")).estado)
    }

    @Test
    fun rumboYVelocidad_enMovimiento() {
        val sim = simulador()
        sim.enviar(Comando.IniciarMision("M3")) // de (2,2) a (22,20): hacia arriba a la derecha
        sim.tick()
        val t = sim.telemetria()
        assertEquals(0.8f, t.velocidadMs)
        assertEquals(42, t.rumboGrados) // atan2(18, 20) = 42 grados
    }
}
