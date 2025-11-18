# 📋 Task Management API

> **API REST Enterprise-Grade Full-Stack para gestión de tareas, desarrollada con Spring Boot 3.5.7, Java 21, PostgreSQL 18, Redis 7, Angular 19 y las mejores prácticas de la industria.**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Angular](https://img.shields.io/badge/Angular-19-red.svg)](https://angular.io/)
[![Docker](https://img.shields.io/badge/Docker-Optimized-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Tabla de Contenidos

- [🎯 Descripción](#-descripción)
- [✨ Características Principales](#-características-principales)
- [🛠️ Tecnologías y Herramientas](#️-tecnologías-y-herramientas)
- [🏗️ Arquitectura](#️-arquitectura)
- [🚀 Inicio Rápido](#-inicio-rápido)
- [🌐 URLs y Endpoints](#-urls-y-endpoints)
- [🔐 Autenticación y Seguridad](#-autenticación-y-seguridad)
- [📊 Monitoreo y Métricas](#-monitoreo-y-métricas)
- [🐳 Docker y Deployment](#-docker-y-deployment)
- [📚 Documentación](#-documentación)

---

## 🎯 Descripción

**Task Management API** es una aplicación **Full-Stack Production-Ready** que demuestra expertise en desarrollo enterprise. El proyecto implementa:

✅ **Arquitectura limpia** con separación de responsabilidades (CQRS, AOP, DDD)
✅ **Seguridad robusta** con JWT, refresh tokens, rate limiting, audit logging
✅ **Cache distribuido** con Redis para optimización de performance
✅ **Monitoreo completo** con Actuator, Prometheus, logging estructurado, correlation IDs
✅ **Base de datos optimizada** con 13+ índices, audit trails, soft deletes
✅ **Docker multi-stage** con capas optimizadas para builds 100x más rápidos
✅ **Frontend moderno** con Angular 19 + Material Design
✅ **CI/CD completo** con GitHub Actions (build, test, deploy)
✅ **Documentación automática** con OpenAPI/Swagger

### Objetivo del Proyecto

Demostración de habilidades **Senior Full-Stack Developer** en:
- ✅ Spring Boot 3.x ecosystem (Security, Data JPA, Cache, AOP, Actuator)
- ✅ Diseño de APIs RESTful con versionado, paginación, filtrado avanzado
- ✅ Seguridad: JWT, refresh tokens, RBAC, rate limiting, CORS, audit logging
- ✅ Performance: Redis cache, query optimization, connection pooling, índices de BD
- ✅ Observability: Logging (SLF4J), métricas (Micrometer), health checks, correlation IDs
- ✅ DevOps: Docker multi-stage, docker-compose, Makefile automation, GitHub Actions
- ✅ Base de datos: PostgreSQL 18, 13+ índices, audit trails, soft deletes
- ✅ Frontend: Angular 19, TypeScript, RxJS, Material Design, Guards, Interceptors

---

## ✨ Características Principales

### 🔐 Seguridad Enterprise-Grade
- ✅ **JWT Authentication** con access tokens (1h) y refresh tokens (7 días)
- ✅ **Token Rotation** automático para prevenir robos
- ✅ **Rate Limiting** distribuido - 100 req/min por IP (Token Bucket algorithm)
- ✅ **CORS configurado** para frontend integrations
- ✅ **Password encryption** con BCrypt
- ✅ **RBAC** (Role-Based Access Control) - roles ADMIN y USER
- ✅ **Audit Logging automático** con Spring AOP (quién, qué, cuándo, cuánto tiempo)
- ✅ **Detección de actividad sospechosa** en audit logs

### ⚡ Performance Optimization
- ✅ **Redis Cache distribuido** con 3 niveles (tasks, tasksByUser, taskStats)
- ✅ **Cache invalidation** inteligente con @CacheEvict
- ✅ **HikariCP** connection pooling optimizado (20 conexiones)
- ✅ **13+ índices compuestos** en PostgreSQL (8 en Task, 5 en AuditLog)
- ✅ **Lazy loading** y proyecciones DTO
- ✅ **Query optimization** con JPA Specifications
- ✅ **TTL diferenciado** por tipo de caché (30min, 15min, 5min)

### 📊 Monitoreo y Observabilidad
- ✅ **Spring Boot Actuator** con health checks
- ✅ **Prometheus metrics** export
- ✅ **Correlation IDs** para request tracing
- ✅ **Structured logging** con SLF4J + Logback
- ✅ **Audit trails** completos en BD con AOP
- ✅ **JVM metrics** y estadísticas de Hibernate
- ✅ **Performance monitoring** con aspectos AOP

### 🗄️ Base de Datos Avanzada
- ✅ **PostgreSQL 18** con optimizaciones de performance
- ✅ **JPA Auditing** automático (createdAt, updatedAt, createdBy, lastModifiedBy)
- ✅ **Soft deletes** con papelera de reciclaje (90 días retention)
- ✅ **13+ índices compuestos** para queries frecuentes
- ✅ **Connection pooling** con HikariCP
- ✅ **Task Priority** (LOW, MEDIUM, HIGH, CRITICAL)

### 🎨 Frontend Moderno
- ✅ **Angular 19** con TypeScript
- ✅ **Material Design** components
- ✅ **RxJS** para reactive programming
- ✅ **Guards** para protección de rutas
- ✅ **Interceptors** para JWT automático
- ✅ **Dashboard** con estadísticas en tiempo real
- ✅ **Papelera** de reciclaje para recuperar tareas
- ✅ **Panel administrativo** para audit logs

### 🐳 DevOps y Deployment
- ✅ **Docker multi-stage** con Spring Boot Layered JARs
- ✅ **Docker Compose** orchestration (PostgreSQL + Redis + App + pgAdmin)
- ✅ **Makefile** con 50+ comandos automatizados
- ✅ **GitHub Actions** CI/CD pipeline completo (3 workflows)
- ✅ **Health checks** en todos los servicios
- ✅ **Resource limits** configurados
- ✅ **Dependabot** para actualizaciones automáticas

### 📚 Documentación Automática
- ✅ **OpenAPI 3.0** specification
- ✅ **Swagger UI** interactive documentation
- ✅ **JavaDoc** completo en código
- ✅ **README exhaustivo** con ejemplos
- ✅ **Documentación técnica** en /docs (4 archivos)

---

## 🛠️ Tecnologías y Herramientas

### Backend Core
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 21 (LTS) | Lenguaje base con virtual threads, pattern matching |
| **Spring Boot** | 3.5.7 | Framework principal |
| **PostgreSQL** | 18 | Base de datos principal |
| **Redis** | 7 | Cache distribuido |

### Frontend
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Angular** | 19 | Framework frontend |
| **TypeScript** | 5.x | Lenguaje tipado |
| **RxJS** | 7.x | Reactive programming |
| **Angular Material** | 19 | UI Components |

### Spring Ecosystem
| Módulo | Propósito |
|--------|-----------|
| **Spring Data JPA** | ORM y repositorios |
| **Spring Security** | Autenticación y autorización |
| **Spring Cache** | Abstracción de caché |
| **Spring AOP** | Logging, auditoría, performance monitoring |
| **Spring Actuator** | Monitoreo y métricas |

### Herramientas y Librerías
| Herramienta | Propósito |
|-------------|-----------|
| **Lombok** | Reduce boilerplate code |
| **MapStruct** | Mapeo DTO ↔ Entity |
| **JJWT** | JWT token generation |
| **Bucket4j** | Rate limiting (Token Bucket) |
| **Micrometer** | Métricas (Prometheus) |
| **SpringDoc OpenAPI** | Documentación Swagger |
| **HikariCP** | Connection pooling |
| **Lettuce** | Cliente Redis |

### DevOps
| Herramienta | Propósito |
|-------------|-----------|
| **Docker** | Containerization |
| **Docker Compose** | Multi-container orchestration |
| **GitHub Actions** | CI/CD automation |
| **Makefile** | Task automation |

---

## 🏗️ Arquitectura

### Patrón: Layered Architecture + CQRS + AOP

```
┌─────────────────────────────────────────────────────────────┐
│                  FRONTEND (Angular 19)                       │
│  - Dashboard                                                 │
│  - Task Management                                           │
│  - Trash / Recycling Bin                                     │
│  - Admin Panel (Audit Logs)                                  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼ HTTP/REST
┌─────────────────────────────────────────────────────────────┐
│              SPRING SECURITY FILTER CHAIN                    │
│  - JwtAuthenticationFilter                                   │
│  - RateLimitFilter (Token Bucket - 100 req/min)             │
│  - CorrelationIdFilter (Request Tracing)                     │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   CONTROLLER LAYER                           │
│  - TaskCommandController (POST, PUT, PATCH, DELETE)          │
│  - TaskQueryController (GET con filtrado avanzado)           │
│  - TaskStatisticsController (estadísticas cacheadas)         │
│  - TaskTrashController (papelera y restore)                  │
│  - AuthController (login, register, refresh)                 │
│  - AuditLogController (logs y estadísticas)                  │
│  - RateLimitAdminController (gestión de rate limiting)       │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    AOP ASPECTS                               │
│  - AuditAspect (@Auditable - audit logging automático)      │
│  - LoggingAspect (logging automático)                        │
│  - PerformanceAspect (medición de performance)               │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                   SERVICE LAYER                              │
│  - TaskServiceImpl (CRUD + cache + validaciones)             │
│  - AuthService (JWT + refresh tokens)                        │
│  - RefreshTokenService (token rotation)                      │
│  - AuditLogService (persistencia de auditoría)               │
│  - JwtService (generación/validación de tokens)              │
│  - RateLimitService (gestión de buckets)                     │
│  - @Transactional, @Cacheable, @CacheEvict                  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                  REPOSITORY LAYER                            │
│  - TaskRepository (JPA + Specifications)                     │
│  - UserRepository                                            │
│  - RoleRepository                                            │
│  - RefreshTokenRepository                                    │
│  - AuditLogRepository                                        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
        ┌───────────────────┴───────────────────┐
        ▼                                       ▼
┌──────────────────┐                  ┌──────────────────┐
│   POSTGRESQL 18  │                  │      REDIS 7     │
│   (Persistent    │                  │   (Cache Layer)  │
│    Database)     │                  │                  │
│  - 13+ índices   │                  │  - tasks cache   │
│  - Audit trails  │                  │  - tasksByUser   │
│  - Soft deletes  │                  │  - taskStats     │
└──────────────────┘                  └──────────────────┘
```

### Componentes Clave

#### 1. **Controllers** (Capa de Presentación)
- **TaskCommandController** - Operaciones de escritura (POST, PUT, PATCH, DELETE) - CQRS Write
- **TaskQueryController** - Operaciones de lectura (GET con paginación y filtrado) - CQRS Read
- **TaskStatisticsController** - Estadísticas y reportes (cacheadas 5min)
- **TaskTrashController** - Papelera de reciclaje (soft deletes, restore, purge)
- **AuthController** - Login, register, refresh tokens
- **AuditLogController** - Logs de auditoría con filtrado avanzado
- **RateLimitAdminController** - Gestión de rate limiting

#### 2. **Services** (Lógica de Negocio)
- **TaskServiceImpl** - CRUD con cache, validaciones, ownership, soft deletes
- **AuthService** - Gestión de autenticación y JWT
- **RefreshTokenService** - Gestión de refresh tokens y rotación
- **AuditLogService** - Registro y consulta de auditoría
- **JwtService** - Generación y validación de tokens
- **RateLimitService** - Rate limiting con Token Bucket

#### 3. **Repositories** (Acceso a Datos)
- **TaskRepository** - Métodos JPA + Specifications (filtrado dinámico)
- **UserRepository** - Gestión de usuarios
- **RoleRepository** - Gestión de roles
- **RefreshTokenRepository** - Almacenamiento de tokens
- **AuditLogRepository** - Registro de auditoría

#### 4. **Security Components**
- **JwtAuthenticationFilter** - Intercepta requests y valida JWT
- **JwtService** - Generación y validación de tokens
- **SecurityConfig** - Configuración de Spring Security
- **RateLimitFilter** - Rate limiting con Bucket4j

#### 5. **Cache Layer**
- **RedisCacheConfig** - Configuración de 3 cachés:
  - `tasks` (30min TTL) - Tareas individuales
  - `tasksByUser` (15min TTL) - Tareas por usuario
  - `taskStats` (5min TTL) - Estadísticas
- Serialización JSON con Jackson
- Transaction-aware cache

#### 6. **AOP Aspects**
- **AuditAspect** - Auditoría automática con @Auditable
- **LoggingAspect** - Logging automático de métodos
- **PerformanceAspect** - Medición de performance

---

## 🚀 Inicio Rápido

### Prerrequisitos

**Opción 1: Docker (Recomendado - No requiere instalaciones)**
- Docker 20.10+
- Docker Compose 2.0+

**Opción 2: Desarrollo Local**
- Java 21+
- Node.js 18+ y npm 9+
- PostgreSQL 18+
- Redis 7+
- Angular CLI 19+
- Gradle 8.5+ (incluido wrapper)

---

### Opción 1: Docker Compose (Recomendado)

#### Inicio con un comando:
```bash
# Clonar repositorio
git clone https://github.com/tu-usuario/task-project.git
cd task-project

# Levantar todos los servicios (PostgreSQL + Redis + Backend + pgAdmin)
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f
```

#### Frontend (en otra terminal):
```bash
cd frontend
npm install
npm start
```

#### ¡Listo! La aplicación está corriendo en:
- 🌐 **Frontend**: http://localhost:4200
- 🌐 **API**: http://localhost:8080
- 📚 **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- 🔍 **pgAdmin**: http://localhost:5050
- ❤️ **Health Check**: http://localhost:8080/api/v1/actuator/health

---

### Opción 2: Desarrollo Local

#### 1. Configurar PostgreSQL
```bash
# Crear base de datos
createdb taskmanagement_db

# O con psql
psql -U postgres
CREATE DATABASE taskmanagement_db;
```

#### 2. Configurar Redis
```bash
# Instalar Redis (Ubuntu/Debian)
sudo apt-get install redis-server

# Iniciar Redis
redis-server

# Verificar
redis-cli ping  # Debe retornar "PONG"
```

#### 3. Configurar variables de entorno
```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar .env con tus credenciales
export DATABASE_URL=jdbc:postgresql://localhost:5432/taskmanagement_db
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=tu_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=$(openssl rand -base64 64)
```

#### 4. Backend - Compilar y ejecutar
```bash
# Compilar proyecto
./gradlew clean build

# Ejecutar aplicación
./gradlew bootRun

# O con el JAR generado
java -jar build/libs/task-management-api-0.0.1-SNAPSHOT.jar
```

#### 5. Frontend - Instalar y ejecutar
```bash
cd frontend
npm install
npm start
```

---

## 🌐 URLs y Endpoints

### URLs Principales

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **Frontend** | `http://localhost:4200` | Aplicación Angular |
| **API Base** | `http://localhost:8080/api/v1` | Base path de todos los endpoints |
| **Swagger UI** | `http://localhost:8080/swagger-ui/index.html` | Documentación interactiva |
| **OpenAPI JSON** | `http://localhost:8080/v3/api-docs` | Especificación OpenAPI 3.0 |
| **Health Check** | `http://localhost:8080/api/v1/actuator/health` | Estado de la aplicación |
| **Metrics** | `http://localhost:8080/api/v1/actuator/metrics` | Métricas de la aplicación |
| **Prometheus** | `http://localhost:8080/api/v1/actuator/prometheus` | Métricas formato Prometheus |
| **pgAdmin** | `http://localhost:5050` | Administrador de PostgreSQL |

### Endpoints de la API

#### Autenticación (`/api/v1/auth`)
| Método | Endpoint | Descripción | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/register` | Registrar nuevo usuario | ❌ No |
| POST | `/auth/login` | Login (retorna access + refresh tokens) | ❌ No |
| POST | `/auth/refresh` | Renovar access token con refresh token | ❌ No |
| POST | `/auth/logout` | Cerrar sesión (invalida tokens) | ✅ Sí |

#### Tareas - Comandos (`/api/v1/tasks`) - CQRS Write
| Método | Endpoint | Descripción | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/tasks` | Crear nueva tarea | ✅ Sí |
| PUT | `/tasks/{id}` | Actualizar tarea completa | ✅ Sí |
| PATCH | `/tasks/{id}` | Actualizar parcialmente | ✅ Sí |
| DELETE | `/tasks/{id}` | Mover a papelera (soft delete) | ✅ Sí |

#### Tareas - Consultas (`/api/v1/tasks`) - CQRS Read
| Método | Endpoint | Descripción | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/tasks` | Listar tareas con paginación | ✅ Sí |
| GET | `/tasks/{id}` | Obtener tarea por ID (cacheada) | ✅ Sí |
| GET | `/tasks/status/{status}` | Filtrar por estado | ✅ Sí |
| GET | `/tasks/search` | Buscar por título | ✅ Sí |
| GET | `/tasks/filter` | Filtrado avanzado (prioridad, fechas, búsqueda) | ✅ Sí |

#### Estadísticas (`/api/v1/tasks/statistics`)
| Método | Endpoint | Descripción | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/statistics` | Estadísticas globales (cacheadas 5min) | ✅ Sí |

#### Papelera (`/api/v1/tasks/trash`)
| Método | Endpoint | Descripción | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/trash` | Listar tareas eliminadas | ✅ Sí |
| POST | `/{id}/restore` | Restaurar tarea | ✅ Sí |
| DELETE | `/trash/purge` | Eliminar permanentemente (>90 días) | ✅ Sí (ADMIN) |

#### Auditoría (`/api/v1/audit`)
| Método | Endpoint | Descripción | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/audit` | Logs de auditoría completos | ✅ Sí (ADMIN) |
| GET | `/audit/user/{username}` | Logs por usuario | ✅ Sí (ADMIN) |
| GET | `/audit/action/{action}` | Logs por acción | ✅ Sí (ADMIN) |
| GET | `/audit/date-range` | Logs por rango de fechas | ✅ Sí (ADMIN) |
| GET | `/audit/resource/{resource}/{id}` | Historial de recurso | ✅ Sí (ADMIN) |
| GET | `/audit/failures` | Operaciones fallidas | ✅ Sí (ADMIN) |
| GET | `/audit/statistics` | Estadísticas de auditoría | ✅ Sí (ADMIN) |
| GET | `/audit/suspicious-activity` | Detección de anomalías | ✅ Sí (ADMIN) |

#### Rate Limiting Admin (`/api/v1/admin/rate-limit`)
| Método | Endpoint | Descripción | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/info` | Configuración de rate limiting | ✅ Sí (ADMIN) |
| GET | `/stats` | Estadísticas de uso | ✅ Sí (ADMIN) |
| POST | `/clear-cache` | Limpiar cache de buckets | ✅ Sí (ADMIN) |

---

## 🔐 Autenticación y Seguridad

### Usuarios de Prueba

| Usuario | Password | Rol | Descripción |
|---------|----------|-----|-------------|
| `admin` | `admin123` | ADMIN | Acceso completo + panel admin |
| `testuser` | `test123` | USER | Usuario estándar |

### Flujo de Autenticación

#### 1. Login (Obtener tokens)
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Respuesta:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

#### 2. Usar Access Token
```bash
curl -X GET http://localhost:8080/api/v1/tasks \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

#### 3. Refresh Token (cuando access token expire)
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }'
```

### Características de Seguridad

✅ **JWT con HS256** (simétrico, configurable a RS256)
✅ **Access Token**: 1 hora de vida
✅ **Refresh Token**: 7 días de vida con rotación automática
✅ **Token Rotation**: Cada refresh genera nuevo token
✅ **Rate Limiting**: 100 requests/minuto por IP (Token Bucket)
✅ **CORS**: Configurado para frontends permitidos
✅ **Password Encryption**: BCrypt con salt
✅ **Audit Logging**: Registro automático con AOP de todas las acciones críticas
✅ **Correlation IDs**: Trazabilidad completa de requests

---

## 📊 Monitoreo y Métricas

### Spring Boot Actuator

#### Health Check
```bash
# Simple health check
curl http://localhost:8080/api/v1/actuator/health

# Health check detallado (requiere autenticación)
curl http://localhost:8080/api/v1/actuator/health \
  -H "Authorization: Bearer <token>"
```

**Respuesta:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500107862016,
        "free": 123456789012
      }
    },
    "ping": {
      "status": "UP"
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    }
  }
}
```

#### Métricas Disponibles
```bash
# Listar todas las métricas
curl http://localhost:8080/api/v1/actuator/metrics

# Métricas específicas
curl http://localhost:8080/api/v1/actuator/metrics/jvm.memory.used
curl http://localhost:8080/api/v1/actuator/metrics/http.server.requests
curl http://localhost:8080/api/v1/actuator/metrics/hikaricp.connections.active
curl http://localhost:8080/api/v1/actuator/metrics/cache.gets
```

#### Prometheus Export
```bash
# Exportar métricas en formato Prometheus
curl http://localhost:8080/api/v1/actuator/prometheus
```

### Audit Logging

Todas las operaciones críticas son registradas automáticamente con AOP:

```bash
# Ver logs de auditoría (requiere rol ADMIN)
curl http://localhost:8080/api/v1/audit \
  -H "Authorization: Bearer <token>"

# Estadísticas de auditoría
curl http://localhost:8080/api/v1/audit/statistics \
  -H "Authorization: Bearer <token>"

# Detectar actividad sospechosa
curl http://localhost:8080/api/v1/audit/suspicious-activity \
  -H "Authorization: Bearer <token>"
```

**Información capturada:**
- Usuario que realizó la acción
- Acción realizada (CREATE_TASK, UPDATE_TASK, DELETE_TASK, etc.)
- Recurso afectado (TASK)
- Timestamp con precisión de milisegundos
- Duración de la operación
- Estado (SUCCESS, FAILURE, ERROR)
- Correlation ID para trazabilidad

---

## 🐳 Docker y Deployment

### Docker Compose

```bash
# Iniciar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down

# Rebuild completo
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

### Makefile Commands

```bash
# Ver todos los comandos disponibles
make help

# Inicio rápido
make up              # Inicia todos los servicios
make logs            # Ver logs en tiempo real
make logs-app        # Ver solo logs de la app
make logs-db         # Ver solo logs de PostgreSQL

# Desarrollo
make dev             # Inicia servicios + muestra logs
make shell-app       # Abrir shell en contenedor app
make shell-db        # Conectarse a PostgreSQL (psql)

# Testing y Health Checks
make test            # Ejecutar tests localmente
make check-health    # Verificar endpoint /actuator/health

# Base de Datos
make db-backup       # Crear backup de PostgreSQL
make db-restore      # Restaurar backup
make db-reset        # Reiniciar BD (borra datos)

# Docker Management
make build           # Construir imágenes Docker
make down            # Detener y eliminar contenedores
make restart         # Reiniciar todos los servicios
make rebuild         # Rebuild completo y reinicio
```

---

## 📚 Documentación

### Documentación del Proyecto

**En la raíz:**
- [README.md](README.md) - Este archivo (guía principal)
- [QUICKSTART.md](QUICKSTART.md) - Inicio rápido en 4 pasos
- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Estructura del código
- [POSTGRESQL_SETUP.md](POSTGRESQL_SETUP.md) - Configuración de BD
- [CONFIGURACION_COMPLETADA.md](CONFIGURACION_COMPLETADA.md) - Configuración inicial
- [AUDITING.md](AUDITING.md) - Sistema de auditoría con JPA
- [DTO_PATTERN.md](DTO_PATTERN.md) - Patrón DTO y Mappers
- [QUALITY_CHECKLIST.md](QUALITY_CHECKLIST.md) - Checklist de calidad
- [RATE_LIMITING.md](RATE_LIMITING.md) - Rate limiting con Bucket4j
- [SECRETS_MANAGEMENT.md](SECRETS_MANAGEMENT.md) - Gestión de secretos

**En /docs:**
- [docs/AUDITORIA_AOP.md](docs/AUDITORIA_AOP.md) - Auditoría automática con AOP
- [docs/CI_CD.md](docs/CI_CD.md) - Pipeline de CI/CD con GitHub Actions
- [docs/JWT_SECURITY.md](docs/JWT_SECURITY.md) - Seguridad JWT y refresh tokens
- [docs/DATABASE_INDEXES.md](docs/DATABASE_INDEXES.md) - Índices de BD optimizados

### Swagger/OpenAPI

Accede a la documentación interactiva en:
**http://localhost:8080/swagger-ui/index.html**

Incluye:
- Todos los endpoints documentados
- Ejemplos de requests/responses
- Schemas de DTOs
- Códigos HTTP explicados
- Autenticación con JWT (botón "Authorize")

---

## 🎯 Características Destacadas

### CQRS (Command Query Responsibility Segregation)
Separación clara entre operaciones de lectura y escritura:
- **TaskCommandController** - Modificaciones (POST, PUT, PATCH, DELETE)
- **TaskQueryController** - Consultas (GET con paginación y filtrado)

### Soft Deletes con Papelera
- Las tareas eliminadas van a la papelera (soft delete)
- Retención de 90 días antes de purge automático
- Restauración con un clic desde el frontend
- Solo ADMIN puede hacer purge manual

### Cache Distribuido Redis
3 niveles de caché con diferentes TTLs:
- **tasks** (30min) - Tareas individuales por ID
- **tasksByUser** (15min) - Tareas filtradas por usuario
- **taskStats** (5min) - Estadísticas globales

### Audit Logging Automático con AOP
Todas las operaciones críticas se registran automáticamente:
- Anotación `@Auditable` en métodos de servicio
- Aspecto AOP intercepta y registra
- Almacenamiento en BD con índices optimizados
- Panel administrativo para consulta y análisis

### Rate Limiting Avanzado
- 100 requests/minuto por IP (configurable)
- Token Bucket algorithm (Bucket4j)
- Headers informativos (X-RateLimit-Remaining)
- Endpoints administrativos para monitoreo
- Paths excluidos (actuator, swagger)

### Filtrado Avanzado
JPA Specifications para filtrado dinámico:
- Por estado (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
- Por prioridad (LOW, MEDIUM, HIGH, CRITICAL)
- Por rango de fechas
- Búsqueda de texto en título/descripción
- Combinación de múltiples filtros

### Frontend Angular 19
- Dashboard con estadísticas en tiempo real
- CRUD completo de tareas
- Papelera de reciclaje
- Panel administrativo (audit logs)
- Guards para protección de rutas
- Interceptors para JWT automático
- Material Design components

---

## 📞 Soporte y Contacto

¿Preguntas? ¿Sugerencias?

- **GitHub Issues**: [https://github.com/tu-usuario/task-project/issues](https://github.com/tu-usuario/task-project/issues)
- **Email**: tu-email@example.com
- **LinkedIn**: [Tu perfil LinkedIn]

---

## 📄 Licencia

MIT License - Ver [LICENSE](LICENSE) para detalles.

---

**Desarrollado con ❤️ por [Tu Nombre]**
**Stack**: Spring Boot 3.5.7 | Java 21 | Angular 19 | PostgreSQL 18 | Redis 7 | Docker
