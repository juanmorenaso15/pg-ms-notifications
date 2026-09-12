package com.pulse_gym.ms_notifications.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulse_gym.lb_common.dto.ConfiguracionGlobalRequestDTO;
import com.pulse_gym.lb_common.dto.ConfiguracionGlobalResponseDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.entity.notification.ConfiguracionGlobal;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_notifications.dto.ConfiguracionGlobalExtendResponseDTO;
import com.pulse_gym.ms_notifications.repository.ConfiguracionGlobalRespository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfiguracionGlobalService {

    /**
     * Maximo de notificaciones por dia por defecto
     */
    private static final long DEFAULT_MAX_POR_DIA = 100L;

    /**
     * Maximo de notificaciones por minuto por defecto
     */
    private static final long DEFAULT_MAX_POR_MINUTO = 10L;

    /**
     * Repositorio para la configuracion global de notificaciones
     */
    private final ConfiguracionGlobalRespository configuracionGlobalRespository;

    /**
     * Indica si el envío de emails está habilitado por configuración de entorno
     */
    @Value("${notificaciones.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Indica si el envío de WhatsApp está habilitado por configuración de entorno
     */
    @Value("${notificaciones.whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    /**
     * Obtiene la configuracion global del sistema con los estados reales de los
     * canales
     *
     * @return Configuracion actual extendida o valores por defecto
     */
    @Transactional(readOnly = true)
    public ConfiguracionGlobalResponseDTO obtenerConfiguracion() {
        ConfiguracionGlobal config = obtenerOInicializarConfiguracion();
        return mapearAResponseExtendido(config);
    }

    /**
     * Actualiza los limites globales de notificaciones
     *
     * @param request Datos de configuracion
     * @param userRol Rol del usuario autenticado
     * @return Mensaje de confirmacion
     */
    @Transactional
    public MessegeGlobalDTO actualizarConfiguracion(ConfiguracionGlobalRequestDTO request, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);

        ConfiguracionGlobal config = obtenerOInicializarConfiguracion();
        config.setMax_notificaciones_por_dia(request.getMaxNotificacionesPorDia());
        config.setMax_notificaciones_por_minuto(request.getMaxNotificacionesPorMinuto());
        configuracionGlobalRespository.save(config);

        return new MessegeGlobalDTO("Configuración global actualizada correctamente");
    }

    /**
     * Obtiene los limites efectivos para rate limiting
     *
     * @return Configuracion persistida o por defecto
     */
    @Transactional
    public ConfiguracionGlobal obtenerLimitesEfectivos() {
        return obtenerOInicializarConfiguracion();
    }

    /**
     * Metodo privado para obtener la configuracion global del sistema o
     * inicializarla
     *
     * @return Configuracion global persistida o nueva configuracion con valores por
     *         defecto si no existe ninguna en la base de datos
     */
    private ConfiguracionGlobal obtenerOInicializarConfiguracion() {
        return configuracionGlobalRespository.findFirstByOrderByIdConfiguracionAsc()
                .orElseGet(() -> {
                    ConfiguracionGlobal config = new ConfiguracionGlobal();
                    config.setMax_notificaciones_por_dia(DEFAULT_MAX_POR_DIA);
                    config.setMax_notificaciones_por_minuto(DEFAULT_MAX_POR_MINUTO);
                    return configuracionGlobalRespository.save(config);
                });
    }

    /**
     * Mapea una entidad de ConfiguracionGlobal al DTO extendido de respuesta,
     * incorporando
     * los límites de base de datos junto con el estado real de los canales y del
     * sistema.
     * 
     * @param config Entidad de configuración global
     * @return DTO de respuesta con los estados y límites mapeados
     */
    private ConfiguracionGlobalExtendResponseDTO mapearAResponseExtendido(ConfiguracionGlobal config) {
        ConfiguracionGlobalExtendResponseDTO dto = new ConfiguracionGlobalExtendResponseDTO();
        dto.setMaxNotificacionesPorDia(config.getMax_notificaciones_por_dia());
        dto.setMaxNotificacionesPorMinuto(config.getMax_notificaciones_por_minuto());

        // Datos reales inyectados desde el entorno y estado operativo
        dto.setEmailHabilitado(emailEnabled);
        dto.setWhatsappHabilitado(whatsappEnabled);
        dto.setEstadoSistema("ACTIVO");
        dto.setMensajeEstado("El sistema de notificaciones está funcionando correctamente.");

        return dto;
    }
}