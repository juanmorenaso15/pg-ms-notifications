package com.pulse_gym.ms_notifications.services;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.pulse_gym.lb_common.enums.EnumEventoAsociado;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

/**
 * Servicio para enviar emails con diseño dinámico.
 * Utiliza DisenoEmailService para generar el HTML basado en configuración de BD.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    /**
     * Logger para la clase
     */
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * Inyeccion de JavaMailSender para enviar emails
     */
    @Autowired
    private JavaMailSender mailSender;

    /**
     * Servicio de diseño dinámico de emails
     */
    @Autowired
    private DisenoEmailService disenoEmailService;

    /**
     * Email del remitente
     */
    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * Indica si el envío de emails está habilitado
     */
    @Value("${notificaciones.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Método para enviar un email a un destinatario (texto plano - legacy)
     * @param destinatario Email del destinatario
     * @param asunto    Asunto del email
     * @param contenido Contenido del email
     */
    public void enviarEmail(String destinatario, String asunto, String contenido) {
        if (!emailEnabled) {
            logger.warn("Envío de emails deshabilitado. Email no enviado a: {}", destinatario);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(contenido);
            mensaje.setFrom(fromEmail);

            mailSender.send(mensaje);
            logger.info("Email enviado exitosamente a: {}", destinatario);

        } catch (Exception e) {
            logger.error("Error al enviar email a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }

    /**
     * Envía un email en formato HTML con diseño dinámico basado en el evento.
     * @param destinatario Email del destinatario
     * @param asunto    Asunto del email
     * @param contenido Contenido HTML del email (se insertará en el body)
     * @param evento   Evento asociado al email
     */
    public void enviarEmailHtml(String destinatario, String asunto, String contenido, EnumEventoAsociado evento) {
        enviarEmailHtml(destinatario, asunto, contenido, evento, null);
    }

    /**
     * Envía un email en formato HTML con diseño dinámico basado en el evento.
     * Soporta variables adicionales para reemplazar en el diseño.
     * @param destinatario Email del destinatario
     * @param asunto    Asunto del email
     * @param contenido Contenido HTML del email (se insertará en el body)
     * @param evento   Evento asociado al email
     * @param variables Variables adicionales para reemplazar en el diseño (puede ser null)
     */
    public void enviarEmailHtml(String destinatario, String asunto, String contenido, 
            EnumEventoAsociado evento, Map<String, Object> variables) {
        
        if (!emailEnabled) {
            logger.warn("Envío de emails deshabilitado. Email HTML no enviado a: {}", destinatario);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(asunto);

            // Generar HTML con diseño dinámico
            String htmlContent = disenoEmailService.generarHtml(contenido, evento, variables);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            logger.info("Email HTML enviado exitosamente a: {} con evento {}", destinatario, evento);

        } catch (MessagingException e) {
            logger.error("Error al enviar email HTML a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("No se pudo enviar el email HTML", e);
        }
    }
}