package com.taskmanagement.api.controller;

import com.taskmanagement.api.aspect.Auditable;
import com.taskmanagement.api.constant.ApiVersion;
import com.taskmanagement.api.dto.ErrorResponseDto;
import com.taskmanagement.api.dto.TaskPatchDto;
import com.taskmanagement.api.dto.TaskRequestDto;
import com.taskmanagement.api.dto.TaskResponseDto;
import com.taskmanagement.api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de COMANDO (Command) de tareas.
 *
 * PRINCIPIO: Single Responsibility Principle (SRP)
 * - Este controlador se encarga ÚNICAMENTE de operaciones de escritura
 * - Gestiona la creación, actualización y eliminación de tareas
 *
 * PATRÓN: Command Query Responsibility Segregation (CQRS)
 * - Separación clara entre comandos (modificación) y queries (lectura)
 * - Facilita la implementación de auditoría y logging
 * - Permite optimizar cada tipo de operación independientemente
 *
 * SEPARACIÓN DE RESPONSABILIDADES:
 * - TaskQueryController: Operaciones GET
 * - TaskCommandController: Operaciones POST, PUT, PATCH, DELETE (este controlador)
 * - TaskStatisticsController: Estadísticas y reportes
 *
 * VENTAJAS DEL DISEÑO:
 * - Código más organizado y mantenible
 * - Facilita implementación de seguridad diferenciada (permisos de lectura vs escritura)
 * - Mejor trazabilidad de operaciones que modifican datos
 * - Facilita testing unitario
 * - Cumple con SOLID principles
 *
 * ENDPOINTS INCLUIDOS:
 * - POST /tasks → Crear nueva tarea
 * - PUT /tasks/{id} → Actualizar completamente una tarea
 * - PATCH /tasks/{id} → Actualizar parcialmente una tarea
 * - DELETE /tasks/{id} → Eliminar una tarea
 *
 * @see TaskQueryController para operaciones de lectura
 * @see TaskStatisticsController para estadísticas
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Task Commands",
    description = """
        API de comandos de tareas. Permite crear, actualizar y eliminar tareas.
        Todas las operaciones de escritura (POST, PUT, PATCH, DELETE).
        """
)
public class TaskCommandController {

    private final TaskService taskService;

