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

// Un punto de control del plano, en metros (origen abajo a la izquierda).
data class PuntoControl(
    val id: String,
    val nombre: String,
    val x: Float,
    val y: Float,
    val temperaturaBase: Float
)

// El lugar donde trabaja el robot. Lo usa el mini mapa.
data class Plano(
    val anchoM: Float,
    val altoM: Float,
    val base: PuntoControl,        // punto seguro
    val puntos: List<PuntoControl> // incluye la base
) {
    fun punto(id: String): PuntoControl? = puntos.firstOrNull { it.id == id }
}
