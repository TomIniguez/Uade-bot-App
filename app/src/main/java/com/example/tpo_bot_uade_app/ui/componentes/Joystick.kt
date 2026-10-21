package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.ui.Movimiento
import com.example.tpo_bot_uade_app.ui.Velocidades
import kotlin.math.roundToInt

private const val ZONA_MUERTA = 0.2f

// Joystick analógico: arriba/abajo = adelante/atrás, izquierda/derecha = girar.
// Nunca manda movimiento lateral (un robot de 2 patas se caería).
@Composable
fun Joystick(
    onMover: (Movimiento) -> Unit,
    onSoltar: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }
    var radio by remember { mutableFloatStateOf(1f) }
    val colores = MaterialTheme.colorScheme
    val tamanoPalanca = 72.dp

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(colores.primaryContainer)
            .border(4.dp, colores.primary, CircleShape)
            .alpha(if (habilitado) 1f else 0.4f)
            .onSizeChanged { radio = it.width / 2f }
            .pointerInput(habilitado) {
                if (!habilitado) return@pointerInput

                // Convierte el dedo en un desplazamiento limitado al círculo y en una velocidad.
                var moviendo = false
                fun seguir(posicion: Offset) {
                    val centro = Offset(size.width / 2f, size.height / 2f)
                    var d = posicion - centro
                    val distancia = d.getDistance()
                    if (distancia > radio) d *= radio / distancia
                    desplazamiento = d

                    val x = d.x / radio
                    val y = d.y / radio
                    if (d.getDistance() / radio < ZONA_MUERTA) {
                        if (moviendo) onSoltar()
                        moviendo = false
                    } else {
                        moviendo = true
                        onMover(Movimiento(-y * Velocidades.MAX_VX, 0.0, -x * Velocidades.MAX_VYAW))
                    }
                }
                fun soltar() {
                    desplazamiento = Offset.Zero
                    moviendo = false
                    onSoltar() // siempre avisa: al levantar el dedo el robot frena
                }

                detectDragGestures(
                    onDragStart = { seguir(it) },
                    onDrag = { cambio, _ ->
                        cambio.consume()
                        seguir(cambio.position)
                    },
                    onDragEnd = { soltar() },
                    onDragCancel = { soltar() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Flechas de referencia en los cuatro bordes.
        val flecha = colores.onPrimaryContainer.copy(alpha = 0.6f)
        Text("▲", color = flecha, modifier = Modifier.align(Alignment.TopCenter).padding(12.dp))
        Text("▼", color = flecha, modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp))
        Text("⟲", color = flecha, modifier = Modifier.align(Alignment.CenterStart).padding(12.dp))
        Text("⟳", color = flecha, modifier = Modifier.align(Alignment.CenterEnd).padding(12.dp))

        // La palanca sigue al dedo.
        Box(
            modifier = Modifier
                .offset { IntOffset(desplazamiento.x.roundToInt(), desplazamiento.y.roundToInt()) }
                .size(tamanoPalanca)
                .clip(CircleShape)
                .background(if (desplazamiento == Offset.Zero) colores.primary else colores.tertiary)
        )
    }
}
