package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.Umbrales
import com.example.tpo_bot_uade_app.domain.model.RobotInfo
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

// Variantes: disponible / bateria baja / fuera de linea (deshabilitado).
@Composable
fun TarjetaRobot(
    robot: RobotInfo,
    conectando: Boolean,
    onConectar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val bateriaBaja = robot.disponible && robot.bateriaPct < Umbrales.BATERIA_MINIMA_INICIO
    Card(modifier = modifier.fillMaxWidth().alpha(if (robot.disponible) 1f else 0.6f)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Pets, contentDescription = null, tint = TextoSecundario, modifier = Modifier.size(32.dp))
                Column(Modifier.weight(1f)) {
                    Text(robot.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.robot_modelo_id, robot.modelo, robot.id),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSecundario
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (robot.disponible) {
                    IndicadorBateria(robot.bateriaPct)
                    if (bateriaBaja) {
                        Text(
                            stringResource(R.string.robot_bateria_baja),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorDe(NivelVisual.ADVERTENCIA)
                        )
                    }
                } else {
                    ChipHmi(Icons.Outlined.CloudOff, stringResource(R.string.robot_fuera_de_linea), TextoSecundario)
                }
            }
            Button(
                onClick = onConectar,
                enabled = robot.disponible && habilitado && !conectando,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (conectando) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.boton_conectar))
                }
            }
            if (!robot.disponible) {
                Text(
                    stringResource(R.string.robot_motivo_fuera_de_linea),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextoSecundario
                )
            }
        }
    }
}
