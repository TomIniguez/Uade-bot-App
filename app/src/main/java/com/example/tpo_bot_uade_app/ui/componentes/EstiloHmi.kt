package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.ui.theme.Advertencia
import com.example.tpo_bot_uade_app.ui.theme.Deshabilitado
import com.example.tpo_bot_uade_app.ui.theme.ErrorHmi
import com.example.tpo_bot_uade_app.ui.theme.Ok
import com.example.tpo_bot_uade_app.ui.theme.Texto
import java.util.Locale

// Los cuatro estados visuales que pide el enunciado. OK se usa para "todo bien" explicito
// (conectado, confirmada); NORMAL es simplemente "sin nada que destacar".
enum class NivelVisual { NORMAL, OK, DESHABILITADO, ADVERTENCIA, ERROR }

fun colorDe(nivel: NivelVisual): Color = when (nivel) {
    NivelVisual.NORMAL -> Texto
    NivelVisual.OK -> Ok
    NivelVisual.DESHABILITADO -> Deshabilitado
    NivelVisual.ADVERTENCIA -> Advertencia
    NivelVisual.ERROR -> ErrorHmi
}

// Alfa para datos antiguos: nunca se muestra un dato viejo como si fuera actual.
const val ALFA_ANTIGUO = 0.5f

// Formato argentino: coma decimal (23,4). Se fija a mano porque el telefono puede estar en ingles.
private val LOCALE_AR: Locale = Locale.forLanguageTag("es-AR")

fun decimal(valor: Float, decimales: Int = 1): String =
    String.format(LOCALE_AR, "%.${decimales}f", valor)

// Chip base: icono + texto sobre un fondo suave del mismo color. El color nunca va solo.
@Composable
fun ChipHmi(
    icono: ImageVector,
    texto: String,
    color: Color,
    modifier: Modifier = Modifier,
    estilo: TextStyle = MaterialTheme.typography.labelMedium
) {
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(texto, style = estilo, color = color)
    }
}
