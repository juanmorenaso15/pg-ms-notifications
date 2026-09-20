package com.pulse_gym.ms_notifications.services;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.pulse_gym.ms_notifications.util.TelefonoUtils;

import lombok.RequiredArgsConstructor;

/**
 * Envia mensajes de WhatsApp a traves de pg-ms-whatsapp-web, un servicio no
 * oficial (Selenium sobre WhatsApp Web) usado mientras el negocio no tiene
 * completada la verificacion de Meta Business. No requiere plantillas
 * aprobadas ni respeta la ventana de 24h de la API oficial, pero conlleva
 * riesgo real de bloqueo del numero por parte de WhatsApp.
 */
@Service
@RequiredArgsConstructor
public class WhatsAppWebService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebService.class);

    private final RestTemplate whatsAppRestTemplate;

    @Value("${whatsapp.web.url:http://pg-ms-whatsapp-web:8095}")
    private String baseUrl;

    @Value("${whatsapp.cloud.default-country-code:57}")
    private String defaultCountryCode;

    public void enviarTexto(String telefono, String contenido) {
        String numero = TelefonoUtils.normalizar(telefono, defaultCountryCode);

        Map<String, Object> body = Map.of("telefono", numero, "mensaje", contenido);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = whatsAppRestTemplate.postForEntity(
                    baseUrl + "/enviar", request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("WhatsApp (web) enviado a {}: {}", telefono, response.getBody());
            } else {
                logger.error("Error al enviar WhatsApp (web). Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException("Error al enviar WhatsApp (web): " + response.getBody());
            }
        } catch (Exception e) {
            logger.error("Excepcion al enviar WhatsApp (web) a {}: {}", telefono, e.getMessage());
            throw new RuntimeException("Error al enviar WhatsApp (web): " + e.getMessage(), e);
        }
    }
}
