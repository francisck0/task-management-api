package com.taskmanagement.api.exception;

/**
 * Excepción personalizada para cuando no se encuentra un recurso.
 *
 * Capa EXCEPTION: Contiene excepciones personalizadas del dominio.
 *
 * Extends RuntimeException:
 * - Es una excepción no verificada (unchecked exception)
 * - No es necesario declararla en la firma del método con "throws"
 * - Permite un código más limpio y legible
 *
 * Esta excepción se lanza cuando se intenta acceder a un recurso
 * que no existe en la base de datos (por ejemplo, tarea no encontrada).
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de error.
     *
     * @param message mensaje descriptivo del error
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y una causa raíz.
     *
     * @param message mensaje descriptivo del error
     * @param cause excepción que causó este error
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
