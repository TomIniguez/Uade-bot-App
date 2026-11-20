package com.example.tpo_bot_uade_app.domain

import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.DefinicionMision
import com.example.tpo_bot_uade_app.domain.model.EventoRobot
import com.example.tpo_bot_uade_app.domain.model.Plano
import com.example.tpo_bot_uade_app.domain.model.RespuestaComando
import com.example.tpo_bot_uade_app.domain.model.RobotInfo
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

// Lo unico que el ViewModel sabe del robot. Hoy lo implementa el simulador;
// con un robot real se escribe otra clase que cumpla esta interfaz.
interface RobotRepository {
    val robots: List<RobotInfo>                 // se lee de nuevo cada vez: la bateria puede cambiar
    val misiones: List<DefinicionMision>
    val plano: Plano                            // puntos de control y su ubicacion (mini mapa)
    val telemetria: StateFlow<Telemetria?>      // null = todavia no conectado
    val alertas: StateFlow<List<Alerta>>
    val eventos: SharedFlow<EventoRobot>        // hechos puntuales para el historial

    suspend fun conectar(robotId: String): Boolean
    suspend fun desconectar()

    // El ViewModel genera el comandoId (para mostrar PENDIENTE antes de enviar) y el robot lo devuelve.
    suspend fun enviar(comandoId: String, comando: Comando): RespuestaComando
}
