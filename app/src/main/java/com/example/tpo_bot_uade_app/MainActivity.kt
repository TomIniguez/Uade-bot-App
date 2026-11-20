package com.example.tpo_bot_uade_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tpo_bot_uade_app.ui.navegacion.AppNavegacion
import com.example.tpo_bot_uade_app.ui.robot.RobotViewModel
import com.example.tpo_bot_uade_app.ui.theme.TPO_Bot_UADE_AppTheme

// Solo arranca la UI: crea el ViewModel y le pasa el estado a la navegacion.
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
                AppNavegacion(uiState, viewModel::onEvento)
            }
        }
    }
}
