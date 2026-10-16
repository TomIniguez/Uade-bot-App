package com.example.tpo_bot_uade_app.data

// Data classes que representan el JSON que manda y recibe el backend.
// Los campos con "?" pueden faltar en la respuesta.

data class EstadoResponse(
    val conectado: Boolean,
    val bateria: Int?,
    val tipo_robot: String?,
    val nombre_robot: String?,
    val sesion_activa: Boolean,
    val equipo_activo: String?
)

data class IniciarSesionRequest(val equipo: String)

data class IniciarSesionResponse(
    val ok: Boolean,
    val equipo: String?,
    val token: String,
    val timeout_minutos: Int?
)

data class FinalizarSesionRequest(val token: String)

data class MoverRequest(val vx: Double, val vy: Double, val vyaw: Double)

data class MoverResponse(
    val ok: Boolean,
    val vx: Double?,
    val vy: Double?,
    val vyaw: Double?,
    val recortado: Boolean?,
    val pedido: MoverRequest?,
    val mensaje: String?
)

data class Accion(val nombre: String, val descripcion: String?)

data class AccionesResponse(val tipo_robot: String?, val acciones: List<Accion>)

// Respuesta genérica: sirve para /parar, /sesion/finalizar y /accion/{nombre}
data class RespuestaSimple(val ok: Boolean, val mensaje: String?, val accion: String? = null)
