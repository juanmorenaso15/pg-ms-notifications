package com.pulse_gym.ms_notifications.util;

import com.pulse_gym.lb_common.enums.EnumCanalNotificacion;
import com.pulse_gym.lb_common.enums.EnumEventoAsociado;
import com.pulse_gym.lb_common.enums.EnumPreferenciaUsuario;

/**
 * Utilidades para validar eventos, canales y categorias de notificaciones
 */
public final class EventoNotificacionUtil {

    private EventoNotificacionUtil() {
    }

    /**
     * Indica si el evento pertenece a la categoria de logros
     *
     * @param evento Tipo de evento
     * @return true si es un evento de logros
     */
    public static boolean esLogro(EnumEventoAsociado evento) {
        return evento == EnumEventoAsociado.ACHIEVEMENT;
    }

    /**
     * Indica si el evento pertenece a la categoria de mantenimiento
     *
     * @param evento Tipo de evento
     * @return true si es un evento de mantenimiento
     */
    public static boolean esMantenimiento(EnumEventoAsociado evento) {
        return evento == EnumEventoAsociado.MAINTENANCE_ALERT;
    }

    /**
     * Indica si el evento pertenece a la categoria promocional
     *
     * @param evento Tipo de evento
     * @return true si es un evento promocional
     */
    public static boolean esPromocion(EnumEventoAsociado evento) {
        return evento == EnumEventoAsociado.PROMOTION
                || evento == EnumEventoAsociado.PAYMENT_REMINDER
                || evento == EnumEventoAsociado.WELCOME;
    }

    /**
     * Valida si el canal esta habilitado segun la preferencia del usuario
     *
     * @param preferencia Preferencia de canal del usuario
     * @param canal       Canal solicitado
     * @return true si el canal esta permitido
     */
    public static boolean canalHabilitado(EnumPreferenciaUsuario preferencia, EnumCanalNotificacion canal) {
        if (preferencia == null || preferencia == EnumPreferenciaUsuario.NINGUNO) {
            return false;
        }
        if (preferencia == EnumPreferenciaUsuario.AMBOS) {
            return true;
        }
        if (canal == EnumCanalNotificacion.EMAIL) {
            return preferencia == EnumPreferenciaUsuario.EMAIL;
        }
        return preferencia == EnumPreferenciaUsuario.WHATSAPP;
    }
}
