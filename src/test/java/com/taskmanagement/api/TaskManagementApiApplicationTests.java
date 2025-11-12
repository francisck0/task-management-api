package com.taskmanagement.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Clase de prueba principal de la aplicación.
 *
 * @SpringBootTest: Carga el contexto completo de Spring para pruebas de integración
 *
 * Esta es una prueba básica que verifica que el contexto de Spring se carga correctamente.
 * Es útil para detectar problemas de configuración o dependencias faltantes.
 *
 * Para ejecutar las pruebas:
 * - Desde línea de comandos: ./gradlew test
 * - Desde IDE: Click derecho > Run Tests
 */
@SpringBootTest
class TaskManagementApiApplicationTests {

	/**
	 * Prueba que verifica que el contexto de la aplicación se carga correctamente.
	 *
	 * Si esta prueba falla, significa que hay un problema con:
	 * - Configuración de beans
	 * - Dependencias faltantes
	 * - Errores de configuración en application.yml
	 */
	@Test
	void contextLoads() {
		// Esta prueba pasa si el contexto de Spring se carga sin errores
	}
}
