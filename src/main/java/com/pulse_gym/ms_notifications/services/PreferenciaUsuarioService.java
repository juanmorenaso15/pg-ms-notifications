package com.pulse_gym.ms_notifications.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulse_gym.lb_common.dto.PreferenciaUsuarioRequestDTO;
import com.pulse_gym.lb_common.dto.PreferenciaUsuarioResponseDTO;
import com.pulse_gym.lb_common.dto.VerificarPreferenciaRequestDTO;
import com.pulse_gym.lb_common.dto.VerificarPreferenciaResponseDTO;
import com.pulse_gym.lb_common.entity.notification.PreferenciaUsuario;
import com.pulse_gym.lb_common.enums.EnumCanalNotificacion;
import com.pulse_gym.lb_common.enums.EnumEventoAsociado;
import com.pulse_gym.lb_common.enums.EnumPreferenciaUsuario;
import com.pulse_gym.lb_common.services.ValidacionDeRoles;
import com.pulse_gym.ms_notifications.repository.PreferenciaUsuarioRepository;
import com.pulse_gym.ms_notifications.util.EventoNotificacionUtil;

import lombok.RequiredArgsConstructor;

/**
 * Servicio para gestionar las preferencias de notificaciones de los usuarios.
 * Permite obtener, actualizar y validar preferencias de canal y categorías de
 * notificaciones.
 */
@Service
@RequiredArgsConstructor
public class PreferenciaUsuarioService {

    /** Repositorio para operaciones CRUD de preferencias de usuario */
    private final PreferenciaUsuarioRepository preferenciaUsuarioRepository;

    /** Servicio para validar límites de envío de notificaciones */
    private final RateLimitService rateLimitService;

    /**
     * Obtiene las preferencias del usuario autenticado.
     * Si el usuario no tiene preferencias configuradas, se crean con valores por
     * defecto.
     *
     * @param usuarioId Identificador del usuario en el sistema de autenticación
     * @param userRol   Rol del usuario autenticado (debe ser SOCIO)
     * @return DTO con las preferencias del usuario
     */
    @Transactional(readOnly = true)
    public PreferenciaUsuarioResponseDTO obtenerMisPreferencias(Long usuarioId, String userRol) {
        ValidacionDeRoles.validarSocio(userRol);
        return mapearAResponse(obtenerOPreferenciasPorDefecto(usuarioId));
    }

    /**
     * Actualiza las preferencias del usuario autenticado.
     *
     * @param usuarioId Identificador del usuario en el sistema de autenticación
     * @param request   DTO con los nuevos datos de preferencias
     * @param userRol   Rol del usuario autenticado (debe ser SOCIO)
     * @return DTO con las preferencias actualizadas
     */
    @Transactional
    public PreferenciaUsuarioResponseDTO actualizarMisPreferencias(
            Long usuarioId, PreferenciaUsuarioRequestDTO request, String userRol) {

        ValidacionDeRoles.validarSocio(userRol);

        PreferenciaUsuario preferencia = preferenciaUsuarioRepository.findByIdUsuario(usuarioId)
                .orElseGet(() -> crearPreferenciaPorDefecto(usuarioId));

        preferencia.setPreferencia(request.getPreferencia());
        preferencia.setLogros_habilitado(request.getLogrosHabilitado());
        preferencia.setMantenimientos_habilitado(request.getMantenimientosHabilitado());
        preferencia.setPromociones_habilitado(request.getPromocionesHabilitado());

        preferenciaUsuarioRepository.save(preferencia);
        return mapearAResponse(preferencia);
    }

    /**
     * Verifica si un envío de notificación está permitido según las preferencias
     * del usuario
     * y los límites de rate limiting.
     *
     * @param request DTO con los datos de verificación (usuarioId, tipoEvento,
     *                canal)
     * @return DTO con el resultado de la verificación (permitido + motivo)
     */
    @Transactional(readOnly = true)
    public VerificarPreferenciaResponseDTO verificarEnvioPermitido(VerificarPreferenciaRequestDTO request) {
        VerificarPreferenciaResponseDTO response = new VerificarPreferenciaResponseDTO();

        try {
            validarPreferenciasUsuario(
                    request.getUsuarioId(),
                    request.getTipoEvento(),
                    request.getCanal());
            rateLimitService.validarLimiteEnvio(request.getUsuarioId());
            response.setPermitido(true);
            response.setMotivo("Envio permitido");
        } catch (RuntimeException ex) {
            response.setPermitido(false);
            response.setMotivo(ex.getMessage());
        }

        return response;
    }

