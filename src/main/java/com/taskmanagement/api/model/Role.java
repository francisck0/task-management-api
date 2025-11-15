package com.taskmanagement.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA que representa un rol en el sistema de seguridad.
 *
 * Los roles se usan para definir permisos y controlar acceso a recursos.
 * Por ejemplo: ROLE_USER, ROLE_ADMIN, etc.
 *
 * IMPORTANTE: Spring Security espera que los roles empiecen con el prefijo "ROLE_"
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre del rol
     * Ejemplo: ROLE_USER, ROLE_ADMIN
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Constructor de conveniencia para crear roles con solo el nombre
     */
    public Role(String name) {
        this.name = name;
    }
}
