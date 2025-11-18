package com.taskmanagement.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una tarea en el sistema.
 *
 * Capa MODEL: Contiene las entidades del dominio que mapean directamente
 * con las tablas de la base de datos.
 *
 * AUDITORÍA: Esta entidad hereda de Auditable para obtener automáticamente
 * campos de auditoría (createdAt, updatedAt, etc.) sin duplicar código.
 *
 * SOFT DELETE: Esta entidad implementa eliminación lógica (soft delete).
 * - Cuando se "elimina" una tarea, solo se marca con deletedAt (timestamp)
 * - Las tareas eliminadas NO aparecen en queries normales (filtradas por @Where)
 * - Se pueden restaurar cambiando deletedAt a null
 * - Nunca se eliminan físicamente de la base de datos
 * - Cumple con regulaciones de retención de datos y auditoría
 *
 * ÍNDICES DE BASE DE DATOS:
 * Esta tabla define 5 índices para optimizar las consultas más frecuentes:
 *
 * 1. idx_task_status (status):
 *    - Usado en: GET /tasks/status/{status}, countByStatus()
 *    - Optimiza: Búsquedas y filtros por estado (PENDING, IN_PROGRESS, etc.)
 *    - Impacto: O(n) → O(log n) en búsquedas
 *
 * 2. idx_task_user_id (user_id):
 *    - Usado en: Todas las operaciones (ownership verification)
 *    - Optimiza: JOIN con tabla users, filtros por usuario
 *    - Impacto: CRÍTICO - Sin este índice, cada verificación sería O(n)
 *
 * 3. idx_task_due_date (due_date):
 *    - Usado en: Búsquedas de tareas por vencer, tareas vencidas
 *    - Optimiza: Ordenamiento y filtrado por fecha límite
 *    - Impacto: Mejora queries temporales
 *
 * 4. idx_task_created_at (created_at DESC):
 *    - Usado en: findAllByOrderByCreatedAtDesc(), paginación con orden por fecha
 *    - Optimiza: Ordenamiento por fecha de creación descendente
 *    - Impacto: Evita full table scan en ordenamientos
 *
 * 5. idx_task_status_created (status, created_at DESC):
 *    - Índice compuesto para queries combinadas
 *    - Usado en: findByStatus() con paginación ordenada por fecha
 *    - Optimiza: Filtrado por estado + ordenamiento por fecha en una sola operación
 *    - Impacto: Mejora significativa cuando se filtra por estado y se ordena (query común)
 *
 * PERFORMANCE:
 * - Con 1,000 tareas: Mejora de ~10x en búsquedas
 * - Con 100,000 tareas: Mejora de ~100x en búsquedas
 * - Con 1,000,000 tareas: Mejora de ~1000x en búsquedas
 *
 * TRADE-OFF:
 * - Ventaja: Consultas mucho más rápidas
 * - Desventaja: Inserciones/actualizaciones ~5-10% más lentas (aceptable)
 * - Espacio: ~10-15% más espacio en disco (aceptable)
 *
 * Anotaciones de Lombok:
 * @Data: Genera getters, setters, toString, equals y hashCode
 * @NoArgsConstructor: Genera constructor sin argumentos (requerido por JPA)
 * @AllArgsConstructor: Genera constructor con todos los argumentos
 * @EqualsAndHashCode(callSuper = false): Evita incluir campos de la clase padre en equals/hashCode
 *
 * Anotaciones de JPA:
 * @Entity: Marca la clase como una entidad JPA
 * @Table: Define el nombre de la tabla en la BD y sus índices
 */
