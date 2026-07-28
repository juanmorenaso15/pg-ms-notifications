package com.pulse_gym.ms_notifications.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulse_gym.lb_common.entity.notification.PreferenciaUsuario;

/**
 * Repositorio JPA para la entidad de preferencias de usuario.
 * Gestiona las preferencias de notificaciones de cada usuario del sistema.
 */
public interface PreferenciaUsuarioRepository extends JpaRepository<PreferenciaUsuario, Long> {

    /**
     * Busca las preferencias de un usuario por su identificador en el sistema de autenticación.
     *
     * @param idUsuario Identificador del usuario en auth
     * @return Optional con las preferencias del usuario, o vacío si no existen
     */
    Optional<PreferenciaUsuario> findByIdUsuario(Long idUsuario);
}
