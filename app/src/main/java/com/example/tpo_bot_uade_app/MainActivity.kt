package com.example.tpo_bot_uade_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tpo_bot_uade_app.data.Resultado
import com.example.tpo_bot_uade_app.data.RobotRepository
import com.example.tpo_bot_uade_app.data.crearApi
import com.example.tpo_bot_uade_app.ui.theme.TPO_Bot_UADE_AppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TPO_Bot_UADE_AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PruebaEstado(Modifier.padding(innerPadding).padding(16.dp))
                }
            }
        }
    }
}

// Pantalla temporal de la Fase 1: sólo para probar que hablamos con el backend.
@Composable
fun PruebaEstado(modifier: Modifier = Modifier) {
    var ip by remember { mutableStateOf("10.0.2.2:8000") }
    var texto by remember { mutableStateOf("Sin consultar") }
    val scope = rememberCoroutineScope()

    Column(modifier) {
        OutlinedTextField(value = ip, onValueChange = { ip = it }, label = { Text("IP:puerto") })
        Button(onClick = {
            // Las llamadas de red van en una coroutine, nunca en el hilo principal.
            scope.launch {
                texto = when (val r = RobotRepository(crearApi(ip)).estado()) {
                    is Resultado.Ok -> {
                        val e = r.datos
                        "Robot: ${e.tipo_robot} (${e.nombre_robot})\nBatería: ${e.bateria}%\nConectado: ${e.conectado}"
                    }
                    is Resultado.Error -> r.mensaje
                }
            }
        }) { Text("Consultar /estado") }
        Text(texto)
    }
}
