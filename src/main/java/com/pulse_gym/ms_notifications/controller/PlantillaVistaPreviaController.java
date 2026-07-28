package com.pulse_gym.ms_notifications.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_notifications.services.PlantillaRenderService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para previsualizar plantillas de notificaciones.
 * Permite a los administradores ver cómo se verá una plantilla después de ser renderizada
 * con valores de prueba, antes de guardarla o enviarla oficialmente.
 * 
 * Este endpoint es útil para:
 * - Verificar que las variables en la plantilla están correctamente formateadas
 * - Probar el diseño visual de la notificación
 * - Validar que todas las variables necesarias están presentes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/plantilla")
public class PlantillaVistaPreviaController {

    /**
     * Inyección del servicio de renderizado de plantillas.
     * Se utiliza para extraer variables, generar valores de ejemplo y renderizar
     * el contenido de la plantilla.
     */
    private final PlantillaRenderService renderService;

    /**
     * Genera una vista previa de una plantilla de notificación.
     * Extrae las variables del contenido, genera valores de ejemplo para cada una,
     * y renderiza el contenido reemplazando las variables por sus valores.
     * 
     * El proceso es el siguiente:
     * 1. Extrae todas las variables del contenido (formato {variable} o {{variable}})
     * 2. Genera valores de ejemplo para cada variable
     * 3. Si se proporcionan valores de prueba, los usa en lugar de los ejemplos
     * 4. Renderiza el contenido reemplazando las variables por los valores
     * 
     * @param request DTO que contiene el contenido de la plantilla y opcionalmente
     *               valores de prueba específicos
     * @param userRol Rol del usuario autenticado (debe ser ADMIN para acceder)
     * @return ResponseEntity con un mapa que incluye:
     *         - "contenido": el contenido renderizado con las variables reemplazadas
     *         - "variables_encontradas": conjunto de variables detectadas en el contenido
     *         - "valores_usados": mapa de valores utilizados para cada variable
     */
    @PostMapping("/vista-previa")
    public ResponseEntity<Map<String, Object>> vistaPrevia(
            @RequestBody VistaPreviaRequest request,
            @RequestHeader(value = "X-User-Rol", required = false) String userRol) {

        ValidacionDeRoles.validarAdmin(userRol);

        Set<String> variables = renderService.extraerVariables(request.getContenido());
        Map<String, Object> valoresEjemplo = renderService.generarValoresEjemplo(variables);

        if (request.getValoresPrueba() != null) {
            valoresEjemplo.putAll(request.getValoresPrueba());
        }

        String contenidoVista = renderService.renderizar(request.getContenido(), valoresEjemplo);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("contenido", contenidoVista);
        respuesta.put("variables_encontradas", variables);
        respuesta.put("valores_usados", valoresEjemplo);

        return ResponseEntity.ok(respuesta);
    }
}
