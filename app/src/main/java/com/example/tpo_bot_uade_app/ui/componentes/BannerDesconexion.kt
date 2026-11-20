package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
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
import com.example.tpo_bot_uade_app.domain.model.EstadoConexion
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.ui.theme.Fondo
import com.example.tpo_bot_uade_app.ui.theme.Texto

// Banner global bajo la barra superior. Variantes: sin conexion / datos antiguos / conexion recuperada.
// Nunca deja que un dato viejo parezca actual: dice de cuando es y que hacer.
@Composable
fun BannerDesconexion(
    telemetria: Telemetria?,
    datosAntiguos: Boolean,
    segundos: Int,
    conexionRecuperada: Boolean,
    modifier: Modifier = Modifier
) {
    val sinConexion = telemetria?.conexion == EstadoConexion.SIN_CONEXION
    val visible = datosAntiguos || conexionRecuperada

    AnimatedVisibility(visible = visible, modifier = modifier) {
        val (icono, color, titulo, detalle) = when {
            sinConexion -> Cuatro(
                Icons.Outlined.WifiOff, colorDe(NivelVisual.ERROR),
                stringResource(R.string.banner_sin_conexion, segundos),
                stringResource(R.string.banner_sin_conexion_detalle)
            )
            datosAntiguos -> Cuatro(
                Icons.Outlined.Schedule, colorDe(NivelVisual.ADVERTENCIA),
                stringResource(R.string.banner_datos_antiguos, segundos),
                stringResource(R.string.banner_datos_antiguos_detalle)
            )
            else -> Cuatro(
                Icons.Outlined.Wifi, colorDe(NivelVisual.OK),
                stringResource(R.string.banner_recuperada),
                stringResource(R.string.banner_recuperada_detalle)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .semantics { liveRegion = LiveRegionMode.Assertive }, // TalkBack lo anuncia enseguida
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Texto oscuro sobre amarillo y verde; claro sobre rojo (contraste 4,5:1).
            val colorTexto = if (sinConexion) Texto else Fondo
            Icon(icono, contentDescription = null, tint = colorTexto, modifier = Modifier.size(22.dp))
            Column {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = colorTexto)
                Text(detalle, style = MaterialTheme.typography.labelMedium, color = colorTexto)
            }
        }
    }
}

private data class Cuatro(
    val icono: androidx.compose.ui.graphics.vector.ImageVector,
    val color: androidx.compose.ui.graphics.Color,
    val titulo: String,
    val detalle: String
)
