package com.taskmanagement.api.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspecto de AOP para logging automático de controllers y services.
 *
 * PROGRAMACIÓN ORIENTADA A ASPECTOS (AOP):
 * =======================================
 *
 * AOP permite separar cross-cutting concerns (preocupaciones transversales)
 * del código de negocio. En este caso: LOGGING.
 *
 * SIN AOP (código repetitivo en cada método):
 * ```java
 * public TaskDto getTask(Long id) {
 *     log.info("Llamando a getTask con id: {}", id);
 *     try {
 *         TaskDto result = taskRepository.findById(id);
 *         log.info("getTask retornó: {}", result);
 *         return result;
 *     } catch (Exception e) {
 *         log.error("Error en getTask", e);
 *         throw e;
 *     }
 * }
 * ```
 *
 * CON AOP (logging automático sin contaminar código):
 * ```java
 * public TaskDto getTask(Long id) {
 *     return taskRepository.findById(id);  // ¡Código limpio!
 * }
 * // El logging se maneja automáticamente por este aspecto
 * ```
 *
 * CONCEPTOS CLAVE DE AOP:
 * =======================
 *
 * - Aspect: Esta clase (LoggingAspect)
 * - Join Point: Punto en la ejecución donde se puede aplicar el aspecto
 * - Pointcut: Expresión que define DÓNDE aplicar el aspecto
 * - Advice: Qué hacer (Before, After, Around, etc.)
 *
 * POINTCUT EXPRESSIONS:
 * ====================
 *
 * execution(* com.example.Controller.*(..))
 *     └─┬─┘ └────────┬──────────┘ └┬┘└─┬─┘
 *    return   package.clase    método params
 *
 * Ejemplos:
 * - execution(* *.*(..))                 → Todos los métodos
 * - execution(* com.example..*.*(..))    → Todos en paquete com.example
 * - execution(public * *(..))            → Solo métodos públicos
 * - @annotation(Auditable)               → Métodos con @Auditable
 * - within(@RestController *)            → Clases con @RestController
 *
 * CONFIGURACIÓN:
 * =============
 *
 * Spring Boot habilita AOP automáticamente con spring-boot-starter-aop.
 * No requiere configuración adicional.
 *
 * @see Auditable
 * @see AuditAspect
 * @see PerformanceAspect
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // =========================================================================
    // POINTCUT DEFINITIONS - Definen DÓNDE aplicar los advices
    // =========================================================================

    /**
     * Pointcut para todos los métodos de controllers.
     *
     * Matches: Cualquier método en clases del paquete
     * com.taskmanagement.api.controller y subpaquetes
     */
    @Pointcut("execution(* com.taskmanagement.api.controller..*(..))")
    public void controllerMethods() {
        // Este método solo define el pointcut, no tiene implementación
    }

    /**
     * Pointcut para todos los métodos de services.
     *
     * Matches: Cualquier método en clases del paquete
     * com.taskmanagement.api.service.impl y subpaquetes
     */
    @Pointcut("execution(* com.taskmanagement.api.service.impl..*(..))")
    public void serviceMethods() {
        // Este método solo define el pointcut, no tiene implementación
    }

    /**
     * Pointcut para métodos públicos de controllers y services.
     *
     * Combina ambos pointcuts anteriores.
     */
    @Pointcut("controllerMethods() || serviceMethods()")
    public void applicationMethods() {
        // Este método solo define el pointcut, no tiene implementación
    }

    // =========================================================================
    // ADVICES - Definen QUÉ hacer en los puntos de corte
    // =========================================================================

    /**
     * Advice BEFORE: Se ejecuta ANTES del método target.
     *
     * USO: Logging de entrada a métodos.
     *
     * CUÁNDO: Antes de que el método se ejecute
     * PUEDE: Inspeccionar parámetros, validar precondiciones
     * NO PUEDE: Modificar el resultado (aún no existe), prevenir la ejecución
     *
     * @param joinPoint información del método siendo ejecutado
     */
    @Before("applicationMethods()")
    public void logMethodEntry(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // Evitar toString() en entidades Hibernate para prevenir ConcurrentModificationException
        log.debug("→ Entrando a {}.{}() con {} argumentos",
                className,
                methodName,
                args != null ? args.length : 0);
    }

    /**
     * Advice AFTER RETURNING: Se ejecuta DESPUÉS del método, solo si retorna normalmente.
     *
     * USO: Logging de salida exitosa de métodos.
     *
     * CUÁNDO: Después de que el método retorna exitosamente
     * PUEDE: Inspeccionar el resultado, logging
     * NO PUEDE: Modificar el resultado (ya fue retornado)
     *
     * @param joinPoint información del método
     * @param result resultado retornado por el método
     */
    @AfterReturning(pointcut = "applicationMethods()", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // No loggear el resultado completo para evitar logs muy largos
        // Solo indicar que retornó exitosamente
        log.debug("← Saliendo de {}.{}() - Ejecución exitosa",
                className,
                methodName);
    }

    /**
     * Advice AFTER THROWING: Se ejecuta DESPUÉS del método, solo si lanza excepción.
     *
     * USO: Logging de errores.
     *
     * CUÁNDO: Después de que el método lanza una excepción
     * PUEDE: Logging del error, métricas de errores
     * NO PUEDE: Prevenir que la excepción se propague
     *
     * @param joinPoint información del método
     * @param exception excepción lanzada
     */
    @AfterThrowing(pointcut = "applicationMethods()", throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.error("✗ Excepción en {}.{}(): {} - {}",
                className,
                methodName,
                exception.getClass().getSimpleName(),
                exception.getMessage());

        // Stack trace completo solo en nivel TRACE para debugging
        log.trace("Stack trace completo:", exception);
    }

    /**
     * Advice AFTER: Se ejecuta DESPUÉS del método, siempre (éxito o error).
     *
     * USO: Cleanup, logging final, métricas.
     *
     * CUÁNDO: Después de que el método termina (sea exitoso o con error)
     * SIMILAR A: finally en try-catch
     * ÚTIL PARA: Liberar recursos, logging final
     *
     * @param joinPoint información del método
     */
    @After("applicationMethods()")
    public void logMethodFinally(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.trace("⊗ Finalizando ejecución de {}.{}()",
                className,
                methodName);
    }

    /**
     * Advice AROUND: Se ejecuta ALREDEDOR del método.
     *
     * USO: Control completo de la ejecución.
     *
     * CUÁNDO: Antes, durante y después del método
     * PUEDE: Todo - modificar parámetros, resultado, prevenir ejecución, manejar excepciones
     * MÁS PODEROSO: Pero también más peligroso, usar con cuidado
     * RESPONSABILIDAD: DEBE llamar a proceed() para ejecutar el método target
     *
     * CASOS DE USO:
     * - Medir tiempo de ejecución
     * - Transacciones
     * - Retry logic
     * - Cache
     * - Modificar parámetros/resultado
     *
     * @param joinPoint información del método con control de ejecución
     * @return resultado del método
     * @throws Throwable si el método lanza excepción
     */
    @Around("controllerMethods()")
    public Object logControllerMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // Timestamp de inicio
        long startTime = System.currentTimeMillis();

        try {
            log.info("▶ Controller: {}.{}() - Iniciando procesamiento",
                    className,
                    methodName);

            // IMPORTANTE: Llamar a proceed() para ejecutar el método real
            Object result = joinPoint.proceed();

            // Calcular tiempo de ejecución
            long executionTime = System.currentTimeMillis() - startTime;

            log.info("✓ Controller: {}.{}() - Completado en {} ms",
                    className,
                    methodName,
                    executionTime);

            return result;

        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;

            log.error("✗ Controller: {}.{}() - Error después de {} ms: {}",
                    className,
                    methodName,
                    executionTime,
                    throwable.getMessage());

            // Re-lanzar la excepción para que no se oculte
            throw throwable;
        }
    }

    // =========================================================================
    // LOGGING CONDICIONAL (Ejemplos avanzados)
    // =========================================================================

    /**
     * Ejemplo: Logging solo de métodos específicos por nombre.
     *
     * Usa expresión regular en pointcut.
     */
    // @Before("execution(* com.taskmanagement.api.controller.*Controller.create*(..))")
    // public void logCreateMethods(JoinPoint joinPoint) {
    //     log.warn("🆕 Operación de CREACIÓN detectada: {}",
    //             joinPoint.getSignature().toShortString());
    // }

    /**
     * Ejemplo: Logging solo de métodos con anotación específica.
     */
    // @Before("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    // public void logPostMappings(JoinPoint joinPoint) {
    //     log.info("📬 POST request: {}", joinPoint.getSignature().getName());
    // }

    /**
     * Ejemplo: Logging detallado solo si está habilitado el nivel DEBUG.
     */
    // @Around("applicationMethods()")
    // public Object logDetailedIfDebug(ProceedingJoinPoint joinPoint) throws Throwable {
    //     if (log.isDebugEnabled()) {
    //         // Logging detallado con parámetros
    //         log.debug("Parámetros: {}", Arrays.toString(joinPoint.getArgs()));
    //     }
    //     return joinPoint.proceed();
    // }
}
