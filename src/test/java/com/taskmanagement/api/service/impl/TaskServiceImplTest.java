package com.taskmanagement.api.service.impl;

import com.taskmanagement.api.dto.TaskPatchDto;
import com.taskmanagement.api.dto.TaskRequestDto;
import com.taskmanagement.api.dto.TaskResponseDto;
import com.taskmanagement.api.exception.ResourceNotFoundException;
import com.taskmanagement.api.model.Task;
import com.taskmanagement.api.model.TaskStatus;
import com.taskmanagement.api.repository.TaskRepository;
import com.taskmanagement.api.service.TaskStatisticsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para TaskServiceImpl.
 *
 * ============================================================================
 * ¿QUÉ SON LOS TESTS UNITARIOS?
 * ============================================================================
 * Los tests unitarios verifican el comportamiento de UNA UNIDAD de código
 * (típicamente un método o clase) de forma AISLADA, sin depender de:
 * - Base de datos real
 * - Servicios externos
 * - Sistema de archivos
 * - Red
 *
 * ============================================================================
 * ¿POR QUÉ SON IMPORTANTES LOS TESTS UNITARIOS?
 * ============================================================================
 * 1. CONFIANZA: Saber que el código funciona correctamente
 * 2. REGRESIÓN: Detectar bugs al hacer cambios (evita romper código existente)
 * 3. DOCUMENTACIÓN: Los tests muestran cómo usar el código
 * 4. DISEÑO: Código testeable = código bien diseñado
 * 5. REFACTORING: Cambiar código con seguridad
 * 6. CALIDAD: Menos bugs en producción = usuarios felices
 * 7. VELOCIDAD: Tests rápidos (milisegundos) vs tests de integración (segundos/minutos)
 *
 * ============================================================================
 * ¿POR QUÉ USAMOS MOCKS?
 * ============================================================================
 * Un MOCK es un objeto simulado que imita el comportamiento de un objeto real.
 *
 * En estos tests, mockeamos TaskRepository porque:
 * 1. NO queremos acceder a la BD real (lento, requiere configuración)
 * 2. Queremos CONTROLAR el comportamiento (simular casos edge, errores, etc)
 * 3. Queremos tests RÁPIDOS (milisegundos vs segundos)
 * 4. Queremos tests AISLADOS (solo testear la lógica del servicio)
 * 5. Queremos tests PREDECIBLES (mismo resultado siempre)
 *
 * MOCKITO nos permite:
 * - when(mock.method()).thenReturn(value) - Definir comportamiento
 * - verify(mock).method() - Verificar que se llamó un método
 * - ArgumentCaptor - Capturar argumentos pasados a mocks
 *
 * ============================================================================
 * PATRÓN AAA (ARRANGE-ACT-ASSERT)
 * ============================================================================
 * Todos los tests siguen el patrón AAA:
 *
 * ARRANGE (Given):  Preparar el escenario de prueba
 *                   - Crear objetos de prueba
 *                   - Configurar mocks
 *                   - Establecer precondiciones
 *
 * ACT (When):       Ejecutar la acción a probar
 *                   - Llamar al método bajo prueba
 *                   - Capturar el resultado
 *
 * ASSERT (Then):    Verificar el resultado
 *                   - Comprobar el valor retornado
 *                   - Verificar llamadas a mocks
 *                   - Verificar el estado del sistema
 *
 * ============================================================================
 * ANOTACIONES UTILIZADAS:
 * ============================================================================
 * @ExtendWith(MockitoExtension.class):
 *   - Integra Mockito con JUnit 5
 *   - Inicializa mocks automáticamente
 *   - Valida uso de mocks
 *
 * @Mock:
 *   - Crea un mock (objeto simulado)
 *   - No tiene implementación real
 *   - Su comportamiento se define con when().thenReturn()
 *
 * @InjectMocks:
 *   - Crea una instancia de la clase bajo prueba
 *   - Inyecta automáticamente los @Mock en sus dependencias
 *   - Equivalente a: new TaskServiceImpl(taskRepository)
 *
 * @BeforeEach:
 *   - Se ejecuta ANTES de cada test
 *   - Usado para inicializar datos comunes
 *
 * @Test:
 *   - Marca un método como test
 *   - JUnit ejecuta estos métodos
 *
 * @DisplayName:
 *   - Nombre legible del test (aparece en reportes)
 *
 * @Nested:
 *   - Agrupa tests relacionados
 *   - Mejora organización
 *
 * ============================================================================
 * CONVENCIÓN DE NOMBRES:
 * ============================================================================
 * should_ExpectedBehavior_When_Condition
 *
 * Ejemplos:
 * - should_ReturnTask_When_TaskExists
 * - should_ThrowException_When_TaskNotFound
 * - should_CreateTask_When_ValidDataProvided
 *
 * ============================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService - Tests Unitarios")
