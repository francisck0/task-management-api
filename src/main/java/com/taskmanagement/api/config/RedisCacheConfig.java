package com.taskmanagement.api.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de Redis Cache para la aplicación.
 *
 * PROPÓSITO:
 * - Configurar Redis como almacén de caché distribuido
 * - Definir estrategias de serialización para objetos Java
 * - Configurar TTL (Time To Live) para diferentes tipos de caché
 * - Optimizar performance reduciendo queries a PostgreSQL
 *
 * ============================================================================
 * ¿POR QUÉ REDIS CACHE?
 * ============================================================================
 *
 * PROBLEMA SIN CACHÉ:
 * - Cada GET /tasks/{id} golpea PostgreSQL
 * - Si 1000 usuarios consultan la misma tarea: 1000 queries a BD
 * - Latencia: ~50-200ms por query
 * - Carga innecesaria en BD
 *
 * SOLUCIÓN CON REDIS:
 * - Primera consulta: Query a PostgreSQL + Guardar en Redis
 * - Siguientes consultas: Leer desde Redis (sin tocar BD)
 * - Latencia: <1ms desde Redis
 * - Reduce carga en PostgreSQL en 90-99%
 *
 * BENEFICIOS:
 * ✅ Performance: Latencia ultra-baja (microsegundos vs milisegundos)
 * ✅ Escalabilidad: Reduce carga en BD permitiendo más usuarios
 * ✅ Disponibilidad: BD puede estar bajo mantenimiento, caché sigue funcionando
 * ✅ Costo: Menos recursos de BD necesarios
 *
 * ============================================================================
 * ESTRATEGIA DE CACHÉ
 * ============================================================================
 *
 * 1. TASKS (Caché de tareas individuales)
 *    - Clave: task-api::tasks::123
 *    - TTL: 30 minutos
 *    - Eviction: Manual con @CacheEvict en update/delete
 *    - Caso de uso: GET /tasks/{id}
 *
 * 2. TASKS_BY_USER (Caché de listas de tareas por usuario)
 *    - Clave: task-api::tasksByUser::userId
 *    - TTL: 15 minutos (más corto porque cambia frecuentemente)
 *    - Eviction: Manual con @CacheEvict en create/update/delete
 *    - Caso de uso: GET /tasks?userId=123
 *
 * 3. TASK_STATISTICS (Estadísticas de tareas)
 *    - Clave: task-api::taskStats::userId
 *    - TTL: 5 minutos (datos que cambian frecuentemente)
 *    - Eviction: Automática por TTL
 *    - Caso de uso: Dashboard de estadísticas
 *
 * ============================================================================
 * SERIALIZACIÓN
 * ============================================================================
 *
 * JACKSON JSON SERIALIZATION:
 * - Objetos Java → JSON → Redis
 * - Redis → JSON → Objetos Java
 *
 * VENTAJAS:
 * - Legible en Redis (puedes inspeccionar con redis-cli)
 * - Compatible con diferentes versiones de clases
 * - Soporta tipos complejos (LocalDateTime, enums, etc)
 *
 * ALTERNATIVAS:
 * - JDK Serialization: Más rápido pero no legible, frágil con cambios de clase
 * - Protobuf/Avro: Muy eficiente pero más complejo
 *
 * ============================================================================
 * CACHE EVICTION (INVALIDACIÓN)
 * ============================================================================
 *
 * CUÁNDO INVALIDAR:
 * - Al crear: Invalidar lista de tareas del usuario
 * - Al actualizar: Invalidar tarea específica + lista de tareas del usuario
 * - Al eliminar: Invalidar tarea específica + lista de tareas del usuario
 *
 * ESTRATEGIAS:
 * - @CacheEvict: Elimina entradas específicas
 * - @CachePut: Actualiza caché con nuevo valor
 * - TTL: Expiración automática por tiempo
 *
 * ============================================================================
 * MONITOREO
 * ============================================================================
 *
 * Comandos Redis útiles:
 * - redis-cli KEYS "task-api::*"          # Ver todas las claves
 * - redis-cli GET "task-api::tasks::123"  # Ver valor de una clave
 * - redis-cli TTL "task-api::tasks::123"  # Ver tiempo restante
 * - redis-cli FLUSHDB                     # Limpiar toda la caché (desarrollo)
 * - redis-cli INFO stats                  # Estadísticas de Redis
 *
 * ============================================================================
 *
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.cache.annotation.CacheEvict
 * @see org.springframework.cache.annotation.CachePut
 */
@Configuration
@EnableCaching
public class RedisCacheConfig {

    /**
     * Nombres de cachés disponibles.
     *
     * Usar constantes en lugar de strings hardcodeadas para:
     * - Type safety
     * - Refactoring más seguro
     * - Autocomplete en IDE
     */
    public static final String CACHE_TASKS = "tasks";
    public static final String CACHE_TASKS_BY_USER = "tasksByUser";
    public static final String CACHE_TASK_STATISTICS = "taskStats";

