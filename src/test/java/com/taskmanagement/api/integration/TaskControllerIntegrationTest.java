package com.taskmanagement.api.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.api.dto.TaskPatchDto;
import com.taskmanagement.api.dto.TaskRequestDto;
import com.taskmanagement.api.model.Task;
import com.taskmanagement.api.model.TaskStatus;
import com.taskmanagement.api.repository.TaskRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración END-TO-END para TaskController.
 *
 * ============================================================================
 * ¿QUÉ SON TESTS END-TO-END (E2E)?
 * ============================================================================
 * Tests E2E verifican el flujo COMPLETO de la aplicación:
 *
 * HTTP Request → Controller → Service → Repository → PostgreSQL
 *      ↓
 * PostgreSQL → Repository → Service → Controller → HTTP Response
 *
 * DIFERENCIAS:
 *
 * Tests Unitarios:
 *   - Testean UNA unidad (método/clase)
 *   - Usan mocks
 *   - Sin BD real
 *   - Muy rápidos (milisegundos)
 *
 * Tests Integración (Repository):
 *   - Testean Repository + PostgreSQL
 *   - Sin HTTP
 *   - Verifican queries reales
 *
 * Tests E2E (Controller):
 *   - Testean TODO el stack
 *   - HTTP Request → Response
 *   - PostgreSQL real
 *   - Verifican flujo completo
 *   - Como si fuera un cliente real (Postman, Frontend)
 *
 * ============================================================================
 * ¿QUÉ ES MockMvc?
 * ============================================================================
 * MockMvc es una herramienta de Spring para simular peticiones HTTP
 * SIN levantar un servidor HTTP real.
 *
 * VENTAJAS:
 * ✅ Simula peticiones HTTP reales (GET, POST, PUT, PATCH, DELETE)
 * ✅ Verifica códigos de estado HTTP (200, 201, 404, 400, etc.)
 * ✅ Verifica headers (Content-Type, Location, etc.)
 * ✅ Verifica el JSON de respuesta
 * ✅ Más rápido que levantar servidor HTTP real
 * ✅ Permite testing de validaciones, excepciones, seguridad
 *
 * EJEMPLO:
 * ```java
 * mockMvc.perform(get("/api/v1/tasks/1"))
 *        .andExpect(status().isOk())
 *        .andExpect(jsonPath("$.title").value("My Task"));
 * ```
 *
 * ============================================================================
 * CONFIGURACIÓN:
 * ============================================================================
 * @AutoConfigureMockMvc:
 *   - Configura automáticamente MockMvc
 *   - Permite inyectar MockMvc con @Autowired
 *
 * @WithMockUser:
 *   - Simula un usuario autenticado
 *   - Necesario porque tenemos Spring Security configurado
 *   - Sin esto, todas las peticiones retornarían 401/403
 *
 * @Transactional:
 *   - Rollback automático después de cada test
 *   - Base de datos limpia para cada test
 *   - Tests independientes y repetibles
 *
 * ============================================================================
 * PATRÓN AAA EN TESTS E2E:
 * ============================================================================
 * ARRANGE: Preparar datos en PostgreSQL
 * ACT: Ejecutar petición HTTP con MockMvc
 * ASSERT: Verificar respuesta HTTP y estado de BD
 *
 * ============================================================================
 * ¿POR QUÉ USAR PostgreSQL REAL?
 * ============================================================================
 * 1. VALIDAR FLUJO COMPLETO:
 *    - Verifica que Controller → Service → Repository → PostgreSQL funciona
 *
 * 2. DETECTAR BUGS REALES:
 *    - Problemas de serialización JSON
 *    - Errores de transacciones
 *    - Problemas de mapeo entidad-DTO
 *
 * 3. CONFIANZA:
 *    - Si estos tests pasan, la API funciona en producción
 *    - No hay sorpresas con "funciona en H2, falla en PostgreSQL"
 *
 * ============================================================================
 */
