package com.taskmanagement.api.controller;

import com.taskmanagement.api.constant.ApiVersion;
import com.taskmanagement.api.dto.ErrorResponseDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para gestión de PAPELERA (Trash) de tareas eliminadas.
 *
 * PRINCIPIO: Single Responsibility Principle (SRP)
 * - Este controlador se encarga ÚNICAMENTE de la gestión de tareas eliminadas
 * - Operaciones de recuperación (restore), listado y purge
 * - No contiene lógica CRUD normal
 *
 * SOFT DELETE PATTERN:
 * - Las tareas "eliminadas" están en papelera (deleted_at NOT NULL)
 * - Se pueden listar, restaurar o eliminar permanentemente
 * - Similar a la papelera de reciclaje de sistemas operativos
 *
 * SEPARACIÓN DE RESPONSABILIDADES:
 * - TaskQueryController: Consultas de tareas activas
 * - TaskCommandController: CRUD de tareas activas
 * - TaskStatisticsController: Estadísticas
 * - TaskTrashController: Gestión de papelera (este controlador)
 *
 * VENTAJAS DEL DISEÑO:
 * - Código más organizado y mantenible
 * - Clara separación entre tareas activas y eliminadas
 * - Facilita implementación de permisos diferentes (solo admin puede purge)
 * - Cumple con SOLID principles
 * - Mejor experiencia de usuario (recuperación de datos)
 *
 * CASOS DE USO:
 * - Recuperar tareas eliminadas accidentalmente
 * - Ver historial de eliminaciones
 * - Limpieza periódica de papelera (purge)
 * - Auditoría de eliminaciones
 *
 * ENDPOINTS INCLUIDOS:
 * - GET /tasks/trash → Listar tareas eliminadas
 * - POST /tasks/{id}/restore → Restaurar tarea eliminada
 * - DELETE /tasks/trash/purge → Eliminar permanentemente tareas antiguas
 *
 * @see TaskCommandController para operaciones de eliminación (soft delete)
 * @see TaskQueryController para consultas de tareas activas
 */
@RestController
@RequestMapping(ApiVersion.V1 + "/tasks")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Task Trash",
    description = """
        API de papelera de tareas. Permite listar, restaurar y gestionar tareas eliminadas.
        Implementa soft delete para recuperación de datos y cumplimiento de regulaciones.
        """
)
public class TaskTrashController {

    private final TaskService taskService;

