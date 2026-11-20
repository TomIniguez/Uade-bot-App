package com.example.tpo_bot_uade_app.ui.navegacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.TipoComando
import com.example.tpo_bot_uade_app.ui.componentes.BarraInferior
import com.example.tpo_bot_uade_app.ui.componentes.BarraSuperiorRobot
import com.example.tpo_bot_uade_app.ui.componentes.BotonDetencion
import com.example.tpo_bot_uade_app.ui.componentes.Destino
import com.example.tpo_bot_uade_app.ui.componentes.DialogoConfirmacion
import com.example.tpo_bot_uade_app.ui.misiones.MisionesScreen
import com.example.tpo_bot_uade_app.ui.robot.DialogoUi
import com.example.tpo_bot_uade_app.ui.robot.EventoUi
import com.example.tpo_bot_uade_app.ui.robot.RobotUiState
import com.example.tpo_bot_uade_app.ui.seleccion.SeleccionRobotScreen
import com.example.tpo_bot_uade_app.ui.supervision.SupervisionScreen
import com.example.tpo_bot_uade_app.ui.theme.Fondo

// Arma la estructura de la app: barra superior, contenido segun la ruta, Detencion operativa
// fija y barra inferior. Rutas como texto ("supervision", "misiones") para que sea facil de leer.
@Composable
fun AppNavegacion(uiState: RobotUiState, onEvento: (EventoUi) -> Unit) {
    val robot = uiState.robotConectado
    if (robot == null) {
        Scaffold(Modifier.fillMaxSize()) { padding ->
            SeleccionRobotScreen(uiState, onEvento, Modifier.padding(padding))
        }
        return
    }

    val navController = rememberNavController()
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route

    fun ir(ruta: String) = navController.navigate(ruta) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            BarraSuperiorRobot(
                nombreRobot = robot.nombre,
                telemetria = uiState.telemetria,
                datosAntiguos = uiState.datosAntiguos,
                segundos = uiState.segundosDesdeActualizacion,
                onDesconectar = { onEvento(EventoUi.Desconectar) }
            )
        },
        bottomBar = {
            Column {
                // Siempre visible y a un toque, encima de la barra inferior.
                BotonDetencion(
                    habilitacion = uiState.habilitacionDe(TipoComando.DETENCION_OPERATIVA),
                    pendiente = uiState.estaPendiente(TipoComando.DETENCION_OPERATIVA),
                    telemetria = uiState.telemetria,
                    onClick = { onEvento(EventoUi.PedirComando(Comando.DetencionOperativa)) },
                    modifier = Modifier.background(Fondo).padding(horizontal = 16.dp, vertical = 8.dp)
                )
                BarraInferior(rutaActual) { ir(it.ruta) }
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = Destino.SUPERVISION.ruta, modifier = Modifier.padding(padding)) {
            composable(Destino.SUPERVISION.ruta) { SupervisionScreen(uiState, onEvento) }
            composable(Destino.MISIONES.ruta) { MisionesScreen(uiState, onEvento) }
        }
    }

    // Dialogos de confirmacion (los decide el ViewModel; aca solo se dibujan).
    uiState.dialogo?.let { dialogo ->
        Dialogos(dialogo, uiState, onEvento, onIniciarConfirmado = { ir(Destino.SUPERVISION.ruta) })
    }
}

@Composable
private fun Dialogos(
    dialogo: DialogoUi,
    uiState: RobotUiState,
    onEvento: (EventoUi) -> Unit,
    onIniciarConfirmado: () -> Unit
) {
    val cerrar = { onEvento(EventoUi.CerrarDialogo) }
    when (dialogo) {
        DialogoUi.ConfirmarDesconexion -> DialogoConfirmacion(
            titulo = stringResource(R.string.dlg_desconectar_titulo),
            texto = stringResource(R.string.dlg_desconectar_texto),
            textoConfirmar = stringResource(R.string.boton_desconectar),
            onConfirmar = { onEvento(EventoUi.ConfirmarDialogo) },
            onCancelar = cerrar
        )
        is DialogoUi.Confirmar -> when (val c = dialogo.comando) {
            is Comando.IniciarMision -> {
                val mision = uiState.misiones.firstOrNull { it.id == c.misionId }
                DialogoConfirmacion(
                    titulo = stringResource(R.string.dlg_iniciar_titulo, mision?.nombre ?: c.misionId),
                    texto = (mision?.puntos?.size ?: 0).let { n -> pluralStringResource(R.plurals.dlg_iniciar_texto, n, n) },
                    textoConfirmar = stringResource(R.string.boton_iniciar),
                    onConfirmar = {
                        onEvento(EventoUi.ConfirmarDialogo)
                        onIniciarConfirmado() // Figma: confirmar inicio lleva a Supervision
                    },
                    onCancelar = cerrar
                )
            }
            Comando.CancelarMision -> DialogoConfirmacion(
                titulo = stringResource(R.string.dlg_cancelar_titulo),
                texto = stringResource(R.string.dlg_cancelar_texto),
                textoConfirmar = stringResource(R.string.dlg_cancelar_confirmar),
                onConfirmar = { onEvento(EventoUi.ConfirmarDialogo) },
                onCancelar = cerrar,
                destructivo = true
            )
            Comando.RegresarAPuntoSeguro -> DialogoConfirmacion(
                titulo = stringResource(R.string.dlg_regresar_titulo),
                texto = stringResource(
                    if (uiState.datosAntiguos) R.string.dlg_regresar_texto_sin_conexion else R.string.dlg_regresar_texto
                ),
                textoConfirmar = stringResource(R.string.cmd_regresar),
                onConfirmar = { onEvento(EventoUi.ConfirmarDialogo) },
                onCancelar = cerrar
            )
            else -> cerrar() // el resto no pide confirmacion
        }
    }
}
