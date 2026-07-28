package com.pulse_gym.ms_notifications.config;

import java.time.LocalDateTime;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.pulse_gym.lb_common.entity.notification.PlantillaDisenoEmail;
import com.pulse_gym.lb_common.entity.notification.PlantillaNotificacion;
import com.pulse_gym.lb_common.enums.EnumCanalNotificacion;
import com.pulse_gym.lb_common.enums.EnumEventoAsociado;
import com.pulse_gym.ms_notifications.repository.PlantillaDisenoEmailRepository;
import com.pulse_gym.ms_notifications.repository.PlantillaNotificationRepository;

/**
 * Inicializa las plantillas y diseños de email por defecto al iniciar la aplicación.
 * Se ejecuta solo si no existen registros en la base de datos.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    /**
     * Logger para registrar el proceso de inicialización de datos. Se utiliza para informar sobre la creación de plantillas y diseños, así como para advertir si se están creando registros por defecto.
     */
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    /**
     * Repositorio para gestionar los diseños de email. Se utiliza para verificar la existencia de diseños asociados a eventos específicos y para crear nuevos diseños si es necesario.
     */
    private final PlantillaDisenoEmailRepository disenoRepository;

    /**
     * Repositorio para gestionar las plantillas de notificación. Se utiliza para verificar la existencia de plantillas asociadas a eventos específicos y para crear nuevas plantillas si es necesario.
     */
    private final PlantillaNotificationRepository plantillaRepository;

    /**
     * Constructor para inicializar los repositorios necesarios.
     *
     * @param disenoRepository Repositorio para gestionar los diseños de email.
     * @param plantillaRepository Repositorio para gestionar las plantillas de notificación.
     */
    public DataInitializer(PlantillaDisenoEmailRepository disenoRepository,
            PlantillaNotificationRepository plantillaRepository) {
        this.disenoRepository = disenoRepository;
        this.plantillaRepository = plantillaRepository;
    }

    /** Método que se ejecuta al iniciar la aplicación. Verifica la existencia de plantillas y diseños asociados a eventos específicos y los crea si no existen. 
     * Se registran logs para informar sobre el proceso de inicialización. 
     * 
     * Eventos cubiertos:
     * - REGISTRO_USUARIO
     * - LOGIN_USUARIO
     * - WELCOME
     * 
     * */
    @Override
    public void run(String... args) {
        logger.info("Verificando e inicializando plantillas de notificacion si faltan...");
        inicializarPlantillasFaltantes();

        logger.info("Verificando e inicializando diseños de email si faltan...");
        inicializarDisenosFaltantes();
    }

    /**
     * Inicializa las plantillas de notificación que faltan.
     */
    private void inicializarPlantillasFaltantes() {
        // 1. REGISTRO_USUARIO
        if (plantillaRepository.findByEventosAsociadosContainingAndEstadoTrueAndEliminadaFalse(EnumEventoAsociado.REGISTRO_USUARIO).isEmpty()) {
            PlantillaNotificacion registro = new PlantillaNotificacion();
            registro.setNombre("Registro de Usuario");
            registro.setTitulo("Bienvenido a Pulse Gym");
            registro.setDescripcion("Notificacion de bienvenida al registrar nuevo usuario");
            registro.setContenido("Hola {username}! Te damos la bienvenida a Pulse Gym. Tu cuenta ha sido creada exitosamente con el email {email}.");
            registro.setTipoPlantilla(EnumCanalNotificacion.EMAIL);
            registro.setEventoAsociado(EnumEventoAsociado.REGISTRO_USUARIO);
            registro.setEventosAsociados(Set.of(EnumEventoAsociado.REGISTRO_USUARIO));
            registro.setEstado(true);
            registro.setEliminada(false);
            registro.setFechaCreacion(LocalDateTime.now());
            plantillaRepository.save(registro);
            logger.info("Plantilla REGISTRO_USUARIO creada.");
        }

        // 2. LOGIN_USUARIO
        if (plantillaRepository.findByEventosAsociadosContainingAndEstadoTrueAndEliminadaFalse(EnumEventoAsociado.LOGIN_USUARIO).isEmpty()) {
            PlantillaNotificacion login = new PlantillaNotificacion();
            login.setNombre("Login de Usuario");
            login.setTitulo("Inicio de sesion detectado");
            login.setDescripcion("Notificacion de inicio de sesion");
            login.setContenido("Hola {username}! Se ha iniciado sesion en tu cuenta desde un nuevo dispositivo. Email: {email}. Si no fuiste tú, contacta a soporte.");
            login.setTipoPlantilla(EnumCanalNotificacion.EMAIL);
            login.setEventoAsociado(EnumEventoAsociado.LOGIN_USUARIO);
            login.setEventosAsociados(Set.of(EnumEventoAsociado.LOGIN_USUARIO));
            login.setEstado(true);
            login.setEliminada(false);
            login.setFechaCreacion(LocalDateTime.now());
            plantillaRepository.save(login);
            logger.info("Plantilla LOGIN_USUARIO creada.");
        }

        // 3. WELCOME
        if (plantillaRepository.findByEventosAsociadosContainingAndEstadoTrueAndEliminadaFalse(EnumEventoAsociado.WELCOME).isEmpty()) {
            PlantillaNotificacion welcome = new PlantillaNotificacion();
            welcome.setNombre("Bienvenida a Pulse Gym");
            welcome.setTitulo("¡Bienvenido a Pulse Gym, {nombre}!");
            welcome.setDescripcion("Notificación de bienvenida al completar el perfil");
            welcome.setContenido("Hola {nombre} {apellido}! Gracias por completar tu perfil. Ahora puedes acceder a todas las funcionalidades de Pulse Gym. Tu objetivo \"{objetivo}\" está más cerca.");
            welcome.setTipoPlantilla(EnumCanalNotificacion.EMAIL);
            welcome.setEventoAsociado(EnumEventoAsociado.WELCOME);
            welcome.setEventosAsociados(Set.of(EnumEventoAsociado.WELCOME));
            welcome.setEstado(true);
            welcome.setEliminada(false);
            welcome.setFechaCreacion(LocalDateTime.now());
            plantillaRepository.save(welcome);
            logger.info("Plantilla WELCOME creada.");
        }
    }

    /**
     * Inicializa los diseños de email que faltan.
     */     
    private void inicializarDisenosFaltantes() {
        // Default
        if (disenoRepository.findByNombreAndEliminadoFalseAndActivoTrue("default").isEmpty()) {
            disenoRepository.save(crearDisenoDefault());
            logger.info("Diseño DEFAULT creado.");
        }
        
        // Registro
        if (disenoRepository.findByEventoAsociadoAndEliminadoFalseAndActivoTrue(EnumEventoAsociado.REGISTRO_USUARIO).isEmpty()) {
            PlantillaDisenoEmail registroDiseno = new PlantillaDisenoEmail();
            registroDiseno.setNombre("registro");
            registroDiseno.setEventoAsociado(EnumEventoAsociado.REGISTRO_USUARIO);
            registroDiseno.setCanal(EnumCanalNotificacion.EMAIL);
            registroDiseno.setColorPrincipal("#2c4b77");
            registroDiseno.setColorSecundario("#8bb5d6");
            registroDiseno.setColorTextoHeader("#ffffff");
            registroDiseno.setTituloHeader("¡Bienvenido a Pulse Gym!");
            registroDiseno.setSubtituloHeader("Comienza tu viaje fitness hoy");
            registroDiseno.setColorFondoContenido("#ffffff");
            registroDiseno.setColorTextoContenido("#5d6d7e");
            registroDiseno.setColorFondoFooter("#f8f9fc");
            registroDiseno.setColorTextoFooter("#9aabbb");
            registroDiseno.setTextoFooter("© 2026 Pulse Gym - Todos los derechos reservados");
            registroDiseno.setTextoFooterSecundario("Este es un mensaje automático, por favor no responder a este correo");
            registroDiseno.setActivo(true);
            registroDiseno.setEliminado(false);
            registroDiseno.setFechaCreacion(LocalDateTime.now());
            disenoRepository.save(registroDiseno);
            logger.info("Diseño REGISTRO_USUARIO creado.");
        }

        // Login
        if (disenoRepository.findByEventoAsociadoAndEliminadoFalseAndActivoTrue(EnumEventoAsociado.LOGIN_USUARIO).isEmpty()) {
            PlantillaDisenoEmail loginDiseno = new PlantillaDisenoEmail();
            loginDiseno.setNombre("login");
            loginDiseno.setEventoAsociado(EnumEventoAsociado.LOGIN_USUARIO);
            loginDiseno.setCanal(EnumCanalNotificacion.EMAIL);
            loginDiseno.setColorPrincipal("#f39c12");
            loginDiseno.setColorSecundario("#e67e22");
            loginDiseno.setColorTextoHeader("#ffffff");
            loginDiseno.setTituloHeader("Nuevo inicio de sesión");
            loginDiseno.setSubtituloHeader("Pulse Gym - Seguridad de tu cuenta");
            loginDiseno.setColorFondoContenido("#ffffff");
            loginDiseno.setColorTextoContenido("#5d6d7e");
            loginDiseno.setColorFondoFooter("#f8f9fc");
            loginDiseno.setColorTextoFooter("#9aabbb");
            loginDiseno.setTextoFooter("© 2026 Pulse Gym - Todos los derechos reservados");
            loginDiseno.setTextoFooterSecundario("Si no reconoces esta actividad, contacta a soporte inmediatamente");
            loginDiseno.setActivo(true);
            loginDiseno.setEliminado(false);
            loginDiseno.setFechaCreacion(LocalDateTime.now());
            disenoRepository.save(loginDiseno);
            logger.info("Diseño LOGIN_USUARIO creado.");
        }

        // Welcome
        if (disenoRepository.findByEventoAsociadoAndEliminadoFalseAndActivoTrue(EnumEventoAsociado.WELCOME).isEmpty()) {
            PlantillaDisenoEmail welcomeDiseno = new PlantillaDisenoEmail();
            welcomeDiseno.setNombre("welcome");
            welcomeDiseno.setEventoAsociado(EnumEventoAsociado.WELCOME);
            welcomeDiseno.setCanal(EnumCanalNotificacion.EMAIL);
            welcomeDiseno.setColorPrincipal("#2c4b77");
            welcomeDiseno.setColorSecundario("#8bb5d6");
            welcomeDiseno.setColorTextoHeader("#ffffff");
            welcomeDiseno.setTituloHeader("¡Bienvenido a Pulse Gym!");
            welcomeDiseno.setSubtituloHeader("Tu viaje fitness comienza hoy");
            welcomeDiseno.setColorFondoContenido("#ffffff");
            welcomeDiseno.setColorTextoContenido("#5d6d7e");
            welcomeDiseno.setColorFondoFooter("#f8f9fc");
            welcomeDiseno.setColorTextoFooter("#9aabbb");
            welcomeDiseno.setTextoFooter("© 2026 Pulse Gym - Todos los derechos reservados");
            welcomeDiseno.setTextoFooterSecundario("Este es un mensaje automático, por favor no responder a este correo");
            welcomeDiseno.setActivo(true);
            welcomeDiseno.setEliminado(false);
            welcomeDiseno.setFechaCreacion(LocalDateTime.now());
            disenoRepository.save(welcomeDiseno);
            logger.info("Diseño WELCOME creado.");
        }

        // Promocion
        if (disenoRepository.findByEventoAsociadoAndEliminadoFalseAndActivoTrue(EnumEventoAsociado.PROMOTION).isEmpty()) {
            PlantillaDisenoEmail promocion = new PlantillaDisenoEmail();
            promocion.setNombre("promocion");
            promocion.setEventoAsociado(EnumEventoAsociado.PROMOTION);
            promocion.setCanal(EnumCanalNotificacion.EMAIL);
            promocion.setColorPrincipal("#ea1616");
            promocion.setColorSecundario("#c83f3f");
            promocion.setColorTextoHeader("#ffffff");
            promocion.setTituloHeader("Pulse Gym");
            promocion.setSubtituloHeader("Tu bienestar, nuestra pasión");
            promocion.setColorFondoContenido("#ffffff");
            promocion.setColorTextoContenido("#5d6d7e");
            promocion.setColorFondoFooter("#f8f9fc");
            promocion.setColorTextoFooter("#9aabbb");
            promocion.setTextoFooter("© 2026 Pulse Gym - Todos los derechos reservados");
            promocion.setTextoFooterSecundario("Este es un mensaje automático, por favor no responder a este correo");
            promocion.setActivo(true);
            promocion.setEliminado(false);
            promocion.setFechaCreacion(LocalDateTime.now());
            disenoRepository.save(promocion);
            logger.info("Diseño PROMOTION creado.");
        }

        // Achievement
        if (disenoRepository.findByEventoAsociadoAndEliminadoFalseAndActivoTrue(EnumEventoAsociado.ACHIEVEMENT).isEmpty()) {
            PlantillaDisenoEmail logro = new PlantillaDisenoEmail();
            logro.setNombre("logro");
            logro.setEventoAsociado(EnumEventoAsociado.ACHIEVEMENT);
            logro.setCanal(EnumCanalNotificacion.EMAIL);
            logro.setColorPrincipal("#d4af37");
            logro.setColorSecundario("#f4d03f");
            logro.setColorTextoHeader("#ffffff");
            logro.setTituloHeader("Pulse Gym");
            logro.setSubtituloHeader("¡Felicitaciones!");
            logro.setColorFondoContenido("#ffffff");
            logro.setColorTextoContenido("#5d6d7e");
            logro.setColorFondoFooter("#f8f9fc");
            logro.setColorTextoFooter("#9aabbb");
            logro.setTextoFooter("© 2026 Pulse Gym - Todos los derechos reservados");
            logro.setTextoFooterSecundario("Este es un mensaje automático, por favor no responder a este correo");
            logro.setActivo(true);
            logro.setEliminado(false);
            logro.setFechaCreacion(LocalDateTime.now());
            disenoRepository.save(logro);
            logger.info("Diseño ACHIEVEMENT creado.");
        }

        // Payment reminder
        if (disenoRepository.findByEventoAsociadoAndEliminadoFalseAndActivoTrue(EnumEventoAsociado.PAYMENT_REMINDER).isEmpty()) {
            PlantillaDisenoEmail pago = new PlantillaDisenoEmail();
            pago.setNombre("pago");
            pago.setEventoAsociado(EnumEventoAsociado.PAYMENT_REMINDER);
            pago.setCanal(EnumCanalNotificacion.EMAIL);
            pago.setColorPrincipal("#e67e22");
            pago.setColorSecundario("#f39c12");
            pago.setColorTextoHeader("#ffffff");
            pago.setTituloHeader("Pulse Gym");
            pago.setSubtituloHeader("Recordatorio de pago");
            pago.setColorFondoContenido("#ffffff");
            pago.setColorTextoContenido("#5d6d7e");
            pago.setColorFondoFooter("#f8f9fc");
            pago.setColorTextoFooter("#9aabbb");
            pago.setTextoFooter("© 2026 Pulse Gym - Todos los derechos reservados");
            pago.setTextoFooterSecundario("Este es un mensaje automático, por favor no responder a este correo");
            pago.setActivo(true);
            pago.setEliminado(false);
            pago.setFechaCreacion(LocalDateTime.now());
            disenoRepository.save(pago);
            logger.info("Diseño PAYMENT_REMINDER creado.");
        }

        // Maintenance alert
        if (disenoRepository.findByEventoAsociadoAndEliminadoFalseAndActivoTrue(EnumEventoAsociado.MAINTENANCE_ALERT).isEmpty()) {
            PlantillaDisenoEmail mantenimiento = new PlantillaDisenoEmail();
            mantenimiento.setNombre("mantenimiento");
            mantenimiento.setEventoAsociado(EnumEventoAsociado.MAINTENANCE_ALERT);
            mantenimiento.setCanal(EnumCanalNotificacion.EMAIL);
            mantenimiento.setColorPrincipal("#7f8c8d");
            mantenimiento.setColorSecundario("#95a5a6");
            mantenimiento.setColorTextoHeader("#ffffff");
            mantenimiento.setTituloHeader("Pulse Gym");
            mantenimiento.setSubtituloHeader("Aviso importante");
            mantenimiento.setColorFondoContenido("#ffffff");
            mantenimiento.setColorTextoContenido("#5d6d7e");
            mantenimiento.setColorFondoFooter("#f8f9fc");
            mantenimiento.setColorTextoFooter("#9aabbb");
            mantenimiento.setTextoFooter("© 2026 Pulse Gym - Todos los derechos reservados");
            mantenimiento.setTextoFooterSecundario("Este es un mensaje automático, por favor no responder a este correo");
            mantenimiento.setActivo(true);
            mantenimiento.setEliminado(false);
            mantenimiento.setFechaCreacion(LocalDateTime.now());
            disenoRepository.save(mantenimiento);
            logger.info("Diseño MAINTENANCE_ALERT creado.");
        }
    }

    /**
     * Crea un diseño de email por defecto que se utilizará para eventos que no tengan un diseño específico asignado. Este diseño incluye colores, títulos y textos genéricos que reflejan la identidad de Pulse Gym.
     * @return Un objeto PlantillaDisenoEmail con la configuración por defecto.
     */
    private PlantillaDisenoEmail crearDisenoDefault() {
        logger.warn("Creando diseño por defecto");
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
        diseno.setActivo(true);
        diseno.setEliminado(false);
        diseno.setFechaCreacion(LocalDateTime.now());
        return diseno;
    }
}