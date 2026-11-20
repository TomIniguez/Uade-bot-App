package com.example.tpo_bot_uade_app.domain

import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.DecisionIncidente
import com.example.tpo_bot_uade_app.domain.model.EstadoConexion
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import com.example.tpo_bot_uade_app.domain.model.Posicion
import com.example.tpo_bot_uade_app.domain.model.Severidad
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.domain.model.TelemetriaServicio
import com.example.tpo_bot_uade_app.domain.model.TipoAlerta
import com.example.tpo_bot_uade_app.domain.model.TipoComando
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasComandoTest {

    // ---------- Ayudantes para armar datos de prueba ----------

    private fun telemetria(
        estado: EstadoOperativo = EstadoOperativo.EN_BASE,
        bateria: Int = 80,
        conexion: EstadoConexion = EstadoConexion.CONECTADO
    ) = Telemetria(
        robotId = "go2-01",
        conexion = conexion,
        senalPct = 80,
        bateriaPct = bateria,
        estado = estado,
        mision = null,
        posicion = Posicion(2f, 2f, "Base de carga"),
        velocidadMs = 0f,
        rumboGrados = 0,
        alertasActivas = 0,
        servicio = TelemetriaServicio(22f, 0, 0),
        timestampMs = 0L
    )

    private fun alerta(
        tipo: TipoAlerta = TipoAlerta.BATERIA_BAJA,
        severidad: Severidad = Severidad.ADVERTENCIA,
        requiereDecision: Boolean = false,
        reconocida: Boolean = false
    ) = Alerta("alr-1", tipo, severidad, "mensaje", requiereDecision, reconocida, 0L)

    private fun contexto(
        telemetria: Telemetria? = telemetria(),
        datosAntiguos: Boolean = false,
        pendiente: Boolean = false,
        alertas: List<Alerta> = emptyList()
    ) = ContextoComando(telemetria, datosAntiguos, pendiente, alertas)

    private fun assertHabilitado(h: Habilitacion) = assertTrue("Esperaba habilitado y fue $h", h.habilitado)

    private fun assertBloqueado(motivo: MotivoBloqueo, h: Habilitacion) =
        assertEquals(Habilitacion.no(motivo), h)

    // ---------- Iniciar mision ----------

    @Test
    fun iniciar_habilitado_enBaseConBateria() =
        assertHabilitado(ReglasComando.evaluar(TipoComando.INICIAR_MISION, contexto()))

    @Test
    fun iniciar_bloqueado_porBateriaBaja() = assertBloqueado(
        MotivoBloqueo.BATERIA_INSUFICIENTE,
        ReglasComando.evaluar(TipoComando.INICIAR_MISION, contexto(telemetria(bateria = 24)))
    )

    @Test
    fun iniciar_habilitado_justoEnElMinimo() =
        assertHabilitado(ReglasComando.evaluar(TipoComando.INICIAR_MISION, contexto(telemetria(bateria = 30))))

    @Test
    fun iniciar_bloqueado_porAlertaCriticaSinReconocer() = assertBloqueado(
        MotivoBloqueo.ALERTA_CRITICA_SIN_RECONOCER,
        ReglasComando.evaluar(
            TipoComando.INICIAR_MISION,
            contexto(alertas = listOf(alerta(TipoAlerta.BATERIA_CRITICA, Severidad.CRITICA)))
        )
    )

    @Test
    fun iniciar_bloqueado_siYaEstaEnMision() = assertBloqueado(
        MotivoBloqueo.ROBOT_NO_EN_BASE,
        ReglasComando.evaluar(TipoComando.INICIAR_MISION, contexto(telemetria(EstadoOperativo.EN_MISION)))
    )

    // ---------- Pausar ----------

    @Test
    fun pausar_habilitado_enMision() = assertHabilitado(
        ReglasComando.evaluar(TipoComando.PAUSAR_MISION, contexto(telemetria(EstadoOperativo.EN_MISION)))
    )

    @Test
    fun pausar_bloqueado_enBase() = assertBloqueado(
        MotivoBloqueo.SIN_MISION_EN_CURSO,
        ReglasComando.evaluar(TipoComando.PAUSAR_MISION, contexto())
    )

    // ---------- Reanudar ----------

    @Test
    fun reanudar_habilitado_pausadoConBateria() = assertHabilitado(
        ReglasComando.evaluar(TipoComando.REANUDAR_MISION, contexto(telemetria(EstadoOperativo.PAUSADO)))
    )

    @Test
    fun reanudar_bloqueado_conBateriaCritica() = assertBloqueado(
        MotivoBloqueo.BATERIA_INSUFICIENTE,
        ReglasComando.evaluar(TipoComando.REANUDAR_MISION, contexto(telemetria(EstadoOperativo.PAUSADO, bateria = 10)))
    )

    @Test
    fun reanudar_bloqueado_siNoEstaPausado() = assertBloqueado(
        MotivoBloqueo.MISION_NO_PAUSADA,
        ReglasComando.evaluar(TipoComando.REANUDAR_MISION, contexto(telemetria(EstadoOperativo.EN_MISION)))
    )

    // ---------- Cancelar ----------

    @Test
    fun cancelar_habilitado_esperandoDecision() = assertHabilitado(
        ReglasComando.evaluar(TipoComando.CANCELAR_MISION, contexto(telemetria(EstadoOperativo.ESPERANDO_DECISION)))
    )

    @Test
    fun cancelar_bloqueado_detenido() = assertBloqueado(
        MotivoBloqueo.SIN_MISION_PARA_CANCELAR,
        ReglasComando.evaluar(TipoComando.CANCELAR_MISION, contexto(telemetria(EstadoOperativo.DETENIDO)))
    )

    // ---------- Regresar a punto seguro ----------

    @Test
    fun regresar_habilitado_sinConexion() = assertHabilitado(
        ReglasComando.evaluar(
            TipoComando.REGRESAR_A_PUNTO_SEGURO,
            contexto(telemetria(EstadoOperativo.EN_MISION, conexion = EstadoConexion.SIN_CONEXION), datosAntiguos = true)
        )
    )

    @Test
    fun regresar_habilitado_desdeDetenido() = assertHabilitado(
        ReglasComando.evaluar(TipoComando.REGRESAR_A_PUNTO_SEGURO, contexto(telemetria(EstadoOperativo.DETENIDO)))
    )

    @Test
    fun regresar_bloqueado_siYaRegresa() = assertBloqueado(
        MotivoBloqueo.YA_REGRESANDO,
        ReglasComando.evaluar(TipoComando.REGRESAR_A_PUNTO_SEGURO, contexto(telemetria(EstadoOperativo.REGRESANDO)))
    )

    // ---------- Detencion operativa ----------

    @Test
    fun detencion_habilitada_sinConexionYConOtraOrdenPendiente() = assertHabilitado(
        ReglasComando.evaluar(
            TipoComando.DETENCION_OPERATIVA,
            contexto(
                telemetria(EstadoOperativo.EN_MISION, conexion = EstadoConexion.SIN_CONEXION),
                datosAntiguos = true,
                pendiente = true
            )
        )
    )

    @Test
    fun detencion_bloqueada_siYaEstaDetenido() = assertBloqueado(
        MotivoBloqueo.YA_DETENIDO,
        ReglasComando.evaluar(TipoComando.DETENCION_OPERATIVA, contexto(telemetria(EstadoOperativo.DETENIDO)))
    )

    // ---------- Reconocer alerta ----------

    @Test
    fun reconocer_habilitado_alertaComun() =
        assertHabilitado(ReglasComando.reconocerAlerta(alerta(), contexto()))

    @Test
    fun reconocer_bloqueado_siRequiereDecision() = assertBloqueado(
        MotivoBloqueo.ALERTA_REQUIERE_DECISION,
        ReglasComando.reconocerAlerta(
            alerta(TipoAlerta.OBSTACULO, Severidad.CRITICA, requiereDecision = true),
            contexto()
        )
    )

    @Test
    fun reconocer_bloqueado_siYaFueReconocida() = assertBloqueado(
        MotivoBloqueo.ALERTA_YA_RECONOCIDA,
        ReglasComando.reconocerAlerta(alerta(reconocida = true), contexto())
    )

    // ---------- Resolver incidente ----------

    private val obstaculo = alerta(TipoAlerta.OBSTACULO, Severidad.CRITICA, requiereDecision = true)
    private val esperando = contexto(telemetria(EstadoOperativo.ESPERANDO_DECISION))

    @Test
    fun resolver_habilitado_decisionValida() = assertHabilitado(
        ReglasComando.resolverIncidente(obstaculo, DecisionIncidente.OMITIR_PUNTO, esperando)
    )

    @Test
    fun resolver_bloqueado_decisionDeOtroTipoDeIncidente() = assertBloqueado(
        MotivoBloqueo.DECISION_NO_VALIDA,
        ReglasComando.resolverIncidente(obstaculo, DecisionIncidente.REINSPECCIONAR, esperando)
    )

    @Test
    fun resolver_bloqueado_sinIncidente() = assertBloqueado(
        MotivoBloqueo.SIN_INCIDENTE,
        ReglasComando.resolverIncidente(obstaculo, DecisionIncidente.REINTENTAR, contexto(telemetria(EstadoOperativo.EN_MISION)))
    )

    // ---------- Reglas transversales ----------

    @Test
    fun conOrdenPendiente_todoBloqueadoSalvoDetencion() {
        val tabla = ReglasComando.todas(contexto(telemetria(EstadoOperativo.EN_MISION), pendiente = true))
        tabla.forEach { (tipo, h) ->
            if (tipo == TipoComando.DETENCION_OPERATIVA) assertHabilitado(h)
            else assertBloqueado(MotivoBloqueo.COMANDO_PENDIENTE, h)
        }
    }

    @Test
    fun conDatosAntiguos_reanudarBloqueado() = assertBloqueado(
        MotivoBloqueo.DATOS_ANTIGUOS,
        ReglasComando.evaluar(TipoComando.REANUDAR_MISION, contexto(telemetria(EstadoOperativo.PAUSADO), datosAntiguos = true))
    )

    @Test
    fun sinConexion_pausarHabilitado_paraIntentarFrenar() = assertHabilitado(
        ReglasComando.evaluar(
            TipoComando.PAUSAR_MISION,
            contexto(telemetria(EstadoOperativo.EN_MISION, conexion = EstadoConexion.SIN_CONEXION), datosAntiguos = true)
        )
    )

    @Test
    fun sinConexion_cancelarBloqueado() = assertBloqueado(
        MotivoBloqueo.SIN_CONEXION,
        ReglasComando.evaluar(
            TipoComando.CANCELAR_MISION,
            contexto(telemetria(EstadoOperativo.EN_MISION, conexion = EstadoConexion.SIN_CONEXION))
        )
    )

    @Test
    fun sinTelemetria_todoBloqueado() {
        ReglasComando.todas(contexto(telemetria = null)).values.forEach {
            assertBloqueado(MotivoBloqueo.SIN_TELEMETRIA, it)
        }
    }
}
