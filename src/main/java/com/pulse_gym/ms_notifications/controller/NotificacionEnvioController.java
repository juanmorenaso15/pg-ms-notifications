package com.pulse_gym.ms_notifications.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulse_gym.lb_common.dto.EnvioEventoNotificacionDTO;
import com.pulse_gym.lb_common.dto.EnvioNotificacionDTO;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_notifications.services.NotificacionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST que expone endpoints públicos y administrativos para el envío
 * de notificaciones dentro del sistema Pulse Gym. Soporta el envío manual por canal,
 * envíos basados en plantillas existentes y envíos reactivos basados en eventos del sistema.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notificaciones")
public class NotificacionEnvioController {

    /**
     * Inyección del servicio NotificacionService encargado de procesar y despachar
     * las notificaciones a través de los canales configurados (Email, WhatsApp, etc.).
     */
    private final NotificacionService notificacionService;

    /**
     * Envía una notificación manual especificando directamente el canal, destinatario y contenido.
     *
     * @param request DTO que contiene los datos detallados del envío (destinatario, canal, asunto, mensaje).
     * @param userRol Rol del usuario autenticado, provisto por la cabecera X-User-Rol.
     * @return ResponseEntity con un mapa que indica el estado de éxito y un mensaje informativo.
     */
    @PostMapping("/enviar")
    public ResponseEntity<Map<String, Object>> enviarNotificacion(
            @Valid @RequestBody EnvioNotificacionDTO request,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        try {
            ValidacionDeRoles.validarAdmin(userRol);
            notificacionService.enviarNotificacion(request);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Notificacion enviada exitosamente");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Envía una notificación utilizando una plantilla preexistente y un conjunto de variables dinámicas para el usuario.
     * Recupera la plantilla por su identificador y la renderiza usando los datos del usuario obtenido de auth y las variables adicionales.
     *
     * @param plantillaId          Identificador único de la plantilla de notificación.
     * @param usuarioId            Identificador único del usuario destino (para extraer email, username, etc.).
     * @param variablesAdicionales Mapa opcional con variables dinámicas que se reemplazarán en el cuerpo de la plantilla.
     * @param userRol              Rol del usuario que ejecuta la petición (requiere privilegios de administrador).
     * @return ResponseEntity con el resultado de la operación (éxito o mensaje de error).
     */
    @PostMapping("/enviar-plantilla/{plantillaId}/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> enviarConPlantilla(
            @PathVariable Long plantillaId,
            @PathVariable Long usuarioId,
            @RequestBody(required = false) Map<String, Object> variablesAdicionales,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        try {
            ValidacionDeRoles.validarAdmin(userRol);
            notificacionService.enviarNotificacionConPlantilla(
                    plantillaId,
                    usuarioId,
                    variablesAdicionales != null ? variablesAdicionales : new HashMap<>());

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Notificacion con plantilla enviada exitosamente");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Envía una notificación reaccionando automáticamente a un evento del sistema (por ejemplo: REGISTRO_USUARIO, LOGIN_USUARIO).
     * Identifica las plantillas activas asociadas al evento y procesa el envío al usuario correspondiente.
     *
     * @param request DTO que encapsula los datos del evento, el usuario objetivo y los parámetros adicionales del evento.
     * @param userRol Rol del usuario autenticado (requiere rol de administrador).
     * @return ResponseEntity indicando si la notificación se pudo enviar correctamente para el evento especificado.
     */
    @PostMapping("/enviar-evento")
    public ResponseEntity<Map<String, Object>> enviarPorEvento(
            @Valid @RequestBody EnvioEventoNotificacionDTO request,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        try {
            ValidacionDeRoles.validarAdmin(userRol);
            notificacionService.enviarNotificacionPorEvento(request);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Notificacion enviada por evento");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
