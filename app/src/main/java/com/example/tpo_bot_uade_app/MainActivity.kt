package com.example.tpo_bot_uade_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tpo_bot_uade_app.ui.ConexionScreen
import com.example.tpo_bot_uade_app.ui.ControlScreen
import com.example.tpo_bot_uade_app.ui.RobotViewModel
import com.example.tpo_bot_uade_app.ui.theme.TPO_Bot_UADE_AppTheme

// Sólo arranca la UI: elige la pantalla según si hay sesión.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TPO_Bot_UADE_AppTheme {
                val viewModel: RobotViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (uiState.conectado) {
                        ControlScreen(
                            uiState = uiState,
                            onDesconectar = viewModel::desconectar,
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        ConexionScreen(
                            uiState = uiState,
                            onCambiarIp = viewModel::cambiarIp,
                            onCambiarEquipo = viewModel::cambiarEquipo,
                            onConectar = viewModel::conectar,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
