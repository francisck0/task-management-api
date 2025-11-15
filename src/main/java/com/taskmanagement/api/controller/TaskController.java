package com.taskmanagement.api.controller;

import com.taskmanagement.api.dto.ErrorResponseDto;
import com.taskmanagement.api.dto.TaskPatchDto;
import com.taskmanagement.api.dto.TaskRequestDto;
import com.taskmanagement.api.dto.TaskResponseDto;
import com.taskmanagement.api.model.TaskStatus;
import com.taskmanagement.api.service.TaskService;
import com.taskmanagement.api.service.TaskStatisticsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar las operaciones CRUD de tareas.
 *
 * Capa CONTROLLER: Punto de entrada de las peticiones HTTP.
 * Responsable de:
 * - Recibir y validar las peticiones HTTP
 * - Delegar la lógica de negocio a la capa de servicio
 * - Devolver respuestas HTTP apropiadas
 *
 * Anotaciones:
 * @RestController: Combina @Controller + @ResponseBody (todas las respuestas son JSON)
 * @RequestMapping: Define el path base para todos los endpoints de este controlador
 * @RequiredArgsConstructor: Inyección de dependencias por constructor
 * @Slf4j: Logger automático
 * @Tag: Documentación OpenAPI - agrupa endpoints en Swagger UI
 *
 * Best practices implementadas:
 * - Versionado de API mediante context-path (configurado en application.yml)
 * - Uso de DTOs para desacoplar la API del modelo de dominio
 * - Validación de entrada con @Valid
 * - Códigos de estado HTTP apropiados
 * - Logging de operaciones importantes
 * - Documentación completa con OpenAPI annotations
 */
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Tasks",
    description = """
        API para gestión de tareas. Permite operaciones CRUD completas sobre tareas,
        búsqueda por estado y título, actualización parcial con PATCH, y estadísticas.
        """
)
public class TaskController {

    private final TaskService taskService;

