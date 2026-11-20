package com.example.tpo_bot_uade_app.ui.pruebas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.ui.componentes.ChipHmi
import com.example.tpo_bot_uade_app.ui.componentes.NivelVisual
import com.example.tpo_bot_uade_app.ui.componentes.colorDe
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario
import kotlin.math.roundToInt

// Panel "Solo demostracion": fuerza fallas en el robot simulado para mostrar los escenarios.
// Esto no existiria con un robot real.
@Composable
fun PanelPruebasScreen(
    estado: SimulacionUiState,
    onEvento: (EventoPrueba) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bateria by remember { mutableFloatStateOf(estado.bateriaPct.toFloat()) }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.boton_volver))
            }
            Text(stringResource(R.string.pruebas_titulo), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        }
        ChipHmi(Icons.Outlined.Science, stringResource(R.string.pruebas_rotulo), colorDe(NivelVisual.ADVERTENCIA))
        Text(stringResource(R.string.pruebas_explicacion), style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)

        estado.aviso?.let { aviso ->
            Text(
                stringResource(
                    when (aviso) {
                        AvisoPrueba.RECHAZO_ARMADO -> R.string.aviso_rechazo
                        AvisoPrueba.BATERIA_FIJADA -> R.string.aviso_bateria
                        AvisoPrueba.OBSTACULO -> R.string.aviso_obstaculo
                        AvisoPrueba.ANOMALIA -> R.string.aviso_anomalia
                        AvisoPrueba.REINICIADA -> R.string.aviso_reiniciada
                    }
                ),
                style = MaterialTheme.typography.labelMedium,
                color = colorDe(NivelVisual.OK)
            )
        }

        Seccion(stringResource(R.string.pruebas_enlace)) {
            // Toda la fila es tocable (no solo el interruptor): area grande y mejor para TalkBack.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .toggleable(
                        value = estado.desconexionForzada,
                        role = Role.Switch,
                        onValueChange = { onEvento(EventoPrueba.ForzarDesconexion(it)) }
                    )
            ) {
                Text(stringResource(R.string.pruebas_forzar_desconexion), modifier = Modifier.weight(1f))
                Switch(checked = estado.desconexionForzada, onCheckedChange = null)
            }
            OutlinedButton(onClick = { onEvento(EventoPrueba.RechazarProxima) }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(stringResource(R.string.pruebas_rechazar))
            }
        }

        Seccion(stringResource(R.string.pruebas_bateria, bateria.roundToInt())) {
            Slider(value = bateria, onValueChange = { bateria = it }, valueRange = 0f..100f)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = false, onClick = { bateria = 21f; onEvento(EventoPrueba.FijarBateria(21)) }, label = { Text(stringResource(R.string.bateria_pct, 21)) })
                FilterChip(selected = false, onClick = { bateria = 11f; onEvento(EventoPrueba.FijarBateria(11)) }, label = { Text(stringResource(R.string.bateria_pct, 11)) })
                OutlinedButton(onClick = { onEvento(EventoPrueba.FijarBateria(bateria.roundToInt())) }) {
                    Text(stringResource(R.string.pruebas_aplicar))
                }
            }
        }

        Seccion(stringResource(R.string.pruebas_incidentes)) {
            OutlinedButton(
                onClick = { onEvento(EventoPrueba.InyectarObstaculo) },
                enabled = estado.enMision,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text(stringResource(R.string.pruebas_obstaculo)) }
            OutlinedButton(
                onClick = { onEvento(EventoPrueba.InyectarAnomalia) },
                enabled = estado.enMision,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text(stringResource(R.string.pruebas_anomalia)) }
            if (!estado.enMision) {
                Text(stringResource(R.string.pruebas_solo_en_mision), style = MaterialTheme.typography.labelMedium, color = TextoSecundario)
            }
        }

        Seccion(stringResource(R.string.pruebas_velocidad)) {
            val opciones = listOf(1, 2, 4)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                opciones.forEachIndexed { i, factor ->
                    SegmentedButton(
                        selected = estado.velocidad == factor,
                        onClick = { onEvento(EventoPrueba.FijarVelocidad(factor)) },
                        shape = SegmentedButtonDefaults.itemShape(i, opciones.size)
                    ) { Text(stringResource(R.string.pruebas_factor, factor)) }
                }
            }
        }

        OutlinedButton(onClick = { onEvento(EventoPrueba.Reiniciar) }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text(stringResource(R.string.pruebas_reiniciar))
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            contenido()
        }
    }
}
