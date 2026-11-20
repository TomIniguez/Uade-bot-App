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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.Umbrales
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import com.example.tpo_bot_uade_app.domain.model.TipoComando
import com.example.tpo_bot_uade_app.ui.componentes.AccionesAlerta
import com.example.tpo_bot_uade_app.ui.componentes.BotonComando
import com.example.tpo_bot_uade_app.ui.componentes.TarjetaAlerta
import com.example.tpo_bot_uade_app.ui.componentes.ChipEstadoComando
import com.example.tpo_bot_uade_app.ui.componentes.ChipEstadoOperativo
import com.example.tpo_bot_uade_app.ui.componentes.Jerarquia
import com.example.tpo_bot_uade_app.ui.componentes.MiniMapa
import com.example.tpo_bot_uade_app.ui.componentes.NivelVisual
import com.example.tpo_bot_uade_app.ui.componentes.TarjetaMision
import com.example.tpo_bot_uade_app.ui.componentes.TarjetaTelemetria
import com.example.tpo_bot_uade_app.ui.componentes.colorDe
import com.example.tpo_bot_uade_app.ui.componentes.decimal
import com.example.tpo_bot_uade_app.ui.robot.EventoUi
import com.example.tpo_bot_uade_app.ui.robot.RobotUiState
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

// Panel de supervision: pantalla de inicio. Combina el panel (estado, mision, mapa, servicio)
// con el control (ultima orden y botones). La Detencion operativa esta fija abajo, fuera de esta pantalla.
@Composable
fun SupervisionScreen(
    uiState: RobotUiState,
    onEvento: (EventoUi) -> Unit,
    onVerTelemetria: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChipEstadoOperativo(t.estado, grande = true, antiguo = antiguo)
            Text(
                stringResource(R.string.ultima_actualizacion, uiState.segundosDesdeActualizacion),
                style = MaterialTheme.typography.labelMedium,
                color = if (antiguo) colorDe(NivelVisual.ADVERTENCIA) else TextoSecundario
            )
        }

        // La alerta activa mas nueva (sin contar incidentes, que abren su propio dialogo), con su accion sugerida.
        uiState.alertasActivas.firstOrNull { !it.requiereDecision }?.let { alerta ->
            TarjetaAlerta(alerta) { AccionesAlerta(alerta, uiState, onEvento) }
        }

        TarjetaMision(t.mision, antiguo = antiguo)
        uiState.plano?.let { MiniMapa(it, t, uiState.definicionMisionActual, antiguo = antiguo) }

        // Las tres variables del servicio
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            TarjetaTelemetria(
                etiqueta = stringResource(R.string.tele_temperatura),
                valor = decimal(t.servicio.temperaturaC),
                unidad = stringResource(R.string.unidad_celsius),
                icono = Icons.Outlined.Thermostat,
                nivel = if (t.servicio.temperaturaC > Umbrales.UMBRAL_TEMPERATURA) NivelVisual.ERROR else NivelVisual.NORMAL,
                antiguo = antiguo,
                modifier = Modifier.weight(1f)
            )
            TarjetaTelemetria(
                etiqueta = stringResource(R.string.tele_puntos),
                valor = t.mision?.let { m -> stringResource(R.string.valor_de_total, t.servicio.puntosInspeccionados, m.totalPuntos) }
                    ?: stringResource(R.string.sin_dato),
                icono = Icons.Outlined.TaskAlt,
                antiguo = antiguo,
                modifier = Modifier.weight(1f)
            )
            TarjetaTelemetria(
                etiqueta = stringResource(R.string.tele_anomalias),
                valor = t.servicio.anomaliasDetectadas.toString(),
                icono = Icons.Outlined.ReportProblem,
                nivel = if (t.servicio.anomaliasDetectadas > 0) NivelVisual.ERROR else NivelVisual.NORMAL,
                antiguo = antiguo,
                modifier = Modifier.weight(1f)
            )
        }

        TextButton(onClick = onVerTelemetria, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.ver_telemetria))
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 4.dp))
        }

        // Ultima orden: siempre visible, con texto e icono (no solo color).
        Text(stringResource(R.string.ultima_orden), style = MaterialTheme.typography.titleMedium)
        val ultimo = uiState.ultimoComando
        if (ultimo != null) {
            ChipEstadoComando(ultimo)
        } else {
            Text(stringResource(R.string.ultima_orden_ninguna), style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
        }

        // Barra de control
        Text(stringResource(R.string.seccion_control), style = MaterialTheme.typography.titleMedium)
        if (t.estado == EstadoOperativo.PAUSADO) {
            BotonComando(
                texto = stringResource(R.string.cmd_reanudar),
                icono = Icons.Outlined.PlayArrow,
                habilitacion = uiState.habilitacionDe(TipoComando.REANUDAR_MISION),
                telemetria = t,
                pendiente = uiState.estaPendiente(TipoComando.REANUDAR_MISION),
                jerarquia = Jerarquia.PRIMARIO,
                onClick = { onEvento(EventoUi.PedirComando(Comando.ReanudarMision)) }
            )
        } else {
            BotonComando(
                texto = stringResource(R.string.cmd_pausar),
                icono = Icons.Outlined.Pause,
                habilitacion = uiState.habilitacionDe(TipoComando.PAUSAR_MISION),
                telemetria = t,
                pendiente = uiState.estaPendiente(TipoComando.PAUSAR_MISION),
                jerarquia = Jerarquia.PRIMARIO,
                onClick = { onEvento(EventoUi.PedirComando(Comando.PausarMision)) }
            )
        }
        BotonComando(
            texto = stringResource(R.string.cmd_regresar),
            icono = Icons.AutoMirrored.Outlined.Undo,
            habilitacion = uiState.habilitacionDe(TipoComando.REGRESAR_A_PUNTO_SEGURO),
            telemetria = t,
            pendiente = uiState.estaPendiente(TipoComando.REGRESAR_A_PUNTO_SEGURO),
            onClick = { onEvento(EventoUi.PedirComando(Comando.RegresarAPuntoSeguro)) }
        )
        BotonComando(
            texto = stringResource(R.string.cmd_cancelar),
            icono = Icons.Outlined.Cancel,
            habilitacion = uiState.habilitacionDe(TipoComando.CANCELAR_MISION),
            telemetria = t,
            pendiente = uiState.estaPendiente(TipoComando.CANCELAR_MISION),
            onClick = { onEvento(EventoUi.PedirComando(Comando.CancelarMision)) }
        )
    }
}
