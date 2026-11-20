package com.example.tpo_bot_uade_app.domain.model

// Robots que se pueden elegir en la pantalla de seleccion.
data class RobotInfo(
    val id: String,
    val nombre: String,
    val modelo: String,
    val disponible: Boolean,
    val bateriaPct: Int
)

// Una mision "de catalogo": su recorrido y la duracion que tendria en el servicio real.
data class DefinicionMision(
    val id: String,
    val nombre: String,
    val puntos: List<String>,      // ids de los puntos a inspeccionar, sin contar la base
    val duracionEstimadaMin: Int
)
