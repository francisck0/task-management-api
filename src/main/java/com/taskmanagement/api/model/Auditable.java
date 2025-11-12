package com.taskmanagement.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Clase base abstracta para auditoría automática de entidades.
 *
 * PATRÓN: Herencia de clase base con auditoría común
 * Todas las entidades que hereden de esta clase tendrán automáticamente
 * campos de auditoría sin necesidad de duplicar código.
 *
 * Anotaciones clave:
 * @MappedSuperclass: Indica que esta clase no se mapea a una tabla propia,
 *                    pero sus campos se heredan en las entidades hijas.
 * @EntityListeners: Define el listener que se ejecutará en eventos JPA.
 *                   AuditingEntityListener es el listener de Spring Data JPA
 *                   que rellena automáticamente los campos de auditoría.
 *
 * Campos de auditoría:
 * - createdDate: Fecha y hora de creación (automática)
 * - lastModifiedDate: Fecha y hora de última modificación (automática)
 * - createdBy: Usuario que creó el registro (requiere configuración adicional)
 * - lastModifiedBy: Usuario que modificó el registro (requiere configuración adicional)
 *
 * Para usar:
 * 1. Habilitar auditoría con @EnableJpaAuditing en la clase principal
 * 2. Heredar de esta clase: public class Task extends Auditable { ... }
 * 3. (Opcional) Implementar AuditorAware para auditoría de usuarios
 *
 * Ventajas:
 * - Reutilización: Un solo lugar para definir auditoría
 * - Automático: Spring gestiona los campos sin código manual
 * - Trazabilidad: Saber cuándo y quién modificó cada registro
 * - Estándar: Sigue las mejores prácticas de Spring Data JPA
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class Auditable {

    /**
     * Fecha y hora de creación del registro.
     *
     * @CreatedDate: Spring Data JPA asigna automáticamente la fecha de creación
     *               cuando se persiste la entidad por primera vez.
     *
     * updatable = false: Previene que este campo se modifique después de crearse.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de la última actualización.
     *
     * @LastModifiedDate: Spring Data JPA actualiza automáticamente este campo
     *                    cada vez que se modifica la entidad.
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Usuario que creó el registro (opcional).
     *
     * @CreatedBy: Spring Data JPA asigna el usuario que creó el registro.
     *             Requiere implementar AuditorAware<String> para obtener el usuario actual.
     *
     * Comentado por defecto. Descomentar cuando se implemente autenticación.
     */
    // @CreatedBy
    // @Column(name = "created_by", updatable = false)
    // private String createdBy;

    /**
     * Usuario que modificó el registro por última vez (opcional).
     *
     * @LastModifiedBy: Spring Data JPA asigna el usuario que modificó el registro.
     *                  Requiere implementar AuditorAware<String> para obtener el usuario actual.
     *
     * Comentado por defecto. Descomentar cuando se implemente autenticación.
     */
    // @LastModifiedBy
    // @Column(name = "last_modified_by")
    // private String lastModifiedBy;
}
