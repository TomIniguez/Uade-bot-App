package com.example.tpo_bot_uade_app.ui.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tpo_bot_uade_app.domain.HistorialRepository
import com.example.tpo_bot_uade_app.domain.model.RegistroEvento
import com.example.tpo_bot_uade_app.domain.model.RegistroMision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PestanaHistorial { MISIONES, EVENTOS }

data class HistorialUiState(
    val cargando: Boolean = true,
    val pestana: PestanaHistorial = PestanaHistorial.MISIONES,
    val misiones: List<RegistroMision> = emptyList(),
    val eventos: List<RegistroEvento> = emptyList(),
    val misionSeleccionada: RegistroMision? = null,
    val confirmarBorrado: Boolean = false
) {
    // Eventos que ocurrieron durante la mision seleccionada (para el detalle).
    val eventosDeLaMision: List<RegistroEvento>
        get() = misionSeleccionada?.let { m ->
            // Margen de 2 s al final: el evento "Mision completada" se guarda apenas despues del fin.
            eventos.filter { it.robotId == m.robotId && it.timestampMs in m.inicioMs..(m.finMs + 2_000) }
        } ?: emptyList()
}

sealed class EventoHistorial {
    data class CambiarPestana(val pestana: PestanaHistorial) : EventoHistorial()
    data class VerMision(val mision: RegistroMision) : EventoHistorial()
    data object CerrarDetalle : EventoHistorial()
    data object PedirBorrado : EventoHistorial()
    data object ConfirmarBorrado : EventoHistorial()
    data object CancelarBorrado : EventoHistorial()
}

// ViewModel chico y separado: solo lee y borra el historial guardado en el telefono.
class HistorialViewModel(private val historial: HistorialRepository) : ViewModel() {

    // Lo que elige el usuario en la pantalla (pestana, mision abierta, dialogo de borrado).
    private val seleccion = MutableStateFlow(HistorialUiState())

    val uiState: StateFlow<HistorialUiState> =
        combine(seleccion, historial.misiones, historial.eventos) { sel, misiones, eventos ->
            sel.copy(cargando = false, misiones = misiones, eventos = eventos)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorialUiState())

    fun onEvento(evento: EventoHistorial) {
        when (evento) {
            is EventoHistorial.CambiarPestana -> seleccion.update { it.copy(pestana = evento.pestana) }
            is EventoHistorial.VerMision -> seleccion.update { it.copy(misionSeleccionada = evento.mision) }
            EventoHistorial.CerrarDetalle -> seleccion.update { it.copy(misionSeleccionada = null) }
            EventoHistorial.PedirBorrado -> seleccion.update { it.copy(confirmarBorrado = true) }
            EventoHistorial.CancelarBorrado -> seleccion.update { it.copy(confirmarBorrado = false) }
            EventoHistorial.ConfirmarBorrado -> {
                seleccion.update { it.copy(confirmarBorrado = false, misionSeleccionada = null) }
                viewModelScope.launch { historial.borrarTodo() }
            }
        }
    }
}
