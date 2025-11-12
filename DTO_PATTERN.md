# 📋 Patrón DTO y Mappers - Documentación Completa

## ✅ Implementación completada

Este proyecto ahora implementa el patrón DTO (Data Transfer Object) con Mappers centralizados, siguiendo las mejores prácticas de arquitectura de software.

---

## 📚 ¿Qué es el Patrón DTO?

El patrón **Data Transfer Object (DTO)** es un objeto que transporta datos entre procesos, capas de aplicación o sistemas externos.

### 🎯 Propósito Principal
Separar la **representación externa (API)** de la **estructura interna (Entidades JPA)**.

---

## 🏗️ Arquitectura Implementada

```
┌─────────────────┐
│   Controller    │  ← Recibe/Devuelve DTOs
└────────┬────────┘
         │
         ├── TaskRequestDto    (entrada: POST/PUT)
         ├── TaskPatchDto      (entrada: PATCH)
         └── TaskResponseDto   (salida: GET/POST/PUT/PATCH)
         │
         ▼
┌─────────────────┐
│   TaskMapper    │  ← Convierte DTOs ↔ Entidades
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│     Service     │  ← Trabaja con Entidades
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Repository    │  ← Persiste Entidades
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   Base de Datos │
└─────────────────┘
```

---

## 📦 DTOs Implementados

### 1. **TaskRequestDto** - DTO de Entrada (POST/PUT)

```java
@Schema(description = "DTO para crear o actualizar una tarea")
public class TaskRequestDto {
    @NotBlank
    @Size(min = 1, max = 100)
    @Schema(description = "Título de la tarea", required = true)
    private String title;

    @Size(max = 1000)
    @Schema(description = "Descripción", nullable = true)
    private String description;

    @NotNull
    @Schema(description = "Estado", required = true)
    private TaskStatus status;

    @Schema(description = "Fecha límite", nullable = true)
    private LocalDateTime dueDate;
}
```

**Características:**
- ✅ Validaciones con Bean Validation (`@NotBlank`, `@Size`, `@NotNull`)
- ✅ Documentación con `@Schema` para OpenAPI
- ✅ Solo campos necesarios para crear/actualizar
- ✅ No incluye ID ni campos de auditoría (readonly)

**Usado en:**
- `POST /api/v1/tasks` - Crear tarea
- `PUT /api/v1/tasks/{id}` - Actualizar tarea completa

---

### 2. **TaskPatchDto** - DTO de Entrada (PATCH)

```java
public class TaskPatchDto {
    // Todos los campos son opcionales (pueden ser null)
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime dueDate;
}
```

**Características:**
- ✅ Todos los campos opcionales
- ✅ Solo campos no-null se actualizan
- ✅ Ideal para actualizaciones parciales

**Usado en:**
- `PATCH /api/v1/tasks/{id}` - Actualizar campos específicos

---

### 3. **TaskResponseDto** - DTO de Salida

```java
@Schema(description = "Respuesta con datos completos de una tarea")
public class TaskResponseDto {
    @Schema(description = "ID único")
    private Long id;

    private String title;
    private String description;
    private TaskStatus status;
    private LocalDateTime dueDate;

    @Schema(accessMode = READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(accessMode = READ_ONLY)
    private LocalDateTime updatedAt;
}
```

**Características:**
- ✅ Incluye todos los campos (incluso readonly)
- ✅ ID y timestamps de auditoría
- ✅ Documentado con `@Schema`
- ✅ Representa exactamente lo que el cliente recibe

**Usado en:**
- Respuestas de `GET`, `POST`, `PUT`, `PATCH`

---

### 4. **ErrorResponseDto** - DTO de Errores (Record Java 21)

```java
@Schema(description = "Respuesta de error estandarizada")
public record ErrorResponseDto(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<String> errors  // Para errores de validación
) {
    // Factory methods
    public static ErrorResponseDto of(...) { ... }
    public static ErrorResponseDto withValidationErrors(...) { ... }
}
```

**Características:**
- ✅ Inmutable (Record de Java 21)
- ✅ Thread-safe
- ✅ Factory methods para creación fluida
- ✅ Formato consistente para todos los errores

**Usado en:**
- `GlobalExceptionHandler` para todos los errores HTTP

---

## 🔧 TaskMapper - Conversiones Centralizadas

