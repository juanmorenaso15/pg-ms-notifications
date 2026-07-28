package com.pulse_gym.ms_notifications.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulse_gym.lb_common.entity.notification.ConfiguracionGlobal;

/**
 * Repositorio JPA para la entidad de configuración global del sistema.
 * Gestiona los límites de rate limiting para el envío de notificaciones.
 */
public interface ConfiguracionGlobalRespository extends JpaRepository<ConfiguracionGlobal, Long> {

    /**
     * Obtiene la configuración global activa del sistema.
     * Como solo existe una configuración global, este método retorna el único registro existente.
     * Si no existe ninguna configuración, retorna un Optional vacío.
     *
     * @return Optional con la configuración global, o vacío si no existe
     */
    Optional<ConfiguracionGlobal> findFirstByOrderByIdConfiguracionAsc();
}
