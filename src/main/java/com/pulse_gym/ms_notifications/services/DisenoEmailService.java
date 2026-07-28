package com.pulse_gym.ms_notifications.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pulse_gym.lb_common.entity.notification.PlantillaDisenoEmail;
import com.pulse_gym.lb_common.enums.EnumCanalNotificacion;
import com.pulse_gym.lb_common.enums.EnumEventoAsociado;
import com.pulse_gym.ms_notifications.repository.PlantillaDisenoEmailRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para generar diseños HTML dinámicos para emails.
 * Lee la configuración de la base de datos y genera el HTML con variables reemplazadas.
 */
@Service
@RequiredArgsConstructor
public class DisenoEmailService {

    private static final Logger logger = LoggerFactory.getLogger(DisenoEmailService.class);

    private static final Pattern PATRON_VARIABLE = Pattern.compile("\\{(\\w+)\\}");

    private final PlantillaDisenoEmailRepository disenoRepository;

    /**
     * Genera el HTML completo del email con el diseño configurado
     * @param contenido Contenido principal del email (ya renderizado con variables)
     * @param evento Evento asociado para buscar el diseño
     * @param variables Variables adicionales para reemplazar en el diseño
     * @return HTML completo del email
     */
    public String generarHtml(String contenido, EnumEventoAsociado evento, Map<String, Object> variables) {
        PlantillaDisenoEmail diseno = obtenerDiseno(evento);
        
        Map<String, Object> contexto = new HashMap<>();
        if (variables != null) {
            contexto.putAll(variables);
        }
        
        String headerHtml = generarHeader(diseno, contexto);
        String footerHtml = generarFooter(diseno, contexto);
        
        return construirHtmlCompleto(headerHtml, contenido, footerHtml, diseno);
    }

    /**
     * Obtiene el diseño según el evento, o el diseño por defecto
     */
    private PlantillaDisenoEmail obtenerDiseno(EnumEventoAsociado evento) {
        if (evento != null) {
            return disenoRepository.findByEventoAsociadoAndEliminadoFalseAndActivoTrue(evento)
                    .orElseGet(() -> obtenerDisenoPorDefecto());
        }
        return obtenerDisenoPorDefecto();
    }

    /**
     * Obtiene el diseño por defecto
     */
    private PlantillaDisenoEmail obtenerDisenoPorDefecto() {
        return disenoRepository.findByNombreAndEliminadoFalseAndActivoTrue("default")
                .orElseGet(() -> crearDisenoDefault());
    }

    /**
     * Crea un diseño por defecto en memoria si no existe en BD
     */
    private PlantillaDisenoEmail crearDisenoDefault() {
        logger.warn("No se encontró diseño en BD, usando configuración por defecto");
        PlantillaDisenoEmail diseno = new PlantillaDisenoEmail();
        diseno.setNombre("default");
        diseno.setColorPrincipal("#2c4b77");
        diseno.setColorSecundario("#8bb5d6");
        diseno.setColorTextoHeader("#ffffff");
        diseno.setTituloHeader("Pulse Gym");
        diseno.setSubtituloHeader("Tu bienestar, nuestra pasión");
        diseno.setColorFondoContenido("#ffffff");
        diseno.setColorTextoContenido("#5d6d7e");
        diseno.setColorFondoFooter("#f8f9fc");
        diseno.setColorTextoFooter("#9aabbb");
        diseno.setTextoFooter("© 2026 Pulse Gym - Todos los derechos reservados");
        diseno.setTextoFooterSecundario("Este es un mensaje automático, por favor no responder a este correo");
        return diseno;
    }

    /**
     * Genera el HTML del header con las variables reemplazadas
     */
    private String generarHeader(PlantillaDisenoEmail diseno, Map<String, Object> contexto) {
        String header = String.format("""
                <div class="header" style="
                    background: linear-gradient(135deg, %s 0%%, %s 100%%);
                    color: %s;
                    padding: 35px 20px;
                    text-align: center;
                ">
                    <h1 style="margin: 0; font-size: 28px; font-weight: 300; letter-spacing: 1px;">
                        <strong>%s</strong>
                    </h1>
                    <p style="margin: 10px 0 0; opacity: 0.9; font-size: 14px;">
                        %s
                    </p>
                </div>
                """,
                diseno.getColorPrincipal(),
                diseno.getColorSecundario(),
                diseno.getColorTextoHeader(),
                diseno.getTituloHeader(),
                diseno.getSubtituloHeader()
        );
        
        return reemplazarVariables(header, contexto);
    }

    /**
     * Genera el HTML del footer con las variables reemplazadas
     */
    private String generarFooter(PlantillaDisenoEmail diseno, Map<String, Object> contexto) {
        String footer = String.format("""
                <div class="footer" style="
                    background-color: %s;
                    padding: 20px 30px;
                    text-align: center;
                    border-top: 1px solid #e8edf2;
                ">
                    <p style="color: %s; font-size: 11px; margin: 5px 0;">
                        %s
                    </p>
                    <p style="color: %s; font-size: 11px; margin: 5px 0;">
                        %s
                    </p>
                </div>
                """,
                diseno.getColorFondoFooter(),
                diseno.getColorTextoFooter(),
                diseno.getTextoFooter(),
                diseno.getColorTextoFooter(),
                diseno.getTextoFooterSecundario()
        );
        
        return reemplazarVariables(footer, contexto);
    }

    /**
     * Construye el HTML completo con estilos y estructura
     */
    private String construirHtmlCompleto(String header, String contenido, String footer, 
            PlantillaDisenoEmail diseno) {
        
        String colorFondo = diseno.getColorFondoContenido();
        
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Pulse Gym</title>
                </head>
                <body style="
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    line-height: 1.6;
                    color: %s;
                    margin: 0;
                    padding: 0;
                    background: linear-gradient(135deg, #e0eafc 0%%, #cfdef3 100%%);
                ">
                    <div style="
                        max-width: 550px;
                        margin: 30px auto;
                        padding: 0;
                        background-color: #ffffff;
                        border-radius: 20px;
                        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    ">
                        %s

                        <div class="content" style="
                            padding: 40px 35px;
                            background-color: %s;
                        ">
                            <div class="contenido-mensaje" style="
                                color: %s;
                                font-size: 15px;
                                line-height: 1.6;
                            ">
                                %s
                            </div>
                        </div>

                        %s
                    </div>
                </body>
                </html>
                """,
                diseno.getColorTextoContenido(),
                header,
                colorFondo,
                diseno.getColorTextoContenido(),
                contenido,
                footer
        );
    }

    /**
     * Reemplaza las variables en el texto dado
     */
    private String reemplazarVariables(String texto, Map<String, Object> contexto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }

        String resultado = texto;
        for (Map.Entry<String, Object> entry : contexto.entrySet()) {
            String doubleCurly = "{{" + entry.getKey() + "}}";
            String singleCurly = "{" + entry.getKey() + "}";
            String valor = entry.getValue() != null ? entry.getValue().toString() : "";
            resultado = resultado.replace(doubleCurly, valor).replace(singleCurly, valor);
        }

        return resultado;
    }

    /**
     * Extrae las variables de una plantilla de diseño
     */
    public Set<String> extraerVariables(String contenido) {
        java.util.Set<String> variables = new java.util.HashSet<>();
        if (contenido == null || contenido.isEmpty()) {
            return variables;
        }
        Matcher matcher = PATRON_VARIABLE.matcher(contenido);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }
}