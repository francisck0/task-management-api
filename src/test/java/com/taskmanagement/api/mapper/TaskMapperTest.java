package com.taskmanagement.api.mapper;

import com.taskmanagement.api.dto.TaskPatchDto;
import com.taskmanagement.api.dto.TaskRequestDto;
import com.taskmanagement.api.dto.TaskResponseDto;
import com.taskmanagement.api.model.Task;
import com.taskmanagement.api.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitarios para TaskMapper.
 *
 * ============================================================================
 * ¿POR QUÉ TESTEAR UN MAPPER?
 * ============================================================================
 * Aunque TaskMapper parece simple, es CRÍTICO testearlo porque:
 *
 * 1. PREVENCIÓN DE BUGS:
 *    - Un error en el mapeo puede causar pérdida de datos
 *    - Mapeos incorrectos pueden exponer datos sensibles
 *    - Null pointer exceptions si no se manejan nulls correctamente
 *
 * 2. CONTRATO CLARO:
 *    - Los tests documentan EXACTAMENTE cómo se mapean los campos
 *    - Sirven como especificación del comportamiento esperado
 *
 * 3. REFACTORING SEGURO:
 *    - Si cambias la lógica de mapeo, los tests detectan regresiones
 *    - Puedes migrar a MapStruct con confianza
 *
 * 4. VALIDACIÓN DE REGLAS DE NEGOCIO:
 *    - Verificar que campos sensibles NO se mapean
 *    - Verificar transformaciones de datos
 *    - Verificar manejo de nulls
 *
 * ============================================================================
 * DIFERENCIA CON TESTS DE SERVICIO:
 * ============================================================================
 * - Tests de Mapper: NO usan @Mock (no hay dependencias externas)
 * - Tests de Mapper: Son PUROS (misma entrada = misma salida)
 * - Tests de Mapper: Son MUY RÁPIDOS (solo lógica de transformación)
 *
 * ============================================================================
 * PATRÓN AAA (ARRANGE-ACT-ASSERT):
 * ============================================================================
 * ARRANGE: Crear objetos de entrada (Task, DTOs)
 * ACT:     Llamar al método del mapper
 * ASSERT:  Verificar que el mapeo es correcto campo por campo
 *
 * ============================================================================
 * NO SE USA @ExtendWith(MockitoExtension.class):
 * ============================================================================
 * TaskMapper es una clase utilitaria con métodos estáticos.
 * No tiene dependencias que mockear.
 * Los tests son PUROS: entrada → transformación → salida
 *
 * ============================================================================
 */
@DisplayName("TaskMapper - Tests Unitarios")
class TaskMapperTest {

    // =========================================================================
    // DATOS DE PRUEBA COMUNES
    // =========================================================================

    private Task task;
    private TaskRequestDto taskRequestDto;
    private TaskPatchDto taskPatchDto;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.of(2025, 11, 15, 12, 0);

        // ARRANGE: Crear una tarea de prueba completa
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(testDateTime);
        task.setCreatedAt(LocalDateTime.of(2025, 11, 1, 10, 0));
        task.setUpdatedAt(LocalDateTime.of(2025, 11, 10, 15, 30));

        // ARRANGE: Crear un DTO de request completo
        taskRequestDto = new TaskRequestDto();
        taskRequestDto.setTitle("New Task");
        taskRequestDto.setDescription("New Description");
        taskRequestDto.setStatus(TaskStatus.IN_PROGRESS);
        taskRequestDto.setDueDate(testDateTime);

