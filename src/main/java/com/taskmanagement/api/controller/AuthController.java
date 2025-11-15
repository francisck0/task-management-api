package com.taskmanagement.api.controller;

import com.taskmanagement.api.dto.AuthResponse;
import com.taskmanagement.api.dto.ErrorResponseDto;
import com.taskmanagement.api.dto.LoginRequest;
import com.taskmanagement.api.dto.RegisterRequest;
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
 *
 * IMPORTANTE: Estos endpoints son públicos (no requieren autenticación)
 * según la configuración de SecurityConfig.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Authentication",
    description = """
        API para autenticación y registro de usuarios.

        **Flujo de autenticación JWT:**
        1. Registrar nuevo usuario o hacer login con credenciales existentes
        2. Recibir token JWT en la respuesta
        3. Incluir el token en todas las peticiones subsecuentes:
           - Header: `Authorization: Bearer {token}`
        4. El token expira en 24 horas (configurable)

        **Endpoints públicos:** No requieren autenticación
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
}