    /**
     * Crea una nueva tarea.
     *
     * Endpoint: POST /tasks
     */
    @PostMapping
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
     * Obtiene todas las tareas con paginación.
     *
     * Endpoint: GET /tasks
     */
    @GetMapping
    @Operation(
        summary = "Obtener todas las tareas con paginación",
        description = """
            Retorna una página de tareas del sistema con soporte de paginación y ordenamiento.

            **Parámetros de paginación (opcionales):**
            - `page`: Número de página (inicia en 0). Default: 0
            - `size`: Cantidad de elementos por página. Default: 20
            - `sort`: Campo(s) para ordenar. Formato: campo,direccion (ej: createdAt,desc)

            **Ejemplos de uso:**
            - `/tasks` → Primera página con 20 elementos
            - `/tasks?page=1&size=10` → Segunda página con 10 elementos
            - `/tasks?page=0&size=5&sort=title,asc` → Primera página, 5 elementos, ordenado por título ascendente
            - `/tasks?sort=createdAt,desc&sort=title,asc` → Ordenamiento múltiple

            **Respuesta incluye:**
            - `content`: Lista de tareas de la página actual
            - `totalElements`: Total de tareas en la base de datos
            - `totalPages`: Total de páginas disponibles
            - `number`: Número de página actual
            - `size`: Tamaño de página
            - `first`: ¿Es la primera página?
            - `last`: ¿Es la última página?
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Página de tareas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    public ResponseEntity<Page<TaskResponseDto>> getAllTasks(
            @Parameter(
                description = "Parámetros de paginación y ordenamiento",
                example = "page=0&size=20&sort=createdAt,desc"
            )
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {

        log.info("Petición recibida para obtener todas las tareas (paginadas) - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<TaskResponseDto> tasks = taskService.getAllTasks(pageable);

        return ResponseEntity.ok(tasks);
    }

    /**
     * Obtiene una tarea por su ID.
     *
     * Endpoint: GET /tasks/{id}
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener una tarea por ID",
        description = """
            Retorna los datos completos de una tarea específica identificada por su ID.

            Si la tarea no existe, retorna un error 404.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tarea encontrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada con el ID especificado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2025-11-12T10:30:00",
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
    public ResponseEntity<TaskResponseDto> getTaskById(
            @Parameter(
                description = "ID de la tarea a consultar",
                example = "1",
                required = true
            )
            @PathVariable Long id) {

        log.info("Petición recibida para obtener tarea con ID: {}", id);

        TaskResponseDto task = taskService.getTaskById(id);

        return ResponseEntity.ok(task);
    }

    /**
     * Busca tareas por estado con paginación.
     *
     * Endpoint: GET /tasks/status/{status}
     */
    @GetMapping("/status/{status}")
    @Operation(
        summary = "Buscar tareas por estado con paginación",
        description = """
            Retorna una página de tareas que tienen el estado especificado.

            **Estados válidos:**
            - **PENDING**: Tareas pendientes de iniciar
            - **IN_PROGRESS**: Tareas en progreso
            - **COMPLETED**: Tareas completadas
            - **CANCELLED**: Tareas canceladas

            **Parámetros de paginación (opcionales):**
            - `page`: Número de página (inicia en 0). Default: 0
            - `size`: Cantidad de elementos por página. Default: 20
            - `sort`: Campo(s) para ordenar. Formato: campo,direccion (ej: createdAt,desc)

            **Ejemplos de uso:**
            - `/tasks/status/PENDING` → Primera página de tareas pendientes
            - `/tasks/status/COMPLETED?page=1&size=10` → Segunda página de tareas completadas
            - `/tasks/status/IN_PROGRESS?sort=dueDate,asc` → Tareas en progreso ordenadas por fecha límite

            **Respuesta incluye metadata de paginación** (content, totalElements, totalPages, etc.)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Página de tareas con el estado especificado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Estado inválido proporcionado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    public ResponseEntity<Page<TaskResponseDto>> getTasksByStatus(
            @Parameter(
                description = "Estado de las tareas a buscar",
                example = "PENDING",
                required = true,
                schema = @Schema(allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
            )
            @PathVariable TaskStatus status,
            @Parameter(
                description = "Parámetros de paginación y ordenamiento",
                example = "page=0&size=20&sort=createdAt,desc"
            )
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {

        log.info("Petición recibida para obtener tareas con estado: {} (paginadas) - Página: {}, Tamaño: {}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        Page<TaskResponseDto> tasks = taskService.getTasksByStatus(status, pageable);

        return ResponseEntity.ok(tasks);
    }

    /**
     * Busca tareas por título con paginación (búsqueda parcial).
     *
     * Endpoint: GET /tasks/search?title={texto}
     */
    @GetMapping("/search")
    @Operation(
        summary = "Buscar tareas por título con paginación",
        description = """
            Realiza una búsqueda parcial (case-insensitive) en el título de las tareas con soporte de paginación.

            **Ejemplos de búsqueda:**
            - `?title=comprar` → Encuentra "Comprar pan", "comprar leche", "COMPRAR todo"
            - `?title=reunión` → Encuentra "Reunión con cliente", "Preparar reunión"

            La búsqueda no distingue entre mayúsculas y minúsculas.

            **Parámetros de paginación (opcionales):**
            - `page`: Número de página (inicia en 0). Default: 0
            - `size`: Cantidad de elementos por página. Default: 20
            - `sort`: Campo(s) para ordenar. Formato: campo,direccion (ej: createdAt,desc)

            **Ejemplos de uso completo:**
            - `/tasks/search?title=comprar` → Primera página de resultados
            - `/tasks/search?title=reunión&page=1&size=10` → Segunda página con 10 resultados
            - `/tasks/search?title=proyecto&sort=dueDate,asc` → Ordenado por fecha límite

            **Respuesta incluye metadata de paginación** (content, totalElements, totalPages, etc.)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Página de tareas que coinciden con la búsqueda",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    public ResponseEntity<Page<TaskResponseDto>> searchTasks(
            @Parameter(
                description = "Texto a buscar en el título de las tareas (case-insensitive)",
                example = "comprar",
                required = true
            )
            @RequestParam String title,
            @Parameter(
                description = "Parámetros de paginación y ordenamiento",
                example = "page=0&size=20&sort=createdAt,desc"
            )
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {

        log.info("Petición recibida para buscar tareas con título: {} (paginadas) - Página: {}, Tamaño: {}",
                title, pageable.getPageNumber(), pageable.getPageSize());

        Page<TaskResponseDto> tasks = taskService.searchTasksByTitle(title, pageable);

        return ResponseEntity.ok(tasks);
    }

    /**
     * Obtiene estadísticas sobre las tareas.
     *
     * Endpoint: GET /tasks/statistics
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Obtener estadísticas de tareas",
        description = """
            Retorna un resumen estadístico con el conteo de tareas por estado.

            **Datos incluidos:**
            - Total de tareas en el sistema
            - Tareas pendientes (PENDING)
            - Tareas en progreso (IN_PROGRESS)
            - Tareas completadas (COMPLETED)
            - Tareas canceladas (CANCELLED)

            Útil para dashboards y reportes.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Estadísticas obtenidas exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskStatisticsDto.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "totalTasks": 42,
                          "pendingTasks": 15,
                          "inProgressTasks": 10,
                          "completedTasks": 12,
                          "cancelledTasks": 5
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<TaskStatisticsDto> getStatistics() {
        log.info("Petición recibida para obtener estadísticas");

        TaskStatisticsDto stats = taskService.getStatistics();

        return ResponseEntity.ok(stats);
    }

    /**
     * Actualiza completamente una tarea existente.
     *
     * Endpoint: PUT /tasks/{id}
     */
    @PutMapping("/{id}")
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
    @Operation(
        summary = "Eliminar una tarea",
        description = """
            Elimina permanentemente una tarea del sistema.

            **ADVERTENCIA:** Esta operación es irreversible. La tarea y todos sus datos
            asociados serán eliminados permanentemente.

            **Respuesta exitosa:** Código HTTP 204 (No Content) sin cuerpo de respuesta.

            **Recomendación:** En producción, considerar implementar "soft delete" (eliminación lógica)
            en lugar de eliminación física.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Tarea eliminada exitosamente (sin contenido en la respuesta)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada con el ID especificado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2025-11-12T10:30:00",
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
