package com.example.tpo_bot_uade_app.ui.componentes

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.tpo_bot_uade_app.R
import com.example.tpo_bot_uade_app.domain.MotivoBloqueo
import com.example.tpo_bot_uade_app.domain.Umbrales
import com.example.tpo_bot_uade_app.domain.model.EstadoComando
import com.example.tpo_bot_uade_app.domain.model.EstadoMision
import com.example.tpo_bot_uade_app.domain.model.Telemetria
import com.example.tpo_bot_uade_app.domain.model.TipoComando

// Traducciones de los enums del dominio a texto de strings.xml.

@Composable
fun textoComando(tipo: TipoComando): String = stringResource(
    when (tipo) {
        TipoComando.INICIAR_MISION -> R.string.cmd_iniciar
        TipoComando.PAUSAR_MISION -> R.string.cmd_pausar
        TipoComando.REANUDAR_MISION -> R.string.cmd_reanudar
        TipoComando.CANCELAR_MISION -> R.string.cmd_cancelar
        TipoComando.REGRESAR_A_PUNTO_SEGURO -> R.string.cmd_regresar
        TipoComando.DETENCION_OPERATIVA -> R.string.cmd_detencion
        TipoComando.RECONOCER_ALERTA -> R.string.cmd_reconocer
        TipoComando.RESOLVER_INCIDENTE -> R.string.cmd_resolver
    }
)

@Composable
fun textoEstadoComando(estado: EstadoComando): String = stringResource(
    when (estado) {
        EstadoComando.PENDIENTE -> R.string.ec_pendiente
        EstadoComando.CONFIRMADA -> R.string.ec_confirmada
        EstadoComando.RECHAZADA -> R.string.ec_rechazada
        EstadoComando.SIN_RESPUESTA -> R.string.ec_sin_respuesta
    }
)

@Composable
fun textoEstadoMision(estado: EstadoMision): String = stringResource(
    when (estado) {
        EstadoMision.EN_CURSO -> R.string.em_en_curso
        EstadoMision.PAUSADA -> R.string.em_pausada
        EstadoMision.COMPLETADA -> R.string.em_completada
        EstadoMision.CANCELADA -> R.string.em_cancelada
        EstadoMision.ABORTADA -> R.string.em_abortada
    }
)

// Motivo que se muestra debajo de un boton deshabilitado.
@Composable
fun textoMotivo(motivo: MotivoBloqueo, telemetria: Telemetria?): String = when (motivo) {
    MotivoBloqueo.BATERIA_INSUFICIENTE ->
        if (telemetria?.estado == com.example.tpo_bot_uade_app.domain.model.EstadoOperativo.EN_BASE)
            stringResource(R.string.mot_bateria_inicio, telemetria.bateriaPct, Umbrales.BATERIA_MINIMA_INICIO)
        else stringResource(R.string.mot_bateria_reanudar, telemetria?.bateriaPct ?: 0)
    else -> stringResource(
        when (motivo) {
            MotivoBloqueo.SIN_TELEMETRIA -> R.string.mot_sin_telemetria
            MotivoBloqueo.COMANDO_PENDIENTE -> R.string.mot_pendiente
            MotivoBloqueo.SIN_CONEXION -> R.string.mot_sin_conexion
            MotivoBloqueo.DATOS_ANTIGUOS -> R.string.mot_datos_antiguos
            MotivoBloqueo.ROBOT_NO_EN_BASE -> R.string.mot_no_en_base
            MotivoBloqueo.SIN_MISION_EN_CURSO -> R.string.mot_sin_mision
            MotivoBloqueo.MISION_NO_PAUSADA -> R.string.mot_no_pausada
            MotivoBloqueo.SIN_MISION_PARA_CANCELAR -> R.string.mot_sin_mision_cancelar
            MotivoBloqueo.YA_EN_BASE -> R.string.mot_ya_en_base
            MotivoBloqueo.YA_REGRESANDO -> R.string.mot_ya_regresando
            MotivoBloqueo.YA_DETENIDO -> R.string.mot_ya_detenido
            MotivoBloqueo.ALERTA_CRITICA_SIN_RECONOCER -> R.string.mot_alerta_critica
            MotivoBloqueo.SIN_INCIDENTE -> R.string.mot_sin_incidente
            MotivoBloqueo.ALERTA_YA_RECONOCIDA -> R.string.mot_ya_reconocida
            MotivoBloqueo.ALERTA_REQUIERE_DECISION -> R.string.mot_requiere_decision
            MotivoBloqueo.DECISION_NO_VALIDA -> R.string.mot_decision_no_valida
            MotivoBloqueo.BATERIA_INSUFICIENTE -> R.string.mot_bateria_reanudar // no se llega: ya resuelto arriba
        }
    )
}

@Composable
fun textoDecision(decision: com.example.tpo_bot_uade_app.domain.model.DecisionIncidente): String = stringResource(
    when (decision) {
        com.example.tpo_bot_uade_app.domain.model.DecisionIncidente.REINTENTAR -> R.string.dec_reintentar
        com.example.tpo_bot_uade_app.domain.model.DecisionIncidente.OMITIR_PUNTO -> R.string.dec_omitir
        com.example.tpo_bot_uade_app.domain.model.DecisionIncidente.REINSPECCIONAR -> R.string.dec_reinspeccionar
        com.example.tpo_bot_uade_app.domain.model.DecisionIncidente.CONTINUAR -> R.string.dec_continuar
    }
)

@Composable
fun textoTipoAlerta(tipo: com.example.tpo_bot_uade_app.domain.model.TipoAlerta): String = stringResource(
    when (tipo) {
        com.example.tpo_bot_uade_app.domain.model.TipoAlerta.BATERIA_BAJA -> R.string.alerta_bateria_baja
        com.example.tpo_bot_uade_app.domain.model.TipoAlerta.BATERIA_CRITICA -> R.string.alerta_bateria_critica
        com.example.tpo_bot_uade_app.domain.model.TipoAlerta.OBSTACULO -> R.string.alerta_obstaculo
        com.example.tpo_bot_uade_app.domain.model.TipoAlerta.ANOMALIA_TERMICA -> R.string.alerta_anomalia
        com.example.tpo_bot_uade_app.domain.model.TipoAlerta.CONEXION_RECUPERADA -> R.string.alerta_conexion_recuperada
        com.example.tpo_bot_uade_app.domain.model.TipoAlerta.PAUSA_AUTOMATICA -> R.string.alerta_pausa_automatica
    }
)
