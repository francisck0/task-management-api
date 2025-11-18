package com.taskmanagement.api.constant;

/**
 * Constantes de versionado de API.
 *
 * VERSIONADO DE API - ESTRATEGIA:
 * ================================
 *
 * Esta aplicación usa versionado de API mediante URL Path versioning,
 * que es el enfoque más común y recomendado en APIs REST.
 *
 * ENFOQUE IMPLEMENTADO:
 * - Versión en URL: /api/v1/tasks, /api/v2/tasks
 * - Base path configurado en application.yml (context-path)
 * - Controladores con versión explícita usando estas constantes
 *
 * VENTAJAS DE URL PATH VERSIONING:
 * - ✅ Explícito y visible en la URL
 * - ✅ Fácil de cachear (diferentes URLs)
 * - ✅ Simple de testear y documentar
 * - ✅ Soportado por todos los navegadores y herramientas
 * - ✅ No requiere headers especiales
 *
 * ALTERNATIVAS NO IMPLEMENTADAS:
 * 1. Header Versioning: Accept: application/vnd.company.v1+json
 * 2. Query Parameter: /api/tasks?version=1
 * 3. Media Type: Accept: application/json;version=1
 *
 * ESTRUCTURA DE VERSIONES:
 * ========================
 *
 * Nivel 1 - Context Path (Global en application.yml):
 *   server.servlet.context-path=/api
 *
 * Nivel 2 - Versión (Constantes en esta clase):
 *   V1 = "/v1"
 *   V2 = "/v2"
 *
 * Nivel 3 - Recurso (Controladores):
 *   TASKS = "/tasks"
 *   AUTH = "/auth"
 *
 * URL Final: /api/v1/tasks
 *             └─┬┘└┬┘└──┬─┘
 *            Context Version Resource
 *
 * ESTRATEGIA DE MÚLTIPLES VERSIONES:
 * ===================================
 *
 * Para mantener múltiples versiones en paralelo:
 *
 * 1. CONTROLLERS SEPARADOS (Recomendado):
 *    ```java
 *    // V1
 *    @RestController
 *    @RequestMapping(ApiVersion.V1 + "/tasks")
 *    public class TaskControllerV1 { ... }
 *
 *    // V2 (nueva versión)
 *    @RestController
 *    @RequestMapping(ApiVersion.V2 + "/tasks")
 *    public class TaskControllerV2 { ... }
 *    ```
 *
 * 2. MISMO CONTROLLER (No recomendado):
 *    ```java
 *    @GetMapping(ApiVersion.V1 + "/tasks")
 *    public ResponseEntity<?> getTasksV1() { ... }
 *
 *    @GetMapping(ApiVersion.V2 + "/tasks")
 *    public ResponseEntity<?> getTasksV2() { ... }
 *    ```
 *
 * CICLO DE VIDA DE VERSIONES:
 * ============================
 *
 * 1. ACTUAL (V1):
 *    - Versión estable en producción
 *    - Recibe bugfixes
 *    - No se agregan nuevas features
 *
 * 2. BETA (V2):
 *    - Nueva versión en desarrollo
 *    - Puede tener breaking changes
 *    - Documentada como "beta" en Swagger
 *
 * 3. DEPRECATED (V0):
 *    - Versión antigua marcada para eliminación
 *    - Retorna warning header: Deprecation: true
 *    - Fecha de sunset comunicada
 *
 * 4. SUNSET (eliminada):
 *    - Versión ya no disponible
 *    - Retorna HTTP 410 Gone
 *
 * DEPRECATION DE VERSIONES:
 * ==========================
 *
 * Para deprecar una versión:
 *
 * 1. Marcar como @Deprecated en código:
 *    ```java
 *    @Deprecated(since = "2.0", forRemoval = true)
 *    public static final String V1 = "/v1";
 *    ```
 *
 * 2. Agregar header en respuestas:
 *    ```java
 *    response.addHeader("Deprecation", "true");
 *    response.addHeader("Sunset", "Wed, 01 Jan 2026 00:00:00 GMT");
 *    response.addHeader("Link", "</api/v2/tasks>; rel=\"successor-version\"");
 *    ```
 *
 * 3. Documentar en Swagger:
 *    ```java
 *    @Operation(
 *        summary = "Get tasks",
 *        deprecated = true,
 *        description = "⚠️ DEPRECATED: Use /api/v2/tasks instead"
 *    )
 *    ```
 *
 * BREAKING CHANGES:
 * =================
 *
 * Cuando hacer una nueva versión (breaking changes):
 * - ❌ Cambiar estructura de response (eliminar/renombrar campos)
 * - ❌ Cambiar tipos de datos (string → number)
 * - ❌ Eliminar endpoints
 * - ❌ Cambiar códigos de estado HTTP
 * - ❌ Cambiar autenticación/autorización
 *
 * Cambios que NO requieren nueva versión (backward compatible):
 * - ✅ Agregar nuevos campos opcionales
 * - ✅ Agregar nuevos endpoints
 * - ✅ Agregar nuevos parámetros opcionales
 * - ✅ Bugfixes
 * - ✅ Mejoras de performance
 *
 * BEST PRACTICES:
 * ===============
 *
 * 1. ✅ Mantener máximo 2 versiones activas simultáneamente
 * 2. ✅ Dar al menos 6 meses de notice antes de sunset
 * 3. ✅ Documentar cambios en changelog
 * 4. ✅ Usar semantic versioning para releases
 * 5. ✅ Probar compatibilidad con tests de integración
 * 6. ✅ Monitorear uso de versiones antiguas
 * 7. ✅ Comunicar deprecations a usuarios
 *
 * REFERENCIAS:
 * ============
 *
 * - RFC 8594 - Sunset HTTP Header: https://www.rfc-editor.org/rfc/rfc8594.html
 * - REST API Versioning: https://restfulapi.net/versioning/
 * - Microsoft API Guidelines: https://github.com/microsoft/api-guidelines
 */
