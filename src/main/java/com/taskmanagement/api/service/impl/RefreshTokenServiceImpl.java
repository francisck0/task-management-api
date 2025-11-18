package com.taskmanagement.api.service.impl;

import com.taskmanagement.api.exception.ResourceNotFoundException;
import com.taskmanagement.api.model.RefreshToken;
import com.taskmanagement.api.model.User;
import com.taskmanagement.api.repository.RefreshTokenRepository;
import com.taskmanagement.api.service.JwtService;
import com.taskmanagement.api.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Implementación del servicio de Refresh Tokens.
 *
 * @see RefreshTokenService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    /**
     * Duración del refresh token en días.
     * Configurable via application.yml (default: 7 días)
     */
    @Value("${jwt.refresh-token.expiration-days:7}")
    private int refreshTokenExpirationDays;

    /**
     * Habilitar rotación automática de refresh tokens.
     * Configurable via application.yml (default: true)
     */
    @Value("${jwt.refresh-token.rotation-enabled:true}")
    private boolean rotationEnabled;

    /**
     * Límite de dispositivos/sesiones simultáneas por usuario.
     * Configurable via application.yml (default: 5)
     */
    @Value("${jwt.refresh-token.max-devices:5}")
    private int maxDevicesPerUser;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String ipAddress, String userAgent) {
        log.debug("Creando refresh token para usuario: {}", user.getUsername());

        // Aplicar límite de dispositivos
        enforceDeviceLimit(user);

        // Generar token único (UUID)
        String tokenValue = UUID.randomUUID().toString();

        // Calcular fecha de expiración
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(refreshTokenExpirationDays);

        // Crear entidad
        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(expiryDate)
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .used(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        log.info("Refresh token creado para usuario '{}' (expira: {}, IP: {})",
                user.getUsername(),
                expiryDate,
                ipAddress);

        return saved;
    }

    @Override
    public RefreshToken validateRefreshToken(String token) {
        log.debug("Validando refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Intento de uso de refresh token inválido o inexistente");
                    return new ResourceNotFoundException("Refresh token inválido");
                });

        // Verificar si está expirado
        if (refreshToken.isExpired()) {
            log.warn("Intento de uso de refresh token expirado (usuario: {})",
                    refreshToken.getUser().getUsername());
            throw new IllegalStateException("Refresh token expirado. Por favor, inicie sesión nuevamente.");
        }

        // Verificar si fue revocado
        if (refreshToken.isRevoked()) {
            log.warn("Intento de uso de refresh token revocado (usuario: {})",
                    refreshToken.getUser().getUsername());
            throw new IllegalStateException("Refresh token revocado. Por favor, inicie sesión nuevamente.");
        }

        // Verificar si ya fue usado (rotation)
        if (rotationEnabled && refreshToken.isUsed()) {
            log.error("ALERTA DE SEGURIDAD: Refresh token ya usado detectado (usuario: {}, token: {}). " +
                            "Posible robo de token. Revocando todos los tokens del usuario.",
                    refreshToken.getUser().getUsername(),
                    token);

            // IMPORTANTE: Si se detecta reuso de token, es señal de compromiso
            // Revocar TODOS los tokens del usuario por seguridad
            revokeAllUserTokens(refreshToken.getUser());

            throw new SecurityException("Token comprometido detectado. Todas las sesiones han sido cerradas por seguridad.");
        }

        log.debug("Refresh token válido para usuario: {}", refreshToken.getUser().getUsername());
        return refreshToken;
    }

    @Override
    @Transactional
    public String renewAccessToken(String refreshTokenValue) {
        log.debug("Renovando access token");

        // Validar refresh token
        RefreshToken refreshToken = validateRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();

        // Generar nuevo access token
        String newAccessToken = jwtService.generateToken(user);

        // Actualizar lastUsedAt
        refreshToken.updateLastUsed();
        refreshTokenRepository.save(refreshToken);

        log.info("Access token renovado para usuario: {}", user.getUsername());

        return newAccessToken;
    }

    @Override
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        log.debug("Rotando refresh token para usuario: {}", oldToken.getUser().getUsername());

        // Marcar el token antiguo como usado
        oldToken.markAsUsed();
        refreshTokenRepository.save(oldToken);

        // Crear nuevo refresh token
        RefreshToken newToken = createRefreshToken(
                oldToken.getUser(),
                oldToken.getIpAddress(),
                oldToken.getUserAgent()
        );

        log.info("Refresh token rotado para usuario: {}", oldToken.getUser().getUsername());

        return newToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        log.debug("Revocando refresh token");

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token no encontrado"));

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        log.info("Refresh token revocado para usuario: {}", refreshToken.getUser().getUsername());
    }

    @Override
    @Transactional
    public int revokeAllUserTokens(User user) {
        log.info("Revocando todos los refresh tokens para usuario: {}", user.getUsername());

        int revokedCount = refreshTokenRepository.revokeAllUserTokens(user);

        log.info("Se revocaron {} refresh tokens para usuario: {}", revokedCount, user.getUsername());

        return revokedCount;
    }

    @Override
    public List<RefreshToken> getActiveSessions(User user) {
        log.debug("Obteniendo sesiones activas para usuario: {}", user.getUsername());

        return refreshTokenRepository.findValidTokensByUser(user, LocalDateTime.now());
    }

    @Override
    public long countActiveSessions(User user) {
        return refreshTokenRepository.countValidTokensByUser(user, LocalDateTime.now());
    }

    @Override
    @Transactional
    public int cleanupExpiredTokens() {
        log.info("Iniciando limpieza de refresh tokens expirados");

        int deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());

        log.info("Limpieza completada: {} refresh tokens expirados eliminados", deletedCount);

        return deletedCount;
    }

    @Override
    @Transactional
    public int cleanupRevokedTokens(int daysOld) {
        log.info("Iniciando limpieza de refresh tokens revocados (más de {} días)", daysOld);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = refreshTokenRepository.deleteRevokedTokensOlderThan(cutoffDate);

        log.info("Limpieza completada: {} refresh tokens revocados eliminados", deletedCount);

        return deletedCount;
    }

    @Override
    public boolean isRefreshTokenValid(String token) {
        return refreshTokenRepository.existsByTokenAndIsValid(token, LocalDateTime.now());
    }

    @Override
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token no encontrado"));
    }

    // =========================================================================
    // MÉTODOS PRIVADOS
    // =========================================================================

    /**
     * Aplica el límite de dispositivos simultáneos por usuario.
     *
     * Si el usuario ya tiene el máximo de dispositivos, elimina los más antiguos.
     */
    private void enforceDeviceLimit(User user) {
        long activeCount = countActiveSessions(user);

        if (activeCount >= maxDevicesPerUser) {
            log.warn("Usuario '{}' alcanzó el límite de dispositivos ({}). Eliminando sesiones antiguas.",
                    user.getUsername(), maxDevicesPerUser);

            // Eliminar tokens excedentes (mantener solo los más recientes)
            refreshTokenRepository.deleteExcessTokens(user.getId(), maxDevicesPerUser - 1);
        }
    }
}
