package com.taskmanagement.api.controller;

import com.taskmanagement.api.constant.ApiVersion;
import com.taskmanagement.api.model.AuditLog;
import com.taskmanagement.api.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controlador REST para consultar registros de auditoría.
 *
 * PROPÓSITO:
 * ==========
 *
 * Provee endpoints REST para que administradores consulten:
 * - Historial completo de operaciones
 * - Operaciones por usuario
 * - Operaciones por tipo de acción
 * - Operaciones en rango de fechas
 * - Estadísticas de auditoría
 * - Detección de actividad sospechosa
 *
 * SEGURIDAD:
 * ==========
 *
 * - Todos los endpoints requieren autenticación (JWT)
 * - Todos los endpoints requieren rol ADMIN
 * - Los registros de auditoría son de solo lectura (no hay endpoints de modificación/eliminación)
 *
 * PAGINACIÓN:
 * ===========
 *
 * Todos los endpoints que retornan listas soportan paginación para manejar grandes volúmenes:
 * - page: número de página (default: 0)
 * - size: tamaño de página (default: 20)
 * - sort: campo de ordenamiento (default: timestamp,desc)
 *
 * CASOS DE USO:
 * =============
 *
 * 1. Auditoría de Seguridad:
 *    GET /api/v1/audit?username=john.doe&startDate=2025-01-01
 *
 * 2. Investigación de Incidentes:
 *    GET /api/v1/audit/failures?hours=24
 *
 * 3. Cumplimiento Normativo:
 *    GET /api/v1/audit/resource/Task/123
 *
 * 4. Reportes Ejecutivos:
 *    GET /api/v1/audit/statistics
 *
 * @see AuditLogService
 * @see AuditLog
 * @see com.taskmanagement.api.aspect.AuditAspect
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/audit")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Auditoría", description = "Endpoints para consultar registros de auditoría del sistema")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditLogController {

    private final AuditLogService auditLogService;

    // =========================================================================
    // CONSULTAS GENERALES
    // =========================================================================

    /**
     * Obtiene todos los registros de auditoría (paginados).
     *
     * @param page número de página (default: 0)
     * @param size tamaño de página (default: 20, max: 100)
     * @return página de registros de auditoría
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener todos los registros de auditoría",
        description = "Retorna una lista paginada de todos los registros de auditoría del sistema. " +
                     "Requiere rol ADMIN. Por defecto se ordenan por timestamp descendente (más recientes primero)."
    )
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(
            @Parameter(description = "Número de página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamaño de página (máximo 100)")
            @RequestParam(defaultValue = "20") int size) {

        // Validar tamaño máximo de página
        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogs = auditLogService.findAll(pageable);

        log.info("Consulta de auditoría: página {}, tamaño {}, total elementos: {}",
                page, size, auditLogs.getTotalElements());

        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Obtiene registros de auditoría de un usuario específico.
     *
     * @param username nombre del usuario
     * @param page número de página
     * @param size tamaño de página
     * @return página de registros de auditoría
     */
    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener auditoría de un usuario",
        description = "Retorna todos los registros de auditoría de un usuario específico. " +
                     "Útil para investigar actividad de un usuario en particular."
    )
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUsername(
            @Parameter(description = "Nombre del usuario a consultar")
            @PathVariable String username,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogs = auditLogService.findByUsername(username, pageable);

        log.info("Consulta de auditoría para usuario '{}': {} registros encontrados",
                username, auditLogs.getTotalElements());

        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Obtiene registros de auditoría por tipo de acción.
     *
     * @param action tipo de acción (CREATE, UPDATE, DELETE, etc.)
     * @param page número de página
     * @param size tamaño de página
     * @return página de registros de auditoría
     */
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener auditoría por tipo de acción",
        description = "Retorna todos los registros de un tipo de acción específico. " +
                     "Ejemplos: CREATE_TASK, UPDATE_TASK, DELETE_TASK, LOGIN, etc."
    )
    public ResponseEntity<Page<AuditLog>> getAuditLogsByAction(
            @Parameter(description = "Tipo de acción (CREATE, UPDATE, DELETE, etc.)")
            @PathVariable String action,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogs = auditLogService.findByAction(action, pageable);

        log.info("Consulta de auditoría para acción '{}': {} registros encontrados",
                action, auditLogs.getTotalElements());

        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Obtiene registros de auditoría en un rango de fechas.
     *
     * @param startDate fecha de inicio (formato: yyyy-MM-dd'T'HH:mm:ss)
     * @param endDate fecha de fin
     * @param page número de página
     * @param size tamaño de página
     * @return página de registros de auditoría
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener auditoría en rango de fechas",
        description = "Retorna registros de auditoría entre dos fechas. " +
                     "Formato de fecha: yyyy-MM-dd'T'HH:mm:ss (ejemplo: 2025-01-15T10:30:00)"
    )
    public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
            @Parameter(description = "Fecha de inicio (yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Fecha de fin (yyyy-MM-dd'T'HH:mm:ss)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogs = auditLogService.findByDateRange(startDate, endDate, pageable);

        log.info("Consulta de auditoría entre {} y {}: {} registros encontrados",
                startDate, endDate, auditLogs.getTotalElements());

        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Obtiene el historial completo de un recurso específico.
     *
     * @param resource tipo de recurso (Task, User, etc.)
     * @param resourceId ID del recurso
     * @param page número de página
     * @param size tamaño de página
     * @return página de registros de auditoría
     */
    @GetMapping("/resource/{resource}/{resourceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener historial de un recurso específico",
        description = "Retorna todos los cambios realizados sobre una entidad específica. " +
                     "Útil para rastrear quién modificó qué y cuándo."
    )
    public ResponseEntity<Page<AuditLog>> getResourceHistory(
            @Parameter(description = "Tipo de recurso (Task, User, etc.)")
            @PathVariable String resource,

            @Parameter(description = "ID del recurso")
            @PathVariable Long resourceId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> auditLogs = auditLogService.findResourceHistory(resource, resourceId, pageable);

        log.info("Consulta de historial para {} con ID {}: {} registros encontrados",
                resource, resourceId, auditLogs.getTotalElements());

        return ResponseEntity.ok(auditLogs);
    }

    // =========================================================================
    // OPERACIONES FALLIDAS Y ALERTAS
    // =========================================================================

    /**
     * Obtiene operaciones fallidas.
     *
     * @param page número de página
     * @param size tamaño de página
     * @return página de registros de auditoría fallidos
     */
    @GetMapping("/failures")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener operaciones fallidas",
        description = "Retorna todos los registros de auditoría con estado FAILURE. " +
                     "Útil para detectar intentos de acceso no autorizado o problemas del sistema."
    )
    public ResponseEntity<Page<AuditLog>> getFailures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<AuditLog> failures = auditLogService.findFailures(pageable);

        log.info("Consulta de operaciones fallidas: {} registros encontrados", failures.getTotalElements());

        return ResponseEntity.ok(failures);
    }

    /**
     * Cuenta operaciones fallidas recientes.
     *
     * @param hours número de horas hacia atrás (default: 24)
     * @return número de operaciones fallidas
     */
    @GetMapping("/failures/count")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Contar operaciones fallidas recientes",
        description = "Retorna el número de operaciones fallidas en las últimas N horas. " +
                     "Útil para monitoreo de salud del sistema."
    )
    public ResponseEntity<Map<String, Object>> countRecentFailures(
            @Parameter(description = "Número de horas hacia atrás (default: 24)")
            @RequestParam(defaultValue = "24") int hours) {

        long count = auditLogService.countRecentFailures(hours);

        Map<String, Object> response = Map.of(
                "hours", hours,
                "failureCount", count,
                "timestamp", LocalDateTime.now()
        );

        log.info("Operaciones fallidas en las últimas {} horas: {}", hours, count);

        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // ESTADÍSTICAS Y REPORTES
    // =========================================================================

    /**
     * Obtiene estadísticas generales de auditoría.
     *
     * @return mapa con estadísticas
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener estadísticas de auditoría",
        description = "Retorna estadísticas generales del sistema de auditoría: " +
                     "total de operaciones, tasa de éxito, acciones más frecuentes, usuarios más activos, etc."
    )
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = auditLogService.getStatistics();

        log.info("Consulta de estadísticas de auditoría");

        return ResponseEntity.ok(stats);
    }

    /**
     * Obtiene las acciones más frecuentes.
     *
     * @param limit número máximo de resultados (default: 10)
     * @return mapa con acciones y frecuencias
     */
    @GetMapping("/statistics/top-actions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener acciones más frecuentes",
        description = "Retorna las operaciones más frecuentes del sistema. " +
                     "Útil para análisis de uso y optimización."
    )
    public ResponseEntity<Map<String, Long>> getMostFrequentActions(
            @Parameter(description = "Número máximo de resultados (default: 10)")
            @RequestParam(defaultValue = "10") int limit) {

        if (limit > 50) limit = 50;  // Limitar a máximo 50

        Map<String, Long> topActions = auditLogService.getMostFrequentActions(limit);

        log.info("Consulta de top {} acciones más frecuentes", limit);

        return ResponseEntity.ok(topActions);
    }

    /**
     * Obtiene los usuarios más activos.
     *
     * @param limit número máximo de resultados (default: 10)
     * @return mapa con usuarios y conteos de acciones
     */
    @GetMapping("/statistics/top-users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Obtener usuarios más activos",
        description = "Retorna los usuarios con mayor actividad en el sistema. " +
                     "Útil para identificar power users y analizar adopción."
    )
    public ResponseEntity<Map<String, Long>> getMostActiveUsers(
            @Parameter(description = "Número máximo de resultados (default: 10)")
            @RequestParam(defaultValue = "10") int limit) {

        if (limit > 50) limit = 50;

        Map<String, Long> topUsers = auditLogService.getMostActiveUsers(limit);

        log.info("Consulta de top {} usuarios más activos", limit);

        return ResponseEntity.ok(topUsers);
    }

    // =========================================================================
    // DETECCIÓN DE ACTIVIDAD SOSPECHOSA
    // =========================================================================

    /**
     * Detecta actividad sospechosa.
     *
     * @param hours número de horas hacia atrás a analizar (default: 24)
     * @return alertas de actividad sospechosa
     */
    @GetMapping("/suspicious-activity")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Detectar actividad sospechosa",
        description = "Analiza los registros de auditoría en busca de patrones sospechosos: " +
                     "múltiples fallos, actividad inusualmente alta, operaciones DELETE masivas, etc."
    )
    public ResponseEntity<Map<String, Object>> detectSuspiciousActivity(
            @Parameter(description = "Número de horas hacia atrás a analizar (default: 24)")
            @RequestParam(defaultValue = "24") int hours) {

        Map<String, Object> alerts = auditLogService.detectSuspiciousActivity(hours);

        log.info("Análisis de actividad sospechosa en las últimas {} horas: {} alertas",
                hours, alerts.size());

        return ResponseEntity.ok(alerts);
    }
}
