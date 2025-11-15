package com.taskmanagement.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enumeración que representa los posibles estados de una tarea.
 *
 * Los enums son útiles para:
 * - Mantener valores predefinidos y constantes
 * - Evitar errores de tipo (type-safety)
 * - Mejorar la legibilidad del código
 */
@Schema(
    description = "Estado actual de una tarea en el sistema",
    enumAsRef = true,
    allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"}
)
public enum TaskStatus {

    @Schema(description = "Tarea pendiente de iniciar - Estado inicial por defecto")
    PENDING,

    @Schema(description = "Tarea en progreso - La tarea está siendo ejecutada actualmente")
    IN_PROGRESS,

    @Schema(description = "Tarea completada - La tarea ha sido finalizada exitosamente")
    COMPLETED,

    @Schema(description = "Tarea cancelada - La tarea fue cancelada antes de completarse")
    CANCELLED
}
