package com.pulse_gym.ms_notifications.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pulse_gym.lb_common.entity.notification.PlantillaDisenoEmail;
import com.pulse_gym.lb_common.enums.EnumEventoAsociado;

/**
 * Repositorio JPA para la entidad de plantillas de diseño de emails.
 * Gestiona los diseños HTML utilizados para enviar correos electrónicos
 * con formato profesional y personalizado.
 */
@Repository
public interface PlantillaDisenoEmailRepository extends JpaRepository<PlantillaDisenoEmail, Long> {

    /**
     * Busca un diseño activo por su nombre.
     * Excluye los diseños que han sido eliminados (soft delete).
     *
     * @param nombre Nombre del diseño a buscar
     * @return Optional con el diseño encontrado, o vacío si no existe o está eliminado
     */
    Optional<PlantillaDisenoEmail> findByNombreAndEliminadoFalse(String nombre);

    /**
     * Busca un diseño activo asociado a un evento específico.
     * Solo retorna diseños que estén activos y no eliminados.
     * Se utiliza para obtener el diseño apropiado al enviar una notificación
     * basada en un evento del sistema.
     *
     * @param evento Evento asociado al diseño (ej: REGISTRO_USUARIO, LOGIN_USUARIO, etc.)
     * @return Optional con el diseño encontrado, o vacío si no existe para el evento
     */
    Optional<PlantillaDisenoEmail> findByEventoAsociadoAndEliminadoFalseAndActivoTrue(EnumEventoAsociado evento);

    /**
     * Busca el diseño por defecto (utilizado cuando no hay un diseño específico para el evento).
     * El diseño por defecto tiene nombre "default" y debe estar activo.
     *
     * @return Optional con el diseño por defecto, o vacío si no existe
     */
    Optional<PlantillaDisenoEmail> findByNombreAndEliminadoFalseAndActivoTrue(String nombre);
}