package com.example.tpo_bot_uade_app.ui

// Un movimiento = (vx adelante/atrás, vy costado, vyaw giro). Límites del backend: ±0.20 m/s y ±0.50 rad/s.
data class Movimiento(val vx: Double, val vy: Double, val vyaw: Double)

// Velocidades máximas del joystick. El robot es bípedo: no se mueve de costado (vy siempre 0).
object Velocidades {
    const val MAX_VX = 0.20
    const val MAX_VYAW = 0.50
}
