package com.taskmanagement.api.dto;

import com.taskmanagement.api.model.TaskPriority;
import com.taskmanagement.api.model.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para las respuestas de consulta de tareas.
 *
 * Este DTO se usa para devolver información de tareas al cliente.
 * Incluye todos los campos relevantes incluyendo id y timestamps.
 *
 * VENTAJAS DE USAR DTOS DE RESPUESTA:
 * - Control preciso sobre qué datos se exponen en la API
 * - Evita exponer la estructura interna del modelo de dominio
 * - Permite agregar campos calculados o derivados sin modificar la entidad
 * - Previene problemas de serialización con relaciones JPA (lazy loading, ciclos)
 * - Facilita el versionado de la API
 * - Mejora la documentación con @Schema annotations
 *
 * ¿POR QUÉ NO ES UN RECORD?
 * Aunque este DTO es de solo lectura y sería ideal como Record,
 * usamos una clase normal por compatibilidad con mappers y frameworks.
 * Si en el futuro migramos a MapStruct o similar, será fácil convertirlo a Record.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Respuesta con los datos completos de una tarea",
    example = """
        {
          "id": 1,
          "title": "Comprar ingredientes para la cena",
          "description": "Comprar tomates, cebolla, ajo y pasta",
          "status": "PENDING",
          "dueDate": "2025-11-15T18:00:00",
          "createdAt": "2025-11-12T10:30:00",
          "updatedAt": "2025-11-12T10:30:00",
          "userId": 1,
          "username": "admin"
        }
        """
)
public class TaskResponseDto {

    @Schema(description = "ID único de la tarea", example = "1")
    private Long id;

    @Schema(
        description = "Título de la tarea",
        example = "Comprar ingredientes para la cena",
        maxLength = 100
    )
    private String title;

    @Schema(
        description = "Descripción detallada de la tarea",
        example = "Comprar tomates, cebolla, ajo y pasta en el supermercado",
        nullable = true
    )
    private String description;

    @Schema(
        description = "Estado actual de la tarea",
        example = "PENDING",
        allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"}
    )
    private TaskStatus status;

    @Schema(
        description = "Prioridad de la tarea (importancia/urgencia)",
        example = "MEDIUM",
        allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"}
    )
    private TaskPriority priority;

    @Schema(
        description = "Fecha y hora límite para completar la tarea",
        example = "2025-11-15T18:00:00",
        type = "string",
        format = "date-time",
        nullable = true
    )
    private LocalDateTime dueDate;

    @Schema(
        description = "Fecha y hora de creación de la tarea",
        example = "2025-11-12T10:30:00",
        type = "string",
        format = "date-time",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime createdAt;

    @Schema(
        description = "Fecha y hora de la última actualización",
        example = "2025-11-12T10:30:00",
        type = "string",
        format = "date-time",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime updatedAt;

    /**
     * ID del usuario propietario de la tarea
     *
     * SEGURIDAD: Solo incluimos el ID y username, NO datos sensibles
     * como email, contraseña, etc.
     *
     * UTILIDAD:
     * - Permite al frontend identificar el propietario
     * - Útil para mostrar "Creado por: username"
     * - No expone información privada
     */
    @Schema(
        description = "ID del usuario propietario de la tarea",
        example = "1",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private Long userId;

    /**
     * Nombre de usuario del propietario de la tarea
     */
    @Schema(
        description = "Nombre de usuario del propietario",
        example = "admin",
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private String username;

    /**
     * Fecha y hora de eliminación lógica (soft delete)
     *
     * - null: Tarea activa (no eliminada)
     * - timestamp: Tarea eliminada lógicamente en esa fecha/hora
     *
     * UTILIDAD:
     * - Permite al frontend identificar tareas eliminadas
     * - Útil para mostrar en la papelera
     * - Permite calcular tiempo desde eliminación
     */
    @Schema(
        description = "Fecha y hora de eliminación de la tarea (null si no está eliminada)",
        example = "2025-11-18T15:00:00",
        type = "string",
        format = "date-time",
        nullable = true,
        accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime deletedAt;
}
