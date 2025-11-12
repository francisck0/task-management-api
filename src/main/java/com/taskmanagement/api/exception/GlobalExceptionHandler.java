package com.taskmanagement.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manejador global de excepciones para toda la aplicación.
 *
 * @RestControllerAdvice: Combina @ControllerAdvice + @ResponseBody
 * - Intercepta excepciones lanzadas por cualquier controlador
 * - Permite manejar errores de forma centralizada
 * - Devuelve respuestas JSON consistentes
 *
 * Beneficios:
 * - Código DRY (Don't Repeat Yourself): manejo de errores en un solo lugar
 * - Respuestas de error consistentes en toda la API
 * - Separación de concerns: los controladores no manejan excepciones
 * - Facilita logging y monitoreo de errores
 *
 * @ExceptionHandler: Define qué método maneja qué tipo de excepción
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja la excepción ResourceNotFoundException.
     *
     * Se lanza cuando un recurso solicitado no existe (ej: tarea no encontrada).
     * Devuelve un código 404 (NOT_FOUND).
     *
     * @param ex excepción lanzada
     * @param request contexto de la petición HTTP
     * @return respuesta de error con código 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {

        log.error("Recurso no encontrado: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Maneja errores de validación de datos (Bean Validation).
     *
     * Se lanza cuando un DTO con @Valid tiene campos que no cumplen las validaciones
     * definidas con anotaciones como @NotBlank, @Size, etc.
     *
     * Devuelve código 400 (BAD_REQUEST) con la lista detallada de errores.
     *
     * @param ex excepción de validación
     * @param request contexto de la petición HTTP
     * @return respuesta con lista de errores de validación y código 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        log.error("Error de validación en la petición");

        // Extraer todos los errores de validación
        List<String> validationErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.add(String.format("%s: %s", fieldName, errorMessage));
        });

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Error de validación en los datos proporcionados",
                request.getDescription(false).replace("uri=", ""),
                validationErrors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja IllegalArgumentException.
     *
     * Se lanza cuando se proporciona un argumento inválido a un método.
     * Devuelve código 400 (BAD_REQUEST).
     *
     * @param ex excepción lanzada
     * @param request contexto de la petición HTTP
     * @return respuesta de error con código 400
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        log.error("Argumento ilegal: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja cualquier otra excepción no capturada específicamente.
     *
     * Actúa como un "catch-all" para errores inesperados.
     * Devuelve código 500 (INTERNAL_SERVER_ERROR).
     *
     * IMPORTANTE: No exponer detalles internos en producción (seguridad).
     *
     * @param ex excepción lanzada
     * @param request contexto de la petición HTTP
     * @return respuesta de error con código 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {

        // Log del stack trace completo para debugging
        log.error("Error interno del servidor", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ha ocurrido un error interno en el servidor. Por favor, contacte al administrador.",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
