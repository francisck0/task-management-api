package com.taskmanagement.api.controller;

import com.taskmanagement.api.config.RateLimitProperties;
import com.taskmanagement.api.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de administración para Rate Limiting.
 *
 * ENDPOINTS DE ADMINISTRACIÓN:
 * - GET /admin/rate-limit/info: Información de configuración
 * - GET /admin/rate-limit/stats: Estadísticas del sistema
 * - POST /admin/rate-limit/clear-cache: Limpiar caché de buckets
 *
 * SEGURIDAD:
 * En producción, estos endpoints deben estar protegidos con autenticación
 * y solo accesibles para administradores (role ADMIN).
 *
 * Ejemplos de protección:
 * - @PreAuthorize("hasRole('ADMIN')")
 * - IP whitelist
 * - API key específica
 */
@RestController
@RequestMapping("/admin/rate-limit")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Rate Limit Admin",
    description = """
        Endpoints de administración para gestionar el sistema de Rate Limiting.

        **IMPORTANTE:** Estos endpoints deben estar protegidos y solo accesibles
        para administradores en producción.
        """
)
public class RateLimitAdminController {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;

    /**
     * Obtiene información de la configuración de rate limiting.
     *
     * Endpoint: GET /admin/rate-limit/info
     */
    @GetMapping("/info")
    @Operation(
        summary = "Obtener configuración de Rate Limiting",
        description = """
            Retorna la configuración actual del sistema de Rate Limiting.

            **Información incluida:**
            - Estado (habilitado/deshabilitado)
            - Capacidad del bucket
            - Tokens por período
            - Período de rellenado
            - Modo (por IP o global)
            - Paths excluidos

            Útil para verificar la configuración en diferentes entornos.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Configuración obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "enabled": true,
                          "capacity": 100,
                          "tokens": 100,
                          "periodMinutes": 1,
                          "perIp": true,
                          "excludedPaths": ["/actuator/**", "/swagger-ui/**"],
                          "description": "Permite 100 peticiones por minuto por IP"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getInfo() {
        log.info("Obteniendo información de configuración de rate limiting");

        Map<String, Object> info = new HashMap<>();
        info.put("enabled", rateLimitProperties.isEnabled());
        info.put("capacity", rateLimitProperties.getCapacity());
        info.put("tokens", rateLimitProperties.getTokens());
        info.put("periodMinutes", rateLimitProperties.getPeriod());
        info.put("perIp", rateLimitProperties.isPerIp());
        info.put("excludedPaths", rateLimitProperties.getExcludedPaths());

        // Descripción legible de la configuración
        String description = String.format(
            "Permite %d peticiones cada %d minuto(s) %s",
            rateLimitProperties.getTokens(),
            rateLimitProperties.getPeriod(),
            rateLimitProperties.isPerIp() ? "por IP" : "globalmente"
        );
        info.put("description", description);

        return ResponseEntity.ok(info);
    }

    /**
     * Obtiene estadísticas del sistema de rate limiting.
     *
     * Endpoint: GET /admin/rate-limit/stats
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Obtener estadísticas de Rate Limiting",
        description = """
            Retorna estadísticas del sistema de Rate Limiting.

            **Estadísticas incluidas:**
            - Número de clientes activos (con buckets en caché)
            - Configuración actual

            Útil para monitorear el uso del sistema y detectar posibles ataques.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Estadísticas obtenidas exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "activeBuckets": 42,
                          "enabled": true,
                          "capacity": 100,
                          "tokensPerPeriod": 100,
                          "periodMinutes": 1
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("Obteniendo estadísticas de rate limiting");

        Map<String, Object> stats = new HashMap<>();
        stats.put("activeBuckets", rateLimitService.getCacheSize());
        stats.put("enabled", rateLimitProperties.isEnabled());
        stats.put("capacity", rateLimitProperties.getCapacity());
        stats.put("tokensPerPeriod", rateLimitProperties.getTokens());
        stats.put("periodMinutes", rateLimitProperties.getPeriod());

        return ResponseEntity.ok(stats);
    }

    /**
     * Limpia la caché de buckets.
     *
     * ADVERTENCIA: Esta operación resetea todos los límites.
     * Todos los clientes volverán a tener el máximo de tokens disponibles.
     *
     * Endpoint: POST /admin/rate-limit/clear-cache
     */
    @PostMapping("/clear-cache")
    @Operation(
        summary = "Limpiar caché de Rate Limiting",
        description = """
            Limpia la caché de buckets de todos los clientes.

            **ADVERTENCIA:** Esta operación resetea todos los límites de rate limiting.
            Todos los clientes volverán a tener el máximo de tokens disponibles.

            **Casos de uso:**
            - Después de cambiar la configuración de rate limiting
            - Para liberar memoria si hay muchos buckets inactivos
            - En testing/debugging
            - Para resetear límites de un cliente problemático

            **Nota:** En producción, usar con precaución ya que permite que todos
            los clientes vuelvan a hacer peticiones inmediatamente.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Caché limpiada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "message": "Caché de rate limiting limpiada exitosamente",
                          "bucketsCleared": 42
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> clearCache() {
        log.warn("Limpiando caché de rate limiting - ADVERTENCIA: Esto resetea todos los límites");

        int previousSize = rateLimitService.getCacheSize();
        rateLimitService.clearCache();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Caché de rate limiting limpiada exitosamente");
        response.put("bucketsCleared", previousSize);

        return ResponseEntity.ok(response);
    }
}
