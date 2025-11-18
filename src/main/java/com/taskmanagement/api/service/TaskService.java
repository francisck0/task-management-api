package com.taskmanagement.api.service;

import com.taskmanagement.api.dto.TaskFilterDto;
import com.taskmanagement.api.dto.TaskPatchDto;
import com.taskmanagement.api.dto.TaskRequestDto;
import com.taskmanagement.api.dto.TaskResponseDto;
import com.taskmanagement.api.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz que define el contrato del servicio de tareas.
 *
 * Capa SERVICE: Contiene la lógica de negocio de la aplicación.
 *
 * Beneficios de usar interfaces:
 * - Desacoplamiento: Permite cambiar la implementación sin afectar otras capas
 * - Testabilidad: Facilita la creación de mocks para pruebas unitarias
 * - Contratos claros: Define explícitamente qué operaciones están disponibles
 * - Múltiples implementaciones: Permite tener diferentes estrategias de negocio
 */
public interface TaskService {

    /**
     * Crea una nueva tarea.
     *
     * @param taskRequestDto datos de la tarea a crear
     * @return la tarea creada con su ID asignado
     */
    TaskResponseDto createTask(TaskRequestDto taskRequestDto);

    /**
     * Busca una tarea por su ID.
     *
     * @param id identificador de la tarea
     * @return la tarea encontrada
     */
    TaskResponseDto getTaskById(Long id);

    /**
     * Actualiza una tarea existente (actualización completa).
     *
     * @param id identificador de la tarea a actualizar
     * @param taskRequestDto nuevos datos de la tarea
     * @return la tarea actualizada
     */
    TaskResponseDto updateTask(Long id, TaskRequestDto taskRequestDto);

    /**
     * Actualiza parcialmente una tarea existente (solo los campos enviados).
     *
     * @param id identificador de la tarea a actualizar
     * @param taskPatchDto campos a actualizar (solo los no null)
     * @return la tarea actualizada
     */
    TaskResponseDto patchTask(Long id, TaskPatchDto taskPatchDto);

    /**
     * Elimina LÓGICAMENTE una tarea por su ID (soft delete).
     *
     * SOFT DELETE:
     * - La tarea NO se elimina físicamente
     * - Se marca con deletedAt = CURRENT_TIMESTAMP
     * - No aparece en consultas normales
     * - Se puede restaurar con restoreTask()
     *
     * @param id identificador de la tarea a eliminar
     */
    void deleteTask(Long id);

    /**
     * Obtiene estadísticas sobre las tareas.
     *
     * @return información estadística (por ejemplo, conteo por estado)
     */
    TaskStatisticsDto getStatistics();

    // =========================================================================
    // MÉTODOS CON PAGINACIÓN
    // =========================================================================

    /**
     * Obtiene todas las tareas con paginación y ordenamiento.
     *
     * VENTAJAS DE PAGINACIÓN:
     * - Mejor rendimiento: No carga todas las tareas en memoria
     * - Escalabilidad: Funciona bien con 10,000+ tareas
     * - UX mejorada: Respuestas más rápidas al cliente
     *
     * Ejemplo de uso:
     * <pre>
     * // Página 0 (primera), 20 elementos, ordenado por fecha de creación descendente
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
     * Page<TaskResponseDto> tasks = taskService.getAllTasks(pageable);
     * </pre>
     *
     * La respuesta Page<> incluye:
     * - content: Lista de tareas de la página actual
     * - totalElements: Total de tareas en BD
     * - totalPages: Total de páginas
     * - number: Número de página actual
     * - size: Tamaño de página
     * - first/last: Indicadores de primera/última página
     *
     * @param pageable configuración de paginación (page, size, sort)
     * @return página de tareas
     */
    Page<TaskResponseDto> getAllTasks(Pageable pageable);

    /**
     * Busca tareas por estado con paginación.
     *
     * @param status estado de las tareas a buscar
     * @param pageable configuración de paginación
     * @return página de tareas con el estado especificado
     */
    Page<TaskResponseDto> getTasksByStatus(TaskStatus status, Pageable pageable);

    /**
     * Busca tareas por título con paginación.
     *
     * @param title texto a buscar en el título
     * @param pageable configuración de paginación
     * @return página de tareas que coinciden con la búsqueda
     */
    Page<TaskResponseDto> searchTasksByTitle(String title, Pageable pageable);

