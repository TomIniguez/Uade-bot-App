package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo

@Composable
fun textoEstado(estado: EstadoOperativo): String = stringResource(
    when (estado) {
        EstadoOperativo.EN_BASE -> R.string.estado_en_base
        EstadoOperativo.EN_MISION -> R.string.estado_en_mision
        EstadoOperativo.PAUSADO -> R.string.estado_pausado
        EstadoOperativo.ESPERANDO_DECISION -> R.string.estado_esperando_decision
        EstadoOperativo.REGRESANDO -> R.string.estado_regresando
        EstadoOperativo.DETENIDO -> R.string.estado_detenido
    }
)

// "Esperando decision" va en advertencia y "Detenido" en error (Figma).
@Composable
fun ChipEstadoOperativo(
    estado: EstadoOperativo,
    modifier: Modifier = Modifier,
    grande: Boolean = false,
    antiguo: Boolean = false
) {
    val (icono, nivel) = when (estado) {
        EstadoOperativo.EN_BASE -> Icons.Outlined.Home to NivelVisual.NORMAL
        EstadoOperativo.EN_MISION -> Icons.Outlined.PlayCircle to NivelVisual.OK
        EstadoOperativo.PAUSADO -> Icons.Outlined.PauseCircle to NivelVisual.NORMAL
        EstadoOperativo.ESPERANDO_DECISION -> Icons.Outlined.WarningAmber to NivelVisual.ADVERTENCIA
        EstadoOperativo.REGRESANDO -> Icons.AutoMirrored.Outlined.Undo to NivelVisual.NORMAL
        EstadoOperativo.DETENIDO -> Icons.Outlined.Block to NivelVisual.ERROR
    }
    ChipHmi(
        icono = icono,
        texto = textoEstado(estado),
        color = colorDe(nivel),
        modifier = modifier.alpha(if (antiguo) ALFA_ANTIGUO else 1f),
        estilo = if (grande) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium
    )
}
