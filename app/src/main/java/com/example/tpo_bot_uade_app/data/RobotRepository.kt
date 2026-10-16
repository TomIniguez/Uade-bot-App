package com.example.tpo_bot_uade_app.data

import android.content.Context
import android.util.Log
import com.example.tpo_bot_uade_app.R
import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException

// Resultado propio: o salió bien (Ok) o falló (Error) con un mensaje listo para mostrar.
sealed class Resultado<out T> {
    data class Ok<T>(val datos: T) : Resultado<T>()
    // codigo = null significa que no hubo respuesta (sin red, IP mal, backend apagado)
    data class Error(val codigo: Int?, val mensaje: String) : Resultado<Nothing>()
}

// Es el único que habla con la API. Guarda el token de la sesión en memoria.
class RobotRepository(private val api: RobotApi, private val contexto: Context) {

    var token: String? = null
        private set

    // GET /estado: batería, conexión del robot y quién tiene el control (no pide token).
    suspend fun estado() = llamar { api.estado() }

    // GET /acciones: lista de acciones disponibles según el robot (no pide token).
    suspend fun acciones() = llamar { api.acciones() }

    // POST /sesion/iniciar: pide el control del robot y guarda el token que devuelve.
    suspend fun iniciarSesion(equipo: String): Resultado<IniciarSesionResponse> {
        val r = llamar { api.iniciarSesion(IniciarSesionRequest(equipo)) }
        if (r is Resultado.Ok) token = r.datos.token
        return r
    }

    // POST /sesion/finalizar: libera el control del robot y borra el token.
    suspend fun finalizarSesion(): Resultado<RespuestaSimple> {
        val t = token ?: return sinSesion()
        val r = llamar { api.finalizarSesion(t, FinalizarSesionRequest(t)) }
        if (r is Resultado.Ok) token = null
        return r
    }

    // POST /mover: una orden de velocidad (dura ~0.4 s, por eso se repite mientras se mantiene apretado).
    suspend fun mover(vx: Double, vy: Double, vyaw: Double): Resultado<MoverResponse> {
        val t = token ?: return sinSesion()
        return llamar { api.mover(t, MoverRequest(vx, vy, vyaw)) }
    }

    // POST /parar: frena el robot.
    suspend fun parar(): Resultado<RespuestaSimple> {
        val t = token ?: return sinSesion()
        return llamar { api.parar(t) }
    }

    // POST /accion/{nombre}: ejecuta una acción como saludar o dar la mano.
    suspend fun accion(nombre: String): Resultado<RespuestaSimple> {
        val t = token ?: return sinSesion()
        return llamar { api.accion(t, nombre) }
    }

    // Olvida el token (por ejemplo cuando el servidor dice 401).
    fun olvidarToken() {
        token = null
    }

    // Error que devolvemos sin llamar a la red cuando todavía no hay token.
    private fun sinSesion() = Resultado.Error(401, contexto.getString(R.string.error_sesion_vencida))

    // Envuelve cada request: convierte excepciones y códigos de error en Resultado.Error.
    private suspend fun <T> llamar(bloque: suspend () -> Response<T>): Resultado<T> {
        return try {
            val respuesta = bloque()
            val cuerpo = respuesta.body()
            if (respuesta.isSuccessful && cuerpo != null) {
                Resultado.Ok(cuerpo)
            } else {
                val detalle = leerDetalle(respuesta.errorBody()?.string())
                Resultado.Error(respuesta.code(), mensajeParaCodigo(respuesta.code(), detalle))
            }
        } catch (e: IOException) {
            Log.w("RobotRepository", "Sin respuesta del backend", e)
            Resultado.Error(null, contexto.getString(R.string.error_sin_conexion))
        }
    }

    // FastAPI manda "detail" como texto, como objeto con "mensaje" (409) o como lista (422).
    private fun leerDetalle(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            val detail = JsonParser.parseString(json).asJsonObject.get("detail") ?: return null
            when {
                detail.isJsonPrimitive -> detail.asString
                detail.isJsonObject -> detail.asJsonObject.get("mensaje")?.asString
                else -> detail.toString()
            }
        } catch (e: Exception) {
            null
        }
    }

    // Convierte el código HTTP de error en un mensaje en español para el usuario.
    private fun mensajeParaCodigo(codigo: Int, detalle: String?): String = when (codigo) {
        401 -> contexto.getString(R.string.error_sesion_vencida)
        403 -> detalle ?: contexto.getString(R.string.error_bloqueado)
        409 -> detalle ?: contexto.getString(R.string.error_ocupado)
        422 -> {
            Log.e("RobotRepository", "422 (body mal armado): $detalle")
            contexto.getString(R.string.error_pedido)
        }
        503 -> contexto.getString(R.string.error_robot)
        else -> detalle ?: contexto.getString(R.string.error_http, codigo)
    }
}
