package com.taskmanagement.api.service.impl;

import com.taskmanagement.api.dto.TaskPatchDto;
import com.taskmanagement.api.dto.TaskRequestDto;
import com.taskmanagement.api.dto.TaskResponseDto;
import com.taskmanagement.api.exception.ResourceNotFoundException;
import com.taskmanagement.api.mapper.TaskMapper;
import com.taskmanagement.api.model.Task;
import com.taskmanagement.api.model.TaskStatus;
import com.taskmanagement.api.repository.TaskRepository;
import com.taskmanagement.api.service.TaskService;
import com.taskmanagement.api.service.TaskStatisticsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de tareas.
 *
 * Capa SERVICE (Implementation): Implementa la lógica de negocio definida
 * en la interfaz TaskService.
 *
 * Anotaciones:
 * @Service: Marca la clase como un componente de servicio de Spring
 * @RequiredArgsConstructor: Genera un constructor con todos los campos final (para inyección de dependencias)
 * @Slf4j: Genera automáticamente un logger (log) usando SLF4J
 * @Transactional: Gestiona transacciones de BD a nivel de clase
 *
 * Patrón de inyección de dependencias:
 * - Constructor injection (recomendado): Campos final + @RequiredArgsConstructor
 * - Facilita testing y asegura que las dependencias no sean null
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskServiceImpl implements TaskService {

    // Inyección de dependencias mediante constructor (inmutable)
    private final TaskRepository taskRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskResponseDto createTask(TaskRequestDto taskRequestDto) {
        log.info("Creando nueva tarea con título: {}", taskRequestDto.getTitle());

        // Usar TaskMapper para convertir DTO a entidad
        Task task = TaskMapper.toEntity(taskRequestDto);

        // Guardar en la base de datos
        Task savedTask = taskRepository.save(task);

        log.info("Tarea creada exitosamente con ID: {}", savedTask.getId());

        // Usar TaskMapper para convertir entidad a DTO de respuesta
        return TaskMapper.toResponseDto(savedTask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)  // Optimiza lecturas (no inicia transacción de escritura)
    public List<TaskResponseDto> getAllTasks() {
        log.info("Obteniendo todas las tareas");

        return taskRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(TaskMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long id) {
        log.info("Buscando tarea con ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

        return TaskMapper.toResponseDto(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByStatus(TaskStatus status) {
        log.info("Buscando tareas con estado: {}", status);

        return taskRepository.findByStatus(status).stream()
                .map(TaskMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TaskResponseDto> searchTasksByTitle(String title) {
        log.info("Buscando tareas con título que contenga: {}", title);

        return taskRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(TaskMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskResponseDto updateTask(Long id, TaskRequestDto taskRequestDto) {
        log.info("Actualizando tarea con ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

        // Usar TaskMapper para actualizar la entidad
        TaskMapper.updateEntityFromDto(task, taskRequestDto);

        Task updatedTask = taskRepository.save(task);

        log.info("Tarea actualizada exitosamente con ID: {}", updatedTask.getId());

        return TaskMapper.toResponseDto(updatedTask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TaskResponseDto patchTask(Long id, TaskPatchDto taskPatchDto) {
        log.info("Actualizando parcialmente tarea con ID: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada con ID: " + id));

        // Usar TaskMapper para actualización parcial
        TaskMapper.patchEntityFromDto(task, taskPatchDto);

        Task updatedTask = taskRepository.save(task);

        log.info("Tarea actualizada parcialmente con éxito con ID: {}", updatedTask.getId());

        return TaskMapper.toResponseDto(updatedTask);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTask(Long id) {
        log.info("Eliminando tarea con ID: {}", id);

        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarea no encontrada con ID: " + id);
        }

        taskRepository.deleteById(id);

        log.info("Tarea eliminada exitosamente con ID: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TaskStatisticsDto getStatistics() {
        log.info("Obteniendo estadísticas de tareas");

        TaskStatisticsDto stats = new TaskStatisticsDto();
        stats.setTotalTasks(taskRepository.count());
        stats.setPendingTasks(taskRepository.countByStatus(TaskStatus.PENDING));
        stats.setInProgressTasks(taskRepository.countByStatus(TaskStatus.IN_PROGRESS));
        stats.setCompletedTasks(taskRepository.countByStatus(TaskStatus.COMPLETED));
        stats.setCancelledTasks(taskRepository.countByStatus(TaskStatus.CANCELLED));

        return stats;
    }

    // =========================================================================
    // NOTA SOBRE MAPEO:
    // Los métodos privados mapToEntity y mapToResponseDto fueron eliminados.
    // Ahora usamos TaskMapper (clase utilitaria centralizada) para todas las
    // conversiones entre entidades y DTOs.
    //
    // VENTAJAS:
    // - Código DRY: Un solo lugar para mapeo
    // - Fácil de testear: TaskMapper es independiente
    // - Reutilizable: Otros servicios pueden usar el mismo mapper
    // - Mantenible: Cambios de mapeo en un solo lugar
    // =========================================================================
}
