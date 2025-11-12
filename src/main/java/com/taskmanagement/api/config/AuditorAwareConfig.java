package com.taskmanagement.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Configuración para auditoría de usuarios con Spring Data JPA.
 *
 * PROPÓSITO:
 * Esta clase proporciona el usuario actual para los campos @CreatedBy y @LastModifiedBy.
 * Sin esta configuración, esos campos no se rellenarán automáticamente.
 *
 * ESTADO ACTUAL: COMENTADO
 * Esta configuración está comentada porque requiere un sistema de autenticación.
 * Descomentar cuando implementes Spring Security o cualquier sistema de autenticación.
 *
 * CÓMO ACTIVAR:
 * 1. Descomentar la anotación @Configuration
 * 2. Descomentar los campos @CreatedBy y @LastModifiedBy en Auditable.java
 * 3. Implementar la lógica para obtener el usuario actual (ejemplo con Spring Security abajo)
 *
 * EJEMPLO CON SPRING SECURITY:
 * ```java
 * @Override
 * public Optional<String> getCurrentAuditor() {
 *     SecurityContext context = SecurityContextHolder.getContext();
 *     Authentication authentication = context.getAuthentication();
 *
 *     if (authentication == null || !authentication.isAuthenticated()) {
 *         return Optional.empty();
 *     }
 *
 *     return Optional.of(authentication.getName());
 * }
 * ```
 */
// @Configuration  // Descomentar cuando tengas autenticación
public class AuditorAwareConfig {

    /**
     * Bean que proporciona el auditor actual (usuario).
     *
     * @return AuditorAware que retorna el identificador del usuario actual
     */
    // @Bean  // Descomentar cuando tengas autenticación
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    /**
     * Implementación de AuditorAware que retorna el usuario actual.
     *
     * VERSIÓN SIMPLE (sin autenticación):
     * Retorna "system" como usuario por defecto.
     *
     * PRODUCCIÓN:
     * Reemplazar con lógica real que obtenga el usuario autenticado
     * desde Spring Security, JWT, sesión, etc.
     */
    private static class AuditorAwareImpl implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            // OPCIÓN 1: Sin autenticación (desarrollo)
            // Retornar un usuario por defecto
            return Optional.of("system");

            // OPCIÓN 2: Con Spring Security (descomentar cuando lo implementes)
            // return Optional.ofNullable(SecurityContextHolder.getContext())
            //         .map(SecurityContext::getAuthentication)
            //         .filter(Authentication::isAuthenticated)
            //         .map(Authentication::getName);

            // OPCIÓN 3: Con JWT o token custom
            // Implementar lógica para extraer el usuario del token
        }
    }
}
