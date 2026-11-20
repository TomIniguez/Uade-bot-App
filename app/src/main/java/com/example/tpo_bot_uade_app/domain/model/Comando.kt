package com.example.tpo_bot_uade_app.domain.model

// Ordenes que la app le puede mandar al robot.
sealed class Comando {
    data class IniciarMision(val misionId: String) : Comando()
    data object PausarMision : Comando()
    data object ReanudarMision : Comando()
    data object CancelarMision : Comando()
    data object RegresarAPuntoSeguro : Comando()
    data object DetencionOperativa : Comando()
    data class ReconocerAlerta(val alertaId: String) : Comando()
    data class ResolverIncidente(val alertaId: String, val decision: DecisionIncidente) : Comando()
}

// Un valor por comando. Sirve de clave para la tabla de habilitacion de botones.
enum class TipoComando {
    INICIAR_MISION,
    PAUSAR_MISION,
    REANUDAR_MISION,
    CANCELAR_MISION,
    REGRESAR_A_PUNTO_SEGURO,
    DETENCION_OPERATIVA,
    RECONOCER_ALERTA,
    RESOLVER_INCIDENTE
}

// Traduce un comando concreto a su tipo.
val Comando.tipo: TipoComando
    get() = when (this) {
        is Comando.IniciarMision -> TipoComando.INICIAR_MISION
        Comando.PausarMision -> TipoComando.PAUSAR_MISION
        Comando.ReanudarMision -> TipoComando.REANUDAR_MISION
        Comando.CancelarMision -> TipoComando.CANCELAR_MISION
        Comando.RegresarAPuntoSeguro -> TipoComando.REGRESAR_A_PUNTO_SEGURO
        Comando.DetencionOperativa -> TipoComando.DETENCION_OPERATIVA
        is Comando.ReconocerAlerta -> TipoComando.RECONOCER_ALERTA
        is Comando.ResolverIncidente -> TipoComando.RESOLVER_INCIDENTE
    }

enum class EstadoComando { PENDIENTE, CONFIRMADA, RECHAZADA, SIN_RESPUESTA }

// Lo que contesta el robot. Nunca trae PENDIENTE: ese estado lo pone el ViewModel antes de enviar.
data class RespuestaComando(
    val comandoId: String,
    val estado: EstadoComando,
    val motivo: String?,           // obligatorio si es RECHAZADA
    val timestampMs: Long
)

// Lo que la UI muestra como "ultima orden" y lo que se guarda en el historial.
data class RegistroComando(
    val comandoId: String,
    val tipo: TipoComando,         // la UI arma el texto ("Pausar mision") desde strings.xml
    val estado: EstadoComando,
    val motivo: String?,
    val enviadoMs: Long
)