class TaskServiceImplTest {

    // =========================================================================
    // MOCKS Y DEPENDENCIAS
    // =========================================================================

    /**
     * Mock del repositorio de tareas.
     *
     * Este mock simula el comportamiento de TaskRepository sin acceder
     * a la base de datos real.
     */
    @Mock
    private TaskRepository taskRepository;

    /**
     * Instancia del servicio bajo prueba.
     *
     * @InjectMocks automáticamente inyecta el taskRepository mock
     * en esta instancia.
     */
    @InjectMocks
    private TaskServiceImpl taskService;

    // =========================================================================
    // DATOS DE PRUEBA COMUNES
    // =========================================================================

    private Task task;
    private TaskRequestDto taskRequestDto;
    private TaskPatchDto taskPatchDto;

    /**
     * Inicializa datos de prueba comunes ANTES de cada test.
     *
     * PATRÓN: Test Data Builder
     * Centraliza la creación de objetos de prueba para evitar duplicación.
     */
    @BeforeEach
    void setUp() {
        // ARRANGE: Crear datos de prueba comunes
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(LocalDateTime.of(2025, 12, 31, 23, 59));
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        taskRequestDto = new TaskRequestDto();
        taskRequestDto.setTitle("Test Task");
        taskRequestDto.setDescription("Test Description");
        taskRequestDto.setStatus(TaskStatus.PENDING);
        taskRequestDto.setDueDate(LocalDateTime.of(2025, 12, 31, 23, 59));

        taskPatchDto = new TaskPatchDto();
    }

    // =========================================================================
    // TESTS: createTask()
    // =========================================================================

    @Nested
    @DisplayName("createTask() - Crear Tarea")
    class CreateTaskTests {

        @Test
        @DisplayName("should_CreateAndReturnTask_When_ValidDataProvided")
        void should_CreateAndReturnTask_When_ValidDataProvided() {
            // ARRANGE (Given): Preparar el escenario
            // Configurar el mock para que retorne la tarea guardada
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // ACT (When): Ejecutar la acción
            TaskResponseDto result = taskService.createTask(taskRequestDto);

            // ASSERT (Then): Verificar el resultado
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Test Task");
            assertThat(result.getDescription()).isEqualTo("Test Description");
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);

            // Verificar que se llamó al método save del repositorio
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("should_SetDefaultStatus_When_StatusNotProvided")
        void should_SetDefaultStatus_When_StatusNotProvided() {
            // ARRANGE
            task.setStatus(TaskStatus.PENDING); // Default status
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // ACT
            TaskResponseDto result = taskService.createTask(taskRequestDto);

            // ASSERT
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
        }
    }

    // =========================================================================
    // TESTS: getAllTasks()
    // =========================================================================

    @Nested
    @DisplayName("getAllTasks() - Obtener Todas las Tareas")
    class GetAllTasksTests {

        @Test
        @DisplayName("should_ReturnAllTasks_When_TasksExist")
        void should_ReturnAllTasks_When_TasksExist() {
            // ARRANGE
            Task task2 = new Task();
            task2.setId(2L);
            task2.setTitle("Task 2");
            task2.setDescription("Description 2");
            task2.setStatus(TaskStatus.IN_PROGRESS);
            task2.setCreatedAt(LocalDateTime.now());
            task2.setUpdatedAt(LocalDateTime.now());

            List<Task> tasks = Arrays.asList(task, task2);
            when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(tasks);

            // ACT
            List<TaskResponseDto> result = taskService.getAllTasks();

            // ASSERT
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);

