package com.example.tpo_bot_uade_app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// Cada función es un endpoint. Devuelven Response<T> para poder leer el código HTTP (401, 409...).
interface RobotApi {

    // Estado del robot y de la sesión.
    @GET("estado")
    suspend fun estado(): Response<EstadoResponse>

    // Acciones que el robot puede hacer.
    @GET("acciones")
    suspend fun acciones(): Response<AccionesResponse>

    // Abre la sesión con el nombre del equipo y devuelve el token.
    @POST("sesion/iniciar")
    suspend fun iniciarSesion(@Body body: IniciarSesionRequest): Response<IniciarSesionResponse>

    // Cierra la sesión (el token va en el header y en el body).
    @POST("sesion/finalizar")
    suspend fun finalizarSesion(
        @Header("X-Robot-Token") token: String,
        @Body body: FinalizarSesionRequest
    ): Response<RespuestaSimple>

    // Mueve el robot con velocidades vx, vy y vyaw.
    @POST("mover")
    suspend fun mover(
        @Header("X-Robot-Token") token: String,
        @Body body: MoverRequest
    ): Response<MoverResponse>

    // Detiene el robot.
    @POST("parar")
    suspend fun parar(@Header("X-Robot-Token") token: String): Response<RespuestaSimple>

    // Ejecuta una acción por nombre.
    @POST("accion/{nombre}")
    suspend fun accion(
        @Header("X-Robot-Token") token: String,
        @Path("nombre") nombre: String
    ): Response<RespuestaSimple>
}
