package com.taskmanagement.api.repository;

import com.taskmanagement.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad User.
 *
 * Proporciona métodos para consultar y gestionar usuarios en la base de datos.
 * Spring Security usa este repositorio para cargar usuarios durante la autenticación.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su nombre de usuario
     *
     * Este método es usado por Spring Security para autenticación
     *
     * @param username Nombre de usuario
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findByUsername(String username);

    /**
     * Busca un usuario por su email
     *
     * @param email Email del usuario
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el username especificado
     *
     * @param username Nombre de usuario
     * @return true si existe, false si no
     */
    boolean existsByUsername(String username);

    /**
     * Verifica si existe un usuario con el email especificado
     *
     * @param email Email del usuario
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
}
