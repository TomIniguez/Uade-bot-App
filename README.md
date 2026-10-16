# Uade-bot-App

TP04 - Desarrollo de Aplicaciones I (UADE). App Android (Kotlin + Jetpack Compose) que funciona como control remoto de un robot Unitree (G1 / Go2).

La app se desarrolla contra el simulador de la cátedra y, el día de la visita, controla el robot real cambiando solamente la IP del backend.

## Funcionalidades
- Conexión al backend (`http://<IP>:8000`) e inicio de sesión con el nombre del equipo
- Movimiento del robot con botones de "mantener apretado": adelante, atrás, giro sobre su eje, costado y curvas (mientras el botón está apretado se repite `/mover` cada 200 ms)
- Botón de parar, y parada automática cuando la app pasa a segundo plano
- Acciones cargadas dinámicamente desde el backend (saludar y, en el G1, dar la mano)
- Panel de estado: batería, conexión del robot, tipo de robot y equipo que tiene el control (se actualiza cada 2 s)
- Mensajes de error legibles (sesión vencida, robot ocupado, sin conexión, etc.)

## Tecnologías
Kotlin, Jetpack Compose, Material 3, MVVM (ViewModel + StateFlow), Retrofit + OkHttp + Gson, minSdk 26. La pantalla está bloqueada en vertical.

## Estructura del proyecto
```
app/src/main/java/com/example/tpo_bot_uade_app/
├── MainActivity.kt        // arranca la UI y elige la pantalla
├── data/                  // modelos, API de Retrofit y repository (el único que habla HTTP)
└── ui/                    // ViewModel, UiState, pantallas y componentes
```

## Cómo correr la app
1. Levantar el backend / simulador (`INICIAR_TP04`). La documentación de la API está en `http://<IP>:8000/docs`.
2. Abrir el proyecto en Android Studio y ejecutar la configuración `app`.
3. En la pantalla de conexión ingresar la dirección del backend y el nombre del equipo, y tocar **Conectar**.

### Qué IP usar
| Dónde corre la app | Dirección |
|---|---|
| Emulador de Android Studio | `10.0.2.2:8000` (no `localhost`: dentro del emulador, `localhost` es el propio emulador) |
| Celular físico | La IP que muestra el script del simulador, con el celular en la **misma red WiFi** |
| Día de la visita (robot real) | La IP que da el profesor, con el celular en la WiFi del robot |

Si el celular no logra conectarse, abrir `http://<IP>:8000/docs` en el navegador del celular para verificar la red y el firewall.

### APK de debug
`Build > Build APK(s)`, o `./gradlew assembleDebug`. El archivo se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Flujo de trabajo
Cada funcionalidad se desarrolla en su propia rama y se mergea a `main` recién después de la revisión.
