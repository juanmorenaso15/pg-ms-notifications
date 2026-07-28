package com.pulse_gym.ms_notifications.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.dto.PlantillaNotificacionRequestDTO;
import com.pulse_gym.lb_common.entity.notification.PlantillaNotificacion;
import com.pulse_gym.ms_notifications.services.PlantillaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/plantilla")
public class PlantillaController {

    /**
     * Inyeccion de PlantillaService para manejar las operaciones de base de datos
     * relacionadas con las plantillas de notificaciones
     */
    private final PlantillaService plantillaService;

    /**
     * Controlador para crear una nueva plantilla de notificacion
     * 
     * @param request objeto con los datos necesarios
     * @param userRol Rol del usuario que hace la peticion
     * @return ResponseEntity<Map<String, Object>> con el resultado de la operación
     */
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearPlantilla(
            @Valid @RequestBody PlantillaNotificacionRequestDTO request,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            MessegeGlobalDTO response = plantillaService.crearPlantilla(request, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(respuesta);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Consulta las plantillas de notificaciones
     * 
     * @param userRol Rol del usuario
     * @return ResponseEntity<Map<String, Object>> con el resultado de la operación
     */
    @GetMapping("/leer")
    public ResponseEntity<Map<String, Object>> leerPlantilla(
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            List<PlantillaNotificacion> notificaciones = plantillaService.leerPlantillas(userRol);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Plantillas encontradas");
            response.put("count", notificaciones.size());
            response.put("data", notificaciones);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Controlador para inactivar una plantilla de notificacion
     * @param id Identificador de la plantilla de notificacion a inactivar
     * @param userRol Rol del usuario que hace la peticion (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con el resultado de la operación 
     */
    @PostMapping("/inactivar/{id}")
    public ResponseEntity<Map<String, Object>> inactivarPlantilla(@PathVariable Long id,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            MessegeGlobalDTO response = plantillaService.inactivarPlantilla(id, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Controlador para activar una plantilla de notificacion
     * @param id Identificador de la plantilla de notificacion a activar
     * @param userRol Rol del usuario que hace la peticion (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con el resultado de la operación
     */
    @PostMapping("/activar/{id}")
    public ResponseEntity<Map<String, Object>> activarPlantilla(@PathVariable Long id,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            MessegeGlobalDTO response = plantillaService.activarPlantilla(id, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Controlador para actualizar una plantilla de notificacion
     * @param id Identificador de la plantilla de notificacion a actualizar
     * @param request Objeto con los datos necesarios para actualizar la plantilla
     * @param userRol Rol del usuario que hace la peticion (desde header X-User-Rol)
     * @return ResponseEntity<Map<String, Object>> con el resultado de la operación
     */
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Map<String, Object>> actualizarPlantilla(@PathVariable Long id,
            @Valid @RequestBody PlantillaNotificacionRequestDTO request,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        try {
            MessegeGlobalDTO response = plantillaService.actualizarPlantilla(id, request, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Elimina una plantilla mediante soft delete
     *
     * @param id      Identificador de la plantilla
     * @param userRol Rol del usuario autenticado
     * @return Resultado de la operacion
     */
    @PostMapping("/eliminar/{id}")
    public ResponseEntity<Map<String, Object>> eliminarPlantilla(@PathVariable Long id,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            MessegeGlobalDTO response = plantillaService.eliminarPlantilla(id, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
