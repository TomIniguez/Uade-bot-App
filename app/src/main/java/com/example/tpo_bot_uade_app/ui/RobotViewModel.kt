package com.example.tpo_bot_uade_app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tpo_bot_uade_app.data.Resultado
import com.example.tpo_bot_uade_app.data.RobotRepository
import com.example.tpo_bot_uade_app.data.crearApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Guarda el estado de la app y sobrevive a la rotación de pantalla.
class RobotViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Se crea al conectar, porque depende de la IP que escribió el usuario.
    private var repo: RobotRepository? = null

    fun cambiarIp(ip: String) = _uiState.update { it.copy(ip = ip) }

    fun cambiarEquipo(equipo: String) = _uiState.update { it.copy(equipo = equipo) }

    fun conectar() {
        val estado = _uiState.value
        if (estado.cargando || estado.equipo.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, mensaje = null) }
            val nuevoRepo = RobotRepository(crearApi(estado.ip))

            when (val sesion = nuevoRepo.iniciarSesion(estado.equipo.trim())) {
                is Resultado.Error -> {
                    _uiState.update { it.copy(cargando = false, mensaje = sesion.mensaje) }
                }
                is Resultado.Ok -> {
                    repo = nuevoRepo
                    // Si /acciones falla no es grave: la lista queda vacía.
                    val acciones = (nuevoRepo.acciones() as? Resultado.Ok)?.datos?.acciones.orEmpty()
                    _uiState.update {
                        it.copy(cargando = false, conectado = true, acciones = acciones)
                    }
                }
            }
        }
    }

    fun desconectar() {
        val repoActual = repo ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            // Primero frenamos al robot, después liberamos la sesión.
            repoActual.parar()
            repoActual.finalizarSesion()
            repoActual.olvidarToken()
            repo = null
            _uiState.update {
                it.copy(cargando = false, conectado = false, acciones = emptyList(), estado = null)
            }
        }
    }
}
