package com.taskmanagement.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entidad JPA que representa un usuario en el sistema.
 *
 * Implementa UserDetails de Spring Security para integrarse
 * con el sistema de autenticación y autorización.
 *
 * PATRÓN: Esta clase combina:
 * - Entidad JPA (persistencia en BD)
 * - UserDetails (integración con Spring Security)
 *
 * AUDITORÍA: Hereda de Auditable para tener campos de creación/actualización automáticos
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class User extends Auditable implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre de usuario único
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Email del usuario (único)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Contraseña encriptada (NUNCA guardar en texto plano)
     */
    @Column(nullable = false)
    private String password;

    /**
     * Nombre completo del usuario
     */
    @Column(length = 100)
    private String fullName;

    /**
     * Indica si la cuenta está activa
     */
    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Indica si la cuenta no está bloqueada
     */
    @Column(nullable = false)
    private boolean accountNonLocked = true;

    /**
     * Roles del usuario
     *
     * EAGER: Carga los roles inmediatamente (necesario para autenticación)
     * CascadeType.MERGE: Al actualizar un usuario, también actualiza sus roles
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    private Set<Role> roles = new HashSet<>();

    // =========================================================================
    // IMPLEMENTACIÓN DE UserDetails (requerido por Spring Security)
    // =========================================================================

    /**
     * Retorna los permisos/autoridades del usuario
     * Convierte los roles en GrantedAuthority que Spring Security entiende
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList());
    }

    /**
     * Indica si la cuenta no ha expirado
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indica si la cuenta no está bloqueada
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * Indica si las credenciales no han expirado
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indica si la cuenta está habilitada
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
