package com.taskmanagement.api.service.impl;

import com.taskmanagement.api.dto.TaskFilterDto;
import com.taskmanagement.api.dto.TaskPatchDto;
import com.taskmanagement.api.dto.TaskRequestDto;
import com.taskmanagement.api.dto.TaskResponseDto;
import com.taskmanagement.api.exception.ForbiddenException;
import com.taskmanagement.api.exception.ResourceNotFoundException;
import com.taskmanagement.api.mapper.TaskMapper;
import com.taskmanagement.api.model.Task;
import com.taskmanagement.api.model.TaskStatus;
import com.taskmanagement.api.model.User;
import com.taskmanagement.api.repository.TaskRepository;
import com.taskmanagement.api.repository.UserRepository;
import com.taskmanagement.api.service.TaskService;
import com.taskmanagement.api.service.TaskStatisticsDto;
import com.taskmanagement.api.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.taskmanagement.api.config.RedisCacheConfig.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de tareas.
 *
 * Capa SERVICE (Implementation): Implementa la lógica de negocio definida
 * en la interfaz TaskService.
 *
 * Anotaciones:
 * @Service: Marca la clase como un componente de servicio de Spring
 * @RequiredArgsConstructor: Genera un constructor con todos los campos final (para inyección de dependencias)
 * @Slf4j: Genera automáticamente un logger (log) usando SLF4J
 * @Transactional: Gestiona transacciones de BD a nivel de clase
 *
 * Patrón de inyección de dependencias:
 * - Constructor injection (recomendado): Campos final + @RequiredArgsConstructor
 * - Facilita testing y asegura que las dependencias no sean null
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    // Inyección de dependencias mediante constructor (inmutable)
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;  // Inyectado por Spring (MapStruct genera @Component)

    // =========================================================================
    // MÉTODOS HELPER PARA SEGURIDAD Y OWNERSHIP
    // =========================================================================

    /**
     * Obtiene el usuario actualmente autenticado desde el contexto de Spring Security.
     *
     * CÓMO FUNCIONA:
     * 1. SecurityContextHolder mantiene el contexto de seguridad del thread actual
     * 2. SecurityContext contiene la Authentication del usuario autenticado
     * 3. Authentication.getPrincipal() retorna el UserDetails (en nuestro caso, User)
     * 4. Casteamos a User y retornamos
     *
     * CUÁNDO SE USA:
     * - Al crear una tarea (para asignar el propietario)
     * - Al verificar ownership (para comprobar permisos)
     *
     * SEGURIDAD:
     * - Este método solo funciona si hay un usuario autenticado
     * - Si no hay usuario (endpoint público), lanzará excepción
     * - Todos los endpoints de tareas requieren autenticación (configurado en SecurityConfig)
     *
     * @return Usuario autenticado actual
     * @throws IllegalStateException si no hay usuario autenticado
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay usuario autenticado");
        }

        // getPrincipal() retorna el UserDetails (nuestra clase User implementa UserDetails)
        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User user = (User) principal;
            // Obtener el usuario completo desde la BD para tener la entidad gestionada por JPA
            return userRepository.findById(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuario autenticado no encontrado en BD: " + user.getId()));
        }

        throw new IllegalStateException("Principal no es una instancia de User");
    }

    /**
     * Verifica que el usuario actual sea el propietario de la tarea.
     *
     * SEGURIDAD - OWNERSHIP:
     * Este método implementa la lógica de autorización a nivel de recurso.
     * Asegura que solo el propietario de una tarea puede:
     * - Verla (getTaskById)
     * - Modificarla (updateTask, patchTask)
     * - Eliminarla (deleteTask)
     *
     * CÓMO FUNCIONA:
     * 1. Obtiene el usuario actual autenticado
     * 2. Compara el ID del usuario con el ID del propietario de la tarea
     * 3. Si NO coinciden, lanza ForbiddenException (403)
     *
     * DIFERENCIA CON @PreAuthorize:
     * - @PreAuthorize: Autorización basada en roles (ROLE_USER, ROLE_ADMIN)
     * - verifyOwnership(): Autorización basada en ownership de recursos
     *
     * NOTA PARA ADMIN:
     * Si quisiéramos que los ADMINs puedan ver/modificar todas las tareas:
     * ```java
     * if (currentUser.getRoles().stream()
     *         .anyMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
     *     return; // Admin tiene acceso a todo
     * }
     * ```
     *
     * @param task Tarea a verificar
     * @throws ForbiddenException si el usuario actual no es el propietario
     */
    private void verifyOwnership(Task task) {
        User currentUser = getCurrentUser();

        if (!task.getUser().getId().equals(currentUser.getId())) {
            log.warn("Usuario {} intentó acceder a tarea {} que pertenece a usuario {}",
                    currentUser.getId(),
                    task.getId(),
                    task.getUser().getId());

            throw new ForbiddenException(
                    "No tienes permiso para acceder a esta tarea. Solo el propietario puede modificarla.");
        }

        log.debug("Ownership verificado: usuario {} es propietario de tarea {}",
                currentUser.getId(),
                task.getId());
    }

    // =========================================================================
    // MÉTODOS DE SERVICIO - OPERACIONES CRUD
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * CACHE EVICTION:
     * - Invalida caché de listas de tareas del usuario (CACHE_TASKS_BY_USER)
     * - Invalida caché de estadísticas (CACHE_TASK_STATISTICS)
     *
     * RAZÓN:
     * - Al crear una tarea, las listas de tareas del usuario cambiar
     * - Las estadísticas (conteo de tareas, tareas por estado) cambian
     * - Debemos invalidar caché para que próximas consultas obtengan datos actualizados
     *
     * NOTA:
     * - No invalidamos CACHE_TASKS porque aún no existe esta tarea en caché
     */
    @Override
    @CacheEvict(value = {CACHE_TASKS_BY_USER, CACHE_TASK_STATISTICS}, allEntries = true)
    public TaskResponseDto createTask(TaskRequestDto taskRequestDto) {
        log.info("Creando nueva tarea con título: {}", taskRequestDto.getTitle());

        // Obtener usuario actual autenticado
        User currentUser = getCurrentUser();

        // Usar TaskMapper para convertir DTO a entidad
        Task task = taskMapper.toEntity(taskRequestDto);

        // SEGURIDAD: Asignar el usuario actual como propietario de la tarea
        task.setUser(currentUser);

        // Guardar en la base de datos
        Task savedTask = taskRepository.save(task);

        log.info("Tarea creada exitosamente con ID: {} para usuario: {} (caché invalidado)",
                savedTask.getId(),
                currentUser.getUsername());

        // Usar TaskMapper para convertir entidad a DTO de respuesta
        return taskMapper.toResponseDto(savedTask);
    }

    /**
     * {@inheritDoc}
     *
     * CACHÉ:
     * - Cache name: "tasks"
     * - Key: ID de la tarea (#id)
     * - TTL: 30 minutos (configurado en RedisCacheConfig)
     *
     * FUNCIONAMIENTO:
     * 1. Primera llamada: Query a PostgreSQL → Guarda en Redis
     * 2. Siguientes llamadas: Lee desde Redis (sin tocar PostgreSQL)
     * 3. Al actualizar/eliminar: Caché se invalida con @CacheEvict
     *
     * BENEFICIO:
     * - Reduce latencia de ~50ms (PostgreSQL) a <1ms (Redis)
     * - Reduce carga en BD (queries repetidas no golpean PostgreSQL)
     *
     * EJEMPLO DE CLAVE EN REDIS:
     * task-api::tasks::123
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_TASKS, key = "#id")
    public TaskResponseDto getTaskById(Long id) {
        log.info("Buscando tarea con ID: {} (cache miss - consultando BD)", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

        // SEGURIDAD: Verificar que el usuario actual es el propietario
        verifyOwnership(task);

        return taskMapper.toResponseDto(task);
    }

    /**
     * {@inheritDoc}
     *
     * CACHE EVICTION:
     * - Invalida caché de la tarea específica (CACHE_TASKS, key=#id)
     * - Invalida todo el caché de listas de tareas (CACHE_TASKS_BY_USER)
     * - Invalida caché de estadísticas (CACHE_TASK_STATISTICS)
     *
     * RAZÓN:
     * - Al actualizar, la tarea cambió → invalidar caché de esa tarea
     * - Las listas también cambiaron (ej: status cambió) → invalidar listas
     * - Las estadísticas pueden haber cambiado → invalidar estadísticas
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_TASKS, key = "#id"),
            @CacheEvict(value = CACHE_TASKS_BY_USER, allEntries = true),
            @CacheEvict(value = CACHE_TASK_STATISTICS, allEntries = true)
    })
    public TaskResponseDto updateTask(Long id, TaskRequestDto taskRequestDto) {
        log.info("Actualizando tarea con ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

        // SEGURIDAD: Verificar que el usuario actual es el propietario
        verifyOwnership(task);

        // Usar TaskMapper para actualizar la entidad
        taskMapper.updateEntityFromDto(task, taskRequestDto);

        Task updatedTask = taskRepository.save(task);

        log.info("Tarea actualizada exitosamente con ID: {} (caché invalidado)", updatedTask.getId());

        return taskMapper.toResponseDto(updatedTask);
    }

    /**
     * {@inheritDoc}
     *
     * CACHE EVICTION:
     * - Similar a updateTask, invalida los mismos cachés
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_TASKS, key = "#id"),
            @CacheEvict(value = CACHE_TASKS_BY_USER, allEntries = true),
            @CacheEvict(value = CACHE_TASK_STATISTICS, allEntries = true)
    })
    public TaskResponseDto patchTask(Long id, TaskPatchDto taskPatchDto) {
        log.info("Actualizando parcialmente tarea con ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

        // SEGURIDAD: Verificar que el usuario actual es el propietario
        verifyOwnership(task);

        // Usar TaskMapper para actualización parcial
        taskMapper.patchEntityFromDto(task, taskPatchDto);

        Task updatedTask = taskRepository.save(task);

        log.info("Tarea actualizada parcialmente con éxito con ID: {} (caché invalidado)", updatedTask.getId());

        return taskMapper.toResponseDto(updatedTask);
    }

    /**
     * {@inheritDoc}
     *
     * CACHE EVICTION:
     * - Invalida caché de la tarea eliminada (CACHE_TASKS, key=#id)
     * - Invalida listas de tareas (CACHE_TASKS_BY_USER)
     * - Invalida estadísticas (CACHE_TASK_STATISTICS)
     */
    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_TASKS, key = "#id"),
            @CacheEvict(value = CACHE_TASKS_BY_USER, allEntries = true),
            @CacheEvict(value = CACHE_TASK_STATISTICS, allEntries = true)
    })
    public void deleteTask(Long id) {
        log.info("Eliminando tarea con ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

        // SEGURIDAD: Verificar que el usuario actual es el propietario
        verifyOwnership(task);

        taskRepository.deleteById(id);

        log.info("Tarea eliminada exitosamente con ID: {}", id);
    }

    /**
     * {@inheritDoc}
     *
     * CACHÉ:
     * - Cache name: "taskStats"
     * - Key: Constante "all" (estadísticas globales)
     * - TTL: 5 minutos (configurado en RedisCacheConfig)
     *
     * RAZÓN:
     * - Las estadísticas requieren múltiples COUNT queries a PostgreSQL
     * - Son datos que cambian frecuentemente pero no necesitan ser tiempo real
     * - Cachear por 5 minutos reduce carga significativamente
     *
     * BENEFICIO:
     * - Reduce 5 queries (1 COUNT por cada estado) a 0 queries
     * - Latencia de ~200ms (5 queries) a <1ms (Redis)
     *
     * INVALIDACIÓN:
     * - Se invalida automáticamente en create/update/delete de tareas
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_TASK_STATISTICS, key = "'all'")
    public TaskStatisticsDto getStatistics() {
        log.info("Obteniendo estadísticas de tareas (cache miss - consultando BD)");

        TaskStatisticsDto stats = new TaskStatisticsDto();
        stats.setTotalTasks(taskRepository.count());
        stats.setPendingTasks(taskRepository.countByStatus(TaskStatus.PENDING));
        stats.setInProgressTasks(taskRepository.countByStatus(TaskStatus.IN_PROGRESS));
        stats.setCompletedTasks(taskRepository.countByStatus(TaskStatus.COMPLETED));
        stats.setCancelledTasks(taskRepository.countByStatus(TaskStatus.CANCELLED));

        return stats;
    }

    // =========================================================================
    // MÉTODOS CON PAGINACIÓN
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Implementación de paginación usando Spring Data JPA.
     *
     * CÓMO FUNCIONA:
     * 1. El Pageable contiene: número de página, tamaño, ordenamiento
     * 2. Spring Data JPA genera automáticamente la query SQL con LIMIT y OFFSET
     * 3. Page<Task> contiene los datos + metadata de paginación
     * 4. Convertimos Task -> TaskResponseDto usando map()
     *
     * VENTAJAS:
     * - Performance: Solo carga datos de la página actual
     * - Escalabilidad: Funciona bien con millones de registros
     * - Metadata: Incluye info de paginación (totalPages, totalElements, etc)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getAllTasks(Pageable pageable) {
        log.info("Obteniendo tareas paginadas - Página: {}, Tamaño: {}, Orden: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        // Page.map() transforma cada elemento de la página
        // Sin necesidad de cargar todo en memoria primero
        return taskRepository.findAll(pageable)
                .map(taskMapper::toResponseDto);
    }

    /**
     * {@inheritDoc}
     *
     * Busca tareas por estado con paginación.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTasksByStatus(TaskStatus status, Pageable pageable) {
        log.info("Buscando tareas con estado: {} - Página: {}, Tamaño: {}",
                status,
                pageable.getPageNumber(),
                pageable.getPageSize());

        return taskRepository.findByStatus(status, pageable)
                .map(taskMapper::toResponseDto);
    }

    /**
     * {@inheritDoc}
     *
     * Busca tareas por título con paginación.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> searchTasksByTitle(String title, Pageable pageable) {
        log.info("Buscando tareas por título: '{}' - Página: {}, Tamaño: {}",
                title,
                pageable.getPageNumber(),
                pageable.getPageSize());

        return taskRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(taskMapper::toResponseDto);
    }

    /**
     * {@inheritDoc}
     *
     * Implementación de filtrado avanzado usando Specification API.
     *
     * FUNCIONAMIENTO:
     * 1. Recibe TaskFilterDto con filtros opcionales
     * 2. TaskSpecification construye predicados dinámicos
     * 3. Spring Data JPA genera SQL optimizado
     * 4. Aplica paginación y ordenamiento
     * 5. Mapea entidades a DTOs
     *
     * PERFORMANCE:
     * - Solo genera predicados para filtros no-null
     * - Usa índices de BD apropiadamente
     * - Paginación eficiente (LIMIT/OFFSET)
     * - Mapeo eficiente con MapStruct
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> filterTasks(TaskFilterDto filters, Pageable pageable) {
        log.info("Filtrando tareas con criterios: {}", filters);

        // Construir Specification basada en filtros
        Specification<Task> spec = TaskSpecification.filterBy(filters);

        // Ejecutar query con paginación
        Page<Task> tasksPage = taskRepository.findAll(spec, pageable);

        log.debug("Tareas encontradas: {} de {} total",
                tasksPage.getNumberOfElements(),
                tasksPage.getTotalElements());

        // Mapear a DTOs
        return tasksPage.map(taskMapper::toResponseDto);
    }

    // =========================================================================
    // SOFT DELETE - IMPLEMENTACIÓN DE MÉTODOS DE PAPELERA
    // =========================================================================

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getDeletedTasks(Pageable pageable) {
        log.info("Obteniendo tareas eliminadas (papelera) - Página: {}, Tamaño: {}",
                pageable.getPageNumber(),
                pageable.getPageSize());

        // Obtener usuario actual
        User currentUser = getCurrentUser();

        // IMPORTANTE: findAllDeletedByUser(userId, pageable) usa query NATIVA que bypasea @Where
        // La query nativa es necesaria porque @Where(clause = "deleted_at IS NULL") en la entidad
        // filtraría automáticamente las tareas eliminadas incluso con @Query personalizado
        Page<TaskResponseDto> deletedTasks = taskRepository.findAllDeletedByUser(currentUser.getId(), pageable)
                .map(taskMapper::toResponseDto);

        log.info("Tareas eliminadas encontradas para usuario {}: {}",
                currentUser.getUsername(),
                deletedTasks.getTotalElements());

        return deletedTasks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskResponseDto restoreTask(Long id) {
        log.info("Restaurando tarea eliminada con ID: {}", id);

        // Buscar tarea en papelera (deletedAt NOT NULL)
        Task deletedTask = taskRepository.findDeletedById(id)
                .orElseThrow(() -> {
                    log.error("Tarea eliminada no encontrada con ID: {}", id);
                    return new ResourceNotFoundException(
                            "Tarea eliminada no encontrada con ID: " + id + ". " +
                            "La tarea no existe o no está en papelera.");
                });

        // SEGURIDAD: Verificar ownership
        verifyOwnership(deletedTask);

        // Restaurar (marcar deletedAt como null)
        deletedTask.restore(); // Usa método helper de la entidad
        Task restoredTask = taskRepository.save(deletedTask);

        log.info("Tarea restaurada exitosamente con ID: {} para usuario: {}",
                restoredTask.getId(),
                getCurrentUser().getUsername());

        return taskMapper.toResponseDto(restoredTask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public int purgeOldDeletedTasks(int retentionDays) {
        log.warn("Iniciando purge de tareas eliminadas hace más de {} días", retentionDays);

        // Calcular fecha límite (threshold)
        java.time.LocalDateTime threshold = java.time.LocalDateTime.now().minusDays(retentionDays);

        log.warn("Eliminando permanentemente tareas con deletedAt < {}", threshold);

        // Ejecutar purge (eliminación física)
        int deletedCount = taskRepository.purgeDeletedBefore(threshold);

        log.warn("Purge completado: {} tareas eliminadas permanentemente", deletedCount);

        return deletedCount;
    }

    // =========================================================================
    // NOTA SOBRE MAPEO:
    // Los métodos privados mapToEntity y mapToResponseDto fueron eliminados.
    // Ahora usamos TaskMapper (clase utilitaria centralizada) para todas las
    // conversiones entre entidades y DTOs.
    //
    // VENTAJAS:
    // - Código DRY: Un solo lugar para mapeo
    // - Fácil de testear: TaskMapper es independiente
    // - Reutilizable: Otros servicios pueden usar el mismo mapper
    // - Mantenible: Cambios de mapeo en un solo lugar
    // =========================================================================
}
