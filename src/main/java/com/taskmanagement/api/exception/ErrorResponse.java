package com.taskmanagement.api.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuestas de error estandarizadas.
 *
 * Proporciona un formato consistente para todos los errores de la API.
 * Esto mejora la experiencia del cliente al consumir la API.
 *
 * Campos incluidos:
 * - timestamp: Momento en que ocurrió el error
 * - status: Código HTTP del error
 * - error: Nombre del error HTTP
 * - message: Mensaje descriptivo del error
 * - path: Ruta de la petición que causó el error
 * - errors: Lista de errores de validación (opcional)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Fecha y hora en que ocurrió el error
     */
    private LocalDateTime timestamp;

    /**
     * Código de estado HTTP
     */
    private int status;

    /**
     * Nombre del error HTTP (ej: "Not Found", "Bad Request")
     */
    private String error;

    /**
     * Mensaje descriptivo del error
     */
    private String message;

    /**
     * Ruta de la petición que generó el error
     */
    private String path;

    /**
     * Lista de errores de validación específicos (opcional)
     * Útil cuando hay múltiples errores de validación
     */
    private List<String> errors;

    /**
     * Constructor sin la lista de errores (para errores simples)
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
