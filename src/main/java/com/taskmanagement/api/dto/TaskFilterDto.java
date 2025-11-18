package com.taskmanagement.api.dto;

import com.taskmanagement.api.model.TaskPriority;
import com.taskmanagement.api.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para filtros avanzados de tareas.
 *
 * PROPÓSITO:
 * Este DTO encapsula todos los criterios de filtrado disponibles para
 * búsquedas avanzadas de tareas.
 *
 * PATRÓN: Query Object / Filter DTO
 * En lugar de tener múltiples parámetros en los métodos de servicio/controller,
 * usamos un objeto que encapsula todos los criterios de filtrado.
 *
 * VENTAJAS:
 * - Código más limpio y mantenible
 * - Fácil agregar nuevos filtros sin cambiar signatures de métodos
 * - Validación centralizada de filtros
 * - Reutilizable en múltiples endpoints
 * - Documentación automática con Swagger
 *
 * TODOS LOS CAMPOS SON OPCIONALES:
 * - Si un campo es null, no se aplica ese filtro
 * - Se pueden combinar múltiples filtros (AND lógico)
 * - Ejemplo: status=PENDING AND priority=HIGH AND createdAfter=...
 *
 * FILTROS DISPONIBLES:
 *
 * 1. FILTROS POR ESTADO Y PRIORIDAD:
 *    - status: Filtrar por estado (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
 *    - priority: Filtrar por prioridad (LOW, MEDIUM, HIGH, CRITICAL)
 *
 * 2. FILTROS POR FECHAS DE CREACIÓN:
 *    - createdAfter: Tareas creadas después de esta fecha
 *    - createdBefore: Tareas creadas antes de esta fecha
 *    - Combinación: Rango de fechas de creación
 *
 * 3. FILTROS POR FECHA DE VENCIMIENTO:
 *    - dueDateAfter: Tareas que vencen después de esta fecha
 *    - dueDateBefore: Tareas que vencen antes de esta fecha
 *    - Casos especiales:
 *      * Tareas vencidas: dueDateBefore = LocalDateTime.now()
 *      * Tareas que vencen hoy: dueDateBefore = fin del día
 *      * Tareas que vencen esta semana: dueDateBefore = fin de semana
 *
 * 4. FILTRO POR TEXTO:
 *    - search: Búsqueda en título y descripción (case-insensitive)
 *
 * CASOS DE USO COMUNES:
 *
 * Tareas pendientes de alta prioridad:
 * ```java
 * TaskFilterDto filter = TaskFilterDto.builder()
 *     .status(TaskStatus.PENDING)
 *     .priority(TaskPriority.HIGH)
 *     .build();
 * ```
 *
 * Tareas creadas esta semana:
 * ```java
 * TaskFilterDto filter = TaskFilterDto.builder()
 *     .createdAfter(LocalDateTime.now().minusWeeks(1))
 *     .build();
 * ```
 *
 * Tareas vencidas:
 * ```java
 * TaskFilterDto filter = TaskFilterDto.builder()
 *     .dueDateBefore(LocalDateTime.now())
 *     .status(TaskStatus.PENDING) // Solo pendientes vencidas
 *     .build();
 * ```
 *
 * Tareas críticas que vencen pronto:
 * ```java
 * TaskFilterDto filter = TaskFilterDto.builder()
 *     .priority(TaskPriority.CRITICAL)
 *     .dueDateBefore(LocalDateTime.now().plusDays(3))
 *     .build();
 * ```
 *
 * Búsqueda de texto:
 * ```java
 * TaskFilterDto filter = TaskFilterDto.builder()
 *     .search("documentación")
 *     .build();
 * ```
 *
 * INTEGRACIÓN CON SPRING DATA JPA:
 * Este DTO se usa con Specification API o Query Methods para
 * construir queries dinámicas basadas en los filtros aplicados.
 *
 * EJEMPLO DE ENDPOINT:
 * ```
 * GET /api/v1/tasks/filter?status=PENDING&priority=HIGH&createdAfter=2025-11-01T00:00:00
 * ```
 *
 * @see com.taskmanagement.api.repository.TaskRepository
 * @see com.taskmanagement.api.service.TaskService
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    description = "Criterios de filtrado avanzado para búsqueda de tareas. Todos los campos son opcionales y se combinan con AND lógico.",
    example = """
        {
          "status": "PENDING",
          "priority": "HIGH",
          "createdAfter": "2025-11-01T00:00:00",
          "dueDateBefore": "2025-11-20T23:59:59",
          "search": "documentación"
        }
        """
)
public class TaskFilterDto {

