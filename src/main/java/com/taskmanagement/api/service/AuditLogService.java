package com.taskmanagement.api.service;

import com.taskmanagement.api.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Servicio para gestión de registros de auditoría.
 *
 * PROPÓSITO:
 * ==========
 *
 * Capa de servicio que encapsula la lógica de negocio relacionada con auditoría:
 * - Consultas de auditoría
 * - Estadísticas y reportes
 * - Limpieza de registros antiguos
 * - Detección de actividad sospechosa
 *
 * SEPARACIÓN DE RESPONSABILIDADES:
 * ================================
 *
 * - AuditAspect: Captura automáticamente operaciones y persiste en BD
 * - AuditLogService: Provee API de alto nivel para consultar auditoría
 * - AuditLogController: Expone endpoints REST para administradores
 *
 * @see AuditLog
 * @see com.taskmanagement.api.repository.AuditLogRepository
 * @see com.taskmanagement.api.aspect.AuditAspect
 */
public interface AuditLogService {

    /**
     * Guarda un registro de auditoría.
     *
     * NOTA: Este método es llamado automáticamente por AuditAspect.
     * No es necesario llamarlo manualmente en el código de negocio.
     *
     * @param auditLog registro a guardar
     * @return registro guardado con ID generado
     */
    AuditLog save(AuditLog auditLog);

    /**
     * Obtiene todos los registros de auditoría (paginados).
     *
     * @param pageable configuración de paginación y ordenamiento
     * @return página de registros de auditoría
     */
    Page<AuditLog> findAll(Pageable pageable);

    /**
     * Obtiene registros de auditoría de un usuario específico.
     *
     * @param username nombre del usuario
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByUsername(String username, Pageable pageable);

    /**
     * Obtiene registros de auditoría por tipo de acción.
     *
     * @param action tipo de acción (CREATE, UPDATE, DELETE, etc.)
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * Obtiene registros de auditoría por tipo de recurso.
     *
     * @param resource tipo de recurso (Task, User, etc.)
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByResource(String resource, Pageable pageable);

    /**
     * Obtiene registros de auditoría en un rango de fechas.
     *
     * @param startDate fecha de inicio
     * @param endDate fecha de fin
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByDateRange(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Obtiene registros de auditoría de un usuario en un rango de fechas.
     *
     * @param username nombre del usuario
     * @param startDate fecha de inicio
     * @param endDate fecha de fin
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByUsernameAndDateRange(
        String username,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Obtiene operaciones fallidas.
     *
     * @param pageable configuración de paginación
     * @return página de registros de auditoría fallidos
     */
    Page<AuditLog> findFailures(Pageable pageable);

    /**
     * Obtiene el historial completo de cambios de un recurso específico.
     *
     * @param resource tipo de recurso
     * @param resourceId ID del recurso
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findResourceHistory(
        String resource,
        Long resourceId,
        Pageable pageable
    );

    /**
     * Cuenta operaciones fallidas recientes.
     *
     * @param hoursAgo número de horas hacia atrás
     * @return número de operaciones fallidas
     */
    long countRecentFailures(int hoursAgo);

    /**
     * Cuenta operaciones de un usuario en las últimas horas.
     *
     * @param username nombre del usuario
     * @param hoursAgo número de horas hacia atrás
     * @return número de operaciones
     */
    long countUserRecentActions(String username, int hoursAgo);

    /**
     * Obtiene estadísticas generales de auditoría.
     *
     * Incluye:
     * - Total de operaciones
     * - Operaciones exitosas vs fallidas
     * - Acciones más frecuentes
     * - Usuarios más activos
     *
     * @return mapa con estadísticas
     */
    Map<String, Object> getStatistics();

    /**
     * Obtiene las acciones más frecuentes.
     *
     * @param limit número máximo de resultados
     * @return mapa con acciones y sus frecuencias
     */
    Map<String, Long> getMostFrequentActions(int limit);

    /**
     * Obtiene los usuarios más activos.
     *
     * @param limit número máximo de resultados
     * @return mapa con usuarios y sus conteos de acciones
     */
    Map<String, Long> getMostActiveUsers(int limit);

    /**
     * Limpia registros antiguos según política de retención.
     *
     * IMPORTANTE: Configurar según requisitos legales y de compliance.
     *
     * @param daysToKeep número de días a mantener
     * @return número de registros eliminados
     */
    int cleanupOldRecords(int daysToKeep);

    /**
     * Detecta actividad sospechosa.
     *
     * Busca patrones como:
     * - Múltiples intentos fallidos de un usuario
     * - Actividad inusualmente alta de un usuario
     * - Operaciones DELETE masivas
     *
     * @param hoursAgo número de horas hacia atrás a analizar
     * @return lista de alertas de actividad sospechosa
     */
    Map<String, Object> detectSuspiciousActivity(int hoursAgo);
}
