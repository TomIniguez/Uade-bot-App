package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.model.DefinicionMision
import com.example.tpo_bot_uade_app.domain.model.Plano
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.ui.theme.ErrorHmi
import com.example.tpo_bot_uade_app.ui.theme.Fondo
import com.example.tpo_bot_uade_app.ui.theme.Ok
import com.example.tpo_bot_uade_app.ui.theme.Primario
import com.example.tpo_bot_uade_app.ui.theme.SuperficieAlta
import com.example.tpo_bot_uade_app.ui.theme.Texto
import com.example.tpo_bot_uade_app.ui.theme.TextoSecundario
import kotlin.math.cos
import kotlin.math.sin

// Plano del deposito dibujado a mano con Canvas: puntos de control, recorrido de la mision,
// puntos ya inspeccionados (verde), omitidos (rojo) y el robot con una linea que marca su rumbo.
// Variantes: normal / antiguo (atenuado).
@Composable
fun MiniMapa(
    plano: Plano,
    telemetria: Telemetria?,
    mision: DefinicionMision?,
    modifier: Modifier = Modifier,
    antiguo: Boolean = false
) {
    val medidor = rememberTextMeasurer()
    val descripcion = stringResource(R.string.mapa_descripcion, telemetria?.posicion?.zona ?: "")
    val estiloEtiqueta = TextStyle(color = TextoSecundario, fontSize = 10.sp)

    Card(
        modifier
            .fillMaxWidth()
            .alpha(if (antiguo) ALFA_ANTIGUO else 1f)
            .semantics { contentDescription = descripcion }
    ) {
        Canvas(Modifier.fillMaxWidth().aspectRatio(plano.anchoM / plano.altoM)) {
            val margen = 12.dp.toPx()
            val escala = minOf((size.width - 2 * margen) / plano.anchoM, (size.height - 2 * margen) / plano.altoM)
            // El plano tiene el origen abajo a la izquierda; la pantalla, arriba a la izquierda.
            fun aPantalla(x: Float, y: Float) = Offset(margen + x * escala, size.height - margen - y * escala)

            // Contorno del deposito
            drawRoundRect(
                color = SuperficieAlta,
                topLeft = aPantalla(0f, plano.altoM),
                size = androidx.compose.ui.geometry.Size(plano.anchoM * escala, plano.altoM * escala),
                cornerRadius = CornerRadius(6.dp.toPx()),
                style = Stroke(1.dp.toPx())
            )

            // Recorrido de la mision: base -> puntos -> base (linea punteada)
            val m = telemetria?.mision
            if (mision != null) {
                val ids = listOf(plano.base.id) + mision.puntos + plano.base.id
                val camino = Path()
                ids.mapNotNull { plano.punto(it) }.forEachIndexed { i, p ->
                    val o = aPantalla(p.x, p.y)
                    if (i == 0) camino.moveTo(o.x, o.y) else camino.lineTo(o.x, o.y)
                }
                drawPath(
                    camino,
                    color = Primario.copy(alpha = 0.5f),
                    style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                )
            }

            // Puntos de control
            plano.puntos.forEach { p ->
                val o = aPantalla(p.x, p.y)
                val indice = mision?.puntos?.indexOf(p.id) ?: -1
                val omitido = m != null && p.id in m.puntosOmitidos
                val hecho = m != null && indice in 0 until m.puntosCompletados && !omitido
                val enRecorrido = indice >= 0
                val color = when {
                    omitido -> ErrorHmi
                    hecho -> Ok
                    p.id == plano.base.id -> Texto
                    enRecorrido -> Primario
                    else -> TextoSecundario
                }
                if (hecho || p.id == plano.base.id) {
                    drawCircle(color, radius = 6.dp.toPx(), center = o)
                } else {
                    drawCircle(Fondo, radius = 6.dp.toPx(), center = o)
                    drawCircle(color, radius = 6.dp.toPx(), center = o, style = Stroke(2.dp.toPx()))
                }
                val etiqueta = if (p.id == plano.base.id) "B" else p.id
                drawText(medidor, etiqueta, topLeft = Offset(o.x + 8.dp.toPx(), o.y - 16.dp.toPx()), style = estiloEtiqueta)
            }

            // Robot: circulo + linea de rumbo
            telemetria?.let { t ->
                val o = aPantalla(t.posicion.x, t.posicion.y)
                val rad = Math.toRadians(t.rumboGrados.toDouble())
                val largo = 14.dp.toPx()
                val punta = Offset(o.x + (cos(rad) * largo).toFloat(), o.y - (sin(rad) * largo).toFloat())
                drawLine(Primario, o, punta, strokeWidth = 3.dp.toPx())
                drawCircle(Primario, radius = 8.dp.toPx(), center = o)
                drawCircle(Fondo, radius = 3.dp.toPx(), center = o)
            }
        }
    }
}
