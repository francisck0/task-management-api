package com.taskmanagement.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para peticiones de registro de nuevos usuarios.
 *
 * Contiene todos los datos necesarios para crear una nueva cuenta de usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Datos para registrar un nuevo usuario",
    example = """
        {
          "username": "johndoe",
          "email": "john.doe@example.com",
          "password": "SecurePass123",
          "fullName": "John Doe"
        }
        """
)
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Schema(
        description = "Nombre de usuario único",
        example = "johndoe",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 3,
        maxLength = 50
    )
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Schema(
        description = "Email único del usuario",
        example = "john.doe@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "email"
    )
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Schema(
        description = "Contraseña del usuario (mínimo 6 caracteres)",
        example = "SecurePass123",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "password",
        minLength = 6
    )
    private String password;

    @Size(max = 100, message = "El nombre completo no puede superar los 100 caracteres")
    @Schema(
        description = "Nombre completo del usuario (opcional)",
        example = "John Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 100,
        nullable = true
    )
    private String fullName;
}
