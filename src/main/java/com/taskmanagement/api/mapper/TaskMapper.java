package com.taskmanagement.api.mapper;

import com.taskmanagement.api.dto.TaskPatchDto;
import com.taskmanagement.api.dto.TaskRequestDto;
import com.taskmanagement.api.dto.TaskResponseDto;
import com.taskmanagement.api.model.Task;
import com.taskmanagement.api.model.User;
import org.mapstruct.*;

/**
 * Mapper para conversiones entre entidades Task y DTOs usando MapStruct.
 *
 * ¿QUÉ ES MAPSTRUCT?
 * MapStruct es un generador de código que crea automáticamente implementaciones
 * de mappers en TIEMPO DE COMPILACIÓN. Esto lo hace:
 * - ✅ MUY EFICIENTE: Sin reflexión en runtime (a diferencia de ModelMapper)
 * - ✅ TYPE-SAFE: Errores detectados en compilación, no en runtime
 * - ✅ RÁPIDO: Código generado tan rápido como código manual
 * - ✅ MENOS BOILERPLATE: No necesitas escribir getters/setters manualmente
 * - ✅ MANTENIBLE: Si cambias un DTO, el compilador te avisa
 *
 * VENTAJAS VS MAPPER MANUAL:
 * - Reduce código boilerplate en ~70%
 * - Actualizaciones automáticas si cambian los DTOs
 * - Errores detectados en compilación
 * - Integración perfecta con Lombok
 * - Performance equivalente al código manual
 *
 * CÓMO FUNCIONA:
 * 1. Defines una INTERFAZ con métodos de mapeo
 * 2. MapStruct GENERA la implementación durante la compilación
 * 3. Spring inyecta el mapper como un bean (@Mapper(componentModel = "spring"))
 *
 * CONFIGURACIÓN:
 * @Mapper: Indica que es un mapper de MapStruct
 * - componentModel = "spring": Genera un @Component para inyección de dependencias
 * - unmappedTargetPolicy = IGNORE: Ignora campos no mapeados (evita warnings)
 * - nullValuePropertyMappingStrategy = IGNORE: No sobrescribe con null en PATCH
 *
 * MAPEOS AUTOMÁTICOS:
 * MapStruct mapea automáticamente campos con el MISMO NOMBRE.
 * Ejemplo: task.title → dto.title (automático)
 *
 * MAPEOS PERSONALIZADOS:
 * Usa @Mapping para mapeos custom:
 * @Mapping(target = "userId", source = "user.id")
 *
 * CÓDIGO GENERADO:
 * Durante la compilación, MapStruct genera TaskMapperImpl.java en:
 * build/generated/sources/annotationProcessor/java/main/
 *
 * Ejemplo del código generado:
 * ```java
 * @Component
 * public class TaskMapperImpl implements TaskMapper {
 *     public TaskResponseDto toResponseDto(Task task) {
 *         if (task == null) return null;
 *         TaskResponseDto dto = new TaskResponseDto();
 *         dto.setId(task.getId());
 *         dto.setTitle(task.getTitle());
 *         // ... resto de campos
 *         return dto;
 *     }
 * }
 * ```
 *
 * USO EN SERVICIOS:
 * ```java
 * @Service
 * @RequiredArgsConstructor
 * public class TaskService {
 *     private final TaskMapper taskMapper;  // Inyección automática
 *
 *     public TaskResponseDto createTask(TaskRequestDto dto) {
 *         Task task = taskMapper.toEntity(dto);
 *         Task saved = taskRepository.save(task);
 *         return taskMapper.toResponseDto(saved);
 *     }
 * }
 * ```
 *
 * DEBUGGING:
 * Para ver el código generado por MapStruct:
 * ./gradlew clean compileJava
 * Luego revisar: build/generated/sources/annotationProcessor/
 *
 * INTEGRACIÓN CON LOMBOK:
 * MapStruct funciona perfectamente con Lombok gracias a lombok-mapstruct-binding
 * que incluimos en build.gradle. MapStruct puede ver los getters/setters
 * generados por Lombok durante la compilación.
 */
@Mapper(
        componentModel = "spring",  // Genera @Component para Spring DI
        unmappedTargetPolicy = ReportingPolicy.IGNORE,  // No advertir por campos no mapeados
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE  // Ignorar nulls en PATCH
)
public interface TaskMapper {

