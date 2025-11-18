package com.taskmanagement.api.controller;

import com.taskmanagement.api.constant.ApiVersion;
import com.taskmanagement.api.dto.*;
import com.taskmanagement.api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación y registro de usuarios.
 *
 * Capa CONTROLLER: Punto de entrada para operaciones de autenticación.
 *
 * Endpoints:
 * - POST /auth/register - Registrar nuevo usuario
 * - POST /auth/login - Autenticar usuario existente
 * - POST /auth/refresh - Renovar access token con refresh token
 * - POST /auth/logout - Cerrar sesión (revocar refresh token)
 *
 * IMPORTANTE: Endpoints públicos (no requieren autenticación):
 * - /auth/register
 * - /auth/login
 * - /auth/refresh
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Authentication",
    description = """
        API para autenticación y registro de usuarios.

        **Flujo de autenticación con Refresh Tokens:**
        1. Registrar nuevo usuario o hacer login con credenciales existentes
        2. Recibir access token (JWT corta duración) y refresh token (larga duración)
        3. Incluir el access token en todas las peticiones subsecuentes:
           - Header: `Authorization: Bearer {accessToken}`
        4. Cuando el access token expire (401), usar refresh token para obtener uno nuevo:
           - POST /auth/refresh con el refresh token
        5. Recibir nuevo access token y continuar usando la aplicación

        **Access Token:** 1 hora de duración - para acceder a recursos
        **Refresh Token:** 7 días de duración - para renovar access token

        **Endpoints públicos:** register, login, refresh
        """
)
public class AuthController {

    private final AuthService authService;

    /**
     * Registra un nuevo usuario en el sistema
     *
     * Endpoint: POST /auth/register
     */
    @PostMapping("/register")
    @Operation(
        summary = "Registrar nuevo usuario",
        description = """
            Crea una nueva cuenta de usuario en el sistema.

            **Proceso:**
            1. Valida que username y email sean únicos
            2. Encripta la contraseña con BCrypt
            3. Asigna el rol ROLE_USER por defecto
            4. Genera un token JWT
            5. Retorna el token y datos del usuario

            **El token JWT debe usarse en peticiones subsecuentes:**
            ```
            Authorization: Bearer {token}
            ```

            **Notas:**
            - Username debe ser único (3-50 caracteres)
            - Email debe ser único y válido
            - Contraseña mínimo 6 caracteres (será encriptada)
            - FullName es opcional
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Usuario registrado exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huZG9lIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwODY0MDB9.signature",
                          "type": "Bearer",
                          "username": "johndoe",
                          "email": "john.doe@example.com",
                          "roles": ["ROLE_USER"]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación o usuario/email ya existe",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2025-11-15T10:30:00",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "El nombre de usuario ya está en uso",
                          "path": "/api/v1/auth/register"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<AuthResponse> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del nuevo usuario a registrar",
                required = true,
                content = @Content(schema = @Schema(implementation = RegisterRequest.class))
            )
            @Valid @RequestBody RegisterRequest request) {

        log.info("Petición de registro recibida para usuario: {}", request.getUsername());

        AuthResponse response = authService.register(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Autentica un usuario existente
     *
     * Endpoint: POST /auth/login
     */
    @PostMapping("/login")
    @Operation(
        summary = "Autenticar usuario (Login)",
        description = """
            Autentica un usuario con sus credenciales y genera un token JWT.

            **Proceso:**
            1. Valida las credenciales (username/password)
            2. Si son válidas, genera un token JWT
            3. Retorna el token y datos del usuario

            **El token JWT debe usarse en peticiones subsecuentes:**
            ```
            Authorization: Bearer {token}
            ```

            **Token JWT:**
            - Válido por 24 horas (configurable)
            - Contiene username y roles del usuario
            - Debe incluirse en el header Authorization de todas las peticiones protegidas

            **Ejemplo de uso del token:**
            ```http
            GET /api/v1/tasks
            Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
            ```
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Autenticación exitosa",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDg2NDAwfQ.signature",
                          "type": "Bearer",
                          "username": "admin",
                          "email": "admin@taskmanagement.com",
                          "roles": ["ROLE_ADMIN", "ROLE_USER"]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2025-11-15T10:30:00",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "Credenciales inválidas",
                          "path": "/api/v1/auth/login"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Credenciales de login (username y password)",
                required = true,
                content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
            @Valid @RequestBody LoginRequest request) {

        log.info("Petición de login recibida para usuario: {}", request.getUsername());

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Renueva un access token usando un refresh token
     *
     * Endpoint: POST /auth/refresh
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Renovar access token",
        description = """
            Renueva un access token expirado usando un refresh token válido.

            **Flujo:**
            1. Cliente detecta que el access token expiró (401 Unauthorized)
            2. Cliente envía el refresh token a este endpoint
            3. Backend valida el refresh token
            4. Backend genera nuevo access token
            5. Si la rotation está habilitada, también genera nuevo refresh token
            6. Backend retorna ambos tokens

            **Cuándo usar:**
            - Cuando tu access token expira y recibes 401 Unauthorized
            - Proactivamente antes de que expire (recomendado)

            **Refresh Token Rotation:**
            Si está habilitada, cada uso del refresh token genera uno nuevo
            y el anterior se invalida. Esto mejora la seguridad detectando
            tokens comprometidos.

            **Seguridad:**
            - Refresh tokens son de un solo uso (si rotation habilitada)
            - Reuso de token revoca todas las sesiones del usuario
            - Refresh tokens expiran después de 7 días
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tokens renovados exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RefreshTokenResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "refreshToken": "660e8400-e29b-41d4-a716-446655440001",
                          "type": "Bearer",
                          "expiresIn": 3600000
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Refresh token inválido o expirado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2025-11-16T10:30:00",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "Refresh token expirado. Por favor, inicie sesión nuevamente.",
                          "path": "/api/v1/auth/refresh"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<RefreshTokenResponse> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Refresh token obtenido durante el login o último refresh",
                required = true,
                content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
            )
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("Petición de refresh token recibida");

        RefreshTokenResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Cierra sesión revocando el refresh token
     *
     * Endpoint: POST /auth/logout
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Cerrar sesión (Logout)",
        description = """
            Cierra la sesión del usuario revocando su refresh token.

            **Proceso:**
            1. Valida el refresh token
            2. Marca el refresh token como revocado
            3. El refresh token ya no puede usarse para renovar access tokens

            **Nota:**
            - El access token actual seguirá siendo válido hasta que expire
            - Para logout completo inmediato, el cliente debe eliminar ambos tokens
            - Para cerrar todas las sesiones, usar /auth/logout-all (TODO)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Sesión cerrada exitosamente"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Refresh token inválido"
        )
    })
    public ResponseEntity<Void> logout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Refresh token a revocar",
                required = true,
                content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
            )
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("Petición de logout recibida");

        authService.logout(request);

        return ResponseEntity.ok().build();
    }
}