@AutoConfigureMockMvc
@Transactional
@DisplayName("TaskController - Tests de Integración END-TO-END")
class TaskControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/v1/tasks";

    // =========================================================================
    // SETUP Y TEARDOWN
    // =========================================================================

    @BeforeEach
    void setUp() {
        // Limpiar base de datos antes de cada test
        taskRepository.deleteAll();
    }

    // =========================================================================
    // TESTS: POST /tasks - Crear Tarea
    // =========================================================================

    @Nested
    @DisplayName("POST /tasks - Create Task")
    class CreateTaskTests {

        @Test
        @WithMockUser(username = "testuser", roles = {"USER"})
        @DisplayName("should_ReturnCreatedTask_When_ValidDataProvided")
        void should_ReturnCreatedTask_When_ValidDataProvided() throws Exception {
            // ARRANGE - Preparar DTO de request
            TaskRequestDto requestDto = new TaskRequestDto();
            requestDto.setTitle("New Task from E2E Test");
            requestDto.setDescription("Testing complete flow");
            requestDto.setStatus(TaskStatus.PENDING);
            requestDto.setDueDate(LocalDateTime.of(2025, 12, 31, 23, 59));

            // ACT & ASSERT - Ejecutar POST y verificar respuesta
            MvcResult result = mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print()) // Imprime request/response para debugging
                    .andExpect(status().isCreated()) // 201 Created
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.title").value("New Task from E2E Test"))
                    .andExpect(jsonPath("$.description").value("Testing complete flow"))
                    .andExpect(jsonPath("$.status").value("PENDING"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists())
                    .andReturn();

            // ASSERT - Verificar que se guardó en PostgreSQL
            long count = taskRepository.count();
            assertThat(count).isEqualTo(1);

            Task savedTask = taskRepository.findAll().get(0);
            assertThat(savedTask.getTitle()).isEqualTo("New Task from E2E Test");
        }

        @Test
        @WithMockUser
        @DisplayName("should_ReturnBadRequest_When_TitleIsMissing")
        void should_ReturnBadRequest_When_TitleIsMissing() throws Exception {
            // ARRANGE - DTO sin título (campo obligatorio)
            TaskRequestDto requestDto = new TaskRequestDto();
            requestDto.setDescription("Task without title");
            requestDto.setStatus(TaskStatus.PENDING);

            // ACT & ASSERT - Debe retornar 400 Bad Request
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest()) // 400 Bad Request
                    .andExpect(jsonPath("$.errors").isArray())
                    .andExpect(jsonPath("$.errors[*]").value(hasItem(containsString("título"))));

            // ASSERT - No debe haberse guardado nada
            assertThat(taskRepository.count()).isZero();
        }

        @Test
        @WithMockUser
        @DisplayName("should_ReturnBadRequest_When_StatusIsMissing")
        void should_ReturnBadRequest_When_StatusIsMissing() throws Exception {
            // ARRANGE
            TaskRequestDto requestDto = new TaskRequestDto();
            requestDto.setTitle("Task without status");
            requestDto.setDescription("Testing validation");
            // Status es null (obligatorio)

            // ACT & ASSERT
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            assertThat(taskRepository.count()).isZero();
        }

        @Test
        @DisplayName("should_ReturnUnauthorized_When_NotAuthenticated")
        void should_ReturnUnauthorized_When_NotAuthenticated() throws Exception {
            // ARRANGE
            TaskRequestDto requestDto = new TaskRequestDto();
            requestDto.setTitle("Task");
            requestDto.setStatus(TaskStatus.PENDING);

            // ACT & ASSERT - Sin @WithMockUser, debe retornar 401/403
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isForbidden()); // 403 Forbidden
        }
    }

    // =========================================================================
    // TESTS: GET /tasks - Obtener Todas las Tareas
    // =========================================================================

    @Nested
    @DisplayName("GET /tasks - Get All Tasks")
    class GetAllTasksTests {

        @Test
        @WithMockUser
        @DisplayName("should_ReturnAllTasks_When_TasksExist")
        void should_ReturnAllTasks_When_TasksExist() throws Exception {
            // ARRANGE - Crear tareas en PostgreSQL
            taskRepository.save(createTask("Task 1", TaskStatus.PENDING));
            taskRepository.save(createTask("Task 2", TaskStatus.IN_PROGRESS));
            taskRepository.save(createTask("Task 3", TaskStatus.COMPLETED));

            // ACT & ASSERT
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[*].title", containsInAnyOrder("Task 1", "Task 2", "Task 3")));
        }

        @Test
        @WithMockUser
        @DisplayName("should_ReturnEmptyArray_When_NoTasksExist")
        void should_ReturnEmptyArray_When_NoTasksExist() throws Exception {
            // ACT & ASSERT
            mockMvc.perform(get(BASE_URL))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    // =========================================================================
    // TESTS: GET /tasks/{id} - Obtener Tarea por ID
    // =========================================================================

    @Nested
    @DisplayName("GET /tasks/{id} - Get Task By ID")
    class GetTaskByIdTests {

        @Test
        @WithMockUser
        @DisplayName("should_ReturnTask_When_TaskExists")
        void should_ReturnTask_When_TaskExists() throws Exception {
            // ARRANGE
            Task task = taskRepository.save(createTask("Existing Task", TaskStatus.PENDING));
            Long taskId = task.getId();

            // ACT & ASSERT
            mockMvc.perform(get(BASE_URL + "/{id}", taskId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(taskId))
                    .andExpect(jsonPath("$.title").value("Existing Task"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @WithMockUser
        @DisplayName("should_ReturnNotFound_When_TaskDoesNotExist")
        void should_ReturnNotFound_When_TaskDoesNotExist() throws Exception {
            // ACT & ASSERT
            mockMvc.perform(get(BASE_URL + "/{id}", 999L))
                    .andDo(print())
                    .andExpect(status().isNotFound()) // 404 Not Found
                    .andExpect(jsonPath("$.message").value(containsString("Tarea no encontrada")));
        }
    }

    // =========================================================================
    // TESTS: PUT /tasks/{id} - Actualizar Tarea Completa
    // =========================================================================

    @Nested
    @DisplayName("PUT /tasks/{id} - Update Task")
    class UpdateTaskTests {

        @Test
        @WithMockUser
        @DisplayName("should_UpdateTask_When_ValidDataProvided")
        void should_UpdateTask_When_ValidDataProvided() throws Exception {
            // ARRANGE
            Task existingTask = taskRepository.save(createTask("Original Title", TaskStatus.PENDING));
            Long taskId = existingTask.getId();

            TaskRequestDto updateDto = new TaskRequestDto();
            updateDto.setTitle("Updated Title");
            updateDto.setDescription("Updated Description");
            updateDto.setStatus(TaskStatus.COMPLETED);
            updateDto.setDueDate(LocalDateTime.of(2026, 1, 1, 0, 0));

            // ACT & ASSERT
            mockMvc.perform(put(BASE_URL + "/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(taskId))
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.description").value("Updated Description"))
                    .andExpect(jsonPath("$.status").value("COMPLETED"));

            // ASSERT - Verificar cambio en PostgreSQL
            Task updatedTask = taskRepository.findById(taskId).orElseThrow();
            assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");
            assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        }

        @Test
        @WithMockUser
        @DisplayName("should_ReturnNotFound_When_UpdatingNonExistentTask")
        void should_ReturnNotFound_When_UpdatingNonExistentTask() throws Exception {
            // ARRANGE
            TaskRequestDto updateDto = new TaskRequestDto();
            updateDto.setTitle("Updated Title");
            updateDto.setStatus(TaskStatus.COMPLETED);

            // ACT & ASSERT
            mockMvc.perform(put(BASE_URL + "/{id}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // TESTS: PATCH /tasks/{id} - Actualizar Tarea Parcialmente
    // =========================================================================

    @Nested
    @DisplayName("PATCH /tasks/{id} - Patch Task")
    class PatchTaskTests {

        @Test
        @WithMockUser
        @DisplayName("should_UpdateOnlyProvidedFields_When_Patching")
        void should_UpdateOnlyProvidedFields_When_Patching() throws Exception {
            // ARRANGE
            Task existingTask = taskRepository.save(createTask("Original Title", TaskStatus.PENDING));
            Long taskId = existingTask.getId();
            String originalTitle = existingTask.getTitle();

            TaskPatchDto patchDto = new TaskPatchDto();
            patchDto.setStatus(TaskStatus.COMPLETED);
            // Solo actualizamos status, title debe permanecer igual

            // ACT & ASSERT
            mockMvc.perform(patch(BASE_URL + "/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchDto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(taskId))
                    .andExpect(jsonPath("$.title").value(originalTitle)) // NO CAMBIA
                    .andExpect(jsonPath("$.status").value("COMPLETED")); // CAMBIA

            // ASSERT - Verificar en PostgreSQL
            Task patchedTask = taskRepository.findById(taskId).orElseThrow();
            assertThat(patchedTask.getTitle()).isEqualTo(originalTitle);
            assertThat(patchedTask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        }

        @Test
        @WithMockUser
        @DisplayName("should_UpdateMultipleFields_When_MultipleFieldsProvided")
        void should_UpdateMultipleFields_When_MultipleFieldsProvided() throws Exception {
            // ARRANGE
            Task existingTask = taskRepository.save(createTask("Original", TaskStatus.PENDING));
            Long taskId = existingTask.getId();

            TaskPatchDto patchDto = new TaskPatchDto();
            patchDto.setTitle("Patched Title");
            patchDto.setStatus(TaskStatus.IN_PROGRESS);

            // ACT & ASSERT
            mockMvc.perform(patch(BASE_URL + "/{id}", taskId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patchDto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Patched Title"))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }
    }

    // =========================================================================
    // TESTS: DELETE /tasks/{id} - Eliminar Tarea
    // =========================================================================

    @Nested
    @DisplayName("DELETE /tasks/{id} - Delete Task")
    class DeleteTaskTests {

        @Test
        @WithMockUser
        @DisplayName("should_DeleteTask_When_TaskExists")
        void should_DeleteTask_When_TaskExists() throws Exception {
            // ARRANGE
            Task task = taskRepository.save(createTask("Task to Delete", TaskStatus.PENDING));
            Long taskId = task.getId();

            // ACT & ASSERT
            mockMvc.perform(delete(BASE_URL + "/{id}", taskId))
                    .andDo(print())
                    .andExpect(status().isNoContent()); // 204 No Content

            // ASSERT - Verificar eliminación en PostgreSQL
            assertThat(taskRepository.findById(taskId)).isEmpty();
            assertThat(taskRepository.count()).isZero();
        }

        @Test
        @WithMockUser
        @DisplayName("should_ReturnNotFound_When_DeletingNonExistentTask")
        void should_ReturnNotFound_When_DeletingNonExistentTask() throws Exception {
            // ACT & ASSERT
            mockMvc.perform(delete(BASE_URL + "/{id}", 999L))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // =========================================================================
    // TESTS: GET /tasks/status/{status} - Buscar por Estado
    // =========================================================================

    @Nested
    @DisplayName("GET /tasks/status/{status} - Get Tasks By Status")
    class GetTasksByStatusTests {

        @Test
        @WithMockUser
        @DisplayName("should_ReturnTasksWithStatus_When_TasksExist")
        void should_ReturnTasksWithStatus_When_TasksExist() throws Exception {
            // ARRANGE
            taskRepository.save(createTask("Pending 1", TaskStatus.PENDING));
            taskRepository.save(createTask("Pending 2", TaskStatus.PENDING));
            taskRepository.save(createTask("Completed", TaskStatus.COMPLETED));

            // ACT & ASSERT
            mockMvc.perform(get(BASE_URL + "/status/{status}", "PENDING"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].status", everyItem(is("PENDING"))));
        }
    }

    // =========================================================================
    // TESTS: GET /tasks/search - Buscar por Título
    // =========================================================================

    @Nested
    @DisplayName("GET /tasks/search - Search Tasks By Title")
    class SearchTasksTests {

        @Test
        @WithMockUser
        @DisplayName("should_ReturnMatchingTasks_When_SearchingByTitle")
        void should_ReturnMatchingTasks_When_SearchingByTitle() throws Exception {
            // ARRANGE
            taskRepository.save(createTask("Buy groceries", TaskStatus.PENDING));
            taskRepository.save(createTask("Buy books", TaskStatus.PENDING));
            taskRepository.save(createTask("Sell items", TaskStatus.PENDING));

            // ACT & ASSERT
            mockMvc.perform(get(BASE_URL + "/search")
                            .param("title", "buy"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].title", everyItem(containsStringIgnoringCase("buy"))));
        }
    }

    // =========================================================================
    // TESTS: GET /tasks/statistics - Obtener Estadísticas
    // =========================================================================

    @Nested
    @DisplayName("GET /tasks/statistics - Get Statistics")
    class GetStatisticsTests {

        @Test
        @WithMockUser
        @DisplayName("should_ReturnCorrectStatistics_When_TasksExist")
        void should_ReturnCorrectStatistics_When_TasksExist() throws Exception {
            // ARRANGE
            taskRepository.save(createTask("Pending 1", TaskStatus.PENDING));
            taskRepository.save(createTask("Pending 2", TaskStatus.PENDING));
            taskRepository.save(createTask("In Progress", TaskStatus.IN_PROGRESS));
            taskRepository.save(createTask("Completed", TaskStatus.COMPLETED));

            // ACT & ASSERT
            mockMvc.perform(get(BASE_URL + "/statistics"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalTasks").value(4))
                    .andExpect(jsonPath("$.pendingTasks").value(2))
                    .andExpect(jsonPath("$.inProgressTasks").value(1))
                    .andExpect(jsonPath("$.completedTasks").value(1))
                    .andExpect(jsonPath("$.cancelledTasks").value(0));
        }
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    private Task createTask(String title, TaskStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description for " + title);
        task.setStatus(status);
        task.setDueDate(LocalDateTime.now().plusDays(7));
        return task;
    }
}
