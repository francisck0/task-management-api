package com.taskmanagement.api.aspect;

import com.taskmanagement.api.model.AuditLog;
import com.taskmanagement.api.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * Aspecto de AOP para auditoría automática de operaciones críticas.
 *
 * PROPÓSITO:
 * ==========
 *
 * Registra automáticamente operaciones críticas del sistema para:
 * - Cumplimiento normativo (compliance)
 * - Trazabilidad de cambios
 * - Seguridad y detección de fraudes
 * - Análisis forense en caso de incidentes
 *
 * FUNCIONAMIENTO:
 * ==============
 *
 * 1. Detecta métodos anotados con @Auditable
 * 2. Captura información del contexto:
 *    - Usuario que realiza la acción (del SecurityContext)
 *    - Timestamp de la operación
 *    - Acción y recurso afectado
 *    - Parámetros y resultado (si está configurado)
 * 3. Registra toda esta información en logs estructurados
 *
 * USO:
 * ====
 *
 * <pre>
 * {@code
 * @Auditable(
 *     action = "CREATE_TASK",
 *     resource = "Task",
 *     description = "Usuario crea nueva tarea",
 *     logParameters = true,
 *     logResult = true
 * )
 * public TaskResponseDto createTask(TaskRequestDto dto) {
 *     // código...
 * }
 * }
 * </pre>
 *
 * SALIDA EN LOGS:
 * ==============
 *
 * ```
 * [AUDIT] 2025-01-15T10:30:45 | User: john.doe@example.com | Action: CREATE_TASK | Resource: Task | Description: Usuario crea nueva tarea | Status: SUCCESS | Duration: 125ms
 * ```
 *
 * MEJORAS FUTURAS:
 * ===============
 *
 * - Persistir auditoría en base de datos (tabla audit_log)
 * - Enviar eventos a sistema de análisis centralizado (ELK, Splunk)
 * - Integración con SIEM para alertas de seguridad
 * - Soporte para auditoría asíncrona (no bloquear la operación)
 *
 * @see Auditable
 * @see LoggingAspect
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Advice AROUND para procesar anotación @Auditable.
     *
     * Se ejecuta alrededor de cualquier método anotado con @Auditable,
     * capturando toda la información relevante para auditoría.
     *
     * @param joinPoint información del método con control de ejecución
     * @param auditable anotación con configuración de auditoría
     * @return resultado del método auditado
     * @throws Throwable si el método lanza excepción
     */
    @Around("@annotation(auditable)")
    public Object auditMethodExecution(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        // =====================================================================
        // 1. CAPTURAR INFORMACIÓN DEL CONTEXTO
        // =====================================================================

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Información de la anotación
        String action = auditable.action();
        String resource = auditable.resource();
        String description = auditable.description();
        boolean logParams = auditable.logParameters();
        boolean logResult = auditable.logResult();

        // =====================================================================
        // 2. LOGGING PRE-EJECUCIÓN
        // =====================================================================

        StringBuilder auditLog = new StringBuilder();
        auditLog.append("\n╔════════════════════════════════════════════════════════════════════════════╗\n");
        auditLog.append("║                           AUDIT LOG - START                                ║\n");
        auditLog.append("╠════════════════════════════════════════════════════════════════════════════╣\n");
        auditLog.append(String.format("║ Timestamp:    %s%n", padRight(timestamp, 60)));
        auditLog.append(String.format("║ User:         %s%n", padRight(username, 60)));
        auditLog.append(String.format("║ Action:       %s%n", padRight(action, 60)));

        if (!resource.isEmpty()) {
            auditLog.append(String.format("║ Resource:     %s%n", padRight(resource, 60)));
        }

        if (!description.isEmpty()) {
            auditLog.append(String.format("║ Description:  %s%n", padRight(description, 60)));
        }

        auditLog.append(String.format("║ Method:       %s.%s()%n", className, methodName));

        if (logParams) {
            Object[] args = joinPoint.getArgs();
            auditLog.append(String.format("║ Parameters:   %s%n", sanitizeForLogging(Arrays.toString(args))));
        }

        auditLog.append("╚════════════════════════════════════════════════════════════════════════════╝");

        log.info(auditLog.toString());

        // =====================================================================
        // 3. EJECUTAR MÉTODO Y CAPTURAR RESULTADO/ERROR
        // =====================================================================

        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        String status = "UNKNOWN";  // Inicializado por defecto, se sobrescribirá en try/catch

        try {
            result = joinPoint.proceed();
            status = "SUCCESS";
            return result;

        } catch (Throwable throwable) {
            error = throwable;
            status = "FAILURE";
            throw throwable;

        } finally {
            // =================================================================
            // 4. LOGGING POST-EJECUCIÓN
            // =================================================================

            long duration = System.currentTimeMillis() - startTime;

            StringBuilder postAuditLog = new StringBuilder();
            postAuditLog.append("\n╔════════════════════════════════════════════════════════════════════════════╗\n");
            postAuditLog.append("║                           AUDIT LOG - END                                  ║\n");
            postAuditLog.append("╠════════════════════════════════════════════════════════════════════════════╣\n");
            postAuditLog.append(String.format("║ Action:       %s%n", padRight(action, 60)));
            postAuditLog.append(String.format("║ Status:       %s%n", padRight(status, 60)));
            postAuditLog.append(String.format("║ Duration:     %d ms%n", duration));

            if (error != null) {
                postAuditLog.append(String.format("║ Error:        %s: %s%n",
                        error.getClass().getSimpleName(),
                        error.getMessage()));
            }

            if (logResult && result != null && status.equals("SUCCESS")) {
                postAuditLog.append(String.format("║ Result:       %s%n", sanitizeForLogging(result.toString())));
            }

            postAuditLog.append("╚════════════════════════════════════════════════════════════════════════════╝");

            if ("SUCCESS".equals(status)) {
                log.info(postAuditLog.toString());
            } else {
                log.error(postAuditLog.toString());
            }

            // =================================================================
            // 5. LOGGING ESTRUCTURADO (Para parsers automáticos)
            // =================================================================

            log.info("[AUDIT] {} | User: {} | Action: {} | Resource: {} | Status: {} | Duration: {}ms",
                    timestamp,
                    username,
                    action,
                    resource.isEmpty() ? "N/A" : resource,
                    status,
                    duration);

            // =================================================================
            // 6. PERSISTIR EN BASE DE DATOS
            // =================================================================

            try {
                AuditLog auditLogEntity = AuditLog.builder()
                        .username(username)
                        .action(action)
                        .resource(resource.isEmpty() ? null : resource)
                        .description(description.isEmpty() ? null : description)
                        .className(className)
                        .methodName(methodName)
                        .parameters(logParams ? sanitizeForLogging(Arrays.toString(joinPoint.getArgs())) : null)
                        .status(status)
                        .errorMessage(error != null ? error.getMessage() : null)
                        .exceptionType(error != null ? error.getClass().getSimpleName() : null)
                        .durationMs(duration)
                        .timestamp(LocalDateTime.now())
                        .build();

                auditLogService.save(auditLogEntity);

                log.trace("Registro de auditoría persistido en BD: ID generado automáticamente");

            } catch (Exception e) {
                // IMPORTANTE: No fallar la operación principal si falla el logging de auditoría
                log.error("ERROR al persistir auditoría en BD (la operación principal continuó exitosamente): {}",
                        e.getMessage(), e);
            }
        }
    }

    /**
     * Obtiene el username del usuario autenticado actual.
     *
     * @return username del usuario autenticado, o "ANONYMOUS" si no hay sesión
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();

                // Filtrar usuarios especiales de Spring Security
                if (!"anonymousUser".equals(username)) {
                    return username;
                }
            }
        } catch (Exception e) {
            log.warn("Error obteniendo usuario autenticado para auditoría: {}", e.getMessage());
        }

        return "ANONYMOUS";
    }

    /**
     * Sanitiza información sensible antes de logging.
     *
     * IMPORTANTE: Nunca loggear:
     * - Contraseñas
     * - Tokens de autenticación
     * - Datos de tarjetas de crédito
     * - Información personal sensible (PII)
     *
     * @param text texto a sanitizar
     * @return texto sanitizado
     */
    private String sanitizeForLogging(String text) {
        if (text == null) {
            return "null";
        }

        // Limitar longitud para evitar logs gigantes
        if (text.length() > 500) {
            text = text.substring(0, 497) + "...";
        }

        // Ocultar posibles contraseñas
        text = text.replaceAll("(?i)(password|passwd|pwd)\\s*[=:]\\s*[^,\\s}]+",
                "$1=***REDACTED***");

        // Ocultar posibles tokens
        text = text.replaceAll("(?i)(token|bearer|authorization)\\s*[=:]\\s*[^,\\s}]+",
                "$1=***REDACTED***");

        return text;
    }

    /**
     * Rellena un string con espacios a la derecha hasta alcanzar la longitud deseada.
     *
     * @param text texto original
     * @param length longitud deseada
     * @return texto rellenado
     */
    private String padRight(String text, int length) {
        if (text.length() >= length) {
            return text.substring(0, length);
        }
        return String.format("%-" + length + "s", text);
    }
}
