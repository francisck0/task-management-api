package com.taskmanagement.api.controller;

import com.taskmanagement.api.constant.ApiVersion;
import com.taskmanagement.api.dto.ErrorResponseDto;
import com.taskmanagement.api.dto.TaskFilterDto;
import com.taskmanagement.api.dto.TaskResponseDto;
import com.taskmanagement.api.model.TaskStatus;
import com.taskmanagement.api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de CONSULTA (Query) de tareas.
 *
 * PRINCIPIO: Single Responsibility Principle (SRP)
 * - Este controlador se encarga ÚNICAMENTE de operaciones de lectura (GET)
 * - No contiene lógica de modificación de datos
 *
 * SEPARACIÓN DE RESPONSABILIDADES:
 * - TaskQueryController: Operaciones GET (este controlador)
 * - TaskCommandController: Operaciones POST, PUT, PATCH, DELETE
 * - TaskStatisticsController: Estadísticas y reportes
 *
 * VENTAJAS DEL DISEÑO:
 * - Código más organizado y mantenible
 * - Facilita testing unitario (menos dependencias por clase)
 * - Cumple con SOLID principles
 * - Mejor escalabilidad (se pueden separar en microservicios)
 * - Documentación más clara y específica
 *
 * ENDPOINTS INCLUIDOS:
 * - GET /tasks → Obtener todas las tareas (paginado)
 * - GET /tasks/{id} → Obtener tarea por ID
 * - GET /tasks/status/{status} → Buscar por estado (paginado)
 * - GET /tasks/search?title={texto} → Buscar por título (paginado)
 *
 * @see TaskCommandController para operaciones de escritura
 * @see TaskStatisticsController para estadísticas
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Task Queries",
    description = """
        API de consulta de tareas. Permite obtener, buscar y filtrar tareas existentes.
        Todas las operaciones de lectura (GET) con soporte de paginación.
        """
)
public class TaskQueryController {

    private final TaskService taskService;

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
     * Filtra tareas usando criterios avanzados.
     *
     * Endpoint: GET /tasks/filter
     */
    @GetMapping("/filter")
    @Operation(
        summary = "Filtrar tareas con criterios avanzados",
        description = """
            Filtra tareas combinando múltiples criterios de búsqueda. Todos los filtros son opcionales
            y se combinan con AND lógico.

            **Filtros disponibles:**

            1. **Por estado (`status`)**:
               - Valores: PENDING, IN_PROGRESS, COMPLETED, CANCELLED
               - Ejemplo: `status=PENDING`

            2. **Por prioridad (`priority`)**:
               - Valores: LOW, MEDIUM, HIGH, CRITICAL
               - Ejemplo: `priority=HIGH`

            3. **Por fecha de creación:**
               - `createdAfter`: Tareas creadas después de esta fecha
               - `createdBefore`: Tareas creadas antes de esta fecha
               - Formato: ISO 8601 (2025-11-15T10:30:00)
               - Ejemplo: `createdAfter=2025-11-01T00:00:00`

            4. **Por fecha de vencimiento:**
               - `dueDateAfter`: Tareas que vencen después de esta fecha
               - `dueDateBefore`: Tareas que vencen antes de esta fecha
               - Formato: ISO 8601
               - **Tareas vencidas**: `dueDateBefore=2025-11-16T17:41:30` (ahora)
               - **Tareas que vencen pronto**: `dueDateBefore=2025-11-23T17:41:30` (próximos 7 días)

            5. **Búsqueda de texto (`search`)**:
               - Busca en título y descripción (case-insensitive)
               - Ejemplo: `search=documentación`

            **Parámetros de paginación (opcionales):**
            - `page`: Número de página (inicia en 0). Default: 0
            - `size`: Cantidad de elementos por página. Default: 20
            - `sort`: Campo(s) para ordenar. Default: createdAt,desc

            **Ejemplos de uso:**

            1. Tareas pendientes de alta prioridad:
               ```
               /tasks/filter?status=PENDING&priority=HIGH
               ```

            2. Tareas vencidas:
               ```
               /tasks/filter?dueDateBefore=2025-11-16T17:41:30&status=PENDING
               ```

            3. Tareas creadas esta semana con búsqueda:
               ```
               /tasks/filter?createdAfter=2025-11-09T17:41:30&search=proyecto
               ```

            4. Tareas críticas que vencen pronto:
               ```
               /tasks/filter?priority=CRITICAL&dueDateBefore=2025-11-23T17:41:30&status=IN_PROGRESS
               ```

            5. Tareas completadas este mes:
               ```
               /tasks/filter?status=COMPLETED&createdAfter=2025-11-01T00:00:00&createdBefore=2025-11-30T23:59:59
               ```

            **SQL generado (ejemplo):**
            ```sql
            SELECT * FROM tasks
            WHERE status = 'PENDING'
              AND priority = 'HIGH'
              AND created_at >= '2025-11-01 00:00:00'
              AND due_date <= '2025-11-30 23:59:59'
              AND (LOWER(title) LIKE '%proyecto%'
                   OR LOWER(description) LIKE '%proyecto%')
              AND deleted_at IS NULL
            ORDER BY created_at DESC
            LIMIT 20 OFFSET 0
            ```

            **Respuesta incluye metadata de paginación** (content, totalElements, totalPages, etc.)
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Página de tareas que cumplen los criterios de filtrado",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros de filtrado inválidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class)
            )
        )
    })
    public ResponseEntity<Page<TaskResponseDto>> filterTasks(
            @Parameter(
                description = "Criterios de filtrado (todos opcionales)",
                schema = @Schema(implementation = TaskFilterDto.class)
            )
            @ModelAttribute TaskFilterDto filters,
            @Parameter(
                description = "Parámetros de paginación y ordenamiento",
                example = "page=0&size=20&sort=createdAt,desc"
            )
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {

        log.info("Petición recibida para filtrar tareas con criterios: {} (paginadas) - Página: {}, Tamaño: {}",
                filters, pageable.getPageNumber(), pageable.getPageSize());

        Page<TaskResponseDto> tasks = taskService.filterTasks(filters, pageable);

        log.info("Tareas filtradas: {} de {} total", tasks.getNumberOfElements(), tasks.getTotalElements());

        return ResponseEntity.ok(tasks);
    }
}
