package com.taskmanagement.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad para registrar auditoría de operaciones críticas del sistema.
 *
 * PROPÓSITO:
 * ==========
 *
 * Persiste en base de datos un registro completo de todas las operaciones
 * auditables del sistema, permitiendo:
 *
 * - Trazabilidad completa de cambios
 * - Cumplimiento normativo (GDPR, SOX, HIPAA, etc.)
 * - Detección de fraudes y anomalías
 * - Análisis forense en caso de incidentes de seguridad
 * - Reportes de actividad de usuarios
 *
 * INFORMACIÓN CAPTURADA:
 * =====================
 *
 * - Quién: Usuario que realizó la acción
 * - Qué: Acción realizada (CREATE, UPDATE, DELETE, etc.)
 * - Cuándo: Timestamp exacto de la operación
 * - Dónde: Método y clase que ejecutaron la acción
 * - Cómo: Resultado de la operación (éxito/fallo)
 * - Por qué: Descripción adicional (opcional)
 *
 * RENDIMIENTO:
 * ============
 *
 * - Índices en: username, action, timestamp (para búsquedas frecuentes)
 * - Particionamiento por fecha para grandes volúmenes
 * - Considerar auditoría asíncrona para operaciones críticas de rendimiento
 *
 * RETENCIÓN DE DATOS:
 * ==================
 *
 * Configurar políticas de retención según requisitos legales:
 * - Típicamente: 1-7 años
 * - Archivar datos antiguos en almacenamiento frío
 * - Job periódico para purgar datos expirados
 *
 * @see com.taskmanagement.api.aspect.AuditAspect
 * @see com.taskmanagement.api.aspect.Auditable
 */
@Entity
@Table(
    name = "audit_log",
    indexes = {
        @Index(name = "idx_audit_username", columnList = "username"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_status", columnList = "status"),
        @Index(name = "idx_audit_resource", columnList = "resource")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    /**
     * ID único del registro de auditoría.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username del usuario que realizó la acción.
     *
     * Obtenido del SecurityContext de Spring Security.
     * Puede ser "ANONYMOUS" si la acción fue realizada sin autenticación.
     */
    @Column(nullable = false, length = 100)
    private String username;

    /**
     * Acción realizada.
     *
     * Ejemplos:
     * - CREATE_TASK
     * - UPDATE_TASK
     * - DELETE_TASK
     * - LOGIN
     * - LOGOUT
     * - CHANGE_PASSWORD
     */
    @Column(nullable = false, length = 100)
    private String action;

    /**
     * Tipo de recurso afectado.
     *
     * Ejemplos: Task, User, Configuration
     */
    @Column(length = 100)
    private String resource;

    /**
     * ID del recurso afectado (opcional).
     *
     * Por ejemplo, el ID de la tarea que se modificó.
     */
    @Column(name = "resource_id")
    private Long resourceId;

    /**
     * Descripción adicional de la operación.
     */
    @Column(length = 500)
    private String description;

    /**
     * Clase que ejecutó la acción.
     */
    @Column(name = "class_name", length = 255)
    private String className;

    /**
     * Método que ejecutó la acción.
     */
    @Column(name = "method_name", length = 100)
    private String methodName;

    /**
     * Parámetros del método (JSON o texto).
     *
     * IMPORTANTE: No almacenar información sensible (contraseñas, tokens).
     */
    @Column(columnDefinition = "TEXT")
    private String parameters;

    /**
     * Resultado de la operación: SUCCESS o FAILURE.
     */
    @Column(nullable = false, length = 20)
    private String status;

    /**
     * Mensaje de error si la operación falló.
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * Tipo de excepción si hubo error.
     */
    @Column(name = "exception_type", length = 255)
    private String exceptionType;

    /**
     * Duración de la operación en milisegundos.
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * Timestamp de cuando se realizó la operación.
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * IP del cliente (opcional - para auditoría de seguridad avanzada).
     *
     * Útil para detectar accesos sospechosos o desde ubicaciones inusuales.
     */
    @Column(name = "client_ip", length = 45)
    private String clientIp;

    /**
     * User Agent del cliente (opcional).
     *
     * Útil para detectar bots o herramientas automatizadas.
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // =========================================================================
    // MÉTODOS DE UTILIDAD
    // =========================================================================

    /**
     * Indica si la operación fue exitosa.
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }

    /**
     * Indica si la operación falló.
     */
    public boolean isFailure() {
        return "FAILURE".equals(status);
    }
}
