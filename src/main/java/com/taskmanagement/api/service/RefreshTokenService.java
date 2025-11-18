package com.taskmanagement.api.service;

import com.taskmanagement.api.model.RefreshToken;
import com.taskmanagement.api.model.User;

import java.util.List;

/**
 * Servicio para gestión de Refresh Tokens.
 *
 * RESPONSABILIDADES:
 * ==================
 *
 * 1. Creación de refresh tokens
 * 2. Validación de refresh tokens
 * 3. Rotación de tokens (seguridad)
 * 4. Revocación de tokens (logout)
 * 5. Limpieza de tokens expirados
 * 6. Gestión de sesiones activas
 *
 * POLÍTICAS DE SEGURIDAD:
 * ======================
 *
 * - Duración del refresh token: 7 días (configurable)
 * - Rotation: Cada uso genera nuevo token (opcional)
 * - Límite de dispositivos: 5 simultáneos (configurable)
 * - Revocación automática al cambiar contraseña
 * - Limpieza diaria de tokens expirados
 *
 * @see RefreshToken
 * @see com.taskmanagement.api.repository.RefreshTokenRepository
 */
public interface RefreshTokenService {

    /**
     * Crea un nuevo refresh token para un usuario.
     *
     * IMPORTANTE:
     * - Guarda el token en la base de datos
     * - Captura IP y User-Agent del cliente
     * - Aplica límite de dispositivos simultáneos
     *
     * @param user usuario para el cual crear el token
     * @param ipAddress IP del cliente (opcional)
     * @param userAgent User-Agent del cliente (opcional)
     * @return refresh token creado
     */
    RefreshToken createRefreshToken(User user, String ipAddress, String userAgent);

    /**
     * Valida un refresh token.
     *
     * Verifica que:
     * - El token existe en BD
     * - No ha expirado
     * - No ha sido revocado
     * - (Opcional) No ha sido usado si rotation está habilitada
     *
     * @param token valor del refresh token
     * @return RefreshToken si es válido
     * @throws RuntimeException si el token es inválido
     */
    RefreshToken validateRefreshToken(String token);

    /**
     * Renueva un access token usando un refresh token.
     *
     * FLUJO:
     * 1. Valida el refresh token
     * 2. Obtiene el usuario asociado
     * 3. Genera nuevo access token
     * 4. (Opcional) Rota el refresh token
     * 5. Actualiza lastUsedAt
     *
     * @param refreshToken valor del refresh token
     * @return nuevo access token (JWT)
     */
    String renewAccessToken(String refreshToken);

    /**
     * Rota un refresh token.
     *
     * Estrategia de seguridad:
     * - Marca el token actual como "usado"
     * - Genera un nuevo refresh token
     * - Retorna el nuevo token
     *
     * VENTAJA:
     * Si un atacante roba un refresh token, solo puede usarlo una vez.
     * El segundo uso (del atacante o del usuario legítimo) genera alerta.
     *
     * @param oldToken refresh token actual
     * @return nuevo refresh token
     */
    RefreshToken rotateRefreshToken(RefreshToken oldToken);

    /**
     * Revoca un refresh token específico (logout).
     *
     * @param token valor del refresh token a revocar
     */
    void revokeRefreshToken(String token);

    /**
     * Revoca todos los refresh tokens de un usuario.
     *
     * Usado cuando:
     * - Usuario hace "logout de todos los dispositivos"
     * - Usuario cambia su contraseña
     * - Administrador invalida sesiones de un usuario
     *
     * @param user usuario
     * @return número de tokens revocados
     */
    int revokeAllUserTokens(User user);

    /**
     * Obtiene las sesiones activas de un usuario.
     *
     * Retorna información de todos los refresh tokens válidos:
     * - Dispositivo (User-Agent)
     * - IP
     * - Fecha de creación
     * - Último uso
     *
     * Útil para que el usuario vea dónde está logueado.
     *
     * @param user usuario
     * @return lista de sesiones activas
     */
    List<RefreshToken> getActiveSessions(User user);

    /**
     * Cuenta sesiones activas de un usuario.
     *
     * @param user usuario
     * @return número de sesiones activas
     */
    long countActiveSessions(User user);

    /**
     * Limpia tokens expirados de la base de datos.
     *
     * Debe ejecutarse periódicamente (job diario).
     *
     * @return número de tokens eliminados
     */
    int cleanupExpiredTokens();

    /**
     * Limpia tokens revocados antiguos.
     *
     * @param daysOld días de antigüedad para eliminar
     * @return número de tokens eliminados
     */
    int cleanupRevokedTokens(int daysOld);

    /**
     * Verifica si un refresh token es válido (sin cargarlo).
     *
     * Query optimizada para validación rápida.
     *
     * @param token valor del token
     * @return true si es válido
     */
    boolean isRefreshTokenValid(String token);

    /**
     * Obtiene un refresh token por su valor.
     *
     * @param token valor del token
     * @return RefreshToken si existe
     * @throws RuntimeException si no existe
     */
    RefreshToken findByToken(String token);
}
