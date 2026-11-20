package com.example.tpo_bot_uade_app.domain.model

// Hechos puntuales que emite el robot. La app los guarda en el historial.
sealed class EventoRobot {
    data class MisionIniciada(val misionId: String, val nombre: String, val robotId: String) : EventoRobot()
    data class MisionFinalizada(val registro: RegistroMision) : EventoRobot()
    data class AlertaEmitida(val alerta: Alerta) : EventoRobot()
    data object ConexionPerdida : EventoRobot()
    data object ConexionRecuperada : EventoRobot()
}

// Una mision terminada, tal como queda en el historial.
data class RegistroMision(
    val id: Long = 0,              // lo asigna la base de datos
    val misionId: String,
    val nombre: String,
    val robotId: String,
    val inicioMs: Long,
    val finMs: Long,
    val resultado: EstadoMision,   // COMPLETADA, CANCELADA o ABORTADA
    val puntosInspeccionados: Int,
    val totalPuntos: Int,
    val anomalias: Int,
    val bateriaInicio: Int,
    val bateriaFin: Int
)

enum class CategoriaEvento { COMANDO, ALERTA, CONEXION, MISION }

// Cualquier otro hecho del historial (ordenes, alertas, cortes de enlace).
data class RegistroEvento(
    val id: Long = 0,              // lo asigna la base de datos
    val timestampMs: Long,
    val categoria: CategoriaEvento,
    val severidad: Severidad,
    val titulo: String,
    val detalle: String,
    val robotId: String
)
