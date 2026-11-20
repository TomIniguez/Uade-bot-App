package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.ui.theme.ErrorHmi
import com.example.tpo_bot_uade_app.ui.theme.Texto

// Variantes: normal / destructivo (boton Confirmar en rojo).
@Composable
fun DialogoConfirmacion(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    destructivo: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
        text = { Text(texto) },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = if (destructivo) ButtonDefaults.buttonColors(containerColor = ErrorHmi, contentColor = Texto)
                else ButtonDefaults.buttonColors()
            ) { Text(textoConfirmar) }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.boton_cancelar)) } }
    )
}
