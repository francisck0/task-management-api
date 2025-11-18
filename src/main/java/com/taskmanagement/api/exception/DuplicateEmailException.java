package com.taskmanagement.api.exception;

/**
 * Excepción lanzada cuando se intenta registrar un usuario con un email que ya existe.
 *
 * HTTP Status: 409 Conflict
 *
 * CASO DE USO:
 * ============
 *
 * Durante el registro de un nuevo usuario, si el email proporcionado
 * ya está en uso por otro usuario, se lanza esta excepción.
 *
 * EJEMPLO:
 * ========
 *
 * Usuario intenta registrarse con email "john@example.com"
 * → Sistema verifica que "john@example.com" ya existe
 * → Lanza DuplicateEmailException
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
 *   "message": "El email 'john@example.com' ya está en uso",
 *   "path": "/api/v1/auth/register"
 * }
 *
 * @see com.taskmanagement.api.service.AuthService#register
 * @see GlobalExceptionHandler
 */
public class DuplicateEmailException extends RuntimeException {

    /**
     * Constructor con mensaje personalizado.
     *
     * @param email email duplicado
     */
    public DuplicateEmailException(String email) {
        super("El email '" + email + "' ya está en uso");
    }

    /**
     * Constructor con mensaje genérico.
     */
    public DuplicateEmailException() {
        super("El email ya está en uso");
    }
}
