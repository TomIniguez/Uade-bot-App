package com.example.tpo_bot_uade_app.domain

import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.DecisionIncidente
import com.example.tpo_bot_uade_app.domain.model.EstadoConexion
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import com.example.tpo_bot_uade_app.domain.model.Severidad
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.domain.model.TipoAlerta
import com.example.tpo_bot_uade_app.domain.model.TipoComando

// Por que un boton esta deshabilitado. La UI lo traduce a texto con strings.xml.
enum class MotivoBloqueo {
    SIN_TELEMETRIA,            // todavia no hay datos del robot
    COMANDO_PENDIENTE,         // hay otra orden esperando respuesta
    SIN_CONEXION,
    DATOS_ANTIGUOS,
    ROBOT_NO_EN_BASE,
    SIN_MISION_EN_CURSO,
    MISION_NO_PAUSADA,
    SIN_MISION_PARA_CANCELAR,
    YA_EN_BASE,
    YA_REGRESANDO,
    YA_DETENIDO,
    BATERIA_INSUFICIENTE,
    ALERTA_CRITICA_SIN_RECONOCER,
    SIN_INCIDENTE,
    ALERTA_YA_RECONOCIDA,
    ALERTA_REQUIERE_DECISION,
    DECISION_NO_VALIDA
}

data class Habilitacion(val habilitado: Boolean, val motivo: MotivoBloqueo? = null) {
    companion object {
        val SI = Habilitacion(true)
        fun no(motivo: MotivoBloqueo) = Habilitacion(false, motivo)
    }
}

// Lo que la interfaz sabe en un momento dado y necesita para decidir que botones habilitar.
data class ContextoComando(
    val telemetria: Telemetria?,
    val datosAntiguos: Boolean,
    val hayComandoPendiente: Boolean,
    val alertas: List<Alerta>
)

// Condiciones de habilitacion de cada orden (tabla 5.5 del plan y diagrama 3 de arquitectura).
// Son funciones puras: mismos datos de entrada, mismo resultado. Por eso se pueden testear facil.
// Ojo: esto solo decide que muestra la interfaz. El robot (simulador) vuelve a validar cada orden.
object ReglasComando {

    const val BATERIA_MINIMA_INICIO = 30
    const val BATERIA_CRITICA = 10

    // Calcula la habilitacion de todos los comandos de una vez (para el UiState).
    fun todas(contexto: ContextoComando): Map<TipoComando, Habilitacion> =
        TipoComando.entries.associateWith { evaluar(it, contexto) }

