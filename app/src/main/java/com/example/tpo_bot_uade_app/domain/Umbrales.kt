package com.example.tpo_bot_uade_app.domain

// Umbrales que necesitan a la vez la interfaz (colores, botones) y el simulador.
// Viven en el dominio porque la UI no puede depender de la capa de datos.
object Umbrales {
    const val BATERIA_MINIMA_INICIO = 30       // por debajo no se puede iniciar una mision
    const val BATERIA_BAJA = 20                // alerta de advertencia
    const val BATERIA_CRITICA = 10             // alerta critica y regreso automatico
    const val UMBRAL_TEMPERATURA = 45f         // por encima, anomalia termica
    const val DATOS_ANTIGUOS_MS = 3000L        // telemetria mas vieja que esto se marca como antigua
}
