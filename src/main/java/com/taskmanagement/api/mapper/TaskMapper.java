package com.taskmanagement.api.mapper;

import com.taskmanagement.api.dto.TaskPatchDto;
import com.taskmanagement.api.dto.TaskRequestDto;
import com.taskmanagement.api.dto.TaskResponseDto;
import com.taskmanagement.api.model.Task;

/**
 * Mapper para conversiones entre entidades Task y DTOs.
 *
 * ¿POR QUÉ UN MAPPER?
 * Los Mappers son clases responsables de convertir entre diferentes representaciones
 * de datos (Entidades ↔ DTOs). Centralizan la lógica de mapeo para:
 * - Mantener el código DRY (Don't Repeat Yourself)
 * - Facilitar cambios en las conversiones
 * - Mejorar la testabilidad
 * - Separar responsabilidades (SRP - Single Responsibility Principle)
 *
 * OPCIONES DE IMPLEMENTACIÓN:
 *
 * 1. MAPPER MANUAL (esta implementación):
 *    Ventajas:
 *    - ✅ Control total sobre las conversiones
 *    - ✅ Sin dependencias externas
 *    - ✅ Fácil de entender y debuggear
 *    - ✅ Ligero y rápido
 *    - ✅ Ideal para proyectos pequeños/medianos
 *
 *    Desventajas:
 *    - ❌ Código manual para cada conversión
 *    - ❌ Debe actualizarse si cambian los DTOs/Entidades
 *
 * 2. MAPSTRUCT (alternativa recomendada para proyectos grandes):
 *    Ventajas:
 *    - ✅ Generación automática de código en tiempo de compilación
 *    - ✅ Muy eficiente (sin reflexión en runtime)
 *    - ✅ Reduce código boilerplate
 *    - ✅ Detección de errores en compilación
 *
 *    Desventajas:
 *    - ❌ Dependencia externa
 *    - ❌ Curva de aprendizaje
 *    - ❌ Puede ser overkill para proyectos simples
 *
 *    Ejemplo con MapStruct:
 *    ```java
 *    @Mapper(componentModel = "spring")
 *    public interface TaskMapper {
 *        TaskResponseDto toResponseDto(Task task);
 *        Task toEntity(TaskRequestDto dto);
 *    }
 *    ```
 *
 * 3. MODELMAPPER (otra alternativa):
 *    Ventajas:
 *    - ✅ Mapeo automático por convención de nombres
 *    - ✅ Flexible
 *
 *    Desventajas:
 *    - ❌ Usa reflexión (más lento)
 *    - ❌ Errores en runtime en lugar de compilación
 *    - ❌ Difícil de debuggear
 *
 * DECISIÓN: Mapper manual
 * Para este proyecto usamos mapper manual porque:
 * - El proyecto es pequeño/mediano
 * - Tenemos pocos DTOs
 * - Queremos control total
 * - No queremos dependencias adicionales
 * - Es fácil migrar a MapStruct si crece el proyecto
 *
 * PATRÓN UTILIZADO:
 * - Clase utilitaria con métodos estáticos
 * - Constructor privado para prevenir instanciación
 * - Métodos nombrados consistentemente (toEntity, toResponseDto, etc.)
 * - Sin estado (stateless) - thread-safe
 *
 * USO:
 * ```java
 * // En el servicio:
 * Task task = TaskMapper.toEntity(taskRequestDto);
 * TaskResponseDto response = TaskMapper.toResponseDto(savedTask);
 * ```
 */
public final class TaskMapper {

    /**
     * Constructor privado para prevenir instanciación.
     *
     * Esta es una clase utilitaria que solo tiene métodos estáticos.
     * No tiene sentido crear instancias de ella.
     */
    private TaskMapper() {
        throw new AssertionError("TaskMapper is a utility class and should not be instantiated");
    }

    /**
     * Convierte un TaskRequestDto a una entidad Task.
     *
     * USADO EN: Creación de tareas (POST)
     *
     * NOTA: No mapea el ID porque es generado por la base de datos.
     * Tampoco mapea createdAt/updatedAt porque son gestionados por auditoría.
     *
     * @param dto DTO con los datos de entrada
     * @return entidad Task nueva (sin ID, createdAt, updatedAt)
     * @throws IllegalArgumentException si dto es null
     */
    public static Task toEntity(TaskRequestDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("TaskRequestDto cannot be null");
        }

        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setDueDate(dto.getDueDate());

