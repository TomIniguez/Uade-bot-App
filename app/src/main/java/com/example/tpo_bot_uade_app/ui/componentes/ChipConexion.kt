package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.model.EstadoConexion
import com.example.tpo_bot_uade_app.domain.model.Telemetria

// Tres variantes: conectado / sin conexion / datos antiguos.
@Composable
fun ChipConexion(
    telemetria: Telemetria?,
    datosAntiguos: Boolean,
    segundos: Int,
    modifier: Modifier = Modifier
) {
    when {
        telemetria == null || telemetria.conexion == EstadoConexion.SIN_CONEXION ->
            ChipHmi(Icons.Outlined.WifiOff, stringResource(R.string.conexion_sin), colorDe(NivelVisual.ERROR), modifier)

        datosAntiguos ->
            ChipHmi(
                Icons.Outlined.Schedule,
                stringResource(R.string.conexion_datos_antiguos, segundos),
                colorDe(NivelVisual.ADVERTENCIA),
                modifier
            )

        else ->
            ChipHmi(
                Icons.Outlined.Wifi,
                stringResource(R.string.conexion_ok, telemetria.senalPct),
                colorDe(NivelVisual.OK),
                modifier
            )
    }
}
