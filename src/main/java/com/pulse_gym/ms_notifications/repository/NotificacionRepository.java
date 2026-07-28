package com.pulse_gym.ms_notifications.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pulse_gym.lb_common.entity.notification.Notificacion;

import org.springframework.data.repository.query.Param;

/**
 * Repositorio JPA para la entidad de notificaciones.
 * Gestiona el registro histórico de todas las notificaciones enviadas en el sistema.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    /**
     * Cuenta las notificaciones efectivas (no rechazadas) enviadas por un usuario
     * en el último minuto.
     * Se utiliza para validar el límite de rate limiting por minuto.
     *
     * @param idUsuario Identificador del usuario en el sistema de autenticación
     * @param fecha   Fecha límite (hace 1 minuto desde el momento actual)
     * @return Número de notificaciones efectivas enviadas en el último minuto
     */
    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.id_usuario = :idUsuario AND n.fechaEnvio > :fecha AND n.estado <> 'RECHAZADO'")
    long countEfectivosUltimoMinuto(@Param("idUsuario") Long idUsuario, @Param("fecha") LocalDateTime fecha);

    /**
     * Cuenta las notificaciones efectivas (no rechazadas) enviadas por un usuario
     * en el último día.
     * Se utiliza para validar el límite de rate limiting por día.
     *
     * @param idUsuario Identificador del usuario en el sistema de autenticación
     * @param fecha   Fecha límite (hace 1 día desde el momento actual)
     * @return Número de notificaciones efectivas enviadas en el último día
     */
    @Query("SELECT COUNT(n) FROM Notificacion n WHERE n.id_usuario = :idUsuario AND n.fechaEnvio > :fecha AND n.estado <> 'RECHAZADO'")
    long countEfectivosUltimoDia(@Param("idUsuario") Long idUsuario, @Param("fecha") LocalDateTime fecha);
}
