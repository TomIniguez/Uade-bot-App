package com.example.tpo_bot_uade_app

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tpo_bot_uade_app.data.historial.HistorialDatabase
import com.example.tpo_bot_uade_app.data.historial.RegistradorHistorial
import com.example.tpo_bot_uade_app.data.historial.RoomHistorialRepository
import com.example.tpo_bot_uade_app.data.simulador.SimulatedRobotRepository
import com.example.tpo_bot_uade_app.domain.ControlSimulacion
import com.example.tpo_bot_uade_app.domain.HistorialRepository
import com.example.tpo_bot_uade_app.domain.RobotRepository
import com.example.tpo_bot_uade_app.ui.historial.HistorialViewModel
import com.example.tpo_bot_uade_app.ui.pruebas.SimulacionViewModel
import com.example.tpo_bot_uade_app.ui.robot.RobotViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// Inyeccion de dependencias "a mano" (sin Hilt ni Koin): aca se crean los objetos
// una sola vez y se le entregan a quien los necesite.
// El dia que haya un robot real, se cambia la linea de SimulatedRobotRepository y nada mas.
class AppContainer(context: Context) {

    // Corrutinas que viven lo mismo que la app (el tick del simulador y el registro del historial).
    private val scopeApp = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val simulado = SimulatedRobotRepository(scopeApp)

    // Los ViewModels reciben interfaces, nunca la clase concreta.
    val robotRepository: RobotRepository = simulado
    val controlSimulacion: ControlSimulacion = simulado
    val historialRepository: HistorialRepository = RoomHistorialRepository(HistorialDatabase.crear(context).dao())

    // Guarda en el historial todo lo que pasa con el robot, aunque no haya ninguna pantalla abierta.
    val registrador = RegistradorHistorial(context, robotRepository, historialRepository, scopeApp).also { it.iniciar() }

    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { RobotViewModel(robotRepository) }
        initializer { SimulacionViewModel(controlSimulacion, robotRepository) }
        initializer { HistorialViewModel(historialRepository) }
    }
}
