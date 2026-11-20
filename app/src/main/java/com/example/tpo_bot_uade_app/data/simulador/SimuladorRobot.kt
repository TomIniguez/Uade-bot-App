package com.example.tpo_bot_uade_app.data.simulador

import com.example.tpo_bot_uade_app.domain.ReglasComando
import com.example.tpo_bot_uade_app.domain.model.Alerta
import com.example.tpo_bot_uade_app.domain.model.Comando
import com.example.tpo_bot_uade_app.domain.model.DecisionIncidente
import com.example.tpo_bot_uade_app.domain.model.DefinicionMision
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.EstadoConexion
import com.example.tpo_bot_uade_app.domain.model.EstadoMision
import com.example.tpo_bot_uade_app.domain.model.EstadoOperativo
import com.example.tpo_bot_uade_app.domain.model.EventoRobot
import com.example.tpo_bot_uade_app.domain.model.MisionActiva
import com.example.tpo_bot_uade_app.domain.model.Posicion
import com.example.tpo_bot_uade_app.domain.model.RegistroMision
import com.example.tpo_bot_uade_app.domain.model.RespuestaComando
import com.example.tpo_bot_uade_app.domain.model.RobotInfo
import com.example.tpo_bot_uade_app.domain.model.Severidad
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.domain.model.TelemetriaServicio
import com.example.tpo_bot_uade_app.domain.model.TipoAlerta
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random

