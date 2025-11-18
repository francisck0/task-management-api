package com.taskmanagement.api.constant;

/**
 * Constantes para el manejo de Correlation IDs.
 *
 * CORRELATION ID:
 * ==============
 *
 * Un Correlation ID es un identificador único asociado a cada request HTTP
 * que permite rastrear y correlacionar logs a través de todo el sistema.
 *
 * PROPÓSITO:
 * ==========
 *
 * 1. **Tracing de Requests:**
 *    - Rastrear un request específico a través de múltiples componentes
 *    - Útil en arquitecturas de microservicios o sistemas distribuidos
 *
 * 2. **Debugging:**
 *    - Buscar todos los logs relacionados a un request específico
 *    - Ejemplo: grep "correlation-id=abc123" application.log
 *
 * 3. **Análisis de Flujo:**
 *    - Entender el flujo completo de un request
 *    - Medir tiempos de respuesta por componente
 *
 * 4. **Soporte al Cliente:**
 *    - Cliente reporta un error con el correlation ID
 *    - Soporte puede rastrear exactamente qué pasó
 *
 * FLUJO:
 * ======
 *
 * 1. Cliente envía request (con o sin X-Correlation-ID header)
 * 2. CorrelationIdFilter intercepta el request:
 *    - Si tiene header X-Correlation-ID → lo usa
 *    - Si no tiene → genera uno nuevo (UUID)
 * 3. Agrega el correlation ID al MDC (Mapped Diagnostic Context)
 * 4. Todos los logs del request incluyen el correlation ID automáticamente
 * 5. Agrega el correlation ID a la respuesta HTTP (header)
 * 6. Limpia el MDC al finalizar el request
 *
 * EJEMPLO DE USO:
 * ==============
 *
 * Request:
 * ```
 * GET /api/v1/tasks/123
 * X-Correlation-ID: abc-123-def-456
 * ```
 *
 * Logs generados (todos con el mismo correlation ID):
 * ```
 * [correlation-id=abc-123-def-456] → Entrando a TaskQueryController.getTaskById()
 * [correlation-id=abc-123-def-456] → Buscando tarea con ID: 123
 * [correlation-id=abc-123-def-456] → Tarea encontrada: Completar documentación
 * [correlation-id=abc-123-def-456] ← Saliendo de TaskQueryController.getTaskById()
 * ```
 *
 * Response:
 * ```
 * HTTP/1.1 200 OK
 * X-Correlation-ID: abc-123-def-456
 * ...
 * ```
 *
 * Si hay error, el correlation ID se incluye en la respuesta:
 * ```json
 * {
 *   "timestamp": "2025-11-16T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Tarea no encontrada",
 *   "path": "/api/v1/tasks/123",
 *   "correlationId": "abc-123-def-456"
 * }
 * ```
 *
 * INTEGRACIÓN CON HERRAMIENTAS:
 * ============================
 *
 * - **ELK Stack:** Indexar por correlation ID para búsquedas rápidas
 * - **Grafana:** Filtrar métricas por correlation ID
 * - **Jaeger/Zipkin:** Usar como trace ID
 * - **APM Tools:** Correlacionar con transacciones
 *
 * @see com.taskmanagement.api.filter.CorrelationIdFilter
 * @see com.taskmanagement.api.exception.ErrorResponse
 */
public final class CorrelationIdConstants {

    /**
     * Nombre del header HTTP para el Correlation ID.
     *
     * Estándar de facto en la industria: X-Correlation-ID
     *
     * Alternativas comunes:
     * - X-Request-ID
     * - X-Trace-ID
     * - Request-ID
     */
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    /**
     * Clave para almacenar el Correlation ID en el MDC (Mapped Diagnostic Context).
     *
     * MDC es un mapa thread-local de SLF4J que permite agregar contexto
     * a todos los logs generados en el mismo thread.
     */
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    /**
     * Constructor privado para prevenir instanciación.
     */
    private CorrelationIdConstants() {
        throw new UnsupportedOperationException("Esta es una clase de constantes y no debe ser instanciada");
    }
}
