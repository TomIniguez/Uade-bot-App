package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.RegistroComando
import com.example.tpo_bot_uade_app.ui.theme.Texto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FORMATO_HORA = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("es-AR"))

fun hora(ms: Long): String = FORMATO_HORA.format(Date(ms))

// Estado de una orden, siempre con icono + texto (no solo color):
// Pendiente (reloj) / Confirmada (tilde) / Rechazada (cruz) / Sin respuesta (senal tachada).
@Composable
fun ChipEstadoComando(registro: RegistroComando, modifier: Modifier = Modifier) {
    val (icono, nivel) = when (registro.estado) {
        EstadoComando.PENDIENTE -> Icons.Outlined.Schedule to NivelVisual.ADVERTENCIA
        EstadoComando.CONFIRMADA -> Icons.Outlined.CheckCircle to NivelVisual.OK
        EstadoComando.RECHAZADA -> Icons.Outlined.Close to NivelVisual.ERROR
        EstadoComando.SIN_RESPUESTA -> Icons.Outlined.SignalWifiOff to NivelVisual.ERROR
    }
    val color = colorDe(nivel)
    val segundaLinea = when (registro.estado) {
        EstadoComando.RECHAZADA -> registro.motivo
        EstadoComando.SIN_RESPUESTA -> stringResource(R.string.ec_sin_respuesta_detalle)
        else -> null
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
            .padding(12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }, // TalkBack anuncia el cambio de estado
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(
                    R.string.ec_linea,
                    textoComando(registro.tipo),
                    textoEstadoComando(registro.estado),
                    hora(registro.enviadoMs)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
            if (segundaLinea != null) {
                Text(segundaLinea, style = MaterialTheme.typography.bodyMedium, color = Texto)
            }
        }
    }
}
