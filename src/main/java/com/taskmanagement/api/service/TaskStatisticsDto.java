package com.taskmanagement.api.service;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
    description = "Estadísticas agregadas sobre las tareas del sistema",
    example = """
        {
          "totalTasks": 42,
          "pendingTasks": 15,
          "inProgressTasks": 10,
          "completedTasks": 12,
          "cancelledTasks": 5
        }
        """
)
public class TaskStatisticsDto {

    /**
     * Total de tareas en el sistema
     */
    @Schema(
        description = "Número total de tareas en el sistema (suma de todas las tareas sin importar el estado)",
        example = "42",
        minimum = "0"
    )
    private Long totalTasks;

    /**
     * Número de tareas pendientes
     */
    @Schema(
        description = "Número de tareas con estado PENDING (pendientes de iniciar)",
        example = "15",
        minimum = "0"
    )
    private Long pendingTasks;

    /**
     * Número de tareas en progreso
     */
    @Schema(
        description = "Número de tareas con estado IN_PROGRESS (actualmente en ejecución)",
        example = "10",
        minimum = "0"
    )
    private Long inProgressTasks;

    /**
     * Número de tareas completadas
     */
    @Schema(
        description = "Número de tareas con estado COMPLETED (finalizadas exitosamente)",
        example = "12",
        minimum = "0"
    )
    private Long completedTasks;

    /**
     * Número de tareas canceladas
     */
    @Schema(
        description = "Número de tareas con estado CANCELLED (canceladas antes de completarse)",
        example = "5",
        minimum = "0"
    )
    private Long cancelledTasks;
}
