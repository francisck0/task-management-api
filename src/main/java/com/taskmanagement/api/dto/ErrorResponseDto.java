package com.taskmanagement.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuestas de error estandarizadas usando Record de Java 21.
 *
 * ¿POR QUÉ UN RECORD?
 * Java Records (introducidos en Java 14, estables en 16+) son ideales para DTOs porque:
 * - Inmutabilidad por defecto (thread-safe)
 * - Menos código boilerplate (no necesitas getters, equals, hashCode, toString)
 * - Semántica clara: representa datos puros sin comportamiento
 * - Mejor rendimiento que clases tradicionales
 * - Perfectos para DTOs de solo lectura como respuestas de error
 *
 * PATRÓN DTO (Data Transfer Object):
 * Los DTOs son objetos que transfieren datos entre capas de la aplicación,
 * especialmente entre el backend y el cliente de la API.
 *
 * VENTAJAS DEL PATRÓN DTO:
 * 1. DESACOPLAMIENTO: Separa la estructura interna (entidades JPA) de la API pública
 *    - Puedes cambiar tu modelo de datos sin romper la API
 *    - Los clientes no dependen de tu estructura de BD
 *
 * 2. SEGURIDAD: Control preciso sobre qué datos exponer
 *    - No expones campos sensibles (passwords, tokens, etc.)
 *    - Evitas exponer relaciones JPA complejas
 *
 * 3. VERSIONADO: Facilita mantener múltiples versiones de la API
 *    - TaskResponseDtoV1, TaskResponseDtoV2, etc.
 *    - Misma entidad, diferentes representaciones
 *
 * 4. OPTIMIZACIÓN: Solo transferir datos necesarios
 *    - Evita cargar relaciones lazy innecesarias
 *    - Reduce el tamaño de las respuestas JSON
 *
 * 5. VALIDACIÓN: DTOs de entrada con Bean Validation
 *    - Validar antes de llegar a la lógica de negocio
 *    - Mensajes de error personalizados
 *
 * 6. DOCUMENTACIÓN: @Schema para OpenAPI/Swagger
 *    - Documenta tu API automáticamente
 *    - Los clientes saben exactamente qué esperar
 *
 * USO:
 * Este DTO se usa en el GlobalExceptionHandler para devolver errores
 * con un formato consistente en toda la API.
 *
 * EJEMPLO DE RESPUESTA:
 * {
 *   "timestamp": "2025-11-12T15:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Tarea no encontrada con ID: 999",
 *   "path": "/api/v1/tasks/999",
 *   "errors": null
 * }
 */
@Schema(
    description = "Respuesta de error estandarizada para toda la API",
    example = """
        {
          "timestamp": "2025-11-12T15:30:00",
          "status": 404,
          "error": "Not Found",
          "message": "Tarea no encontrada con ID: 999",
          "path": "/api/v1/tasks/999"
        }
        """
)
@JsonInclude(JsonInclude.Include.NON_NULL)  // No incluir campos null en JSON
public record ErrorResponseDto(

        @Schema(
            description = "Fecha y hora exacta en que ocurrió el error",
            example = "2025-11-12T15:30:00",
            type = "string",
            format = "date-time"
        )
        LocalDateTime timestamp,

        @Schema(
            description = "Código de estado HTTP del error",
            example = "404",
            minimum = "100",
            maximum = "599"
        )
        int status,

        @Schema(
            description = "Nombre descriptivo del tipo de error HTTP",
            example = "Not Found"
        )
        String error,

        @Schema(
            description = "Mensaje descriptivo del error en español",
            example = "Tarea no encontrada con ID: 999"
        )
        String message,

        @Schema(
            description = "Ruta de la petición HTTP que generó el error",
            example = "/api/v1/tasks/999"
        )
        String path,

        @Schema(
            description = "Lista de errores de validación específicos (solo para errores 400 de validación)",
            example = """
                [
                  "title: El título es obligatorio",
                  "status: El estado es obligatorio"
                ]
                """,
            nullable = true
        )
        List<String> errors
) {

    /**
     * Constructor compacto para validación automática.
     *
     * Los Records pueden tener un "compact constructor" que valida los parámetros.
     * Se ejecuta antes de asignar los valores a los campos.
     */
    public ErrorResponseDto {
        // Validaciones (opcional)
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (status < 100 || status > 599) {
            throw new IllegalArgumentException("HTTP status code must be between 100 and 599");
        }
        if (message == null || message.isBlank()) {
            message = "An error occurred";
        }
    }

    /**
     * Constructor sin la lista de errores (para errores simples).
     *
     * Los Records permiten múltiples constructores,
     * pero deben delegar al constructor canónico.
     */
    public ErrorResponseDto(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
        this(timestamp, status, error, message, path, null);
    }

    /**
     * Factory method para crear ErrorResponseDto de forma fluida.
     *
     * Builder pattern simplificado para Records.
     */
    public static ErrorResponseDto of(
            int status,
            String error,
            String message,
            String path
    ) {
        return new ErrorResponseDto(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                null
        );
    }

    /**
     * Factory method con errores de validación.
     */
    public static ErrorResponseDto withValidationErrors(
            int status,
            String error,
            String message,
            String path,
            List<String> validationErrors
    ) {
        return new ErrorResponseDto(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                validationErrors
        );
    }
}
