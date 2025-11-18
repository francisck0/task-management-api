package com.taskmanagement.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementación de AuditorAware para Spring Data JPA Auditing.
 *
 * PROPÓSITO:
 * Proporciona automáticamente el usuario actual para los campos de auditoría
 * @CreatedBy y @LastModifiedBy en las entidades que heredan de Auditable.
 *
 * CÓMO FUNCIONA:
 * 1. Spring Data JPA llama a getCurrentAuditor() cuando se crea o modifica una entidad
 * 2. Este método obtiene el usuario autenticado del SecurityContext
 * 3. Spring Data JPA asigna automáticamente el username a los campos correspondientes:
 *    - @CreatedBy → Solo al crear (no se actualiza después)
 *    - @LastModifiedBy → En cada actualización
 *
 * FLUJO COMPLETO:
 * 1. Usuario hace login → JWT generado con username
 * 2. Usuario hace petición con JWT → Spring Security valida y carga User en SecurityContext
 * 3. Usuario crea/modifica Task → JPA intercepta la operación
 * 4. JPA llama a getCurrentAuditor() → Retorna "frank"
 * 5. JPA asigna "frank" a task.createdBy o task.lastModifiedBy
 * 6. JPA guarda en BD: INSERT/UPDATE tasks SET created_by = 'frank'
 *
 * EJEMPLO DE USO:
 * ```java
 * // Usuario "frank" autenticado crea una tarea
 * Task task = new Task();
 * task.setTitle("Nueva tarea");
 * taskRepository.save(task);
 * // Resultado en BD:
 * // created_by = "frank"
 * // last_modified_by = "frank"
 * // created_at = 2025-11-16 10:30:00
 * // updated_at = 2025-11-16 10:30:00
 *
 * // Usuario "maria" modifica la tarea
 * task.setTitle("Tarea modificada");
 * taskRepository.save(task);
 * // Resultado en BD:
 * // created_by = "frank"        (NO cambia - creador original)
 * // last_modified_by = "maria"  (Cambia - último editor)
 * // created_at = 2025-11-16 10:30:00 (NO cambia)
 * // updated_at = 2025-11-16 11:45:00 (Cambia - última modificación)
 * ```
 *
 * CASOS ESPECIALES:
 *
 * 1. Usuario no autenticado:
 *    - Retorna Optional.empty()
 *    - JPA deja los campos en NULL
 *    - Útil para operaciones del sistema (migrations, seeds, etc.)
 *
 * 2. Operaciones en background/scheduled:
 *    - No hay SecurityContext
 *    - Retorna Optional.empty()
 *    - Alternativa: Configurar un usuario "SYSTEM" manualmente
 *
 * 3. Testing:
 *    - Mock SecurityContext en tests
 *    - O usar @WithMockUser de Spring Security Test
 *
 * CONFIGURACIÓN REQUERIDA:
 * En clase @Configuration con @EnableJpaAuditing:
 * ```java
 * @EnableJpaAuditing(auditorAwareRef = "auditorAware")
 * ```
 *
 * VENTAJAS DE ESTE ENFOQUE:
 * - Automático: No requiere código manual en cada save()
 * - Consistente: Siempre se registra el usuario correctamente
 * - Auditable: Trazabilidad completa de cambios
 * - Seguro: Usa el usuario del SecurityContext (no se puede falsificar)
 * - Compliance: Útil para GDPR, SOC2, ISO 27001
 *
 * @see com.taskmanagement.api.model.Auditable
 * @see org.springframework.data.jpa.repository.config.EnableJpaAuditing
 */
@Component("auditorAware")
@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {

    /**
     * Obtiene el username del usuario actualmente autenticado.
     *
     * IMPLEMENTACIÓN:
     * 1. Obtiene el Authentication del SecurityContext
     * 2. Verifica que existe y está autenticado
     * 3. Extrae el principal (usuario autenticado)
     * 4. Retorna el username
     *
     * CASOS:
     * - Usuario autenticado: Optional.of("username")
     * - No autenticado: Optional.empty()
     * - Principal no es UserDetails: Optional.empty()
     *
     * @return Optional con el username del usuario autenticado, o vacío si no hay usuario
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        // Obtener el Authentication del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verificar que existe autenticación y está activa
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("No hay usuario autenticado. Campos de auditoría se dejarán NULL.");
            return Optional.empty();
        }

        // Obtener el principal (usuario autenticado)
        Object principal = authentication.getPrincipal();

        // Caso 1: Principal es un String (username directamente)
        // Ocurre en algunos flujos de autenticación
        if (principal instanceof String) {
            String username = (String) principal;

            // Ignorar usuario "anonymousUser" (usuario anónimo de Spring Security)
            if ("anonymousUser".equals(username)) {
                log.debug("Usuario anónimo detectado. Campos de auditoría se dejarán NULL.");
                return Optional.empty();
            }

            log.debug("Auditor detectado (String): {}", username);
            return Optional.of(username);
        }

        // Caso 2: Principal es UserDetails (nuestro User implementa UserDetails)
        // Este es el caso más común en nuestra aplicación
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails =
                (org.springframework.security.core.userdetails.UserDetails) principal;
            String username = userDetails.getUsername();

            log.debug("Auditor detectado (UserDetails): {}", username);
            return Optional.of(username);
        }

        // Caso 3: Principal es de otro tipo (no esperado)
        log.warn("Principal no es String ni UserDetails: {}. Campos de auditoría se dejarán NULL.",
                principal.getClass().getName());
        return Optional.empty();
    }
}
