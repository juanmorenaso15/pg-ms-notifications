package com.pulse_gym.ms_notifications.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.pulse_gym.lb_common.entity.notification.ConfiguracionGlobal;
import com.pulse_gym.ms_notifications.repository.NotificacionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    /**
     * Inyeccion de repositorio de notificaciones
     */
    private final NotificacionRepository notificacionRepository;

    /**
     * Inyeccion de servicio de configuracion global
     */
    private final ConfiguracionGlobalService configuracionGlobalService;

    /**
     * Valida si el usuario puede recibir otra notificacion segun limites globales
     *
     * @param usuarioId Identificador del usuario en auth
     * @throws RuntimeException Si se supera el limite por minuto o por dia
     */
    public void validarLimiteEnvio(Long usuarioId) {
        ConfiguracionGlobal limites = configuracionGlobalService.obtenerLimitesEfectivos();
        LocalDateTime ahora = LocalDateTime.now();

        long enviadosUltimoMinuto = notificacionRepository.countEfectivosUltimoMinuto(
                usuarioId, ahora.minusMinutes(1));

        if (enviadosUltimoMinuto >= limites.getMax_notificaciones_por_minuto()) {
            throw new RuntimeException("Limite de notificaciones por minuto alcanzado para el usuario");
        }

        long enviadosUltimoDia = notificacionRepository.countEfectivosUltimoDia(
                usuarioId, ahora.minusDays(1));

        if (enviadosUltimoDia >= limites.getMax_notificaciones_por_dia()) {
            throw new RuntimeException("Limite de notificaciones por dia alcanzado para el usuario");
        }
    }
}
