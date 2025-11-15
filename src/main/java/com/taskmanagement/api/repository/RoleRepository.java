package com.taskmanagement.api.repository;

import com.taskmanagement.api.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Role.
 *
 * Proporciona métodos para consultar y gestionar roles en la base de datos.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca un rol por su nombre
     *
     * @param name Nombre del rol (ej: "ROLE_USER", "ROLE_ADMIN")
     * @return Optional con el rol si existe, vacío si no
     */
    Optional<Role> findByName(String name);

    /**
     * Verifica si existe un rol con el nombre especificado
     *
     * @param name Nombre del rol
     * @return true si existe, false si no
     */
    boolean existsByName(String name);
}
