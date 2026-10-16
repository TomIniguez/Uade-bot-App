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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.ui.componentes.PanelEstado

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
        Text(stringResource(R.string.conexion_titulo), style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = uiState.ip,
            onValueChange = onCambiarIp,
            label = { Text(stringResource(R.string.campo_ip)) },
            singleLine = true,
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.equipo,
            onValueChange = onCambiarEquipo,
            label = { Text(stringResource(R.string.campo_equipo)) },
            singleLine = true,
            enabled = !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onConectar,
            enabled = uiState.equipo.isNotBlank() && !uiState.cargando,
            modifier = Modifier.fillMaxWidth()
        ) { Text(stringResource(R.string.boton_conectar)) }

        if (uiState.cargando) CircularProgressIndicator()

        // Útil para ver si el robot está libre antes de conectar.
        PanelEstado(estado = uiState.estado, miEquipo = "")

        uiState.mensaje?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}
