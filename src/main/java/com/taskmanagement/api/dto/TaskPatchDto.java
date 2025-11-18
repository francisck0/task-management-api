package com.taskmanagement.api.dto;

import com.taskmanagement.api.model.TaskPriority;
import com.taskmanagement.api.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object) para actualizaciones parciales de tareas (PATCH).
 *
 * A diferencia de TaskRequestDto, en este DTO todos los campos son opcionales.
 * Solo se actualizarán los campos que sean enviados (no null).
 *
 * Diferencia entre PUT y PATCH:
 * - PUT: Reemplaza completamente el recurso (todos los campos son obligatorios)
 * - PATCH: Actualiza parcialmente el recurso (solo los campos enviados)
 *
 * Ejemplo de uso PATCH:
 * {
 *   "status": "COMPLETED"
 * }
 * Solo actualiza el estado, los demás campos permanecen sin cambios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "DTO para actualizar parcialmente una tarea (PATCH). Todos los campos son opcionales, solo se actualizan los campos enviados.",
    example = """
        {
          "status": "COMPLETED"
        }
        """
)
public class TaskPatchDto {

    /**
     * Título de la tarea (opcional)
     * Si se envía, debe tener entre 1 y 100 caracteres
     */
    @Size(min = 1, max = 100, message = "El título debe tener entre 1 y 100 caracteres")
    @Schema(
        description = "Nuevo título de la tarea (opcional)",
        example = "Tarea actualizada",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        minLength = 1,
        maxLength = 100,
        nullable = true
    )
    private String title;

    /**
     * Descripción de la tarea (opcional)
     * Si se envía, no puede superar los 1000 caracteres
     */
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    @Schema(
        description = "Nueva descripción de la tarea (opcional)",
        example = "Descripción actualizada con más detalles",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 1000,
        nullable = true
    )
    private String description;

    /**
     * Estado de la tarea (opcional)
     */
    @Schema(
        description = "Nuevo estado de la tarea (opcional)",
        example = "IN_PROGRESS",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"},
        nullable = true
    )
    private TaskStatus status;

    /**
     * Prioridad de la tarea (opcional)
     */
    @Schema(
        description = "Nueva prioridad de la tarea (opcional)",
        example = "HIGH",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"},
        nullable = true
    )
    private TaskPriority priority;

    /**
     * Fecha límite (opcional)
     */
    @Schema(
        description = "Nueva fecha y hora límite para completar la tarea (opcional)",
        example = "2025-12-01T12:00:00",
        type = "string",
        format = "date-time",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        nullable = true
    )
    private LocalDateTime dueDate;
}
