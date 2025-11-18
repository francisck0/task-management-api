package com.taskmanagement.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
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
 *
 * SOFT DELETE: Esta entidad implementa eliminación lógica (soft delete).
 * - Cuando se "elimina" un usuario, solo se marca con deletedAt (timestamp)
 * - Los usuarios eliminados NO aparecen en queries normales
 * - Se pueden restaurar cambiando deletedAt a null
 * - Las tareas del usuario NO se eliminan automáticamente
 * - IMPORTANTE: Usuario eliminado = cuenta desactivada, no puede hacer login
 *
 * ÍNDICES DE BASE DE DATOS:
 * Esta tabla define 2 índices para optimizar las consultas de autenticación:
 *
 * 1. idx_user_username (username):
 *    - Índice único automático por @Column(unique=true)
 *    - Usado en: Login (findByUsername), verificación de duplicados
 *    - Impacto: CRÍTICO - Cada login requiere este índice
 *
 * 2. idx_user_email (email):
 *    - Índice único automático por @Column(unique=true)
 *    - Usado en: Búsqueda por email (findByEmail), verificación de duplicados
 *    - Impacto: Mejora significativa en búsquedas y validaciones
 *
 * ÍNDICES ADICIONALES (definidos en schema.sql):
 * - idx_user_enabled: Índice parcial para usuarios activos
 *
 * NOTA: Las columnas con unique=true generan automáticamente índices únicos,
 * por lo que no es necesario definirlos explícitamente en @Index.
 */
@Entity
@Table(name = "users")
@org.hibernate.annotations.Where(clause = "deleted_at IS NULL")
@org.hibernate.annotations.SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
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

    /**
     * Tareas creadas por este usuario
     *
     * @OneToMany: Relación uno-a-muchos (un usuario tiene muchas tareas)
     * mappedBy = "user": Indica que el lado dueño de la relación está en Task.user
     * cascade = CascadeType.ALL: Al eliminar un usuario, elimina sus tareas
     * orphanRemoval = true: Si se elimina una tarea de la lista, se elimina de la BD
     * FetchType.LAZY: No carga las tareas automáticamente (optimización)
     *
     * NOTA: Esta es una relación bidireccional, pero normalmente accedemos
     * a las tareas mediante queries en TaskRepository, no a través de user.getTasks().
     * La mantenemos para tener el modelo completo y permitir operaciones en cascada.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();

    /**
     * Fecha de eliminación lógica (soft delete)
     *
     * SOFT DELETE IMPLEMENTATION:
     * - null: Usuario activo
     * - timestamp: Usuario eliminado lógicamente
     *
     * IMPORTANTE:
     * - Usuario eliminado NO puede hacer login
     * - Las tareas del usuario NO se eliminan (mantienen referencia)
     * - Se puede restaurar la cuenta
     *
     * @Column(name = "deleted_at"): Nombre de columna en BD
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // =========================================================================
    // MÉTODOS HELPER PARA SOFT DELETE
    // =========================================================================

    /**
     * Verifica si el usuario está eliminado (soft deleted)
     *
     * @return true si el usuario fue eliminado lógicamente
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Marca el usuario como eliminado (soft delete)
     * Establece deletedAt al momento actual
     */
    public void markAsDeleted() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Restaura el usuario eliminado (undelete)
     * Establece deletedAt a null
     */
    public void restore() {
        this.deletedAt = null;
    }

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
