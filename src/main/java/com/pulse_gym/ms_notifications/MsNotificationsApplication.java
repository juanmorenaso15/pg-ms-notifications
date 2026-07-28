package com.pulse_gym.ms_notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Clase principal que inicializa y arranca el microservicio de notificaciones.
 * Esta clase configura e inicia la aplicación Spring Boot, habilitando la ejecución
 * asíncrona y el cliente Feign para la comunicación entre microservicios.
 */
@SpringBootApplication(scanBasePackages = "com.pulse_gym")

@EntityScan("com.pulse_gym.lb_common.entity.notification")

@EnableAsync
@EnableFeignClients(basePackages = "com.pulse_gym.lb_common.client") 
public class MsNotificationsApplication {

	/**
	 * Punto de entrada principal de la aplicación.
	 * Inicializa el contexto de Spring y arranca el microservicio de notificaciones.
	 *
	 * @param args Argumentos de la línea de comandos pasados al iniciar la aplicación.
	 */
	public static void main(String[] args) {
		SpringApplication.run(MsNotificationsApplication.class, args);
	}

}
