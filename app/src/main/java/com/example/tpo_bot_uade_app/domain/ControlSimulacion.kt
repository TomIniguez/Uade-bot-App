package com.example.tpo_bot_uade_app.domain

// Controles para forzar fallas. Solo lo usa el panel de pruebas (via SimulacionViewModel).
// Las pantallas de operacion nunca lo ven.
interface ControlSimulacion {
    fun forzarDesconexion(activa: Boolean)
    fun rechazarProximoComando()
    fun fijarBateria(pct: Int)
    fun inyectarObstaculo()
    fun inyectarAnomalia()
    fun fijarVelocidad(factor: Int)             // 1, 2 o 4
    fun reiniciar()
}
