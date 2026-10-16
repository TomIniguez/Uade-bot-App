package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

// Botón "mantener apretado": avisa cuando se aprieta y cuando se suelta.
// Un Button común con onClick no sirve porque sólo avisa al soltar.
@Composable
fun BotonMantener(
    texto: String,
    onApretar: () -> Unit,
    onSoltar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    var apretado by remember { mutableStateOf(false) }
    val colores = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (apretado) colores.primary else colores.primaryContainer)
            .alpha(if (habilitado) 1f else 0.4f)
            .pointerInput(habilitado) {
                if (!habilitado) return@pointerInput
                detectTapGestures(onPress = {
                    apretado = true
                    onApretar()
                    // Espera hasta que el dedo se suelta (o el gesto se cancela).
                    // El finally garantiza que siempre se avise, aunque el botón se deshabilite a mitad.
                    try {
                        tryAwaitRelease()
                    } finally {
                        apretado = false
                        onSoltar()
                    }
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            color = if (apretado) colores.onPrimary else colores.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
