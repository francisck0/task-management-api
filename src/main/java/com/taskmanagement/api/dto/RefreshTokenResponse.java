package com.taskmanagement.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para respuesta de renovación de access token.
 *
 * CAMPOS:
 * =======
 *
 * - accessToken: Nuevo JWT para acceder a recursos (corta duración)
 * - refreshToken: Nuevo refresh token si rotation está habilitada, o el mismo si no
 * - type: Tipo de token (siempre "Bearer")
 * - expiresIn: Tiempo de expiración del nuevo access token en milisegundos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Respuesta con nuevo access token y opcionalmente nuevo refresh token",
    example = """
        {
          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
          "refreshToken": "660e8400-e29b-41d4-a716-446655440001",
          "type": "Bearer",
          "expiresIn": 3600000
        }
        """
)
public class RefreshTokenResponse {

    @Schema(
        description = "Nuevo access token (JWT) para autenticación",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;

    @Schema(
        description = "Refresh token (nuevo si rotation habilitada, mismo si no)",
        example = "660e8400-e29b-41d4-a716-446655440001"
    )
    private String refreshToken;

    @Schema(
        description = "Tipo de token (siempre 'Bearer')",
        example = "Bearer",
        defaultValue = "Bearer"
    )
    private String type = "Bearer";

    @Schema(
        description = "Tiempo de expiración del nuevo access token en milisegundos",
        example = "3600000"
    )
    private Long expiresIn;

    /**
     * Constructor sin el campo 'type' (será 'Bearer' por defecto)
     */
    public RefreshTokenResponse(String accessToken, String refreshToken, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.type = "Bearer";
        this.expiresIn = expiresIn;
    }
}
