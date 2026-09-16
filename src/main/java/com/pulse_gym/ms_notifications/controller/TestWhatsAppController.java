package com.pulse_gym.ms_notifications.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulse_gym.ms_notifications.services.WhatsAppCloudService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de prueba para verificar la integración con WhatsApp Cloud API (Meta).
 * Este controlador está destinado exclusivamente para entornos de desarrollo y pruebas.
 * Envía la plantilla "hello_world" (aprobada por defecto por Meta para toda app nueva)
 * a un número predefinido, para confirmar que el token y el phone-number-id son válidos
 * sin depender de la ventana de 24h que exige el envío de texto libre.
 *
 * ADVERTENCIA: Este endpoint debe deshabilitarse o protegerse adecuadamente en producción.
 */
@RestController
@RequiredArgsConstructor
public class TestWhatsAppController {

    /**
     * Inyección del servicio de WhatsApp Cloud API (Meta).
     */
    private final WhatsAppCloudService whatsAppCloudService;

    /**
     * Envía la plantilla de prueba "hello_world" a un número predefinido de WhatsApp.
     * Al ser una plantilla aprobada por Meta, se puede enviar en cualquier momento,
     * a diferencia de un mensaje de texto libre que solo funciona dentro de las 24h
     * posteriores a que el destinatario le haya escrito al número de negocio.
     *
     * @return Cadena de texto confirmando que el mensaje fue enviado.
     *         En caso de error, retorna una excepción con el mensaje de error.
     */
    @GetMapping("/test-whatsapp")
    public String enviarPrueba() {

        whatsAppCloudService.enviarPlantilla(
                "+573248589488",
                "hello_world",
                "en_US",
                List.of());

        return "Mensaje enviado";
    }
}
