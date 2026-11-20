package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.ui.theme.Superficie

// Nombre del robot, conexion, bateria y boton Desconectar. (El icono de Pruebas llega en la fase 5.)
@Composable
fun BarraSuperiorRobot(
    nombreRobot: String,
    telemetria: Telemetria?,
    datosAntiguos: Boolean,
    segundos: Int,
    onDesconectar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Superficie)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(nombreRobot, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipConexion(telemetria, datosAntiguos, segundos)
                telemetria?.let { IndicadorBateria(it.bateriaPct, antiguo = datosAntiguos) }
            }
        }
        IconButton(onClick = onDesconectar) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = stringResource(R.string.boton_desconectar))
        }
    }
}
