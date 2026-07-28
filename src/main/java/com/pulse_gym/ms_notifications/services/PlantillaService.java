package com.pulse_gym.ms_notifications.services;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.dto.PlantillaNotificacionRequestDTO;
import com.pulse_gym.lb_common.entity.notification.PlantillaNotificacion;
import com.pulse_gym.lb_common.enums.EnumEventoAsociado;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_notifications.repository.PlantillaNotificationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlantillaService {

    /**
    * Repositorio para la gestion de plantillas de notificaciones
    */
    private final PlantillaNotificationRepository plantillaNotificationRepository;

    /**
     * Registra una nueva plantilla de notificacion en la base de datos
     *
     * @param request Datos de la plantilla
     * @param userRol Rol del usuario autenticado
     * @return Mensaje de confirmacion
     */
    @Transactional
    public MessegeGlobalDTO crearPlantilla(PlantillaNotificacionRequestDTO request, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);

        Set<EnumEventoAsociado> eventos = resolverEventos(request);
        PlantillaNotificacion notificacion = new PlantillaNotificacion();

        notificacion.setNombre(request.getNombre());
        notificacion.setTitulo(request.getTitulo());
        notificacion.setDescripcion(request.getDescripcion());
        notificacion.setContenido(request.getContenido());
        notificacion.setTipoPlantilla(request.getTipoPlantilla());
        notificacion.setEventoAsociado(eventos.iterator().next());
        notificacion.setEventosAsociados(eventos);
        notificacion.setEstado(true);
        notificacion.setEliminada(false);
        notificacion.setFechaCreacion(LocalDateTime.now());

        plantillaNotificationRepository.save(notificacion);
        return new MessegeGlobalDTO("Plantilla de notificacion registrada correctamente");
    }

    /**
     * Obtiene las plantillas de notificaciones no eliminadas
     *
     * @param userRol Rol del usuario autenticado
     * @return Lista de plantillas
     */
    public List<PlantillaNotificacion> leerPlantillas(String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);

        List<PlantillaNotificacion> notificaciones = plantillaNotificationRepository.findByEliminadaFalse();
        if (notificaciones.isEmpty()) {
            throw new RuntimeException("No hay plantillas de notificaciones registradas");
        }
        return notificaciones;
    }

    /**
     * Inactiva una plantilla de notificacion
     *
     * @param id      Identificador de la plantilla
     * @param userRol Rol del usuario autenticado
     * @return Mensaje de confirmacion
     */
    public MessegeGlobalDTO inactivarPlantilla(Long id, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);
        Long plantillaId = Objects.requireNonNull(id, "El id de la plantilla es obligatorio");

        PlantillaNotificacion notificacion = plantillaNotificationRepository.findById(plantillaId)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada con id: " + plantillaId));

        notificacion.setEstado(false);
        plantillaNotificationRepository.save(notificacion);
        return new MessegeGlobalDTO("Plantilla de notificacion inactivada correctamente");
    }

    /**
     * Activa una plantilla de notificacion
     *
     * @param id      Identificador de la plantilla
     * @param userRol Rol del usuario autenticado
     * @return Mensaje de confirmacion
     */
    public MessegeGlobalDTO activarPlantilla(Long id, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);
        Long plantillaId = Objects.requireNonNull(id, "El id de la plantilla es obligatorio");

        PlantillaNotificacion notificacion = plantillaNotificationRepository.findById(plantillaId)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada con id: " + plantillaId));

        if (Boolean.TRUE.equals(notificacion.getEliminada())) {
            throw new RuntimeException("No se puede activar una plantilla eliminada");
        }

        notificacion.setEstado(true);
        plantillaNotificationRepository.save(notificacion);
        return new MessegeGlobalDTO("Plantilla de notificacion activada correctamente");
    }

    /**
     * Elimina una plantilla mediante soft delete
     *
     * @param id      Identificador de la plantilla
     * @param userRol Rol del usuario autenticado
     * @return Mensaje de confirmacion
     */
    @Transactional
    public MessegeGlobalDTO eliminarPlantilla(Long id, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);
        Long plantillaId = Objects.requireNonNull(id, "El id de la plantilla es obligatorio");

        PlantillaNotificacion notificacion = plantillaNotificationRepository.findById(plantillaId)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada con id: " + plantillaId));

        notificacion.setEliminada(true);
        notificacion.setEstado(false);
        plantillaNotificationRepository.save(notificacion);
        return new MessegeGlobalDTO("Plantilla de notificacion eliminada correctamente");
    }

    /**
     * Actualiza una plantilla de notificacion
     *
     * @param id      Identificador de la plantilla
     * @param request Datos actualizados
     * @param userRol Rol del usuario autenticado
     * @return Mensaje de confirmacion
     */
    public MessegeGlobalDTO actualizarPlantilla(Long id, PlantillaNotificacionRequestDTO request, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);
        Long plantillaId = Objects.requireNonNull(id, "El id de la plantilla es obligatorio");

        PlantillaNotificacion notificacion = plantillaNotificationRepository.findById(plantillaId)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada con id: " + plantillaId));

        if (Boolean.TRUE.equals(notificacion.getEliminada())) {
            throw new RuntimeException("No se puede actualizar una plantilla eliminada");
        }

        Set<EnumEventoAsociado> eventos = resolverEventos(request);
        notificacion.setNombre(request.getNombre());
        notificacion.setTitulo(request.getTitulo());
        notificacion.setDescripcion(request.getDescripcion());
        notificacion.setContenido(request.getContenido());
        notificacion.setTipoPlantilla(request.getTipoPlantilla());
        notificacion.setEventoAsociado(eventos.iterator().next());
        notificacion.setEventosAsociados(eventos);

        if (request.getEstado() != null) {
            notificacion.setEstado(request.getEstado());
        }

        plantillaNotificationRepository.save(notificacion);
        return new MessegeGlobalDTO("Plantilla de notificacion actualizada correctamente");
    }

    private Set<EnumEventoAsociado> resolverEventos(PlantillaNotificacionRequestDTO request) {
        if (request.getEventosAsociados() != null && !request.getEventosAsociados().isEmpty()) {
            return new HashSet<>(request.getEventosAsociados());
        }
        if (request.getEventoAsociado() != null) {
            return Set.of(request.getEventoAsociado());
        }
        throw new RuntimeException("Debe indicar al menos un evento asociado");
    }
}
