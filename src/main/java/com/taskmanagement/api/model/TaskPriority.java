package com.taskmanagement.api.model;

/**
 * Enumeración para las prioridades de las tareas.
 *
 * PROPÓSITO:
 * Define los niveles de prioridad que puede tener una tarea, permitiendo
 * a los usuarios organizar y filtrar tareas por importancia/urgencia.
 *
 * VALORES:
 * - LOW: Prioridad baja (tareas opcionales, no urgentes)
 * - MEDIUM: Prioridad media (tareas importantes pero no urgentes)
 * - HIGH: Prioridad alta (tareas urgentes e importantes)
 * - CRITICAL: Prioridad crítica (tareas urgentes que bloquean otras)
 *
 * MATRIZ DE EISENHOWER:
 * Esta enumeración se puede usar para implementar la matriz de priorización:
 * - CRITICAL: Urgente e Importante
 * - HIGH: Importante pero no urgente
 * - MEDIUM: Urgente pero no importante
 * - LOW: Ni urgente ni importante
 *
 * USO EN FILTROS:
 * Los usuarios pueden filtrar tareas por prioridad:
 * - Ver solo tareas críticas
 * - Ordenar por prioridad descendente
 * - Combinar con otros filtros (estado, fecha, etc.)
 *
 * ALMACENAMIENTO EN BD:
 * Se almacena como VARCHAR usando @Enumerated(EnumType.STRING)
 * Ventajas:
 * - Legible en la base de datos
 * - Cambios en el orden no afectan datos existentes
 * - Fácil de debuggear
 *
 * EJEMPLO DE USO:
 * ```java
 * Task task = new Task();
 * task.setPriority(TaskPriority.HIGH);
 *
 * // En queries
 * List<Task> criticalTasks = taskRepository.findByPriority(TaskPriority.CRITICAL);
 * ```
 *
 * FUTURAS MEJORAS:
 * - Agregar campo numericValue para ordenamiento custom
 * - Agregar colores asociados para UI
 * - Agregar SLA (Service Level Agreement) por prioridad
 */
public enum TaskPriority {
    /**
     * Prioridad baja - Tareas opcionales, "nice to have"
     *
     * Características:
     * - No tienen deadline estricto
     * - Pueden posponerse si hay tareas más prioritarias
     * - Ejemplo: Mejorar documentación, refactoring opcional
     */
    LOW,

    /**
     * Prioridad media - Tareas importantes pero no urgentes
     *
     * Características:
     * - Tienen deadline pero no inmediato
     * - Deben completarse pero no bloquean otras tareas
     * - Ejemplo: Desarrollo de features planificadas
     */
    MEDIUM,

    /**
     * Prioridad alta - Tareas urgentes e importantes
     *
     * Características:
     * - Deadline cercano
     * - Impacto significativo en el proyecto
     * - Deben atenderse pronto
     * - Ejemplo: Bugs en producción, features con deadline próximo
     */
    HIGH,

    /**
     * Prioridad crítica - Tareas que bloquean el trabajo
     *
     * Características:
     * - Máxima urgencia
     * - Bloquean otras tareas o afectan producción
     * - Deben resolverse inmediatamente
     * - Ejemplo: Incidentes de producción, bugs críticos, blockers
     */
    CRITICAL;

    /**
     * Obtiene el nivel numérico de prioridad para ordenamiento.
     *
     * Útil para ordenar tareas por prioridad de mayor a menor:
     * CRITICAL (3) > HIGH (2) > MEDIUM (1) > LOW (0)
     *
     * @return nivel numérico (0-3)
     */
    public int getNumericValue() {
        return switch (this) {
            case LOW -> 0;
            case MEDIUM -> 1;
            case HIGH -> 2;
            case CRITICAL -> 3;
        };
    }

    /**
     * Obtiene la descripción legible de la prioridad.
     *
     * @return descripción en español
     */
    public String getDisplayName() {
        return switch (this) {
            case LOW -> "Baja";
            case MEDIUM -> "Media";
            case HIGH -> "Alta";
            case CRITICAL -> "Crítica";
        };
    }

    /**
     * Convierte desde un valor numérico a TaskPriority.
     *
     * @param value valor numérico (0-3)
     * @return TaskPriority correspondiente
     * @throws IllegalArgumentException si el valor no es válido
     */
    public static TaskPriority fromNumericValue(int value) {
        return switch (value) {
            case 0 -> LOW;
            case 1 -> MEDIUM;
            case 2 -> HIGH;
            case 3 -> CRITICAL;
            default -> throw new IllegalArgumentException("Invalid priority value: " + value);
        };
    }
}
