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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    // MÉTODOS CON PAGINACIÓN
    // =========================================================================

    /**
     * {@inheritDoc}
     *
     * Implementación de paginación usando Spring Data JPA.
     *
     * CÓMO FUNCIONA:
     * 1. El Pageable contiene: número de página, tamaño, ordenamiento
     * 2. Spring Data JPA genera automáticamente la query SQL con LIMIT y OFFSET
     * 3. Page<Task> contiene los datos + metadata de paginación
     * 4. Convertimos Task -> TaskResponseDto usando map()
     *
     * VENTAJAS:
     * - Performance: Solo carga datos de la página actual
     * - Escalabilidad: Funciona bien con millones de registros
     * - Metadata: Incluye info de paginación (totalPages, totalElements, etc)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getAllTasks(Pageable pageable) {
        log.info("Obteniendo tareas paginadas - Página: {}, Tamaño: {}, Orden: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        // Page.map() transforma cada elemento de la página
        // Sin necesidad de cargar todo en memoria primero
        return taskRepository.findAll(pageable)
                .map(TaskMapper::toResponseDto);
    }

    /**
     * {@inheritDoc}
     *
     * Busca tareas por estado con paginación.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> getTasksByStatus(TaskStatus status, Pageable pageable) {
        log.info("Buscando tareas con estado: {} - Página: {}, Tamaño: {}",
                status,
                pageable.getPageNumber(),
                pageable.getPageSize());

        return taskRepository.findByStatus(status, pageable)
                .map(TaskMapper::toResponseDto);
    }

    /**
     * {@inheritDoc}
     *
     * Busca tareas por título con paginación.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponseDto> searchTasksByTitle(String title, Pageable pageable) {
        log.info("Buscando tareas por título: '{}' - Página: {}, Tamaño: {}",
                title,
                pageable.getPageNumber(),
                pageable.getPageSize());

        return taskRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(TaskMapper::toResponseDto);
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
