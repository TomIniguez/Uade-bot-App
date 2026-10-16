package com.example.tpo_bot_uade_app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.data.Resultado
import com.example.tpo_bot_uade_app.data.RobotRepository
import com.example.tpo_bot_uade_app.data.crearApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// Guarda el estado de la app y sobrevive a la rotación de pantalla.
private const val MAX_FALLOS_DE_RED = 3

class RobotViewModel(application: Application) : AndroidViewModel(application) {

    // Atajo para leer textos de strings.xml desde el ViewModel.
    private fun texto(id: Int) = getApplication<Application>().getString(id)

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
            val nuevoRepo = try {
                RobotRepository(crearApi(estado.ip), getApplication())
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(cargando = false, mensaje = texto(R.string.error_ip), mensajeEsError = true) }
                return@launch
            }

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

    // Mientras el dedo esté apretado repetimos /mover cada 200 ms (el backend frena solo a los ~0.4 s).
    private var trabajoMover: Job? = null

    fun empezarMover(movimiento: Movimiento) {
        val repoActual = repo ?: return
        trabajoMover?.cancel() // un solo movimiento a la vez
        trabajoMover = viewModelScope.launch {
            var fallosDeRed = 0
            while (isActive) {
                when (val r = repoActual.mover(movimiento.vx, movimiento.vy, movimiento.vyaw)) {
                    is Resultado.Ok -> {
                        fallosDeRed = 0
                        val recortado = r.datos.recortado == true
                        _uiState.update {
                            it.copy(
                                ultimoRecorte = recortado,
                                mensaje = if (recortado) r.datos.mensaje else null,
                                mensajeEsError = false // el recorte es un aviso, no un error
                            )
                        }
                    }
                    is Resultado.Error -> {
                        if (r.codigo == 401) {
                            sesionVencida()
                            return@launch
                        }
                        // codigo == null: no hubo respuesta. Con 3 seguidos cortamos el loop.
                        if (r.codigo == null) fallosDeRed++ else fallosDeRed = 0
                        if (fallosDeRed >= MAX_FALLOS_DE_RED) {
                            _uiState.update { it.copy(mensaje = texto(R.string.error_perdio_conexion), mensajeEsError = true) }
                            trabajoMover = null
                            return@launch
                        }
                        _uiState.update { it.copy(mensaje = r.mensaje, mensajeEsError = true) }
                    }
                }
                delay(200)
            }
        }
    }

    // Al soltar: cortamos el loop y le pedimos al robot que pare.
    fun soltar() {
        trabajoMover?.cancel()
        trabajoMover = null
        val repoActual = repo ?: return
        viewModelScope.launch {
            val r = repoActual.parar()
            if (r is Resultado.Error && r.codigo == 401) sesionVencida()
        }
    }

    // Ejecuta una acción del robot (saludo, dar_la_mano...). Un 403 se muestra tal cual.
    fun hacerAccion(nombre: String) {
        val repoActual = repo ?: return
        viewModelScope.launch {
            when (val r = repoActual.accion(nombre)) {
                is Resultado.Ok -> _uiState.update {
                    it.copy(mensaje = r.datos.mensaje ?: texto(R.string.aviso_accion_ok), mensajeEsError = false)
                }
                is Resultado.Error -> {
                    if (r.codigo == 401) sesionVencida()
                    else _uiState.update { it.copy(mensaje = r.mensaje, mensajeEsError = true) }
                }
            }
        }
    }

    // Polling de /estado cada 2 s. Funciona con o sin sesión (/estado no pide token),
    // así también se ve si el robot está libre antes de conectar.
    private var trabajoEstado: Job? = null
    private var repoLectura: RobotRepository? = null
    private var ipLectura: String? = null

    fun empezarPolling() {
        if (trabajoEstado?.isActive == true) return
        trabajoEstado = viewModelScope.launch {
            while (isActive) {
                val lector = repo ?: repoDeLectura(_uiState.value.ip)
                val r = lector?.estado()
                _uiState.update { it.copy(estado = (r as? Resultado.Ok)?.datos) }
                delay(2000)
            }
        }
    }

    // Sin sesión usamos un repositorio sólo de lectura, que se recrea si cambia la IP.
    // Devuelve null si la IP escrita todavía no es una URL válida.
    private fun repoDeLectura(ip: String): RobotRepository? {
        if (ip != ipLectura) {
            repoLectura = try {
                RobotRepository(crearApi(ip), getApplication())
            } catch (e: IllegalArgumentException) {
                null
            }
            ipLectura = ip
        }
        return repoLectura
    }

    fun detenerPolling() {
        trabajoEstado?.cancel()
        trabajoEstado = null
    }

    override fun onCleared() {
        trabajoMover?.cancel()
        trabajoEstado?.cancel()
    }

    // El token ya no sirve: volvemos a la pantalla de conexión.
    private fun sesionVencida() {
        trabajoMover?.cancel()
        trabajoMover = null
        repo?.olvidarToken()
        repo = null
        _uiState.update {
            it.copy(conectado = false, acciones = emptyList(), estado = null, mensaje = texto(R.string.error_sesion_vencida))
        }
    }

    fun desconectar() {
        trabajoMover?.cancel()
        trabajoMover = null
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
