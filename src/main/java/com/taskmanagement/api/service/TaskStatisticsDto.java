package com.taskmanagement.api.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para devolver estadísticas sobre las tareas.
 *
 * Este DTO agrupa información estadística útil
 * sin necesidad de hacer múltiples llamadas a la API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatisticsDto {

    /**
     * Total de tareas en el sistema
     */
    private Long totalTasks;

    /**
     * Número de tareas pendientes
     */
    private Long pendingTasks;

    /**
     * Número de tareas en progreso
     */
    private Long inProgressTasks;

    /**
     * Número de tareas completadas
     */
    private Long completedTasks;

    /**
     * Número de tareas canceladas
     */
    private Long cancelledTasks;
}
