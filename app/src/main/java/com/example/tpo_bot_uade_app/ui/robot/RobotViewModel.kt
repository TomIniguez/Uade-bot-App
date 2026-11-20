package com.example.tpo_bot_uade_app.ui.robot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tpo_bot_uade_app.domain.ContextoComando
import com.example.tpo_bot_uade_app.domain.ReglasComando
import com.example.tpo_bot_uade_app.domain.Umbrales
import com.example.tpo_bot_uade_app.domain.RobotRepository
import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.EstadoConexion
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Guarda el estado de las pantallas y sobrevive a la rotacion.
// Solo conoce la interfaz RobotRepository: no sabe si del otro lado hay un simulador o un robot real.
class RobotViewModel(
    private val repo: RobotRepository,
    private val reloj: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _uiState = MutableStateFlow(RobotUiState(robots = repo.robots, misiones = repo.misiones))
    val uiState: StateFlow<RobotUiState> = _uiState.asStateFlow()

    // Late una vez por segundo para que "hace N s" avance aunque no llegue telemetria nueva.
    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(1000)
        }
    }

    init {
        viewModelScope.launch {
            combine(repo.telemetria, repo.alertas, ticker) { telemetria, alertas, _ -> telemetria to alertas }
                .collect { (telemetria, alertas) -> recalcular(telemetria, alertas) }
        }
    }

    // Unica entrada publica para la UI.
    fun onEvento(evento: EventoUi) {
        when (evento) {
            is EventoUi.SeleccionarRobot -> conectar(evento.robotId)
            EventoUi.Desconectar -> desconectar()
        }
    }

    private fun conectar(robotId: String) {
        if (_uiState.value.conectando != null) return
        _uiState.update { it.copy(conectando = robotId) }
        viewModelScope.launch {
            val ok = repo.conectar(robotId)
            _uiState.update {
                it.copy(
                    conectando = null,
                    robotConectado = if (ok) repo.robots.first { r -> r.id == robotId } else null
                )
            }
        }
    }

    private fun desconectar() {
        viewModelScope.launch {
            repo.desconectar()
            _uiState.update { it.copy(robotConectado = null) }
        }
    }

    // Calcula lo "derivado": si los datos estan viejos y que botones se pueden usar.
    private fun recalcular(telemetria: Telemetria?, alertas: List<Alerta>) {
        val edadMs = if (telemetria == null) 0L else (reloj() - telemetria.timestampMs).coerceAtLeast(0L)
        val antiguos = telemetria != null &&
            (telemetria.conexion == EstadoConexion.SIN_CONEXION || edadMs > Umbrales.DATOS_ANTIGUOS_MS)
        val contexto = ContextoComando(telemetria, antiguos, hayComandoPendiente = false, alertas = alertas)
        _uiState.update {
            it.copy(
                telemetria = telemetria,
                alertas = alertas,
                datosAntiguos = antiguos,
                segundosDesdeActualizacion = (edadMs / 1000).toInt(),
                habilitacion = ReglasComando.todas(contexto)
            )
        }
    }
}
