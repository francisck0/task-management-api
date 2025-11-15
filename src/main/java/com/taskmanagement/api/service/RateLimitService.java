package com.taskmanagement.api.service;

import com.taskmanagement.api.config.RateLimitProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para gestionar rate limiting usando el algoritmo Token Bucket.
 *
 * CÓMO FUNCIONA:
 * 1. Cada cliente (identificado por IP) tiene su propio bucket
 * 2. Cada bucket tiene una capacidad máxima de tokens
 * 3. Cada petición consume 1 token
 * 4. Los tokens se rellenan a una tasa constante
 * 5. Si no hay tokens disponibles, la petición es rechazada (429 Too Many Requests)
 *
 * EJEMPLO DE CONFIGURACIÓN:
 * - Capacity: 100 tokens
 * - Refill: 100 tokens cada 1 minuto
 * - Resultado: Permite 100 peticiones/minuto con bursts de hasta 100
 *
 * THREAD-SAFE:
 * - Usa ConcurrentHashMap para almacenar buckets por IP
 * - Bucket4j es thread-safe por diseño
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final RateLimitProperties rateLimitProperties;

    /**
     * Almacena los buckets por IP del cliente.
     * ConcurrentHashMap garantiza thread-safety para acceso concurrente.
     *
     * Key: IP del cliente
     * Value: Bucket con tokens disponibles
     */
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Intenta consumir un token del bucket del cliente.
     *
     * @param clientId Identificador del cliente (típicamente su IP)
     * @return true si hay tokens disponibles, false si se excedió el límite
     */
    public boolean tryConsume(String clientId) {
        if (!rateLimitProperties.isEnabled()) {
            // Si rate limiting está deshabilitado, permitir todas las peticiones
            return true;
        }

        // Obtener o crear bucket para el cliente
        Bucket bucket = resolveBucket(clientId);

        // Intentar consumir 1 token
        boolean consumed = bucket.tryConsume(1);

        if (!consumed) {
            log.warn("Rate limit excedido para cliente: {}", clientId);
        }

        return consumed;
    }

    /**
     * Obtiene el número de tokens disponibles para un cliente.
     *
     * @param clientId Identificador del cliente
     * @return Número de tokens disponibles
     */
    public long getAvailableTokens(String clientId) {
        Bucket bucket = resolveBucket(clientId);
        return bucket.getAvailableTokens();
    }

    /**
     * Obtiene el bucket para un cliente, creándolo si no existe.
     *
     * @param clientId Identificador del cliente
     * @return Bucket para el cliente
     */
    private Bucket resolveBucket(String clientId) {
        // computeIfAbsent es thread-safe y atómico
        return cache.computeIfAbsent(clientId, key -> createNewBucket());
    }

    /**
     * Crea un nuevo bucket con la configuración especificada.
     *
     * CONFIGURACIÓN DEL BUCKET:
     * - Bandwidth: Define la capacidad y la tasa de rellenado
     * - Refill.intervally(): Rellena tokens a intervalos regulares
     *
     * Ejemplo:
     * - capacity=100, tokens=100, period=1 minuto
     * - Permite 100 peticiones/minuto
     * - Permite bursts de hasta 100 peticiones instantáneas
     *
     * @return Nuevo bucket configurado
     */
    private Bucket createNewBucket() {
        // Crear límite de ancho de banda
        Bandwidth limit = Bandwidth.classic(
                rateLimitProperties.getCapacity(),
                Refill.intervally(
                        rateLimitProperties.getTokens(),
                        Duration.ofMinutes(rateLimitProperties.getPeriod())
                )
        );

        // Crear y retornar bucket con el límite configurado
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Limpia la caché de buckets.
     * Útil para testing o para liberar memoria periódicamente.
     */
    public void clearCache() {
        cache.clear();
        log.info("Caché de rate limiting limpiada");
    }

    /**
     * Obtiene el número de clientes en la caché.
     *
     * @return Número de clientes con buckets activos
     */
    public int getCacheSize() {
        return cache.size();
    }
}
