package com.taskmanagement.api.controller;

import com.taskmanagement.api.constant.ApiVersion;
import com.taskmanagement.api.service.TaskService;
import com.taskmanagement.api.service.TaskStatisticsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para operaciones de ESTADÍSTICAS de tareas.
 *
 * PRINCIPIO: Single Responsibility Principle (SRP)
 * - Este controlador se encarga ÚNICAMENTE de reportes y estadísticas
 * - No contiene lógica CRUD ni de búsqueda
 * - Especializado en agregaciones y resúmenes de datos
 *
 * SEPARACIÓN DE RESPONSABILIDADES:
 * - TaskQueryController: Operaciones GET (consultas individuales)
 * - TaskCommandController: Operaciones POST, PUT, PATCH, DELETE
 * - TaskStatisticsController: Estadísticas y reportes (este controlador)
 *
 * VENTAJAS DEL DISEÑO:
 * - Código más organizado y mantenible
 * - Facilita la implementación de caché específico para estadísticas
 * - Permite optimizar queries de agregación independientemente
 * - Facilita la adición de nuevos reportes sin afectar otros controladores
 * - Cumple con SOLID principles
 * - Mejor escalabilidad (se puede separar en servicio de analytics)
 *
 * CASOS DE USO:
 * - Dashboards y visualizaciones
 * - Reportes ejecutivos
 * - Métricas de productividad
 * - KPIs (Key Performance Indicators)
 *
 * FUTURAS EXTENSIONES POSIBLES:
 * - Estadísticas por rango de fechas
 * - Estadísticas por usuario
 * - Tendencias y gráficas históricas
 * - Reportes de tareas vencidas
 * - Tiempo promedio de completación
 * - Distribución de tareas por prioridad
 *
 * ENDPOINTS INCLUIDOS:
 * - GET /tasks/statistics → Obtener estadísticas generales
 *
 * @see TaskQueryController para consultas de tareas
 * @see TaskCommandController para operaciones de escritura
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Task Statistics",
    description = """
        API de estadísticas y reportes de tareas. Proporciona métricas agregadas,
        conteos por estado, y resúmenes para dashboards y análisis.
        """
)
public class TaskStatisticsController {

    private final TaskService taskService;

    /**
     * Obtiene estadísticas sobre las tareas.
     *
     * Endpoint: GET /tasks/statistics
     *
     * OPTIMIZACIÓN:
     * - Este endpoint es candidato ideal para implementar caché
     * - Las estadísticas pueden cachearse por 1-5 minutos
     * - Reduce carga en la base de datos
     *
     * EJEMPLO DE CACHÉ (para implementar en el futuro):
     * @Cacheable(value = "taskStatistics", key = "'all'")
     * @CacheEvict(value = "taskStatistics", allEntries = true) en createTask, updateTask, deleteTask
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Obtener estadísticas de tareas",
        description = """
            Retorna un resumen estadístico con el conteo de tareas por estado.

            **Datos incluidos:**
            - **totalTasks**: Total de tareas en el sistema
            - **pendingTasks**: Tareas pendientes de iniciar (PENDING)
            - **inProgressTasks**: Tareas en progreso (IN_PROGRESS)
            - **completedTasks**: Tareas completadas (COMPLETED)
            - **cancelledTasks**: Tareas canceladas (CANCELLED)

            **Casos de uso:**
            - Dashboards de gestión de proyectos
            - Reportes de productividad
            - Visualizaciones de estado general
            - KPIs de desempeño del equipo

            **Rendimiento:**
            - Este endpoint ejecuta múltiples queries COUNT
            - Tiempo de respuesta típico: < 100ms
            - Recomendado: Implementar caché para reducir carga en BD

            **Futuras extensiones:**
            - Filtrado por rango de fechas
            - Estadísticas por usuario
            - Tendencias históricas
            - Tareas vencidas y próximas a vencer
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Estadísticas obtenidas exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskStatisticsDto.class),
                examples = {
                    @ExampleObject(
                        name = "Estadísticas con tareas",
                        description = "Ejemplo de estadísticas con tareas distribuidas en varios estados",
                        value = """
                            {
                              "totalTasks": 42,
                              "pendingTasks": 15,
                              "inProgressTasks": 10,
                              "completedTasks": 12,
                              "cancelledTasks": 5
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Sistema sin tareas",
                        description = "Respuesta cuando no hay tareas en el sistema",
                        value = """
                            {
                              "totalTasks": 0,
                              "pendingTasks": 0,
                              "inProgressTasks": 0,
                              "completedTasks": 0,
                              "cancelledTasks": 0
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Alta productividad",
                        description = "Ejemplo con alto porcentaje de tareas completadas",
                        value = """
                            {
                              "totalTasks": 100,
                              "pendingTasks": 10,
                              "inProgressTasks": 15,
                              "completedTasks": 70,
                              "cancelledTasks": 5
                            }
                            """
                    )
                }
            )
        )
    })
    public ResponseEntity<TaskStatisticsDto> getStatistics() {
        log.info("Petición recibida para obtener estadísticas de tareas");

        TaskStatisticsDto stats = taskService.getStatistics();

        log.debug("Estadísticas calculadas: Total={}, Pending={}, InProgress={}, Completed={}, Cancelled={}",
                stats.getTotalTasks(),
                stats.getPendingTasks(),
                stats.getInProgressTasks(),
                stats.getCompletedTasks(),
                stats.getCancelledTasks());

        return ResponseEntity.ok(stats);
    }

    // =========================================================================
    // ENDPOINTS FUTUROS (Comentados para referencia)
    // =========================================================================

    /*
    @GetMapping("/statistics/by-date-range")
    @Operation(summary = "Estadísticas por rango de fechas")
    public ResponseEntity<TaskStatisticsDto> getStatisticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Estadísticas solicitadas para rango: {} - {}", startDate, endDate);

        TaskStatisticsDto stats = taskService.getStatisticsByDateRange(startDate, endDate);

        return ResponseEntity.ok(stats);
    }
    */

    /*
    @GetMapping("/statistics/by-user/{userId}")
    @Operation(summary = "Estadísticas por usuario")
    public ResponseEntity<TaskStatisticsDto> getStatisticsByUser(
            @PathVariable Long userId) {

        log.info("Estadísticas solicitadas para usuario ID: {}", userId);

        TaskStatisticsDto stats = taskService.getStatisticsByUser(userId);

        return ResponseEntity.ok(stats);
    }
    */

    /*
    @GetMapping("/statistics/overdue")
    @Operation(summary = "Estadísticas de tareas vencidas")
    public ResponseEntity<OverdueStatisticsDto> getOverdueStatistics() {

        log.info("Estadísticas de tareas vencidas solicitadas");

        OverdueStatisticsDto stats = taskService.getOverdueStatistics();

        return ResponseEntity.ok(stats);
    }
    */

    /*
    @GetMapping("/statistics/trends")
    @Operation(summary = "Tendencias de tareas (últimos 30 días)")
    public ResponseEntity<List<DailyTaskTrendDto>> getTrends() {

        log.info("Tendencias de tareas solicitadas");

        List<DailyTaskTrendDto> trends = taskService.getTaskTrends();

        return ResponseEntity.ok(trends);
    }
    */
}
