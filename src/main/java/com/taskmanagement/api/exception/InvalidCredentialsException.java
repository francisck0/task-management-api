package com.taskmanagement.api.exception;

/**
 * Excepción lanzada cuando las credenciales de login son inválidas.
 *
 * HTTP Status: 401 Unauthorized
 *
 * CASO DE USO:
 * ============
 *
 * Durante el login, si el username o password son incorrectos,
 * se lanza esta excepción.
 *
 * SEGURIDAD:
 * ==========
 *
 * Por razones de seguridad, el mensaje NO debe indicar si el username
 * o el password es el incorrecto. Siempre usar mensaje genérico:
 * "Credenciales inválidas"
 *
 * ESTO PREVIENE:
 * - Enumeración de usuarios (atacante saber qué usuarios existen)
 * - Timing attacks (diferencia de tiempo entre "user not found" vs "wrong password")
 *
 * EJEMPLO:
 * ========
 *
 * Usuario intenta login con credenciales incorrectas
 * → AuthenticationManager falla la autenticación
 * → Lanza InvalidCredentialsException
 * → GlobalExceptionHandler la captura
 * → Retorna 401 Unauthorized con mensaje genérico
 *
 * RESPUESTA HTTP:
 * ==============
 *
 * {
 *   "timestamp": "2025-11-16T10:30:00",
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "Credenciales inválidas",
 *   "path": "/api/v1/auth/login"
 * }
 *
 * MEJORES PRÁCTICAS:
 * =================
 *
 * ❌ MAL: "Usuario no encontrado" o "Contraseña incorrecta"
 * ✅ BIEN: "Credenciales inválidas" (genérico)
 *
 * ❌ MAL: Responder más rápido si el usuario no existe
 * ✅ BIEN: Tiempo de respuesta constante (usar hash dummy si user no existe)
 *
 * @see com.taskmanagement.api.service.AuthService#login
 * @see GlobalExceptionHandler
 */
public class InvalidCredentialsException extends RuntimeException {

    /**
     * Constructor con mensaje por defecto.
     *
     * IMPORTANTE: No incluir detalles específicos por seguridad.
     */
    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }

    /**
     * Constructor con mensaje personalizado.
     *
     * ADVERTENCIA: Usar con precaución. No exponer información sensible.
     *
     * @param message mensaje personalizado
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
