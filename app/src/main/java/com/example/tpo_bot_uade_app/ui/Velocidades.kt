package com.example.tpo_bot_uade_app.ui

// Un movimiento = (vx adelante/atrás, vy costado, vyaw giro). Límites del backend: ±0.20 m/s y ±0.50 rad/s.
data class Movimiento(val vx: Double, val vy: Double, val vyaw: Double)

// Todas las velocidades en un solo lugar, para cambiarlas fácil.
object Velocidades {
    val ADELANTE = Movimiento(0.20, 0.0, 0.0)
    val ATRAS = Movimiento(-0.20, 0.0, 0.0)
    val GIRAR_IZQUIERDA = Movimiento(0.0, 0.0, 0.50)   // sobre su eje
    val GIRAR_DERECHA = Movimiento(0.0, 0.0, -0.50)
    val COSTADO_IZQUIERDA = Movimiento(0.0, 0.20, 0.0)
    val COSTADO_DERECHA = Movimiento(0.0, -0.20, 0.0)
    val CURVA_IZQUIERDA = Movimiento(0.15, 0.0, 0.40)  // avanza girando
    val CURVA_DERECHA = Movimiento(0.15, 0.0, -0.40)
}
