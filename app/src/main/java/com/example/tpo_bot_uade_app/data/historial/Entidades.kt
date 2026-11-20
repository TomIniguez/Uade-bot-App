package com.example.tpo_bot_uade_app.data.historial

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tablas de Room. Solo las conoce la capa de datos: las pantallas usan RegistroMision / RegistroEvento.
// Privacidad (riesgo R5): solo datos operativos. Nada de imagenes ni datos personales.

@Entity(tableName = "misiones")
data class MisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val misionId: String,
    val nombre: String,
    val robotId: String,
    val inicioMs: Long,
    val finMs: Long,
    val resultado: String,             // COMPLETADA, CANCELADA o ABORTADA
    val puntosInspeccionados: Int,
    val totalPuntos: Int,
    val anomalias: Int,
    val bateriaInicio: Int,
    val bateriaFin: Int
)

@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long,
    val categoria: String,             // COMANDO, ALERTA, CONEXION o MISION
    val severidad: String,             // INFO, ADVERTENCIA o CRITICA
    val titulo: String,
    val detalle: String,
    val robotId: String
)
