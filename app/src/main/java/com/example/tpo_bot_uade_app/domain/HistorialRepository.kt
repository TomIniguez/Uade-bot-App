package com.example.tpo_bot_uade_app.domain

import com.example.tpo_bot_uade_app.domain.model.RegistroEvento
import com.example.tpo_bot_uade_app.domain.model.RegistroMision
import kotlinx.coroutines.flow.Flow

// Guarda misiones y eventos en el telefono. Lo implementa Room (fase 7).
interface HistorialRepository {
    val misiones: Flow<List<RegistroMision>>
    val eventos: Flow<List<RegistroEvento>>
    suspend fun guardarMision(registro: RegistroMision)
    suspend fun guardarEvento(registro: RegistroEvento)
    suspend fun borrarTodo()
}
