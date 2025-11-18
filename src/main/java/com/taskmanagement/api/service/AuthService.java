package com.taskmanagement.api.service;

import com.taskmanagement.api.dto.*;
import com.taskmanagement.api.exception.*;
import com.taskmanagement.api.model.RefreshToken;
import com.taskmanagement.api.model.Role;
import com.taskmanagement.api.model.User;
import com.taskmanagement.api.repository.RoleRepository;
import com.taskmanagement.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación y registro de usuarios.
 *
 * Capa SERVICE: Contiene la lógica de negocio para autenticación.
 *
 * Responsabilidades:
 * - Registrar nuevos usuarios
 * - Autenticar usuarios existentes
 * - Generar access tokens (JWT) y refresh tokens
 * - Renovar access tokens con refresh tokens
 * - Gestionar sesiones (logout)
 * - Validar credenciales
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Registra un nuevo usuario en el sistema
     *
     * @param request Datos del nuevo usuario
     * @return Respuesta con token JWT y datos del usuario
     * @throws DuplicateUsernameException si el username ya existe
     * @throws DuplicateEmailException si el email ya existe
     * @throws RoleNotFoundException si el rol USER no existe en BD
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Intento de registro de nuevo usuario: {}", request.getUsername());

        // Validar que el username no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException(request.getUsername());
        }

        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // Obtener el rol USER (todos los nuevos usuarios son USER por defecto)
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("ROLE_USER"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        // Crear el nuevo usuario
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encriptar contraseña
        user.setFullName(request.getFullName());
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setRoles(roles);

        // Guardar en la base de datos
        User savedUser = userRepository.save(user);

        log.info("Usuario registrado exitosamente: {}", savedUser.getUsername());

        // Generar access token (JWT)
        String accessToken = jwtService.generateToken(savedUser);

        // Generar refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                savedUser,
                getClientIp(),
                getUserAgent()
        );

        // Preparar respuesta
        List<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtExpiration,
                savedUser.getUsername(),
                savedUser.getEmail(),
                roleNames
        );
    }

    /**
     * Autentica un usuario y genera un token JWT
     *
     * @param request Credenciales de login
     * @return Respuesta con token JWT y datos del usuario
     * @throws InvalidCredentialsException si las credenciales son inválidas
     * @throws ResourceNotFoundException si el usuario no existe (no debería ocurrir)
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login de usuario: {}", request.getUsername());

        // Autenticar con Spring Security
        // Si las credenciales son inválidas, lanzará BadCredentialsException
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            // Convertir a nuestra excepción personalizada con mensaje genérico de seguridad
            throw new InvalidCredentialsException();
        }

        // Si llegamos aquí, las credenciales son válidas
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        log.info("Usuario autenticado exitosamente: {}", user.getUsername());

        // Generar access token (JWT)
        String accessToken = jwtService.generateToken(user);

        // Generar refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user,
                getClientIp(),
                getUserAgent()
        );

        // Preparar respuesta
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtExpiration,
                user.getUsername(),
                user.getEmail(),
                roleNames
        );
    }

    /**
     * Renueva un access token usando un refresh token válido
     *
     * @param request Request con el refresh token
     * @return Respuesta con nuevo access token y opcionalmente nuevo refresh token
     */
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        log.info("Renovando access token");

        // Validar refresh token
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());

        // Generar nuevo access token
        String newAccessToken = jwtService.generateToken(refreshToken.getUser());

        // Rotar refresh token si está habilitado
        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);

        log.info("Access token renovado exitosamente para usuario: {}", refreshToken.getUser().getUsername());

        return new RefreshTokenResponse(
                newAccessToken,
                newRefreshToken.getToken(),
                jwtExpiration
        );
    }

    /**
     * Cierra sesión revocando el refresh token
     *
     * @param request Request con el refresh token a revocar
     */
    @Transactional
    public void logout(RefreshTokenRequest request) {
        log.info("Cerrando sesión");

        refreshTokenService.revokeRefreshToken(request.getRefreshToken());

        log.info("Sesión cerrada exitosamente");
    }

    // =========================================================================
    // MÉTODOS DE UTILIDAD PRIVADOS
    // =========================================================================

    /**
     * Obtiene la IP del cliente desde el request actual.
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Intentar obtener IP real si está detrás de proxy/load balancer
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }

                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }

                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Error obteniendo IP del cliente: {}", e.getMessage());
        }

        return null;
    }

    /**
     * Obtiene el User-Agent del cliente desde el request actual.
     */
    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.warn("Error obteniendo User-Agent del cliente: {}", e.getMessage());
        }

        return null;
    }
}
