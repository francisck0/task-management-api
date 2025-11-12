package com.taskmanagement.api.dto;

import com.taskmanagement.api.model.TaskStatus;
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
public class TaskPatchDto {

    /**
     * Título de la tarea (opcional)
     * Si se envía, debe tener entre 1 y 100 caracteres
     */
    @Size(min = 1, max = 100, message = "El título debe tener entre 1 y 100 caracteres")
    private String title;

    /**
     * Descripción de la tarea (opcional)
     * Si se envía, no puede superar los 1000 caracteres
     */
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    private String description;

    /**
     * Estado de la tarea (opcional)
     */
    private TaskStatus status;

    /**
     * Fecha límite (opcional)
     */
    private LocalDateTime dueDate;
}
