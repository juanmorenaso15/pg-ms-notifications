package com.pulse_gym.ms_notifications.dto;

import com.pulse_gym.lb_common.dto.ConfiguracionGlobalResponseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConfiguracionGlobalExtendResponseDTO extends ConfiguracionGlobalResponseDTO {
    
    /**
     * Indica si el canal de email está habilitado globalmente por entorno
     */
    private boolean emailHabilitado;

    /**
     * Indica si el canal de WhatsApp está habilitado globalmente por entorno
     */
    private boolean whatsappHabilitado;

    /**
     * Estado operativo actual del sistema de notificaciones
     */
    private String estadoSistema;

    /**
     * Mensaje descriptivo del estado actual
     */
    private String mensajeEstado;
}