### Implementación

```java
public final class TaskMapper {

    // Constructor privado - clase utilitaria
    private TaskMapper() {
        throw new AssertionError("No se puede instanciar");
    }

    // DTO → Entidad (para crear)
    public static Task toEntity(TaskRequestDto dto) { ... }

    // Entidad → DTO (para responder)
    public static TaskResponseDto toResponseDto(Task task) { ... }

    // Actualización completa (PUT)
    public static Task updateEntityFromDto(Task task, TaskRequestDto dto) { ... }

    // Actualización parcial (PATCH)
    public static Task patchEntityFromDto(Task task, TaskPatchDto dto) { ... }
}
```

### Uso en el Servicio

**ANTES** (código duplicado):
```java
// En cada método del servicio:
private Task mapToEntity(TaskRequestDto dto) {
    Task task = new Task();
    task.setTitle(dto.getTitle());
    // ... más código duplicado
    return task;
}

private TaskResponseDto mapToResponseDto(Task task) {
    TaskResponseDto dto = new TaskResponseDto();
    dto.setId(task.getId());
    // ... más código duplicado
    return dto;
}
```

**AHORA** (centralizado):
```java
// En el servicio, una sola línea:
Task task = TaskMapper.toEntity(taskRequestDto);
TaskResponseDto response = TaskMapper.toResponseDto(savedTask);
```

### Ventajas del Mapper Centralizado

