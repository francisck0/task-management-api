package com.taskmanagement.api.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Aspecto de AOP para monitoreo de rendimiento y métricas.
 *
 * PROPÓSITO:
 * ==========
 *
 * Monitorea automáticamente el rendimiento de la aplicación:
 * - Tiempo de ejecución de métodos
 * - Métodos lentos (que exceden umbrales)
 * - Métricas para Prometheus/Grafana
 * - Detección de cuellos de botella
 *
 * INTEGRACIÓN CON MICROMETER:
 * ===========================
 *
 * Este aspecto se integra con Micrometer (incluido en Spring Boot Actuator)
 * para exponer métricas en formato Prometheus. Las métricas están disponibles en:
 *
 * http://localhost:8080/actuator/prometheus
 *
 * MÉTRICAS GENERADAS:
 * ==================
 *
 * - method_execution_seconds_count: Número de ejecuciones
 * - method_execution_seconds_sum: Tiempo total de todas las ejecuciones
 * - method_execution_seconds_max: Tiempo máximo de ejecución
 *
 * Etiquetas (tags):
 * - class: Nombre de la clase
 * - method: Nombre del método
 * - status: success/failure
 *
 * EJEMPLO DE MÉTRICA EN PROMETHEUS:
 * =================================
 *
 * ```
 * method_execution_seconds_count{class="TaskServiceImpl",method="createTask",status="success"} 1523
 * method_execution_seconds_sum{class="TaskServiceImpl",method="createTask",status="success"} 45.234
 * method_execution_seconds_max{class="TaskServiceImpl",method="createTask",status="success"} 1.234
 * ```
 *
 * ALERTAS AUTOMÁTICAS:
 * ===================
 *
 * El aspecto detecta automáticamente métodos "lentos" que exceden umbrales:
 * - WARNING: > 1 segundo
 * - CRITICAL: > 5 segundos
 *
 * VISUALIZACIÓN EN GRAFANA:
 * =========================
 *
 * Puedes crear dashboards en Grafana para visualizar:
 * - Métodos más lentos
 * - Tendencias de rendimiento
 * - Percentiles (p50, p95, p99)
 * - Comparación entre métodos
 *
 * @see LoggingAspect
 * @see AuditAspect
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class PerformanceAspect {

    private final MeterRegistry meterRegistry;

    // Umbrales de rendimiento (en milisegundos)
    private static final long WARNING_THRESHOLD_MS = 1000;   // 1 segundo
    private static final long CRITICAL_THRESHOLD_MS = 5000;  // 5 segundos

    // =========================================================================
    // POINTCUT DEFINITIONS
    // =========================================================================

    /**
     * Pointcut para todos los métodos públicos de services.
     *
     * Solo monitoreamos services porque:
     * - Contienen la lógica de negocio
     * - Son los que potencialmente pueden ser lentos
     * - Controllers son generalmente rápidos (delegan a services)
     */
    @Pointcut("execution(public * com.taskmanagement.api.service.impl..*(..))")
    public void servicePublicMethods() {
        // Este método solo define el pointcut
    }

    /**
     * Pointcut para métodos de repositorios.
     *
     * Monitorear repositorios es útil para detectar:
     * - Queries lentas
     * - Problemas de índices
     * - N+1 queries
     */
    @Pointcut("execution(* com.taskmanagement.api.repository..*(..))")
    public void repositoryMethods() {
        // Este método solo define el pointcut
    }

    // =========================================================================
    // PERFORMANCE MONITORING
    // =========================================================================

    /**
     * Monitorea el rendimiento de métodos de service.
     *
     * Para cada ejecución:
     * 1. Mide el tiempo de ejecución
     * 2. Registra métricas en Micrometer
     * 3. Logea advertencias si excede umbrales
     * 4. Detecta y reporta métodos lentos
     *
     * @param joinPoint información del método con control de ejecución
     * @return resultado del método monitoreado
     * @throws Throwable si el método lanza excepción
     */
    @Around("servicePublicMethods()")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // Crear un timer de Micrometer para esta ejecución
        Timer.Sample sample = Timer.start(meterRegistry);

        long startTime = System.currentTimeMillis();
        String status = "success";
        Throwable exception = null;

        try {
            // Ejecutar el método real
            Object result = joinPoint.proceed();
            return result;

        } catch (Throwable throwable) {
            status = "failure";
            exception = throwable;
            throw throwable;

        } finally {
            // Calcular tiempo de ejecución
            long executionTime = System.currentTimeMillis() - startTime;

            // Registrar métrica en Micrometer con tags
            sample.stop(Timer.builder("method.execution.seconds")
                    .description("Tiempo de ejecución de métodos")
                    .tag("class", className)
                    .tag("method", methodName)
                    .tag("status", status)
                    .register(meterRegistry));

            // Logging y alertas basadas en umbrales
            logPerformanceMetrics(className, methodName, executionTime, status, exception);
        }
    }

    /**
     * Monitorea el rendimiento de métodos de repositorio.
     *
     * Similar al monitoreo de services, pero específico para:
     * - Detectar queries lentas
     * - Identificar problemas de base de datos
     * - Monitorear acceso a datos
     *
     * @param joinPoint información del método con control de ejecución
     * @return resultado del método monitoreado
     * @throws Throwable si el método lanza excepción
     */
    @Around("repositoryMethods()")
    public Object monitorRepositoryPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        Timer.Sample sample = Timer.start(meterRegistry);

        long startTime = System.currentTimeMillis();
        String status = "success";

        try {
            Object result = joinPoint.proceed();
            return result;

        } catch (Throwable throwable) {
            status = "failure";
            throw throwable;

        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            // Registrar métrica con tag adicional para repositorios
            sample.stop(Timer.builder("repository.query.seconds")
                    .description("Tiempo de ejecución de queries de repositorio")
                    .tag("repository", className)
                    .tag("method", methodName)
                    .tag("status", status)
                    .register(meterRegistry));

            // Alertas para queries lentas
            if (executionTime > 500) {  // Queries > 500ms son sospechosas
                log.warn("⚠️  [PERFORMANCE] SLOW QUERY: {}.{}() tomó {} ms",
                        className,
                        methodName,
                        executionTime);
            }
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Logea métricas de rendimiento con diferentes niveles según umbrales.
     *
     * @param className nombre de la clase
     * @param methodName nombre del método
     * @param executionTime tiempo de ejecución en ms
     * @param status estado de ejecución (success/failure)
     * @param exception excepción si hubo error
     */
    private void logPerformanceMetrics(String className, String methodName,
                                       long executionTime, String status,
                                       Throwable exception) {

        String methodSignature = className + "." + methodName + "()";

        // CRITICAL: Métodos extremadamente lentos
        if (executionTime > CRITICAL_THRESHOLD_MS) {
            log.error("🔴 [PERFORMANCE CRITICAL] {} tomó {} ms ({} segundos) - Status: {}",
                    methodSignature,
                    executionTime,
                    String.format("%.2f", executionTime / 1000.0),
                    status);

            if (exception != null) {
                log.error("   └─ Error: {}: {}", exception.getClass().getSimpleName(), exception.getMessage());
            }

            // TODO: Enviar alerta a sistema de monitoreo (PagerDuty, Slack, etc.)
            // alertService.sendCriticalAlert("Slow method detected", methodSignature, executionTime);
        }
        // WARNING: Métodos lentos
        else if (executionTime > WARNING_THRESHOLD_MS) {
            log.warn("🟡 [PERFORMANCE WARNING] {} tomó {} ms ({} segundos) - Status: {}",
                    methodSignature,
                    executionTime,
                    String.format("%.2f", executionTime / 1000.0),
                    status);

            if (exception != null) {
                log.warn("   └─ Error: {}: {}", exception.getClass().getSimpleName(), exception.getMessage());
            }
        }
        // INFO: Métodos normales (solo si es nivel DEBUG)
        else if (log.isDebugEnabled()) {
            log.debug("⚡ [PERFORMANCE] {} completado en {} ms - Status: {}",
                    methodSignature,
                    executionTime,
                    status);
        }
    }

    // =========================================================================
    // MÉTRICAS ADICIONALES (Ejemplos comentados)
    // =========================================================================

    /**
     * Ejemplo: Contador de excepciones por tipo.
     *
     * Útil para detectar errores frecuentes.
     */
    /*
    private void recordException(String className, String methodName, Throwable exception) {
        meterRegistry.counter("method.exceptions",
                "class", className,
                "method", methodName,
                "exception", exception.getClass().getSimpleName())
            .increment();
    }
    */

    /**
     * Ejemplo: Gauge para métodos activos concurrentes.
     *
     * Útil para detectar posibles deadlocks o carga alta.
     */
    /*
    private final AtomicInteger activeExecutions = new AtomicInteger(0);

    @PostConstruct
    public void registerGauges() {
        meterRegistry.gauge("method.active.executions", activeExecutions);
    }
    */

    /**
     * Ejemplo: Histograma de distribución de tiempos.
     *
     * Útil para percentiles (p50, p95, p99).
     */
    /*
    @Around("servicePublicMethods()")
    public Object monitorWithHistogram(ProceedingJoinPoint joinPoint) throws Throwable {
        return Timer.builder("method.execution.histogram")
            .publishPercentiles(0.5, 0.95, 0.99)  // p50, p95, p99
            .publishPercentileHistogram()
            .register(meterRegistry)
            .record(() -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
    }
    */
}
