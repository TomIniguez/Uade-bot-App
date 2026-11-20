package com.example.tpo_bot_uade_app.ui.telemetria

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BatteryFull
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.Umbrales
import com.example.tpo_bot_uade_app.domain.model.EstadoConexion
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import com.example.tpo_bot_uade_app.ui.componentes.NivelVisual
import com.example.tpo_bot_uade_app.ui.componentes.TarjetaTelemetria
import com.example.tpo_bot_uade_app.ui.componentes.decimal
import com.example.tpo_bot_uade_app.ui.componentes.nivelBateria
import com.example.tpo_bot_uade_app.ui.componentes.textoEstado
import com.example.tpo_bot_uade_app.ui.robot.RobotUiState

// Un dato de la grilla (para armar los grupos de forma ordenada).
private data class Dato(
    val etiqueta: String,
    val valor: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector,
    val unidad: String = "",
    val nivel: NivelVisual = NivelVisual.NORMAL,
    val esTexto: Boolean = false
)

// Telemetria detallada: todos los campos con su unidad, agrupados. Atenuada si los datos son viejos.
@Composable
fun TelemetriaScreen(uiState: RobotUiState, onVolver: () -> Unit, modifier: Modifier = Modifier) {
    val t = uiState.telemetria
    if (t == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val antiguo = uiState.datosAntiguos
    val sinDato = stringResource(R.string.sin_dato)
    val conectado = t.conexion == EstadoConexion.CONECTADO

    val grupos = listOf(
        stringResource(R.string.tg_enlace) to listOf(
            Dato(
                stringResource(R.string.tele_conexion),
                stringResource(if (conectado) R.string.tele_conectado else R.string.conexion_sin),
                Icons.Outlined.Wifi, nivel = if (conectado) NivelVisual.OK else NivelVisual.ERROR, esTexto = true
            ),
            Dato(stringResource(R.string.tele_senal), t.senalPct.toString(), Icons.Outlined.SignalCellularAlt, stringResource(R.string.unidad_pct)),
            Dato(
                stringResource(R.string.tele_actualizacion),
                stringResource(R.string.tele_hace, uiState.segundosDesdeActualizacion),
                Icons.Outlined.Schedule, nivel = if (antiguo) NivelVisual.ADVERTENCIA else NivelVisual.NORMAL, esTexto = true
            )
        ),
        stringResource(R.string.tg_energia) to listOf(
            Dato(stringResource(R.string.tele_bateria), t.bateriaPct.toString(), Icons.Outlined.BatteryFull, stringResource(R.string.unidad_pct), nivelBateria(t.bateriaPct))
        ),
        stringResource(R.string.tg_operacion) to listOf(
            Dato(
                stringResource(R.string.tele_estado), textoEstado(t.estado), Icons.Outlined.SmartToy,
                nivel = when (t.estado) {
                    EstadoOperativo.ESPERANDO_DECISION -> NivelVisual.ADVERTENCIA
                    EstadoOperativo.DETENIDO -> NivelVisual.ERROR
                    else -> NivelVisual.NORMAL
                },
                esTexto = true
            ),
            Dato(stringResource(R.string.tele_mision), t.mision?.nombre ?: sinDato, Icons.Outlined.Route, esTexto = true),
            Dato(stringResource(R.string.tele_progreso), t.mision?.progresoPct?.toString() ?: sinDato, Icons.AutoMirrored.Outlined.TrendingUp,
                if (t.mision != null) stringResource(R.string.unidad_pct) else "")
        ),
        stringResource(R.string.tg_ubicacion) to listOf(
            Dato(stringResource(R.string.tele_zona), t.posicion.zona, Icons.Outlined.Place, esTexto = true),
            Dato(stringResource(R.string.tele_x), decimal(t.posicion.x), Icons.Outlined.SwapHoriz, stringResource(R.string.unidad_m)),
            Dato(stringResource(R.string.tele_y), decimal(t.posicion.y), Icons.Outlined.SwapVert, stringResource(R.string.unidad_m)),
            Dato(stringResource(R.string.tele_velocidad), decimal(t.velocidadMs), Icons.Outlined.Speed, stringResource(R.string.unidad_ms)),
            Dato(stringResource(R.string.tele_rumbo), t.rumboGrados.toString(), Icons.Outlined.Explore, stringResource(R.string.unidad_grados))
        ),
        stringResource(R.string.tg_servicio) to listOf(
            Dato(
                stringResource(R.string.tele_temperatura), decimal(t.servicio.temperaturaC), Icons.Outlined.Thermostat,
                stringResource(R.string.unidad_celsius),
                if (t.servicio.temperaturaC > Umbrales.UMBRAL_TEMPERATURA) NivelVisual.ERROR else NivelVisual.NORMAL
            ),
            Dato(
                stringResource(R.string.tele_puntos),
                t.mision?.let { stringResource(R.string.valor_de_total, t.servicio.puntosInspeccionados, it.totalPuntos) } ?: sinDato,
                Icons.Outlined.TaskAlt
            ),
            Dato(
                stringResource(R.string.tele_anomalias), t.servicio.anomaliasDetectadas.toString(), Icons.Outlined.ReportProblem,
                nivel = if (t.servicio.anomaliasDetectadas > 0) NivelVisual.ERROR else NivelVisual.NORMAL
            )
        ),
        stringResource(R.string.tg_alertas) to listOf(
            Dato(
                stringResource(R.string.tele_alertas_activas), t.alertasActivas.toString(), Icons.Outlined.NotificationsActive,
                nivel = if (t.alertasActivas > 0) NivelVisual.ADVERTENCIA else NivelVisual.NORMAL
            )
        )
    )

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.boton_volver))
            }
            Text(stringResource(R.string.tele_titulo), style = MaterialTheme.typography.titleLarge)
        }
        grupos.forEach { (titulo, datos) ->
            Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            // Grilla de 2 columnas
            datos.chunked(2).forEach { fila ->
                // IntrinsicSize.Min + fillMaxHeight: las dos tarjetas de la fila quedan de la misma altura.
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    fila.forEach { d ->
                        TarjetaTelemetria(
                            etiqueta = d.etiqueta, valor = d.valor, icono = d.icono, unidad = d.unidad,
                            nivel = d.nivel, antiguo = antiguo, valorEsTexto = d.esTexto, modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    if (fila.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}
