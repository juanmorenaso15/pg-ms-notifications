package com.pulse_gym.ms_notifications.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate plano (sin @LoadBalanced) para llamadas a APIs externas como
 * WhatsApp Cloud (Meta). pg-lib-common ya expone un RestTemplate propio, pero
 * viene marcado @LoadBalanced para resolver nombres de servicio internos via
 * Consul, por lo que no sirve para invocar una URL externa como graph.facebook.com.
 */
@Configuration
public class WhatsAppRestTemplateConfig {

    @Bean
    public RestTemplate whatsAppRestTemplate() {
        return new RestTemplate();
    }
}
