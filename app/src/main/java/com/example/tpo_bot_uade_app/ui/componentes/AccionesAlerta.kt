package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.ReglasComando
import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.TipoAlerta
import com.example.tpo_bot_uade_app.domain.model.TipoComando
import com.example.tpo_bot_uade_app.ui.robot.EventoUi
import com.example.tpo_bot_uade_app.ui.robot.RobotUiState

// Botones de una alerta activa: accion sugerida (si tiene) y Reconocer.
// Los incidentes no se reconocen: se resuelven en el DialogoIncidente.
@Composable
fun AccionesAlerta(alerta: Alerta, uiState: RobotUiState, onEvento: (EventoUi) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (alerta.requiereDecision) {
            Text(
                stringResource(R.string.alerta_requiere_decision),
                style = MaterialTheme.typography.labelMedium,
                color = colorDe(NivelVisual.ADVERTENCIA)
            )
            return@Column
        }
        if (alerta.tipo == TipoAlerta.BATERIA_BAJA) {
            BotonComando(
                texto = stringResource(R.string.alerta_sugerida_regresar),
                icono = Icons.AutoMirrored.Outlined.Undo,
                habilitacion = uiState.habilitacionDe(TipoComando.REGRESAR_A_PUNTO_SEGURO),
                telemetria = uiState.telemetria,
                pendiente = uiState.estaPendiente(TipoComando.REGRESAR_A_PUNTO_SEGURO),
                jerarquia = Jerarquia.PRIMARIO,
                onClick = { onEvento(EventoUi.PedirComando(Comando.RegresarAPuntoSeguro)) }
            )
        }
        BotonComando(
            texto = stringResource(R.string.boton_reconocer),
            icono = Icons.Outlined.Done,
            habilitacion = ReglasComando.reconocerAlerta(alerta, uiState.contextoComando),
            telemetria = uiState.telemetria,
            pendiente = uiState.estaPendiente(TipoComando.RECONOCER_ALERTA),
            onClick = { onEvento(EventoUi.PedirComando(Comando.ReconocerAlerta(alerta.id))) }
        )
    }
}
