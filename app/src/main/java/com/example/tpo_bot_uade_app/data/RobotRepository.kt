package com.example.tpo_bot_uade_app.data

import android.util.Log
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
class RobotRepository(private val api: RobotApi) {

    var token: String? = null
        private set

    suspend fun estado() = llamar { api.estado() }

    suspend fun acciones() = llamar { api.acciones() }

    suspend fun iniciarSesion(equipo: String): Resultado<IniciarSesionResponse> {
        val r = llamar { api.iniciarSesion(IniciarSesionRequest(equipo)) }
        if (r is Resultado.Ok) token = r.datos.token
        return r
    }

    suspend fun finalizarSesion(): Resultado<RespuestaSimple> {
        val t = token ?: return sinSesion()
        val r = llamar { api.finalizarSesion(t, FinalizarSesionRequest(t)) }
        if (r is Resultado.Ok) token = null
        return r
    }

    suspend fun mover(vx: Double, vy: Double, vyaw: Double): Resultado<MoverResponse> {
        val t = token ?: return sinSesion()
        return llamar { api.mover(t, MoverRequest(vx, vy, vyaw)) }
    }

    suspend fun parar(): Resultado<RespuestaSimple> {
        val t = token ?: return sinSesion()
        return llamar { api.parar(t) }
    }

    suspend fun accion(nombre: String): Resultado<RespuestaSimple> {
        val t = token ?: return sinSesion()
        return llamar { api.accion(t, nombre) }
    }

    // Olvida el token (por ejemplo cuando el servidor dice 401).
    fun olvidarToken() {
        token = null
    }

    private fun sinSesion() = Resultado.Error(401, "La sesión venció. Volvé a conectarte.")

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
            Resultado.Error(null, "No se pudo conectar. Revisá IP y WiFi.")
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

    private fun mensajeParaCodigo(codigo: Int, detalle: String?): String = when (codigo) {
        401 -> "La sesión venció. Volvé a conectarte."
        403 -> detalle ?: "Acción bloqueada por seguridad."
        409 -> detalle ?: "El robot está ocupado por otro equipo."
        422 -> {
            Log.e("RobotRepository", "422 (body mal armado): $detalle")
            "Error interno: pedido mal armado."
        }
        503 -> "El robot no pudo ejecutar la orden."
        else -> detalle ?: "Error inesperado (HTTP $codigo)."
    }
}
