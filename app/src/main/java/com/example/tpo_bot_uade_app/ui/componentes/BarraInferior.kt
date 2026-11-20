package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Modifier
import com.example.tpo_bot_uade_app.R

// Destinos de la barra inferior.
enum class Destino(val ruta: String, val textoId: Int, val icono: ImageVector) {
    SUPERVISION("supervision", R.string.nav_supervision, Icons.Outlined.Dashboard),
    MISIONES("misiones", R.string.nav_misiones, Icons.Outlined.Route),
    ALERTAS("alertas", R.string.nav_alertas, Icons.Outlined.NotificationsActive),
    HISTORIAL("historial", R.string.nav_historial, Icons.Outlined.History)
}

// Variantes: activo Supervision / Misiones / Alertas / Historial. Alertas lleva contador de activas.
@Composable
fun BarraInferior(rutaActual: String?, alertasActivas: Int, onIr: (Destino) -> Unit) {
    NavigationBar {
        Destino.entries.forEach { destino ->
            val descripcion = if (destino == Destino.ALERTAS && alertasActivas > 0)
                stringResource(R.string.nav_alertas_contador, alertasActivas) else stringResource(destino.textoId)
            NavigationBarItem(
                selected = rutaActual == destino.ruta,
                onClick = { onIr(destino) },
                icon = {
                    if (destino == Destino.ALERTAS && alertasActivas > 0) {
                        BadgedBox(badge = { Badge { Text(alertasActivas.toString()) } }) {
                            Icon(destino.icono, contentDescription = null)
                        }
                    } else {
                        Icon(destino.icono, contentDescription = null)
                    }
                },
                label = { Text(stringResource(destino.textoId)) },
                modifier = Modifier.semantics { contentDescription = descripcion }
            )
        }
    }
}
