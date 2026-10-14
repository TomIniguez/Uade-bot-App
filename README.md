# Uade-bot-App

TP04 - Desarrollo de Aplicaciones I (UADE). Android app (Kotlin + Jetpack Compose) that works as a remote control for a Unitree robot (G1 / Go2).

The app is developed against the course simulator and, on visit day, controls the real robot by changing only the backend IP.

## Features (planned)
- Connect to the backend (`http://<IP>:8000`) and start a session with a team name
- Move the robot (forward, backward, turn, strafe, curves) with joystick-style controls
- Stop and wave (shake hand on G1)
- Status screen: battery, connection, robot type, team in control

## Stack
Kotlin, Jetpack Compose, Material 3, minSdk 26.

## Workflow
Each feature is developed in its own branch and merged into `main` only after review.
