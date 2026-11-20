package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Battery2Bar
import androidx.compose.material.icons.outlined.BatteryAlert
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.Umbrales

// Umbrales visuales del Figma: normal >= 21 %, advertencia 11-20 %, error <= 10 %.
fun nivelBateria(pct: Int): NivelVisual = when {
    pct <= Umbrales.BATERIA_CRITICA -> NivelVisual.ERROR
    pct <= Umbrales.BATERIA_BAJA -> NivelVisual.ADVERTENCIA
    else -> NivelVisual.NORMAL
}

@Composable
fun IndicadorBateria(pct: Int, modifier: Modifier = Modifier, antiguo: Boolean = false) {
    val nivel = nivelBateria(pct)
    val icono = when (nivel) {
        NivelVisual.ERROR -> Icons.Outlined.BatteryAlert
        NivelVisual.ADVERTENCIA -> Icons.Outlined.Battery2Bar
        else -> Icons.Outlined.BatteryFull
    }
    ChipHmi(
        icono = icono,
        texto = stringResource(R.string.bateria_pct, pct),
        color = colorDe(nivel),
        modifier = modifier.alpha(if (antiguo) ALFA_ANTIGUO else 1f)
    )
}
