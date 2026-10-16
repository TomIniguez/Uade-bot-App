package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.data.EstadoResponse

private const val BATERIA_BAJA = 25

// Muestra lo que devuelve /estado. "miEquipo" sirve para avisar si otro equipo tiene el control.
@Composable
fun PanelEstado(estado: EstadoResponse?, miEquipo: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (estado == null) {
                Text(stringResource(R.string.estado_sin_datos), color = MaterialTheme.colorScheme.error)
                return@Column
            }

            val desconocido = stringResource(R.string.estado_desconocido)
            Text(stringResource(R.string.estado_robot, estado.nombre_robot ?: desconocido, estado.tipo_robot ?: desconocido))
            Text(
                stringResource(if (estado.conectado) R.string.estado_robot_conectado else R.string.estado_robot_desconectado),
                color = if (estado.conectado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )

            val bateria = estado.bateria
            if (bateria != null) {
                val color = if (bateria < BATERIA_BAJA) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
                Text(stringResource(R.string.estado_bateria, bateria))
                LinearProgressIndicator(
                    progress = { bateria.coerceIn(0, 100) / 100f },
                    color = color,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text(stringResource(R.string.estado_control, estado.equipo_activo ?: stringResource(R.string.estado_control_nadie)))
            val activo = estado.equipo_activo
            if (miEquipo.isNotBlank() && activo != null && activo != miEquipo.trim()) {
                Text(
                    stringResource(R.string.estado_otro_equipo, activo),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
