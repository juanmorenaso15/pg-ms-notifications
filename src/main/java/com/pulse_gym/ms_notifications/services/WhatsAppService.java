package com.pulse_gym.ms_notifications.services;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WhatsAppService {

    /**
     * Logger para la clase
     */
    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    /**
     * Inyeccion de RestTemplate para enviar solicitudes a Twilio
     */
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${notificaciones.whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    @Value("${notificaciones.whatsapp.provider:mock}")
    private String provider;

    @Value("${notificaciones.whatsapp.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${notificaciones.whatsapp.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${notificaciones.whatsapp.twilio.from-number:}")
    private String twilioFromNumber;

    /**
     * Envia un mensaje de WhatsApp al destinatario indicado
     *
     * @param telefono  Numero de telefono del destinatario
     * @param contenido Contenido del mensaje
     */
    public void enviarWhatsApp(String telefono, String contenido) {
        if (!whatsappEnabled) {
            logger.warn("Envio de WhatsApp deshabilitado. Mensaje no enviado a: {}", telefono);
            return;
        }

        if (telefono == null || telefono.trim().isEmpty()) {
            throw new RuntimeException("El telefono del destinatario es obligatorio para WhatsApp");
        }

        if ("twilio".equalsIgnoreCase(provider)) {
            enviarConTwilio(telefono, contenido);
            return;
        }

        logger.info("WhatsApp simulado enviado a {}: {}", telefono, contenido);
    }

    /**
     * Envía un mensaje de WhatsApp utilizando la API de Twilio. Este método construye la solicitud HTTP necesaria para interactuar con la API de Twilio, incluyendo la autenticación básica y 
     * los parámetros requeridos para el envío del mensaje. Si las credenciales de Twilio no están configuradas correctamente, se lanzará una excepción indicando que las credenciales son incompletas.
     *
     * @param telefono  Número de teléfono del destinatario
     * @param contenido Contenido del mensaje
    */
    private void enviarConTwilio(String telefono, String contenido) {
        if (twilioAccountSid == null || twilioAccountSid.isBlank()
                || twilioAuthToken == null || twilioAuthToken.isBlank()
                || twilioFromNumber == null || twilioFromNumber.isBlank()) {
            throw new RuntimeException("Credenciales de Twilio incompletas para envio de WhatsApp");
        }

        String url = "https://api.twilio.com/2010-04-01/Accounts/" + twilioAccountSid + "/Messages.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = twilioAccountSid + ":" + twilioAuthToken;
        headers.set("Authorization", "Basic " + Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8)));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("From", twilioFromNumber);
        String telefonoDestino = telefono.startsWith("whatsapp:")
                ? telefono
                : "whatsapp:" + telefono;

        body.add("To", telefonoDestino);
        body.add("Body", contenido);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error al enviar WhatsApp via Twilio");
        }

        logger.info("WhatsApp enviado exitosamente a: {}", telefono);
    }
}
