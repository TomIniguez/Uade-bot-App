package com.example.tpo_bot_uade_app.ui.robot

import com.example.tpo_bot_uade_app.domain.Habilitacion
import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.DefinicionMision
import com.example.tpo_bot_uade_app.domain.model.RobotInfo
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.domain.model.TipoComando

// Todo lo que las pantallas necesitan para dibujarse. Las pantallas solo leen esto.
// (En la fase 4 se suman el ultimo comando, los recientes y los dialogos.)
data class RobotUiState(
    val robots: List<RobotInfo> = emptyList(),
    val misiones: List<DefinicionMision> = emptyList(),
    val robotConectado: RobotInfo? = null,
    val conectando: String? = null,                // id del robot al que se esta conectando
    val telemetria: Telemetria? = null,
    val datosAntiguos: Boolean = false,
    val segundosDesdeActualizacion: Int = 0,
    val alertas: List<Alerta> = emptyList(),
    val habilitacion: Map<TipoComando, Habilitacion> = emptyMap()
)

// Lo que la UI le puede pedir al ViewModel. Es la unica forma de "hablarle".
sealed class EventoUi {
    data class SeleccionarRobot(val robotId: String) : EventoUi()
    data object Desconectar : EventoUi()
}
