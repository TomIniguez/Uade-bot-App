package com.example.tpo_bot_uade_app.ui.supervision

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.Umbrales
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.ui.componentes.ChipEstadoOperativo
import com.example.tpo_bot_uade_app.ui.componentes.NivelVisual
import com.example.tpo_bot_uade_app.ui.componentes.TarjetaTelemetria
import com.example.tpo_bot_uade_app.ui.componentes.colorDe
import com.example.tpo_bot_uade_app.ui.componentes.decimal
import com.example.tpo_bot_uade_app.ui.robot.RobotUiState
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

// Panel de supervision (version minima de la fase 3): estado, ultima actualizacion y telemetria viva.
// En la fase 4 se suman la tarjeta de mision, el mini mapa y la barra de control.
@Composable
fun SupervisionScreen(uiState: RobotUiState, modifier: Modifier = Modifier) {
    val t = uiState.telemetria
    if (t == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val antiguo = uiState.datosAntiguos

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ChipEstadoOperativo(t.estado, grande = true, antiguo = antiguo)
        Text(
            stringResource(R.string.ultima_actualizacion, uiState.segundosDesdeActualizacion),
            style = MaterialTheme.typography.labelMedium,
            color = if (antiguo) colorDe(NivelVisual.ADVERTENCIA) else TextoSecundario
        )

        Text(stringResource(R.string.seccion_servicio), style = MaterialTheme.typography.titleMedium)
        FilaDeTres(
            { TarjetaTemperatura(t, antiguo, it) },
            {
                TarjetaTelemetria(
                    etiqueta = stringResource(R.string.tele_puntos),
                    valor = t.mision?.let { m -> stringResource(R.string.valor_de_total, t.servicio.puntosInspeccionados, m.totalPuntos) }
                        ?: stringResource(R.string.sin_dato),
                    icono = Icons.Outlined.TaskAlt,
                    antiguo = antiguo,
                    modifier = it
                )
            },
            {
                TarjetaTelemetria(
                    etiqueta = stringResource(R.string.tele_anomalias),
                    valor = t.servicio.anomaliasDetectadas.toString(),
                    icono = Icons.Outlined.ReportProblem,
                    nivel = if (t.servicio.anomaliasDetectadas > 0) NivelVisual.ERROR else NivelVisual.NORMAL,
                    antiguo = antiguo,
                    modifier = it
                )
            }
        )

        Text(stringResource(R.string.seccion_robot), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TarjetaTelemetria(
                etiqueta = stringResource(R.string.tele_zona),
                valor = t.posicion.zona,
                valorEsTexto = true,
                icono = Icons.Outlined.Place,
                antiguo = antiguo,
                modifier = Modifier.weight(1f)
            )
            TarjetaTelemetria(
                etiqueta = stringResource(R.string.tele_velocidad),
                valor = decimal(t.velocidadMs),
                unidad = stringResource(R.string.unidad_ms),
                icono = Icons.Outlined.Speed,
                antiguo = antiguo,
                modifier = Modifier.weight(1f)
            )
        }
        TarjetaTelemetria(
            etiqueta = stringResource(R.string.tele_senal),
            valor = t.senalPct.toString(),
            unidad = stringResource(R.string.unidad_pct),
            icono = Icons.Outlined.SignalCellularAlt,
            antiguo = antiguo,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TarjetaTemperatura(t: Telemetria, antiguo: Boolean, modifier: Modifier) {
    val alta = t.servicio.temperaturaC > Umbrales.UMBRAL_TEMPERATURA
    TarjetaTelemetria(
        etiqueta = stringResource(R.string.tele_temperatura),
        valor = decimal(t.servicio.temperaturaC),
        unidad = stringResource(R.string.unidad_celsius),
        icono = Icons.Outlined.Thermostat,
        nivel = if (alta) NivelVisual.ERROR else NivelVisual.NORMAL,
        antiguo = antiguo,
        modifier = modifier
    )
}

@Composable
private fun FilaDeTres(
    a: @Composable (Modifier) -> Unit,
    b: @Composable (Modifier) -> Unit,
    c: @Composable (Modifier) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        a(Modifier.weight(1f))
        b(Modifier.weight(1f))
        c(Modifier.weight(1f))
    }
}
