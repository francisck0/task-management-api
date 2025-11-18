package com.taskmanagement.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad para gestionar Refresh Tokens.
 *
 * PROPÓSITO:
 * ==========
 *
 * Los Refresh Tokens permiten renovar Access Tokens sin necesidad de
 * volver a introducir credenciales (usuario/contraseña).
 *
 * FLUJO DE AUTENTICACIÓN CON REFRESH TOKENS:
 * ==========================================
 *
 * 1. LOGIN INICIAL:
 *    Usuario → [username + password] → API
 *    API → Valida credenciales → Genera Access Token (corta duración)
 *                              → Genera Refresh Token (larga duración)
 *    API → Retorna ambos tokens
 *
 * 2. ACCESO A RECURSOS:
 *    Usuario → [Access Token] → API
 *    API → Valida Access Token → Permite acceso
 *
 * 3. ACCESS TOKEN EXPIRA:
 *    Usuario → [Access Token expirado] → API
 *    API → 401 Unauthorized
 *
 * 4. RENOVACIÓN (sin volver a pedir contraseña):
 *    Usuario → [Refresh Token] → /api/v1/auth/refresh
 *    API → Valida Refresh Token → Genera nuevo Access Token
 *                               → Opcionalmente rota Refresh Token
 *    API → Retorna nuevo Access Token
 *
 * VENTAJAS:
 * =========
 *
 * 1. Seguridad:
 *    - Access Token de corta duración (15 min - 1 hora)
 *      Si es robado, solo es válido por poco tiempo
 *    - Refresh Token de larga duración (7-30 días)
 *      Solo se usa para renovar, no para acceder a recursos
 *
 * 2. Experiencia de Usuario:
 *    - No necesita volver a hacer login constantemente
 *    - Sesión permanece activa mientras use la app
 *
 * 3. Control:
 *    - Posibilidad de revocar Refresh Tokens (logout, cambio de contraseña)
 *    - Tracking de dispositivos/sesiones activas
 *    - Detección de tokens comprometidos (rotation)
 *
 * REFRESH TOKEN ROTATION:
 * ======================
 *
 * Estrategia de seguridad donde cada vez que se usa un Refresh Token,
 * se genera uno nuevo y el anterior se invalida.
 *
 * Beneficios:
 * - Detecta tokens robados (si se usa el anterior después de rotar)
 * - Limita la ventana de vulnerabilidad
 * - Permite revocar toda la cadena si se detecta uso sospechoso
 *
 * ALMACENAMIENTO EN CLIENTE:
 * ==========================
 *
 * - Access Token: Memoria (variable JavaScript) o sessionStorage
 * - Refresh Token: httpOnly cookie (más seguro) o localStorage
 *
 * IMPORTANTE: Nunca exponer Refresh Token en código JavaScript
 * si es posible usar httpOnly cookies.
 *
 * @see User
 * @see com.taskmanagement.api.service.JwtService
 * @see com.taskmanagement.api.controller.AuthController
 */
@Entity
@Table(
    name = "refresh_tokens",
    indexes = {
        @Index(name = "idx_refresh_token", columnList = "token"),
        @Index(name = "idx_refresh_user_id", columnList = "user_id"),
        @Index(name = "idx_refresh_expiry", columnList = "expiryDate")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    /**
     * ID único del refresh token.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * El token único (UUID o JWT).
     *
     * IMPORTANTE: Este campo debe ser único y estar indexado para búsquedas rápidas.
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * Usuario al que pertenece este refresh token.
     *
     * Relación Many-to-One: Un usuario puede tener múltiples refresh tokens
     * (uno por dispositivo/sesión).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Fecha y hora de expiración del token.
     *
     * Típicamente: 7-30 días desde la creación.
     *
     * Después de esta fecha, el token no puede usarse para renovar
     * y el usuario debe hacer login de nuevo.
     */
    @Column(nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Fecha y hora de creación del token.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Indica si el token ha sido revocado manualmente.
     *
     * Casos de revocación:
     * - Usuario hace logout
     * - Usuario cambia su contraseña
     * - Administrador revoca sesiones de un usuario
     * - Se detecta uso sospechoso
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    /**
     * Si este token ya fue usado (para rotation).
     *
     * Cuando se usa un refresh token para obtener un nuevo access token,
     * se puede marcar como "usado" y generar un nuevo refresh token.
     *
     * Esto implementa la estrategia de "Refresh Token Rotation".
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    /**
     * Timestamp de cuando se usó este token por última vez.
     *
     * Útil para:
     * - Detectar tokens que no se usan hace mucho
     * - Limpieza de tokens antiguos
     * - Análisis de actividad de usuarios
     */
    @Column
    private LocalDateTime lastUsedAt;

    /**
     * IP desde la cual se creó el token.
     *
     * Útil para:
     * - Seguridad: detectar accesos desde IPs sospechosas
     * - Análisis de actividad geográfica
     */
    @Column(length = 45)
    private String ipAddress;

    /**
     * User Agent del cliente que creó el token.
     *
     * Útil para:
     * - Identificar el dispositivo/navegador
     * - Mostrar "sesiones activas" al usuario
     * - Permitir cerrar sesión en dispositivos específicos
     */
    @Column(length = 500)
    private String userAgent;

    // =========================================================================
    // MÉTODOS DE UTILIDAD
    // =========================================================================

    /**
     * Verifica si el token ha expirado.
     *
     * @return true si el token expiró
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * Verifica si el token es válido.
     *
     * Un token es válido si:
     * - No está expirado
     * - No fue revocado
     * - No fue usado (si se implementa rotation estricta)
     *
     * @return true si el token es válido
     */
    public boolean isValid() {
        return !isExpired() && !revoked;
    }

    /**
     * Marca el token como revocado.
     */
    public void revoke() {
        this.revoked = true;
    }

    /**
     * Marca el token como usado (para rotation).
     */
    public void markAsUsed() {
        this.used = true;
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Actualiza el timestamp de último uso.
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}
