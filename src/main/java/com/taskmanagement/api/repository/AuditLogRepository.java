package com.taskmanagement.api.repository;

import com.taskmanagement.api.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para gestión de registros de auditoría.
 *
 * PROPÓSITO:
 * ==========
 *
 * Provee métodos de consulta especializados para auditoría:
 * - Búsqueda por usuario
 * - Búsqueda por acción
 * - Búsqueda por rango de fechas
 * - Búsqueda de operaciones fallidas
 * - Estadísticas de actividad
 *
 * CASOS DE USO:
 * =============
 *
 * 1. Auditoría de Seguridad:
 *    - ¿Qué hizo el usuario X en las últimas 24 horas?
 *    - ¿Cuántos intentos de login fallidos hubo hoy?
 *
 * 2. Cumplimiento Normativo:
 *    - Exportar todas las operaciones de un usuario para reporte legal
 *    - Demostrar quién modificó qué y cuándo
 *
 * 3. Análisis Forense:
 *    - Investigar incidente de seguridad
 *    - Rastrear cambios no autorizados
 *
 * 4. Reportes de Actividad:
 *    - Generar reportes de uso del sistema
 *    - Identificar usuarios más activos
 *
 * OPTIMIZACIÓN:
 * =============
 *
 * - Índices en: username, action, timestamp (definidos en la entidad)
 * - Usar paginación para grandes volúmenes de datos
 * - Considerar cache para consultas frecuentes
 *
 * @see AuditLog
 * @see com.taskmanagement.api.aspect.AuditAspect
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Busca registros de auditoría por username.
     *
     * Útil para:
     * - Ver todas las acciones de un usuario específico
     * - Generar reportes de actividad por usuario
     * - Investigaciones de seguridad
     *
     * @param username nombre del usuario
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByUsername(String username, Pageable pageable);

    /**
     * Busca registros de auditoría por acción.
     *
     * Útil para:
     * - Ver todas las operaciones de un tipo (ej: todos los DELETE)
     * - Analizar patrones de uso
     * - Detectar comportamientos anómalos
     *
     * @param action tipo de acción
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * Busca registros de auditoría por recurso.
     *
     * Útil para:
     * - Ver historial de cambios de un tipo de entidad
     * - Auditar operaciones sobre recursos críticos
     *
     * @param resource tipo de recurso
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByResource(String resource, Pageable pageable);

    /**
     * Busca registros de auditoría por status.
     *
     * Útil para:
     * - Ver solo operaciones exitosas o solo fallidas
     * - Detectar patrones de errores
     *
     * @param status estado (SUCCESS/FAILURE)
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByStatus(String status, Pageable pageable);

    /**
     * Busca registros de auditoría en un rango de fechas.
     *
     * Útil para:
     * - Reportes mensuales/trimestrales
     * - Análisis de actividad en períodos específicos
     * - Cumplimiento de auditorías externas
     *
     * @param startDate fecha de inicio
     * @param endDate fecha de fin
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByTimestampBetween(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Busca registros de auditoría de un usuario en un rango de fechas.
     *
     * Combinación de filtros más utilizada en la práctica.
     *
     * @param username nombre del usuario
     * @param startDate fecha de inicio
     * @param endDate fecha de fin
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByUsernameAndTimestampBetween(
        String username,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * Busca operaciones fallidas de un usuario.
     *
     * Útil para:
     * - Detectar intentos de acceso no autorizado
     * - Identificar problemas de uso del sistema
     * - Alertas de seguridad
     *
     * @param username nombre del usuario
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByUsernameAndStatus(
        String username,
        String status,
        Pageable pageable
    );

    /**
     * Busca operaciones sobre un recurso específico por su ID.
     *
     * Útil para:
     * - Ver historial completo de cambios de una entidad específica
     * - Investigar quién modificó un registro en particular
     * - Análisis forense detallado
     *
     * @param resource tipo de recurso
     * @param resourceId ID del recurso
     * @param pageable configuración de paginación
     * @return página de registros de auditoría
     */
    Page<AuditLog> findByResourceAndResourceId(
        String resource,
        Long resourceId,
        Pageable pageable
    );

    /**
     * Cuenta operaciones fallidas en las últimas N horas.
     *
     * Útil para:
     * - Detectar ataques de fuerza bruta
     * - Monitoreo de salud del sistema
     * - Alertas automáticas
     *
     * @param since timestamp desde cuando contar
     * @return número de operaciones fallidas
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.status = 'FAILURE' AND a.timestamp >= :since")
    long countFailuresSince(@Param("since") LocalDateTime since);

    /**
     * Cuenta operaciones de un usuario en las últimas N horas.
     *
     * Útil para:
     * - Detectar actividad inusual
     * - Rate limiting basado en actividad
     * - Análisis de patrones de uso
     *
     * @param username nombre del usuario
     * @param since timestamp desde cuando contar
     * @return número de operaciones
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.username = :username AND a.timestamp >= :since")
    long countUserActionsSince(
        @Param("username") String username,
        @Param("since") LocalDateTime since
    );

    /**
     * Obtiene las acciones más frecuentes (top N).
     *
     * Útil para:
     * - Análisis de uso del sistema
     * - Optimización de funcionalidades más usadas
     * - Reportes ejecutivos
     *
     * @param pageable configuración de paginación (usar para limitar resultados)
     * @return lista de acciones con su frecuencia
     */
    @Query("""
        SELECT a.action, COUNT(a) as frequency
        FROM AuditLog a
        GROUP BY a.action
        ORDER BY COUNT(a) DESC
        """)
    List<Object[]> findMostFrequentActions(Pageable pageable);

    /**
     * Obtiene los usuarios más activos (top N).
     *
     * Útil para:
     * - Identificar power users
     * - Análisis de adopción
     * - Reportes de actividad
     *
     * @param pageable configuración de paginación (usar para limitar resultados)
     * @return lista de usuarios con su conteo de acciones
     */
    @Query("""
        SELECT a.username, COUNT(a) as actionCount
        FROM AuditLog a
        WHERE a.username != 'ANONYMOUS'
        GROUP BY a.username
        ORDER BY COUNT(a) DESC
        """)
    List<Object[]> findMostActiveUsers(Pageable pageable);

    /**
     * Obtiene estadísticas de éxito/fallo por acción.
     *
     * Útil para:
     * - Identificar operaciones problemáticas
     * - Métricas de calidad
     * - Análisis de UX
     *
     * @return lista de acciones con conteos de éxito y fallo
     */
    @Query("""
        SELECT a.action, a.status, COUNT(a)
        FROM AuditLog a
        GROUP BY a.action, a.status
        ORDER BY a.action, a.status
        """)
    List<Object[]> findSuccessFailureStats();

    /**
     * Elimina registros antiguos (para limpieza periódica).
     *
     * IMPORTANTE: Configurar políticas de retención según requisitos legales.
     *
     * @param before fecha límite (eliminar registros anteriores a esta fecha)
     * @return número de registros eliminados
     */
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :before")
    int deleteOldRecords(@Param("before") LocalDateTime before);
}
