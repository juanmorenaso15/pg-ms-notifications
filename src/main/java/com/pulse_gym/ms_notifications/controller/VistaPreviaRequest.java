package com.pulse_gym.ms_notifications.controller;

import java.util.Map;

import lombok.Data;

/**
 * DTO (Data Transfer Object) para la solicitud de vista previa de una plantilla.
 * Se utiliza para enviar el contenido de una plantilla y renderizarla con valores de prueba
 * para que el administrador pueda previsualizar cómo se verá la notificación final.
 */
@Data
public class VistaPreviaRequest {

    /**
     * Contenido de la plantilla que se desea previsualizar.
     * Puede contener variables en formato {variable} o {{variable}} que serán
     * reemplazadas por los valores de prueba.
     */
    private String contenido;

    /**
     * Mapa opcional de valores de prueba para reemplazar las variables en el contenido.
     * Si no se proporciona, el sistema utilizará valores de ejemplo por defecto
     * para cada variable detectada en el contenido.
     * 
     * Ejemplo de valores de prueba:
     * {
     *   "nombre": "Juan Pérez",
     *   "email": "juan@ejemplo.com",
     *   "plan": "Premium"
     * }
     */
    private Map<String, Object> valoresPrueba;
}