    /**
     * Configura el CacheManager de Spring con Redis como backend.
     *
     * FUNCIONAMIENTO:
     * 1. Spring intercepta métodos con @Cacheable
     * 2. Genera una clave basada en parámetros del método
     * 3. Busca la clave en Redis
     * 4. Si existe: retorna valor de Redis (cache hit)
     * 5. Si no existe: ejecuta método, guarda resultado en Redis (cache miss)
     *
     * @param connectionFactory Factory de conexiones a Redis (auto-configurado por Spring Boot)
     * @return CacheManager configurado con Redis
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Configuración base para todos los cachés
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                // TTL por defecto: 30 minutos
                // Después de este tiempo, las entradas expiran automáticamente
                .entryTtl(Duration.ofMinutes(30))

                // Serializer para las claves (siempre String)
                // Ejemplo de clave: "task-api::tasks::123"
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new StringRedisSerializer()
                        )
                )

                // Serializer para los valores (objetos Java serializados a JSON)
                // Usa Jackson para convertir Task, List<Task>, etc. a JSON
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                createGenericJackson2JsonRedisSerializer()
                        )
                )

                // Deshabilitar caché de valores null
                // Previene cache penetration attacks donde se consultan IDs que no existen
                .disableCachingNullValues();

        // ====================================================================
        // CONFIGURACIONES ESPECÍFICAS POR CACHÉ
        // ====================================================================
        // Cada caché puede tener diferente TTL según su caso de uso
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // CACHE: tasks
        // Tareas individuales consultadas frecuentemente
        // TTL: 30 minutos (datos relativamente estables)
        cacheConfigurations.put(CACHE_TASKS,
                defaultConfig.entryTtl(Duration.ofMinutes(30))
        );

        // CACHE: tasksByUser
        // Listas de tareas por usuario
        // TTL: 15 minutos (cambia cuando se crea/actualiza/elimina una tarea)
        cacheConfigurations.put(CACHE_TASKS_BY_USER,
                defaultConfig.entryTtl(Duration.ofMinutes(15))
        );

        // CACHE: taskStats
        // Estadísticas de tareas (conteos, promedios, etc)
        // TTL: 5 minutos (datos que cambian frecuentemente)
        cacheConfigurations.put(CACHE_TASK_STATISTICS,
                defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        // Construir el CacheManager con las configuraciones
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                // Transaction-aware: Sincroniza caché con transacciones de BD
                // Si la transacción falla, el caché no se actualiza
                .transactionAware()
                .build();
    }

    /**
     * Crea un serializador JSON personalizado para Redis.
     *
     * CONFIGURACIÓN:
     * - Soporta LocalDateTime, LocalDate (JavaTimeModule)
     * - Incluye información de tipo en JSON para deserialización correcta
     * - Maneja polimorfismo (si Task tiene subclases)
     *
     * EJEMPLO DE JSON EN REDIS:
     * {
     *   "@class": "com.taskmanagement.api.model.Task",
     *   "id": 123,
     *   "title": "Implementar caché Redis",
     *   "status": "IN_PROGRESS",
     *   "createdAt": "2025-11-16T10:30:00",
     *   "user": {
     *     "@class": "com.taskmanagement.api.model.User",
     *     "id": 1,
     *     "username": "frank"
     *   }
     * }
     *
     * @return Serializador JSON configurado
     */
    private GenericJackson2JsonRedisSerializer createGenericJackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();

        // ====================================================================
        // MÓDULOS
        // ====================================================================

        // JavaTimeModule: Soporta tipos de fecha/hora de Java 8+
        // Sin esto, LocalDateTime se serializaría como un número o fallaría
        objectMapper.registerModule(new JavaTimeModule());

        // Deshabilitar serialización de fechas como timestamps
        // Serializa como string ISO-8601: "2025-11-16T10:30:00"
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // ====================================================================
        // TYPE INFORMATION
        // ====================================================================

        // Incluir información de tipo en JSON para deserialización correcta
        // IMPORTANTE: Sin esto, Redis no sabría cómo reconstruir objetos Task
        //
        // BasicPolymorphicTypeValidator: Validador de seguridad
        // - Previene deserialization attacks (Remote Code Execution)
        // - Solo permite deserializar clases de nuestro paquete
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)  // Permitir Object (base de todo)
                .build();

        // Activar tipo polimórfico por defecto
        // Agrega campo "@class" en JSON con el tipo completo de la clase
        objectMapper.activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,  // Solo para clases no-final
                JsonTypeInfo.As.PROPERTY               // Como propiedad "@class"
        );

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * Bean opcional de RedisTemplate para operaciones manuales con Redis.
     *
     * CUÁNDO USAR:
     * - @Cacheable es declarativo: Spring gestiona automáticamente
     * - RedisTemplate es imperativo: Control manual del caché
     *
     * CASOS DE USO:
     * - Operaciones complejas de caché no soportadas por @Cacheable
     * - Invalidación masiva de caché
     * - Búsquedas por patrón (KEYS, SCAN)
     * - Operaciones atómicas (INCR, DECR)
     * - Rate limiting distribuido
     *
     * EJEMPLO DE USO:
     * ```java
     * @Autowired
     * private RedisTemplate<String, Object> redisTemplate;
     *
     * // Guardar manualmente en caché
     * redisTemplate.opsForValue().set("myKey", myValue, Duration.ofMinutes(10));
     *
     * // Leer desde caché
     * Object value = redisTemplate.opsForValue().get("myKey");
     *
     * // Eliminar por patrón
     * Set<String> keys = redisTemplate.keys("task-api::tasks::*");
     * redisTemplate.delete(keys);
     * ```
     *
     * @param connectionFactory Factory de conexiones a Redis
     * @return RedisTemplate configurado para operaciones manuales
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = createGenericJackson2JsonRedisSerializer();

        // Configurar serializers para diferentes tipos de operaciones
        template.setKeySerializer(stringSerializer);           // Claves: String
        template.setValueSerializer(jsonSerializer);           // Valores: JSON
        template.setHashKeySerializer(stringSerializer);       // Claves de Hash: String
        template.setHashValueSerializer(jsonSerializer);       // Valores de Hash: JSON

        template.afterPropertiesSet();
        return template;
    }
}