public final class ApiVersion {

    // =========================================================================
    // CONSTRUCTOR PRIVADO - Clase de utilidad, no instanciable
    // =========================================================================

    private ApiVersion() {
        throw new UnsupportedOperationException("Esta es una clase de constantes y no debe ser instanciada");
    }

    // =========================================================================
    // VERSIONES DE API
    // =========================================================================

    /**
     * Versión 1 de la API (actual).
     *
     * Estado: ESTABLE
     * Desde: 2025-01-15
     * Deprecado: No
     *
     * Uso:
     * <pre>
     * {@code
     * @RestController
     * @RequestMapping(ApiVersion.V1 + "/tasks")
     * public class TaskController { ... }
     * }
     * </pre>
     *
     * URL resultante: /api/v1/tasks
     */
    public static final String V1 = "/v1";

    /**
     * Versión 2 de la API (futura).
     *
     * Estado: PLANIFICADA
     * Desde: TBD
     * Deprecado: No
     *
     * Cuando se implemente V2:
     * - Mantener V1 activo durante al menos 6 meses
     * - Marcar V1 como @Deprecated
     * - Documentar breaking changes
     * - Agregar migration guide
     *
     * Uso (ejemplo para el futuro):
     * <pre>
     * {@code
     * @RestController
     * @RequestMapping(ApiVersion.V2 + "/tasks")
     * public class TaskControllerV2 { ... }
     * }
     * </pre>
     */
    // public static final String V2 = "/v2";

    // =========================================================================
    // VERSIÓN POR DEFECTO
    // =========================================================================

    /**
     * Versión actual/por defecto de la API.
     *
     * Apunta a la versión estable más reciente.
     * Actualizar cuando se lance una nueva versión.
     */
    public static final String CURRENT = V1;

    /**
     * Versión legacy/antigua (si existe).
     *
     * Solo definir si se está manteniendo una versión anterior
     * durante el período de migración.
     */
    // public static final String LEGACY = "/v0";

    // =========================================================================
    // METADATA DE VERSIONES
    // =========================================================================

    /**
     * Version string para headers y metadata.
     *
     * Usado en:
     * - HTTP Header: X-API-Version: 1.0.0
     * - OpenAPI: info.version
     * - Response body metadata
     */
    public static final String VERSION_STRING = "1.0.0";

    /**
     * Formato de version header para respuestas.
     *
     * Uso:
     * <pre>
     * {@code
     * response.addHeader(ApiVersion.VERSION_HEADER, ApiVersion.VERSION_STRING);
     * }
     * </pre>
     */
    public static final String VERSION_HEADER = "X-API-Version";

    // =========================================================================
    // HELPER METHODS (Opcional - para lógica de versionado avanzada)
    // =========================================================================

    /**
     * Obtiene la versión desde un path.
     *
     * Ejemplo: "/v1/tasks" → "v1"
     *
     * @param path path de la request
     * @return versión extraída o null si no se encuentra
     */
    public static String extractVersionFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        // Buscar patrón /v{número}
        String[] parts = path.split("/");
        for (String part : parts) {
            if (part.matches("v\\d+")) {
                return part;
            }
        }

        return null;
    }

    /**
     * Verifica si una versión es la actual.
     *
     * @param version versión a verificar (ej: "/v1")
     * @return true si es la versión actual
     */
    public static boolean isCurrent(String version) {
        return CURRENT.equals(version);
    }

    /**
     * Verifica si una versión está deprecada.
     *
     * Implementar lógica según las versiones deprecadas.
     *
     * @param version versión a verificar
     * @return true si está deprecada
     */
    public static boolean isDeprecated(String version) {
        // Ejemplo: Si V0 está deprecado
        // return "/v0".equals(version);
        return false; // Por ahora no hay versiones deprecadas
    }
}
