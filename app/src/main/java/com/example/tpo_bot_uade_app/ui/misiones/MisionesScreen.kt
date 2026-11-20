package com.example.tpo_bot_uade_app.ui.misiones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.DefinicionMision
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import com.example.tpo_bot_uade_app.domain.model.TipoComando
import com.example.tpo_bot_uade_app.ui.componentes.BotonComando
import com.example.tpo_bot_uade_app.ui.componentes.Jerarquia
import com.example.tpo_bot_uade_app.ui.componentes.TarjetaMision
import com.example.tpo_bot_uade_app.ui.robot.EventoUi
import com.example.tpo_bot_uade_app.ui.robot.RobotUiState
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario

// Misiones disponibles con su recorrido y duracion. Si hay una en curso, aparece arriba.
@Composable
fun MisionesScreen(uiState: RobotUiState, onEvento: (EventoUi) -> Unit, modifier: Modifier = Modifier) {
    val t = uiState.telemetria
    val enCurso = t?.mision != null && t.estado != EstadoOperativo.EN_BASE

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (enCurso) {
            item {
                Text(stringResource(R.string.misiones_en_curso), style = MaterialTheme.typography.titleMedium)
            }
            item { TarjetaMision(t.mision, antiguo = uiState.datosAntiguos) }
        }
        item {
            Text(stringResource(R.string.misiones_disponibles), style = MaterialTheme.typography.titleLarge)
        }
        items(uiState.misiones, key = { it.id }) { mision ->
            TarjetaDefinicionMision(mision, uiState, onEvento)
        }
    }
}

@Composable
private fun TarjetaDefinicionMision(mision: DefinicionMision, uiState: RobotUiState, onEvento: (EventoUi) -> Unit) {
    val nombres = mision.puntos.mapNotNull { uiState.plano?.punto(it)?.nombre }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(mision.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                nombres.joinToString(" → "),
                style = MaterialTheme.typography.bodyMedium,
                color = TextoSecundario
            )
            Text(
                pluralStringResource(R.plurals.misiones_resumen, mision.puntos.size, mision.puntos.size, mision.duracionEstimadaMin),
                style = MaterialTheme.typography.labelMedium,
                color = TextoSecundario
            )
            BotonComando(
                texto = stringResource(R.string.boton_iniciar),
                icono = Icons.Outlined.PlayArrow,
                habilitacion = uiState.habilitacionDe(TipoComando.INICIAR_MISION),
                telemetria = uiState.telemetria,
                pendiente = uiState.estaPendiente(TipoComando.INICIAR_MISION),
                jerarquia = Jerarquia.PRIMARIO,
                onClick = { onEvento(EventoUi.PedirComando(Comando.IniciarMision(mision.id))) }
            )
        }
    }
}
