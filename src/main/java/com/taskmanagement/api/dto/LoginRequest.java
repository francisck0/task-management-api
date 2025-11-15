package com.taskmanagement.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para peticiones de login/autenticación.
 *
 * Contiene las credenciales del usuario para autenticarse en el sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Credenciales de login para autenticación",
    example = """
        {
          "username": "admin",
          "password": "admin123"
        }
        """
)
public class LoginRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Schema(
        description = "Nombre de usuario",
        example = "admin",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Schema(
        description = "Contraseña del usuario",
        example = "admin123",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "password"
    )
    private String password;
}
