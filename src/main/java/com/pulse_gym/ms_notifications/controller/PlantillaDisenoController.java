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
import com.pulse_gym.lb_common.entity.notification.PlantillaDisenoEmail;
import com.pulse_gym.ms_notifications.services.PlantillaDisenoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/diseno-email")
public class PlantillaDisenoController {

    /**
     * Inyeccion de PlantillaDisenoService para manejar las operaciones de base de datos
     * relacionadas con los diseños de email
     */
    private final PlantillaDisenoService disenoService;

    /**
     * Controlador para crear un nuevo diseño de email
     * @param request
     * @param userRol
     * @return 
     */
    @PostMapping("/crear")
    public ResponseEntity<Map<String, Object>> crearDiseno(
            @Valid @RequestBody PlantillaDisenoEmail request,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            MessegeGlobalDTO response = disenoService.crearDiseno(request, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearError(e.getMessage()));
        }
    }

    /**
     * Lista todos los diseños de email
     */
    @GetMapping("/leer")
    public ResponseEntity<Map<String, Object>> listarDisenos(
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            List<PlantillaDisenoEmail> disenos = disenoService.listarDisenos(userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", "Diseños encontrados");
            respuesta.put("count", disenos.size());
            respuesta.put("data", disenos);
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearError(e.getMessage()));
        }
    }

    /**
     * Obtiene un diseño por ID
     */
    @GetMapping("/leer/{id}")
    public ResponseEntity<Map<String, Object>> obtenerDiseno(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            PlantillaDisenoEmail diseno = disenoService.obtenerPorId(id, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("data", diseno);
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearError(e.getMessage()));
        }
    }

    /**
     * Actualiza un diseño existente
     */
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Map<String, Object>> actualizarDiseno(
            @PathVariable Long id,
            @Valid @RequestBody PlantillaDisenoEmail request,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            MessegeGlobalDTO response = disenoService.actualizarDiseno(id, request, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearError(e.getMessage()));
        }
    }

    /**
     * Elimina un diseño (soft delete)
     */
    @PostMapping("/eliminar/{id}")
    public ResponseEntity<Map<String, Object>> eliminarDiseno(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            MessegeGlobalDTO response = disenoService.eliminarDiseno(id, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearError(e.getMessage()));
        }
    }

    /**
     * Activa un diseño eliminado
     */
    @PostMapping("/activar/{id}")
    public ResponseEntity<Map<String, Object>> activarDiseno(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {
        try {
            MessegeGlobalDTO response = disenoService.activarDiseno(id, userRol);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("message", response.getMessage());
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(crearError(e.getMessage()));
        }
    }

    private Map<String, Object> crearError(String mensaje) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", mensaje);
        return error;
    }
}