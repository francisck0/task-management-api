package com.taskmanagement.api.repository;

import com.taskmanagement.api.model.Task;
import com.taskmanagement.api.model.TaskStatus;
import com.taskmanagement.api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para acceso a datos de la entidad Task.
 *
 * Capa REPOSITORY: Responsable de la persistencia y recuperación de datos.
 * Actúa como intermediario entre la capa de servicio y la base de datos.
 *
 * INTERFACES EXTENDIDAS:
 *
 * 1. JpaRepository<Task, Long>:
 *    - Task: Tipo de entidad que maneja el repositorio
 *    - Long: Tipo del identificador (ID) de la entidad
 *    - Proporciona métodos CRUD listos para usar:
 *      * save(entity): Guarda o actualiza una entidad
 *      * findById(id): Busca por ID
 *      * findAll(): Obtiene todas las entidades
 *      * deleteById(id): Elimina por ID
 *      * count(): Cuenta el total de entidades
 *      * existsById(id): Verifica si existe una entidad
 *
 * 2. JpaSpecificationExecutor<Task>:
 *    - Permite ejecutar queries dinámicas usando Specification API
 *    - Métodos proporcionados:
 *      * findAll(Specification): Query dinámica
 *      * findOne(Specification): Buscar uno
 *      * findAll(Specification, Pageable): Query dinámica con paginación
 *      * findAll(Specification, Sort): Query dinámica con ordenamiento
 *      * count(Specification): Contar con filtros
 *    - VENTAJAS:
 *      * Queries dinámicas type-safe
 *      * Combinación flexible de filtros
 *      * Sin necesidad de crear múltiples query methods
 *
 * EJEMPLO DE USO CON SPECIFICATIONS:
 * ```java
 * TaskFilterDto filters = new TaskFilterDto();
 * filters.setStatus(TaskStatus.PENDING);
 * filters.setPriority(TaskPriority.HIGH);
 *
 * Specification<Task> spec = TaskSpecification.filterBy(filters);
 * Page<Task> tasks = taskRepository.findAll(spec, pageable);
 * ```
 *
 * Spring Data JPA genera automáticamente la implementación en tiempo de ejecución.
 * No es necesario escribir código de implementación.
 *
 * @see com.taskmanagement.api.specification.TaskSpecification
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * Cuenta el número de tareas con un estado específico.
     *
     * @param status estado a contar
     * @return número de tareas con ese estado
     */
    Long countByStatus(TaskStatus status);

    /**
     * Cuenta el número de tareas con una prioridad específica.
     *
     * @param priority prioridad a contar
     * @return número de tareas con esa prioridad
     */
    Long countByPriority(com.taskmanagement.api.model.TaskPriority priority);

    /**
     * Verifica si existe alguna tarea con el título exacto especificado.
     *
     * @param title título a verificar
     * @return true si existe, false en caso contrario
     */
    boolean existsByTitle(String title);

    // =========================================================================
    // MÉTODOS CON PAGINACIÓN
    // =========================================================================

    /**
     * Encuentra todas las tareas con paginación y ordenamiento.
     *
     * Este método sobrescribe el findAll() de JpaRepository para agregar
     * soporte de paginación.
     *
     * IMPORTANTE: Spring Data JPA automáticamente aplica el Pageable a la query.
     *
     * Ejemplo de uso:
     * <pre>
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
     * Page<Task> tasks = taskRepository.findAll(pageable);
     * </pre>
     *
     * @param pageable objeto con información de paginación (page, size, sort)
     * @return página de tareas
     */
    Page<Task> findAll(Pageable pageable);

    /**
     * Encuentra tareas por estado con paginación.
     *
     * Spring Data JPA genera automáticamente:
     * SELECT * FROM tasks WHERE status = ? ORDER BY ... LIMIT ? OFFSET ?
     *
     * Ejemplo:
     * <pre>
     * Pageable pageable = PageRequest.of(0, 10);
     * Page<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING, pageable);
     * </pre>
     *
     * @param status estado a buscar
     * @param pageable configuración de paginación
     * @return página de tareas con el estado especificado
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    /**
     * Busca tareas por título (case-insensitive) con paginación.
     *
     * Query generada:
     * SELECT * FROM tasks WHERE LOWER(title) LIKE LOWER(CONCAT('%', ?, '%'))
     * ORDER BY ... LIMIT ? OFFSET ?
     *
     * @param title texto a buscar en el título
     * @param pageable configuración de paginación
     * @return página de tareas que contienen el texto
     */
    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // =========================================================================
    // SOFT DELETE - MÉTODOS PARA GESTIÓN DE TAREAS ELIMINADAS
    // =========================================================================

    /**
     * Encuentra todas las tareas eliminadas del usuario actual con paginación.
     *
     * IMPORTANTE: Usa query nativa para bypassear el filtro @Where(clause = "deleted_at IS NULL")
     * de la entidad Task. De lo contrario, Hibernate filtraría automáticamente las tareas eliminadas.
     *
     * NOTA: El ORDER BY se omite en la query porque Spring Data JPA lo agrega automáticamente
     * desde el Pageable. Si se especifica aquí, Hibernate intenta agregar ambos y causa errores.
     *
     * @param userId ID del usuario propietario de las tareas
     * @param pageable configuración de paginación (incluye sort por deleted_at DESC)
     * @return página de tareas eliminadas del usuario
     */
    @Query(value = "SELECT * FROM tasks WHERE deleted_at IS NOT NULL AND user_id = :userId",
           countQuery = "SELECT COUNT(*) FROM tasks WHERE deleted_at IS NOT NULL AND user_id = :userId",
           nativeQuery = true)
    Page<Task> findAllDeletedByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Encuentra una tarea eliminada por ID.
     *
     * IMPORTANTE:
     * - Usa query nativa para bypassear el filtro @Where(clause = "deleted_at IS NULL")
     * - Puede encontrar tareas eliminadas
     * - Útil para restaurar tareas específicas
     *
     * @param id ID de la tarea
     * @return Optional con la tarea si está eliminada, vacío si no
     */
    @Query(value = "SELECT * FROM tasks WHERE id = :id AND deleted_at IS NOT NULL", nativeQuery = true)
    Optional<Task> findDeletedById(@Param("id") Long id);

    /**
     * Restaura una tarea eliminada (marca deletedAt como null).
     *
     * IMPORTANTE:
     * - Query nativa que actualiza directamente la BD
     * - Bypass del filtro @Where
     * - @Modifying indica que es una query de actualización
     *
     * @param id ID de la tarea a restaurar
     * @return número de filas afectadas (1 si se restauró, 0 si no existe)
     */
    @Modifying
    @Query("UPDATE Task t SET t.deletedAt = NULL WHERE t.id = :id AND t.deletedAt IS NOT NULL")
    int restoreById(@Param("id") Long id);

    /**
     * Elimina permanentemente tareas que fueron eliminadas hace más de X días.
     *
     * PURGE (LIMPIEZA):
     * - Elimina físicamente tareas de la BD
     * - Solo elimina tareas con deletedAt antiguo
     * - Útil para limpieza periódica (cumplimiento GDPR)
     *
     * EJEMPLO DE USO:
     * ```java
     * // Eliminar tareas eliminadas hace más de 90 días
     * LocalDateTime threshold = LocalDateTime.now().minusDays(90);
     * int deleted = taskRepository.purgeDeletedBefore(threshold);
     * ```
     *
     * @param threshold fecha límite (eliminar si deletedAt < threshold)
     * @return número de tareas eliminadas permanentemente
     */
    @Modifying
    @Query(value = "DELETE FROM tasks WHERE deleted_at < :threshold", nativeQuery = true)
    int purgeDeletedBefore(@Param("threshold") java.time.LocalDateTime threshold);

    /**
     * Cuenta el número de tareas eliminadas.
     *
     * @return número de tareas en papelera
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.deletedAt IS NOT NULL")
    Long countDeleted();
}
