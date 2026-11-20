package com.example.tpo_bot_uade_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Un solo tema, oscuro, con colores fijos: en una HMI el color de cada estado
// tiene que ser siempre el mismo (por eso no se usan los "colores dinamicos" de Android 12+).
private val EsquemaHmi = darkColorScheme(
    primary = Primario,
    onPrimary = SobrePrimario,
    secondary = TextoSecundario,
    onSecondary = Fondo,
    background = Fondo,
    onBackground = Texto,
    surface = Superficie,
    onSurface = Texto,
    surfaceVariant = SuperficieAlta,
    onSurfaceVariant = TextoSecundario,
    surfaceContainer = Superficie,
    surfaceContainerHigh = Superficie,
    surfaceContainerHighest = Superficie,   // las Card de Material usan este color: tarjetas = superficie
    outline = SuperficieAlta,
    error = ErrorHmi,
    onError = Texto
)

@Composable
fun TPO_Bot_UADE_AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaHmi,
        typography = Typography,
        content = content
    )
}
