package com.example.tpo_bot_uade_app.domain.model

enum class Severidad { INFO, ADVERTENCIA, CRITICA }

enum class TipoAlerta {
    BATERIA_BAJA,
    BATERIA_CRITICA,
    OBSTACULO,
    ANOMALIA_TERMICA,
    CONEXION_RECUPERADA,
    PAUSA_AUTOMATICA
}

data class Alerta(
    val id: String,
    val tipo: TipoAlerta,
    val severidad: Severidad,
    val mensaje: String,
    val requiereDecision: Boolean, // true = es un incidente: se resuelve con una decision, no con "Reconocer"
    val reconocida: Boolean,
    val timestampMs: Long
)

// Decisiones que puede tomar el operador ante un incidente.
enum class DecisionIncidente { REINTENTAR, OMITIR_PUNTO, REINSPECCIONAR, CONTINUAR }
