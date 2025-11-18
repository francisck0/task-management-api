package com.taskmanagement.api.exception;

/**
 * Excepción lanzada cuando se intenta registrar un usuario con un username que ya existe.
 *
 * HTTP Status: 409 Conflict
 *
 * CASO DE USO:
 * ============
 *
 * Durante el registro de un nuevo usuario, si el username proporcionado
 * ya está en uso por otro usuario, se lanza esta excepción.
 *
 * EJEMPLO:
 * ========
 *
 * Usuario intenta registrarse con username "johndoe"
 * → Sistema verifica que "johndoe" ya existe
 * → Lanza DuplicateUsernameException
 * → GlobalExceptionHandler la captura
 * → Retorna 409 Conflict con mensaje apropiado
 *
 * RESPUESTA HTTP:
 * ==============
 *
 * {
 *   "timestamp": "2025-11-16T10:30:00",
 *   "status": 409,
 *   "error": "Conflict",
 *   "message": "El nombre de usuario 'johndoe' ya está en uso",
 *   "path": "/api/v1/auth/register"
 * }
 *
 * @see com.taskmanagement.api.service.AuthService#register
 * @see GlobalExceptionHandler
 */
public class DuplicateUsernameException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param username nombre de usuario duplicado
     */
    public DuplicateUsernameException(String username) {
        super("El nombre de usuario '" + username + "' ya está en uso");
    }

    /**
     * Constructor con mensaje genérico.
     */
    public DuplicateUsernameException() {
        super("El nombre de usuario ya está en uso");
    }
}
