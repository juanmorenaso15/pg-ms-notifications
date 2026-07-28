package com.pulse_gym.ms_notifications.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulse_gym.ms_notifications.services.WhatsAppService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de prueba para verificar la integración con el servicio de WhatsApp.
 * Este controlador está destinado exclusivamente para entornos de desarrollo y pruebas.
 * Envía un mensaje de prueba a un número predefinido para verificar que la
 * configuración de Twilio está correcta.
 * 
 * ADVERTENCIA: Este endpoint debe deshabilitarse o protegerse adecuadamente en producción.
 */
@RestController
@RequiredArgsConstructor
public class TestWhatsAppController {

    /**
     * Inyección del servicio de WhatsApp.
     * Se utiliza para enviar mensajes de prueba a través de la API de Twilio.
     */
    private final WhatsAppService whatsAppService;

    /**
     * Envía un mensaje de prueba a un número predefinido de WhatsApp.
     * Este endpoint se utiliza para verificar que la integración con Twilio
     * está configurada correctamente.
     * 
     * El mensaje se envía al número +573248589488 (formato WhatsApp: whatsapp:+573248589488).
     * 
     * @return Cadena de texto confirmando que el mensaje fue enviado.
     *         En caso de error, retorna una excepción con el mensaje de error.
     */
    @GetMapping("/test-whatsapp")
    public String enviarPrueba() {

        whatsAppService.enviarWhatsApp(
                "whatsapp:+573248589488",
                "Prueba Twilio desde Pulse Gym");

        return "Mensaje enviado";
    }
}