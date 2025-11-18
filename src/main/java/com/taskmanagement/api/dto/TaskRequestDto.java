package com.taskmanagement.api.dto;

import com.taskmanagement.api.model.TaskPriority;
import com.taskmanagement.api.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para las peticiones de creación/actualización de tareas.
 *
 * PATRÓN DTO - Data Transfer Object:
 * Los DTOs son objetos que transfieren datos entre capas de la aplicación,
 * especialmente entre el controlador y el servicio.
 *
 * VENTAJAS DEL PATRÓN DTO:
 *
 * 1. DESACOPLAMIENTO:
 *    - Separa la API pública de la estructura interna de datos
 *    - Puedes cambiar tu modelo de dominio sin afectar a los clientes
 *    - Los clientes de la API no dependen de tu esquema de base de datos
 *
 * 2. VALIDACIÓN:
 *    - Valida datos de entrada ANTES de llegar a la lógica de negocio
 *    - Mensajes de error personalizados y claros
 *    - Bean Validation (jakarta.validation) integrado
 *
 * 3. SEGURIDAD:
 *    - Control preciso sobre qué campos acepta la API
 *    - Evita Mass Assignment vulnerabilities
 *    - No expones campos internos sensibles
 *
 * 4. VERSIONADO:
 *    - Mantén múltiples versiones de la API con DTOs diferentes
 *    - TaskRequestDtoV1, TaskRequestDtoV2, etc.
 *    - Misma entidad, diferentes contratos de API
 *
 * 5. DOCUMENTACIÓN:
 *    - @Schema annotations para OpenAPI/Swagger
 *    - Genera documentación automática
 *    - Los clientes saben exactamente qué enviar
 *
 * DIFERENCIA DTO DE ENTRADA vs SALIDA:
 * - DTO de entrada (este): Validación estricta, solo campos necesarios para crear/actualizar
 * - DTO de salida (TaskResponseDto): Incluye campos adicionales como ID, createdAt, etc.
 *
 * ¿POR QUÉ NO UN RECORD?
 * Para DTOs de entrada usamos clases normales (no records) porque:
 * - Bean Validation funciona mejor con setters
 * - Frameworks de deserialización (Jackson) los manejan mejor
 * - Más flexible para validaciones complejas
 * - Records son mejores para DTOs de solo lectura (respuestas)
 *
 * ANOTACIONES DE VALIDACIÓN:
 * @NotBlank: Campo no puede ser null, vacío o solo espacios en blanco
 * @NotNull: Campo no puede ser null
 * @Size: Define longitud mínima y máxima del string
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "DTO para crear o actualizar una tarea (PUT)",
    example = """
        {
          "title": "Comprar ingredientes para la cena",
          "description": "Comprar tomates, cebolla, ajo y pasta",
          "status": "PENDING",
          "dueDate": "2025-11-15T18:00:00"
        }
        """
)
public class TaskRequestDto {

    /**
     * Título de la tarea
     * Obligatorio, mínimo 1 carácter, máximo 100
     */
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 1, max = 100, message = "El título debe tener entre 1 y 100 caracteres")
    @Schema(
        description = "Título de la tarea",
        example = "Comprar ingredientes para la cena",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 1,
        maxLength = 100
    )
    private String title;

    /**
     * Descripción de la tarea (opcional)
     * Máximo 1000 caracteres
     */
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    @Schema(
        description = "Descripción detallada de la tarea (opcional)",
        example = "Comprar tomates, cebolla, ajo y pasta en el supermercado",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 1000,
        nullable = true
    )
    private String description;

    /**
     * Estado de la tarea
     * Obligatorio
     */
    @NotNull(message = "El estado es obligatorio")
    @Schema(
        description = "Estado actual de la tarea",
        example = "PENDING",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"}
    )
    private TaskStatus status;

    /**
     * Prioridad de la tarea
     * Obligatorio (por defecto MEDIUM si no se especifica)
     */
    @NotNull(message = "La prioridad es obligatoria")
    @Schema(
        description = "Prioridad de la tarea (importancia/urgencia)",
        example = "MEDIUM",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"}
    )
    private TaskPriority priority;

    /**
     * Fecha límite (opcional)
     */
    @Schema(
        description = "Fecha y hora límite para completar la tarea (opcional)",
        example = "2025-11-15T18:00:00",
        type = "string",
        format = "date-time",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        nullable = true
    )
    private LocalDateTime dueDate;
}
