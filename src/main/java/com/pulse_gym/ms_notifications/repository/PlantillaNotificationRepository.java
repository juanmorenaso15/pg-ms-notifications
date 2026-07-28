package com.pulse_gym.ms_notifications.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pulse_gym.lb_common.entity.notification.PlantillaNotificacion;
import com.pulse_gym.lb_common.enums.EnumEventoAsociado;

/**
 * Repositorio JPA para la entidad de plantillas de notificaciones.
 * Gestiona el CRUD de plantillas utilizadas para enviar notificaciones a los usuarios.
 */
@Repository
public interface PlantillaNotificationRepository extends JpaRepository<PlantillaNotificacion, Long> {

    /**
     * Obtiene todas las plantillas que no han sido eliminadas (soft delete).
     * Retorna tanto plantillas activas como inactivas.
     *
     * @return Lista de plantillas no eliminadas
     */
    List<PlantillaNotificacion> findByEliminadaFalse();

    /**
     * Busca plantillas activas y no eliminadas asociadas a un evento específico.
     * Se utiliza para encontrar la plantilla adecuada al enviar una notificación
     * basada en un evento del sistema.
     *
     * @param evento Evento asociado a la plantilla (ej: REGISTRO_USUARIO, LOGIN_USUARIO, etc.)
     * @return Lista de plantillas que coinciden con el evento
     */
    List<PlantillaNotificacion> findByEventosAsociadosContainingAndEstadoTrueAndEliminadaFalse(
            EnumEventoAsociado evento);
}
