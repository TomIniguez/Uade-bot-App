# Uade-bot-App

TP04 - Desarrollo de Aplicaciones I (UADE). Android app (Kotlin + Jetpack Compose) that works as a remote control for a Unitree robot (G1 / Go2).

The app is developed against the course simulator and, on visit day, controls the real robot by changing only the backend IP.

## Features
- Connect to the backend (`http://<IP>:8000`) and start a session with a team name
- Move the robot with hold-to-move buttons: forward, backward, turn in place, strafe and curves (`/mover` is repeated every 200 ms while the button is pressed)
- Stop button, plus automatic stop when the app goes to the background
- Actions loaded dynamically from the backend (wave, and shake hand on the G1)
- Status panel: battery, robot connection, robot type and team in control (refreshed every 2 s)
- Readable error messages (session expired, robot busy, no connection, ...)

## Stack
Kotlin, Jetpack Compose, Material 3, MVVM (ViewModel + StateFlow), Retrofit + OkHttp + Gson, minSdk 26. The screen is locked to portrait.

## Project structure
```
app/src/main/java/com/example/tpo_bot_uade_app/
├── MainActivity.kt        // starts the UI, picks the screen
├── data/                  // models, Retrofit API, repository (the only one talking HTTP)
└── ui/                    // ViewModel, UiState, screens and components
```

## How to run
1. Start the backend / simulator (`INICIAR_TP04`). API docs live at `http://<IP>:8000/docs`.
2. Open the project in Android Studio and run the `app` configuration.
3. On the connection screen enter the backend address and a team name, then tap **Conectar**.

### Which IP to use
| Where the app runs | Address |
|---|---|
| Android Studio emulator | `10.0.2.2:8000` (not `localhost`: inside the emulator that is the emulator itself) |
| Physical phone | The IP printed by the simulator script, with the phone on the **same WiFi** |
| Visit day (real robot) | The IP given by the professor, with the phone on the robot's WiFi |

If the phone cannot connect, open `http://<IP>:8000/docs` in the phone browser to check network and firewall.

### Debug APK
`Build > Build APK(s)`, or `./gradlew assembleDebug`. The file is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Workflow
Each feature is developed in its own branch and merged into `main` only after review.