            verify(taskRepository, times(1)).findAllByOrderByCreatedAtDesc();
        }

        @Test
        @DisplayName("should_ReturnEmptyList_When_NoTasksExist")
        void should_ReturnEmptyList_When_NoTasksExist() {
            // ARRANGE
            when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());

            // ACT
            List<TaskResponseDto> result = taskService.getAllTasks();

            // ASSERT
            assertThat(result).isEmpty();
            verify(taskRepository, times(1)).findAllByOrderByCreatedAtDesc();
        }
    }

    // =========================================================================
    // TESTS: getTaskById()
    // =========================================================================

    @Nested
    @DisplayName("getTaskById() - Obtener Tarea por ID")
    class GetTaskByIdTests {

        @Test
        @DisplayName("should_ReturnTask_When_TaskExists")
        void should_ReturnTask_When_TaskExists() {
            // ARRANGE
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            // ACT
            TaskResponseDto result = taskService.getTaskById(1L);

            // ASSERT
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Test Task");

            verify(taskRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("should_ThrowResourceNotFoundException_When_TaskNotFound")
        void should_ThrowResourceNotFoundException_When_TaskNotFound() {
            // ARRANGE
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // ACT & ASSERT
            // Verificar que se lanza la excepción correcta
            assertThatThrownBy(() -> taskService.getTaskById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Tarea no encontrada con ID: 999");

            verify(taskRepository, times(1)).findById(999L);
        }
    }

    // =========================================================================
    // TESTS: getTasksByStatus()
    // =========================================================================

    @Nested
    @DisplayName("getTasksByStatus() - Buscar Tareas por Estado")
    class GetTasksByStatusTests {

        @Test
        @DisplayName("should_ReturnTasksWithStatus_When_TasksExist")
        void should_ReturnTasksWithStatus_When_TasksExist() {
            // ARRANGE
            Task task2 = new Task();
            task2.setId(2L);
            task2.setTitle("Task 2");
            task2.setStatus(TaskStatus.PENDING);
            task2.setCreatedAt(LocalDateTime.now());
            task2.setUpdatedAt(LocalDateTime.now());

            List<Task> pendingTasks = Arrays.asList(task, task2);
            when(taskRepository.findByStatus(TaskStatus.PENDING)).thenReturn(pendingTasks);

            // ACT
            List<TaskResponseDto> result = taskService.getTasksByStatus(TaskStatus.PENDING);

            // ASSERT
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(t -> t.getStatus() == TaskStatus.PENDING);

            verify(taskRepository, times(1)).findByStatus(TaskStatus.PENDING);
        }

        @Test
        @DisplayName("should_ReturnEmptyList_When_NoTasksWithStatus")
        void should_ReturnEmptyList_When_NoTasksWithStatus() {
            // ARRANGE
            when(taskRepository.findByStatus(TaskStatus.COMPLETED)).thenReturn(Collections.emptyList());

            // ACT
            List<TaskResponseDto> result = taskService.getTasksByStatus(TaskStatus.COMPLETED);

            // ASSERT
            assertThat(result).isEmpty();
            verify(taskRepository, times(1)).findByStatus(TaskStatus.COMPLETED);
        }
    }

    // =========================================================================
    // TESTS: searchTasksByTitle()
    // =========================================================================

    @Nested
    @DisplayName("searchTasksByTitle() - Buscar Tareas por Título")
    class SearchTasksByTitleTests {

        @Test
        @DisplayName("should_ReturnMatchingTasks_When_TitleMatches")
        void should_ReturnMatchingTasks_When_TitleMatches() {
            // ARRANGE
            List<Task> matchingTasks = Collections.singletonList(task);
            when(taskRepository.findByTitleContainingIgnoreCase("Test")).thenReturn(matchingTasks);

            // ACT
            List<TaskResponseDto> result = taskService.searchTasksByTitle("Test");

            // ASSERT
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).contains("Test");

            verify(taskRepository, times(1)).findByTitleContainingIgnoreCase("Test");
        }

        @Test
        @DisplayName("should_ReturnEmptyList_When_NoTitleMatches")
        void should_ReturnEmptyList_When_NoTitleMatches() {
            // ARRANGE
            when(taskRepository.findByTitleContainingIgnoreCase("NonExistent"))
                    .thenReturn(Collections.emptyList());

            // ACT
            List<TaskResponseDto> result = taskService.searchTasksByTitle("NonExistent");

            // ASSERT
            assertThat(result).isEmpty();
            verify(taskRepository, times(1)).findByTitleContainingIgnoreCase("NonExistent");
        }

        @Test
        @DisplayName("should_SearchCaseInsensitive_When_SearchingByTitle")
        void should_SearchCaseInsensitive_When_SearchingByTitle() {
            // ARRANGE
            List<Task> matchingTasks = Collections.singletonList(task);
            when(taskRepository.findByTitleContainingIgnoreCase("test")).thenReturn(matchingTasks);

            // ACT
            List<TaskResponseDto> result = taskService.searchTasksByTitle("test");

            // ASSERT
            assertThat(result).hasSize(1);
            verify(taskRepository, times(1)).findByTitleContainingIgnoreCase("test");
        }
    }

    // =========================================================================
    // TESTS: updateTask()
    // =========================================================================

    @Nested
    @DisplayName("updateTask() - Actualizar Tarea Completa (PUT)")
    class UpdateTaskTests {

        @Test
        @DisplayName("should_UpdateAndReturnTask_When_TaskExists")
        void should_UpdateAndReturnTask_When_TaskExists() {
            // ARRANGE
            TaskRequestDto updateDto = new TaskRequestDto();
            updateDto.setTitle("Updated Title");
            updateDto.setDescription("Updated Description");
            updateDto.setStatus(TaskStatus.COMPLETED);
            updateDto.setDueDate(LocalDateTime.of(2026, 1, 1, 0, 0));

            Task updatedTask = new Task();
            updatedTask.setId(1L);
            updatedTask.setTitle("Updated Title");
            updatedTask.setDescription("Updated Description");
            updatedTask.setStatus(TaskStatus.COMPLETED);
            updatedTask.setDueDate(LocalDateTime.of(2026, 1, 1, 0, 0));
            updatedTask.setCreatedAt(task.getCreatedAt());
            updatedTask.setUpdatedAt(LocalDateTime.now());

            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

            // ACT
            TaskResponseDto result = taskService.updateTask(1L, updateDto);

            // ASSERT
            assertThat(result.getTitle()).isEqualTo("Updated Title");
            assertThat(result.getDescription()).isEqualTo("Updated Description");
            assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);

            verify(taskRepository, times(1)).findById(1L);
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("should_ThrowResourceNotFoundException_When_TaskNotFound")
        void should_ThrowResourceNotFoundException_When_TaskNotFound() {
            // ARRANGE
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> taskService.updateTask(999L, taskRequestDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Tarea no encontrada con ID: 999");

            verify(taskRepository, times(1)).findById(999L);
            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    // =========================================================================
    // TESTS: patchTask()
    // =========================================================================

    @Nested
    @DisplayName("patchTask() - Actualizar Tarea Parcialmente (PATCH)")
    class PatchTaskTests {

        @Test
        @DisplayName("should_UpdateOnlyProvidedFields_When_PatchingTask")
        void should_UpdateOnlyProvidedFields_When_PatchingTask() {
            // ARRANGE
            taskPatchDto.setStatus(TaskStatus.COMPLETED);
            // Solo actualizamos el status, los demás campos permanecen igual

            Task patchedTask = new Task();
            patchedTask.setId(1L);
            patchedTask.setTitle("Test Task"); // No cambia
            patchedTask.setDescription("Test Description"); // No cambia
            patchedTask.setStatus(TaskStatus.COMPLETED); // CAMBIA
            patchedTask.setDueDate(task.getDueDate()); // No cambia
            patchedTask.setCreatedAt(task.getCreatedAt());
            patchedTask.setUpdatedAt(LocalDateTime.now());

            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(patchedTask);

            // ACT
            TaskResponseDto result = taskService.patchTask(1L, taskPatchDto);

            // ASSERT
            assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);
            assertThat(result.getTitle()).isEqualTo("Test Task"); // Sin cambios
            assertThat(result.getDescription()).isEqualTo("Test Description"); // Sin cambios

            verify(taskRepository, times(1)).findById(1L);
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("should_UpdateMultipleFields_When_MultiplePatchFieldsProvided")
        void should_UpdateMultipleFields_When_MultiplePatchFieldsProvided() {
            // ARRANGE
            taskPatchDto.setTitle("Patched Title");
            taskPatchDto.setStatus(TaskStatus.IN_PROGRESS);

            Task patchedTask = new Task();
            patchedTask.setId(1L);
            patchedTask.setTitle("Patched Title");
            patchedTask.setDescription("Test Description");
            patchedTask.setStatus(TaskStatus.IN_PROGRESS);
            patchedTask.setDueDate(task.getDueDate());
            patchedTask.setCreatedAt(task.getCreatedAt());
            patchedTask.setUpdatedAt(LocalDateTime.now());

            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(patchedTask);

            // ACT
            TaskResponseDto result = taskService.patchTask(1L, taskPatchDto);

            // ASSERT
            assertThat(result.getTitle()).isEqualTo("Patched Title");
            assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(result.getDescription()).isEqualTo("Test Description"); // Sin cambios

            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("should_ThrowResourceNotFoundException_When_TaskNotFound")
        void should_ThrowResourceNotFoundException_When_TaskNotFound() {
            // ARRANGE
            taskPatchDto.setStatus(TaskStatus.COMPLETED);
            when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

            // ACT & ASSERT
            assertThatThrownBy(() -> taskService.patchTask(999L, taskPatchDto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Tarea no encontrada con ID: 999");

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    // =========================================================================
    // TESTS: deleteTask()
    // =========================================================================

    @Nested
    @DisplayName("deleteTask() - Eliminar Tarea")
    class DeleteTaskTests {

        @Test
        @DisplayName("should_DeleteTask_When_TaskExists")
        void should_DeleteTask_When_TaskExists() {
            // ARRANGE
            when(taskRepository.existsById(1L)).thenReturn(true);
            doNothing().when(taskRepository).deleteById(1L);

            // ACT
            taskService.deleteTask(1L);

            // ASSERT
            verify(taskRepository, times(1)).existsById(1L);
            verify(taskRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("should_ThrowResourceNotFoundException_When_TaskNotFound")
        void should_ThrowResourceNotFoundException_When_TaskNotFound() {
            // ARRANGE
            when(taskRepository.existsById(anyLong())).thenReturn(false);

            // ACT & ASSERT
            assertThatThrownBy(() -> taskService.deleteTask(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Tarea no encontrada con ID: 999");

            verify(taskRepository, times(1)).existsById(999L);
            verify(taskRepository, never()).deleteById(anyLong());
        }
    }

    // =========================================================================
    // TESTS: getStatistics()
    // =========================================================================

    @Nested
    @DisplayName("getStatistics() - Obtener Estadísticas")
    class GetStatisticsTests {

        @Test
        @DisplayName("should_ReturnCorrectStatistics_When_TasksExist")
        void should_ReturnCorrectStatistics_When_TasksExist() {
            // ARRANGE
            when(taskRepository.count()).thenReturn(100L);
            when(taskRepository.countByStatus(TaskStatus.PENDING)).thenReturn(30L);
            when(taskRepository.countByStatus(TaskStatus.IN_PROGRESS)).thenReturn(25L);
            when(taskRepository.countByStatus(TaskStatus.COMPLETED)).thenReturn(40L);
            when(taskRepository.countByStatus(TaskStatus.CANCELLED)).thenReturn(5L);

            // ACT
            TaskStatisticsDto result = taskService.getStatistics();

            // ASSERT
            assertThat(result.getTotalTasks()).isEqualTo(100L);
            assertThat(result.getPendingTasks()).isEqualTo(30L);
            assertThat(result.getInProgressTasks()).isEqualTo(25L);
            assertThat(result.getCompletedTasks()).isEqualTo(40L);
            assertThat(result.getCancelledTasks()).isEqualTo(5L);

            // Verificar que se llamó a todos los métodos de conteo
            verify(taskRepository, times(1)).count();
            verify(taskRepository, times(1)).countByStatus(TaskStatus.PENDING);
            verify(taskRepository, times(1)).countByStatus(TaskStatus.IN_PROGRESS);
            verify(taskRepository, times(1)).countByStatus(TaskStatus.COMPLETED);
            verify(taskRepository, times(1)).countByStatus(TaskStatus.CANCELLED);
        }

        @Test
        @DisplayName("should_ReturnZeroStatistics_When_NoTasksExist")
        void should_ReturnZeroStatistics_When_NoTasksExist() {
            // ARRANGE
            when(taskRepository.count()).thenReturn(0L);
            when(taskRepository.countByStatus(any(TaskStatus.class))).thenReturn(0L);

            // ACT
            TaskStatisticsDto result = taskService.getStatistics();

            // ASSERT
            assertThat(result.getTotalTasks()).isZero();
            assertThat(result.getPendingTasks()).isZero();
            assertThat(result.getInProgressTasks()).isZero();
            assertThat(result.getCompletedTasks()).isZero();
            assertThat(result.getCancelledTasks()).isZero();
        }
    }

    // =========================================================================
    // CASOS EDGE (EDGE CASES)
    // =========================================================================
    // Edge cases son situaciones límite o inusuales que pueden causar errores

    @Nested
    @DisplayName("Edge Cases - Casos Límite")
    class EdgeCasesTests {

        @Test
        @DisplayName("should_HandleNullDescription_When_CreatingTask")
        void should_HandleNullDescription_When_CreatingTask() {
            // ARRANGE
            taskRequestDto.setDescription(null); // Descripción null (válido)
            task.setDescription(null);

            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // ACT
            TaskResponseDto result = taskService.createTask(taskRequestDto);

            // ASSERT
            assertThat(result.getDescription()).isNull();
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("should_HandleNullDueDate_When_CreatingTask")
        void should_HandleNullDueDate_When_CreatingTask() {
            // ARRANGE
            taskRequestDto.setDueDate(null); // DueDate null (válido)
            task.setDueDate(null);

            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // ACT
            TaskResponseDto result = taskService.createTask(taskRequestDto);

            // ASSERT
            assertThat(result.getDueDate()).isNull();
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("should_HandleEmptyPatchDto_When_AllFieldsNull")
        void should_HandleEmptyPatchDto_When_AllFieldsNull() {
            // ARRANGE
            TaskPatchDto emptyPatchDto = new TaskPatchDto();
            // Todos los campos son null

            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            // ACT
            TaskResponseDto result = taskService.patchTask(1L, emptyPatchDto);

            // ASSERT
            // Ningún campo debe haber cambiado
            assertThat(result.getTitle()).isEqualTo("Test Task");
            assertThat(result.getDescription()).isEqualTo("Test Description");
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);

            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("should_HandleLargeNumberOfTasks_When_GettingAll")
        void should_HandleLargeNumberOfTasks_When_GettingAll() {
            // ARRANGE
            // Simular una gran cantidad de tareas (1000)
            List<Task> largeTasks = new java.util.ArrayList<>();
            for (int i = 1; i <= 1000; i++) {
                Task t = new Task();
                t.setId((long) i);
                t.setTitle("Task " + i);
                t.setStatus(TaskStatus.PENDING);
                t.setCreatedAt(LocalDateTime.now());
                t.setUpdatedAt(LocalDateTime.now());
                largeTasks.add(t);
            }

            when(taskRepository.findAllByOrderByCreatedAtDesc()).thenReturn(largeTasks);

            // ACT
            List<TaskResponseDto> result = taskService.getAllTasks();

            // ASSERT
            assertThat(result).hasSize(1000);
            verify(taskRepository, times(1)).findAllByOrderByCreatedAtDesc();
        }
    }
}
