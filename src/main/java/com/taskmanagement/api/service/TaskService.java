package com.taskmanagement.api.service;

import com.taskmanagement.api.dto.TaskPatchDto;
import com.taskmanagement.api.dto.TaskRequestDto;
import com.taskmanagement.api.dto.TaskResponseDto;
import com.taskmanagement.api.model.TaskStatus;

import java.util.List;

/**
 * Interfaz que define el contrato del servicio de tareas.
 *
 * Capa SERVICE: Contiene la lógica de negocio de la aplicación.
 *
 * Beneficios de usar interfaces:
 * - Desacoplamiento: Permite cambiar la implementación sin afectar otras capas
 * - Testabilidad: Facilita la creación de mocks para pruebas unitarias
 * - Contratos claros: Define explícitamente qué operaciones están disponibles
 * - Múltiples implementaciones: Permite tener diferentes estrategias de negocio
 */
public interface TaskService {

    /**
     * Crea una nueva tarea.
     *
     * @param taskRequestDto datos de la tarea a crear
     * @return la tarea creada con su ID asignado
     */
    TaskResponseDto createTask(TaskRequestDto taskRequestDto);

    /**
     * Obtiene todas las tareas del sistema.
     *
     * @return lista de todas las tareas
     */
    List<TaskResponseDto> getAllTasks();

    /**
     * Busca una tarea por su ID.
     *
     * @param id identificador de la tarea
     * @return la tarea encontrada
     */
    TaskResponseDto getTaskById(Long id);

    /**
     * Busca tareas por su estado.
     *
     * @param status estado de las tareas a buscar
     * @return lista de tareas con el estado especificado
     */
    List<TaskResponseDto> getTasksByStatus(TaskStatus status);

    /**
     * Busca tareas cuyo título contenga el texto especificado.
     *
     * @param title texto a buscar
     * @return lista de tareas que coinciden con la búsqueda
     */
    List<TaskResponseDto> searchTasksByTitle(String title);

    /**
     * Actualiza una tarea existente (actualización completa).
     *
     * @param id identificador de la tarea a actualizar
     * @param taskRequestDto nuevos datos de la tarea
     * @return la tarea actualizada
     */
    TaskResponseDto updateTask(Long id, TaskRequestDto taskRequestDto);

    /**
     * Actualiza parcialmente una tarea existente (solo los campos enviados).
     *
     * @param id identificador de la tarea a actualizar
     * @param taskPatchDto campos a actualizar (solo los no null)
     * @return la tarea actualizada
     */
    TaskResponseDto patchTask(Long id, TaskPatchDto taskPatchDto);

    /**
     * Elimina una tarea por su ID.
     *
     * @param id identificador de la tarea a eliminar
     */
    void deleteTask(Long id);

    /**
     * Obtiene estadísticas sobre las tareas.
     *
     * @return información estadística (por ejemplo, conteo por estado)
     */
    TaskStatisticsDto getStatistics();
}
