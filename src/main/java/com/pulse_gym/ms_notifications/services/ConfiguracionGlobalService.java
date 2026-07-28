package com.pulse_gym.ms_notifications.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulse_gym.lb_common.dto.ConfiguracionGlobalRequestDTO;
import com.pulse_gym.lb_common.dto.ConfiguracionGlobalResponseDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.entity.notification.ConfiguracionGlobal;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_notifications.repository.ConfiguracionGlobalRespository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfiguracionGlobalService {

    /**
     * Maximo de notidficaciones por dia
     */
    private static final long DEFAULT_MAX_POR_DIA = 100L;

    /**
     * Maximo de notidficaciones por minuto
     */
    private static final long DEFAULT_MAX_POR_MINUTO = 10L;

    /**
     * Repositorio para la configuracion global de notificaciones
     */
    private final ConfiguracionGlobalRespository configuracionGlobalRespository;

    /**
     * Obtiene la configuracion global del sistema
     *
     * @return Configuracion actual o valores por defecto
     */
    @Transactional(readOnly = true)
    public ConfiguracionGlobalResponseDTO obtenerConfiguracion() {
        ConfiguracionGlobal config = obtenerOInicializarConfiguracion();
        return mapearAResponse(config);
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

        return new MessegeGlobalDTO("Configuracion global actualizada correctamente");
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
     * metodo privado para obtener la configuracion global del sistema o inicializarla
     *
     * @return Configuracion global persistida o nueva configuracion con valores por defecto si no existe ninguna en la base de datos
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
     * Mapea una entidad de ConfiguracionGlobal a un DTO de ConfiguracionGlobalResponseDTO para su uso en respuestas de API. Este método se encarga de extraer los valores relevantes de la entidad y 
     * asignarlos a las propiedades correspondientes del DTO, facilitando así la transferencia de datos entre la capa de servicio y la capa de presentación.
     * @param config 
     * @return 
     */
    private ConfiguracionGlobalResponseDTO mapearAResponse(ConfiguracionGlobal config) {
        ConfiguracionGlobalResponseDTO dto = new ConfiguracionGlobalResponseDTO();
        dto.setMaxNotificacionesPorDia(config.getMax_notificaciones_por_dia());
        dto.setMaxNotificacionesPorMinuto(config.getMax_notificaciones_por_minuto());
        return dto;
    }
}
