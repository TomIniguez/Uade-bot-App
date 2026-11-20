package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.tpo_bot_uade_app.R

// Destinos de la barra inferior. Por ahora Supervision y Misiones;
// Alertas (fase 5) e Historial (fase 7) se suman despues.
enum class Destino(val ruta: String, val textoId: Int, val icono: ImageVector) {
    SUPERVISION("supervision", R.string.nav_supervision, Icons.Outlined.Dashboard),
    MISIONES("misiones", R.string.nav_misiones, Icons.Outlined.Route)
}

@Composable
fun BarraInferior(rutaActual: String?, onIr: (Destino) -> Unit) {
    NavigationBar {
        Destino.entries.forEach { destino ->
            NavigationBarItem(
                selected = rutaActual == destino.ruta,
                onClick = { onIr(destino) },
                icon = { Icon(destino.icono, contentDescription = null) },
                label = { Text(stringResource(destino.textoId)) }
            )
        }
    }
}
