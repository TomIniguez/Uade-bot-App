package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Spacer
import com.example.tpo_bot_uade_app.domain.Habilitacion
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

enum class Jerarquia { PRIMARIO, SECUNDARIO }

// Boton de una orden. Estados: normal / deshabilitado (con el motivo debajo) / pendiente (cargando).
@Composable
fun BotonComando(
    texto: String,
    icono: ImageVector,
    habilitacion: Habilitacion,
    telemetria: Telemetria?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pendiente: Boolean = false,
    jerarquia: Jerarquia = Jerarquia.SECUNDARIO
) {
    val habilitado = habilitacion.habilitado && !pendiente
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val contenido: @Composable () -> Unit = {
            if (pendiente) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(texto, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        val mod = Modifier.fillMaxWidth().height(56.dp)
        if (jerarquia == Jerarquia.PRIMARIO) {
            Button(onClick = onClick, enabled = habilitado, modifier = mod) { contenido() }
        } else {
            OutlinedButton(onClick = onClick, enabled = habilitado, modifier = mod) { contenido() }
        }
        val motivo = habilitacion.motivo
        if (!pendiente && !habilitacion.habilitado && motivo != null) {
            Text(textoMotivo(motivo, telemetria), style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
        }
    }
}
