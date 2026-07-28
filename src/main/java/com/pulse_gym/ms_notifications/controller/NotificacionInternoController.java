package com.pulse_gym.ms_notifications.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulse_gym.lb_common.dto.EnvioEventoNotificacionDTO;
import com.pulse_gym.lb_common.dto.VerificarPreferenciaRequestDTO;
import com.pulse_gym.lb_common.dto.VerificarPreferenciaResponseDTO;
import com.pulse_gym.ms_notifications.services.NotificacionService;
import com.pulse_gym.ms_notifications.services.PreferenciaUsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para endpoints internos del microservicio de notificaciones.
 * Este controlador expone endpoints que son consumidos por otros microservicios del sistema
 * para verificar preferencias de usuarios y enviar notificaciones basadas en eventos del sistema.
 * 
 * Los endpoints de este controlador no requieren autenticación a nivel de API gateway,
 * ya que la validación de seguridad se maneja en la capa de comunicación entre microservicios.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/notificaciones")
public class NotificacionInternoController {

    /**
     * Inyección del servicio de preferencias de usuario.
     * Se utiliza para verificar si un usuario tiene habilitada la recepción de notificaciones
     * y para obtener sus preferencias de canal y categorías.
     */
    private final PreferenciaUsuarioService preferenciaUsuarioService;

    /**
     * Inyección del servicio de notificaciones.
     * Se utiliza para procesar y enviar notificaciones basadas en eventos del sistema.
     */
    private final NotificacionService notificacionService;

    /**
     * Verifica si un usuario acepta recibir una notificación según sus preferencias
     * configuradas y los límites de rate limiting del sistema.
     * Este endpoint es consumido internamente por otros microservicios para determinar
     * si pueden enviar una notificación a un usuario específico.
     *
     * @param request DTO que contiene el identificador del usuario, el tipo de evento
     *               y el canal de notificación a verificar
     * @return ResponseEntity con un DTO que indica si el envío está permitido y,
     *         en caso negativo, el motivo de la restricción
     */
    @PostMapping("/verificar")
    public ResponseEntity<VerificarPreferenciaResponseDTO> verificarPreferencia(
            @Valid @RequestBody VerificarPreferenciaRequestDTO request) {
        return ResponseEntity.ok(preferenciaUsuarioService.verificarEnvioPermitido(request));
    }

    /**
     * Envía una notificación automática basada en un evento del sistema.
     * Este endpoint es consumido por otros microservicios para enviar notificaciones
     * reactivas a eventos como: registro de usuario, inicio de sesión, logros alcanzados,
     * recordatorios de pago, etc.
     *
     * El servicio identifica la plantilla activa asociada al evento, renderiza el contenido
     * con las variables del usuario y envía la notificación por el canal configurado.
     *
     * @param request DTO que encapsula los datos del evento (tipo de evento),
     *               el identificador del usuario destino y variables adicionales opcionales
     * @return ResponseEntity con un mapa que indica el estado de éxito y un mensaje informativo.
     *         En caso de error, retorna el mensaje de excepción correspondiente
     */
    @PostMapping("/enviar-evento")
    public ResponseEntity<Map<String, Object>> enviarPorEvento(
            @Valid @RequestBody EnvioEventoNotificacionDTO request) {

        notificacionService.enviarNotificacionPorEvento(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notificacion enviada por evento");
        return ResponseEntity.ok(response);
    }
}
