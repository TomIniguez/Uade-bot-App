package com.example.tpo_bot_uade_app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.tpo_bot_uade_app.ui.componentes.Joystick
import com.example.tpo_bot_uade_app.ui.componentes.PanelEstado

// Pantalla 2: el control remoto. Joystick (adelante/atrás + girar) y botón PARAR.
@Composable
fun ControlScreen(
    uiState: UiState,
    onDesconectar: () -> Unit,
    onMover: (Movimiento) -> Unit,
    onSoltar: () -> Unit,
    onAccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val habilitado = !uiState.cargando

    // "Hombre muerto": si la app pasa a segundo plano (llamada, botón Home), el robot frena.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { onSoltar() }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(R.string.equipo_actual, uiState.equipo), style = MaterialTheme.typography.titleMedium)
            Button(onClick = onDesconectar, enabled = habilitado) { Text(stringResource(R.string.boton_desconectar)) }
        }

        PanelEstado(estado = uiState.estado, miEquipo = uiState.equipo)

        uiState.mensaje?.let {
            Text(
                it,
                color = if (uiState.mensajeEsError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.tertiary
            )
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Joystick(
                    onMover = onMover,
                    onSoltar = onSoltar,
                    habilitado = habilitado,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // PARAR es un botón normal: con un toque basta.
            Button(
                onClick = onSoltar,
                // PARAR queda siempre habilitado: es el botón de seguridad.
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) { Text(stringResource(R.string.boton_parar), style = MaterialTheme.typography.titleLarge) }
        }

        // Un botón por cada acción que informa el backend (el G1 trae más que el Go2).
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.acciones.forEach { accion ->
                Button(onClick = { onAccion(accion.nombre) }, enabled = habilitado) {
                    Text(accion.descripcion ?: accion.nombre)
                }
            }
        }
    }
}
