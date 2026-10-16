package com.example.tpo_bot_uade_app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Pantalla 2: por ahora vacía. Los botones de movimiento llegan en la Fase 4.
@Composable
fun ControlScreen(
    uiState: UiState,
    onDesconectar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Conectado como ${uiState.equipo}")
        Text("Acciones disponibles: ${uiState.acciones.size}")
        Button(onClick = onDesconectar, enabled = !uiState.cargando) { Text("Desconectar") }
    }
}