    /**
     * Filtrar por estado de la tarea
     */
    @Schema(
        description = "Filtrar tareas por estado",
        example = "PENDING",
        allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"},
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        nullable = true
    )
    private TaskStatus status;

    /**
     * Filtrar por prioridad de la tarea
     */
    @Schema(
        description = "Filtrar tareas por prioridad",
        example = "HIGH",
        allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"},
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        nullable = true
    )
    private TaskPriority priority;

    /**
     * Filtrar tareas creadas después de esta fecha
     */
    @Schema(
        description = "Obtener tareas creadas después de esta fecha/hora",
        example = "2025-11-01T00:00:00",
        type = "string",
        format = "date-time",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        nullable = true
    )
    private LocalDateTime createdAfter;

    /**
     * Filtrar tareas creadas antes de esta fecha
     */
    @Schema(
        description = "Obtener tareas creadas antes de esta fecha/hora",
        example = "2025-11-15T23:59:59",
        type = "string",
        format = "date-time",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        nullable = true
    )
    private LocalDateTime createdBefore;

    /**
     * Filtrar tareas que vencen después de esta fecha
     */
    @Schema(
        description = "Obtener tareas con fecha de vencimiento después de esta fecha/hora",
        example = "2025-11-20T00:00:00",
        type = "string",
        format = "date-time",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        nullable = true
    )
    private LocalDateTime dueDateAfter;

    /**
     * Filtrar tareas que vencen antes de esta fecha
     *
     * USO ESPECIAL:
     * - Para obtener tareas vencidas: dueDateBefore = LocalDateTime.now()
     * - Para obtener tareas que vencen hoy: dueDateBefore = fin del día
     */
    @Schema(
        description = "Obtener tareas con fecha de vencimiento antes de esta fecha/hora. Útil para encontrar tareas vencidas.",
        example = "2025-11-30T23:59:59",
        type = "string",
        format = "date-time",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        nullable = true
    )
    private LocalDateTime dueDateBefore;

    /**
     * Búsqueda de texto en título y descripción
     *
     * Realiza búsqueda case-insensitive en los campos:
     * - title
     * - description
     */
    @Schema(
        description = "Buscar texto en título y descripción de las tareas (case-insensitive)",
        example = "documentación",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        nullable = true
    )
    private String search;

    // =========================================================================
    // MÉTODOS HELPER
    // =========================================================================

    /**
     * Verifica si hay algún filtro aplicado.
     *
     * ÚTIL PARA:
     * - Validar que el usuario aplicó al menos un filtro
     * - Optimización: evitar queries complejas si no hay filtros
     * - Logging y debugging
     *
     * @return true si al menos un filtro está presente
     */
    public boolean hasAnyFilter() {
        return status != null
                || priority != null
                || createdAfter != null
                || createdBefore != null
                || dueDateAfter != null
                || dueDateBefore != null
                || (search != null && !search.trim().isEmpty());
    }

    /**
     * Verifica si hay filtros de fecha de creación.
     *
     * @return true si hay filtros de createdAfter o createdBefore
     */
    public boolean hasCreatedDateFilter() {
        return createdAfter != null || createdBefore != null;
    }

    /**
     * Verifica si hay filtros de fecha de vencimiento.
     *
     * @return true si hay filtros de dueDateAfter o dueDateBefore
     */
    public boolean hasDueDateFilter() {
        return dueDateAfter != null || dueDateBefore != null;
    }

    /**
     * Verifica si hay filtro de búsqueda de texto.
     *
     * @return true si hay texto de búsqueda
     */
    public boolean hasSearchFilter() {
        return search != null && !search.trim().isEmpty();
    }
}
