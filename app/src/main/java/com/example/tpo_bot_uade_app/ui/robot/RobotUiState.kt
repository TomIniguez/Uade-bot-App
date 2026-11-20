package com.example.tpo_bot_uade_app.ui.robot

import com.example.tpo_bot_uade_app.domain.Habilitacion
import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.DefinicionMision
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.Plano
import com.example.tpo_bot_uade_app.domain.model.RegistroComando
import com.example.tpo_bot_uade_app.domain.model.RobotInfo
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.domain.model.TipoComando

// Todo lo que las pantallas necesitan para dibujarse. Las pantallas solo leen esto.
data class RobotUiState(
    val robots: List<RobotInfo> = emptyList(),
    val misiones: List<DefinicionMision> = emptyList(),
    val plano: Plano? = null,
    val robotConectado: RobotInfo? = null,
    val conectando: String? = null,                // id del robot al que se esta conectando
    val telemetria: Telemetria? = null,
    val datosAntiguos: Boolean = false,
    val segundosDesdeActualizacion: Int = 0,
    val alertas: List<Alerta> = emptyList(),
    val ultimoComando: RegistroComando? = null,
    val comandosRecientes: List<RegistroComando> = emptyList(),   // ultimos 5, el mas nuevo primero
    val habilitacion: Map<TipoComando, Habilitacion> = emptyMap(),
    val dialogo: DialogoUi? = null                 // confirmacion abierta, si hay
) {
    // true si esta orden es la que esta esperando respuesta (para mostrar el boton "cargando").
    fun estaPendiente(tipo: TipoComando): Boolean =
        comandosRecientes.any { it.tipo == tipo && it.estado == EstadoComando.PENDIENTE }

    fun habilitacionDe(tipo: TipoComando): Habilitacion = habilitacion[tipo] ?: Habilitacion(false)

    // La definicion de la mision en curso (para el mini mapa y la lista de puntos).
    val definicionMisionActual: DefinicionMision?
        get() = telemetria?.mision?.let { m -> misiones.firstOrNull { it.id == m.id } }
}

// Lo que la UI le puede pedir al ViewModel. Es la unica forma de "hablarle".
sealed class EventoUi {
    data class SeleccionarRobot(val robotId: String) : EventoUi()
    data object Desconectar : EventoUi()                         // abre el dialogo de confirmacion
    data class PedirComando(val comando: Comando) : EventoUi()   // abre dialogo si corresponde
    data object ConfirmarDialogo : EventoUi()
    data object CerrarDialogo : EventoUi()
}

// Dialogos de confirmacion que puede pedir el ViewModel.
sealed class DialogoUi {
    data class Confirmar(val comando: Comando) : DialogoUi()
    data object ConfirmarDesconexion : DialogoUi()
}
