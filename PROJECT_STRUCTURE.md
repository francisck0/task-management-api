# Estructura del Proyecto - Task Management API

## 📋 Índice
- [Árbol de Directorios](#árbol-de-directorios)
- [Backend Structure](#backend-structure)
- [Frontend Structure](#frontend-structure)
- [Desglose por Capa](#desglose-por-capa)
- [Estadísticas del Proyecto](#estadísticas-del-proyecto)

---

## 🌳 Árbol de Directorios

```
task-project/
│
├── .github/                              # GitHub Actions workflows
│   ├── workflows/
│   │   ├── ci.yml                        # Pipeline de CI (tests, build)
│   │   ├── docker-build.yml              # Build y push de Docker images
│   │   └── deploy.yml                    # Deployment automático
│   └── dependabot.yml                    # Actualización automática de dependencias
│
├── docs/                                 # Documentación técnica avanzada
│   ├── AUDITORIA_AOP.md                 # Auditoría automática con AOP
│   ├── CI_CD.md                         # Pipeline de CI/CD completo
│   ├── JWT_SECURITY.md                  # Seguridad JWT y refresh tokens
│   └── DATABASE_INDEXES.md              # Índices de BD optimizados
│
├── frontend/                             # Frontend Angular 19
│   ├── src/
│   │   ├── app/
│   │   │   ├── auth/                    # Módulo de autenticación
│   │   │   │   ├── login/
│   │   │   │   └── register/
│   │   │   ├── tasks/                   # Módulo de tareas
│   │   │   │   ├── task-list/
│   │   │   │   ├── task-form/
│   │   │   │   └── task-detail/
│   │   │   ├── dashboard/               # Dashboard con estadísticas
│   │   │   ├── admin/                   # Panel administrativo
│   │   │   │   └── audit-logs/
│   │   │   ├── trash/                   # Papelera de reciclaje
│   │   │   ├── core/                    # Servicios core
│   │   │   │   ├── services/
│   │   │   │   ├── guards/
│   │   │   │   ├── interceptors/
│   │   │   │   └── models/
│   │   │   └── shared/                  # Componentes compartidos
│   │   ├── assets/
│   │   ├── environments/
│   │   └── styles/
│   ├── angular.json
│   ├── package.json
│   └── tsconfig.json
│
├── gradle/                               # Gradle Wrapper
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── scripts/                              # Scripts de automatización
│   ├── postgres-utils.sql               # Queries útiles de PostgreSQL
│   ├── verify-indexes.sql               # Verificación de índices
│   ├── load-env.sh                      # Cargar variables de entorno
│   └── generate-secrets.sh              # Generar secretos seguros
│
├── src/
│   ├── main/
│   │   ├── java/com/taskmanagement/api/
│   │   │   │
│   │   │   ├── TaskManagementApiApplication.java    # Clase principal de Spring Boot
│   │   │   │
│   │   │   ├── controller/                          # Capa de Presentación (7 controladores)
│   │   │   │   ├── TaskCommandController.java       # CQRS Write (POST, PUT, PATCH, DELETE)
│   │   │   │   ├── TaskQueryController.java         # CQRS Read (GET con filtrado)
│   │   │   │   ├── TaskStatisticsController.java    # Estadísticas (cacheadas)
│   │   │   │   ├── TaskTrashController.java         # Papelera de reciclaje
│   │   │   │   ├── AuthController.java              # Login, register, refresh tokens
│   │   │   │   ├── AuditLogController.java          # Logs de auditoría
│   │   │   │   └── RateLimitAdminController.java    # Gestión de rate limiting
│   │   │   │
│   │   │   ├── service/                             # Capa de Negocio (6 servicios)
│   │   │   │   ├── TaskService.java                 # Interfaz del servicio
│   │   │   │   ├── AuthService.java                 # Autenticación y JWT
│   │   │   │   ├── RefreshTokenService.java         # Gestión de refresh tokens
│   │   │   │   ├── AuditLogService.java             # Servicio de auditoría
│   │   │   │   ├── JwtService.java                  # Generación/validación JWT
│   │   │   │   ├── RateLimitService.java            # Rate limiting con Token Bucket
│   │   │   │   └── impl/
│   │   │   │       ├── TaskServiceImpl.java         # Implementación del servicio
│   │   │   │       ├── RefreshTokenServiceImpl.java
│   │   │   │       └── AuditLogServiceImpl.java
│   │   │   │
│   │   │   ├── repository/                          # Capa de Persistencia (5 repositorios)
│   │   │   │   ├── TaskRepository.java              # Repositorio JPA de tareas
│   │   │   │   ├── UserRepository.java              # Gestión de usuarios
│   │   │   │   ├── RoleRepository.java              # Gestión de roles
│   │   │   │   ├── RefreshTokenRepository.java      # Tokens de refresco
│   │   │   │   └── AuditLogRepository.java          # Logs de auditoría
│   │   │   │
│   │   │   ├── model/                               # Capa de Dominio (8 entidades)
│   │   │   │   ├── Auditable.java                   # Clase base con audit fields
│   │   │   │   ├── Task.java                        # Entidad Task (con 8 índices)
│   │   │   │   ├── TaskStatus.java                  # Enum (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
│   │   │   │   ├── TaskPriority.java                # Enum (LOW, MEDIUM, HIGH, CRITICAL)
│   │   │   │   ├── User.java                        # Entidad User
│   │   │   │   ├── Role.java                        # Entidad Role (ADMIN, USER)
│   │   │   │   ├── RefreshToken.java                # Tokens de refresco
│   │   │   │   └── AuditLog.java                    # Logs de auditoría (con 5 índices)
│   │   │   │
│   │   │   ├── dto/                                 # Data Transfer Objects (4 DTOs)
│   │   │   │   ├── TaskRequestDto.java              # DTO para crear/actualizar tareas
│   │   │   │   ├── TaskResponseDto.java             # DTO para respuestas de tareas
│   │   │   │   ├── TaskPatchDto.java                # DTO para actualizaciones parciales
│   │   │   │   ├── TaskFilterDto.java               # DTO para filtrado avanzado
│   │   │   │   ├── AuthResponse.java                # DTO de respuesta de autenticación
│   │   │   │   ├── RefreshTokenRequest.java         # DTO para refresh token
│   │   │   │   └── RefreshTokenResponse.java        # DTO de respuesta de refresh
│   │   │   │
│   │   │   ├── mapper/                              # Mappers (1 mapper)
│   │   │   │   └── TaskMapper.java                  # Conversiones DTO ↔ Entity
│   │   │   │
│   │   │   ├── exception/                           # Manejo de Excepciones (8 clases)
│   │   │   │   ├── ResourceNotFoundException.java   # Excepción 404
│   │   │   │   ├── ErrorResponse.java               # DTO para respuestas de error
│   │   │   │   ├── GlobalExceptionHandler.java      # Manejador global (11 exception handlers)
│   │   │   │   ├── DuplicateEmailException.java     # Email duplicado
│   │   │   │   ├── DuplicateUsernameException.java  # Username duplicado
│   │   │   │   ├── ForbiddenException.java          # Acceso denegado
│   │   │   │   ├── InvalidCredentialsException.java # Credenciales inválidas
│   │   │   │   └── RoleNotFoundException.java       # Rol no encontrado
│   │   │   │
│   │   │   ├── config/                              # Configuraciones (8 clases)
│   │   │   │   ├── CorsConfig.java                  # Configuración de CORS
│   │   │   │   ├── OpenApiConfig.java               # Configuración de Swagger
│   │   │   │   ├── SecurityConfig.java              # Spring Security con JWT
│   │   │   │   ├── RedisCacheConfig.java            # Configuración de Redis (3 cachés)
│   │   │   │   ├── AuditorAwareImpl.java            # Captura usuario para JPA Auditing
│   │   │   │   ├── JwtSecretValidator.java          # Validación de JWT secret
│   │   │   │   ├── UserDetailsServiceConfig.java    # UserDetailsService
│   │   │   │   └── RateLimitProperties.java         # Configuración de rate limiting
│   │   │   │
│   │   │   ├── security/                            # Componentes de Seguridad (1 filtro)
│   │   │   │   └── JwtAuthenticationFilter.java     # Filtro JWT
│   │   │   │
│   │   │   ├── filter/                              # Filtros HTTP (2 filtros)
│   │   │   │   ├── CorrelationIdFilter.java         # Correlation IDs para tracing
│   │   │   │   └── RateLimitFilter.java             # Rate limiting
│   │   │   │
│   │   │   ├── aspect/                              # AOP Aspects (4 clases)
│   │   │   │   ├── AuditAspect.java                 # Auditoría automática con @Auditable
│   │   │   │   ├── LoggingAspect.java               # Logging automático
│   │   │   │   ├── PerformanceAspect.java           # Medición de performance
│   │   │   │   └── Auditable.java                   # Anotación para auditoría
│   │   │   │
│   │   │   ├── specification/                       # JPA Specifications (1 clase)
│   │   │   │   └── TaskSpecification.java           # Filtrado dinámico de tareas
│   │   │   │
│   │   │   └── constant/                            # Constantes (2 clases)
│   │   │       ├── ApiVersion.java                  # Versión de API (/api/v1)
│   │   │       └── CorrelationIdConstants.java      # Constantes de correlation IDs
│   │   │
│   │   └── resources/
│   │       ├── application.yml                      # Configuración principal
│   │       ├── application-dev.yml                  # Perfil de desarrollo
│   │       ├── application-test.yml                 # Perfil de testing
│   │       ├── application-prod.yml                 # Perfil de producción
│   │       ├── data.sql                             # Datos iniciales (usuarios de prueba)
│   │       ├── schema.sql                           # Schema SQL (opcional)
│   │       └── logback-spring.xml                   # Configuración de logging
│   │
│   └── test/
│       └── java/com/taskmanagement/api/
│           ├── TaskManagementApiApplicationTests.java  # Test básico
│           ├── controller/
│           │   └── TaskControllerIntegrationTest.java  # Tests de integración (22 tests)
│           └── service/
│               └── TaskServiceImplTest.java            # Tests unitarios (28 tests)
│
├── .dockerignore                         # Archivos ignorados por Docker
├── .env.example                          # Ejemplo de variables de entorno
├── .gitignore                            # Archivos ignorados por Git
├── build.gradle                          # Configuración de Gradle y dependencias
├── docker-compose.yml                    # Configuración multi-contenedor (PostgreSQL + Redis + App + pgAdmin)
├── Dockerfile                            # Dockerfile multi-stage optimizado
├── gradlew                               # Script de Gradle Wrapper (Unix/Linux/Mac)
├── gradlew.bat                           # Script de Gradle Wrapper (Windows)
├── Makefile                              # 50+ comandos de automatización
├── settings.gradle                       # Configuración del proyecto Gradle
│
├── README.md                             # Documentación principal del proyecto
├── QUICKSTART.md                         # Guía de inicio rápido
├── PROJECT_STRUCTURE.md                  # Este archivo
├── POSTGRESQL_SETUP.md                   # Configuración completa de PostgreSQL
├── CONFIGURACION_COMPLETADA.md           # Configuración inicial completada
├── AUDITING.md                           # Sistema de auditoría con JPA
├── DTO_PATTERN.md                        # Patrón DTO y Mappers
├── QUALITY_CHECKLIST.md                  # Checklist de calidad profesional
├── RATE_LIMITING.md                      # Rate limiting con Bucket4j
└── SECRETS_MANAGEMENT.md                 # Gestión de secretos y variables de entorno
```

---

## 🖥️ Backend Structure

### Estadísticas

| Categoría | Cantidad | Descripción |
|-----------|----------|-------------|
| **Controllers** | 7 | TaskCommand, TaskQuery, TaskStatistics, TaskTrash, Auth, AuditLog, RateLimitAdmin |
| **Services** | 6 + 3 impl | Task, Auth, RefreshToken, AuditLog, Jwt, RateLimit |
| **Repositories** | 5 | Task, User, Role, RefreshToken, AuditLog |
| **Entities** | 6 | Task, User, Role, RefreshToken, AuditLog, Auditable (base) |
| **Enums** | 2 | TaskStatus, TaskPriority |
| **DTOs** | 7 | TaskRequest, TaskResponse, TaskPatch, TaskFilter, AuthResponse, RefreshTokenRequest/Response |
| **Mappers** | 1 | TaskMapper (conversiones centralizadas) |
| **Exception Handlers** | 11 | GlobalExceptionHandler con 11 tipos de excepciones |
| **Configuraciones** | 8 | Cors, OpenApi, Security, RedisCache, AuditorAware, JwtValidator, UserDetailsService, RateLimitProperties |
| **Filtros** | 3 | JwtAuthenticationFilter, CorrelationIdFilter, RateLimitFilter |
| **Aspectos AOP** | 3 + 1 anotación | AuditAspect, LoggingAspect, PerformanceAspect, @Auditable |
| **Specifications** | 1 | TaskSpecification (filtrado dinámico) |
| **Tests** | 50+ | 22 tests de integración + 28 tests unitarios |

---

## 🎨 Frontend Structure

### Componentes Principales

```
frontend/src/app/
├── auth/
│   ├── login/                    # Componente de login
│   └── register/                 # Componente de registro
│
├── tasks/
│   ├── task-list/                # Lista de tareas con paginación
│   ├── task-form/                # Formulario crear/editar tarea
│   └── task-detail/              # Detalle de tarea
│
├── dashboard/                    # Dashboard con estadísticas
│   └── dashboard.component.ts    # Componente principal
│
├── admin/
│   └── audit-logs/               # Panel administrativo - Logs de auditoría
│
├── trash/                        # Papelera de reciclaje
│   └── trash.component.ts        # Componente de papelera
│
├── core/
│   ├── services/
│   │   ├── auth.service.ts       # Servicio de autenticación
│   │   ├── task.service.ts       # Servicio de tareas
│   │   ├── audit.service.ts      # Servicio de auditoría
│   │   └── token.service.ts      # Gestión de tokens
│   ├── guards/
│   │   ├── auth.guard.ts         # Guard de autenticación
│   │   └── admin.guard.ts        # Guard de autorización (ADMIN)
│   ├── interceptors/
│   │   └── jwt.interceptor.ts    # Interceptor JWT automático
│   └── models/
│       ├── task.model.ts         # Interface de Task
│       ├── user.model.ts         # Interface de User
│       └── audit-log.model.ts    # Interface de AuditLog
│
└── shared/
    ├── components/
    │   ├── navbar/
    │   ├── footer/
    │   └── loader/
    └── pipes/
```

### Características del Frontend

- ✅ **Angular 19** con TypeScript
- ✅ **Material Design** (Angular Material 19)
- ✅ **RxJS** para programación reactiva
- ✅ **Guards** para protección de rutas
- ✅ **Interceptors** para JWT automático
- ✅ **Lazy Loading** de módulos
- ✅ **Standalone Components** (nuevo en Angular 19)

---

## 📦 Desglose por Capa

### 1. Controller Layer (7 controladores)

#### CQRS Pattern

**Command Controllers (Write Operations):**
- **TaskCommandController**: POST, PUT, PATCH, DELETE
  - `createTask()` - Crear tarea
  - `updateTask()` - Actualizar completamente
  - `patchTask()` - Actualizar parcialmente
  - `deleteTask()` - Soft delete (a papelera)

**Query Controllers (Read Operations):**
- **TaskQueryController**: GET con paginación y filtrado
  - `getAllTasks()` - Listar con paginación
  - `getTaskById()` - Obtener por ID (cacheada)
  - `getTasksByStatus()` - Filtrar por estado
  - `searchTasks()` - Buscar por título
  - `filterTasks()` - Filtrado avanzado (prioridad, fechas, texto)

**Specialized Controllers:**
- **TaskStatisticsController**: Estadísticas (cacheadas 5min)
  - `getStatistics()` - Estadísticas globales

- **TaskTrashController**: Papelera de reciclaje
  - `getDeletedTasks()` - Listar tareas eliminadas
  - `restoreTask()` - Restaurar tarea
  - `purgeOldTasks()` - Purge permanente (>90 días)

- **AuthController**: Autenticación
  - `register()` - Registro de usuario
  - `login()` - Login (JWT + refresh token)
  - `refreshToken()` - Renovar access token
  - `logout()` - Cerrar sesión

- **AuditLogController**: Auditoría (solo ADMIN)
  - `getAllAuditLogs()` - Todos los logs
  - `getAuditLogsByUser()` - Por usuario
  - `getAuditLogsByAction()` - Por acción
  - `getAuditLogsByDateRange()` - Por rango de fechas
  - `getResourceHistory()` - Historial de recurso
  - `getFailedOperations()` - Operaciones fallidas
  - `getAuditStatistics()` - Estadísticas
  - `getSuspiciousActivity()` - Detección de anomalías

- **RateLimitAdminController**: Administración de rate limiting (solo ADMIN)
  - `getRateLimitInfo()` - Configuración actual
  - `getRateLimitStats()` - Estadísticas de uso
  - `clearCache()` - Limpiar cache de buckets

---

### 2. Service Layer (6 servicios + 3 implementaciones)

| Servicio | Responsabilidades Principales |
|----------|------------------------------|
| **TaskServiceImpl** | CRUD completo, cache Redis, validaciones de ownership, soft deletes, filtrado avanzado con Specifications |
| **AuthService** | Registro, login, generación de JWT, validación de credenciales |
| **RefreshTokenServiceImpl** | Gestión de refresh tokens, rotación automática, validación, revocación, limpieza de expirados |
| **AuditLogServiceImpl** | Persistencia de logs, consultas, estadísticas, detección de actividad sospechosa |
| **JwtService** | Generación de tokens JWT, validación, extracción de claims, gestión de expiración |
| **RateLimitService** | Rate limiting con Token Bucket algorithm, gestión de buckets por IP, cache de buckets |

**Características de los Servicios:**
- ✅ `@Transactional` para gestión de transacciones
- ✅ `@Cacheable` y `@CacheEvict` para cache distribuido
- ✅ `@Auditable` para audit logging automático con AOP
- ✅ Validaciones de negocio (ownership, límites, etc.)
- ✅ Logging estructurado con SLF4J

---

### 3. Repository Layer (5 repositorios)

| Repositorio | Métodos Destacados |
|-------------|-------------------|
| **TaskRepository** | `findByUserId()`, `findByStatus()`, `findByDeletedAtIsNull()`, `findByDeletedAtIsNotNull()`, `countByUserId()`, Specifications para filtrado dinámico |
| **UserRepository** | `findByUsername()`, `findByEmail()`, `existsByUsername()`, `existsByEmail()` |
| **RoleRepository** | `findByName()` |
| **RefreshTokenRepository** | `findByToken()`, `findByUser()`, `deleteByExpiryDateBefore()` |
| **AuditLogRepository** | `findByUsername()`, `findByAction()`, `findByResource()`, `findByTimestampBetween()`, `findByStatus()`, `countByAction()` |

**Características:**
- ✅ Extienden `JpaRepository<T, ID>`
- ✅ Query methods derivados de Spring Data JPA
- ✅ `@Query` custom para consultas complejas
- ✅ Specifications para filtrado dinámico
- ✅ Paginación con `Pageable`

---

### 4. Model Layer (8 entidades + 2 enums)

#### Entidades

**Auditable (Clase base abstracta)**
- `createdAt` (LocalDateTime) - @CreatedDate
- `updatedAt` (LocalDateTime) - @LastModifiedDate
- `createdBy` (String) - @CreatedBy
- `lastModifiedBy` (String) - @LastModifiedBy

**Task**
- Hereda de `Auditable`
- 8 índices compuestos para optimización
- Campos: `id`, `title`, `description`, `status`, `priority`, `dueDate`, `deletedAt`, `user`
- Relación `@ManyToOne` con User
- Soft delete con `deletedAt`

**User**
- Campos: `id`, `username`, `email`, `password` (BCrypt), `roles`
- Implementa `UserDetails` de Spring Security
- Relación `@ManyToMany` con Role

**Role**
- Campos: `id`, `name` (ROLE_ADMIN, ROLE_USER)
- Relación `@ManyToMany` con User

**RefreshToken**
- Campos: `id`, `token`, `user`, `expiryDate`, `revoked`
- Relación `@OneToOne` con User
- TTL de 7 días

**AuditLog**
- 5 índices para búsquedas eficientes
- Campos: `id`, `username`, `action`, `resource`, `resourceId`, `status`, `timestamp`, `durationMs`, `correlationId`, `ipAddress`, `userAgent`, `details`

#### Enums

**TaskStatus**
- `PENDING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
- Almacenado como STRING en BD

**TaskPriority**
- `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- Almacenado como STRING en BD

---

### 5. DTO Layer (7 DTOs)

| DTO | Propósito | Validaciones |
|-----|-----------|--------------|
| **TaskRequestDto** | POST/PUT de tareas | `@NotBlank`, `@Size`, `@NotNull` |
| **TaskResponseDto** | Respuestas de tareas | Incluye campos de auditoría (readonly) |
| **TaskPatchDto** | PATCH de tareas | Todos los campos opcionales |
| **TaskFilterDto** | Filtrado avanzado | Criterios de búsqueda múltiples |
| **AuthResponse** | Respuesta de login | `accessToken`, `refreshToken`, `tokenType`, `expiresIn` |
| **RefreshTokenRequest** | Request de refresh | `refreshToken` |
| **RefreshTokenResponse** | Respuesta de refresh | Nuevo `accessToken` |

**Características:**
- ✅ Bean Validation con `@NotBlank`, `@Size`, `@NotNull`, `@Email`, etc.
- ✅ `@Schema` de OpenAPI para documentación
- ✅ Separación clara entre DTOs de entrada y salida
- ✅ Mappers centralizados para conversión DTO ↔ Entity

---

### 6. Configuration Layer (8 configuraciones)

| Configuración | Propósito |
|---------------|-----------|
| **SecurityConfig** | Spring Security con JWT, endpoints públicos/protegidos, RBAC |
| **RedisCacheConfig** | 3 cachés con TTL diferenciado (30min, 15min, 5min), serialización JSON |
| **OpenApiConfig** | Swagger/OpenAPI 3.0, documentación automática, ejemplos |
| **CorsConfig** | CORS para frontend, orígenes permitidos |
| **AuditorAwareImpl** | Captura usuario actual para JPA Auditing |
| **JwtSecretValidator** | Validación de JWT secret al inicio de la aplicación |
| **UserDetailsServiceConfig** | UserDetailsService personalizado |
| **RateLimitProperties** | Configuración de rate limiting (capacity, tokens, period) |

---

### 7. AOP Layer (3 aspectos + 1 anotación)

| Aspecto | Propósito |
|---------|-----------|
| **AuditAspect** | Intercepta métodos con `@Auditable`, captura contexto (usuario, timestamp, duración), persiste en BD |
| **LoggingAspect** | Logging automático de entrada/salida de métodos |
| **PerformanceAspect** | Medición de tiempo de ejecución de métodos |
| **@Auditable** | Anotación personalizada para marcar métodos auditables |

**Características:**
- ✅ `@Around` advice para interceptar antes y después
- ✅ Captura de excepciones y contexto completo
- ✅ Sanitización de información sensible (passwords, tokens)
- ✅ Almacenamiento asíncrono en BD

---

### 8. Filter Layer (3 filtros)

| Filtro | Orden | Propósito |
|--------|-------|-----------|
| **CorrelationIdFilter** | 1 | Genera/extrae correlation ID para trazabilidad de requests |
| **RateLimitFilter** | 2 | Rate limiting con Token Bucket (100 req/min por IP) |
| **JwtAuthenticationFilter** | 3 | Extrae y valida JWT, establece SecurityContext |

**Características:**
- ✅ Implementan `OncePerRequestFilter`
- ✅ Orden configurado con `@Order`
- ✅ Exclusión de paths específicos (actuator, swagger)

---

## 📊 Estadísticas del Proyecto

### Backend

| Métrica | Cantidad |
|---------|----------|
| **Total de clases Java** | 70+ |
| **Total de archivos de configuración** | 8 |
| **Total de archivos de documentación** | 14 |
| **Endpoints REST** | 35+ |
| **Tests** | 50+ (22 integración + 28 unitarios) |
| **Índices de BD** | 13+ (8 en Task, 5 en AuditLog) |
| **Cachés Redis** | 3 (tasks, tasksByUser, taskStats) |
| **Exception Handlers** | 11 tipos de excepciones manejadas |

### Frontend

| Métrica | Cantidad |
|---------|----------|
| **Componentes** | 15+ |
| **Servicios** | 6 |
| **Guards** | 2 |
| **Interceptors** | 1 |
| **Models/Interfaces** | 10+ |

### DevOps

| Métrica | Cantidad |
|---------|----------|
| **GitHub Actions Workflows** | 3 (CI, Docker Build, Deploy) |
| **Comandos Make** | 50+ |
| **Archivos Docker** | 2 (Dockerfile multi-stage, docker-compose.yml) |
| **Scripts de automatización** | 4 |

---

## 🎯 Desglose por Responsabilidad

### Operaciones CRUD de Tareas

**CREATE:**
- Controller: `TaskCommandController.createTask()`
- Service: `TaskServiceImpl.createTask()`
- Repository: `TaskRepository.save()`
- Audit: `@Auditable` registra automáticamente
- Cache: Invalida cache `tasksByUser`

**READ:**
- Controller: `TaskQueryController.getAllTasks()`, `getTaskById()`
- Service: `TaskServiceImpl.getAllTasks()`, `getTaskById()`
- Repository: `TaskRepository.findAll()`, `findById()`
- Cache: `@Cacheable("tasks")` para `getTaskById()`

**UPDATE:**
- Controller: `TaskCommandController.updateTask()`, `patchTask()`
- Service: `TaskServiceImpl.updateTask()`, `patchTask()`
- Repository: `TaskRepository.save()`
- Audit: `@Auditable` registra cambios
- Cache: `@CacheEvict` invalida cache

**DELETE (Soft):**
- Controller: `TaskCommandController.deleteTask()`
- Service: `TaskServiceImpl.deleteTask()`
- Repository: Actualiza `deletedAt` con `save()`
- Audit: `@Auditable` registra eliminación
- Cache: `@CacheEvict` invalida cache

---

## 🔍 Convenciones de Nomenclatura

### Java

- **Clases**: PascalCase (ej: `TaskController`, `TaskServiceImpl`)
- **Métodos**: camelCase (ej: `getAllTasks`, `createTask`)
- **Variables**: camelCase (ej: `taskRepository`, `jwtToken`)
- **Constantes**: UPPER_SNAKE_CASE (ej: `API_VERSION`, `MAX_POOL_SIZE`)
- **Paquetes**: lowercase (ej: `com.taskmanagement.api`)
- **DTOs**: Sufijo "Dto" (ej: `TaskRequestDto`, `TaskResponseDto`)
- **Implementaciones**: Sufijo "Impl" en paquete `impl/` (ej: `TaskServiceImpl`)
- **Excepciones**: Sufijo "Exception" (ej: `ResourceNotFoundException`)

### TypeScript/Angular

- **Componentes**: kebab-case (ej: `task-list`, `task-form`)
- **Servicios**: camelCase (ej: `taskService`, `authService`)
- **Interfaces**: PascalCase (ej: `Task`, `User`)
- **Variables**: camelCase (ej: `tasks`, `isLoading`)
- **Constantes**: UPPER_SNAKE_CASE o camelCase según contexto

---

## 📁 Archivos de Configuración Importantes

| Archivo | Propósito |
|---------|-----------|
| **application.yml** | Configuración principal (profiles, datasource, Redis, logging, actuator) |
| **application-dev.yml** | Perfil de desarrollo |
| **application-test.yml** | Perfil de testing |
| **application-prod.yml** | Perfil de producción |
| **logback-spring.xml** | Configuración de logging (consola, archivo, patrones) |
| **docker-compose.yml** | Orquestación de contenedores (PostgreSQL + Redis + App + pgAdmin) |
| **Dockerfile** | Imagen Docker multi-stage optimizada |
| **build.gradle** | Dependencias y plugins de Gradle |
| **.env.example** | Variables de entorno de ejemplo |
| **Makefile** | Comandos de automatización (50+ comandos) |

---

## 🔗 Flujo de Datos Típico

### Ejemplo: Crear una Tarea

```
1. Frontend (Angular)
   ↓ HTTP POST /api/v1/tasks
   ↓ Headers: Authorization: Bearer <JWT>
   ↓ Body: { "title": "...", "description": "...", "status": "PENDING" }

2. Backend - Filter Chain
   ↓ CorrelationIdFilter: Genera correlation-id-123
   ↓ RateLimitFilter: Verifica tokens disponibles (99/100 restantes)
   ↓ JwtAuthenticationFilter: Valida JWT, extrae username "admin"

3. Controller Layer
   ↓ TaskCommandController.createTask(@Valid @RequestBody TaskRequestDto dto)
   ↓ Validación de Bean Validation (@NotBlank, @Size, @NotNull)

4. AOP Layer (Before)
   ↓ AuditAspect: Captura contexto (username="admin", action="CREATE_TASK", timestamp)
   ↓ LoggingAspect: Log "Entrando a createTask()"
   ↓ PerformanceAspect: Inicia timer

5. Service Layer
   ↓ TaskServiceImpl.createTask(dto)
   ↓ @Transactional: Inicia transacción
   ↓ Validaciones de negocio (límite de tareas por usuario, etc.)
   ↓ TaskMapper.toEntity(dto): Convierte DTO → Entity
   ↓ task.setUser(currentUser)

6. Repository Layer
   ↓ TaskRepository.save(task)
   ↓ JPA Auditing: Establece createdAt, updatedAt, createdBy
   ↓ INSERT INTO tasks (title, description, status, ...) VALUES (...)

7. Database
   ↓ PostgreSQL 18: Ejecuta INSERT
   ↓ Índices actualizados automáticamente (8 índices en tasks)
   ↓ Task guardada con id=123

8. Repository Layer (return)
   ↑ Retorna Task entity con id=123

9. Service Layer (return)
   ↑ TaskMapper.toResponseDto(task): Convierte Entity → DTO
   ↑ @CacheEvict("tasksByUser"): Invalida cache de tareas por usuario
   ↑ @Transactional: Commit de transacción

10. AOP Layer (After)
    ↑ PerformanceAspect: Stop timer (durationMs=45)
    ↑ LoggingAspect: Log "Saliendo de createTask() con resultado=TaskResponseDto(...)"
    ↑ AuditAspect: Persiste AuditLog en BD
       - username: "admin"
       - action: "CREATE_TASK"
       - resource: "TASK"
       - resourceId: "123"
       - status: "SUCCESS"
       - timestamp: "2025-11-18T10:30:00"
       - durationMs: 45
       - correlationId: "correlation-id-123"

11. Controller Layer (return)
    ↑ ResponseEntity.status(201).body(taskResponseDto)
    ↑ Headers: X-RateLimit-Remaining: 99

12. Frontend (Angular)
    ↑ HTTP 201 Created
    ↑ Body: { "id": 123, "title": "...", "createdAt": "...", ... }
    ↑ Actualiza UI con nueva tarea
```

---

## 📚 Próximos Pasos Recomendados

### Para Nuevos Desarrolladores

1. ✅ Leer [README.md](README.md) - Guía principal
2. ✅ Seguir [QUICKSTART.md](QUICKSTART.md) - Inicio rápido
3. ✅ Revisar [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Este archivo
4. ✅ Estudiar [POSTGRESQL_SETUP.md](POSTGRESQL_SETUP.md) - Configuración de BD
5. ✅ Entender [AUDITING.md](AUDITING.md) - Sistema de auditoría
6. ✅ Revisar [docs/AUDITORIA_AOP.md](docs/AUDITORIA_AOP.md) - Auditoría con AOP
7. ✅ Explorar código en orden: Models → Repositories → Services → Controllers

### Para Extender Funcionalidades

1. **Agregar nuevo endpoint**:
   - Controller → Service → Repository
   - Agregar `@Auditable` si es operación crítica
   - Agregar `@Cacheable` si es operación de lectura frecuente
   - Documentar con `@Operation` de OpenAPI

2. **Agregar nueva entidad**:
   - Crear Entity extendiendo `Auditable`
   - Crear Repository extendiendo `JpaRepository`
   - Crear DTOs (Request, Response, Patch)
   - Crear Mapper para conversión DTO ↔ Entity
   - Crear Service con lógica de negocio
   - Crear Controller con endpoints REST
   - Agregar índices si es necesario

3. **Agregar nueva funcionalidad en frontend**:
   - Crear componente en módulo apropiado
   - Crear servicio para comunicación con backend
   - Agregar rutas en routing module
   - Agregar guards si requiere autenticación/autorización

---

**Última actualización:** 2025-11-18
**Versión del proyecto:** 2.0.0
**Arquitectura**: Layered + CQRS + AOP + Full-Stack