    fun evaluar(tipo: TipoComando, contexto: ContextoComando): Habilitacion {
        val t = contexto.telemetria ?: return Habilitacion.no(MotivoBloqueo.SIN_TELEMETRIA)

        // Detencion operativa nunca se bloquea por otra orden pendiente ni por falta de conexion.
        if (tipo == TipoComando.DETENCION_OPERATIVA) {
            return when (t.estado) {
                EstadoOperativo.EN_BASE -> Habilitacion.no(MotivoBloqueo.YA_EN_BASE)
                EstadoOperativo.DETENIDO -> Habilitacion.no(MotivoBloqueo.YA_DETENIDO)
                else -> Habilitacion.SI
            }
        }

        // Mientras haya una orden PENDIENTE no se manda otra.
        if (contexto.hayComandoPendiente) return Habilitacion.no(MotivoBloqueo.COMANDO_PENDIENTE)

        // Regresar se permite aun sin conexion: intentar es mas seguro que impedir.
        if (tipo == TipoComando.REGRESAR_A_PUNTO_SEGURO) {
            return when (t.estado) {
                EstadoOperativo.EN_BASE -> Habilitacion.no(MotivoBloqueo.YA_EN_BASE)
                EstadoOperativo.REGRESANDO -> Habilitacion.no(MotivoBloqueo.YA_REGRESANDO)
                else -> Habilitacion.SI
            }
        }

        // El resto necesita conexion y datos vigentes.
        motivoDeEnlace(t, contexto.datosAntiguos)?.let { return Habilitacion.no(it) }

        return when (tipo) {
            TipoComando.INICIAR_MISION -> when {
                t.estado != EstadoOperativo.EN_BASE -> Habilitacion.no(MotivoBloqueo.ROBOT_NO_EN_BASE)
                t.bateriaPct < BATERIA_MINIMA_INICIO -> Habilitacion.no(MotivoBloqueo.BATERIA_INSUFICIENTE)
                hayCriticaSinReconocer(contexto.alertas) -> Habilitacion.no(MotivoBloqueo.ALERTA_CRITICA_SIN_RECONOCER)
                else -> Habilitacion.SI
            }

            TipoComando.PAUSAR_MISION ->
                if (t.estado == EstadoOperativo.EN_MISION) Habilitacion.SI
                else Habilitacion.no(MotivoBloqueo.SIN_MISION_EN_CURSO)

            TipoComando.REANUDAR_MISION -> when {
                t.estado != EstadoOperativo.PAUSADO -> Habilitacion.no(MotivoBloqueo.MISION_NO_PAUSADA)
                t.bateriaPct <= BATERIA_CRITICA -> Habilitacion.no(MotivoBloqueo.BATERIA_INSUFICIENTE)
                else -> Habilitacion.SI
            }

            TipoComando.CANCELAR_MISION -> when (t.estado) {
                EstadoOperativo.EN_MISION,
                EstadoOperativo.PAUSADO,
                EstadoOperativo.ESPERANDO_DECISION -> Habilitacion.SI
                else -> Habilitacion.no(MotivoBloqueo.SIN_MISION_PARA_CANCELAR)
            }

            // "Reconocer" depende de cada alerta: ver reconocerAlerta(). Aca solo se mira el enlace.
            TipoComando.RECONOCER_ALERTA -> Habilitacion.SI

            TipoComando.RESOLVER_INCIDENTE ->
                if (t.estado == EstadoOperativo.ESPERANDO_DECISION) Habilitacion.SI
                else Habilitacion.no(MotivoBloqueo.SIN_INCIDENTE)

            // Ya resueltos arriba; se repiten para que el "when" cubra todos los casos.
            TipoComando.DETENCION_OPERATIVA, TipoComando.REGRESAR_A_PUNTO_SEGURO -> Habilitacion.SI
        }
    }

    // Habilitacion del boton "Reconocer" de una alerta puntual.
    fun reconocerAlerta(alerta: Alerta, contexto: ContextoComando): Habilitacion {
        val general = evaluar(TipoComando.RECONOCER_ALERTA, contexto)
        return when {
            !general.habilitado -> general
            alerta.reconocida -> Habilitacion.no(MotivoBloqueo.ALERTA_YA_RECONOCIDA)
            alerta.requiereDecision -> Habilitacion.no(MotivoBloqueo.ALERTA_REQUIERE_DECISION)
            else -> Habilitacion.SI
        }
    }

    // Habilitacion de una decision puntual ante un incidente.
    fun resolverIncidente(alerta: Alerta, decision: DecisionIncidente, contexto: ContextoComando): Habilitacion {
        val general = evaluar(TipoComando.RESOLVER_INCIDENTE, contexto)
        return when {
            !general.habilitado -> general
            decision !in decisionesValidas(alerta.tipo) -> Habilitacion.no(MotivoBloqueo.DECISION_NO_VALIDA)
            else -> Habilitacion.SI
        }
    }

    // Que decisiones tienen sentido para cada tipo de incidente.
    fun decisionesValidas(tipo: TipoAlerta): List<DecisionIncidente> = when (tipo) {
        TipoAlerta.OBSTACULO -> listOf(DecisionIncidente.REINTENTAR, DecisionIncidente.OMITIR_PUNTO)
        TipoAlerta.ANOMALIA_TERMICA -> listOf(DecisionIncidente.REINSPECCIONAR, DecisionIncidente.CONTINUAR)
        else -> emptyList()
    }

    private fun motivoDeEnlace(t: Telemetria, datosAntiguos: Boolean): MotivoBloqueo? = when {
        t.conexion != EstadoConexion.CONECTADO -> MotivoBloqueo.SIN_CONEXION
        datosAntiguos -> MotivoBloqueo.DATOS_ANTIGUOS
        else -> null
    }

    private fun hayCriticaSinReconocer(alertas: List<Alerta>) =
        alertas.any { it.severidad == Severidad.CRITICA && !it.reconocida }
}
