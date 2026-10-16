package com.example.tpo_bot_uade_app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.ui.componentes.BotonMantener

// Pantalla 2: el control remoto. Cruceta 3x3 con los 8 movimientos y PARAR al centro.
@Composable
fun ControlScreen(
    uiState: UiState,
    onDesconectar: () -> Unit,
    onEmpezarMover: (Movimiento) -> Unit,
    onSoltar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habilitado = !uiState.cargando

    // Cada botón de movimiento ocupa una celda de la grilla.
    @Composable
    fun RowScope.Mover(texto: String, movimiento: Movimiento) {
        BotonMantener(
            texto = texto,
            onApretar = { onEmpezarMover(movimiento) },
            onSoltar = onSoltar,
            habilitado = habilitado,
            modifier = Modifier.weight(1f).fillMaxSize()
        )
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Equipo: ${uiState.equipo}", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onDesconectar, enabled = habilitado) { Text("Desconectar") }
        }

        uiState.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Mover("↖ Curva izq", Velocidades.CURVA_IZQUIERDA)
                Mover("▲ Adelante", Velocidades.ADELANTE)
                Mover("Curva der ↗", Velocidades.CURVA_DERECHA)
            }
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Mover("◀ Costado", Velocidades.COSTADO_IZQUIERDA)
                // PARAR es un botón normal: con un toque basta.
                Button(
                    onClick = onSoltar,
                    enabled = habilitado,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f).fillMaxSize()
                ) { Text("■ PARAR", style = MaterialTheme.typography.titleLarge) }
                Mover("Costado ▶", Velocidades.COSTADO_DERECHA)
            }
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Mover("⟲ Girar izq", Velocidades.GIRAR_IZQUIERDA)
                Mover("▼ Atrás", Velocidades.ATRAS)
                Mover("Girar der ⟳", Velocidades.GIRAR_DERECHA)
            }
        }
    }
}