1. ✅ **DRY (Don't Repeat Yourself)**
   - Un solo lugar para definir conversiones
   - Cambios de mapeo en un solo lugar

2. ✅ **Testeable**
   - Fácil hacer tests unitarios del mapper
   - Independiente del servicio

3. ✅ **Reutilizable**
   - Otros servicios pueden usar el mismo mapper
   - Consistencia en toda la aplicación

4. ✅ **Mantenible**
   - Si cambia un DTO, solo actualizas el mapper
   - Reduce bugs y código duplicado

---

## 🎯 Ventajas del Patrón DTO

### 1. DESACOPLAMIENTO 🔌
```
┌──────────────────┐       ┌──────────────────┐
│   Cliente API    │       │  Entidad JPA     │
│  (conoce DTOs)   │   X   │  (estructura BD) │
└──────────────────┘       └──────────────────┘
         │                          │
         └────────── DTO ───────────┘
              (intermediario)
```

**Beneficio:** Puedes cambiar tu modelo de datos sin romper la API.

**Ejemplo:**
```java
// Puedes cambiar la entidad Task:
@Entity
class Task {
    private String taskTitle;  // Era "title"
    // ...
}

// Sin afectar la API (el DTO sigue igual):
class TaskResponseDto {
    private String title;  // No cambia
    // ...
}

// El mapper se encarga de la conversión
```

---

### 2. SEGURIDAD 🔒

**Problema sin DTOs:**
```java
// ❌ Exponer entidad directamente
@GetMapping("/{id}")
public Task getTask(@PathVariable Long id) {
    return taskRepository.findById(id);
    // Expone TODO: password, tokens, relaciones lazy, etc.
}
```

**Solución con DTOs:**
```java
// ✅ Solo expones lo necesario
@GetMapping("/{id}")
public TaskResponseDto getTask(@PathVariable Long id) {
    Task task = taskRepository.findById(id);
    return TaskMapper.toResponseDto(task);
    // Solo incluye campos específicos del DTO
}
```

**Prevención de Mass Assignment:**
```java
// Sin DTOs - vulnerable:
@PostMapping
public Task create(@RequestBody Task task) {
    // ❌ Cliente puede enviar cualquier campo: id, createdAt, etc.
    return taskRepository.save(task);
}

// Con DTOs - seguro:
@PostMapping
public TaskResponseDto create(@RequestBody TaskRequestDto dto) {
    // ✅ Solo acepta campos definidos en TaskRequestDto
    Task task = TaskMapper.toEntity(dto);
    return TaskMapper.toResponseDto(taskRepository.save(task));
}
```

---

### 3. VALIDACIÓN ✅

```java
// DTOs con Bean Validation
public class TaskRequestDto {
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 1, max = 100)
    private String title;

    @NotNull
    private TaskStatus status;
}

// En el controlador:
@PostMapping
public TaskResponseDto create(@Valid @RequestBody TaskRequestDto dto) {
    // Si la validación falla, se lanza MethodArgumentNotValidException
    // antes de llegar al servicio
}
```

**Ventajas:**
- ✅ Validación ANTES de la lógica de negocio
- ✅ Mensajes de error personalizados
- ✅ Documentación clara de restricciones

---

### 4. VERSIONADO 🔄

```java
// API v1
public class TaskResponseDtoV1 {
    private Long id;
    private String title;
    private String status;  // String simple
}

// API v2 (nueva versión)
public class TaskResponseDtoV2 {
    private Long id;
    private String title;
    private TaskStatus status;  // Enum complejo
    private LocalDateTime dueDate;  // Campo nuevo
}

// Misma entidad Task, diferentes representaciones:
@GetMapping("/v1/tasks/{id}")
public TaskResponseDtoV1 getTaskV1(@PathVariable Long id) { ... }

@GetMapping("/v2/tasks/{id}")
public TaskResponseDtoV2 getTaskV2(@PathVariable Long id) { ... }
```

---

### 5. OPTIMIZACIÓN ⚡

```java
// Sin DTOs - carga innecesaria:
@Entity
class Task {
    @ManyToOne(fetch = LAZY)
    private User assignedTo;

    @OneToMany(fetch = LAZY)
    private List<Comment> comments;  // 100+ comentarios
}

// Con DTOs - solo lo necesario:
public class TaskSummaryDto {
    private Long id;
    private String title;
    private String status;
    // No incluye comments ni assignedTo
    // → No se cargan de la BD
}
```

**Beneficios:**
- ✅ Menos datos transferidos por red
- ✅ Evita N+1 queries
- ✅ Respuestas más rápidas

---

### 6. DOCUMENTACIÓN 📖

```java
@Schema(
    description = "DTO para crear una tarea",
    example = """
        {
          "title": "Comprar pan",
          "status": "PENDING"
        }
        """
)
public class TaskRequestDto {

    @Schema(
        description = "Título de la tarea",
        example = "Comprar pan",
        requiredMode = REQUIRED,
        minLength = 1,
        maxLength = 100
    )
    private String title;
}
```

**Genera Swagger UI automático:**
- ✅ Clientes saben exactamente qué enviar
- ✅ Ejemplos claros
- ✅ Validaciones documentadas
- ✅ Tipos y formatos específicos

---

## 🔄 PUT vs PATCH - Diferencias Clave

### PUT - Actualización Completa

```bash
PUT /api/v1/tasks/1
{
  "title": "Nuevo título",
  "description": "Nueva descripción",
  "status": "IN_PROGRESS",
  "dueDate": "2025-11-15T18:00:00"
}
```

- ✅ Todos los campos son obligatorios
- ✅ Reemplaza completamente el recurso
- ✅ Usa `TaskRequestDto` (con validaciones)
- ✅ Campos no enviados se pueden perder

### PATCH - Actualización Parcial

```bash
PATCH /api/v1/tasks/1
{
  "status": "COMPLETED"
}
```

- ✅ Solo envías los campos a cambiar
- ✅ Campos no enviados permanecen sin cambios
- ✅ Usa `TaskPatchDto` (sin validaciones obligatorias)
- ✅ Ideal para cambios pequeños

---

## 🆚 Alternativas al Mapper Manual

### 1. MapStruct (Recomendado para proyectos grandes)

```java
@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskResponseDto toResponseDto(Task task);

    Task toEntity(TaskRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateTaskFromDto(@MappingTarget Task task, TaskRequestDto dto);
}
```

**Ventajas:**
- ✅ Generación de código en compilación (sin reflexión)
- ✅ Muy eficiente
- ✅ Reduce código boilerplate
- ✅ Type-safe

**Desventajas:**
- ❌ Dependencia externa
- ❌ Curva de aprendizaje
- ❌ Overkill para proyectos pequeños

**Para implementar:**
```gradle
dependencies {
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
}
```

---

### 2. ModelMapper (No recomendado)

```java
ModelMapper modelMapper = new ModelMapper();
TaskResponseDto dto = modelMapper.map(task, TaskResponseDto.class);
```

**Ventajas:**
- ✅ Configuración mínima
- ✅ Mapeo automático por nombres

**Desventajas:**
- ❌ Usa reflexión (lento)
- ❌ Errores en runtime en lugar de compilación
- ❌ Difícil de debuggear
- ❌ Configuración compleja para casos especiales

---

## 📊 Comparación de Enfoques

| Aspecto | Mapper Manual | MapStruct | ModelMapper |
|---------|---------------|-----------|-------------|
| **Rendimiento** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| **Mantenibilidad** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| **Facilidad** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Type-safe** | ✅ | ✅ | ❌ |
| **Reflexión** | ❌ | ❌ | ✅ |
| **Dependencias** | 0 | 1 | 1 |
| **Ideal para** | Pequeños | Grandes | Prototipos |

**Nuestra elección:** Mapper Manual
- Proyecto pequeño/mediano
- Control total
- Sin dependencias
- Fácil migración a MapStruct si crece

---

## 📁 Estructura de Archivos

```
src/main/java/com/taskmanagement/api/
├── dto/
│   ├── TaskRequestDto.java      ← DTO entrada (POST/PUT)
│   ├── TaskPatchDto.java        ← DTO entrada (PATCH)
│   ├── TaskResponseDto.java     ← DTO salida
│   └── ErrorResponseDto.java    ← DTO errores (Record)
│
├── mapper/
│   └── TaskMapper.java          ← Conversiones centralizadas
│
├── model/
│   └── Task.java                ← Entidad JPA
│
├── service/
│   └── impl/
│       └── TaskServiceImpl.java ← Usa TaskMapper
│
└── controller/
    └── TaskController.java      ← Recibe/devuelve DTOs
```

---

## ✅ Checklist de Implementación

- [x] Crear TaskRequestDto con validaciones
- [x] Crear TaskPatchDto sin validaciones obligatorias
- [x] Crear TaskResponseDto con todos los campos
- [x] Crear ErrorResponseDto como Record
- [x] Agregar @Schema annotations a todos los DTOs
- [x] Crear TaskMapper con métodos estáticos
- [x] Actualizar TaskServiceImpl para usar TaskMapper
- [x] Eliminar métodos de mapeo duplicados
- [x] Agregar dependencia springdoc-openapi
- [x] Probar todos los endpoints

---

## 🚀 Próximos Pasos (Opcionales)

1. **Habilitar Swagger UI completamente**
   - Configurar OpenAPI con context-path
   - Acceder a http://localhost:8080/swagger-ui.html

2. **Migrar a MapStruct** (si el proyecto crece)
   - Agregar dependencia
   - Convertir TaskMapper a interfaz
   - Dejar que MapStruct genere el código

3. **Crear DTOs adicionales**
   - `TaskSummaryDto` - Solo ID, title, status
   - `TaskDetailDto` - Con relaciones (comments, assignee)
   - `TaskStatisticsDto` - Datos agregados

4. **Versionado de API**
   - `/api/v1/tasks` - Versión actual
   - `/api/v2/tasks` - Con DTOs mejorados

---

## 💡 Mejores Prácticas

1. ✅ **Separar DTOs de entrada y salida**
   - Entrada: Validación estricta
   - Salida: Todos los campos

2. ✅ **Usar Records para DTOs inmutables**
   - ErrorResponseDto (solo lectura)
   - En futuro: TaskResponseDto si no usas MapStruct

3. ✅ **Documentar con @Schema**
   - Genera documentación automática
   - Ejemplos claros para clientes

4. ✅ **Validar en DTOs, no en entidades**
   - Entidades = modelo de dominio
   - DTOs = contrato de API

5. ✅ **Centralizar mapeo en Mapper**
   - No duplicar código de conversión
   - Fácil de mantener y testear

6. ✅ **Nunca exponer entidades directamente**
   - Siempre usar DTOs en controllers
   - Seguridad y desacoplamiento

---

## 🎓 Recursos Adicionales

- [Martin Fowler - DTO Pattern](https://martinfowler.com/eaaCatalog/dataTransferObject.html)
- [Spring Data JPA Best Practices](https://spring.io/guides/gs/accessing-data-jpa/)
- [MapStruct Documentation](https://mapstruct.org/)
- [Bean Validation Specification](https://beanvalidation.org/)
- [OpenAPI/Swagger](https://swagger.io/specification/)

---

¡Patrón DTO implementado con éxito! 🎉
