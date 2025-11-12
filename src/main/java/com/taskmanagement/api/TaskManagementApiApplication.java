package com.taskmanagement.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Clase principal de la aplicación Spring Boot.
 *
 * Anotaciones:
 * @SpringBootApplication es una anotación de conveniencia que combina:
 * - @Configuration: Marca la clase como fuente de definiciones de beans
 * - @EnableAutoConfiguration: Habilita la configuración automática de Spring Boot
 * - @ComponentScan: Escanea componentes, configuraciones y servicios en el paquete actual
 *
 * @EnableJpaAuditing: Habilita la auditoría automática de JPA.
 * Permite usar anotaciones como @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy
 * en las entidades para rastrear automáticamente cuándo y quién creó/modificó cada registro.
 *
 * Con esta configuración, Spring Data JPA automáticamente:
 * - Rellena @CreatedDate cuando se crea una entidad
 * - Actualiza @LastModifiedDate cuando se modifica una entidad
 * - (Opcional) Rellena @CreatedBy y @LastModifiedBy si se implementa AuditorAware
 */
@SpringBootApplication
@EnableJpaAuditing
public class TaskManagementApiApplication {

	/**
	 * Método principal que inicia la aplicación Spring Boot
	 *
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(TaskManagementApiApplication.class, args);
	}
}