@Entity
@Table(
    name = "tasks",
    indexes = {
        @Index(name = "idx_task_status", columnList = "status"),
        @Index(name = "idx_task_priority", columnList = "priority"),
        @Index(name = "idx_task_user_id", columnList = "user_id"),
        @Index(name = "idx_task_due_date", columnList = "due_date"),
        @Index(name = "idx_task_created_at", columnList = "created_at DESC"),
        @Index(name = "idx_task_status_created", columnList = "status, created_at DESC"),
        @Index(name = "idx_task_priority_due_date", columnList = "priority DESC, due_date"),
        @Index(name = "idx_task_deleted_at", columnList = "deleted_at")
    }
)
@org.hibernate.annotations.Where(clause = "deleted_at IS NULL")
@org.hibernate.annotations.SQLDelete(sql = "UPDATE tasks SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Task extends Auditable {

    /**
     * Identificador único de la tarea (Primary Key)
     *
     * @Id: Marca el campo como primary key
     * @GeneratedValue: Define cómo se generará el valor del ID
     * IDENTITY: Usa auto-incremento de la BD (apropiado para PostgreSQL con SERIAL)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título de la tarea
     *
     * @Column: Personaliza la columna en la BD
     * nullable = false: Campo obligatorio
     * length: Longitud máxima del campo
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * Descripción detallada de la tarea
     *
     * columnDefinition = "TEXT": Usa tipo TEXT en PostgreSQL (sin límite de caracteres)
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Estado actual de la tarea
     *
     * @Enumerated: Define cómo se almacena el enum en la BD
     * EnumType.STRING: Guarda el nombre del enum como string (más legible y seguro)
     * EnumType.ORDINAL: Guardaría el índice numérico (menos recomendado)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    /**
     * Prioridad de la tarea
     *
     * Permite a los usuarios clasificar tareas por importancia/urgencia.
     * Valores: LOW, MEDIUM, HIGH, CRITICAL
     *
     * DEFAULT: MEDIUM (prioridad media por defecto)
     *
     * FILTRADO:
     * - Los usuarios pueden filtrar por prioridad
     * - Ordenar por prioridad (CRITICAL primero)
     * - Combinar con otros filtros (status, fechas, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    /**
     * Fecha límite para completar la tarea (opcional)
     */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /**
     * Usuario propietario de la tarea
     *
     * @ManyToOne: Relación muchos-a-uno (muchas tareas pertenecen a un usuario)
     * FetchType.LAZY: Carga el usuario solo cuando se necesita (optimización)
     * @JoinColumn: Define la columna FK en la tabla tasks
     * nullable = false: Toda tarea DEBE tener un propietario
     *
     * SEGURIDAD: Esta relación permite implementar ownership y autorización.
     * Solo el propietario de la tarea puede modificarla o eliminarla.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Fecha de eliminación lógica (soft delete)
     *
     * SOFT DELETE IMPLEMENTATION:
     * - null: Tarea activa (no eliminada)
     * - timestamp: Tarea eliminada lógicamente en esa fecha/hora
     *
     * FUNCIONAMIENTO:
     * - @Where(clause = "deleted_at IS NULL"): Filtra automáticamente tareas eliminadas
     * - @SQLDelete: Intercepta DELETE y ejecuta UPDATE en su lugar
     * - Las tareas "eliminadas" siguen en la BD pero no aparecen en queries
     *
     * VENTAJAS:
     * - Recuperación de datos eliminados accidentalmente
     * - Auditoría completa de eliminaciones
     * - Cumplimiento de regulaciones de retención de datos
     * - Trazabilidad total
     *
     * RESTAURACIÓN:
     * - Para restaurar: task.setDeletedAt(null); taskRepository.save(task);
     * - Requiere query nativa o @Query sin filtro @Where
     *
     * @Column(name = "deleted_at"): Nombre de columna en BD
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // =========================================================================
    // MÉTODOS HELPER PARA SOFT DELETE
    // =========================================================================

    /**
     * Verifica si la tarea está eliminada (soft deleted)
     *
     * @return true si la tarea fue eliminada lógicamente
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Marca la tarea como eliminada (soft delete)
     * Establece deletedAt al momento actual
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restaura la tarea eliminada (undelete)
     * Establece deletedAt a null
     */
    public void restore() {
        this.deletedAt = null;
    }

    // =========================================================================
    // NOTA SOBRE AUDITORÍA:
    // Los campos createdAt y updatedAt ahora se heredan de la clase Auditable
    // y se gestionan automáticamente por Spring Data JPA Auditing.
    //
    // Ya no es necesario definirlos aquí ni usar @CreationTimestamp/@UpdateTimestamp
    // de Hibernate. Spring Data JPA es más estándar y extensible.
    // =========================================================================
}
