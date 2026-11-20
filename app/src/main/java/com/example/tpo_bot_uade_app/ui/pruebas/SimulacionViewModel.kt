package com.example.tpo_bot_uade_app.ui.pruebas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tpo_bot_uade_app.domain.ControlSimulacion
import com.example.tpo_bot_uade_app.domain.RobotRepository
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Lo que muestra el panel de pruebas.
data class SimulacionUiState(
    val desconexionForzada: Boolean = false,
    val velocidad: Int = 1,
    val bateriaPct: Int = 0,
    val enMision: Boolean = false,                // obstaculo y anomalia solo tienen sentido en mision
    val aviso: AvisoPrueba? = null                // confirmacion corta de la ultima accion
)

enum class AvisoPrueba { RECHAZO_ARMADO, BATERIA_FIJADA, OBSTACULO, ANOMALIA, REINICIADA }

sealed class EventoPrueba {
    data class ForzarDesconexion(val activa: Boolean) : EventoPrueba()
    data object RechazarProxima : EventoPrueba()
    data class FijarBateria(val pct: Int) : EventoPrueba()
    data object InyectarObstaculo : EventoPrueba()
    data object InyectarAnomalia : EventoPrueba()
    data class FijarVelocidad(val factor: Int) : EventoPrueba()
    data object Reiniciar : EventoPrueba()
}

// ViewModel chico y separado: es el unico que ve ControlSimulacion.
// Las pantallas de operacion (Supervision, Misiones...) nunca pueden forzar fallas.
class SimulacionViewModel(
    private val control: ControlSimulacion,
    repo: RobotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimulacionUiState())
    val uiState: StateFlow<SimulacionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.telemetria.collect { t ->
                // Con desconexion forzada la telemetria queda congelada: no pisamos el estado del panel.
                if (t != null) _uiState.update {
                    it.copy(bateriaPct = t.bateriaPct, enMision = t.estado == EstadoOperativo.EN_MISION)
                }
            }
        }
    }

    fun onEvento(evento: EventoPrueba) {
        when (evento) {
            is EventoPrueba.ForzarDesconexion -> {
                control.forzarDesconexion(evento.activa)
                _uiState.update { it.copy(desconexionForzada = evento.activa, aviso = null) }
            }
            EventoPrueba.RechazarProxima -> {
                control.rechazarProximoComando()
                avisar(AvisoPrueba.RECHAZO_ARMADO)
            }
            is EventoPrueba.FijarBateria -> {
                control.fijarBateria(evento.pct)
                avisar(AvisoPrueba.BATERIA_FIJADA)
            }
            EventoPrueba.InyectarObstaculo -> {
                control.inyectarObstaculo()
                avisar(AvisoPrueba.OBSTACULO)
            }
            EventoPrueba.InyectarAnomalia -> {
                control.inyectarAnomalia()
                avisar(AvisoPrueba.ANOMALIA)
            }
            is EventoPrueba.FijarVelocidad -> {
                control.fijarVelocidad(evento.factor)
                _uiState.update { it.copy(velocidad = evento.factor) }
            }
            EventoPrueba.Reiniciar -> {
                control.reiniciar()
                _uiState.update { it.copy(desconexionForzada = false, velocidad = 1, aviso = AvisoPrueba.REINICIADA) }
            }
        }
    }

    private fun avisar(aviso: AvisoPrueba) = _uiState.update { it.copy(aviso = aviso) }
}
