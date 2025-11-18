package com.taskmanagement.api.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para marcar métodos que requieren auditoría.
 *
 * PROPÓSITO:
 * Permite registrar automáticamente operaciones críticas del sistema
 * como creación, actualización y eliminación de recursos.
 *
 * USO:
 * <pre>
 * {@code
 * @Auditable(action = "CREATE_TASK", resource = "Task")
 * public TaskResponseDto createTask(TaskRequestDto dto) {
 *     // código...
 * }
 * }
 * </pre>
 *
 * PROCESAMIENTO:
 * Esta anotación es procesada por {@link AuditAspect}, que automáticamente:
 * - Registra en logs la operación realizada
 * - Captura usuario que realizó la acción (del SecurityContext)
 * - Registra timestamp
 * - Captura parámetros y resultado (opcional)
 *
 * CASOS DE USO:
 * - Operaciones CRUD críticas
 * - Cambios de configuración
 * - Acciones administrativas
 * - Operaciones de seguridad (login, cambio de contraseña, etc.)
 *
 * @see AuditAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Acción que se está auditando.
     *
     * Ejemplos: "CREATE", "UPDATE", "DELETE", "LOGIN", "EXPORT_DATA"
     *
     * @return nombre de la acción
     */
    String action();

    /**
     * Tipo de recurso afectado.
     *
     * Ejemplos: "Task", "User", "Configuration"
     *
     * @return tipo de recurso
     */
    String resource() default "";

    /**
     * Descripción adicional de la operación (opcional).
     *
     * @return descripción de la operación
     */
    String description() default "";

    /**
     * Si se debe registrar los parámetros del método.
     *
     * ADVERTENCIA: Solo activar si los parámetros NO contienen datos sensibles
     * (contraseñas, tokens, etc.)
     *
     * @return true si se deben registrar los parámetros
     */
    boolean logParameters() default false;

    /**
     * Si se debe registrar el resultado del método.
     *
     * @return true si se debe registrar el resultado
     */
    boolean logResult() default false;
}
