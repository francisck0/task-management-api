package com.taskmanagement.api.specification;

import com.taskmanagement.api.dto.TaskFilterDto;
import com.taskmanagement.api.model.Task;
import com.taskmanagement.api.model.TaskPriority;
import com.taskmanagement.api.model.TaskStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Especificaciones JPA para filtrado dinámico de tareas.
 *
 * PATRÓN: Specification Pattern (Spring Data JPA)
 * Las Specifications permiten construir queries dinámicas de forma type-safe
 * combinando múltiples criterios de filtrado.
 *
 * VENTAJAS DE USAR SPECIFICATIONS:
 *
 * 1. QUERIES DINÁMICAS:
 *    - Construye queries basadas en filtros presentes
 *    - Solo incluye criterios cuando son necesarios
 *    - Evita crear múltiples query methods para cada combinación
 *
 * 2. TYPE-SAFE:
 *    - Criteria API usa metamodel de JPA
 *    - Errores detectados en compilación
 *    - Refactoring seguro
 *
 * 3. COMPOSICIÓN:
 *    - Se pueden combinar Specifications con AND/OR
 *    - Reutilizables y modulares
 *    - Ejemplo: spec1.and(spec2).or(spec3)
 *
 * 4. PERFORMANCE:
 *    - Genera SQL optimizado
 *    - Solo include JOINs necesarios
 *    - Predicados combinados eficientemente
 *
 * ¿CUÁNDO USAR SPECIFICATIONS?
 * - Filtros dinámicos y opcionales ✅
 * - Combinaciones complejas de criterios ✅
 * - Queries que varían según contexto ✅
 *
 * ¿CUÁNDO NO USAR SPECIFICATIONS?
 * - Queries simples y fijas → Query Methods
 * - Queries con lógica muy compleja → Native Queries
 *
 * EJEMPLO DE USO:
 * ```java
 * TaskFilterDto filters = new TaskFilterDto();
 * filters.setStatus(TaskStatus.PENDING);
 * filters.setPriority(TaskPriority.HIGH);
 *
 * Specification<Task> spec = TaskSpecification.filterBy(filters);
 * List<Task> tasks = taskRepository.findAll(spec);
 * // SQL generado: WHERE status = 'PENDING' AND priority = 'HIGH'
 * ```
 *
 * SQL GENERADO (ejemplo con múltiples filtros):
 * ```sql
 * SELECT t.*
 * FROM tasks t
 * WHERE t.status = 'PENDING'
 *   AND t.priority = 'HIGH'
 *   AND t.created_at >= '2025-11-01 00:00:00'
 *   AND t.due_date <= '2025-11-30 23:59:59'
 *   AND (LOWER(t.title) LIKE '%documentación%'
 *        OR LOWER(t.description) LIKE '%documentación%')
 *   AND t.deleted_at IS NULL
 * ORDER BY t.created_at DESC
 * ```
 *
 * @see org.springframework.data.jpa.domain.Specification
 * @see com.taskmanagement.api.repository.TaskRepository
 * @see com.taskmanagement.api.dto.TaskFilterDto
 */
public class TaskSpecification {

    /**
     * Crea una Specification basada en los filtros proporcionados.
     *
     * FUNCIONAMIENTO:
     * 1. Recibe TaskFilterDto con filtros opcionales
     * 2. Construye lista de predicados (condiciones WHERE)
     * 3. Solo agrega predicados para filtros no-null
     * 4. Combina todos los predicados con AND
     * 5. Retorna Specification que genera el SQL
     *
     * PREDICADOS CONSTRUIDOS:
     * - status: WHERE status = ?
     * - priority: WHERE priority = ?
     * - createdAfter: WHERE created_at >= ?
     * - createdBefore: WHERE created_at <= ?
     * - dueDateAfter: WHERE due_date >= ?
     * - dueDateBefore: WHERE due_date <= ?
     * - search: WHERE LOWER(title) LIKE ? OR LOWER(description) LIKE ?
     *
     * @param filters criterios de filtrado (campos opcionales)
     * @return Specification que combina todos los filtros con AND
     */
    public static Specification<Task> filterBy(TaskFilterDto filters) {
        return (Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            // Lista para acumular predicados (condiciones WHERE)
            List<Predicate> predicates = new ArrayList<>();

            // ================================================================
            // FILTRO POR ESTADO
            // ================================================================
            if (filters.getStatus() != null) {
                // WHERE status = 'PENDING'
                predicates.add(cb.equal(root.get("status"), filters.getStatus()));
            }

            // ================================================================
            // FILTRO POR PRIORIDAD
            // ================================================================
            if (filters.getPriority() != null) {
                // WHERE priority = 'HIGH'
                predicates.add(cb.equal(root.get("priority"), filters.getPriority()));
            }

            // ================================================================
            // FILTROS POR FECHA DE CREACIÓN
            // ================================================================
            if (filters.getCreatedAfter() != null) {
                // WHERE created_at >= '2025-11-01 00:00:00'
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        filters.getCreatedAfter()
                ));
            }

