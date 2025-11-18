package com.taskmanagement.api.exception;

/**
 * Excepción personalizada para cuando un usuario intenta acceder a un recurso
 * sin tener los permisos necesarios.
 *
 * Capa EXCEPTION: Excepción de autorización personalizada del dominio.
 *
 * Extends RuntimeException:
 * - Es una excepción no verificada (unchecked exception)
 * - No es necesario declararla en la firma del método con "throws"
 * - Permite un código más limpio y legible
 *
 * Esta excepción se lanza cuando un usuario autenticado intenta acceder
 * o modificar un recurso que no le pertenece o para el cual no tiene permiso.
 *
 * DIFERENCIA CON 401 UNAUTHORIZED:
 * - 401: Usuario no autenticado (no ha iniciado sesión)
 * - 403 FORBIDDEN: Usuario autenticado pero sin permisos (esta excepción)
 *
 * CASOS DE USO:
 * - Usuario intenta modificar/eliminar tarea de otro usuario
 * - Usuario sin rol ADMIN intenta acceder a endpoints de administración
 * - Usuario intenta acceder a recursos de otro usuario
 */
public class ForbiddenException extends RuntimeException {

    /**
     * Constructor que acepta un mensaje de error.
     *
     * @param message mensaje descriptivo del error
     */
    public ForbiddenException(String message) {
        super(message);
    }

    /**
     * Constructor que acepta un mensaje y una causa raíz.
     *
     * @param message mensaje descriptivo del error
     * @param cause excepción que causó este error
     */
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
