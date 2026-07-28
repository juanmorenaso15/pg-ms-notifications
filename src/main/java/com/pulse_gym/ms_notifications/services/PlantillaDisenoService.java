package com.pulse_gym.ms_notifications.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.entity.notification.PlantillaDisenoEmail;
import com.pulse_gym.lb_common.enums.EnumCanalNotificacion;
import com.pulse_gym.lb_common.enums.EnumEventoAsociado;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_notifications.repository.PlantillaDisenoEmailRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio CRUD para gestionar las plantillas de diseño de emails
 */
@Service
@RequiredArgsConstructor
public class PlantillaDisenoService {

    private final PlantillaDisenoEmailRepository disenoRepository;

    /**
     * Crea un nuevo diseño de email
     */
    @Transactional
    public MessegeGlobalDTO crearDiseno(PlantillaDisenoEmail diseno, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);

        if (disenoRepository.findByNombreAndEliminadoFalse(diseno.getNombre()).isPresent()) {
            throw new RuntimeException("Ya existe un diseño con el nombre: " + diseno.getNombre());
        }

        diseno.setEliminado(false);
        diseno.setActivo(true);
        diseno.setFechaCreacion(LocalDateTime.now());
        disenoRepository.save(diseno);

        return new MessegeGlobalDTO("Diseño de email creado correctamente");
    }

    /**
     * Lista todos los diseños activos
     */
    public List<PlantillaDisenoEmail> listarDisenos(String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);
        return disenoRepository.findAll();
    }

    /**
     * Obtiene un diseño por ID
     */
    public PlantillaDisenoEmail obtenerPorId(Long id, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);
        return disenoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diseño no encontrado con id: " + id));
    }

    /**
     * Actualiza un diseño existente
     */
    @Transactional
    public MessegeGlobalDTO actualizarDiseno(Long id, PlantillaDisenoEmail datos, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);

        PlantillaDisenoEmail diseno = disenoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diseño no encontrado con id: " + id));

        if (Boolean.TRUE.equals(diseno.getEliminado())) {
            throw new RuntimeException("No se puede actualizar un diseño eliminado");
        }

        // Actualizar campos
        if (datos.getNombre() != null) {
            diseno.setNombre(datos.getNombre());
        }
        if (datos.getEventoAsociado() != null) {
            diseno.setEventoAsociado(datos.getEventoAsociado());
        }
        if (datos.getColorPrincipal() != null) {
            diseno.setColorPrincipal(datos.getColorPrincipal());
        }
        if (datos.getColorSecundario() != null) {
            diseno.setColorSecundario(datos.getColorSecundario());
        }
        if (datos.getColorTextoHeader() != null) {
            diseno.setColorTextoHeader(datos.getColorTextoHeader());
        }
        if (datos.getTituloHeader() != null) {
            diseno.setTituloHeader(datos.getTituloHeader());
        }
        if (datos.getSubtituloHeader() != null) {
            diseno.setSubtituloHeader(datos.getSubtituloHeader());
        }
        if (datos.getColorFondoContenido() != null) {
            diseno.setColorFondoContenido(datos.getColorFondoContenido());
        }
        if (datos.getColorTextoContenido() != null) {
            diseno.setColorTextoContenido(datos.getColorTextoContenido());
        }
        if (datos.getColorFondoFooter() != null) {
            diseno.setColorFondoFooter(datos.getColorFondoFooter());
        }
        if (datos.getColorTextoFooter() != null) {
            diseno.setColorTextoFooter(datos.getColorTextoFooter());
        }
        if (datos.getTextoFooter() != null) {
            diseno.setTextoFooter(datos.getTextoFooter());
        }
        if (datos.getTextoFooterSecundario() != null) {
            diseno.setTextoFooterSecundario(datos.getTextoFooterSecundario());
        }
        if (datos.getActivo() != null) {
            diseno.setActivo(datos.getActivo());
        }

        diseno.setFechaActualizacion(LocalDateTime.now());
        disenoRepository.save(diseno);

        return new MessegeGlobalDTO("Diseño de email actualizado correctamente");
    }

    /**
     * Elimina un diseño (soft delete)
     */
    @Transactional
    public MessegeGlobalDTO eliminarDiseno(Long id, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);

        PlantillaDisenoEmail diseno = disenoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diseño no encontrado con id: " + id));

        diseno.setEliminado(true);
        diseno.setActivo(false);
        diseno.setFechaActualizacion(LocalDateTime.now());
        disenoRepository.save(diseno);

        return new MessegeGlobalDTO("Diseño de email eliminado correctamente");
    }

    /**
     * Activa un diseño eliminado
     */
    @Transactional
    public MessegeGlobalDTO activarDiseno(Long id, String userRol) {
        ValidacionDeRoles.validarAdmin(userRol);

        PlantillaDisenoEmail diseno = disenoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diseño no encontrado con id: " + id));

        diseno.setEliminado(false);
        diseno.setActivo(true);
        diseno.setFechaActualizacion(LocalDateTime.now());
        disenoRepository.save(diseno);

        return new MessegeGlobalDTO("Diseño de email activado correctamente");
    }
}