// El "robot" de mentira. Es Kotlin puro (nada de Android) para poder testearlo con JUnit.
//
// Como funciona:
// - Alguien llama a tick() una vez por segundo simulado. En cada tick el robot se mueve,
//   gasta bateria, mide temperatura y revisa umbrales.
// - procesar() recibe una orden, valida si corresponde en el estado actual (diagrama 3 de
//   arquitectura) y la aplica o la rechaza con un motivo.
// - Las fallas (obstaculo, anomalia, corte de enlace) solo pasan cuando se fuerzan, para
//   que las demos se puedan repetir. El unico azar es el ruido de temperatura y senal.
class SimuladorRobot(
    private val robot: RobotInfo,
    private val random: Random = Random.Default,
    private val reloj: () -> Long = System::currentTimeMillis
) {
    // En que parte del trabajo esta dentro de una mision.
    private enum class Fase { MOVIENDO, INSPECCIONANDO, ESPERANDO_REINTENTO }

    // ---------- Estado del robot ----------
    var estado = EstadoOperativo.EN_BASE
        private set
    private var bateria = robot.bateriaPct.toFloat()
    private var x = CasoDeUso.BASE.x
    private var y = CasoDeUso.BASE.y
    private var rumbo = 0
    private var seMovio = false
    private var temperatura = CasoDeUso.BASE.temperaturaBase
    private var senal = CasoDeUso.SENAL_MAX

    // La bateria se muestra redondeada para arriba, y las reglas usan ese mismo numero,
    // asi la interfaz y el robot nunca discrepan (ej.: 29,5 % se ve 30 % y permite iniciar).
    private val bateriaPct: Int get() = ceil(bateria).toInt()

    // ---------- Mision ----------
    private var mision: DefinicionMision? = null
    private var resultado: EstadoMision? = null    // null = mision abierta
    private var misionCerrada = true               // true = ya se emitio MisionFinalizada
    private var indicePunto = 0                    // punto de la mision hacia el que va
    private var fase = Fase.MOVIENDO
    private var ticksRestantes = 0
    private var origenX = x                        // de donde arranco el tramo actual
    private var origenY = y
    private var progresoCongelado = 0
    private var lecturaForzada: Float? = null
    private var inicioMs = 0L
    private var bateriaInicio = 0
    var puntosInspeccionados = 0
        private set
    var anomalias = 0
        private set
    private val omitidos = mutableSetOf<String>()
    val puntosOmitidos: Set<String> get() = omitidos.toSet()

    // ---------- Alertas e incidentes ----------
    private val listaAlertas = mutableListOf<Alerta>()
    val alertas: List<Alerta> get() = listaAlertas.toList()
    private var incidente: Alerta? = null
    private var anomaliaPendiente = false
    private var avisoBateriaBaja = false
    private var avisoBateriaCritica = false
    private var contadorAlertas = 0

    // ---------- Enlace con la app ----------
    private var enlace = true
    private var ticksSinEnlace = 0
    private var avisarPausaAutomatica = false

    private var rechazarProximo = false
    private val eventosPendientes = mutableListOf<EventoRobot>()

    // =====================================================================
    // Tick: un segundo simulado
    // =====================================================================

    fun tick() {
        seMovio = false

        if (!enlace) {
            ticksSinEnlace++
            if (ticksSinEnlace == CasoDeUso.PAUSA_POR_DESCONEXION_TICKS && estado == EstadoOperativo.EN_MISION) {
                estado = EstadoOperativo.PAUSADO
                avisarPausaAutomatica = true
            }
        }

        when (estado) {
            EstadoOperativo.EN_BASE -> bateria = (bateria + CasoDeUso.CARGA_EN_BASE).coerceAtMost(100f)
            EstadoOperativo.EN_MISION -> avanzarMision()
            EstadoOperativo.REGRESANDO -> if (moverHacia(CasoDeUso.BASE)) llegarABase()
            else -> Unit // PAUSADO, ESPERANDO_DECISION y DETENIDO: quieto
        }

        val consumo = when {
            seMovio -> CasoDeUso.CONSUMO_MOVIMIENTO
            estado == EstadoOperativo.EN_BASE -> 0f
            else -> CasoDeUso.CONSUMO_QUIETO
        }
        bateria = (bateria - consumo).coerceAtLeast(0f)
        revisarBateria()

        temperatura = lecturaForzada ?: medirTemperatura()
        senal = random.nextInt(CasoDeUso.SENAL_MIN, CasoDeUso.SENAL_MAX + 1)
    }

    private fun avanzarMision() {
        val m = mision ?: return
        when (fase) {
            Fase.MOVIENDO -> {
                val destino = CasoDeUso.punto(m.puntos[indicePunto])
                if (moverHacia(destino)) empezarInspeccion(destino)
            }
            Fase.INSPECCIONANDO -> {
                ticksRestantes--
                if (ticksRestantes <= 0) terminarInspeccion()
            }
            Fase.ESPERANDO_REINTENTO -> {
                ticksRestantes--
                if (ticksRestantes <= 0) fase = Fase.MOVIENDO
            }
        }
    }

    // Avanza 0,8 m hacia el punto. Devuelve true si llego.
    private fun moverHacia(p: PuntoControl): Boolean {
        val dx = p.x - x
        val dy = p.y - y
        val distancia = hypot(dx, dy)
        if (distancia < 0.001f) return true
        rumbo = ((Math.toDegrees(atan2(dy, dx).toDouble()).roundToInt() + 360) % 360)
        seMovio = true
        return if (distancia <= CasoDeUso.AVANCE_POR_TICK_M) {
            x = p.x
            y = p.y
            true
        } else {
            x += dx / distancia * CasoDeUso.AVANCE_POR_TICK_M
            y += dy / distancia * CasoDeUso.AVANCE_POR_TICK_M
            false
        }
    }

    private fun empezarInspeccion(p: PuntoControl) {
        fase = Fase.INSPECCIONANDO
        ticksRestantes = CasoDeUso.TICKS_INSPECCION
        if (anomaliaPendiente) {
            anomaliaPendiente = false
            lecturaForzada = CasoDeUso.TEMPERATURA_ANOMALIA
        }
        val lectura = lecturaForzada ?: medirTemperatura()
        if (lectura > CasoDeUso.UMBRAL_TEMPERATURA) {
            lecturaForzada = lectura
            abrirIncidente(TipoAlerta.ANOMALIA_TERMICA, "Anomalía térmica en ${p.nombre}: ${grados(lectura)}")
        }
    }

    private fun terminarInspeccion() {
        puntosInspeccionados++
        lecturaForzada = null
        siguientePunto()
    }

    private fun siguientePunto() {
        val m = mision ?: return
        val actual = CasoDeUso.punto(m.puntos[indicePunto])
        origenX = actual.x
        origenY = actual.y
        indicePunto++
        fase = Fase.MOVIENDO
        if (indicePunto >= m.puntos.size) {
            resultado = EstadoMision.COMPLETADA
            estado = EstadoOperativo.REGRESANDO
        }
    }

    private fun llegarABase() {
        estado = EstadoOperativo.EN_BASE
        val m = mision ?: return
        if (misionCerrada) return
        misionCerrada = true
        eventosPendientes += EventoRobot.MisionFinalizada(
            RegistroMision(
                misionId = m.id,
                nombre = m.nombre,
                robotId = robot.id,
                inicioMs = inicioMs,
                finMs = reloj(),
                resultado = resultado ?: EstadoMision.CANCELADA,
                puntosInspeccionados = puntosInspeccionados,
                totalPuntos = m.puntos.size,
                anomalias = anomalias,
                bateriaInicio = bateriaInicio,
                bateriaFin = bateriaPct
            )
        )
    }

    private fun revisarBateria() {
        val pct = bateriaPct
        if (pct > CasoDeUso.BATERIA_BAJA) avisoBateriaBaja = false
        if (pct > CasoDeUso.BATERIA_CRITICA) avisoBateriaCritica = false

        if (pct <= CasoDeUso.BATERIA_BAJA && !avisoBateriaBaja) {
            avisoBateriaBaja = true
            emitirAlerta(
                TipoAlerta.BATERIA_BAJA, Severidad.ADVERTENCIA,
                "Batería baja ($pct %). Se sugiere regresar a punto seguro"
            )
        }
        if (pct <= CasoDeUso.BATERIA_CRITICA && !avisoBateriaCritica) {
            avisoBateriaCritica = true
            // Diagrama 3: el regreso automatico sale de EN_MISION o PAUSADO.
            val regresa = estado == EstadoOperativo.EN_MISION || estado == EstadoOperativo.PAUSADO
            val mensaje = if (regresa) "Batería crítica ($pct %). Regreso automático a la base"
            else "Batería crítica ($pct %)"
            emitirAlerta(TipoAlerta.BATERIA_CRITICA, Severidad.CRITICA, mensaje)
            if (regresa) {
                cerrarMision(EstadoMision.ABORTADA)
                estado = EstadoOperativo.REGRESANDO
            }
        }
    }

    // =====================================================================
    // Ordenes
    // =====================================================================

    fun procesar(comandoId: String, comando: Comando): RespuestaComando {
        if (rechazarProximo) {
            rechazarProximo = false
            return responder(comandoId, "Rechazo forzado desde el panel de pruebas")
        }
        // Cada rama valida y aplica. Devuelve null si salio bien o el motivo del rechazo.
        val motivo: String? = when (comando) {
            is Comando.IniciarMision -> iniciar(comando.misionId)
            Comando.PausarMision -> pausar()
            Comando.ReanudarMision -> reanudar()
            Comando.CancelarMision -> cancelar()
            Comando.RegresarAPuntoSeguro -> regresar()
            Comando.DetencionOperativa -> detener()
            is Comando.ReconocerAlerta -> reconocer(comando.alertaId)
            is Comando.ResolverIncidente -> resolver(comando.alertaId, comando.decision)
        }
        return responder(comandoId, motivo)
    }

    private fun responder(comandoId: String, motivoRechazo: String?) = RespuestaComando(
        comandoId = comandoId,
        estado = if (motivoRechazo == null) EstadoComando.CONFIRMADA else EstadoComando.RECHAZADA,
        motivo = motivoRechazo,
        timestampMs = reloj()
    )

    private fun iniciar(misionId: String): String? {
        if (estado != EstadoOperativo.EN_BASE) return "El robot no está en la base"
        val def = CasoDeUso.MISIONES.firstOrNull { it.id == misionId } ?: return "Misión desconocida: $misionId"
        if (bateriaPct < CasoDeUso.BATERIA_MINIMA_INICIO) {
            return "Batería insuficiente para iniciar la misión ($bateriaPct %, mínimo ${CasoDeUso.BATERIA_MINIMA_INICIO} %)"
        }
        mision = def
        resultado = null
        misionCerrada = false
        indicePunto = 0
        fase = Fase.MOVIENDO
        origenX = x
        origenY = y
        puntosInspeccionados = 0
        anomalias = 0
        omitidos.clear()
        lecturaForzada = null
        inicioMs = reloj()
        bateriaInicio = bateriaPct
        estado = EstadoOperativo.EN_MISION
        eventosPendientes += EventoRobot.MisionIniciada(def.id, def.nombre, robot.id)
        return null
    }

    private fun pausar(): String? {
        if (estado != EstadoOperativo.EN_MISION) return "No hay una misión en curso para pausar"
        estado = EstadoOperativo.PAUSADO
        return null
    }

    private fun reanudar(): String? {
        if (estado != EstadoOperativo.PAUSADO) return "No hay una misión pausada para reanudar"
        if (bateriaPct <= CasoDeUso.BATERIA_CRITICA) return "Batería insuficiente para reanudar ($bateriaPct %)"
        estado = EstadoOperativo.EN_MISION
        avisarPausaAutomatica = false
        return null
    }

    private fun cancelar(): String? {
        if (estado != EstadoOperativo.EN_MISION && estado != EstadoOperativo.PAUSADO &&
            estado != EstadoOperativo.ESPERANDO_DECISION
        ) return "No hay una misión para cancelar"
        cerrarMision(EstadoMision.CANCELADA)
        estado = EstadoOperativo.REGRESANDO
        return null
    }

    private fun regresar(): String? {
        if (estado == EstadoOperativo.EN_BASE) return "El robot ya está en la base"
        if (estado == EstadoOperativo.REGRESANDO) return "El robot ya está regresando"
        cerrarMision(EstadoMision.CANCELADA) // si venia de DETENIDO, la mision ya estaba ABORTADA y no cambia
        estado = EstadoOperativo.REGRESANDO
        return null
    }

    private fun detener(): String? {
        if (estado == EstadoOperativo.EN_BASE) return "El robot está en la base"
        if (estado == EstadoOperativo.DETENIDO) return "El robot ya está detenido"
        cerrarMision(EstadoMision.ABORTADA)
        estado = EstadoOperativo.DETENIDO
        return null
    }

    private fun reconocer(alertaId: String): String? {
        val alerta = listaAlertas.firstOrNull { it.id == alertaId } ?: return "La alerta no existe"
        if (alerta.reconocida) return "La alerta ya estaba reconocida"
        if (alerta.requiereDecision) return "Esta alerta requiere una decisión del operador"
        marcarReconocida(alertaId)
        return null
    }

    private fun resolver(alertaId: String, decision: DecisionIncidente): String? {
        val inc = incidente
        if (estado != EstadoOperativo.ESPERANDO_DECISION || inc == null || inc.id != alertaId) {
            return "No hay un incidente pendiente con ese identificador"
        }
        if (decision !in ReglasComando.decisionesValidas(inc.tipo)) return "Decisión no válida para este incidente"

        marcarReconocida(inc.id)
        incidente = null
        estado = EstadoOperativo.EN_MISION
        val m = mision ?: return null
        when (decision) {
            DecisionIncidente.REINTENTAR -> {
                fase = Fase.ESPERANDO_REINTENTO
                ticksRestantes = CasoDeUso.TICKS_REINTENTO
            }
            DecisionIncidente.OMITIR_PUNTO -> {
                omitidos += m.puntos[indicePunto]
                lecturaForzada = null
                siguientePunto()
            }
            DecisionIncidente.REINSPECCIONAR -> {
                anomalias++
                fase = Fase.INSPECCIONANDO
                ticksRestantes = CasoDeUso.TICKS_INSPECCION // la lectura alta se mantiene
            }
            DecisionIncidente.CONTINUAR -> {
                anomalias++ // queda para revision humana
                terminarInspeccion()
            }
        }
        return null
    }

    // Pone el resultado final si la mision seguia abierta. Tambien cierra cualquier incidente.
    private fun cerrarMision(final: EstadoMision) {
        if (mision != null && resultado == null) {
            progresoCongelado = progresoAbierto()
            resultado = final
        }
        incidente?.let { marcarReconocida(it.id) }
        incidente = null
        anomaliaPendiente = false
        lecturaForzada = null
    }

    // =====================================================================
    // Fallas inyectables (las usa el panel de pruebas a traves del repositorio)
    // =====================================================================

    fun rechazarProximoComando() {
        rechazarProximo = true
    }

    fun fijarBateria(pct: Int) {
        bateria = pct.coerceIn(0, 100).toFloat()
        revisarBateria()
    }

    // Devuelve false si no corresponde (solo se puede durante una mision en curso).
    fun inyectarObstaculo(): Boolean {
        val m = mision
        if (estado != EstadoOperativo.EN_MISION || m == null || indicePunto >= m.puntos.size) return false
        val destino = CasoDeUso.punto(m.puntos[indicePunto])
        abrirIncidente(TipoAlerta.OBSTACULO, "Obstáculo en el recorrido hacia ${destino.nombre}")
        return true
    }

    fun inyectarAnomalia(): Boolean {
        if (estado != EstadoOperativo.EN_MISION) return false
        anomaliaPendiente = true
        return true
    }

    // Lo llama el repositorio cuando se corta o vuelve el enlace.
    fun fijarEnlace(activo: Boolean) {
        if (activo == enlace) return
        enlace = activo
        if (!activo) {
            ticksSinEnlace = 0
            return
        }
        emitirAlerta(TipoAlerta.CONEXION_RECUPERADA, Severidad.INFO, "Conexión recuperada")
        if (avisarPausaAutomatica) {
            avisarPausaAutomatica = false
            emitirAlerta(TipoAlerta.PAUSA_AUTOMATICA, Severidad.INFO, "Pausa automática por pérdida de enlace")
        }
    }

    // Devuelve los eventos acumulados desde la ultima vez y vacia la lista.
    fun tomarEventos(): List<EventoRobot> {
        val copia = eventosPendientes.toList()
        eventosPendientes.clear()
        return copia
    }

    // =====================================================================
    // Telemetria
    // =====================================================================

    fun telemetria(): Telemetria = Telemetria(
        robotId = robot.id,
        conexion = if (enlace) EstadoConexion.CONECTADO else EstadoConexion.SIN_CONEXION,
        senalPct = if (enlace) senal else 0,
        bateriaPct = bateriaPct,
        estado = estado,
        mision = misionActiva(),
        posicion = Posicion(x, y, puntoMasCercano().nombre),
        velocidadMs = if (seMovio) CasoDeUso.AVANCE_POR_TICK_M else 0f,
        rumboGrados = rumbo,
        alertasActivas = listaAlertas.count { !it.reconocida },
        servicio = TelemetriaServicio(temperatura, puntosInspeccionados, anomalias),
        timestampMs = reloj()
    )

    private fun misionActiva(): MisionActiva? {
        val m = mision ?: return null
        val estadoMision = resultado
            ?: if (estado == EstadoOperativo.PAUSADO) EstadoMision.PAUSADA else EstadoMision.EN_CURSO
        val progreso = when (resultado) {
            null -> progresoAbierto()
            EstadoMision.COMPLETADA -> 100
            else -> progresoCongelado
        }
        val puntoActual = when {
            estado == EstadoOperativo.REGRESANDO -> CasoDeUso.BASE.nombre
            resultado != null -> null
            else -> CasoDeUso.punto(m.puntos[indicePunto]).nombre
        }
        return MisionActiva(m.id, m.nombre, estadoMision, progreso, puntoActual, m.puntos.size)
    }

    // (puntos completados + fraccion del tramo actual) / total. Llega a 100 solo al completar.
    private fun progresoAbierto(): Int {
        val m = mision ?: return 0
        if (indicePunto >= m.puntos.size) return 100
        val fraccion = if (fase == Fase.MOVIENDO) {
            val destino = CasoDeUso.punto(m.puntos[indicePunto])
            val largo = hypot(destino.x - origenX, destino.y - origenY)
            if (largo < 0.001f) 1f else (1f - hypot(destino.x - x, destino.y - y) / largo).coerceIn(0f, 1f)
        } else 1f
        return ((indicePunto + fraccion) / m.puntos.size * 100).toInt().coerceAtMost(99)
    }

    // =====================================================================
    // Ayudantes
    // =====================================================================

    private fun abrirIncidente(tipo: TipoAlerta, mensaje: String) {
        estado = EstadoOperativo.ESPERANDO_DECISION
        incidente = emitirAlerta(tipo, Severidad.CRITICA, mensaje, requiereDecision = true)
    }

    private fun emitirAlerta(
        tipo: TipoAlerta,
        severidad: Severidad,
        mensaje: String,
        requiereDecision: Boolean = false
    ): Alerta {
        contadorAlertas++
        val alerta = Alerta(
            id = "alr-%04d".format(contadorAlertas),
            tipo = tipo,
            severidad = severidad,
            mensaje = mensaje,
            requiereDecision = requiereDecision,
            reconocida = false,
            timestampMs = reloj()
        )
        listaAlertas += alerta
        eventosPendientes += EventoRobot.AlertaEmitida(alerta)
        return alerta
    }

    private fun marcarReconocida(alertaId: String) {
        val i = listaAlertas.indexOfFirst { it.id == alertaId }
        if (i >= 0) listaAlertas[i] = listaAlertas[i].copy(reconocida = true)
    }

    private fun puntoMasCercano(): PuntoControl =
        CasoDeUso.PUNTOS.minBy { hypot(it.x - x, it.y - y) }

    // Temperatura base de la zona mas cercana +/- 0,5 grados de ruido.
    private fun medirTemperatura(): Float =
        puntoMasCercano().temperaturaBase + (random.nextFloat() - 0.5f)

    private fun grados(valor: Float) = String.format(Locale.forLanguageTag("es-AR"), "%.1f °C", valor)
}