    /**
     * Convierte un TaskRequestDto a una entidad Task.
     *
     * USADO EN: Creación de tareas (POST)
     *
     * MAPEOS AUTOMÁTICOS (mismo nombre):
     * - title → title
     * - description → description
     * - status → status
     * - priority → priority
     * - dueDate → dueDate
     *
     * CAMPOS IGNORADOS (se establecen después):
     * - id: Generado por la base de datos
     * - user: Establecido en el servicio (desde contexto de seguridad)
     * - createdAt/updatedAt: Gestionados por @PrePersist/@PreUpdate
     *
     * @param dto DTO con los datos de entrada
     * @return entidad Task nueva (sin ID, user, timestamps)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Task toEntity(TaskRequestDto dto);

    /**
     * Convierte una entidad Task a TaskResponseDto.
     *
     * USADO EN: Consultas y respuestas (GET, POST, PUT, PATCH)
     *
     * MAPEOS AUTOMÁTICOS:
     * - id → id
     * - title → title
     * - description → description
     * - status → status
     * - priority → priority
     * - dueDate → dueDate
     * - createdAt → createdAt
     * - updatedAt → updatedAt
     * - deletedAt → deletedAt
     *
     * MAPEOS PERSONALIZADOS:
     * - user.id → userId
     * - user.username → username
     *
     * SEGURIDAD: Solo mapeamos userId y username, NO datos sensibles.
     *
     * @param task entidad Task
     * @return DTO de respuesta con todos los campos
     */
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "deletedAt", source = "deletedAt")
    TaskResponseDto toResponseDto(Task task);

    /**
     * Actualiza una entidad Task existente con datos de TaskRequestDto.
     *
     * USADO EN: Actualización completa (PUT)
     *
     * @MappingTarget: Indica que task es el objeto a actualizar (no crear uno nuevo)
     *
     * ACTUALIZA:
     * - title
     * - description
     * - status
     * - priority
     * - dueDate
     *
     * NO ACTUALIZA (ignorados):
     * - id: No se puede cambiar
     * - user: No se puede cambiar el propietario
     * - createdAt: No se puede cambiar
     * - updatedAt: Se actualiza automáticamente
     *
     * IMPORTANTE: Modifica la entidad recibida (mutable operation)
     *
     * @param task entidad Task existente a actualizar
     * @param dto DTO con los nuevos datos
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntityFromDto(@MappingTarget Task task, TaskRequestDto dto);

    /**
     * Actualiza parcialmente una entidad Task con datos de TaskPatchDto.
     *
     * USADO EN: Actualización parcial (PATCH)
     *
     * DIFERENCIA CON updateEntityFromDto:
     * - updateEntityFromDto: Actualiza TODOS los campos (PUT)
     * - patchEntityFromDto: Solo actualiza campos NO null (PATCH)
     *
     * CONFIGURACIÓN CLAVE:
     * nullValuePropertyMappingStrategy = IGNORE en @Mapper hace que
     * MapStruct ignore campos null, permitiendo actualizaciones parciales.
     *
     * EJEMPLO:
     * Si dto solo tiene status="COMPLETED" (y resto null),
     * solo se actualiza el status. Los demás campos permanecen sin cambios.
     *
     * IMPORTANTE: Modifica la entidad recibida (mutable operation)
     *
     * @param task entidad Task existente a actualizar
     * @param dto DTO con los campos a actualizar (solo los no-null)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void patchEntityFromDto(@MappingTarget Task task, TaskPatchDto dto);

    // =========================================================================
    // MÉTODOS DEFAULT CON LÓGICA PERSONALIZADA
    // =========================================================================

    /**
     * Verifica si un TaskPatchDto tiene algún campo para actualizar.
     *
     * USADO EN: Validación antes de hacer PATCH
     *
     * Útil para evitar actualizaciones vacías.
     *
     * NOTA: Este es un método default porque requiere lógica custom
     * que MapStruct no puede generar automáticamente.
     *
     * @param dto DTO a verificar
     * @return true si al menos un campo no es null
     */
    default boolean hasAnyFieldToUpdate(TaskPatchDto dto) {
        if (dto == null) {
            return false;
        }

        return dto.getTitle() != null
                || dto.getDescription() != null
                || dto.getStatus() != null
                || dto.getPriority() != null
                || dto.getDueDate() != null;
    }

    /**
     * Mapea manualmente un User a su ID para casos especiales.
     *
     * USADO EN: Casos donde necesitas solo el ID del usuario
     *
     * @AfterMapping: Se ejecuta después del mapeo principal
     *
     * NOTA: Este método es opcional y se puede usar cuando necesites
     * lógica adicional después del mapeo automático.
     *
     * @param user usuario
     * @return ID del usuario o null
     */
    default Long userToUserId(User user) {
        return user != null ? user.getId() : null;
    }

    /**
     * Mapea manualmente un User a su username para casos especiales.
     *
     * @param user usuario
     * @return username o null
     */
    default String userToUsername(User user) {
        return user != null ? user.getUsername() : null;
    }
}
