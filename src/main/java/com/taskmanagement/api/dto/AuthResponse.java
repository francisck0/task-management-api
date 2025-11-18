package com.taskmanagement.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuestas de autenticación.
 *
 * Contiene el Access Token (JWT) y el Refresh Token para renovación.
 *
 * TOKENS:
 * - accessToken: JWT de corta duración (1 hora) para acceder a recursos protegidos
 * - refreshToken: Token de larga duración (7 días) para renovar el access token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Respuesta de autenticación con access token, refresh token y datos del usuario",
    example = """
        {
          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
          "type": "Bearer",
          "expiresIn": 3600000,
          "username": "admin",
          "email": "admin@taskmanagement.com",
          "roles": ["ROLE_ADMIN", "ROLE_USER"]
        }
        """
)
public class AuthResponse {

    @Schema(
        description = "Access Token (JWT) para autenticación en peticiones subsecuentes. Duración corta (1 hora).",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;

    @Schema(
        description = "Refresh Token para renovar el access token cuando expire. Duración larga (7 días).",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String refreshToken;

    @Schema(
        description = "Tipo de token (siempre 'Bearer')",
        example = "Bearer",
        defaultValue = "Bearer"
    )
    private String type = "Bearer";

    @Schema(
        description = "Tiempo de expiración del access token en milisegundos",
        example = "3600000"
    )
    private Long expiresIn;

    @Schema(
        description = "Nombre de usuario del usuario autenticado",
        example = "admin"
    )
    private String username;

    @Schema(
        description = "Email del usuario autenticado",
        example = "admin@taskmanagement.com"
    )
    private String email;

    @Schema(
        description = "Roles asignados al usuario",
        example = "[\"ROLE_ADMIN\", \"ROLE_USER\"]"
    )
    private List<String> roles;

    /**
     * Constructor con todos los campos necesarios.
     */
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn,
                       String username, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.type = "Bearer";
        this.expiresIn = expiresIn;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    /**
     * Constructor sin refresh token (para compatibilidad)
     */
    public AuthResponse(String accessToken, String username, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.type = "Bearer";
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
