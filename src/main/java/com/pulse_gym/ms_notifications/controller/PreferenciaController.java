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

import com.pulse_gym.lb_common.dto.PreferenciaUsuarioRequestDTO;
import com.pulse_gym.lb_common.dto.PreferenciaUsuarioResponseDTO;
import com.pulse_gym.ms_notifications.services.PreferenciaUsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la gestión de preferencias de notificaciones de los usuarios.
 * Expone endpoints para que los socios puedan consultar y actualizar sus preferencias
 * de canales y categorías de notificaciones.
 * 
 * Los endpoints de este controlador requieren que el usuario esté autenticado como socio.
 * Las preferencias incluyen:
 * - Canal preferido (EMAIL, WHATSAPP o AMBOS)
 * - Notificaciones de logros (habilitado/deshabilitado)
 * - Notificaciones de mantenimiento (habilitado/deshabilitado)
 * - Notificaciones promocionales (habilitado/deshabilitado)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/preferencias")
public class PreferenciaController {

    /**
     * Inyección del servicio de preferencias de usuario.
     * Se utiliza para obtener, actualizar y validar las preferencias de notificaciones
     * de los usuarios del sistema.
     */
    private final PreferenciaUsuarioService preferenciaUsuarioService;

    /**
     * Obtiene las preferencias de notificaciones del socio autenticado.
     * Si el usuario no tiene preferencias configuradas previamente, el sistema
     * crea automáticamente preferencias con valores por defecto:
     * - Canal: AMBOS (EMAIL y WHATSAPP)
     * - Logros: habilitado
     * - Mantenimientos: habilitado
     * - Promociones: habilitado
     *
     * @param usuarioId Identificador del usuario en el sistema de autenticación (cabecera X-User-Id)
     * @param userRol   Rol del usuario autenticado (cabecera X-User-Rol)
     * @return ResponseEntity con un mapa que incluye las preferencias del usuario en la clave "data".
     *         Si el usuario no tiene preferencias, se devuelven las preferencias por defecto creadas automáticamente
     */
    @GetMapping("/mis-preferencias")
    public ResponseEntity<Map<String, Object>> obtenerMisPreferencias(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        PreferenciaUsuarioResponseDTO data = preferenciaUsuarioService.obtenerMisPreferencias(usuarioId, userRol);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza las preferencias de notificaciones del socio autenticado.
     * Permite modificar el canal preferido y las categorías de notificaciones que desea recibir.
     * Los cambios se persisten inmediatamente en la base de datos.
     *
     * @param usuarioId Identificador del usuario en el sistema de autenticación (cabecera X-User-Id)
     * @param request   DTO con los nuevos datos de preferencias:
     *                - preferencia: canal preferido (EMAIL, WHATSAPP o AMBOS)
     *                - logrosHabilitado: habilitar/deshabilitar notificaciones de logros
     *                - mantenimientosHabilitado: habilitar/deshabilitar notificaciones de mantenimiento
     *                - promocionesHabilitado: habilitar/deshabilitar notificaciones promocionales
     * @param userRol   Rol del usuario autenticado (cabecera X-User-Rol)
     * @return ResponseEntity con un mapa que incluye:
     *         - "success": true si la actualización fue exitosa
     *         - "message": mensaje de confirmación
     *         - "data": las preferencias actualizadas
     */
    @PutMapping("/mis-preferencias")
    public ResponseEntity<Map<String, Object>> actualizarMisPreferencias(
            @RequestHeader(value = "X-User-Id", required = false) Long usuarioId,
            @Valid @RequestBody PreferenciaUsuarioRequestDTO request,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        PreferenciaUsuarioResponseDTO data = preferenciaUsuarioService.actualizarMisPreferencias(
                usuarioId, request, userRol);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Preferencias actualizadas correctamente");
        response.put("data", data);
        return ResponseEntity.ok(response);
    }
}
