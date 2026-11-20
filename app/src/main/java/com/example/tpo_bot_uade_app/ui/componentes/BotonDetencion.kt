package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.Habilitacion
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.ui.theme.Deshabilitado
import com.example.tpo_bot_uade_app.ui.theme.Detencion
import com.example.tpo_bot_uade_app.ui.theme.Texto
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

// Detencion operativa: un solo toque, sin confirmacion, nunca bloqueada por otra orden pendiente.
// Estados: normal / pendiente / deshabilitado (con motivo).
@Composable
fun BotonDetencion(
    habilitacion: Habilitacion,
    pendiente: Boolean,
    telemetria: Telemetria?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Button(
            onClick = onClick,
            enabled = habilitacion.habilitado && !pendiente,
            colors = ButtonDefaults.buttonColors(
                containerColor = Detencion,
                contentColor = Texto,
                disabledContainerColor = Deshabilitado,
                disabledContentColor = TextoSecundario
            ),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (pendiente) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Texto)
            } else {
                Icon(Icons.Outlined.PanTool, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.cmd_detencion), style = MaterialTheme.typography.titleMedium)
        }
        val motivo = habilitacion.motivo
        val leyenda = if (!habilitacion.habilitado && motivo != null && !pendiente) {
            textoMotivo(motivo, telemetria) + " · " + stringResource(R.string.detencion_leyenda)
        } else stringResource(R.string.detencion_leyenda)
        Text(
            leyenda,
            style = MaterialTheme.typography.labelMedium,
            color = TextoSecundario,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
