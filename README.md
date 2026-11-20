# HMI Go2 — Ronda de inspección (Uade-bot-App)

Trabajo práctico de Desarrollo de Aplicaciones I (UADE). App Android que funciona como **interfaz de operación y supervisión (HMI)** de un robot cuadrúpedo **Unitree Go2** que hace la **ronda nocturna de inspección en un depósito logístico**: recorre puntos de control, mide temperatura y reporta anomalías, mientras el guardia de turno supervisa y decide desde el celular.

**El robot es simulado dentro de la app.** Funciona sin robot, sin servidor y sin internet (la app ni siquiera pide permiso de red).

> Dos ideas que la app muestra siempre:
> - **Una orden enviada no es una orden ejecutada.** Cada orden muestra su estado: pendiente, confirmada, rechazada o sin respuesta.
> - **La detención operativa es una orden de software simulada. No reemplaza la parada de emergencia física del robot.**

## Funcionalidades

- **Selección de robot**: tres Go2 (uno disponible, uno con batería baja y uno fuera de línea).
- **Supervisión**: estado operativo, misión con progreso, mini mapa del depósito, temperatura / puntos inspeccionados / anomalías, última orden con su estado y barra de control.
- **Telemetría detallada**: conexión, señal, última actualización, batería, estado, misión, progreso, posición, velocidad, rumbo, variables del servicio y alertas.
- **Misiones**: tres rondas predefinidas; iniciar con confirmación; botón deshabilitado con el motivo cuando no se puede.
- **Control**: pausar, reanudar, cancelar, regresar a punto seguro y **detención operativa** (un toque, siempre visible).
- **Alertas**: activas y reconocidas; batería baja/crítica, obstáculo, anomalía térmica, conexión recuperada, pausa automática.
- **Incidentes**: diálogo de decisión del operador (reintentar / omitir punto, reinspeccionar / continuar).
- **Desconexión**: banner "Sin conexión — datos de hace N s", datos atenuados, órdenes *sin respuesta* a los 3 s, pausa automática y aviso de recuperación.
- **Historial persistente** (Room): misiones y eventos guardados en el teléfono, con detalle por misión.
- **Panel de pruebas ("Solo demostración")**: forzar desconexión, rechazar la próxima orden, fijar batería, inyectar obstáculo o anomalía, velocidad ×1/×2/×4 y reinicio.

## Tecnologías

Kotlin, Jetpack Compose (Material 3), MVVM con `ViewModel` + `StateFlow`, Navigation Compose, Room (con KSP), corrutinas. Inyección de dependencias manual (`AppContainer`, sin Hilt). minSdk 26. Tema oscuro único, pantalla vertical.

## Arquitectura

```
UI (Compose)  ──eventos──▶  ViewModels (StateFlow)  ──▶  Interfaces del dominio
                                                         ▲            ▲
                                       SimulatedRobotRepository   RoomHistorialRepository
                                       + SimuladorRobot           + Room
```

- La UI **solo observa estado y envía eventos**; nunca toca el simulador.
- Los ViewModels conocen **interfaces** (`RobotRepository`, `HistorialRepository`, `ControlSimulacion`), nunca clases concretas.
- Para un robot real se escribe otra implementación de `RobotRepository` y se cambia una línea en `AppContainer.kt`.

```
app/src/main/java/com/example/tpo_bot_uade_app/
├── MainActivity.kt, RobotApp.kt, AppContainer.kt
├── domain/        modelos, interfaces, ReglasComando (qué botón se habilita), Umbrales
├── data/
│   ├── simulador/ CasoDeUso (depósito, robots, misiones), SimuladorRobot, SimulatedRobotRepository
│   └── historial/ Room: entidades, DAO, base, repositorio y RegistradorHistorial
└── ui/            robot (ViewModel principal), seleccion, supervision, telemetria, misiones,
                   alertas, historial, pruebas, navegacion, componentes, theme
```

## Cómo instalar y correr

**Desde Android Studio:** abrir el proyecto y ejecutar la configuración `app` (emulador o celular).

**APK:** `Build > Build APK(s)` o `./gradlew assembleDebug`. Se genera en `app/build/outputs/apk/debug/app-debug.apk`. Para instalarlo en un celular: copiar el archivo y abrirlo (hay que permitir "instalar apps desconocidas"), o con el celular conectado por USB: `adb install app-debug.apk`.

**Tests (JUnit, sin emulador):** `./gradlew testDebugUnitTest`. Cubren las reglas de habilitación, el simulador (misiones, pausas, batería, incidentes, enlace), el repositorio simulado (latencia, rechazo, sin respuesta) y el ViewModel (flujo de órdenes, incidentes y desconexión).

## Escenarios de demostración (resumen)

Preparación: conectar **Go2 Depósito Norte** → ícono del matraz (Panel de pruebas) → velocidad **×4**.

| # | Escenario | Cómo |
|---|---|---|
| 1 | Misión normal | Misiones → Ronda corta → Iniciar → confirmar. Ver Pendiente → Confirmada, progreso, regreso y misión Completada en Historial |
| 2 | Batería baja | Con misión en curso: Panel → 21 % (o 11 % para el regreso automático) |
| 3 | Desconexión | Con misión en curso: Panel → Forzar desconexión → tocar Pausar → esperar → restaurar |
| 4 | Incidente | Con misión en curso: Panel → Inyectar obstáculo → elegir Omitir punto |
| 5 | Anomalía térmica | Iniciar Verificación de sala eléctrica → Panel → Inyectar anomalía térmica |

Extra: conectar **Go2 Depósito Sur** (24 %) para ver *Iniciar* deshabilitado con su motivo; Panel → *Rechazar la próxima orden* para ver una orden **Rechazada**.

## Documentación

Los documentos de la entrega (contrato JSON de mensajes, registro de decisiones y limitaciones, guiones de escenarios con capturas y diagrama de arquitectura) se entregan por separado.

El contrato de mensajes es **propio del trabajo práctico**: no es un protocolo ni una API oficial de Unitree.

## Versión anterior

La versión anterior de la app (control remoto por HTTP contra el backend de la cátedra) quedó en el tag `v1-control-remoto`.

## Flujo de trabajo

Cada fase se desarrolla en su propia rama y se mergea a `develop`. `main` toma `develop` cuando el grupo lo decide.
