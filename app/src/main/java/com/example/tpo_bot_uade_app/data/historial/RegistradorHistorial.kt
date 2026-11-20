package com.example.tpo_bot_uade_app.data.historial

import android.content.Context
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.HistorialRepository
import com.example.tpo_bot_uade_app.domain.RobotRepository
import com.example.tpo_bot_uade_app.domain.model.CategoriaEvento
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.DecisionIncidente
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.EstadoMision
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import com.example.tpo_bot_uade_app.domain.model.EventoRobot
import com.example.tpo_bot_uade_app.domain.model.RegistroEvento
import com.example.tpo_bot_uade_app.domain.model.Severidad
import com.example.tpo_bot_uade_app.domain.model.TipoAlerta
import com.example.tpo_bot_uade_app.domain.model.TipoComando
import com.example.tpo_bot_uade_app.domain.model.tipo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Escucha los hechos del robot y los guarda en el historial.
// Vive en el AppContainer (lo mismo que la app), asi no se pierde nada aunque se cierre una pantalla.
// Los textos salen de strings.xml y quedan guardados ya escritos, listos para que los lea el supervisor.
class RegistradorHistorial(
    private val context: Context,
    private val robot: RobotRepository,
    private val historial: HistorialRepository,
    private val scope: CoroutineScope,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    fun iniciar() {
        scope.launch {
            robot.eventos.collect { evento -> registrar(evento) }
        }
    }

    // Riesgo R3: si la app pasa a segundo plano con una mision activa, queda registrado.
    fun appEnSegundoPlano() {
        val t = robot.telemetria.value ?: return
        if (t.estado == EstadoOperativo.EN_BASE) return
        guardar(CategoriaEvento.MISION, Severidad.ADVERTENCIA, texto(R.string.h_segundo_plano), t.mision?.nombre ?: "")
    }

    private suspend fun registrar(evento: EventoRobot) {
        when (evento) {
            is EventoRobot.MisionIniciada ->
                guardar(CategoriaEvento.MISION, Severidad.INFO, texto(R.string.h_mision_iniciada), evento.nombre, evento.robotId)

            is EventoRobot.MisionFinalizada -> {
                val r = evento.registro
                historial.guardarMision(r)
                val (titulo, severidad) = when (r.resultado) {
                    EstadoMision.COMPLETADA -> R.string.h_mision_completada to Severidad.INFO
                    EstadoMision.CANCELADA -> R.string.h_mision_cancelada to Severidad.ADVERTENCIA
                    else -> R.string.h_mision_abortada to Severidad.CRITICA
                }
                guardar(
                    CategoriaEvento.MISION, severidad, texto(titulo),
                    texto(
                        R.string.h_mision_detalle,
                        r.nombre,
                        cantidad(R.plurals.p_puntos_inspeccionados, r.totalPuntos, r.puntosInspeccionados, r.totalPuntos),
                        cantidad(R.plurals.p_anomalias, r.anomalias, r.anomalias)
                    ),
                    r.robotId
                )
            }

            is EventoRobot.AlertaEmitida ->
                guardar(CategoriaEvento.ALERTA, evento.alerta.severidad, textoAlerta(evento.alerta.tipo), evento.alerta.mensaje)

            EventoRobot.ConexionPerdida ->
                guardar(CategoriaEvento.CONEXION, Severidad.ADVERTENCIA, texto(R.string.h_conexion_perdida), "")

            EventoRobot.ConexionRecuperada ->
                guardar(CategoriaEvento.CONEXION, Severidad.INFO, texto(R.string.h_conexion_recuperada), "")

            is EventoRobot.ComandoResuelto -> {
                val r = evento.respuesta
                val severidad = if (r.estado == EstadoComando.CONFIRMADA) Severidad.INFO else Severidad.ADVERTENCIA
                val titulo = texto(R.string.h_comando, textoComando(evento.comando.tipo), textoEstado(r.estado))
                val partes = listOfNotNull(
                    (evento.comando as? Comando.ResolverIncidente)?.let { texto(R.string.h_decision, textoDecision(it.decision)) },
                    r.motivo,
                    if (r.estado == EstadoComando.SIN_RESPUESTA) texto(R.string.ec_sin_respuesta_detalle) else null
                )
                guardar(CategoriaEvento.COMANDO, severidad, titulo, partes.joinToString(" · "))
            }
        }
    }

    private fun guardar(
        categoria: CategoriaEvento,
        severidad: Severidad,
        titulo: String,
        detalle: String,
        robotId: String = robot.telemetria.value?.robotId ?: ""
    ) {
        val registro = RegistroEvento(
            timestampMs = reloj(), categoria = categoria, severidad = severidad,
            titulo = titulo, detalle = detalle, robotId = robotId
        )
        scope.launch { historial.guardarEvento(registro) }
    }

    // ---------- Textos ----------

    private fun texto(id: Int, vararg args: Any): String = context.getString(id, *args)

    private fun cantidad(id: Int, n: Int, vararg args: Any): String = context.resources.getQuantityString(id, n, *args)

    private fun textoComando(tipo: TipoComando) = texto(
        when (tipo) {
            TipoComando.INICIAR_MISION -> R.string.cmd_iniciar
            TipoComando.PAUSAR_MISION -> R.string.cmd_pausar
            TipoComando.REANUDAR_MISION -> R.string.cmd_reanudar
            TipoComando.CANCELAR_MISION -> R.string.cmd_cancelar
            TipoComando.REGRESAR_A_PUNTO_SEGURO -> R.string.cmd_regresar
            TipoComando.DETENCION_OPERATIVA -> R.string.cmd_detencion
            TipoComando.RECONOCER_ALERTA -> R.string.cmd_reconocer
            TipoComando.RESOLVER_INCIDENTE -> R.string.cmd_resolver
        }
    )

    private fun textoEstado(estado: EstadoComando) = texto(
        when (estado) {
            EstadoComando.PENDIENTE -> R.string.ec_pendiente
            EstadoComando.CONFIRMADA -> R.string.ec_confirmada
            EstadoComando.RECHAZADA -> R.string.ec_rechazada
            EstadoComando.SIN_RESPUESTA -> R.string.ec_sin_respuesta
        }
    )

    private fun textoDecision(decision: DecisionIncidente) = texto(
        when (decision) {
            DecisionIncidente.REINTENTAR -> R.string.dec_reintentar
            DecisionIncidente.OMITIR_PUNTO -> R.string.dec_omitir
            DecisionIncidente.REINSPECCIONAR -> R.string.dec_reinspeccionar
            DecisionIncidente.CONTINUAR -> R.string.dec_continuar
        }
    )

    private fun textoAlerta(tipo: TipoAlerta) = texto(
        when (tipo) {
            TipoAlerta.BATERIA_BAJA -> R.string.alerta_bateria_baja
            TipoAlerta.BATERIA_CRITICA -> R.string.alerta_bateria_critica
            TipoAlerta.OBSTACULO -> R.string.alerta_obstaculo
            TipoAlerta.ANOMALIA_TERMICA -> R.string.alerta_anomalia
            TipoAlerta.CONEXION_RECUPERADA -> R.string.alerta_conexion_recuperada
            TipoAlerta.PAUSA_AUTOMATICA -> R.string.alerta_pausa_automatica
        }
    )
}
