package com.taskmanagement.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
     * Maneja violaciones de integridad de datos en la base de datos.
     *
     * Se lanza cuando se violan constraints de BD como:
     * - Unique constraints (valor duplicado)
     * - Foreign key constraints (referencia inválida)
     * - Not null constraints (valor null no permitido)
     *
     * Devuelve código 409 (CONFLICT).
     *
     * @param ex excepción de integridad de datos
     * @param request contexto de la petición HTTP
     * @return respuesta de error con código 409
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            WebRequest request) {

        log.error("Violación de integridad de datos: {}", ex.getMessage());

        // Extraer mensaje más amigable del error de BD
        String message = "Violación de integridad de datos";
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();

        // Identificar tipo de constraint violado
        if (rootCause != null) {
            if (rootCause.contains("duplicate key") || rootCause.contains("unique constraint")) {
                message = "Ya existe un registro con estos datos. Por favor, use valores únicos.";
            } else if (rootCause.contains("foreign key constraint")) {
                message = "No se puede completar la operación debido a referencias con otros datos.";
            } else if (rootCause.contains("not-null constraint")) {
                message = "Faltan campos obligatorios en la solicitud.";
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Maneja errores de parsing de JSON en el request body.
     *
     * Se lanza cuando:
     * - El JSON está malformado
     * - Falta una llave o coma
     * - Tipos de datos no coinciden
     * - Valores de enum inválidos
     *
     * Devuelve código 400 (BAD_REQUEST).
     *
     * @param ex excepción de mensaje no legible
     * @param request contexto de la petición HTTP
     * @return respuesta de error con código 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            WebRequest request) {

        log.error("Error al leer el cuerpo de la petición: {}", ex.getMessage());

        String message = "El cuerpo de la petición es inválido o está mal formado";

        // Proporcionar mensaje más específico si es posible
        String rootCause = ex.getMessage();
        if (rootCause != null) {
            if (rootCause.contains("JSON parse error")) {
                message = "Error al parsear JSON. Verifique la sintaxis del JSON.";
            } else if (rootCause.contains("Cannot deserialize")) {
                message = "Tipo de dato inválido en el JSON. Verifique los tipos de los campos.";
            } else if (rootCause.contains("not one of the values accepted for Enum")) {
                message = "Valor de enumeración inválido. Verifique los valores permitidos.";
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de tipo de argumento en path variables o request params.
     *
     * Se lanza cuando:
     * - Se espera un Long pero se recibe texto: /tasks/abc
     * - Se espera un Enum pero se recibe valor inválido: /tasks/status/INVALID
     * - Tipos primitivos reciben valores no convertibles
     *
     * Devuelve código 400 (BAD_REQUEST).
     *
     * @param ex excepción de tipo de argumento incorrecto
     * @param request contexto de la petición HTTP
     * @return respuesta de error con código 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request) {

        log.error("Tipo de argumento incorrecto: {}", ex.getMessage());

        String parameterName = ex.getName();
        String requiredType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "desconocido";
        Object providedValue = ex.getValue();

        String message = String.format(
                "El parámetro '%s' debe ser de tipo %s. Valor proporcionado: '%s'",
                parameterName,
                requiredType,
                providedValue
        );

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores de autorización (Access Denied).
     *
     * Se lanza cuando:
     * - Usuario no tiene el rol requerido
     * - Usuario no tiene permisos para el recurso
     * - @PreAuthorize falla
     *
     * Devuelve código 403 (FORBIDDEN).
     *
     * @param ex excepción de acceso denegado
     * @param request contexto de la petición HTTP
     * @return respuesta de error con código 403
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request) {

        log.warn("Acceso denegado: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "No tiene permisos para acceder a este recurso",
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
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
