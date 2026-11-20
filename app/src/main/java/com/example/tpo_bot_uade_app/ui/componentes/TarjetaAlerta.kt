package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.Severidad
import com.example.tpo_bot_uade_app.ui.theme.Primario
import com.example.tpo_bot_uade_app.ui.theme.Superficie
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

fun colorSeveridad(severidad: Severidad): Color = when (severidad) {
    Severidad.INFO -> Primario
    Severidad.ADVERTENCIA -> colorDe(NivelVisual.ADVERTENCIA)
    Severidad.CRITICA -> colorDe(NivelVisual.ERROR)
}

// Variantes: severidad info / advertencia / critica · estado activa / reconocida.
// "acciones" es el lugar para el boton Reconocer o una accion sugerida (ej.: Regresar a punto seguro).
@Composable
fun TarjetaAlerta(
    alerta: Alerta,
    modifier: Modifier = Modifier,
    acciones: @Composable () -> Unit = {}
) {
    val color = colorSeveridad(alerta.severidad)
    val icono = when {
        alerta.reconocida -> Icons.Outlined.CheckCircle
        alerta.severidad == Severidad.CRITICA -> Icons.Outlined.ErrorOutline
        alerta.severidad == Severidad.ADVERTENCIA -> Icons.Outlined.WarningAmber
        else -> Icons.Outlined.Info
    }
    val textoSeveridad = stringResource(
        when (alerta.severidad) {
            Severidad.INFO -> R.string.sev_info
            Severidad.ADVERTENCIA -> R.string.sev_advertencia
            Severidad.CRITICA -> R.string.sev_critica
        }
    )
    Card(
        modifier = modifier.fillMaxWidth().alpha(if (alerta.reconocida) 0.7f else 1f),
        colors = CardDefaults.cardColors(containerColor = if (alerta.reconocida) Superficie else color.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icono, contentDescription = null, tint = if (alerta.reconocida) TextoSecundario else color, modifier = Modifier.size(22.dp))
                Text(textoTipoAlerta(alerta.tipo), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(hora(alerta.timestampMs), style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
            }
            Text(alerta.mensaje, style = MaterialTheme.typography.bodyMedium)
            Text(
                if (alerta.reconocida) stringResource(R.string.alerta_reconocida, textoSeveridad) else textoSeveridad,
                style = MaterialTheme.typography.labelMedium,
                color = if (alerta.reconocida) TextoSecundario else color
            )
            acciones()
        }
    }
}
