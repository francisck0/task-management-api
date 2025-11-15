package com.taskmanagement.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para respuestas de autenticación.
 *
 * Contiene el token JWT y la información del usuario autenticado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Respuesta de autenticación con token JWT y datos del usuario",
    example = """
        {
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          "type": "Bearer",
          "username": "admin",
          "email": "admin@taskmanagement.com",
          "roles": ["ROLE_ADMIN", "ROLE_USER"]
        }
        """
)
public class AuthResponse {

    @Schema(
        description = "Token JWT para autenticación en peticiones subsecuentes",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String token;

    @Schema(
        description = "Tipo de token (siempre 'Bearer')",
        example = "Bearer",
        defaultValue = "Bearer"
    )
    private String type = "Bearer";

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
     * Constructor sin el campo 'type' (será 'Bearer' por defecto)
     */
    public AuthResponse(String token, String username, String email, List<String> roles) {
        this.token = token;
        this.type = "Bearer";
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}