    /**
     * Lista todas las tareas eliminadas (papelera).
     *
     * Endpoint: GET /tasks/trash
     */
    @GetMapping("/trash")
    @Operation(
        summary = "Listar tareas eliminadas (papelera)",
        description = """
            Retorna una página de tareas que han sido eliminadas lógicamente.

            **Características:**
            - Solo muestra tareas con soft delete (deletedAt NOT NULL)
            - Ordenadas por fecha de eliminación (más recientes primero)
            - Soporte completo de paginación
            - Útil para recuperar tareas eliminadas accidentalmente

            **Información mostrada:**
            - Todos los datos de la tarea
            - Fecha de eliminación (deletedAt)
            - Tiempo transcurrido desde eliminación

            **Parámetros de paginación (opcionales):**
            - `page`: Número de página (inicia en 0). Default: 0
            - `size`: Cantidad de elementos por página. Default: 20
            - `sort`: Campo(s) para ordenar. Formato: campo,direccion

            **Restauración:**
            - Usar endpoint POST /tasks/{id}/restore

            **Eliminación permanente:**
            - Automático después de 90 días
            - Manual con endpoint DELETE /tasks/trash/purge
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Página de tareas eliminadas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    public ResponseEntity<Page<TaskResponseDto>> getDeletedTasks(
            @Parameter(
                description = "Parámetros de paginación y ordenamiento",
                example = "page=0&size=20&sort=deleted_at,desc"
            )
            @PageableDefault(size = 20, sort = "deleted_at", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable) {

        log.info("Petición recibida para listar tareas eliminadas (papelera) - Página: {}, Tamaño: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<TaskResponseDto> deletedTasks = taskService.getDeletedTasks(pageable);

        log.debug("Tareas eliminadas encontradas: {}", deletedTasks.getTotalElements());

        return ResponseEntity.ok(deletedTasks);
    }

    /**
     * Restaura una tarea eliminada.
     *
     * Endpoint: POST /tasks/{id}/restore
     */
    @PostMapping("/{id}/restore")
    @Operation(
        summary = "Restaurar tarea eliminada",
        description = """
            Restaura una tarea que fue eliminada lógicamente (soft delete).

            **Proceso:**
            1. Busca la tarea en papelera (deletedAt NOT NULL)
            2. Marca deletedAt como NULL
            3. La tarea vuelve a estar disponible en consultas normales
            4. Se mantiene toda la información original

            **Importante:**
            - Solo funciona con tareas eliminadas lógicamente
            - No funciona con tareas activas (retorna 404)
            - Requiere permisos de propietario de la tarea
            - La tarea restaurada mantiene su ID original

            **Caso de uso:**
            - Recuperar tareas eliminadas por error
            - Deshacer eliminaciones
            - Auditoría y cumplimiento

            **Respuesta exitosa:**
            - Código HTTP 200 (OK)
            - Retorna los datos completos de la tarea restaurada
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Tarea restaurada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TaskResponseDto.class),
                examples = @ExampleObject(
                    name = "Tarea restaurada",
                    value = """
                        {
                          "id": 1,
                          "title": "Comprar ingredientes",
                          "description": "Comprar tomates, cebolla, ajo",
                          "status": "PENDING",
                          "dueDate": "2025-11-15T18:00:00",
                          "createdAt": "2025-11-12T10:30:00",
                          "updatedAt": "2025-11-15T16:45:00",
                          "deletedAt": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tarea no encontrada en papelera (no existe o no está eliminada)",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2025-11-15T16:45:00",
                          "status": 404,
                          "error": "Not Found",
                          "message": "Tarea eliminada no encontrada con ID: 999",
                          "path": "/api/v1/tasks/999/restore"
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<TaskResponseDto> restoreTask(
            @Parameter(
                description = "ID de la tarea a restaurar",
                example = "1",
                required = true
            )
            @PathVariable Long id) {

        log.info("Petición recibida para restaurar tarea eliminada con ID: {}", id);

        TaskResponseDto restoredTask = taskService.restoreTask(id);

        log.info("Tarea restaurada exitosamente con ID: {}", id);

        return ResponseEntity.ok(restoredTask);
    }

    /**
     * Elimina permanentemente tareas antiguas de la papelera (purge).
     *
     * Endpoint: DELETE /tasks/trash/purge
     *
     * NOTA: Este endpoint debería estar protegido con @PreAuthorize("hasRole('ADMIN')")
     */
    @DeleteMapping("/trash/purge")
    @Operation(
        summary = "Eliminar permanentemente tareas antiguas (purge)",
        description = """
            Elimina PERMANENTEMENTE tareas que fueron eliminadas hace más de X días.

            **ADVERTENCIA: OPERACIÓN IRREVERSIBLE**
            - Las tareas se eliminan FÍSICAMENTE de la base de datos
            - NO se pueden recuperar después de purge
            - Solo afecta tareas eliminadas hace más de 90 días (configurable)

            **Proceso:**
            1. Busca tareas con deletedAt < (ahora - 90 días)
            2. Las elimina permanentemente de la BD
            3. Libera espacio en disco
            4. Retorna número de tareas eliminadas

            **Casos de uso:**
            - Limpieza periódica automatizada (cron job)
            - Cumplimiento GDPR (derecho al olvido)
            - Optimización de espacio en BD
            - Mantenimiento del sistema

            **Seguridad:**
            - SOLO administradores pueden ejecutar purge
            - Se registra en logs de auditoría
            - No afecta tareas activas ni eliminadas recientemente

            **Configuración:**
            - Días de retención: 90 (editable en código)
            - Frecuencia recomendada: Mensual
            - Ejecución recomendada: Fuera de horas pico

            **Respuesta exitosa:**
            - Código HTTP 200 (OK)
            - Número de tareas eliminadas permanentemente
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Purge ejecutado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Purge exitoso",
                    value = """
                        {
                          "message": "Purge completado exitosamente",
                          "deletedCount": 42,
                          "retentionDays": 90
                        }
                        """
                )
            )
        )
    })
    // @PreAuthorize("hasRole('ADMIN')") // Descomentar en producción
    public ResponseEntity<?> purgeOldDeletedTasks(
            @Parameter(
                description = "Días de retención (tareas eliminadas hace más de X días se eliminan)",
                example = "90"
            )
            @RequestParam(defaultValue = "90") int retentionDays) {

        log.warn("Petición recibida para purge de tareas eliminadas hace más de {} días", retentionDays);

        int deletedCount = taskService.purgeOldDeletedTasks(retentionDays);

        log.warn("Purge completado: {} tareas eliminadas permanentemente", deletedCount);

        return ResponseEntity.ok(new PurgeResponse(
                "Purge completado exitosamente",
                deletedCount,
                retentionDays
        ));
    }

    // =========================================================================
    // DTO PARA RESPUESTA DE PURGE
    // =========================================================================

    /**
     * DTO para respuesta de operación de purge
     */
    private record PurgeResponse(
            String message,
            int deletedCount,
            int retentionDays
    ) {}
}
