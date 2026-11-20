package com.example.tpo_bot_uade_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tpo_bot_uade_app.ui.historial.HistorialViewModel
import com.example.tpo_bot_uade_app.ui.navegacion.AppNavegacion
import com.example.tpo_bot_uade_app.ui.pruebas.SimulacionViewModel
import com.example.tpo_bot_uade_app.ui.robot.RobotViewModel
import com.example.tpo_bot_uade_app.ui.theme.TPO_Bot_UADE_AppTheme

// Solo arranca la UI: crea los ViewModels y le pasa su estado a la navegacion.
class MainActivity : ComponentActivity() {

    private val container by lazy { (application as RobotApp).container }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Iconos claros en la barra de estado (el fondo de la app es oscuro).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            TPO_Bot_UADE_AppTheme {
                // Los tres ViewModels son de la Activity (se comparten entre pantallas).
                val robotVm: RobotViewModel = viewModel(factory = container.viewModelFactory)
                val simulacionVm: SimulacionViewModel = viewModel(factory = container.viewModelFactory)
                val historialVm: HistorialViewModel = viewModel(factory = container.viewModelFactory)

                val uiState by robotVm.uiState.collectAsStateWithLifecycle()
                val simulacion by simulacionVm.uiState.collectAsStateWithLifecycle()
                val historial by historialVm.uiState.collectAsStateWithLifecycle()

                AppNavegacion(
                    uiState, robotVm::onEvento,
                    simulacion, simulacionVm::onEvento,
                    historial, historialVm::onEvento
                )
            }
        }
    }

    // Riesgo R3: si la app pasa a segundo plano con una mision activa, queda en el historial.
    // (El robot simulado sigue trabajando; la app simplemente deja de mirarlo.)
    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) container.registrador.appEnSegundoPlano()
    }
}
