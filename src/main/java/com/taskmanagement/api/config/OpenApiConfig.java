package com.taskmanagement.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
     * SEGURIDAD JWT:
     * - Configura el esquema de seguridad Bearer JWT
     * - Agrega el botón "Authorize" en Swagger UI
     * - Permite probar endpoints protegidos ingresando el token JWT
     *
     * CÓMO USAR EN SWAGGER UI:
     * 1. Ir a /api/v1/swagger-ui.html
     * 2. Hacer clic en el botón "Authorize" (candado verde)
     * 3. Ingresar el token JWT (sin "Bearer ")
     * 4. Hacer clic en "Authorize"
     * 5. Ahora puedes probar endpoints protegidos
     *
     * @return configuración OpenAPI personalizada
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // Nombre del esquema de seguridad (debe coincidir con @SecurityRequirement en controllers)
        final String securitySchemeName = "bearer-jwt";

        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        localServer(),
                        productionServer()
                ))
                // Configurar el esquema de seguridad JWT
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("""
                                                # Autenticación JWT

                                                Para acceder a los endpoints protegidos, necesitas un token JWT.

                                                ## Cómo obtener el token:

                                                1. **Registrar un usuario** (si no tienes cuenta):
                                                   ```
                                                   POST /auth/register
                                                   {
                                                     "username": "usuario@ejemplo.com",
                                                     "password": "password123"
                                                   }
                                                   ```

                                                2. **Iniciar sesión**:
                                                   ```
                                                   POST /auth/login
                                                   {
                                                     "username": "usuario@ejemplo.com",
                                                     "password": "password123"
                                                   }
                                                   ```

                                                3. **Copiar el token** del campo `token` en la respuesta

                                                4. **Pegar el token aquí** (sin el prefijo "Bearer ")

                                                ## Ejemplo de token:
                                                ```
                                                eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
                                                ```

                                                ## Expiración:
                                                Los tokens expiran después de 24 horas por defecto.
                                                Si recibes error 401, necesitas obtener un nuevo token.
                                                """)
                        )
                )
                // Aplicar seguridad JWT globalmente a todos los endpoints
                // (Los endpoints públicos como /auth/** están excluidos en SecurityConfig)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
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

                        ## 🔐 Autenticación

                        **La mayoría de los endpoints requieren autenticación JWT.**

                        1. **Regístrate** en `POST /auth/register`
                        2. **Inicia sesión** en `POST /auth/login` para obtener tu token
                        3. Usa el botón **"Authorize"** 🔓 arriba para ingresar tu token
                        4. ¡Ahora puedes probar todos los endpoints protegidos!

                        ## Características
                        - ✅ Autenticación JWT con Spring Security
                        - ✅ Crear, leer, actualizar y eliminar tareas
                        - ✅ Búsqueda por estado y título
                        - ✅ Paginación en todos los endpoints de consulta
                        - ✅ Actualización parcial (PATCH)
                        - ✅ Soft delete con papelera de reciclaje
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
                        - `401 Unauthorized` - No autenticado o token inválido
                        - `403 Forbidden` - Sin permisos para el recurso
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
     *
     * NOTA: La URL solo incluye /api (context path)
     * La versión (/v1) está en los controladores usando ApiVersion.V1
     */
    private Server localServer() {
        return new Server()
                .url("http://localhost:8080/api")
                .description("Servidor de desarrollo local");
    }

    /**
     * Configuración del servidor de producción.
     *
     * Define la URL del servidor de producción.
     * Los usuarios pueden cambiar entre servidores en Swagger UI.
     *
     * NOTA: La URL solo incluye el dominio base
     * La versión (/v1) está en los controladores usando ApiVersion.V1
     */
    private Server productionServer() {
        return new Server()
                .url("https://api.taskmanagement.com")
                .description("Servidor de producción");
    }
}
