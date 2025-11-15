package com.taskmanagement.api.integration;

import com.taskmanagement.api.model.Task;
import com.taskmanagement.api.model.TaskStatus;
import com.taskmanagement.api.repository.TaskRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para TaskRepository.
 *
 * ============================================================================
 * ¿QUÉ ESTAMOS TESTEANDO AQUÍ?
 * ============================================================================
 * Estos tests verifican que:
 * 1. Los queries de Spring Data JPA funcionan correctamente
 * 2. La persistencia en PostgreSQL REAL funciona
 * 3. Las transacciones funcionan correctamente
 * 4. Los constraints de BD se respetan
 * 5. Los índices y búsquedas funcionan como esperado
 *
 * DIFERENCIA CON TESTS UNITARIOS:
 * - Tests Unitarios: Mock del repository (no hay BD real)
 * - Tests Integración: PostgreSQL REAL en contenedor Docker
 *
 * ============================================================================
 * ¿POR QUÉ USAR POSTGRESQL REAL?
 * ============================================================================
 * 1. QUERIES ESPECÍFICOS DE POSTGRESQL:
 *    - findByTitleContainingIgnoreCase usa ILIKE de PostgreSQL
 *    - Diferente comportamiento que H2/HSQLDB
 *
 * 2. TIPOS DE DATOS:
 *    - ENUM (TaskStatus) se mapea diferente en cada BD
 *    - TIMESTAMP con zona horaria
 *    - TEXT vs VARCHAR
 *
 * 3. CONSTRAINTS:
 *    - NOT NULL, UNIQUE, CHECK constraints
 *    - Foreign keys y cascadas
 *    - Índices y performance
 *
 * 4. TRANSACCIONES:
 *    - Comportamiento de ACID
 *    - Niveles de aislamiento
 *    - Rollback y commit
 *
 * 5. FUNCIONES ESPECÍFICAS:
 *    - LOWER(), UPPER()
 *    - String matching (LIKE, ILIKE)
 *    - Date/Time functions
 *
 * ============================================================================
 * CONFIGURACIÓN:
 * ============================================================================
 * - Hereda de AbstractIntegrationTest (PostgreSQL en TestContainers)
 * - @Transactional: Cada test se ejecuta en una transacción
 *   - Rollback automático después de cada test
 *   - Base de datos limpia para cada test
 *   - Tests independientes y repetibles
 *
 * ============================================================================
 * PATRÓN AAA (ARRANGE-ACT-ASSERT):
 * ============================================================================
 * ARRANGE: Insertar datos de prueba en PostgreSQL
 * ACT: Ejecutar query real contra PostgreSQL
 * ASSERT: Verificar resultados de la base de datos real
 *
 * ============================================================================
 */
