package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.model.EstadoMision
import com.example.tpo_bot_uade_app.domain.model.MisionActiva
import com.example.tpo_bot_uade_app.ui.theme.Primario
import com.example.tpo_bot_uade_app.ui.theme.SuperficieAlta
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

// Variantes: sin mision / en curso / pausada / completada / cancelada / abortada.
@Composable
fun TarjetaMision(mision: MisionActiva?, modifier: Modifier = Modifier, antiguo: Boolean = false) {
    Card(modifier.fillMaxWidth().alpha(if (antiguo) ALFA_ANTIGUO else 1f)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (mision == null) {
                Text(stringResource(R.string.mision_ninguna), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.mision_ninguna_ayuda), style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
                return@Column
            }
            val (icono, nivel) = when (mision.estado) {
                EstadoMision.EN_CURSO -> Icons.Outlined.PlayCircle to NivelVisual.OK
                EstadoMision.PAUSADA -> Icons.Outlined.PauseCircle to NivelVisual.NORMAL
                EstadoMision.COMPLETADA -> Icons.Outlined.CheckCircle to NivelVisual.OK
                EstadoMision.CANCELADA -> Icons.Outlined.Cancel to NivelVisual.ADVERTENCIA
                EstadoMision.ABORTADA -> Icons.Outlined.Block to NivelVisual.ERROR
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(mision.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                ChipHmi(icono, textoEstadoMision(mision.estado), colorDe(nivel))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { mision.progresoPct / 100f },
                    color = Primario,
                    trackColor = SuperficieAlta,
                    modifier = Modifier.weight(1f)
                )
                Text(stringResource(R.string.mision_progreso, mision.progresoPct), style = MaterialTheme.typography.labelMedium)
            }
            Row {
                Text(
                    mision.puntoActual?.let { stringResource(R.string.mision_punto_actual, it) } ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    stringResource(R.string.mision_puntos, mision.puntosCompletados, mision.totalPuntos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSecundario
                )
            }
        }
    }
}
