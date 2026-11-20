package com.example.tpo_bot_uade_app.ui.seleccion

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
import com.example.tpo_bot_uade_app.ui.componentes.TarjetaRobot
import com.example.tpo_bot_uade_app.ui.robot.EventoUi
import com.example.tpo_bot_uade_app.ui.robot.RobotUiState
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

// Primera pantalla: elegir con que Go2 trabajar.
@Composable
fun SeleccionRobotScreen(
    uiState: RobotUiState,
    onEvento: (EventoUi) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(R.string.seleccion_titulo),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        items(uiState.robots, key = { it.id }) { robot ->
            TarjetaRobot(
                robot = robot,
                conectando = uiState.conectando == robot.id,
                habilitado = uiState.conectando == null,
                onConectar = { onEvento(EventoUi.SeleccionarRobot(robot.id)) }
            )
        }
        item {
            Text(
                stringResource(R.string.seleccion_pie),
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
