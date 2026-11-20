package com.example.tpo_bot_uade_app.data.simulador

import com.example.tpo_bot_uade_app.domain.Umbrales
import com.example.tpo_bot_uade_app.domain.model.DefinicionMision
import com.example.tpo_bot_uade_app.domain.model.RobotInfo

// TODO lo especifico del caso de uso vive aca: ronda nocturna de inspeccion en un deposito
// logistico con un Go2. Si el grupo elige otro servicio, se cambia este archivo y nada mas.

// Un punto de control del plano (40 m x 24 m, origen abajo a la izquierda).
data class PuntoControl(
    val id: String,
    val nombre: String,
    val x: Float,
    val y: Float,
    val temperaturaBase: Float
)

object CasoDeUso {

    val BASE = PuntoControl("BASE", "Base de carga", 2f, 2f, 22f)

    val PUNTOS = listOf(
        BASE,
        PuntoControl("P1", "Recepción", 10f, 4f, 22f),
        PuntoControl("P2", "Pasillo A", 10f, 18f, 22f),
        PuntoControl("P3", "Sala eléctrica", 22f, 20f, 31f),
        PuntoControl("P4", "Pasillo B", 30f, 12f, 22f),
        PuntoControl("P5", "Cámara de frío", 36f, 4f, 6f),
        PuntoControl("P6", "Muelle de carga", 22f, 3f, 20f)
    )

    const val ANCHO_PLANO_M = 40f
    const val ALTO_PLANO_M = 24f

    fun punto(id: String): PuntoControl = PUNTOS.first { it.id == id }

    val ROBOTS = listOf(
        RobotInfo("go2-01", "Go2 Depósito Norte", "Go2", disponible = true, bateriaPct = 86),
        RobotInfo("go2-02", "Go2 Depósito Sur", "Go2", disponible = true, bateriaPct = 24),
        RobotInfo("go2-03", "Go2 Taller", "Go2", disponible = false, bateriaPct = 0)
    )

    // Los puntos no incluyen la base: el robot sale y vuelve a ella siempre.
    val MISIONES = listOf(
        DefinicionMision("M1", "Ronda completa", listOf("P1", "P2", "P3", "P4", "P5", "P6"), 12),
        DefinicionMision("M2", "Ronda corta — sector norte", listOf("P1", "P2", "P3"), 6),
        DefinicionMision("M3", "Verificación de sala eléctrica", listOf("P3"), 3)
    )

    // ---------- Umbrales ----------
    const val BATERIA_MINIMA_INICIO = Umbrales.BATERIA_MINIMA_INICIO
    const val BATERIA_BAJA = Umbrales.BATERIA_BAJA
    const val BATERIA_CRITICA = Umbrales.BATERIA_CRITICA
    const val UMBRAL_TEMPERATURA = Umbrales.UMBRAL_TEMPERATURA
    const val TEMPERATURA_ANOMALIA = 58f       // lectura cuando se inyecta una anomalia

    // ---------- Tiempos ----------
    const val LATENCIA_MIN_MS = 300L
    const val LATENCIA_MAX_MS = 900L
    const val TIMEOUT_COMANDO_MS = 3000L
    const val DATOS_ANTIGUOS_MS = Umbrales.DATOS_ANTIGUOS_MS
    const val DEMORA_CONEXION_MS = 600L
    const val PAUSA_POR_DESCONEXION_TICKS = 20

    // ---------- Movimiento y bateria (por tick = 1 segundo simulado) ----------
    const val AVANCE_POR_TICK_M = 0.8f
    const val TICKS_INSPECCION = 4
    const val TICKS_REINTENTO = 3
    const val CONSUMO_QUIETO = 0.02f
    const val CONSUMO_MOVIMIENTO = 0.15f
    const val CARGA_EN_BASE = 1f
    const val SENAL_MIN = 70
    const val SENAL_MAX = 95
}
