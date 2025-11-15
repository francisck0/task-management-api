package com.taskmanagement.api.repository;

import com.taskmanagement.api.model.Task;
import com.taskmanagement.api.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para acceso a datos de la entidad Task.
 *
 * Capa REPOSITORY: Responsable de la persistencia y recuperación de datos.
 * Actúa como intermediario entre la capa de servicio y la base de datos.
 *
 * JpaRepository<Task, Long>:
 * - Task: Tipo de entidad que maneja el repositorio
 * - Long: Tipo del identificador (ID) de la entidad
 *
 * JpaRepository proporciona métodos CRUD listos para usar:
 * - save(entity): Guarda o actualiza una entidad
 * - findById(id): Busca por ID
 * - findAll(): Obtiene todas las entidades
 * - deleteById(id): Elimina por ID
 * - count(): Cuenta el total de entidades
 * - existsById(id): Verifica si existe una entidad
 *
 * Spring Data JPA genera automáticamente la implementación en tiempo de ejecución.
 * No es necesario escribir código de implementación.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Encuentra todas las tareas con un estado específico.
     *
     * Spring Data JPA deriva automáticamente la consulta del nombre del método:
     * - findBy: Indica una consulta de búsqueda
     * - Status: Nombre del campo en la entidad Task
     *
     * Query generada: SELECT * FROM tasks WHERE status = ?
     *
     * @param status estado de las tareas a buscar
     * @return lista de tareas con el estado especificado
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Encuentra tareas cuyo título contenga el texto especificado (ignorando mayúsculas).
     *
     * Naming conventions de Spring Data JPA:
     * - findBy: Prefijo de búsqueda
     * - Title: Campo a buscar
     * - Containing: Operador LIKE (%texto%)
     * - IgnoreCase: Búsqueda insensible a mayúsculas/minúsculas
     *
     * Query generada: SELECT * FROM tasks WHERE LOWER(title) LIKE LOWER(CONCAT('%', ?, '%'))
     *
     * @param title texto a buscar en el título
     * @return lista de tareas que contienen el texto en el título
     */
    List<Task> findByTitleContainingIgnoreCase(String title);

    /**
     * Encuentra tareas ordenadas por fecha de creación descendente (más recientes primero).
     *
     * @return lista de todas las tareas ordenadas por fecha de creación
     */
    List<Task> findAllByOrderByCreatedAtDesc();

    /**
     * Cuenta el número de tareas con un estado específico.
     *
     * @param status estado a contar
     * @return número de tareas con ese estado
     */
    Long countByStatus(TaskStatus status);

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
}