@Transactional // Rollback automático después de cada test
@DisplayName("TaskRepository - Tests de Integración con PostgreSQL")
class TaskRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    // =========================================================================
    // SETUP Y TEARDOWN
    // =========================================================================

    @BeforeEach
    protected void setUp() {
        // Limpiar la base de datos antes de cada test
        // Aunque @Transactional hace rollback, esto asegura estado limpio
        taskRepository.deleteAll();
    }

    // =========================================================================
    // TESTS: OPERACIONES CRUD BÁSICAS
    // =========================================================================

    @Nested
    @DisplayName("CRUD Operations - Operaciones Básicas")
    class CrudOperationsTests {

        @Test
        @DisplayName("should_SaveAndRetrieveTask_When_ValidTaskProvided")
        void should_SaveAndRetrieveTask_When_ValidTaskProvided() {
            // ARRANGE - Crear tarea de prueba
            Task task = new Task();
            task.setTitle("Integration Test Task");
            task.setDescription("Testing with real PostgreSQL");
            task.setStatus(TaskStatus.PENDING);
            task.setDueDate(LocalDateTime.of(2025, 12, 31, 23, 59));

            // ACT - Guardar en PostgreSQL real
            Task savedTask = taskRepository.save(task);

            // Recuperar de PostgreSQL real
            Optional<Task> retrievedTask = taskRepository.findById(savedTask.getId());

            // ASSERT - Verificar persistencia real
            assertThat(retrievedTask).isPresent();
            assertThat(retrievedTask.get().getId()).isNotNull();
            assertThat(retrievedTask.get().getTitle()).isEqualTo("Integration Test Task");
            assertThat(retrievedTask.get().getDescription()).isEqualTo("Testing with real PostgreSQL");
            assertThat(retrievedTask.get().getStatus()).isEqualTo(TaskStatus.PENDING);
            assertThat(retrievedTask.get().getDueDate()).isEqualTo(LocalDateTime.of(2025, 12, 31, 23, 59));

            // Verificar que Auditable funciona (createdAt y updatedAt)
            assertThat(retrievedTask.get().getCreatedAt()).isNotNull();
            assertThat(retrievedTask.get().getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should_UpdateTask_When_ModifyingExistingTask")
        void should_UpdateTask_When_ModifyingExistingTask() {
            // ARRANGE - Crear y guardar tarea
            Task task = createTask("Original Title", TaskStatus.PENDING);
            Task savedTask = taskRepository.save(task);
            LocalDateTime originalCreatedAt = savedTask.getCreatedAt();

            // Esperar un momento para que updatedAt sea diferente
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // ACT - Modificar tarea
            savedTask.setTitle("Updated Title");
            savedTask.setStatus(TaskStatus.COMPLETED);
            Task updatedTask = taskRepository.save(savedTask);

            // ASSERT - Verificar actualización
            assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");
            assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.COMPLETED);

            // Verificar que createdAt NO cambia pero updatedAt SÍ
            assertThat(updatedTask.getCreatedAt()).isEqualTo(originalCreatedAt);
            assertThat(updatedTask.getUpdatedAt()).isAfter(originalCreatedAt);
        }

        @Test
        @DisplayName("should_DeleteTask_When_TaskExists")
        void should_DeleteTask_When_TaskExists() {
            // ARRANGE
            Task task = createTask("Task to Delete", TaskStatus.PENDING);
            Task savedTask = taskRepository.save(task);
            Long taskId = savedTask.getId();

            // ACT
            taskRepository.deleteById(taskId);

            // ASSERT
            Optional<Task> deletedTask = taskRepository.findById(taskId);
            assertThat(deletedTask).isEmpty();
        }

        @Test
        @DisplayName("should_CountTasks_When_TasksExist")
        void should_CountTasks_When_TasksExist() {
            // ARRANGE - Crear múltiples tareas
            taskRepository.save(createTask("Task 1", TaskStatus.PENDING));
            taskRepository.save(createTask("Task 2", TaskStatus.IN_PROGRESS));
            taskRepository.save(createTask("Task 3", TaskStatus.COMPLETED));

            // ACT
            long count = taskRepository.count();

            // ASSERT
            assertThat(count).isEqualTo(3);
        }
    }

    // =========================================================================
    // TESTS: QUERIES PERSONALIZADOS
    // =========================================================================

    @Nested
    @DisplayName("Custom Queries - Queries Personalizados")
    class CustomQueriesTests {

        @Test
        @DisplayName("should_FindTasksByStatus_When_TasksExist")
        void should_FindTasksByStatus_When_TasksExist() {
            // ARRANGE - Crear tareas con diferentes estados
            taskRepository.save(createTask("Pending 1", TaskStatus.PENDING));
            taskRepository.save(createTask("Pending 2", TaskStatus.PENDING));
            taskRepository.save(createTask("In Progress", TaskStatus.IN_PROGRESS));
            taskRepository.save(createTask("Completed", TaskStatus.COMPLETED));

            // ACT - Buscar tareas PENDING
            List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING);

            // ASSERT
            assertThat(pendingTasks).hasSize(2);
            assertThat(pendingTasks)
                    .extracting(Task::getStatus)
                    .containsOnly(TaskStatus.PENDING);
            assertThat(pendingTasks)
                    .extracting(Task::getTitle)
                    .containsExactlyInAnyOrder("Pending 1", "Pending 2");
        }

        @Test
        @DisplayName("should_FindTasksByTitleContaining_When_UsingCaseInsensitiveSearch")
        void should_FindTasksByTitleContaining_When_UsingCaseInsensitiveSearch() {
            // ARRANGE - Crear tareas con diferentes títulos
            taskRepository.save(createTask("Buy groceries", TaskStatus.PENDING));
            taskRepository.save(createTask("BUY BOOKS", TaskStatus.PENDING));
            taskRepository.save(createTask("Sell old items", TaskStatus.PENDING));
            taskRepository.save(createTask("buy tickets", TaskStatus.PENDING));

            // ACT - Buscar con diferentes casos
            List<Task> tasksWithBuy = taskRepository.findByTitleContainingIgnoreCase("buy");
            List<Task> tasksWithBUY = taskRepository.findByTitleContainingIgnoreCase("BUY");
            List<Task> tasksWithBuY = taskRepository.findByTitleContainingIgnoreCase("BuY");

            // ASSERT - Todas las búsquedas deben retornar el mismo resultado
            assertThat(tasksWithBuy).hasSize(3);
            assertThat(tasksWithBUY).hasSize(3);
            assertThat(tasksWithBuY).hasSize(3);

            // Verificar que encuentra las tareas correctas
            assertThat(tasksWithBuy)
                    .extracting(Task::getTitle)
                    .containsExactlyInAnyOrder("Buy groceries", "BUY BOOKS", "buy tickets");
        }

        @Test
        @DisplayName("should_FindAllTasksOrderedByCreatedAtDesc_When_TasksExist")
        void should_FindAllTasksOrderedByCreatedAtDesc_When_TasksExist() {
            // ARRANGE - Crear tareas en orden específico con delay
            Task task1 = createTask("First Task", TaskStatus.PENDING);
            taskRepository.save(task1);

            sleep(100); // Asegurar diferentes timestamps

            Task task2 = createTask("Second Task", TaskStatus.PENDING);
            taskRepository.save(task2);

            sleep(100);

            Task task3 = createTask("Third Task", TaskStatus.PENDING);
            taskRepository.save(task3);

            // ACT - Obtener tareas ordenadas por createdAt descendente
            List<Task> tasks = taskRepository.findAllByOrderByCreatedAtDesc();

            // ASSERT - Verificar orden (más recientes primero)
            assertThat(tasks).hasSize(3);
            assertThat(tasks.get(0).getTitle()).isEqualTo("Third Task"); // Más reciente
            assertThat(tasks.get(1).getTitle()).isEqualTo("Second Task");
            assertThat(tasks.get(2).getTitle()).isEqualTo("First Task"); // Más antigua
        }

        @Test
        @DisplayName("should_CountTasksByStatus_When_TasksExist")
        void should_CountTasksByStatus_When_TasksExist() {
            // ARRANGE - Crear tareas con diferentes estados
            taskRepository.save(createTask("Task 1", TaskStatus.PENDING));
            taskRepository.save(createTask("Task 2", TaskStatus.PENDING));
            taskRepository.save(createTask("Task 3", TaskStatus.PENDING));
            taskRepository.save(createTask("Task 4", TaskStatus.IN_PROGRESS));
            taskRepository.save(createTask("Task 5", TaskStatus.IN_PROGRESS));
            taskRepository.save(createTask("Task 6", TaskStatus.COMPLETED));

            // ACT
            Long pendingCount = taskRepository.countByStatus(TaskStatus.PENDING);
            Long inProgressCount = taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
            Long completedCount = taskRepository.countByStatus(TaskStatus.COMPLETED);
            Long cancelledCount = taskRepository.countByStatus(TaskStatus.CANCELLED);

            // ASSERT
            assertThat(pendingCount).isEqualTo(3);
            assertThat(inProgressCount).isEqualTo(2);
            assertThat(completedCount).isEqualTo(1);
            assertThat(cancelledCount).isZero();
        }

        @Test
        @DisplayName("should_CheckExistenceByTitle_When_TaskExists")
        void should_CheckExistenceByTitle_When_TaskExists() {
            // ARRANGE
            taskRepository.save(createTask("Unique Task Title", TaskStatus.PENDING));

            // ACT
            boolean exists = taskRepository.existsByTitle("Unique Task Title");
            boolean notExists = taskRepository.existsByTitle("Non Existent Task");

            // ASSERT
            assertThat(exists).isTrue();
            assertThat(notExists).isFalse();
        }
    }

    // =========================================================================
    // TESTS: VALIDACIONES Y CONSTRAINTS
    // =========================================================================

    @Nested
    @DisplayName("Validations & Constraints - Validaciones y Restricciones")
    class ValidationsAndConstraintsTests {

        @Test
        @DisplayName("should_SaveTaskWithNullDescription_When_DescriptionIsOptional")
        void should_SaveTaskWithNullDescription_When_DescriptionIsOptional() {
            // ARRANGE
            Task task = new Task();
            task.setTitle("Task without description");
            task.setDescription(null); // Description es opcional
            task.setStatus(TaskStatus.PENDING);

            // ACT
            Task savedTask = taskRepository.save(task);

            // ASSERT
            assertThat(savedTask.getId()).isNotNull();
            assertThat(savedTask.getDescription()).isNull();
        }

        @Test
        @DisplayName("should_SaveTaskWithNullDueDate_When_DueDateIsOptional")
        void should_SaveTaskWithNullDueDate_When_DueDateIsOptional() {
            // ARRANGE
            Task task = new Task();
            task.setTitle("Task without due date");
            task.setStatus(TaskStatus.PENDING);
            task.setDueDate(null); // DueDate es opcional

            // ACT
            Task savedTask = taskRepository.save(task);

            // ASSERT
            assertThat(savedTask.getId()).isNotNull();
            assertThat(savedTask.getDueDate()).isNull();
        }

        @Test
        @DisplayName("should_SaveTasksWithSameTitle_When_TitleNotUnique")
        void should_SaveTasksWithSameTitle_When_TitleNotUnique() {
            // ARRANGE - Crear dos tareas con el mismo título
            Task task1 = createTask("Duplicate Title", TaskStatus.PENDING);
            Task task2 = createTask("Duplicate Title", TaskStatus.COMPLETED);

            // ACT
            Task savedTask1 = taskRepository.save(task1);
            Task savedTask2 = taskRepository.save(task2);

            // ASSERT - Ambas deben guardarse (title NO es unique)
            assertThat(savedTask1.getId()).isNotNull();
            assertThat(savedTask2.getId()).isNotNull();
            assertThat(savedTask1.getId()).isNotEqualTo(savedTask2.getId());
        }
    }

    // =========================================================================
    // TESTS: TIPOS DE DATOS ESPECÍFICOS DE POSTGRESQL
    // =========================================================================

    @Nested
    @DisplayName("PostgreSQL Specific - Específicos de PostgreSQL")
    class PostgreSQLSpecificTests {

        @Test
        @DisplayName("should_HandleEnumCorrectly_When_SavingTaskStatus")
        void should_HandleEnumCorrectly_When_SavingTaskStatus() {
            // ARRANGE - Crear tarea con cada estado del enum
            for (TaskStatus status : TaskStatus.values()) {
                Task task = createTask("Task with " + status, status);

                // ACT
                Task savedTask = taskRepository.save(task);

                // ASSERT
                Task retrievedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
                assertThat(retrievedTask.getStatus()).isEqualTo(status);
            }
        }

        @Test
        @DisplayName("should_HandleLocalDateTimePrecision_When_SavingTimestamps")
        void should_HandleLocalDateTimePrecision_When_SavingTimestamps() {
            // ARRANGE - Crear fecha con precisión de nanosegundos
            LocalDateTime preciseDateTime = LocalDateTime.of(
                    2025, 11, 15, 14, 30, 45, 123456789
            );

            Task task = createTask("Task with precise date", TaskStatus.PENDING);
            task.setDueDate(preciseDateTime);

            // ACT
            Task savedTask = taskRepository.save(task);

            // ASSERT - PostgreSQL debe preservar la precisión
            Task retrievedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
            assertThat(retrievedTask.getDueDate()).isEqualTo(preciseDateTime);
        }

        @Test
        @DisplayName("should_HandleLongText_When_DescriptionIsVeryLong")
        void should_HandleLongText_When_DescriptionIsVeryLong() {
            // ARRANGE - Crear descripción muy larga (>10000 caracteres)
            String longDescription = "A".repeat(15000);

            Task task = createTask("Task with long description", TaskStatus.PENDING);
            task.setDescription(longDescription);

            // ACT
            Task savedTask = taskRepository.save(task);

            // ASSERT - PostgreSQL TEXT type debe manejar textos largos
            Task retrievedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
            assertThat(retrievedTask.getDescription()).hasSize(15000);
            assertThat(retrievedTask.getDescription()).isEqualTo(longDescription);
        }

        @Test
        @DisplayName("should_HandleSpecialCharacters_When_SavingText")
        void should_HandleSpecialCharacters_When_SavingText() {
            // ARRANGE - Caracteres especiales, emojis, unicode
            String specialTitle = "Task with émojis 🎉 and spéciàl çhars ñ";
            String specialDescription = "Description with\nnewlines\tand\ttabs and 中文字符";

            Task task = createTask(specialTitle, TaskStatus.PENDING);
            task.setDescription(specialDescription);

            // ACT
            Task savedTask = taskRepository.save(task);

            // ASSERT
            Task retrievedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
            assertThat(retrievedTask.getTitle()).isEqualTo(specialTitle);
            assertThat(retrievedTask.getDescription()).isEqualTo(specialDescription);
        }
    }

    // =========================================================================
    // TESTS: CASOS EDGE Y PERFORMANCE
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases & Performance - Casos Límite y Rendimiento")
    class EdgeCasesAndPerformanceTests {

        @Test
        @DisplayName("should_ReturnEmptyList_When_NoTasksMatchCriteria")
        void should_ReturnEmptyList_When_NoTasksMatchCriteria() {
            // ARRANGE
            taskRepository.save(createTask("Some Task", TaskStatus.PENDING));

            // ACT
            List<Task> completedTasks = taskRepository.findByStatus(TaskStatus.COMPLETED);
            List<Task> tasksWithXYZ = taskRepository.findByTitleContainingIgnoreCase("XYZ");

            // ASSERT
            assertThat(completedTasks).isEmpty();
            assertThat(tasksWithXYZ).isEmpty();
        }

        @Test
        @DisplayName("should_HandleBulkInsert_When_SavingManyTasks")
        void should_HandleBulkInsert_When_SavingManyTasks() {
            // ARRANGE - Crear 100 tareas
            for (int i = 1; i <= 100; i++) {
                Task task = createTask("Task " + i, TaskStatus.PENDING);
                taskRepository.save(task);
            }

            // ACT
            long count = taskRepository.count();
            List<Task> allTasks = taskRepository.findAll();

            // ASSERT
            assertThat(count).isEqualTo(100);
            assertThat(allTasks).hasSize(100);
        }

        @Test
        @DisplayName("should_HandlePartialMatches_When_SearchingByTitle")
        void should_HandlePartialMatches_When_SearchingByTitle() {
            // ARRANGE
            taskRepository.save(createTask("Buy groceries", TaskStatus.PENDING));
            taskRepository.save(createTask("Sell groceries", TaskStatus.PENDING));
            taskRepository.save(createTask("Grocery shopping", TaskStatus.PENDING));

            // ACT - Buscar "grocer" (parcial)
            List<Task> matches = taskRepository.findByTitleContainingIgnoreCase("grocer");

            // ASSERT - Debe encontrar todas las que contengan "grocer"
            assertThat(matches).hasSize(3);
        }
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    /**
     * Crea una tarea de prueba con los datos mínimos requeridos.
     */
    private Task createTask(String title, TaskStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Test description for " + title);
        task.setStatus(status);
        task.setDueDate(LocalDateTime.now().plusDays(7));
        return task;
    }

    /**
     * Sleep helper para tests que requieren diferencia temporal.
     */
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
