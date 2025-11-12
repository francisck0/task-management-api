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
 * Anotaciones de Lombok:
 * @Data: Genera getters, setters, toString, equals y hashCode
 * @NoArgsConstructor: Genera constructor sin argumentos (requerido por JPA)
 * @AllArgsConstructor: Genera constructor con todos los argumentos
 * @EqualsAndHashCode(callSuper = false): Evita incluir campos de la clase padre en equals/hashCode
 *
 * Anotaciones de JPA:
 * @Entity: Marca la clase como una entidad JPA
 * @Table: Define el nombre de la tabla en la BD (opcional)
 */
@Entity
@Table(name = "tasks")
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
     * Fecha límite para completar la tarea (opcional)
     */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    // =========================================================================
    // NOTA SOBRE AUDITORÍA:
    // Los campos createdAt y updatedAt ahora se heredan de la clase Auditable
    // y se gestionan automáticamente por Spring Data JPA Auditing.
    //
    // Ya no es necesario definirlos aquí ni usar @CreationTimestamp/@UpdateTimestamp
    // de Hibernate. Spring Data JPA es más estándar y extensible.
    // =========================================================================
}