            if (filters.getCreatedBefore() != null) {
                // WHERE created_at <= '2025-11-15 23:59:59'
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"),
                        filters.getCreatedBefore()
                ));
            }

            // ================================================================
            // FILTROS POR FECHA DE VENCIMIENTO
            // ================================================================
            if (filters.getDueDateAfter() != null) {
                // WHERE due_date >= '2025-11-20 00:00:00'
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("dueDate"),
                        filters.getDueDateAfter()
                ));
            }

            if (filters.getDueDateBefore() != null) {
                // WHERE due_date <= '2025-11-30 23:59:59'
                // ÚTIL PARA: Tareas vencidas (dueDateBefore = now)
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("dueDate"),
                        filters.getDueDateBefore()
                ));
            }

            // ================================================================
            // FILTRO POR BÚSQUEDA DE TEXTO
            // ================================================================
            if (filters.getSearch() != null && !filters.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filters.getSearch().toLowerCase() + "%";

                // WHERE (LOWER(title) LIKE '%search%' OR LOWER(description) LIKE '%search%')
                Predicate titlePredicate = cb.like(
                        cb.lower(root.get("title")),
                        searchPattern
                );

                Predicate descriptionPredicate = cb.like(
                        cb.lower(root.get("description")),
                        searchPattern
                );

                // Combinar con OR: title LIKE ? OR description LIKE ?
                predicates.add(cb.or(titlePredicate, descriptionPredicate));
            }

            // ================================================================
            // COMBINAR TODOS LOS PREDICADOS CON AND
            // ================================================================
            // WHERE predicate1 AND predicate2 AND predicate3 AND ...
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // =========================================================================
    // SPECIFICATIONS INDIVIDUALES (COMPOSABLES)
    // =========================================================================
    // Estos métodos permiten crear specifications modulares que se pueden
    // combinar de formas diferentes según la necesidad.

    /**
     * Specification para filtrar por estado.
     *
     * @param status estado a filtrar
     * @return Specification
     */
    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Specification para filtrar por prioridad.
     *
     * @param priority prioridad a filtrar
     * @return Specification
     */
    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    /**
     * Specification para tareas creadas después de una fecha.
     *
     * @param date fecha mínima
     * @return Specification
     */
    public static Specification<Task> createdAfter(LocalDateTime date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), date);
    }

    /**
     * Specification para tareas creadas antes de una fecha.
     *
     * @param date fecha máxima
     * @return Specification
     */
    public static Specification<Task> createdBefore(LocalDateTime date) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), date);
    }

    /**
     * Specification para tareas que vencen después de una fecha.
     *
     * @param date fecha mínima de vencimiento
     * @return Specification
     */
    public static Specification<Task> dueDateAfter(LocalDateTime date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dueDate"), date);
    }

    /**
     * Specification para tareas que vencen antes de una fecha.
     *
     * ÚTIL PARA:
     * - Tareas vencidas: dueDateBefore(LocalDateTime.now())
     * - Tareas que vencen pronto: dueDateBefore(LocalDateTime.now().plusDays(7))
     *
     * @param date fecha máxima de vencimiento
     * @return Specification
     */
    public static Specification<Task> dueDateBefore(LocalDateTime date) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dueDate"), date);
    }

    /**
     * Specification para búsqueda de texto en título y descripción.
     *
     * @param searchText texto a buscar (case-insensitive)
     * @return Specification
     */
    public static Specification<Task> searchByText(String searchText) {
        return (root, query, cb) -> {
            String pattern = "%" + searchText.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Specification para tareas vencidas (overdue).
     *
     * Tareas que:
     * - Tienen dueDate
     * - dueDate < now
     * - No están completadas ni canceladas
     *
     * @return Specification
     */
    public static Specification<Task> isOverdue() {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("dueDate")),
                cb.lessThan(root.get("dueDate"), LocalDateTime.now()),
                cb.or(
                        cb.equal(root.get("status"), TaskStatus.PENDING),
                        cb.equal(root.get("status"), TaskStatus.IN_PROGRESS)
                )
        );
    }

    /**
     * Specification para tareas que vencen pronto (próximas X días).
     *
     * @param days número de días
     * @return Specification
     */
    public static Specification<Task> dueWithinDays(int days) {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(days);
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("dueDate")),
                cb.between(root.get("dueDate"), LocalDateTime.now(), futureDate),
                cb.or(
                        cb.equal(root.get("status"), TaskStatus.PENDING),
                        cb.equal(root.get("status"), TaskStatus.IN_PROGRESS)
                )
        );
    }
}
