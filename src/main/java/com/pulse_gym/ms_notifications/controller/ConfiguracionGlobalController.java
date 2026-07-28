package com.pulse_gym.ms_notifications.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulse_gym.lb_common.dto.ConfiguracionGlobalRequestDTO;
import com.pulse_gym.lb_common.dto.ConfiguracionGlobalResponseDTO;
import com.pulse_gym.lb_common.dto.MessegeGlobalDTO;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_notifications.services.ConfiguracionGlobalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST que expone endpoints para la gestión de la configuración global
 * del microservicio de notificaciones. Permite consultar y actualizar los límites
 * de envío globales.
 * 
 * Acceso restringido a usuarios con rol de administrador.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/configuracion-global")
public class ConfiguracionGlobalController {

    /**
     * Inyección del servicio ConfiguracionGlobalService para gestionar
     * las operaciones de negocio y persistencia de la configuración global de notificaciones.
     */
    private final ConfiguracionGlobalService configuracionGlobalService;

    /**
     * Consulta los limites globales de notificaciones
     *
     * @param userRol Rol del usuario autenticado
     * @return Configuracion global
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> obtenerConfiguracion(
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        ValidacionDeRoles.validarAdmin(userRol);
        ConfiguracionGlobalResponseDTO data = configuracionGlobalService.obtenerConfiguracion();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza los limites globales de notificaciones
     *
     * @param request Datos de configuracion
     * @param userRol Rol del usuario autenticado
     * @return Mensaje de confirmacion
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> actualizarConfiguracion(
            @Valid @RequestBody ConfiguracionGlobalRequestDTO request,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        MessegeGlobalDTO message = configuracionGlobalService.actualizarConfiguracion(request, userRol);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message.getMessage());
        return ResponseEntity.ok(response);
    }
}
