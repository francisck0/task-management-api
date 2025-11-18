package com.taskmanagement.api.filter;

import com.taskmanagement.api.constant.CorrelationIdConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro que gestiona el Correlation ID para tracing de requests.
 *
 * PROPÓSITO:
 * ==========
 *
 * Este filtro intercepta TODOS los requests HTTP y:
 * 1. Extrae o genera un Correlation ID único
 * 2. Lo agrega al MDC para que aparezca en todos los logs
 * 3. Lo propaga en la respuesta HTTP (header)
 * 4. Limpia el MDC al finalizar el request
 *
 * ORDEN DE EJECUCIÓN:
 * ==================
 *
 * @Order(1): Este filtro se ejecuta PRIMERO, antes que cualquier otro filtro,
 * para asegurar que el Correlation ID esté disponible desde el inicio.
 *
 * Orden típico de filtros:
 * 1. CorrelationIdFilter (este) - Orden 1
 * 2. CorsFilter - Orden 2
 * 3. SecurityFilter - Orden 3
 * 4. RateLimitFilter - Orden 4
 * 5. ... otros filtros
 *
 * FUNCIONAMIENTO:
 * ==============
 *
 * OncePerRequestFilter garantiza que el filtro se ejecute exactamente
 * una vez por request, incluso si hay forwards/includes internos.
 *
 * Flujo:
 * ```
 * Request → CorrelationIdFilter
 *           ├─ Extrae/Genera Correlation ID
 *           ├─ Agrega al MDC (thread-local)
 *           ├─ Ejecuta la cadena de filtros
 *           │  └─ Controllers/Services/etc (todos los logs incluyen correlation ID)
 *           ├─ Agrega Correlation ID a la respuesta (header)
 *           └─ Limpia el MDC
 * ```
 *
 * EJEMPLO:
 * ========
 *
 * Caso 1: Cliente envía Correlation ID
 * ```
 * Request:
 *   GET /api/v1/tasks
 *   X-Correlation-ID: abc-123
 *
 * → Filtro usa "abc-123"
 * → Logs: [correlationId=abc-123] ...
 * → Response header: X-Correlation-ID: abc-123
 * ```
 *
 * Caso 2: Cliente NO envía Correlation ID
 * ```
 * Request:
 *   GET /api/v1/tasks
 *   (sin header)
 *
 * → Filtro genera UUID: "550e8400-e29b-41d4-a716-446655440000"
 * → Logs: [correlationId=550e8400-e29b-41d4-a716-446655440000] ...
 * → Response header: X-Correlation-ID: 550e8400-e29b-41d4-a716-446655440000
 * ```
 *
 * INTEGRACIÓN CON LOGS:
 * ====================
 *
 * El pattern de logging en application.yml debe incluir %X{correlationId}:
 *
 * ```yaml
 * logging:
 *   pattern:
 *     console: "%d{yyyy-MM-dd HH:mm:ss} [%X{correlationId}] - %msg%n"
 * ```
 *
 * Resultado:
 * ```
 * 2025-11-16 10:30:00 [abc-123] - → Entrando a TaskController.getAllTasks()
 * 2025-11-16 10:30:01 [abc-123] - Buscando todas las tareas
 * 2025-11-16 10:30:02 [abc-123] - ← Saliendo de TaskController.getAllTasks()
 * ```
 *
 * BÚSQUEDA EN LOGS:
 * ================
 *
 * Con Correlation ID es fácil buscar todos los logs de un request:
 *
 * ```bash
 * # Grep por correlation ID
 * grep "abc-123" application.log
 *
 * # En ELK
 * correlationId:"abc-123"
 *
 * # En Splunk
 * correlationId=abc-123
 * ```
 *
 * MEJORES PRÁCTICAS:
 * =================
 *
 * 1. **Propagar en llamadas externas:**
 *    Al llamar a otros servicios, incluir el Correlation ID:
 *    ```java
 *    restTemplate.exchange(url, method,
 *        new HttpEntity<>(headers.set("X-Correlation-ID", correlationId)),
 *        responseType);
 *    ```
 *
 * 2. **Incluir en mensajes de cola:**
 *    Al publicar mensajes a Kafka/RabbitMQ, incluir el Correlation ID
 *    en los headers del mensaje.
 *
 * 3. **Logs estructurados:**
 *    Usar JSON logging para facilitar indexación:
 *    ```json
 *    {"timestamp":"...", "correlationId":"abc-123", "message":"..."}
 *    ```
 *
 * @see CorrelationIdConstants
 * @see org.slf4j.MDC
 */
@Component
@Order(1)  // Ejecutar primero, antes que otros filtros
@Slf4j
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Extraer o generar Correlation ID
            String correlationId = extractOrGenerateCorrelationId(request);

            // 2. Agregar al MDC (Mapped Diagnostic Context)
            // Esto hace que el correlation ID esté disponible en todos los logs
            // del thread actual usando %X{correlationId} en el pattern
            MDC.put(CorrelationIdConstants.CORRELATION_ID_MDC_KEY, correlationId);

            // 3. Agregar a la respuesta HTTP (header)
            // Esto permite al cliente correlacionar la respuesta con el request
            response.setHeader(CorrelationIdConstants.CORRELATION_ID_HEADER, correlationId);

            // Log de inicio del request (útil para debugging)
            log.debug("Request iniciado: {} {} [correlationId={}]",
                    request.getMethod(),
                    request.getRequestURI(),
                    correlationId);

            // 4. Continuar con la cadena de filtros
            filterChain.doFilter(request, response);

            // Log de fin del request
            log.debug("Request completado: {} {} [correlationId={}]",
                    request.getMethod(),
                    request.getRequestURI(),
                    correlationId);

        } finally {
            // 5. IMPORTANTE: Limpiar el MDC
            // El MDC es thread-local, si no se limpia, el correlation ID
            // podría "contaminar" requests subsecuentes procesados por el mismo thread
            // (los threads se reutilizan en el pool de Tomcat)
            MDC.clear();
        }
    }

    /**
     * Extrae el Correlation ID del header del request, o genera uno nuevo si no existe.
     *
     * LÓGICA:
     * 1. Buscar header X-Correlation-ID en el request
     * 2. Si existe y no está vacío → usar ese valor
     * 3. Si no existe o está vacío → generar nuevo UUID
     *
     * @param request request HTTP
     * @return correlation ID (existente o generado)
     */
    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CorrelationIdConstants.CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.trim().isEmpty()) {
            // No hay Correlation ID en el request → generar uno nuevo
            correlationId = UUID.randomUUID().toString();
            log.trace("Correlation ID generado: {}", correlationId);
        } else {
            // Ya existe Correlation ID en el request → reutilizarlo
            log.trace("Correlation ID extraído del header: {}", correlationId);
        }

        return correlationId;
    }

    /**
     * Método estático para obtener el Correlation ID actual desde cualquier parte del código.
     *
     * ÚTIL PARA:
     * - Agregarlo manualmente a logs
     * - Incluirlo en llamadas a servicios externos
     * - Agregarlo a mensajes de cola
     * - Incluirlo en respuestas personalizadas
     *
     * EJEMPLO:
     * ```java
     * String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
     * log.info("Procesando con correlation ID: {}", correlationId);
     * ```
     *
     * @return correlation ID actual, o null si no está en contexto de request
     */
    public static String getCurrentCorrelationId() {
        return MDC.get(CorrelationIdConstants.CORRELATION_ID_MDC_KEY);
    }
}
