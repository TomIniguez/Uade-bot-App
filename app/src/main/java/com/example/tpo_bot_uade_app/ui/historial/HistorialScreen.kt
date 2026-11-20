package com.example.tpo_bot_uade_app.ui.historial

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.model.RegistroMision
import com.example.tpo_bot_uade_app.ui.componentes.DialogoConfirmacion
import com.example.tpo_bot_uade_app.ui.componentes.ItemHistorial
import com.example.tpo_bot_uade_app.ui.componentes.fechaHora
import com.example.tpo_bot_uade_app.ui.componentes.textoEstadoMision
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

// Historial guardado en el telefono (Room): pestanas Misiones / Eventos y detalle de una mision.
// Pensado para el supervisor de seguridad, que lo consulta al dia siguiente.
@Composable
fun HistorialScreen(estado: HistorialUiState, onEvento: (EventoHistorial) -> Unit, modifier: Modifier = Modifier) {
    val mision = estado.misionSeleccionada
    if (mision != null) {
        BackHandler { onEvento(EventoHistorial.CerrarDetalle) }
        DetalleMision(mision, estado, onEvento, modifier)
    } else {
        ListaHistorial(estado, onEvento, modifier)
    }

    if (estado.confirmarBorrado) {
        DialogoConfirmacion(
            titulo = stringResource(R.string.h_borrar_titulo),
            texto = stringResource(R.string.h_borrar_texto),
            textoConfirmar = stringResource(R.string.h_borrar),
            onConfirmar = { onEvento(EventoHistorial.ConfirmarBorrado) },
            onCancelar = { onEvento(EventoHistorial.CancelarBorrado) },
            destructivo = true
        )
    }
}

@Composable
private fun ListaHistorial(estado: HistorialUiState, onEvento: (EventoHistorial) -> Unit, modifier: Modifier) {
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.nav_historial), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            IconButton(
                onClick = { onEvento(EventoHistorial.PedirBorrado) },
                enabled = estado.misiones.isNotEmpty() || estado.eventos.isNotEmpty()
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.h_borrar))
            }
        }
        PrimaryTabRow(selectedTabIndex = estado.pestana.ordinal) {
            Tab(
                selected = estado.pestana == PestanaHistorial.MISIONES,
                onClick = { onEvento(EventoHistorial.CambiarPestana(PestanaHistorial.MISIONES)) },
                unselectedContentColor = TextoSecundario,
                text = { Text(stringResource(R.string.h_pestana_misiones, estado.misiones.size)) }
            )
            Tab(
                selected = estado.pestana == PestanaHistorial.EVENTOS,
                onClick = { onEvento(EventoHistorial.CambiarPestana(PestanaHistorial.EVENTOS)) },
                unselectedContentColor = TextoSecundario,
                text = { Text(stringResource(R.string.h_pestana_eventos, estado.eventos.size)) }
            )
        }

        when {
            estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

            estado.pestana == PestanaHistorial.MISIONES && estado.misiones.isEmpty() -> Vacio(stringResource(R.string.h_vacio_misiones))
            estado.pestana == PestanaHistorial.EVENTOS && estado.eventos.isEmpty() -> Vacio(stringResource(R.string.h_vacio_eventos))

            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (estado.pestana == PestanaHistorial.MISIONES) {
                    items(estado.misiones, key = { it.id }) { m ->
                        ItemHistorial(m, onClick = { onEvento(EventoHistorial.VerMision(m)) })
                    }
                } else {
                    items(estado.eventos, key = { it.id }) { e -> ItemHistorial(e) }
                }
            }
        }
    }
}

@Composable
private fun Vacio(texto: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario)
    }
}

@Composable
private fun DetalleMision(mision: RegistroMision, estado: HistorialUiState, onEvento: (EventoHistorial) -> Unit, modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onEvento(EventoHistorial.CerrarDetalle) }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.boton_volver))
                }
                Text(mision.nombre, style = MaterialTheme.typography.titleLarge)
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Dato(stringResource(R.string.h_resultado), textoEstadoMision(mision.resultado))
                    Dato(stringResource(R.string.h_robot), mision.robotId)
                    Dato(stringResource(R.string.h_inicio), fechaHora(mision.inicioMs))
                    Dato(stringResource(R.string.h_fin), fechaHora(mision.finMs))
                    Dato(stringResource(R.string.h_puntos), stringResource(R.string.valor_de_total, mision.puntosInspeccionados, mision.totalPuntos))
                    Dato(stringResource(R.string.tele_anomalias), mision.anomalias.toString())
                    Dato(stringResource(R.string.h_bateria), stringResource(R.string.h_bateria_valor, mision.bateriaInicio, mision.bateriaFin))
                }
            }
        }
        item { Text(stringResource(R.string.h_eventos_mision), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
        if (estado.eventosDeLaMision.isEmpty()) {
            item { Text(stringResource(R.string.h_vacio_eventos), color = TextoSecundario) }
        }
        items(estado.eventosDeLaMision, key = { it.id }) { ItemHistorial(it) }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Row {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = TextoSecundario, modifier = Modifier.weight(1f))
        Text(valor, style = MaterialTheme.typography.bodyMedium)
    }
}
