package com.example.tpo_bot_uade_app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// Cada función es un endpoint. Devuelven Response<T> para poder leer el código HTTP (401, 409...).
interface RobotApi {

    @GET("estado")
    suspend fun estado(): Response<EstadoResponse>

    @GET("acciones")
    suspend fun acciones(): Response<AccionesResponse>

    @POST("sesion/iniciar")
    suspend fun iniciarSesion(@Body body: IniciarSesionRequest): Response<IniciarSesionResponse>

    @POST("sesion/finalizar")
    suspend fun finalizarSesion(
        @Header("X-Robot-Token") token: String,
        @Body body: FinalizarSesionRequest
    ): Response<RespuestaSimple>

    @POST("mover")
    suspend fun mover(
        @Header("X-Robot-Token") token: String,
        @Body body: MoverRequest
    ): Response<MoverResponse>

    @POST("parar")
    suspend fun parar(@Header("X-Robot-Token") token: String): Response<RespuestaSimple>

    @POST("accion/{nombre}")
    suspend fun accion(
        @Header("X-Robot-Token") token: String,
        @Path("nombre") nombre: String
    ): Response<RespuestaSimple>
}