        // ARRANGE: Crear un DTO de patch
        taskPatchDto = new TaskPatchDto();
    }

    // =========================================================================
    // TESTS: toEntity()
    // =========================================================================

    @Nested
    @DisplayName("toEntity() - Convertir TaskRequestDto a Task")
    class ToEntityTests {

        @Test
        @DisplayName("should_MapAllFields_When_ConvertingDtoToEntity")
        void should_MapAllFields_When_ConvertingDtoToEntity() {
            // ACT
            Task result = TaskMapper.toEntity(taskRequestDto);

            // ASSERT - Verificar que todos los campos se mapearon correctamente
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("New Task");
            assertThat(result.getDescription()).isEqualTo("New Description");
            assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(result.getDueDate()).isEqualTo(testDateTime);

            // Verificar que campos autogenerados NO se mapean
            assertThat(result.getId()).isNull(); // ID se genera en BD
            assertThat(result.getCreatedAt()).isNull(); // Se gestiona por auditoría
            assertThat(result.getUpdatedAt()).isNull(); // Se gestiona por auditoría
        }

        @Test
        @DisplayName("should_ThrowException_When_DtoIsNull")
        void should_ThrowException_When_DtoIsNull() {
            // ACT & ASSERT
            assertThatThrownBy(() -> TaskMapper.toEntity(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TaskRequestDto cannot be null");
        }

        @Test
        @DisplayName("should_HandleNullDescription_When_ConvertingToEntity")
        void should_HandleNullDescription_When_ConvertingToEntity() {
            // ARRANGE
            taskRequestDto.setDescription(null);

            // ACT
            Task result = TaskMapper.toEntity(taskRequestDto);

            // ASSERT
            assertThat(result.getDescription()).isNull();
            assertThat(result.getTitle()).isNotNull(); // Otros campos siguen mapeados
        }

        @Test
        @DisplayName("should_HandleNullDueDate_When_ConvertingToEntity")
        void should_HandleNullDueDate_When_ConvertingToEntity() {
            // ARRANGE
            taskRequestDto.setDueDate(null);

            // ACT
            Task result = TaskMapper.toEntity(taskRequestDto);

            // ASSERT
            assertThat(result.getDueDate()).isNull();
            assertThat(result.getTitle()).isNotNull(); // Otros campos siguen mapeados
        }

        @Test
        @DisplayName("should_MapAllStatuses_When_ConvertingToEntity")
        void should_MapAllStatuses_When_ConvertingToEntity() {
            // Test con cada estado posible
            for (TaskStatus status : TaskStatus.values()) {
                // ARRANGE
                taskRequestDto.setStatus(status);

                // ACT
                Task result = TaskMapper.toEntity(taskRequestDto);

                // ASSERT
                assertThat(result.getStatus()).isEqualTo(status);
            }
        }
    }

    // =========================================================================
    // TESTS: toResponseDto()
    // =========================================================================

    @Nested
    @DisplayName("toResponseDto() - Convertir Task a TaskResponseDto")
    class ToResponseDtoTests {

        @Test
        @DisplayName("should_MapAllFields_When_ConvertingEntityToDto")
        void should_MapAllFields_When_ConvertingEntityToDto() {
            // ACT
            TaskResponseDto result = TaskMapper.toResponseDto(task);

            // ASSERT - Verificar que TODOS los campos se mapearon
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Test Task");
            assertThat(result.getDescription()).isEqualTo("Test Description");
            assertThat(result.getStatus()).isEqualTo(TaskStatus.PENDING);
            assertThat(result.getDueDate()).isEqualTo(testDateTime);
            assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 11, 1, 10, 0));
            assertThat(result.getUpdatedAt()).isEqualTo(LocalDateTime.of(2025, 11, 10, 15, 30));
        }

        @Test
        @DisplayName("should_ThrowException_When_TaskIsNull")
        void should_ThrowException_When_TaskIsNull() {
            // ACT & ASSERT
            assertThatThrownBy(() -> TaskMapper.toResponseDto(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Task cannot be null");
        }

        @Test
        @DisplayName("should_HandleNullDescription_When_ConvertingToResponseDto")
        void should_HandleNullDescription_When_ConvertingToResponseDto() {
            // ARRANGE
            task.setDescription(null);

            // ACT
            TaskResponseDto result = TaskMapper.toResponseDto(task);

            // ASSERT
            assertThat(result.getDescription()).isNull();
            assertThat(result.getId()).isEqualTo(1L); // Otros campos siguen mapeados
        }

        @Test
        @DisplayName("should_HandleNullDueDate_When_ConvertingToResponseDto")
        void should_HandleNullDueDate_When_ConvertingToResponseDto() {
            // ARRANGE
            task.setDueDate(null);

            // ACT
            TaskResponseDto result = TaskMapper.toResponseDto(task);

            // ASSERT
            assertThat(result.getDueDate()).isNull();
            assertThat(result.getId()).isEqualTo(1L); // Otros campos siguen mapeados
        }

        @Test
        @DisplayName("should_PreserveTimestamps_When_ConvertingToResponseDto")
        void should_PreserveTimestamps_When_ConvertingToResponseDto() {
            // ACT
            TaskResponseDto result = TaskMapper.toResponseDto(task);

            // ASSERT - Los timestamps deben mapearse exactamente
            assertThat(result.getCreatedAt()).isEqualTo(task.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(task.getUpdatedAt());
        }
    }

    // =========================================================================
    // TESTS: updateEntityFromDto()
    // =========================================================================

    @Nested
    @DisplayName("updateEntityFromDto() - Actualizar Task desde TaskRequestDto (PUT)")
    class UpdateEntityFromDtoTests {

        @Test
        @DisplayName("should_UpdateAllEditableFields_When_UpdatingEntity")
        void should_UpdateAllEditableFields_When_UpdatingEntity() {
            // ARRANGE
            TaskRequestDto updateDto = new TaskRequestDto();
            updateDto.setTitle("Updated Title");
            updateDto.setDescription("Updated Description");
            updateDto.setStatus(TaskStatus.COMPLETED);
            updateDto.setDueDate(LocalDateTime.of(2026, 1, 1, 0, 0));

            LocalDateTime originalCreatedAt = task.getCreatedAt();
            Long originalId = task.getId();

            // ACT
            Task result = TaskMapper.updateEntityFromDto(task, updateDto);

            // ASSERT - Verificar que se actualizaron los campos editables
            assertThat(result).isSameAs(task); // Mismo objeto (mutación)
            assertThat(result.getTitle()).isEqualTo("Updated Title");
            assertThat(result.getDescription()).isEqualTo("Updated Description");
            assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);
            assertThat(result.getDueDate()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));

            // Verificar que NO se modificaron campos inmutables
            assertThat(result.getId()).isEqualTo(originalId); // ID no cambia
            assertThat(result.getCreatedAt()).isEqualTo(originalCreatedAt); // createdAt no cambia
        }

        @Test
        @DisplayName("should_ThrowException_When_TaskIsNull")
        void should_ThrowException_When_TaskIsNull() {
            // ACT & ASSERT
            assertThatThrownBy(() -> TaskMapper.updateEntityFromDto(null, taskRequestDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Task cannot be null");
        }

        @Test
        @DisplayName("should_ThrowException_When_DtoIsNull")
        void should_ThrowException_When_DtoIsNull() {
            // ACT & ASSERT
            assertThatThrownBy(() -> TaskMapper.updateEntityFromDto(task, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TaskRequestDto cannot be null");
        }

        @Test
        @DisplayName("should_UpdateToNullDescription_When_DtoDescriptionIsNull")
        void should_UpdateToNullDescription_When_DtoDescriptionIsNull() {
            // ARRANGE
            taskRequestDto.setDescription(null);

            // ACT
            TaskMapper.updateEntityFromDto(task, taskRequestDto);

            // ASSERT
            assertThat(task.getDescription()).isNull();
        }

        @Test
        @DisplayName("should_ReturnSameInstance_When_Updating")
        void should_ReturnSameInstance_When_Updating() {
            // ACT
            Task result = TaskMapper.updateEntityFromDto(task, taskRequestDto);

            // ASSERT - El método retorna la misma instancia (para fluent API)
            assertThat(result).isSameAs(task);
        }
    }

    // =========================================================================
    // TESTS: patchEntityFromDto()
    // =========================================================================

    @Nested
    @DisplayName("patchEntityFromDto() - Actualizar Task parcialmente desde TaskPatchDto (PATCH)")
    class PatchEntityFromDtoTests {

        @Test
        @DisplayName("should_UpdateOnlyTitle_When_OnlyTitleProvided")
        void should_UpdateOnlyTitle_When_OnlyTitleProvided() {
            // ARRANGE
            taskPatchDto.setTitle("Patched Title");
            // Otros campos son null

            String originalDescription = task.getDescription();
            TaskStatus originalStatus = task.getStatus();
            LocalDateTime originalDueDate = task.getDueDate();

            // ACT
            TaskMapper.patchEntityFromDto(task, taskPatchDto);

            // ASSERT
            assertThat(task.getTitle()).isEqualTo("Patched Title"); // CAMBIA
            assertThat(task.getDescription()).isEqualTo(originalDescription); // NO CAMBIA
            assertThat(task.getStatus()).isEqualTo(originalStatus); // NO CAMBIA
            assertThat(task.getDueDate()).isEqualTo(originalDueDate); // NO CAMBIA
        }

        @Test
        @DisplayName("should_UpdateOnlyStatus_When_OnlyStatusProvided")
        void should_UpdateOnlyStatus_When_OnlyStatusProvided() {
            // ARRANGE
            taskPatchDto.setStatus(TaskStatus.COMPLETED);

            String originalTitle = task.getTitle();
            String originalDescription = task.getDescription();
            LocalDateTime originalDueDate = task.getDueDate();

            // ACT
            TaskMapper.patchEntityFromDto(task, taskPatchDto);

            // ASSERT
            assertThat(task.getStatus()).isEqualTo(TaskStatus.COMPLETED); // CAMBIA
            assertThat(task.getTitle()).isEqualTo(originalTitle); // NO CAMBIA
            assertThat(task.getDescription()).isEqualTo(originalDescription); // NO CAMBIA
            assertThat(task.getDueDate()).isEqualTo(originalDueDate); // NO CAMBIA
        }

        @Test
        @DisplayName("should_UpdateMultipleFields_When_MultipleFieldsProvided")
        void should_UpdateMultipleFields_When_MultipleFieldsProvided() {
            // ARRANGE
            taskPatchDto.setTitle("Patched Title");
            taskPatchDto.setStatus(TaskStatus.IN_PROGRESS);
            taskPatchDto.setDueDate(LocalDateTime.of(2026, 6, 1, 12, 0));
            // Description sigue siendo null, no debe cambiar

            String originalDescription = task.getDescription();

            // ACT
            TaskMapper.patchEntityFromDto(task, taskPatchDto);

            // ASSERT
            assertThat(task.getTitle()).isEqualTo("Patched Title"); // CAMBIA
            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS); // CAMBIA
            assertThat(task.getDueDate()).isEqualTo(LocalDateTime.of(2026, 6, 1, 12, 0)); // CAMBIA
            assertThat(task.getDescription()).isEqualTo(originalDescription); // NO CAMBIA
        }

        @Test
        @DisplayName("should_NotUpdateAnyField_When_AllFieldsAreNull")
        void should_NotUpdateAnyField_When_AllFieldsAreNull() {
            // ARRANGE
            TaskPatchDto emptyPatch = new TaskPatchDto(); // Todos los campos null

            String originalTitle = task.getTitle();
            String originalDescription = task.getDescription();
            TaskStatus originalStatus = task.getStatus();
            LocalDateTime originalDueDate = task.getDueDate();

            // ACT
            TaskMapper.patchEntityFromDto(task, emptyPatch);

            // ASSERT - NINGÚN campo debe haber cambiado
            assertThat(task.getTitle()).isEqualTo(originalTitle);
            assertThat(task.getDescription()).isEqualTo(originalDescription);
            assertThat(task.getStatus()).isEqualTo(originalStatus);
            assertThat(task.getDueDate()).isEqualTo(originalDueDate);
        }

        @Test
        @DisplayName("should_UpdateDescriptionToNull_When_DescriptionProvidedAsNull")
        void should_UpdateDescriptionToNull_When_DescriptionProvidedAsNull() {
            // ARRANGE
            // Este test verifica comportamiento: si se envía null explícitamente,
            // ¿debería actualizar a null o ignorar?
            // Según la implementación actual: solo actualiza si NO es null
            taskPatchDto.setDescription(null);

            String originalDescription = task.getDescription();

            // ACT
            TaskMapper.patchEntityFromDto(task, taskPatchDto);

            // ASSERT
            // Comportamiento esperado: NO actualiza si es null
            assertThat(task.getDescription()).isEqualTo(originalDescription);
        }

        @Test
        @DisplayName("should_ThrowException_When_TaskIsNull")
        void should_ThrowException_When_TaskIsNull() {
            // ACT & ASSERT
            assertThatThrownBy(() -> TaskMapper.patchEntityFromDto(null, taskPatchDto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Task cannot be null");
        }

        @Test
        @DisplayName("should_ThrowException_When_PatchDtoIsNull")
        void should_ThrowException_When_PatchDtoIsNull() {
            // ACT & ASSERT
            assertThatThrownBy(() -> TaskMapper.patchEntityFromDto(task, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TaskPatchDto cannot be null");
        }

        @Test
        @DisplayName("should_ReturnSameInstance_When_Patching")
        void should_ReturnSameInstance_When_Patching() {
            // ARRANGE
            taskPatchDto.setTitle("Patched");

            // ACT
            Task result = TaskMapper.patchEntityFromDto(task, taskPatchDto);

            // ASSERT - El método retorna la misma instancia (para fluent API)
            assertThat(result).isSameAs(task);
        }
    }

    // =========================================================================
    // TESTS: copyTaskData()
    // =========================================================================

    @Nested
    @DisplayName("copyTaskData() - Copiar datos entre Tasks")
    class CopyTaskDataTests {

        @Test
        @DisplayName("should_CopyAllEditableFields_When_CopyingTaskData")
        void should_CopyAllEditableFields_When_CopyingTaskData() {
            // ARRANGE
            Task target = new Task();
            target.setId(999L); // ID diferente
            target.setTitle("Old Title");
            target.setDescription("Old Description");
            target.setStatus(TaskStatus.CANCELLED);
            target.setDueDate(LocalDateTime.of(2020, 1, 1, 0, 0));
            target.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0));

            Long originalTargetId = target.getId();
            LocalDateTime originalTargetCreatedAt = target.getCreatedAt();

            // ACT
            Task result = TaskMapper.copyTaskData(task, target);

            // ASSERT - Verificar que se copiaron los campos editables
            assertThat(result).isSameAs(target); // Mismo objeto
            assertThat(result.getTitle()).isEqualTo(task.getTitle());
            assertThat(result.getDescription()).isEqualTo(task.getDescription());
            assertThat(result.getStatus()).isEqualTo(task.getStatus());
            assertThat(result.getDueDate()).isEqualTo(task.getDueDate());

            // Verificar que NO se copiaron campos inmutables
            assertThat(result.getId()).isEqualTo(originalTargetId); // ID no se copia
            assertThat(result.getCreatedAt()).isEqualTo(originalTargetCreatedAt); // createdAt no se copia
        }

        @Test
        @DisplayName("should_ThrowException_When_SourceIsNull")
        void should_ThrowException_When_SourceIsNull() {
            // ARRANGE
            Task target = new Task();

            // ACT & ASSERT
            assertThatThrownBy(() -> TaskMapper.copyTaskData(null, target))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Source and target tasks cannot be null");
        }

        @Test
        @DisplayName("should_ThrowException_When_TargetIsNull")
        void should_ThrowException_When_TargetIsNull() {
            // ACT & ASSERT
            assertThatThrownBy(() -> TaskMapper.copyTaskData(task, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Source and target tasks cannot be null");
        }

        @Test
        @DisplayName("should_ThrowException_When_BothAreNull")
        void should_ThrowException_When_BothAreNull() {
            // ACT & ASSERT
            assertThatThrownBy(() -> TaskMapper.copyTaskData(null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Source and target tasks cannot be null");
        }
    }

    // =========================================================================
    // TESTS: hasAnyFieldToUpdate()
    // =========================================================================

    @Nested
    @DisplayName("hasAnyFieldToUpdate() - Verificar si PatchDto tiene campos para actualizar")
    class HasAnyFieldToUpdateTests {

        @Test
        @DisplayName("should_ReturnTrue_When_TitleIsNotNull")
        void should_ReturnTrue_When_TitleIsNotNull() {
            // ARRANGE
            taskPatchDto.setTitle("Some Title");

            // ACT
            boolean result = TaskMapper.hasAnyFieldToUpdate(taskPatchDto);

            // ASSERT
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_DescriptionIsNotNull")
        void should_ReturnTrue_When_DescriptionIsNotNull() {
            // ARRANGE
            taskPatchDto.setDescription("Some Description");

            // ACT
            boolean result = TaskMapper.hasAnyFieldToUpdate(taskPatchDto);

            // ASSERT
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_StatusIsNotNull")
        void should_ReturnTrue_When_StatusIsNotNull() {
            // ARRANGE
            taskPatchDto.setStatus(TaskStatus.COMPLETED);

            // ACT
            boolean result = TaskMapper.hasAnyFieldToUpdate(taskPatchDto);

            // ASSERT
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_DueDateIsNotNull")
        void should_ReturnTrue_When_DueDateIsNotNull() {
            // ARRANGE
            taskPatchDto.setDueDate(LocalDateTime.now());

            // ACT
            boolean result = TaskMapper.hasAnyFieldToUpdate(taskPatchDto);

            // ASSERT
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should_ReturnFalse_When_AllFieldsAreNull")
        void should_ReturnFalse_When_AllFieldsAreNull() {
            // ARRANGE
            TaskPatchDto emptyPatch = new TaskPatchDto(); // Todos los campos null

            // ACT
            boolean result = TaskMapper.hasAnyFieldToUpdate(emptyPatch);

            // ASSERT
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should_ReturnFalse_When_DtoIsNull")
        void should_ReturnFalse_When_DtoIsNull() {
            // ACT
            boolean result = TaskMapper.hasAnyFieldToUpdate(null);

            // ASSERT
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should_ReturnTrue_When_MultipleFieldsAreNotNull")
        void should_ReturnTrue_When_MultipleFieldsAreNotNull() {
            // ARRANGE
            taskPatchDto.setTitle("Title");
            taskPatchDto.setStatus(TaskStatus.COMPLETED);

            // ACT
            boolean result = TaskMapper.hasAnyFieldToUpdate(taskPatchDto);

            // ASSERT
            assertThat(result).isTrue();
        }
    }

    // =========================================================================
    // TESTS: Constructor (Utility Class)
    // =========================================================================

    @Nested
    @DisplayName("Constructor - Clase Utilitaria")
    class ConstructorTests {

        @Test
        @DisplayName("should_ThrowAssertionError_When_TryingToInstantiate")
        void should_ThrowAssertionError_When_TryingToInstantiate() {
            // ACT & ASSERT
            // Verificar que el constructor privado lanza AssertionError
            assertThatThrownBy(() -> {
                // Usar reflection para acceder al constructor privado
                var constructor = TaskMapper.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            })
            .hasCauseInstanceOf(AssertionError.class)
            .getCause()
            .hasMessage("TaskMapper is a utility class and should not be instantiated");
        }
    }

    // =========================================================================
    // CASOS EDGE (EDGE CASES) Y CASOS ESPECIALES
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases - Casos Límite y Especiales")
    class EdgeCasesTests {

        @Test
        @DisplayName("should_HandleEmptyStrings_When_MappingToEntity")
        void should_HandleEmptyStrings_When_MappingToEntity() {
            // ARRANGE
            taskRequestDto.setTitle("");
            taskRequestDto.setDescription("");

            // ACT
            Task result = TaskMapper.toEntity(taskRequestDto);

            // ASSERT
            assertThat(result.getTitle()).isEmpty();
            assertThat(result.getDescription()).isEmpty();
        }

        @Test
        @DisplayName("should_HandleVeryLongStrings_When_Mapping")
        void should_HandleVeryLongStrings_When_Mapping() {
            // ARRANGE
            String veryLongTitle = "A".repeat(1000);
            String veryLongDescription = "B".repeat(10000);
            taskRequestDto.setTitle(veryLongTitle);
            taskRequestDto.setDescription(veryLongDescription);

            // ACT
            Task result = TaskMapper.toEntity(taskRequestDto);

            // ASSERT
            assertThat(result.getTitle()).hasSize(1000);
            assertThat(result.getDescription()).hasSize(10000);
        }

        @Test
        @DisplayName("should_HandleSpecialCharacters_When_Mapping")
        void should_HandleSpecialCharacters_When_Mapping() {
            // ARRANGE
            taskRequestDto.setTitle("Task with émojis 🎉 and spéciàl çhars");
            taskRequestDto.setDescription("Description with\nnewlines\tand\ttabs");

            // ACT
            Task result = TaskMapper.toEntity(taskRequestDto);

            // ASSERT
            assertThat(result.getTitle()).contains("émojis", "🎉", "spéciàl");
            assertThat(result.getDescription()).contains("\n", "\t");
        }

        @Test
        @DisplayName("should_HandleAllTaskStatuses_When_Converting")
        void should_HandleAllTaskStatuses_When_Converting() {
            // Test que todos los estados se mapean correctamente
            for (TaskStatus status : TaskStatus.values()) {
                // ARRANGE
                task.setStatus(status);

                // ACT
                TaskResponseDto result = TaskMapper.toResponseDto(task);

                // ASSERT
                assertThat(result.getStatus()).isEqualTo(status);
            }
        }

        @Test
        @DisplayName("should_PreserveDateTimePrecision_When_Mapping")
        void should_PreserveDateTimePrecision_When_Mapping() {
            // ARRANGE
            LocalDateTime preciseDateTime = LocalDateTime.of(2025, 11, 15, 14, 30, 45, 123456789);
            task.setDueDate(preciseDateTime);
            task.setCreatedAt(preciseDateTime);
            task.setUpdatedAt(preciseDateTime);

            // ACT
            TaskResponseDto result = TaskMapper.toResponseDto(task);

            // ASSERT - Los nanosegundos deben preservarse
            assertThat(result.getDueDate()).isEqualTo(preciseDateTime);
            assertThat(result.getCreatedAt()).isEqualTo(preciseDateTime);
            assertThat(result.getUpdatedAt()).isEqualTo(preciseDateTime);
        }
    }
}