        return task;
    }

    /**
     * Convierte una entidad Task a TaskResponseDto.
     *
     * USADO EN: Consultas y respuestas (GET, POST, PUT, PATCH)
     *
     * Incluye todos los campos incluyendo ID y campos de auditoría.
     *
     * @param task entidad Task
     * @return DTO de respuesta con todos los campos
     * @throws IllegalArgumentException si task es null
     */
    public static TaskResponseDto toResponseDto(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }

        TaskResponseDto dto = new TaskResponseDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        return dto;
    }

    /**
     * Actualiza una entidad Task existente con datos de TaskRequestDto.
     *
     * USADO EN: Actualización completa (PUT)
     *
     * Actualiza TODOS los campos editables.
     * No modifica: ID, createdAt (updatedAt se actualiza automáticamente por auditoría)
     *
     * IMPORTANTE: Modifica la entidad recibida (mutable operation)
     *
     * @param task entidad Task existente a actualizar
     * @param dto DTO con los nuevos datos
     * @return la misma entidad task actualizada (para fluent API)
     * @throws IllegalArgumentException si task o dto son null
     */
    public static Task updateEntityFromDto(Task task, TaskRequestDto dto) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("TaskRequestDto cannot be null");
        }

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus());
        task.setDueDate(dto.getDueDate());

        return task;
    }

    /**
     * Actualiza parcialmente una entidad Task con datos de TaskPatchDto.
     *
     * USADO EN: Actualización parcial (PATCH)
     *
     * Solo actualiza los campos que NO son null en el DTO.
     * Esto permite actualizaciones parciales sin sobrescribir campos.
     *
     * DIFERENCIA CON updateEntityFromDto:
     * - updateEntityFromDto: Actualiza TODOS los campos (PUT)
     * - patchEntityFromDto: Solo actualiza campos NO null (PATCH)
     *
     * EJEMPLO:
     * Si dto solo tiene status="COMPLETED", solo se actualiza el status.
     * Los demás campos permanecen sin cambios.
     *
     * IMPORTANTE: Modifica la entidad recibida (mutable operation)
     *
     * @param task entidad Task existente a actualizar
     * @param dto DTO con los campos a actualizar (solo los no-null)
     * @return la misma entidad task actualizada (para fluent API)
     * @throws IllegalArgumentException si task o dto son null
     */
    public static Task patchEntityFromDto(Task task, TaskPatchDto dto) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("TaskPatchDto cannot be null");
        }

        // Solo actualizar campos que no son null
        if (dto.getTitle() != null) {
            task.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            task.setDescription(dto.getDescription());
        }

        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }

        if (dto.getDueDate() != null) {
            task.setDueDate(dto.getDueDate());
        }

        return task;
    }

    // =========================================================================
    // MÉTODOS ADICIONALES ÚTILES
    // =========================================================================

    /**
     * Copia todos los datos de una Task a otra.
     *
     * USADO EN: Clonación de tareas, tests, etc.
     *
     * Útil cuando necesitas crear una copia de una tarea.
     * NO copia el ID ni los campos de auditoría.
     *
     * @param source tarea origen
     * @param target tarea destino
     * @return la tarea target actualizada
     */
    public static Task copyTaskData(Task source, Task target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target tasks cannot be null");
        }

        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setStatus(source.getStatus());
        target.setDueDate(source.getDueDate());

        return target;
    }

    /**
     * Verifica si un TaskPatchDto tiene algún campo para actualizar.
     *
     * USADO EN: Validación antes de hacer PATCH
     *
     * Útil para evitar actualizaciones vacías.
     *
     * @param dto DTO a verificar
     * @return true si al menos un campo no es null
     */
    public static boolean hasAnyFieldToUpdate(TaskPatchDto dto) {
        if (dto == null) {
            return false;
        }

        return dto.getTitle() != null
                || dto.getDescription() != null
                || dto.getStatus() != null
                || dto.getDueDate() != null;
    }
}
