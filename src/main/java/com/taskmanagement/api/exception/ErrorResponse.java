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
 * - correlationId: ID único para tracing del request (NUEVO)
 * - errors: Lista de errores de validación (opcional)
 *
 * CORRELATION ID:
 * ==============
 *
 * El correlation ID permite rastrear el request completo a través del sistema:
 * - Buscar todos los logs relacionados con este error
 * - Debugging y troubleshooting
 * - Soporte al cliente (proveer el correlation ID para investigación)
 *
 * Ejemplo de respuesta de error con correlation ID:
 * ```json
 * {
 *   "timestamp": "2025-11-16T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Tarea no encontrada",
 *   "path": "/api/v1/tasks/123",
 *   "correlationId": "550e8400-e29b-41d4-a716-446655440000"
 * }
 * ```
 *
 * Con el correlation ID, puedes buscar todos los logs:
 * ```bash
 * grep "550e8400-e29b-41d4-a716-446655440000" application.log
 * ```
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
     * Correlation ID para tracing del request.
     *
     * Permite correlacionar logs y rastrear el flujo completo del request.
     * Útil para debugging y soporte al cliente.
     */
    private String correlationId;

    /**
     * Lista de errores de validación específicos (opcional)
     * Útil cuando hay múltiples errores de validación
     */
    private List<String> errors;

    /**
     * Constructor sin la lista de errores ni correlation ID (para errores simples)
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    /**
     * Constructor con correlation ID pero sin lista de errores
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, String correlationId) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.correlationId = correlationId;
    }
}
