package com.example.tpo_bot_uade_app.domain.model

// Todo lo que el robot informa una vez por segundo.

enum class EstadoConexion { CONECTADO, SIN_CONEXION }

enum class EstadoOperativo { EN_BASE, EN_MISION, PAUSADO, ESPERANDO_DECISION, REGRESANDO, DETENIDO }

enum class EstadoMision { EN_CURSO, PAUSADA, COMPLETADA, CANCELADA, ABORTADA }

// Posicion en el plano del deposito, en metros. "zona" es el punto de control mas cercano.
data class Posicion(val x: Float, val y: Float, val zona: String)

data class MisionActiva(
    val id: String,
    val nombre: String,
    val estado: EstadoMision,
    val progresoPct: Int,          // 0..100
    val puntoActual: String?,      // punto hacia el que va o que esta inspeccionando
    val totalPuntos: Int,
    val puntosCompletados: Int = 0,                 // inspeccionados + omitidos (para el mini mapa)
    val puntosOmitidos: List<String> = emptyList()  // ids de los puntos salteados por un obstaculo
)

// Las tres variables propias del servicio (ronda de inspeccion).
data class TelemetriaServicio(
    val temperaturaC: Float,
    val puntosInspeccionados: Int,
    val anomaliasDetectadas: Int
)

data class Telemetria(
    val robotId: String,
    val conexion: EstadoConexion,
    val senalPct: Int,
    val bateriaPct: Int,
    val estado: EstadoOperativo,
    val mision: MisionActiva?,     // null si no hay mision
    val posicion: Posicion,
    val velocidadMs: Float,
    val rumboGrados: Int,          // 0..359
    val alertasActivas: Int,       // alertas sin reconocer
    val servicio: TelemetriaServicio,
    val timestampMs: Long          // "ultima actualizacion"
)