    /**
     * Valida las preferencias de canal y categoría antes de un envío.
     * Para eventos de autenticación (REGISTRO_USUARIO, LOGIN_USUARIO) solo valida
     * el canal,
     * saltando la validación de categorías (logros, mantenimiento, promociones).
     *
     * @param usuarioId  Identificador del usuario en el sistema de autenticación
     * @param tipoEvento Evento de la notificación a enviar
     * @param canal      Canal por el que se desea enviar la notificación
     */
    @Transactional
    public void validarPreferenciasUsuario(Long usuarioId, EnumEventoAsociado tipoEvento, EnumCanalNotificacion canal) {
        PreferenciaUsuario preferencia = obtenerOPreferenciasPorDefecto(usuarioId);

        if (!EventoNotificacionUtil.canalHabilitado(preferencia.getPreferencia(), canal)) {
            throw new RuntimeException("El usuario no acepta notificaciones por el canal solicitado");
        }

        // Para eventos de autenticación, no validamos categorías (logros,
        // mantenimiento, promociones)
        if (tipoEvento == EnumEventoAsociado.REGISTRO_USUARIO || tipoEvento == EnumEventoAsociado.LOGIN_USUARIO) {
            return;
        }

        if (EventoNotificacionUtil.esLogro(tipoEvento) && Boolean.FALSE.equals(preferencia.getLogros_habilitado())) {
            throw new RuntimeException("El usuario deshabilito notificaciones de logros");
        }

        if (EventoNotificacionUtil.esMantenimiento(tipoEvento)
                && Boolean.FALSE.equals(preferencia.getMantenimientos_habilitado())) {
            throw new RuntimeException("El usuario deshabilito notificaciones de mantenimiento");
        }

        if (EventoNotificacionUtil.esPromocion(tipoEvento)
                && Boolean.FALSE.equals(preferencia.getPromociones_habilitado())) {
            throw new RuntimeException("El usuario deshabilito notificaciones promocionales");
        }
    }

    /**
     * Obtiene las preferencias del usuario o crea una nueva con valores por defecto
     * si no existen.
     *
     * @param usuarioId Identificador del usuario en el sistema de autenticación
     * @return Entidad de preferencias del usuario
     */
    private PreferenciaUsuario obtenerOPreferenciasPorDefecto(Long usuarioId) {
        return preferenciaUsuarioRepository.findByIdUsuario(usuarioId)
                .orElseGet(() -> crearPreferenciaPorDefecto(usuarioId));
    }

    /**
     * Crea las preferencias del usuario con valores por defecto:
     * - Canal: AMBOS
     * - Logros: habilitado
     * - Mantenimientos: habilitado
     * - Promociones: habilitado
     *
     * @param usuarioId Identificador del usuario en el sistema de autenticación
     * @return Entidad de preferencias recién creada
     */
    private PreferenciaUsuario crearPreferenciaPorDefecto(Long usuarioId) {
        PreferenciaUsuario preferencia = new PreferenciaUsuario();
        preferencia.setIdUsuario(usuarioId);
        preferencia.setPreferencia(EnumPreferenciaUsuario.AMBOS);
        preferencia.setLogros_habilitado(true);
        preferencia.setMantenimientos_habilitado(true);
        preferencia.setPromociones_habilitado(true);
        return preferenciaUsuarioRepository.save(preferencia);
    }

    /**
     * Convierte una entidad PreferenciaUsuario a su correspondiente DTO de
     * respuesta.
     *
     * @param preferencia Entidad de preferencias a convertir
     * @return DTO con los datos de preferencias mapeados
     */
    private PreferenciaUsuarioResponseDTO mapearAResponse(PreferenciaUsuario preferencia) {
        PreferenciaUsuarioResponseDTO dto = new PreferenciaUsuarioResponseDTO();
        dto.setIdUsuario(preferencia.getIdUsuario());
        dto.setPreferencia(preferencia.getPreferencia());
        dto.setLogrosHabilitado(preferencia.getLogros_habilitado());
        dto.setMantenimientosHabilitado(preferencia.getMantenimientos_habilitado());
        dto.setPromocionesHabilitado(preferencia.getPromociones_habilitado());
        return dto;
    }
}