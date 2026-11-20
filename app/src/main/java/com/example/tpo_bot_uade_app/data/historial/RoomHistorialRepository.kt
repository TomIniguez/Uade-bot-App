package com.example.tpo_bot_uade_app.data.historial

import com.example.tpo_bot_uade_app.domain.HistorialRepository
import com.example.tpo_bot_uade_app.domain.model.CategoriaEvento
import com.example.tpo_bot_uade_app.domain.model.EstadoMision
import com.example.tpo_bot_uade_app.domain.model.RegistroEvento
import com.example.tpo_bot_uade_app.domain.model.RegistroMision
import com.example.tpo_bot_uade_app.domain.model.Severidad
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Implementa HistorialRepository con Room. Convierte entidades <-> modelos del dominio,
// asi el resto de la app no sabe que existe Room.
class RoomHistorialRepository(private val dao: HistorialDao) : HistorialRepository {

    override val misiones: Flow<List<RegistroMision>> = dao.misiones().map { lista -> lista.map { it.aDominio() } }

    override val eventos: Flow<List<RegistroEvento>> = dao.eventos().map { lista -> lista.map { it.aDominio() } }

    override suspend fun guardarMision(registro: RegistroMision) = dao.insertarMision(registro.aEntidad())

    override suspend fun guardarEvento(registro: RegistroEvento) = dao.insertarEvento(registro.aEntidad())

    override suspend fun borrarTodo() {
        dao.borrarMisiones()
        dao.borrarEventos()
    }
}

// ---------- Conversiones ----------

private fun MisionEntity.aDominio() = RegistroMision(
    id = id, misionId = misionId, nombre = nombre, robotId = robotId, inicioMs = inicioMs, finMs = finMs,
    resultado = EstadoMision.valueOf(resultado), puntosInspeccionados = puntosInspeccionados,
    totalPuntos = totalPuntos, anomalias = anomalias, bateriaInicio = bateriaInicio, bateriaFin = bateriaFin
)

private fun RegistroMision.aEntidad() = MisionEntity(
    misionId = misionId, nombre = nombre, robotId = robotId, inicioMs = inicioMs, finMs = finMs,
    resultado = resultado.name, puntosInspeccionados = puntosInspeccionados, totalPuntos = totalPuntos,
    anomalias = anomalias, bateriaInicio = bateriaInicio, bateriaFin = bateriaFin
)

private fun EventoEntity.aDominio() = RegistroEvento(
    id = id, timestampMs = timestampMs, categoria = CategoriaEvento.valueOf(categoria),
    severidad = Severidad.valueOf(severidad), titulo = titulo, detalle = detalle, robotId = robotId
)

private fun RegistroEvento.aEntidad() = EventoEntity(
    timestampMs = timestampMs, categoria = categoria.name, severidad = severidad.name,
    titulo = titulo, detalle = detalle, robotId = robotId
)
