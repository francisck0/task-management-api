package com.taskmanagement.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración para Rate Limiting.
 *
 * RATE LIMITING:
 * Técnica para controlar el número de peticiones que un cliente puede hacer
 * a la API en un período de tiempo determinado.
 *
 * BENEFICIOS:
 * - Protección contra ataques DDoS y abuso
 * - Garantiza uso justo de recursos
 * - Previene sobrecarga del servidor
 * - Mejora la estabilidad del sistema
 *
 * ALGORITMO TOKEN BUCKET:
 * - Cada cliente tiene un "bucket" (cubo) con tokens
 * - Cada petición consume 1 token
 * - Los tokens se rellenan a una tasa constante
 * - Si no hay tokens disponibles, la petición es rechazada
 *
 * CONFIGURACIÓN:
 * Las propiedades se cargan desde application.yml bajo el prefijo "rate-limit"
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
@Data
public class RateLimitProperties {

    /**
     * Habilitar o deshabilitar rate limiting globalmente.
     * Default: true
     */
    private boolean enabled = true;

    /**
     * Capacidad del bucket (número máximo de tokens).
     * Representa el número máximo de peticiones que se pueden hacer
     * en ráfaga (burst).
     *
     * Ejemplo: capacity=100 permite hasta 100 peticiones inmediatas
     * Default: 100 peticiones
     */
    private long capacity = 100;

    /**
     * Número de tokens que se rellenan por intervalo.
     * Representa cuántas peticiones se pueden hacer por período.
     *
     * Ejemplo: tokens=100 con period=1 minuto = 100 peticiones/minuto
     * Default: 100 tokens
     */
    private long tokens = 100;

    /**
     * Duración del intervalo de rellenado en minutos.
     * Define cada cuánto tiempo se rellenan los tokens.
     *
     * Ejemplo: period=1 significa que cada minuto se rellenan los tokens
     * Default: 1 minuto
     */
    private long period = 1;

    /**
     * Si es true, el rate limiting se aplica por IP.
     * Si es false, el rate limiting se aplica globalmente.
     *
     * POR IP: Cada dirección IP tiene su propio bucket
     * GLOBAL: Todas las peticiones comparten el mismo bucket
     *
     * Default: true (por IP)
     */
    private boolean perIp = true;

    /**
     * Lista de paths que están excluidos del rate limiting.
     * Útil para endpoints públicos como documentación, health checks, etc.
     *
     * Ejemplo: ["/actuator/**", "/swagger-ui/**", "/v3/api-docs/**"]
     */
    private String[] excludedPaths = new String[]{
        "/actuator/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/favicon.ico"
    };
}
