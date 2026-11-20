package com.example.tpo_bot_uade_app.data.historial

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Consultas a la base. Room genera el codigo a partir de estas anotaciones (con KSP).
// Las que devuelven Flow se actualizan solas cuando cambia la tabla.
@Dao
interface HistorialDao {

    @Query("SELECT * FROM misiones ORDER BY finMs DESC")
    fun misiones(): Flow<List<MisionEntity>>

    @Query("SELECT * FROM eventos ORDER BY timestampMs DESC, id DESC")
    fun eventos(): Flow<List<EventoEntity>>

    @Insert
    suspend fun insertarMision(mision: MisionEntity)

    @Insert
    suspend fun insertarEvento(evento: EventoEntity)

    @Query("DELETE FROM misiones")
    suspend fun borrarMisiones()

    @Query("DELETE FROM eventos")
    suspend fun borrarEventos()
}
