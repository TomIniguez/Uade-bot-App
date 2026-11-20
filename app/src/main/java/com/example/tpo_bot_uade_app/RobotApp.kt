package com.example.tpo_bot_uade_app

import android.app.Application

// Arranca antes que cualquier pantalla y vive mientras la app este abierta.
// Su unico trabajo es crear el AppContainer (una sola vez).
class RobotApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}
