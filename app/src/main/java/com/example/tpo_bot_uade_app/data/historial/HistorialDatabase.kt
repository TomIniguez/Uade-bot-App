package com.example.tpo_bot_uade_app.data.historial

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// La base de datos del telefono (archivo "historial.db" dentro de la app).
@Database(entities = [MisionEntity::class, EventoEntity::class], version = 1, exportSchema = false)
abstract class HistorialDatabase : RoomDatabase() {

    abstract fun dao(): HistorialDao

    companion object {
        fun crear(context: Context): HistorialDatabase =
            Room.databaseBuilder(context, HistorialDatabase::class.java, "historial.db").build()
    }
}
