package com.taskmanagement.api.exception;

/**
 * Excepción lanzada cuando no se encuentra un rol en la base de datos.
 *
 * HTTP Status: 500 Internal Server Error
 *
 * CASO DE USO:
 * ============
 *
 * Durante el registro o asignación de roles, si un rol requerido
 * no existe en la base de datos, se lanza esta excepción.
 *
 * CAUSAS COMUNES:
 * ==============
 *
 * 1. No se ejecutó el script de inicialización (data.sql)
 * 2. Los roles fueron eliminados manualmente de la BD
 * 3. Error en la migración de base de datos
 *
 * EJEMPLO:
 * ========
 *
 * Usuario intenta registrarse
 * → Sistema busca rol "ROLE_USER"
 * → Rol no existe en la tabla roles
 * → Lanza RoleNotFoundException
 * → GlobalExceptionHandler la captura
 * → Retorna 500 Internal Server Error
 *
 * SOLUCIÓN:
 * =========
 *
 * 1. Verificar que data.sql se ejecutó correctamente
 * 2. Ejecutar manualmente:
 *    INSERT INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN');
 *
 * RESPUESTA HTTP:
 * ==============
 *
 * {
 *   "timestamp": "2025-11-16T10:30:00",
 *   "status": 500,
 *   "error": "Internal Server Error",
 *   "message": "Rol 'ROLE_USER' no encontrado en la base de datos. Ejecute el script de inicialización.",
 *   "path": "/api/v1/auth/register"
 * }
 *
 * @see com.taskmanagement.api.service.AuthService#register
 * @see GlobalExceptionHandler
 */
public class RoleNotFoundException extends RuntimeException {

    /**
     * Constructor con nombre del rol no encontrado.
     *
     * @param roleName nombre del rol que no se encontró
     */
    public RoleNotFoundException(String roleName) {
        super("Rol '" + roleName + "' no encontrado en la base de datos. " +
              "Ejecute el script de inicialización (data.sql)");
    }

    /**
     * Constructor con mensaje genérico.
     */
    public RoleNotFoundException() {
        super("Rol no encontrado en la base de datos");
    }
}
