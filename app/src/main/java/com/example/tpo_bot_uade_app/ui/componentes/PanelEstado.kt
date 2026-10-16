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
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.data.EstadoResponse

private const val BATERIA_BAJA = 25

// Muestra lo que devuelve /estado. "miEquipo" sirve para avisar si otro equipo tiene el control.
@Composable
fun PanelEstado(estado: EstadoResponse?, miEquipo: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (estado == null) {
                Text("Estado: sin datos del backend", color = MaterialTheme.colorScheme.error)
                return@Column
            }

            Text("Robot: ${estado.nombre_robot ?: "?"} (${estado.tipo_robot ?: "?"})")
            Text(
                if (estado.conectado) "Robot conectado" else "Robot desconectado",
                color = if (estado.conectado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )

            val bateria = estado.bateria
            if (bateria != null) {
                val color = if (bateria < BATERIA_BAJA) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
                Text("Batería: $bateria %")
                LinearProgressIndicator(
                    progress = { bateria.coerceIn(0, 100) / 100f },
                    color = color,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text("Control: ${estado.equipo_activo ?: "nadie"}")
            val activo = estado.equipo_activo
            if (miEquipo.isNotBlank() && activo != null && activo != miEquipo.trim()) {
                Text(
                    "Ojo: el control lo tiene otro equipo ($activo).",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
