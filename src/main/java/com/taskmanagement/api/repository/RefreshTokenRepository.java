package com.taskmanagement.api.repository;

import com.taskmanagement.api.model.RefreshToken;
import com.taskmanagement.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de Refresh Tokens.
 *
 * PROPÓSITO:
 * ==========
 *
 * Provee métodos de consulta especializados para:
 * - Búsqueda de tokens por valor
 * - Búsqueda de tokens por usuario
 * - Limpieza de tokens expirados
 * - Revocación masiva de tokens
 * - Gestión de sesiones activas
 *
 * CASOS DE USO:
 * =============
 *
 * 1. Renovación de Access Token:
 *    - findByToken() → Validar que existe y es válido
 *
 * 2. Logout:
 *    - findByToken() → Marcar como revocado
 *
 * 3. Logout de todos los dispositivos:
 *    - findByUser() → Marcar todos como revocados
 *
 * 4. Cambio de contraseña (invalidar todas las sesiones):
 *    - revokeAllUserTokens() → Invalida todos los tokens del usuario
 *
 * 5. Limpieza periódica:
 *    - deleteExpiredTokens() → Elimina tokens caducados
 *
 * 6. Ver sesiones activas:
 *    - findValidTokensByUser() → Muestra dispositivos/sesiones activas
 *
 * @see RefreshToken
 * @see com.taskmanagement.api.service.RefreshTokenService
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un refresh token por su valor.
     *
     * Usado principalmente en el endpoint /refresh para validar
     * el token enviado por el cliente.
     *
     * @param token valor del refresh token
     * @return Optional con el RefreshToken si existe
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Busca todos los refresh tokens de un usuario.
     *
     * Útil para:
     * - Ver todas las sesiones activas de un usuario
     * - Revocar todas las sesiones al cambiar contraseña
     * - Permitir al usuario cerrar sesiones remotas
     *
     * @param user usuario
     * @return lista de refresh tokens del usuario
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Busca todos los tokens válidos (no expirados ni revocados) de un usuario.
     *
     * Útil para:
     * - Mostrar "sesiones activas" al usuario
     * - Limitar número de dispositivos simultáneos
     * - Análisis de seguridad
     *
     * @param user usuario
     * @param now timestamp actual
     * @return lista de tokens válidos
     */
    @Query("""
        SELECT rt FROM RefreshToken rt
        WHERE rt.user = :user
        AND rt.revoked = false
        AND rt.expiryDate > :now
        ORDER BY rt.createdAt DESC
        """)
    List<RefreshToken> findValidTokensByUser(
        @Param("user") User user,
        @Param("now") LocalDateTime now
    );

    /**
     * Cuenta tokens válidos de un usuario.
     *
     * Útil para:
     * - Limitar número de sesiones simultáneas
     * - Métricas de uso
     *
     * @param user usuario
     * @param now timestamp actual
     * @return número de tokens válidos
     */
    @Query("""
        SELECT COUNT(rt) FROM RefreshToken rt
        WHERE rt.user = :user
        AND rt.revoked = false
        AND rt.expiryDate > :now
        """)
    long countValidTokensByUser(
        @Param("user") User user,
        @Param("now") LocalDateTime now
    );

    /**
     * Revoca todos los tokens de un usuario.
     *
     * Usado cuando:
     * - Usuario cambia su contraseña (invalidar todas las sesiones)
     * - Usuario hace "logout de todos los dispositivos"
     * - Administrador invalida las sesiones de un usuario
     * - Se detecta actividad sospechosa
     *
     * @param user usuario
     * @return número de tokens revocados
     */
    @Modifying
    @Query("""
        UPDATE RefreshToken rt
        SET rt.revoked = true
        WHERE rt.user = :user
        AND rt.revoked = false
        """)
    int revokeAllUserTokens(@Param("user") User user);

    /**
     * Elimina tokens expirados.
     *
     * Debe ejecutarse periódicamente (ej: diario) para limpiar la BD.
     *
     * Alternativa: En lugar de eliminar, marcar como "eliminables"
     * y archivar para auditoría.
     *
     * @param now timestamp actual
     * @return número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Elimina tokens revocados antiguos.
     *
     * Los tokens revocados pueden conservarse un tiempo para auditoría,
     * pero eventualmente pueden eliminarse.
     *
     * @param before timestamp límite (ej: hace 30 días)
     * @return número de tokens eliminados
     */
    @Modifying
    @Query("""
        DELETE FROM RefreshToken rt
        WHERE rt.revoked = true
        AND rt.createdAt < :before
        """)
    int deleteRevokedTokensOlderThan(@Param("before") LocalDateTime before);

    /**
     * Elimina tokens no usados hace mucho tiempo.
     *
     * Si un usuario no ha usado su app en X días, su refresh token
     * puede eliminarse por seguridad.
     *
     * @param before timestamp límite (ej: hace 90 días)
     * @return número de tokens eliminados
     */
    @Modifying
    @Query("""
        DELETE FROM RefreshToken rt
        WHERE rt.lastUsedAt < :before
        OR (rt.lastUsedAt IS NULL AND rt.createdAt < :before)
        """)
    int deleteUnusedTokensOlderThan(@Param("before") LocalDateTime before);

    /**
     * Busca tokens creados desde una IP específica.
     *
     * Útil para:
     * - Investigar actividad sospechosa
     * - Detectar patrones de ataque
     * - Análisis de seguridad
     *
     * @param ipAddress dirección IP
     * @return lista de tokens
     */
    List<RefreshToken> findByIpAddress(String ipAddress);

    /**
     * Cuenta tokens activos por IP en las últimas N horas.
     *
     * Útil para detectar:
     * - Ataques de fuerza bruta
     * - Creación masiva de sesiones desde una IP
     *
     * @param ipAddress dirección IP
     * @param since timestamp desde cuando contar
     * @return número de tokens
     */
    @Query("""
        SELECT COUNT(rt) FROM RefreshToken rt
        WHERE rt.ipAddress = :ipAddress
        AND rt.createdAt >= :since
        AND rt.revoked = false
        """)
    long countActiveTokensByIpSince(
        @Param("ipAddress") String ipAddress,
        @Param("since") LocalDateTime since
    );

    /**
     * Busca el refresh token más reciente de un usuario.
     *
     * Útil para:
     * - Implementar "single session mode" (solo un dispositivo a la vez)
     * - Debugging
     *
     * @param user usuario
     * @return Optional con el token más reciente
     */
    Optional<RefreshToken> findFirstByUserOrderByCreatedAtDesc(User user);

    /**
     * Verifica si existe un token específico y es válido.
     *
     * Query optimizada para validación rápida sin cargar toda la entidad.
     *
     * @param token valor del token
     * @param now timestamp actual
     * @return true si existe y es válido
     */
    @Query("""
        SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END
        FROM RefreshToken rt
        WHERE rt.token = :token
        AND rt.revoked = false
        AND rt.expiryDate > :now
        """)
    boolean existsByTokenAndIsValid(
        @Param("token") String token,
        @Param("now") LocalDateTime now
    );

    /**
     * Limita el número de tokens activos por usuario.
     *
     * Si un usuario supera el límite de dispositivos, elimina los más antiguos.
     *
     * @param user usuario
     * @param limit número máximo de tokens a mantener
     * @return número de tokens eliminados
     */
    @Modifying
    @Query(value = """
        DELETE FROM refresh_tokens
        WHERE id IN (
            SELECT id FROM refresh_tokens
            WHERE user_id = :userId
            AND revoked = false
            ORDER BY created_at DESC
            OFFSET :limit
        )
        """, nativeQuery = true)
    int deleteExcessTokens(
        @Param("userId") Long userId,
        @Param("limit") int limit
    );
}