    /**
     * Filtra tareas usando criterios avanzados con paginación.
     *
     * FILTROS AVANZADOS:
     * Este método permite combinar múltiples criterios de filtrado:
     * - Estado (status)
     * - Prioridad (priority)
     * - Rangos de fechas de creación (createdAfter, createdBefore)
     * - Rangos de fechas de vencimiento (dueDateAfter, dueDateBefore)
     * - Búsqueda de texto en título y descripción (search)
     *
     * VENTAJAS:
     * - Queries dinámicas: Solo se aplican filtros proporcionados (no-null)
     * - Combinación flexible: Todos los filtros se combinan con AND lógico
     * - Type-safe: Usa Specification API de JPA
     * - Performance: Solo include predicados necesarios en SQL
     * - Paginación: Soporta millones de registros sin problemas
     *
     * EJEMPLOS DE USO:
     *
     * 1. Tareas pendientes de alta prioridad:
     * ```java
     * TaskFilterDto filter = TaskFilterDto.builder()
     *     .status(TaskStatus.PENDING)
     *     .priority(TaskPriority.HIGH)
     *     .build();
     * Page<TaskResponseDto> tasks = taskService.filterTasks(filter, pageable);
     * ```
     *
     * 2. Tareas vencidas (overdue):
     * ```java
     * TaskFilterDto filter = TaskFilterDto.builder()
     *     .dueDateBefore(LocalDateTime.now())
     *     .status(TaskStatus.PENDING)
     *     .build();
     * ```
     *
     * 3. Tareas creadas esta semana con búsqueda de texto:
     * ```java
     * TaskFilterDto filter = TaskFilterDto.builder()
     *     .createdAfter(LocalDateTime.now().minusWeeks(1))
     *     .search("documentación")
     *     .build();
     * ```
     *
     * SQL GENERADO (ejemplo):
     * ```sql
     * SELECT t.* FROM tasks t
     * WHERE t.status = 'PENDING'
     *   AND t.priority = 'HIGH'
     *   AND t.created_at >= '2025-11-01 00:00:00'
     *   AND t.due_date <= '2025-11-30 23:59:59'
     *   AND (LOWER(t.title) LIKE '%search%'
     *        OR LOWER(t.description) LIKE '%search%')
     *   AND t.deleted_at IS NULL
     * ORDER BY t.created_at DESC
     * LIMIT 20 OFFSET 0
     * ```
     *
     * @param filters criterios de filtrado (todos opcionales)
     * @param pageable configuración de paginación y ordenamiento
     * @return página de tareas que cumplen los criterios
     *
     * @see TaskFilterDto
     * @see com.taskmanagement.api.specification.TaskSpecification
     */
    Page<TaskResponseDto> filterTasks(TaskFilterDto filters, Pageable pageable);

    // =========================================================================
    // SOFT DELETE - MÉTODOS DE PAPELERA (TRASH)
    // =========================================================================

    /**
     * Obtiene todas las tareas eliminadas (papelera) con paginación.
     *
     * SOFT DELETE:
     * - Solo retorna tareas con deletedAt NOT NULL
     * - Ordenadas por fecha de eliminación (más recientes primero)
     * - Útil para recuperar tareas eliminadas accidentalmente
     *
     * @param pageable configuración de paginación
     * @return página de tareas eliminadas
     */
    Page<TaskResponseDto> getDeletedTasks(Pageable pageable);

    /**
     * Restaura una tarea eliminada (marca deletedAt como null).
     *
     * PROCESO:
     * 1. Busca la tarea en papelera (deletedAt NOT NULL)
     * 2. Verifica ownership (seguridad)
     * 3. Establece deletedAt = NULL
     * 4. La tarea vuelve a aparecer en consultas normales
     *
     * @param id identificador de la tarea a restaurar
     * @return la tarea restaurada
     * @throws ResourceNotFoundException si la tarea no está en papelera
     */
    TaskResponseDto restoreTask(Long id);

    /**
     * Elimina permanentemente tareas eliminadas hace más de X días (purge).
     *
     * PURGE - ELIMINACIÓN FÍSICA:
     * - Busca tareas con deletedAt < (ahora - retentionDays)
     * - Las elimina PERMANENTEMENTE de la BD
     * - Operación IRREVERSIBLE
     * - Útil para cumplimiento GDPR y optimización de espacio
     *
     * SEGURIDAD:
     * - Solo administradores deberían poder ejecutar esto
     * - Se registra en logs de auditoría
     *
     * @param retentionDays días de retención (ej: 90)
     * @return número de tareas eliminadas permanentemente
     */
    int purgeOldDeletedTasks(int retentionDays);
}
