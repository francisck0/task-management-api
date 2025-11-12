package com.taskmanagement.api.model;

/**
 * Enumeración que representa los posibles estados de una tarea.
 *
 * Los enums son útiles para:
 * - Mantener valores predefinidos y constantes
 * - Evitar errores de tipo (type-safety)
 * - Mejorar la legibilidad del código
 */
public enum TaskStatus {
    PENDING,      // Tarea pendiente de iniciar
    IN_PROGRESS,  // Tarea en progreso
    COMPLETED,    // Tarea completada
    CANCELLED     // Tarea cancelada
}
