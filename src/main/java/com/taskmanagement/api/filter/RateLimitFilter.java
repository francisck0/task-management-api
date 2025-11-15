package com.taskmanagement.api.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.api.config.RateLimitProperties;
import com.taskmanagement.api.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Filtro HTTP para aplicar rate limiting a las peticiones.
 *
 * FILTROS EN SPRING:
 * - Se ejecutan ANTES de que la petición llegue al controlador
 * - OncePerRequestFilter garantiza que se ejecuta solo una vez por petición
 * - Es el lugar ideal para validaciones transversales (rate limiting, autenticación, etc)
 *
 * FLUJO DE EJECUCIÓN:
 * 1. Cliente hace petición HTTP → 2. RateLimitFilter intercepta
 * 3. Verifica si el path está excluido
 * 4. Obtiene IP del cliente
 * 5. Intenta consumir token del bucket
 * 6. Si hay tokens: continúa al controlador
 * 7. Si no hay tokens: retorna 429 Too Many Requests
 *
 * HEADERS HTTP ESTÁNDAR:
 * - X-RateLimit-Limit: Límite total de peticiones
 * - X-RateLimit-Remaining: Peticiones restantes
 * - X-RateLimit-Retry-After: Segundos hasta que se rellenen tokens
 *
 * ORDEN DE FILTROS:
 * Este filtro debe ejecutarse temprano en la cadena para evitar
 * procesamiento innecesario de peticiones que serán rechazadas.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;
    private final ObjectMapper objectMapper;

    // Matcher para comparar paths con patrones (soporta wildcards como **)
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Método principal que procesa cada petición HTTP.
     *
     * @param request Petición HTTP
     * @param response Respuesta HTTP
     * @param filterChain Cadena de filtros
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Si rate limiting está deshabilitado, continuar sin validar
        if (!rateLimitProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Verificar si el path está excluido del rate limiting
        String requestPath = request.getRequestURI();
        if (isExcludedPath(requestPath)) {
            log.debug("Path excluido de rate limiting: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // Obtener identificador del cliente (IP)
        String clientId = getClientId(request);

        // Intentar consumir un token
        boolean allowed = rateLimitService.tryConsume(clientId);

        if (allowed) {
            // Hay tokens disponibles, permitir la petición
            addRateLimitHeaders(response, clientId);
            filterChain.doFilter(request, response);
        } else {
            // No hay tokens, rechazar con 429 Too Many Requests
            handleRateLimitExceeded(response, clientId);
        }
    }

    /**
     * Verifica si un path está excluido del rate limiting.
     *
     * @param requestPath Path de la petición
     * @return true si está excluido, false en caso contrario
     */
    private boolean isExcludedPath(String requestPath) {
        String[] excludedPaths = rateLimitProperties.getExcludedPaths();
        if (excludedPaths == null || excludedPaths.length == 0) {
            return false;
        }

        for (String pattern : excludedPaths) {
            if (pathMatcher.match(pattern, requestPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Obtiene el identificador del cliente (su IP).
     *
     * NOTA: Considera headers de proxy para obtener la IP real:
     * - X-Forwarded-For: IP cuando hay proxy/load balancer
     * - X-Real-IP: IP real del cliente
     *
     * @param request Petición HTTP
     * @return IP del cliente
     */
    private String getClientId(HttpServletRequest request) {
        // Intentar obtener IP real si hay proxy
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For puede contener múltiples IPs, tomar la primera
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Si no hay headers de proxy, usar la IP remota directa
        return request.getRemoteAddr();
    }

    /**
     * Agrega headers informativos de rate limiting a la respuesta.
     *
     * HEADERS ESTÁNDAR (de facto):
     * - X-RateLimit-Limit: Capacidad total del bucket
     * - X-RateLimit-Remaining: Tokens restantes
     * - X-RateLimit-Reset: (opcional) Timestamp de próximo reset
     *
     * @param response Respuesta HTTP
     * @param clientId ID del cliente
     */
    private void addRateLimitHeaders(HttpServletResponse response, String clientId) {
        long remaining = rateLimitService.getAvailableTokens(clientId);
        long limit = rateLimitProperties.getCapacity();

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
    }

    /**
     * Maneja el caso cuando se excede el límite de peticiones.
     *
     * Retorna respuesta 429 Too Many Requests con un cuerpo JSON informativo.
     *
     * ESTRUCTURA DE RESPUESTA:
     * {
     *   "timestamp": "2025-11-15T19:30:00",
     *   "status": 429,
     *   "error": "Too Many Requests",
     *   "message": "Has excedido el límite de peticiones. Intenta nuevamente más tarde.",
     *   "path": "/api/v1/tasks",
     *   "limit": 100,
     *   "retryAfter": 60
     * }
     *
     * @param response Respuesta HTTP
     * @param clientId ID del cliente
     */
    private void handleRateLimitExceeded(HttpServletResponse response, String clientId)
            throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Agregar headers de rate limiting
        long limit = rateLimitProperties.getCapacity();
        long retryAfter = rateLimitProperties.getPeriod() * 60; // en segundos

        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setHeader("Retry-After", String.valueOf(retryAfter));

        // Crear cuerpo de respuesta JSON
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        errorResponse.put("error", "Too Many Requests");
        errorResponse.put("message", "Has excedido el límite de peticiones. Intenta nuevamente más tarde.");
        errorResponse.put("limit", limit);
        errorResponse.put("retryAfter", retryAfter + " segundos");

        // Escribir respuesta JSON
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));

        log.warn("Rate limit excedido para cliente: {} - Límite: {}/{}min",
                clientId, limit, rateLimitProperties.getPeriod());
    }
}
