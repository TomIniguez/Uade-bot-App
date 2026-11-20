package com.example.tpo_bot_uade_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tpo_bot_uade_app.ui.componentes.BarraSuperiorRobot
import com.example.tpo_bot_uade_app.ui.robot.EventoUi
import com.example.tpo_bot_uade_app.ui.robot.RobotViewModel
import com.example.tpo_bot_uade_app.ui.seleccion.SeleccionRobotScreen
import com.example.tpo_bot_uade_app.ui.supervision.SupervisionScreen
import com.example.tpo_bot_uade_app.ui.theme.TPO_Bot_UADE_AppTheme

// Solo arranca la UI. Por ahora elige pantalla segun haya un robot conectado;
// la navegacion completa (barra inferior, Misiones, Alertas...) llega en las fases 4 y 8.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Iconos claros en la barra de estado (el fondo de la app es oscuro).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        val container = (application as RobotApp).container
        setContent {
            TPO_Bot_UADE_AppTheme {
                // Un solo RobotViewModel para toda la Activity (se comparte entre pantallas).
                val viewModel: RobotViewModel = viewModel(factory = container.viewModelFactory)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val robot = uiState.robotConectado

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (robot != null) {
                            BarraSuperiorRobot(
                                nombreRobot = robot.nombre,
                                telemetria = uiState.telemetria,
                                datosAntiguos = uiState.datosAntiguos,
                                segundos = uiState.segundosDesdeActualizacion,
                                onDesconectar = { viewModel.onEvento(EventoUi.Desconectar) }
                            )
                        }
                    }
                ) { innerPadding ->
                    if (robot == null) {
                        SeleccionRobotScreen(uiState, viewModel::onEvento, Modifier.padding(innerPadding))
                    } else {
                        SupervisionScreen(uiState, Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}
