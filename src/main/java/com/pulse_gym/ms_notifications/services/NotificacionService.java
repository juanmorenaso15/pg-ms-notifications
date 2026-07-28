package com.pulse_gym.ms_notifications.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.pulse_gym.lb_common.client.AuthClient;
import com.pulse_gym.lb_common.client.UsuarioClient;
import com.pulse_gym.lb_common.dto.AuthUserDTO;
import com.pulse_gym.lb_common.dto.EnvioNotificacionDTO;
import com.pulse_gym.lb_common.dto.EnvioEventoNotificacionDTO;
import com.pulse_gym.lb_common.dto.UsuarioPerfilResponseDTO;
import com.pulse_gym.lb_common.entity.notification.Notificacion;
import com.pulse_gym.lb_common.entity.notification.PlantillaNotificacion;
import com.pulse_gym.lb_common.enums.EnumCanalNotificacion;
import com.pulse_gym.lb_common.enums.EnumEstadoNotificacion;
import com.pulse_gym.lb_common.enums.EnumEventoAsociado;
import com.pulse_gym.ms_notifications.repository.NotificacionRepository;
import com.pulse_gym.ms_notifications.repository.PlantillaNotificationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    /**
     * Logger para la clase
     */
    private static final Logger logger = LoggerFactory.getLogger(NotificacionService.class);
    
    /**
     * Formato de fecha para la clase
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Servicio de email
     */
    private final EmailService emailService;
    
    /**
     * Servicio de WhatsApp
     */
    private final WhatsAppService whatsAppService;
    
    /**
     * Repositorio de notificaciones
     */ 
    private final NotificacionRepository notificacionRepository;
    
    /**
     * Repositorio de plantillas de notificaciones
     */
    private final PlantillaNotificationRepository plantillaRepository;
    
    /**
     * Servicio de renderizado de plantillas
     */
    private final PlantillaRenderService renderService;
    
    /**
     * Cliente para interactuar con el servicio de usuarios
     */
    private final UsuarioClient usuarioClient;
    
    /**
     * Cliente para interactuar con el servicio de autenticación
     */
    private final AuthClient authClient;
    
    /**
     * Servicio de preferencias de usuario
     */
    private final PreferenciaUsuarioService preferenciaUsuarioService;
    
    /**
     * Servicio de límite de tasa
     */
    private final RateLimitService rateLimitService;

    /**
     * Envía una notificación utilizando una plantilla con variables dinámicas.
     * Para eventos de autenticación (REGISTRO_USUARIO, LOGIN_USUARIO) no requiere
     * perfil de usuario.
     *
     * @param plantillaId          Identificador de la plantilla a utilizar
     * @param usuarioAuthId        Identificador del usuario en el sistema de
     *                             autenticación
     * @param variablesAdicionales Mapa con variables adicionales para renderizar la
     *                             plantilla
     * @throws RuntimeException Si la plantilla no existe, está eliminada o inactiva
     */
    public void enviarNotificacionConPlantilla(Long plantillaId, Long usuarioAuthId,
            Map<String, Object> variablesAdicionales) {

        PlantillaNotificacion plantilla = plantillaRepository.findById(plantillaId)
                .orElseThrow(() -> new RuntimeException("Plantilla no encontrada con id: " + plantillaId));

        if (Boolean.TRUE.equals(plantilla.getEliminada())) {
            throw new RuntimeException("La plantilla fue eliminada");
        }

        if (Boolean.FALSE.equals(plantilla.getEstado())) {
            throw new RuntimeException("La plantilla esta inactiva");
        }

        AuthUserDTO authUser = obtenerAuthUser(usuarioAuthId);
        EnumEventoAsociado evento = resolverEventoPlantilla(plantilla);
        UsuarioPerfilResponseDTO usuario = null;

        if (evento != EnumEventoAsociado.REGISTRO_USUARIO &&
                evento != EnumEventoAsociado.LOGIN_USUARIO) {
            try {
                usuario = obtenerPerfilPorEmail(authUser.getEmail());
            } catch (Exception e) {
                logger.warn("No se pudo obtener perfil para evento {}: {}", evento, e.getMessage());
            }
        } else {
            logger.info("Evento de autenticación {}, no se requiere perfil de usuario", evento);
        }

        EnumCanalNotificacion canal = plantilla.getTipoPlantilla();

        preferenciaUsuarioService.validarPreferenciasUsuario(usuarioAuthId, evento, canal);
        rateLimitService.validarLimiteEnvio(usuarioAuthId);

        Map<String, Object> contexto = construirContextoFlexible(usuario, authUser, variablesAdicionales, evento);
        String contenidoRenderizado = renderService.renderizar(plantilla.getContenido(), contexto);

        EnvioNotificacionDTO dto = new EnvioNotificacionDTO();
        dto.setUsuarioId(usuarioAuthId);
        dto.setContenido(contenidoRenderizado);
        dto.setAsunto(plantilla.getTitulo());
        dto.setCanal(canal.name());
        dto.setPlantillaId(plantillaId);
        dto.setTipoEvento(evento.name());

        if (canal == EnumCanalNotificacion.EMAIL) {
            dto.setDestinatario(authUser.getEmail());
        } else if (usuario != null && usuario.getTelefono() != null && !usuario.getTelefono().isEmpty()) {
            dto.setDestinatario(usuario.getTelefono());
        } else {
            logger.warn("No se puede enviar WhatsApp: usuario {} no tiene teléfono", usuarioAuthId);
            return;
        }

        enviarNotificacion(dto);
    }

    /**
     * Envía una notificación basada en un evento, utilizando la plantilla activa
     * configurada para dicho evento.
     *
     * @param request DTO con los datos del evento y el usuario destino
     * @throws RuntimeException Si no existe una plantilla activa para el evento
     *                          especificado
     */
    @Transactional
    public void enviarNotificacionPorEvento(EnvioEventoNotificacionDTO request) {
        logger.info("Buscando plantilla para evento: {} y usuario: {}", request.getEvento(), request.getUsuarioId());

        List<PlantillaNotificacion> plantillas = plantillaRepository
                .findByEventosAsociadosContainingAndEstadoTrueAndEliminadaFalse(request.getEvento());

        if (plantillas.isEmpty()) {
            logger.error("No existe plantilla activa para el evento: {} - usuario: {}", request.getEvento(),
                    request.getUsuarioId());
            throw new RuntimeException("No existe plantilla activa para el evento: " + request.getEvento());
        }

        PlantillaNotificacion plantilla = plantillas.get(0);
        logger.info("Plantilla encontrada: {} (ID: {}) para evento: {}",
                plantilla.getNombre(), plantilla.getIdPlantilla(), request.getEvento());

        enviarNotificacionConPlantilla(
                plantilla.getIdPlantilla(),
                request.getUsuarioId(),
                request.getVariablesAdicionales());
    }

    /**
     * Envía una notificación por el canal indicado (EMAIL o WHATSAPP).
     * Registra la notificación en la base de datos y actualiza su estado según el
     * resultado del envío.
     *
     * @param dto DTO con los datos del envío
     * @throws RuntimeException Si ocurre un error durante el envío de la
     *                          notificación
     */
    public void enviarNotificacion(EnvioNotificacionDTO dto) {
        logger.info("Enviando notificacion a {} por canal {}", dto.getDestinatario(), dto.getCanal());

        EnumCanalNotificacion canal = EnumCanalNotificacion.valueOf(dto.getCanal().toUpperCase());
        EnumEventoAsociado evento = dto.getTipoEvento() != null
                ? EnumEventoAsociado.valueOf(dto.getTipoEvento())
                : EnumEventoAsociado.WELCOME;

        preferenciaUsuarioService.validarPreferenciasUsuario(dto.getUsuarioId(), evento, canal);
        rateLimitService.validarLimiteEnvio(dto.getUsuarioId());

        PlantillaNotificacion plantilla = null;
        if (dto.getPlantillaId() != null) {
            plantilla = plantillaRepository.findById(dto.getPlantillaId()).orElse(null);
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setId_usuario(dto.getUsuarioId());
        notificacion.setId_plantilla(plantilla);
        notificacion.setTitulo(dto.getAsunto() != null ? dto.getAsunto() : "Notificacion Pulse Gym");
        notificacion.setContenido(dto.getContenido());
        notificacion.setEstado(EnumEstadoNotificacion.PENDIENTE);
        notificacion.setFechaEnvio(LocalDateTime.now());
        notificacionRepository.save(notificacion);

        try {
            if (canal == EnumCanalNotificacion.EMAIL) {
                Map<String, Object> contexto = construirContextoParaEmail(dto);
                emailService.enviarEmailHtml(
                        dto.getDestinatario(),
                        notificacion.getTitulo(),
                        dto.getContenido(), evento, contexto);
                notificacion.setEstado(EnumEstadoNotificacion.ENVIADO);
            } else {
                whatsAppService.enviarWhatsApp(dto.getDestinatario(), dto.getContenido());
                notificacion.setEstado(EnumEstadoNotificacion.ENVIADO);
            }

            notificacionRepository.save(notificacion);
            logger.info("Notificacion enviada exitosamente a {}", dto.getDestinatario());
        } catch (Exception e) {
            logger.error("Error al enviar notificacion: {}", e.getMessage());
            notificacion.setEstado(EnumEstadoNotificacion.RECHAZADO);
            notificacionRepository.save(notificacion);
            throw new RuntimeException("Error al enviar notificacion: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene los datos del usuario desde el microservicio de autenticación.
     *
     * @param usuarioAuthId Identificador del usuario en auth
     * @return DTO con los datos del usuario
     * @throws RuntimeException Si no se pueden obtener los datos del usuario
     */
    private AuthUserDTO obtenerAuthUser(Long usuarioAuthId) {
        try {
            return authClient.obtenerUsuarioPorId(usuarioAuthId);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener datos de auth para el usuario: " + usuarioAuthId);
        }
    }

    /**
     * Obtiene el perfil del usuario desde el microservicio de usuarios por su
     * email.
     *
     * @param email Email del usuario
     * @return DTO con el perfil del usuario, o null si no se encuentra
     */
    private UsuarioPerfilResponseDTO obtenerPerfilPorEmail(String email) {
        try {
            return usuarioClient.obtenerUsuarioPorEmail(email);
        } catch (Exception e) {
            logger.warn("No se pudo obtener perfil por email {}: {}", email, e.getMessage());
            return null;
        }
    }

    /**
     * Resuelve el evento asociado a una plantilla de notificación.
     * Prioriza la colección de eventos asociados sobre el evento individual.
     *
     * @param plantilla Plantilla de notificación
     * @return Evento asociado a la plantilla
     */
    private EnumEventoAsociado resolverEventoPlantilla(PlantillaNotificacion plantilla) {
        if (plantilla.getEventosAsociados() != null && !plantilla.getEventosAsociados().isEmpty()) {
            return plantilla.getEventosAsociados().iterator().next();
        }
        return plantilla.getEventoAsociado();
    }

    /**
     * Construye el contexto de variables para renderizar la plantilla de forma
     * flexible.
     * Prioriza los datos disponibles: primero los de autenticación, luego los del
     * perfil,
     * y finalmente las variables adicionales.
     *
     * @param usuario              Perfil del usuario (puede ser null)
     * @param authUser             Datos de autenticación del usuario
     * @param variablesAdicionales Variables adicionales proporcionadas externamente
     * @param evento               Evento que dispara la notificación
     * @return Mapa con el contexto completo para renderizar la plantilla
     */
    private Map<String, Object> construirContextoFlexible(UsuarioPerfilResponseDTO usuario,
            AuthUserDTO authUser,
            Map<String, Object> variablesAdicionales,
            EnumEventoAsociado evento) {

        Map<String, Object> contexto = new HashMap<>();

        if (authUser != null) {
            contexto.put("email", authUser.getEmail());
            contexto.put("username", authUser.getUsername());
            contexto.put("rol", authUser.getRol() != null ? authUser.getRol().toString() : null);
            contexto.put("estado_usuario", authUser.getEstado());
            contexto.put("id_usuario", authUser.getId());
        }

        if (usuario != null) {
            contexto.put("id", usuario.getIdUsuario());
            contexto.put("nombre", usuario.getNombre());
            contexto.put("apellido", usuario.getApellido());
            contexto.put("apellidos", usuario.getApellido());
            contexto.put("telefono", usuario.getTelefono());
            contexto.put("documento", usuario.getDocumentoIdentidad());

            if (usuario.getFechaRegistro() != null) {
                contexto.put("fecha_registro", usuario.getFechaRegistro().format(DATE_FORMATTER));
            }

            if (usuario.getFechaNacimiento() != null) {
                contexto.put("fecha_nacimiento", usuario.getFechaNacimiento().format(DATE_FORMATTER));
            }

            if (usuario.getObjetivoPrincipal() != null) {
                contexto.put("objetivo", usuario.getObjetivoPrincipal());
                contexto.put("objetivo_principal", usuario.getObjetivoPrincipal());
            }

            if (usuario.getNivelExperiencia() != null) {
                contexto.put("nivel_experiencia", usuario.getNivelExperiencia().toString());
            }
        }

        if (variablesAdicionales != null) {
            contexto.putAll(variablesAdicionales);
        }

        return contexto;
    }

    /**
     * Construye el contexto con variables del usuario para pasar al diseño del
     * email.
     * Este contexto se utiliza para reemplazar variables en el header y footer del
     * email.
     *
     * @param dto DTO con los datos del envío
     * @return Mapa con variables para el diseño del email
     */
    private Map<String, Object> construirContextoParaEmail(EnvioNotificacionDTO dto) {
        Map<String, Object> contexto = new HashMap<>();

        if (dto.getUsuarioId() != null) {
            contexto.put("usuario_id", dto.getUsuarioId());
        }

        return contexto;
    }
}