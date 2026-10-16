package com.example.tpo_bot_uade_app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Pantalla 1: IP del backend + nombre del equipo.
@Composable
fun ConexionScreen(
    uiState: UiState,
    onCambiarIp: (String) -> Unit,
    onCambiarEquipo: (String) -> Unit,
    onConectar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Control del robot", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = uiState.ip,
            onValueChange = onCambiarIp,
            label = { Text("IP:puerto del backend") },
            singleLine = true,
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.equipo,
            onValueChange = onCambiarEquipo,
            label = { Text("Nombre del equipo") },
            singleLine = true,
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onConectar,
            enabled = uiState.equipo.isNotBlank() && !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Conectar") }

        if (uiState.cargando) CircularProgressIndicator()

        uiState.mensaje?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