    /**
     * Crea una nueva tarea.
     *
     * Endpoint: POST /tasks
     */
    @PostMapping
    @Auditable(
        action = "CREATE_TASK",
        resource = "Task",
        description = "Usuario crea una nueva tarea en el sistema",
        logParameters = false,
        logResult = true
    )
    @Operation(
        summary = "Crear una nueva tarea",
        description = """
            Crea una nueva tarea en el sistema.

            **Campos requeridos:**
            - `title`: Título de la tarea (1-100 caracteres)
            - `status`: Estado inicial (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)

            **Campos opcionales:**
            - `description`: Descripción detallada (máximo 1000 caracteres)
            - `dueDate`: Fecha límite en formato ISO 8601 (ej: 2025-11-15T18:00:00)

            **Campos automáticos:**
            - `id`: Generado automáticamente
            - `createdAt`: Fecha de creación automática
            - `updatedAt`: Fecha de actualización automática

            La tarea se crea con auditoría automática de fechas.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Tarea creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponseDto.class),
                examples = @ExampleObject(
                    name = "Tarea creada",
                    value = """
                        {
                          "id": 1,
                          "title": "Comprar ingredientes",
                          "description": "Comprar tomates, cebolla, ajo",
                          "status": "PENDING",
                          "dueDate": "2025-11-15T18:00:00",
                          "createdAt": "2025-11-12T10:30:00",
                          "updatedAt": "2025-11-12T10:30:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación en los datos proporcionados",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    name = "Error de validación",
                    value = """
                        {
                          "timestamp": "2025-11-12T10:30:00",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Error de validación en los datos proporcionados",
                          "path": "/api/v1/tasks",
                          "errors": [
                            "title: El título es obligatorio",
                            "status: El estado es obligatorio"
                          ]
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<TaskResponseDto> createTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos de la tarea a crear",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = TaskRequestDto.class)
                )
            )
            @Valid @RequestBody TaskRequestDto taskRequestDto) {

        log.info("Petición recibida para crear tarea: {}", taskRequestDto.getTitle());

        TaskResponseDto createdTask = taskService.createTask(taskRequestDto);

        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    /**
     * Actualiza completamente una tarea existente.
     *
     * Endpoint: PUT /tasks/{id}
     */
    @PutMapping("/{id}")
    @Auditable(
        action = "UPDATE_TASK",
        resource = "Task",
        description = "Usuario actualiza completamente una tarea existente (PUT)",
        logParameters = false,
        logResult = true
    )
    @Operation(
        summary = "Actualizar completamente una tarea (PUT)",
        description = """
            Actualiza **TODOS** los campos de una tarea existente.

            **Diferencia PUT vs PATCH:**
            - **PUT**: Reemplaza completamente el recurso → todos los campos son obligatorios
            - **PATCH**: Actualiza solo los campos enviados → campos opcionales

            **Campos requeridos:**
            - `title`: Nuevo título
            - `status`: Nuevo estado
            - `description`: Nueva descripción (puede ser null)
            - `dueDate`: Nueva fecha límite (puede ser null)

            **Campos automáticos:**
            - `updatedAt`: Se actualiza automáticamente

            **Nota:** `id` y `createdAt` nunca se modifican.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tarea actualizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación en los datos proporcionados",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada con el ID especificado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    public ResponseEntity<TaskResponseDto> updateTask(
            @Parameter(
                description = "ID de la tarea a actualizar",
                example = "1",
                required = true
            )
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Nuevos datos completos de la tarea",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = TaskRequestDto.class)
                )
            )
            @Valid @RequestBody TaskRequestDto taskRequestDto) {

        log.info("Petición recibida para actualizar completamente tarea con ID: {}", id);

        TaskResponseDto updatedTask = taskService.updateTask(id, taskRequestDto);

        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Actualiza parcialmente una tarea existente.
     *
     * Endpoint: PATCH /tasks/{id}
     */
    @PatchMapping("/{id}")
    @Auditable(
        action = "PATCH_TASK",
        resource = "Task",
        description = "Usuario actualiza parcialmente una tarea existente (PATCH)",
        logParameters = false,
        logResult = true
    )
    @Operation(
        summary = "Actualizar parcialmente una tarea (PATCH)",
        description = """
            Actualiza **SOLO** los campos enviados de una tarea existente.

            **Diferencia PUT vs PATCH:**
            - **PUT**: Reemplaza completamente el recurso → todos los campos son obligatorios
            - **PATCH**: Actualiza solo los campos enviados → campos opcionales

            **Ventajas de PATCH:**
            - Solo envías lo que quieres cambiar
            - Los campos no enviados permanecen sin cambios
            - Ideal para cambios pequeños (ej: solo cambiar el estado)

            **Ejemplo de uso:**
            ```json
            {
              "status": "COMPLETED"
            }
            ```
            Solo actualiza el estado, los demás campos permanecen iguales.

            **Campos opcionales (todos):**
            - `title`: Nuevo título
            - `description`: Nueva descripción
            - `status`: Nuevo estado
            - `dueDate`: Nueva fecha límite

            **Campos automáticos:**
            - `updatedAt`: Se actualiza automáticamente

            **Nota:** Puedes enviar solo 1 campo o todos los campos que necesites actualizar.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tarea actualizada parcialmente exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponseDto.class),
                examples = @ExampleObject(
                    name = "Actualización parcial exitosa",
                    description = "Solo se actualizó el campo 'status'",
                    value = """
                        {
                          "id": 1,
                          "title": "Comprar ingredientes",
                          "description": "Comprar tomates, cebolla, ajo",
                          "status": "COMPLETED",
                          "dueDate": "2025-11-15T18:00:00",
                          "createdAt": "2025-11-12T10:30:00",
                          "updatedAt": "2025-11-12T15:45:00"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error de validación en los datos proporcionados",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada con el ID especificado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    public ResponseEntity<TaskResponseDto> patchTask(
            @Parameter(
                description = "ID de la tarea a actualizar parcialmente",
                example = "1",
                required = true
            )
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Campos a actualizar (solo los que se envíen serán modificados)",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = TaskPatchDto.class),
                    examples = {
                        @ExampleObject(
                            name = "Solo actualizar estado",
                            description = "Actualiza únicamente el estado de la tarea",
                            value = """
                                {
                                  "status": "COMPLETED"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Actualizar título y descripción",
                            description = "Actualiza título y descripción, el resto permanece igual",
                            value = """
                                {
                                  "title": "Nuevo título actualizado",
                                  "description": "Nueva descripción actualizada"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Actualizar múltiples campos",
                            description = "Actualiza varios campos a la vez",
                            value = """
                                {
                                  "title": "Tarea actualizada",
                                  "status": "IN_PROGRESS",
                                  "dueDate": "2025-12-01T12:00:00"
                                }
                                """
                        )
                    }
                )
            )
            @Valid @RequestBody TaskPatchDto taskPatchDto) {

        log.info("Petición recibida para actualizar parcialmente tarea con ID: {}", id);

        TaskResponseDto updatedTask = taskService.patchTask(id, taskPatchDto);

        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Elimina una tarea.
     *
     * Endpoint: DELETE /tasks/{id}
     */
    @DeleteMapping("/{id}")
    @Auditable(
        action = "DELETE_TASK",
        resource = "Task",
        description = "Usuario elimina una tarea (soft delete)",
        logParameters = false,
        logResult = false
    )
    @Operation(
        summary = "Eliminar una tarea (soft delete)",
        description = """
            Elimina LÓGICAMENTE una tarea del sistema (soft delete).

            **SOFT DELETE IMPLEMENTADO:**
            - La tarea NO se elimina físicamente de la base de datos
            - Se marca con una fecha de eliminación (deletedAt)
            - La tarea no aparecerá en consultas normales
            - Se puede restaurar usando el endpoint de restauración
            - Cumple con regulaciones de retención de datos

            **Ventajas:**
            - Recuperación de datos eliminados accidentalmente
            - Auditoría completa de eliminaciones
            - Cumplimiento GDPR y regulaciones
            - Trazabilidad total

            **Restauración:**
            - Usar endpoint: POST /tasks/{id}/restore
            - La tarea volverá a estar disponible

            **Eliminación permanente:**
            - Solo mediante proceso de purge automatizado
            - Tareas eliminadas hace más de 90 días se eliminan físicamente
            - Configurado por administradores del sistema

            **Respuesta exitosa:** Código HTTP 204 (No Content) sin cuerpo de respuesta.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Tarea eliminada lógicamente (soft delete) - Sin contenido en la respuesta"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada con el ID especificado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    name = "Tarea no encontrada",
                    value = """
                        {
                          "timestamp": "2025-11-15T10:30:00",
                          "status": 404,
                          "error": "Not Found",
                          "message": "Tarea no encontrada con ID: 999",
                          "path": "/api/v1/tasks/999"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<Void> deleteTask(
            @Parameter(
                description = "ID de la tarea a eliminar",
                example = "1",
                required = true
            )
            @PathVariable Long id) {

        log.info("Petición recibida para eliminar tarea con ID: {}", id);

        taskService.deleteTask(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
