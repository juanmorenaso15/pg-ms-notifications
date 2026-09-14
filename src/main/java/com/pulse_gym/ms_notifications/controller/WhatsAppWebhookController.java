package com.pulse_gym.ms_notifications.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WhatsAppWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    @Value("${whatsapp.cloud.verify-token}")
    private String verifyToken;

    /**
     * Endpoint de verificación (GET). Meta lo usa para comprobar que la URL es tuya.
     */
    @GetMapping("/whatsapp")
    public ResponseEntity<String> verificarWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.challenge") String challenge,
            @RequestParam("hub.verify_token") String token) {

        logger.info("Recibida petición de verificación de Meta. Mode: {}, Token: {}", mode, token);

        // Si el modo es 'subscribe' y el token coincide con el que configuramos
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            logger.info("Verificación exitosa. Devolviendo challenge.");
            // Meta espera EXACTAMENTE el valor de 'challenge' como texto plano
            return ResponseEntity.ok(challenge);
        } else {
            logger.warn("Verificación fallida. Token recibido: {} | Token esperado: {}", token, verifyToken);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token de verificación inválido");
        }
    }

    /**
     * Endpoint para recibir notificaciones reales (POST). 
     * Aquí llegarán los mensajes de usuarios y estados de entrega.
     */
    @PostMapping("/whatsapp")
    public ResponseEntity<String> recibirNotificacion(@RequestBody String payload) {
        logger.info("Webhook POST recibido de Meta: {}", payload);
        
        return ResponseEntity.ok("EVENT_RECEIVED");
    }
}