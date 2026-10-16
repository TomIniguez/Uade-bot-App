package com.example.tpo_bot_uade_app.ui

import com.example.tpo_bot_uade_app.data.Accion
import com.example.tpo_bot_uade_app.data.EstadoResponse

// Todo lo que las pantallas necesitan para dibujarse.
data class UiState(
    val ip: String = "10.0.2.2:8000",
    val equipo: String = "",
    val conectado: Boolean = false,      // tenemos token
    val cargando: Boolean = false,
    val estado: EstadoResponse? = null,
    val acciones: List<Accion> = emptyList(),
    val mensaje: String? = null,         // aviso o error para el usuario
    val mensajeEsError: Boolean = true,  // false = aviso neutro (recorte, acción OK)
    val ultimoRecorte: Boolean = false
)
