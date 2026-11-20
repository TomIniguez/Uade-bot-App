package com.example.tpo_bot_uade_app.ui.alertas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.ui.componentes.AccionesAlerta
import com.example.tpo_bot_uade_app.ui.componentes.TarjetaAlerta
import com.example.tpo_bot_uade_app.ui.robot.EventoUi
import com.example.tpo_bot_uade_app.ui.robot.RobotUiState
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

// Alertas activas (arriba) y reconocidas (abajo), la mas nueva primero.
@Composable
fun AlertasScreen(uiState: RobotUiState, onEvento: (EventoUi) -> Unit, modifier: Modifier = Modifier) {
    val activas = uiState.alertasActivas
    val reconocidas = uiState.alertasReconocidas

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(stringResource(R.string.alertas_activas), style = MaterialTheme.typography.titleLarge) }
        if (activas.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.alertas_vacio),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
        items(activas, key = { it.id }) { alerta ->
            TarjetaAlerta(alerta) { AccionesAlerta(alerta, uiState, onEvento) }
        }

        if (reconocidas.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.alertas_reconocidas),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(reconocidas, key = { it.id }) { alerta -> TarjetaAlerta(alerta) }
        }
    }
}
