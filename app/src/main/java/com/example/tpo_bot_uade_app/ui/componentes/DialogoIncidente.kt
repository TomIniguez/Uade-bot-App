package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.ContextoComando
import com.example.tpo_bot_uade_app.domain.Habilitacion
import com.example.tpo_bot_uade_app.domain.ReglasComando
import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.DecisionIncidente
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.RegistroComando
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.domain.model.TipoAlerta
import com.example.tpo_bot_uade_app.domain.model.TipoComando
import com.example.tpo_bot_uade_app.ui.theme.ErrorHmi

// Decision del operador ante un incidente (obstaculo o anomalia termica).
// No se cierra tocando afuera ni con "Atras": se cierra solo cuando el robot sale de ESPERANDO_DECISION.
// Incluye la Detencion operativa, porque esa orden nunca puede quedar tapada por un dialogo.
@Composable
fun DialogoIncidente(
    alerta: Alerta,
    telemetria: Telemetria?,
    contexto: ContextoComando,
    habilitacion: (TipoComando) -> Habilitacion,
    pendiente: (TipoComando) -> Boolean,
    ultimoComando: RegistroComando?,
    onComando: (Comando) -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card {
            Column(
                Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = ErrorHmi, modifier = Modifier.size(28.dp))
                    Text(
                        stringResource(
                            if (alerta.tipo == TipoAlerta.OBSTACULO) R.string.inc_titulo_obstaculo else R.string.inc_titulo_anomalia
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(alerta.mensaje, style = MaterialTheme.typography.bodyMedium)
                telemetria?.mision?.puntoActual?.let {
                    Text(stringResource(R.string.inc_punto, it), style = MaterialTheme.typography.labelMedium, color = colorDe(NivelVisual.ADVERTENCIA))
                }
                Text(stringResource(R.string.inc_pregunta), style = MaterialTheme.typography.titleMedium)

                // Decisiones propias de este tipo de incidente
                ReglasComando.decisionesValidas(alerta.tipo).forEachIndexed { i, decision ->
                    BotonComando(
                        texto = textoDecision(decision),
                        icono = if (decision == DecisionIncidente.REINTENTAR || decision == DecisionIncidente.REINSPECCIONAR)
                            Icons.Outlined.Refresh else Icons.Outlined.SkipNext,
                        habilitacion = ReglasComando.resolverIncidente(alerta, decision, contexto),
                        telemetria = telemetria,
                        pendiente = pendiente(TipoComando.RESOLVER_INCIDENTE),
                        jerarquia = if (i == 0) Jerarquia.PRIMARIO else Jerarquia.SECUNDARIO,
                        onClick = { onComando(Comando.ResolverIncidente(alerta.id, decision)) }
                    )
                }

                HorizontalDivider()
                BotonComando(
                    texto = stringResource(R.string.cmd_regresar),
                    icono = Icons.AutoMirrored.Outlined.Undo,
                    habilitacion = habilitacion(TipoComando.REGRESAR_A_PUNTO_SEGURO),
                    telemetria = telemetria,
                    pendiente = pendiente(TipoComando.REGRESAR_A_PUNTO_SEGURO),
                    onClick = { onComando(Comando.RegresarAPuntoSeguro) }
                )
                BotonComando(
                    texto = stringResource(R.string.cmd_cancelar),
                    icono = Icons.Outlined.Cancel,
                    habilitacion = habilitacion(TipoComando.CANCELAR_MISION),
                    telemetria = telemetria,
                    pendiente = pendiente(TipoComando.CANCELAR_MISION),
                    onClick = { onComando(Comando.CancelarMision) }
                )

                // Si la ultima orden fue rechazada o no tuvo respuesta, se ve aca mismo.
                ultimoComando?.takeIf { it.estado != EstadoComando.CONFIRMADA }?.let { ChipEstadoComando(it) }

                BotonDetencion(
                    habilitacion = habilitacion(TipoComando.DETENCION_OPERATIVA),
                    pendiente = pendiente(TipoComando.DETENCION_OPERATIVA),
                    telemetria = telemetria,
                    onClick = { onComando(Comando.DetencionOperativa) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
