package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

// Un dato de telemetria: etiqueta, valor y unidad. Variantes: normal / advertencia / error / antiguo.
@Composable
fun TarjetaTelemetria(
    etiqueta: String,
    valor: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    unidad: String = "",
    nivel: NivelVisual = NivelVisual.NORMAL,
    antiguo: Boolean = false,
    valorEsTexto: Boolean = false     // true para valores largos como "Sala eléctrica": letra mas chica, hasta 2 lineas
) {
    val color = colorDe(nivel)
    Card(
        modifier = modifier
            .alpha(if (antiguo) ALFA_ANTIGUO else 1f)
            .semantics(mergeDescendants = true) { contentDescription = "$etiqueta: $valor $unidad" }
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icono, contentDescription = null, tint = if (nivel == NivelVisual.NORMAL) TextoSecundario else color, modifier = Modifier.size(18.dp))
                Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = TextoSecundario, maxLines = 1)
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    valor,
                    style = if (valorEsTexto) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                    color = color,
                    maxLines = if (valorEsTexto) 2 else 1
                )
                if (unidad.isNotEmpty()) {
                    Text(unidad, style = MaterialTheme.typography.labelMedium, color = TextoSecundario, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }
    }
}
