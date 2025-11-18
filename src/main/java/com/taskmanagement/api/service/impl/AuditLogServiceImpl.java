package com.taskmanagement.api.service.impl;

import com.taskmanagement.api.model.AuditLog;
import com.taskmanagement.api.repository.AuditLogRepository;
import com.taskmanagement.api.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de auditoría.
 *
 * @see AuditLogService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    // Umbrales para detección de actividad sospechosa
    private static final int SUSPICIOUS_FAILURES_THRESHOLD = 10;  // Fallos en X horas
    private static final int SUSPICIOUS_ACTIONS_THRESHOLD = 100;  // Acciones de un usuario en X horas

    @Override
    @Transactional
    public AuditLog save(AuditLog auditLog) {
        log.trace("Guardando registro de auditoría: {} - {} - {}",
                auditLog.getUsername(),
                auditLog.getAction(),
                auditLog.getStatus());

        return auditLogRepository.save(auditLog);
    }

    @Override
    public Page<AuditLog> findAll(Pageable pageable) {
        log.debug("Consultando todos los registros de auditoría (página {})", pageable.getPageNumber());
        return auditLogRepository.findAll(pageable);
    }

    @Override
    public Page<AuditLog> findByUsername(String username, Pageable pageable) {
        log.debug("Consultando auditoría para usuario: {}", username);
        return auditLogRepository.findByUsername(username, pageable);
    }

    @Override
    public Page<AuditLog> findByAction(String action, Pageable pageable) {
        log.debug("Consultando auditoría para acción: {}", action);
        return auditLogRepository.findByAction(action, pageable);
    }

    @Override
    public Page<AuditLog> findByResource(String resource, Pageable pageable) {
        log.debug("Consultando auditoría para recurso: {}", resource);
        return auditLogRepository.findByResource(resource, pageable);
    }

    @Override
    public Page<AuditLog> findByDateRange(
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        log.debug("Consultando auditoría entre {} y {}", startDate, endDate);
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
    }

    @Override
    public Page<AuditLog> findByUsernameAndDateRange(
            String username,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        log.debug("Consultando auditoría para usuario {} entre {} y {}",
                username, startDate, endDate);

        return auditLogRepository.findByUsernameAndTimestampBetween(
                username, startDate, endDate, pageable);
    }

    @Override
    public Page<AuditLog> findFailures(Pageable pageable) {
        log.debug("Consultando operaciones fallidas");
        return auditLogRepository.findByStatus("FAILURE", pageable);
    }

    @Override
    public Page<AuditLog> findResourceHistory(
            String resource,
            Long resourceId,
            Pageable pageable) {

        log.debug("Consultando historial de {} con ID {}", resource, resourceId);
        return auditLogRepository.findByResourceAndResourceId(resource, resourceId, pageable);
    }

    @Override
    public long countRecentFailures(int hoursAgo) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursAgo);
        long count = auditLogRepository.countFailuresSince(since);

        log.debug("Operaciones fallidas en las últimas {} horas: {}", hoursAgo, count);
        return count;
    }

    @Override
    public long countUserRecentActions(String username, int hoursAgo) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursAgo);
        long count = auditLogRepository.countUserActionsSince(username, since);

        log.debug("Acciones de usuario {} en las últimas {} horas: {}",
                username, hoursAgo, count);

        return count;
    }

    @Override
    public Map<String, Object> getStatistics() {
        log.debug("Generando estadísticas de auditoría");

        Map<String, Object> stats = new HashMap<>();

        // Total de operaciones
        long totalOperations = auditLogRepository.count();
        stats.put("totalOperations", totalOperations);

        // Operaciones en las últimas 24 horas
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        Pageable pageable = PageRequest.of(0, 1);
        Page<AuditLog> recent = auditLogRepository.findByTimestampBetween(
                last24Hours, LocalDateTime.now(), pageable);
        stats.put("operationsLast24Hours", recent.getTotalElements());

        // Fallos recientes
        long recentFailures = countRecentFailures(24);
        stats.put("failuresLast24Hours", recentFailures);

        // Acciones más frecuentes
        stats.put("mostFrequentActions", getMostFrequentActions(5));

        // Usuarios más activos
        stats.put("mostActiveUsers", getMostActiveUsers(5));

        // Tasa de éxito
        if (totalOperations > 0) {
            long totalFailures = auditLogRepository.findByStatus(
                    "FAILURE",
                    PageRequest.of(0, 1)
            ).getTotalElements();

            double successRate = ((double) (totalOperations - totalFailures) / totalOperations) * 100;
            stats.put("successRate", String.format("%.2f%%", successRate));
        } else {
            stats.put("successRate", "N/A");
        }

        return stats;
    }

    @Override
    public Map<String, Long> getMostFrequentActions(int limit) {
        log.debug("Obteniendo las {} acciones más frecuentes", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = auditLogRepository.findMostFrequentActions(pageable);

        Map<String, Long> frequentActions = new LinkedHashMap<>();
        for (Object[] row : results) {
            String action = (String) row[0];
            Long frequency = (Long) row[1];
            frequentActions.put(action, frequency);
        }

        return frequentActions;
    }

    @Override
    public Map<String, Long> getMostActiveUsers(int limit) {
        log.debug("Obteniendo los {} usuarios más activos", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = auditLogRepository.findMostActiveUsers(pageable);

        Map<String, Long> activeUsers = new LinkedHashMap<>();
        for (Object[] row : results) {
            String username = (String) row[0];
            Long actionCount = (Long) row[1];
            activeUsers.put(username, actionCount);
        }

        return activeUsers;
    }

    @Override
    @Transactional
    public int cleanupOldRecords(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);

        log.info("Limpiando registros de auditoría anteriores a {}", cutoffDate);

        int deletedCount = auditLogRepository.deleteOldRecords(cutoffDate);

        log.info("Se eliminaron {} registros de auditoría antiguos", deletedCount);

        return deletedCount;
    }

    @Override
    public Map<String, Object> detectSuspiciousActivity(int hoursAgo) {
        log.debug("Detectando actividad sospechosa en las últimas {} horas", hoursAgo);

        Map<String, Object> alerts = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusHours(hoursAgo);

        // 1. Detectar múltiples fallos recientes
        long recentFailures = countRecentFailures(hoursAgo);
        if (recentFailures > SUSPICIOUS_FAILURES_THRESHOLD) {
            alerts.put("highFailureRate", Map.of(
                    "count", recentFailures,
                    "threshold", SUSPICIOUS_FAILURES_THRESHOLD,
                    "severity", "WARNING",
                    "message", String.format(
                            "Se detectaron %d operaciones fallidas en las últimas %d horas",
                            recentFailures, hoursAgo)
            ));
        }

        // 2. Detectar usuarios con actividad inusualmente alta
        Pageable top10Users = PageRequest.of(0, 10);
        List<Object[]> activeUsers = auditLogRepository.findMostActiveUsers(top10Users);

        for (Object[] row : activeUsers) {
            String username = (String) row[0];
            Long actionCount = (Long) row[1];

            long recentActions = countUserRecentActions(username, hoursAgo);

            if (recentActions > SUSPICIOUS_ACTIONS_THRESHOLD) {
                alerts.put("suspiciousUser_" + username, Map.of(
                        "username", username,
                        "recentActions", recentActions,
                        "threshold", SUSPICIOUS_ACTIONS_THRESHOLD,
                        "severity", "INFO",
                        "message", String.format(
                                "Usuario %s realizó %d acciones en las últimas %d horas",
                                username, recentActions, hoursAgo)
                ));
            }
        }

        // 3. Detectar múltiples operaciones DELETE
        Pageable deleteActions = PageRequest.of(0, 1);
        Page<AuditLog> deleteOps = auditLogRepository.findByTimestampBetween(
                since, LocalDateTime.now(), deleteActions);

        // Obtener el total de elementos de la primera página
        long totalDeletes = deleteOps.getTotalElements();

        if (totalDeletes > 20) {  // Más de 20 deletes en el período
            alerts.put("highDeleteActivity", Map.of(
                    "count", totalDeletes,
                    "severity", "WARNING",
                    "message", String.format(
                            "Se detectaron %d operaciones DELETE en las últimas %d horas",
                            totalDeletes, hoursAgo)
            ));
        }

        if (alerts.isEmpty()) {
            alerts.put("status", "No se detectó actividad sospechosa");
        }

        return alerts;
    }
}
