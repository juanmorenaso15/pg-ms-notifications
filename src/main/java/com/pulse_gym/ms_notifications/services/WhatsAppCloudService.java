package com.pulse_gym.ms_notifications.services;

import java.util.List;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WhatsAppCloudService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppCloudService.class);

    private final RestTemplate restTemplate;

    @Value("${whatsapp.cloud.api-url}")
    private String apiUrl;

    @Value("${whatsapp.cloud.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.cloud.access-token}")
    private String accessToken;

    @Value("${whatsapp.cloud.enabled:false}")
    private boolean enabled;

    /**
     * Envía un mensaje de texto simple a un número de WhatsApp.
     * 
     * @param telefono Número destino
     * @param contenido Contenido del mensaje
     * 
     * 
     */
    public void enviarTexto(String telefono, String contenido) {
        if (!enabled) {
            logger.warn("WhatsApp Cloud deshabilitado. Mensaje no enviado a: {}", telefono);
            return;
        }

        validarTelefono(telefono);

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", normalizarTelefono(telefono),
                "type", "text",
                "text", Map.of("body", contenido)
        );

        enviar(body, telefono);
    }

    /**
     * Envía un mensaje usando una plantilla aprobada por Meta.
     * Necesario para iniciar conversaciones fuera de la ventana de 24h.
     *
     * @param telefono     Número destino
     * @param nombrePlantilla Nombre exacto de la plantilla aprobada
     * @param idioma       Código de idioma ("es", "es_CO", "en_US", etc.)
     * @param variables    Lista ordenada de valores para las variables {{1}}, {{2}}...
     */
    public void enviarPlantilla(String telefono, String nombrePlantilla,
                                 String idioma, List<String> variables) {
        if (!enabled) {
            logger.warn("WhatsApp Cloud deshabilitado. Plantilla no enviada a: {}", telefono);
            return;
        }

        validarTelefono(telefono);

        List<Map<String, String>> parametros = variables.stream()
                .map(v -> Map.of("type", "text", "text", v))
                .toList();

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", normalizarTelefono(telefono),
                "type", "template",
                "template", Map.of(
                        "name", nombrePlantilla,
                        "language", Map.of("code", idioma),
                        "components", List.of(
                                Map.of("type", "body", "parameters", parametros)
                        )
                )
        );

        enviar(body, telefono);
    }

    /**
     * Método privado para enviar la solicitud HTTP a la API de WhatsApp Cloud.
     *
     * @param body     Cuerpo de la solicitud
     * @param telefono Número destino (para logging)
     */
    private void enviar(Map<String, Object> body, String telefono) {
        String url = apiUrl + "/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("WhatsApp enviado a {}: {}", telefono, response.getBody());
            } else {
                logger.error("Error al enviar WhatsApp. Status: {}, Body: {}",
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException("Error al enviar WhatsApp: " + response.getBody());
            }
        } catch (Exception e) {
            logger.error("Excepción al enviar WhatsApp a {}: {}", telefono, e.getMessage());
            throw new RuntimeException("Error al enviar WhatsApp: " + e.getMessage(), e);
        }
    }

    private void validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new RuntimeException("El teléfono del destinatario es obligatorio para WhatsApp");
        }
    }

    /**
     * Normaliza el número de teléfono eliminando cualquier carácter que no sea un dígito.
     *
     * @param telefono Número de teléfono a normalizar
     * @return Número de teléfono normalizado
     */
    private String normalizarTelefono(String telefono) {
        return telefono.replaceAll("[^0-9]", "");
    }
}