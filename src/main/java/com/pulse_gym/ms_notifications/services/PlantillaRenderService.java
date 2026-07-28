package com.pulse_gym.ms_notifications.services;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PlantillaRenderService {
    
    /**
     * Patron regex para identificar variables en el formato {variable}
     */
    private static final Pattern PATRON_VARIABLE = Pattern.compile("\\{(\\w+)\\}");
    
    /**
     * Extrae las variables de una plantilla de notificación a partir de su contenido
     * @param contenido Contenido de la plantilla con posibles variables en formato {variable}
     * @return Conjunto de variables encontradas en el contenido de la plantilla
     */
    public Set<String> extraerVariables(String contenido) {
        Set<String> variables = new HashSet<>();
        if (contenido == null || contenido.isEmpty()) {
            return variables;
        }
        Matcher matcher = PATRON_VARIABLE.matcher(contenido);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }
    
    /**
     *  Renderiza una plantilla de notificación con sus variables dinámicas
     * @param contenido Contenido de la plantilla con variables en formato {variable}
     * @param contexto Mapa con los valores para cada variable a reemplazar en el contenido de la plantilla
     * @return Contenido de la plantilla con las variables reemplazadas por sus valores correspondientes del contexto. Si una variable no tiene un valor en el contexto, se reemplaza por una cadena vacía.
     */
    public String renderizar(String contenido, Map<String, Object> contexto) {
        if (contenido == null || contenido.isEmpty()) {
            return contenido;
        }
        
        String resultado = contenido;
        
        for (Map.Entry<String, Object> entry : contexto.entrySet()) {
            String doubleCurly = "{{" + entry.getKey() + "}}";
            String singleCurly = "{" + entry.getKey() + "}";
            String valor = entry.getValue() != null ? entry.getValue().toString() : "";
            resultado = resultado.replace(doubleCurly, valor).replace(singleCurly, valor);
        }
        
        return resultado;
    }
    
    /**
    * Genera valores de ejemplo para un conjunto de variables, utilizando un mapa de valores por defecto. Si una variable no tiene un valor por defecto, se genera un valor de ejemplo genérico.
    * @param variables Conjunto de variables para las cuales se desean generar valores de ejemplo
    * @return Mapa con los valores de ejemplo generados para cada variable del conjunto
    */
    public Map<String, Object> generarValoresEjemplo(Set<String> variables) {
        Map<String, Object> ejemplos = getValoresEjemploPorDefecto();

        Map<String, Object> resultado = new HashMap<>();
        for (String variable : variables) {
            resultado.put(variable, ejemplos.getOrDefault(variable, "[EJEMPLO_" + variable.toUpperCase() + "]"));
        }
        
        return resultado;
    }
    
    /**
     * Proporciona un mapa de valores de ejemplo por defecto para variables comunes en plantillas de notificaciones. Este método se puede ampliar 
     * para incluir más variables y valores según las necesidades del sistema.
     * @return Mapa con valores de ejemplo por defecto para variables comunes en plantillas de notificaciones
     */
    private Map<String, Object> getValoresEjemploPorDefecto() {
        Map<String, Object> ejemplos = new HashMap<>();
        ejemplos.put("nombre", "María González");
        ejemplos.put("apellido", "González");
        ejemplos.put("email", "maria@ejemplo.com");
        ejemplos.put("telefono", "+57 300 123 4567");
        ejemplos.put("fecha_registro", "15/01/2024");
        ejemplos.put("fecha_vencimiento", "15/04/2025");
        ejemplos.put("monto", "$150.00");
        ejemplos.put("plan", "Premium");
        ejemplos.put("dias_restantes", "5");
        ejemplos.put("rol", "SOCIO");
        ejemplos.put("objetivo", "Aumentar masa muscular");
        ejemplos.put("nivel_experiencia", "INTERMEDIO");
        return ejemplos;
    }
}