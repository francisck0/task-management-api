package com.taskmanagement.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de renovación de access token.
 *
 * FLUJO:
 * ======
 *
 * 1. Cliente detecta que el access token expiró (401 Unauthorized)
 * 2. Cliente envía el refresh token a /api/v1/auth/refresh
 * 3. Backend valida el refresh token
 * 4. Backend genera nuevo access token
 * 5. Backend retorna nuevo access token (y opcionalmente nuevo refresh token)
 *
 * EJEMPLO DE USO:
 * ==============
 *
 * POST /api/v1/auth/refresh
 * Content-Type: application/json
 *
 * {
 *   "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
 * }
 *
 * Respuesta:
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "refreshToken": "660e8400-e29b-41d4-a716-446655440001",  // Nuevo si rotation habilitada
 *   "type": "Bearer",
 *   "expiresIn": 3600000
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Solicitud para renovar un access token expirado usando un refresh token",
    example = """
        {
          "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
        }
        """
)
public class RefreshTokenRequest {

    @NotBlank(message = "El refresh token es obligatorio")
    @Schema(
        description = "Refresh token obtenido durante el login o último refresh",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true
    )
    private String refreshToken;
}
