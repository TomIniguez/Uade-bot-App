package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.model.CategoriaEvento
import com.example.tpo_bot_uade_app.domain.model.EstadoMision
import com.example.tpo_bot_uade_app.domain.model.RegistroEvento
import com.example.tpo_bot_uade_app.domain.model.RegistroMision
import com.example.tpo_bot_uade_app.domain.model.Severidad
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FORMATO_FECHA = SimpleDateFormat("dd/MM HH:mm:ss", Locale.forLanguageTag("es-AR"))

fun fechaHora(ms: Long): String = FORMATO_FECHA.format(Date(ms))

// Fila base del historial: icono, titulo, detalle y fecha.
@Composable
private fun FilaHistorial(
    icono: ImageVector,
    color: Color,
    titulo: String,
    detalle: String,
    fecha: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(modifier.fillMaxWidth().let { if (onClick != null) it.clickable(onClick = onClick) else it }) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium)
                if (detalle.isNotBlank()) Text(detalle, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                Text(fecha, style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
            }
        }
    }
}

// Variante mision: completada / cancelada / abortada.
@Composable
fun ItemHistorial(mision: RegistroMision, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (icono, nivel) = when (mision.resultado) {
        EstadoMision.COMPLETADA -> Icons.Outlined.CheckCircle to NivelVisual.OK
        EstadoMision.CANCELADA -> Icons.Outlined.Cancel to NivelVisual.ADVERTENCIA
        else -> Icons.Outlined.Block to NivelVisual.ERROR
    }
    FilaHistorial(
        icono = icono,
        color = colorDe(nivel),
        titulo = stringResource(R.string.h_item_mision, mision.nombre, textoEstadoMision(mision.resultado)),
        detalle = stringResource(
            R.string.h_item_mision_detalle,
            pluralStringResource(R.plurals.p_puntos_inspeccionados, mision.totalPuntos, mision.puntosInspeccionados, mision.totalPuntos),
            pluralStringResource(R.plurals.p_anomalias, mision.anomalias, mision.anomalias)
        ),
        fecha = fechaHora(mision.finMs),
        modifier = modifier,
        onClick = onClick
    )
}

// Variante evento: comando / alerta / conexion / mision.
@Composable
fun ItemHistorial(evento: RegistroEvento, modifier: Modifier = Modifier) {
    val icono = when (evento.categoria) {
        CategoriaEvento.COMANDO -> Icons.Outlined.TouchApp
        CategoriaEvento.ALERTA -> Icons.Outlined.NotificationsActive
        CategoriaEvento.CONEXION -> Icons.Outlined.Wifi
        CategoriaEvento.MISION -> Icons.Outlined.Route
    }
    val color = when (evento.severidad) {
        Severidad.INFO -> TextoSecundario
        Severidad.ADVERTENCIA -> colorDe(NivelVisual.ADVERTENCIA)
        Severidad.CRITICA -> colorDe(NivelVisual.ERROR)
    }
    FilaHistorial(icono, color, evento.titulo, evento.detalle, fechaHora(evento.timestampMs), modifier)
}
