package com.taskmanagement.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación automática de la API.
 *
 * OpenAPI (anteriormente conocido como Swagger) es una especificación estándar
 * para documentar APIs REST de forma que sea legible tanto por humanos como por máquinas.
 *
 * BENEFICIOS DE OPENAPI:
 * 1. Documentación interactiva (Swagger UI)
 * 2. Generación automática de clientes API
 * 3. Validación de contratos de API
 * 4. Testing interactivo desde el navegador
 * 5. Estándar de la industria
 *
 * SWAGGER UI:
 * Interfaz web interactiva que permite:
 * - Ver todos los endpoints disponibles
 * - Ver schemas de request/response
 * - Probar endpoints directamente desde el navegador
 * - Ver ejemplos de uso
 *
 * ACCESO:
 * Dado que esta API usa context-path: /api/v1, las URLs son:
 * - Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/api/v1/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/api/v1/v3/api-docs.yaml
 *
 * ANOTACIONES OPENAPI DISPONIBLES:
 * - @Tag: Agrupa endpoints (a nivel de clase)
 * - @Operation: Describe una operación (a nivel de método)
 * - @ApiResponse(s): Define respuestas posibles
 * - @Parameter: Documenta parámetros
 * - @Schema: Define estructura de datos (DTOs)
 * - @SecurityRequirement: Define seguridad requerida
 *
 * SPRINGDOC VS SPRINGFOX:
 * - SpringDoc: Moderno, soporta OpenAPI 3.0, mejor integración Spring Boot 3
 * - SpringFox: Antiguo, solo OpenAPI 2.0 (Swagger 2), no recomendado
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configuración principal de OpenAPI.
     *
     * Define metadatos de la API como título, descripción, versión, etc.
     * Esta información se muestra en la página principal de Swagger UI.
     *
     * @return configuración OpenAPI personalizada
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        localServer(),
                        productionServer()
                ));
    }

    /**
     * Información general de la API.
     *
     * Incluye:
     * - Título y descripción
     * - Versión
     * - Contacto del desarrollador
     * - Licencia
     * - Términos de servicio
     *
     * Esta información aparece en el header de Swagger UI.
     */
    private Info apiInfo() {
        return new Info()
                .title("Task Management API")
                .description("""
                        # API REST para gestión de tareas

                        Esta API permite gestionar tareas con operaciones CRUD completas.

                        ## Características
                        - ✅ Crear, leer, actualizar y eliminar tareas
                        - ✅ Búsqueda por estado y título
                        - ✅ Actualización parcial (PATCH)
                        - ✅ Auditoría automática (createdAt, updatedAt)
                        - ✅ Validación de datos con Bean Validation
                        - ✅ Manejo de errores estandarizado
                        - ✅ Documentación OpenAPI completa

                        ## Estados de tarea
                        - `PENDING` - Pendiente de iniciar
                        - `IN_PROGRESS` - En progreso
                        - `COMPLETED` - Completada
                        - `CANCELLED` - Cancelada

                        ## Códigos de respuesta HTTP
                        - `200 OK` - Operación exitosa
                        - `201 Created` - Recurso creado exitosamente
                        - `204 No Content` - Operación exitosa sin contenido
                        - `400 Bad Request` - Error de validación
                        - `404 Not Found` - Recurso no encontrado
                        - `500 Internal Server Error` - Error del servidor

                        ## Formato de fechas
                        Todas las fechas usan formato ISO 8601: `yyyy-MM-dd'T'HH:mm:ss`

                        Ejemplo: `2025-11-15T18:00:00`
                        """)
                .version("1.0.0")
                .contact(apiContact())
                .license(apiLicense())
                .termsOfService("https://taskmanagement.com/terms");
    }

    /**
     * Información de contacto del desarrollador/equipo.
     *
     * Aparece en Swagger UI para que los usuarios de la API
     * puedan contactar al equipo de desarrollo.
     */
    private Contact apiContact() {
        return new Contact()
                .name("Task Management Team")
                .email("support@taskmanagement.com")
                .url("https://taskmanagement.com");
    }

    /**
     * Información de licencia de la API.
     *
     * Define bajo qué licencia se distribuye la API.
     */
    private License apiLicense() {
        return new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
    }

    /**
     * Configuración del servidor local (desarrollo).
     *
     * Define la URL base del servidor local.
     * Swagger UI usará esta URL para hacer las peticiones de prueba.
     */
    private Server localServer() {
        return new Server()
                .url("http://localhost:8080/api/v1")
                .description("Servidor de desarrollo local");
    }

    /**
     * Configuración del servidor de producción.
     *
     * Define la URL del servidor de producción.
     * Los usuarios pueden cambiar entre servidores en Swagger UI.
     */
    private Server productionServer() {
        return new Server()
                .url("https://api.taskmanagement.com/v1")
                .description("Servidor de producción");
    }

    // =========================================================================
    // CONFIGURACIÓN ADICIONAL (Opcional)
    // =========================================================================

    /**
     * Si en el futuro implementas autenticación (JWT, OAuth, etc.),
     * puedes agregar SecuritySchemes aquí:
     *
     * @Bean
     * public OpenAPI secureOpenAPI() {
     *     return new OpenAPI()
     *         .components(new Components()
     *             .addSecuritySchemes("bearer-jwt",
     *                 new SecurityScheme()
     *                     .type(SecurityScheme.Type.HTTP)
     *                     .scheme("bearer")
     *                     .bearerFormat("JWT")
     *                     .description("JWT token obtenido del endpoint /auth/login")
     *             )
     *         )
     *         .security(List.of(
     *             new SecurityRequirement().addList("bearer-jwt")
     *         ));
     * }
     *
     * Y en los controllers:
     * @SecurityRequirement(name = "bearer-jwt")
     * @GetMapping("/protected-endpoint")
     * public ResponseEntity<?> protectedEndpoint() { ... }
     */
}
