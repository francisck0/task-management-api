# 📋 Task Management API

> API REST profesional para gestión de tareas, desarrollada con Spring Boot 3.5.7, Java 21, PostgreSQL 18 y las mejores prácticas de la industria.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Características Principales](#-características-principales)
- [Tecnologías y Herramientas](#-tecnologías-y-herramientas)
- [Arquitectura](#-arquitectura)
- [Configuración Inicial](#-configuración-inicial)
- [Autenticación](#-autenticación)
- [Endpoints de la API](#-endpoints-de-la-api)
- [Ejemplos con Postman](#-ejemplos-con-postman)
- [Swagger y Documentación](#-swagger-y-documentación)
- [Monitoreo y Métricas](#-monitoreo-y-métricas)
- [Rate Limiting](#-rate-limiting)
- [Logging](#-logging)
- [Perfiles de Configuración](#-perfiles-de-configuración)
- [Seguridad](#-seguridad)
- [Base de Datos](#-base-de-datos)
- [Testing](#-testing)
- [Despliegue](#-despliegue)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Descripción

**Task Management API** es una aplicación REST completa que demuestra el desarrollo profesional de APIs con Spring Boot, implementando patrones de diseño modernos, seguridad robusta, y monitoreo avanzado.

### Objetivo del Proyecto

Este proyecto fue desarrollado como demostración de habilidades en:
- Desarrollo backend con Spring Boot y Java 21
- Diseño de APIs RESTful siguiendo mejores prácticas
- Implementación de seguridad con JWT y Spring Security
- Gestión de base de datos con JPA/Hibernate y PostgreSQL
- Monitoreo y observabilidad con Actuator y Prometheus
- Control de tráfico con Rate Limiting
- Documentación automática con OpenAPI/Swagger

---

## ✨ Características Principales

### 🔐 Seguridad
- ✅ **Autenticación JWT** - Tokens seguros para autenticación stateless
- ✅ **Spring Security** - Control de acceso basado en roles (RBAC)
- ✅ **BCrypt** - Encriptación de contraseñas con algoritmo resistente a ataques
- ✅ **Rate Limiting** - Protección contra abuso y ataques DDoS
- ✅ **CORS** - Configuración segura para peticiones cross-origin
- ✅ **Variables de entorno** - Gestión segura de secretos

### 📊 Funcionalidad
- ✅ **CRUD completo** - Crear, leer, actualizar y eliminar tareas
- ✅ **Búsqueda y filtrado** - Por estado, título, fechas
- ✅ **Paginación** - Soporte para grandes volúmenes de datos
- ✅ **Validaciones** - Bean Validation para integridad de datos
- ✅ **Auditoría** - Timestamps automáticos (createdAt, updatedAt)
- ✅ **Estadísticas** - Dashboard de métricas de tareas

### 🛠️ Calidad y Mantenibilidad
- ✅ **Arquitectura en capas** - Separación clara de responsabilidades
- ✅ **DTOs** - Desacoplamiento entre capas
- ✅ **Manejo de excepciones** - Respuestas de error estandarizadas
- ✅ **Logging estructurado** - SLF4J con múltiples niveles
- ✅ **Documentación automática** - Swagger/OpenAPI 3.0
- ✅ **Código documentado** - Comentarios explicativos en cada clase

### 📈 Monitoreo y Observabilidad
- ✅ **Spring Boot Actuator** - Endpoints de monitoreo
- ✅ **Health Checks** - Verificación del estado de la aplicación
- ✅ **Métricas** - Prometheus para monitoreo de performance
- ✅ **Perfiles** - Configuraciones por entorno (dev, test, prod)

---

## 🛠️ Tecnologías y Herramientas

### Backend
| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| **Java** | 21 (LTS) | Lenguaje de programación |
| **Spring Boot** | 3.5.7 | Framework principal |
| **Spring Data JPA** | 3.5.7 | Capa de persistencia |
| **Spring Security** | 6.x | Seguridad y autenticación |
| **Spring Validation** | 3.5.7 | Validación de datos |

### Base de Datos
| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| **PostgreSQL** | 18 | Base de datos relacional |
| **HikariCP** | 5.x | Pool de conexiones de alto rendimiento |
| **Flyway** | (opcional) | Migraciones de base de datos |

### Seguridad y JWT
| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| **JJWT** | 0.12.3 | Generación y validación de JWT |
| **BCrypt** | - | Encriptación de contraseñas |

### Rate Limiting
| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| **Bucket4j** | 8.10.1 | Rate limiting con algoritmo Token Bucket |

### Documentación y Monitoreo
| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| **SpringDoc OpenAPI** | 2.3.0 | Documentación Swagger/OpenAPI 3.0 |
| **Spring Boot Actuator** | 3.5.7 | Endpoints de monitoreo |
| **Micrometer** | - | Métricas en formato Prometheus |

### Utilidades
| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| **Lombok** | - | Reducción de código boilerplate |
| **Gradle** | 8.x | Gestión de dependencias y build |
| **Docker** | - | Contenedorización |
| **Docker Compose** | - | Orquestación de contenedores |

---

## 🏗️ Arquitectura

### Patrón: Arquitectura en Capas (Layered Architecture)

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENTE (Postman, Frontend)              │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  FILTROS (Security, Rate Limiting, CORS, Exception Handler) │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│               CAPA DE PRESENTACIÓN (Controller)              │
│  • Recibe peticiones HTTP                                    │
│  • Valida entrada con @Valid                                 │
│  • Delega a la capa de servicio                             │
│  • Retorna ResponseEntity con status HTTP                    │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                CAPA DE NEGOCIO (Service)                     │
│  • Implementa lógica de negocio                             │
│  • Maneja transacciones (@Transactional)                     │
│  • Convierte entre DTOs y Entidades                         │
│  • Valida reglas de negocio                                 │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│             CAPA DE PERSISTENCIA (Repository)                │
│  • Acceso a base de datos                                   │
│  • Operaciones CRUD con JPA                                 │
│  • Query methods derivados                                  │
│  • Abstracción de la BD                                     │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   BASE DE DATOS (PostgreSQL)                 │
│  • Persistencia de datos                                    │
│  • Integridad referencial                                   │
│  • Transacciones ACID                                       │
└─────────────────────────────────────────────────────────────┘
```

### Estructura del Proyecto

```
src/main/java/com/taskmanagement/api/
├── 📁 config/               # Configuraciones de Spring
│   ├── CorsConfig.java              # Configuración CORS
│   ├── OpenApiConfig.java           # Configuración Swagger
│   ├── AuditorAwareConfig.java      # Auditoría JPA
│   ├── RateLimitProperties.java     # Propiedades de rate limiting
│   └── ...
│
├── 📁 controller/           # Capa de Presentación (REST Controllers)
│   ├── TaskController.java          # Endpoints de tareas
│   ├── AuthController.java          # Endpoints de autenticación
│   └── RateLimitAdminController.java
│
├── 📁 service/              # Capa de Negocio
│   ├── TaskService.java             # Interfaz del servicio
│   ├── impl/
│   │   └── TaskServiceImpl.java     # Implementación de lógica
│   ├── AuthService.java
│   ├── JwtService.java
│   └── RateLimitService.java
│
├── 📁 repository/           # Capa de Persistencia
│   ├── TaskRepository.java          # Acceso a datos de tareas
│   ├── UserRepository.java
│   └── RoleRepository.java
│
├── 📁 model/                # Entidades de Dominio
│   ├── Task.java                    # Entidad Tarea
│   ├── User.java                    # Entidad Usuario
│   └── Role.java                    # Entidad Rol
│
├── 📁 dto/                  # Data Transfer Objects
│   ├── TaskRequestDto.java          # DTO para crear/actualizar
│   ├── TaskResponseDto.java         # DTO para respuestas
│   ├── TaskPatchDto.java            # DTO para actualizaciones parciales
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   └── AuthResponse.java
│
├── 📁 security/             # Configuración de Seguridad
│   ├── SecurityConfig.java          # Configuración Spring Security
│   ├── JwtAuthenticationFilter.java # Filtro JWT
│   └── ...
│
├── 📁 filter/               # Filtros HTTP
│   └── RateLimitFilter.java        # Filtro de rate limiting
│
├── 📁 exception/            # Manejo de Excepciones
│   ├── GlobalExceptionHandler.java  # Manejo global de errores
│   ├── ResourceNotFoundException.java
│   └── ...
│
└── 📁 mapper/               # Conversión entre DTOs y Entidades
    └── TaskMapper.java

src/main/resources/
├── application.yml          # Configuración principal
├── data.sql                 # Datos iniciales (usuarios de prueba)
└── ...
```

---

## 🚀 Configuración Inicial

### Requisitos Previos

- ☕ **Java 21 JDK** - [Descargar](https://adoptium.net/)
- 🐘 **PostgreSQL 18** - [Descargar](https://www.postgresql.org/download/) o usar Docker
- 🐳 **Docker & Docker Compose** (recomendado) - [Descargar](https://www.docker.com/)
- 📦 **Git** - [Descargar](https://git-scm.com/)

### Paso 1: Clonar el Repositorio

```bash
git clone https://github.com/tu-usuario/task-management-api.git
cd task-management-api
```

### Paso 2: Configurar Variables de Entorno

```bash
# Copiar archivo de ejemplo
cp .env.example .env

# Editar con tus configuraciones
nano .env  # o vim, code, etc.
```

**Variables mínimas requeridas:**
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/taskmanagement_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET=tu-secreto-generado-con-openssl
```

**Generar JWT Secret seguro:**
```bash
# Opción 1: Usar script incluido
./scripts/generate-secrets.sh

# Opción 2: Manual con OpenSSL
openssl rand -hex 64
```

### Paso 3: Iniciar PostgreSQL

**Opción A: Docker Compose (Recomendado)**

```bash
# Iniciar PostgreSQL + pgAdmin
docker compose up -d

# Verificar que está corriendo
docker compose ps

# Ver logs
docker compose logs -f postgres
```

Acceso a pgAdmin: http://localhost:5050
- Email: `admin@admin.com`
- Password: `admin`

**Opción B: PostgreSQL Local**

```bash
# Crear base de datos
createdb taskmanagement_db

# O usando psql
psql -U postgres
CREATE DATABASE taskmanagement_db;
\q
```

### Paso 4: Compilar y Ejecutar

```bash
# Compilar el proyecto
./gradlew build

# Ejecutar la aplicación
./gradlew bootRun

# O ejecutar el JAR generado
java -jar build/libs/task-management-api-0.0.1-SNAPSHOT.jar
```

### Paso 5: Verificar que funciona

```bash
# Health check
curl http://localhost:8080/api/v1/actuator/health

# Debería retornar:
# {"status":"UP"}
```

🎉 **¡Listo!** La aplicación está corriendo en: http://localhost:8080/api/v1

---

## 🔐 Autenticación

### Sistema de Autenticación JWT

La API utiliza **JSON Web Tokens (JWT)** para autenticación stateless:

1. **Login** o **Registro** → Recibe token JWT
2. **Incluir token** en todas las peticiones subsecuentes
3. **Token válido por 24 horas** (configurable)

### Usuarios de Prueba

La aplicación crea automáticamente dos usuarios de prueba:

| Usuario | Password | Email | Roles |
|---------|----------|-------|-------|
| `admin` | `admin123` | admin@taskmanagement.com | ROLE_ADMIN, ROLE_USER |
| `testuser` | `test123` | test@taskmanagement.com | ROLE_USER |

### Flujo de Autenticación

```
┌─────────┐              ┌─────────┐              ┌─────────┐
│ Cliente │              │   API   │              │   BD    │
└────┬────┘              └────┬────┘              └────┬────┘
     │                        │                        │
     │  POST /auth/login      │                        │
     │  {username, password}  │                        │
     ├───────────────────────>│                        │
     │                        │  Validar credenciales  │
     │                        ├───────────────────────>│
     │                        │<───────────────────────┤
     │                        │  Usuario válido        │
     │                        │                        │
     │                        │  Generar JWT           │
     │                        │                        │
     │  200 OK + JWT token    │                        │
     │<───────────────────────┤                        │
     │                        │                        │
     │  GET /tasks            │                        │
     │  Authorization: Bearer │                        │
     │  {token}               │                        │
     ├───────────────────────>│                        │
     │                        │  Validar JWT           │
     │                        │                        │
     │                        │  Obtener tareas        │
     │                        ├───────────────────────>│
     │                        │<───────────────────────┤
     │  200 OK + Lista tareas │                        │
     │<───────────────────────┤                        │
```

---

## 📡 Endpoints de la API

### Base URL

```
http://localhost:8080/api/v1
```

### 🔓 Autenticación (Endpoints Públicos)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/auth/register` | Registrar nuevo usuario | ❌ No |
| POST | `/auth/login` | Iniciar sesión | ❌ No |

### 📋 Tareas (Requieren Autenticación)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/tasks` | Listar todas las tareas (paginado) | ✅ Sí |
| GET | `/tasks/{id}` | Obtener tarea por ID | ✅ Sí |
| GET | `/tasks/status/{status}` | Filtrar por estado (paginado) | ✅ Sí |
| GET | `/tasks/search?title={texto}` | Buscar por título (paginado) | ✅ Sí |
| GET | `/tasks/statistics` | Obtener estadísticas | ✅ Sí |
| POST | `/tasks` | Crear nueva tarea | ✅ Sí |
| PUT | `/tasks/{id}` | Actualizar tarea completa | ✅ Sí |
| PATCH | `/tasks/{id}` | Actualizar parcialmente | ✅ Sí |
| DELETE | `/tasks/{id}` | Eliminar tarea | ✅ Sí |

### 🛡️ Rate Limiting Admin (Requieren Autenticación)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/admin/rate-limit/info` | Ver configuración | ✅ Sí |
| GET | `/admin/rate-limit/stats` | Ver estadísticas | ✅ Sí |
| POST | `/admin/rate-limit/clear-cache` | Limpiar caché | ✅ Sí |

### 📊 Actuator (Endpoints de Monitoreo)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/actuator/health` | Health check | ❌ No |
| GET | `/actuator/info` | Información de la app | ❌ No |
| GET | `/actuator/metrics` | Métricas generales | ✅ Sí |
| GET | `/actuator/prometheus` | Métricas Prometheus | ✅ Sí |

### Estados de Tareas

| Estado | Descripción |
|--------|-------------|
| `PENDING` | Tarea pendiente de iniciar |
| `IN_PROGRESS` | Tarea en progreso |
| `COMPLETED` | Tarea completada |
| `CANCELLED` | Tarea cancelada |

---

## 📮 Ejemplos con Postman

### 1. 🔐 Login (Obtener Token JWT)

**Endpoint:** `POST http://localhost:8080/api/v1/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Respuesta (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMDAwMDAwMCwiZXhwIjoxNzAwMDg2NDAwfQ.signature...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@taskmanagement.com",
  "roles": ["ROLE_ADMIN", "ROLE_USER"]
}
```

**⚠️ IMPORTANTE:** Copiar el valor de `token` para usarlo en las siguientes peticiones.

---

### 2. 📝 Crear Tarea

**Endpoint:** `POST http://localhost:8080/api/v1/tasks`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Body (raw JSON):**
```json
{
  "title": "Implementar autenticación JWT",
  "description": "Agregar Spring Security con JWT para autenticación stateless",
  "status": "IN_PROGRESS",
  "dueDate": "2025-12-31T23:59:59"
}
```

**Respuesta (201 CREATED):**
```json
{
  "id": 1,
  "title": "Implementar autenticación JWT",
  "description": "Agregar Spring Security con JWT para autenticación stateless",
  "status": "IN_PROGRESS",
  "dueDate": "2025-12-31T23:59:59",
  "createdAt": "2025-11-15T10:30:00",
  "updatedAt": "2025-11-15T10:30:00"
}
```

---

### 3. 📜 Listar Tareas (Con Paginación)

**Endpoint:** `GET http://localhost:8080/api/v1/tasks?page=0&size=20&sort=createdAt,desc`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Parámetros de Query:**
- `page`: Número de página (inicia en 0)
- `size`: Elementos por página
- `sort`: Campo y dirección de ordenamiento

**Respuesta (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Implementar autenticación JWT",
      "description": "...",
      "status": "IN_PROGRESS",
      "dueDate": "2025-12-31T23:59:59",
      "createdAt": "2025-11-15T10:30:00",
      "updatedAt": "2025-11-15T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "size": 20,
  "number": 0,
  "numberOfElements": 1,
  "empty": false
}
```

---

### 4. 🔍 Obtener Tarea por ID

**Endpoint:** `GET http://localhost:8080/api/v1/tasks/1`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Respuesta (200 OK):**
```json
{
  "id": 1,
  "title": "Implementar autenticación JWT",
  "description": "Agregar Spring Security con JWT para autenticación stateless",
  "status": "IN_PROGRESS",
  "dueDate": "2025-12-31T23:59:59",
  "createdAt": "2025-11-15T10:30:00",
  "updatedAt": "2025-11-15T10:30:00"
}
```

---

### 5. ✏️ Actualizar Tarea (PUT - Completa)

**Endpoint:** `PUT http://localhost:8080/api/v1/tasks/1`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Body (raw JSON):**
```json
{
  "title": "Implementar autenticación JWT - COMPLETADO",
  "description": "Spring Security con JWT implementado y testeado",
  "status": "COMPLETED",
  "dueDate": "2025-12-31T23:59:59"
}
```

---

### 6. 🔧 Actualizar Parcialmente (PATCH)

**Endpoint:** `PATCH http://localhost:8080/api/v1/tasks/1`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Body (raw JSON):**
```json
{
  "status": "COMPLETED"
}
```

**💡 Nota:** Solo actualiza el campo `status`, los demás permanecen igual.

---

### 7. 🗑️ Eliminar Tarea

**Endpoint:** `DELETE http://localhost:8080/api/v1/tasks/1`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Respuesta (204 NO CONTENT):**
```
(Sin contenido)
```

---

### 8. 🔎 Filtrar por Estado

**Endpoint:** `GET http://localhost:8080/api/v1/tasks/status/IN_PROGRESS?page=0&size=10`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

### 9. 🔍 Buscar por Título

**Endpoint:** `GET http://localhost:8080/api/v1/tasks/search?title=autenticación&page=0&size=10`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

### 10. 📊 Obtener Estadísticas

**Endpoint:** `GET http://localhost:8080/api/v1/tasks/statistics`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Respuesta (200 OK):**
```json
{
  "totalTasks": 42,
  "pendingTasks": 15,
  "inProgressTasks": 10,
  "completedTasks": 12,
  "cancelledTasks": 5
}
```

---

### 📋 Colección de Postman

Puedes importar esta colección completa en Postman:

1. Abrir Postman
2. Click en **Import**
3. Pegar este JSON o crear archivo `Task-Management-API.postman_collection.json`

**Variables de entorno recomendadas:**
```json
{
  "base_url": "http://localhost:8080/api/v1",
  "jwt_token": "tu-token-aqui"
}
```

---

## 📚 Swagger y Documentación

### Acceder a Swagger UI

La documentación interactiva está disponible en:

**URL:** http://localhost:8080/api/v1/swagger-ui/index.html

### OpenAPI JSON

Especificación OpenAPI 3.0:

**URL:** http://localhost:8080/api/v1/v3/api-docs

### Características de Swagger

✅ **Documentación interactiva** - Prueba endpoints directamente desde el navegador
✅ **Esquemas de datos** - Visualiza DTOs y modelos de datos
✅ **Ejemplos de peticiones** - Request y response bodies pre-configurados
✅ **Autorización integrada** - Botón "Authorize" para agregar JWT token
✅ **Códigos de estado** - Todas las respuestas posibles documentadas

### Cómo Usar Swagger

1. **Abrir Swagger UI** en el navegador
2. **Click en "Authorize"** (botón con candado)
3. **Ingresar:** `Bearer {tu-token-jwt}`
4. **Click en "Authorize"** y luego "Close"
5. **Probar endpoints** haciendo click en "Try it out"

---

## 📈 Monitoreo y Métricas

### Spring Boot Actuator

Endpoints de monitoreo disponibles:

#### Health Check
```bash
curl http://localhost:8080/api/v1/actuator/health
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
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760
      }
    }
  }
}
```

#### Información de la Aplicación
```bash
curl http://localhost:8080/api/v1/actuator/info
```

#### Métricas Generales
```bash
# Requiere autenticación
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/actuator/metrics
```

Métricas disponibles:
- `jvm.memory.used` - Memoria JVM utilizada
- `jvm.threads.live` - Threads activos
- `http.server.requests` - Estadísticas de peticiones HTTP
- `hikaricp.connections.active` - Conexiones activas del pool
- `system.cpu.usage` - Uso de CPU

#### Métricas de Prometheus
```bash
# Requiere autenticación
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/actuator/prometheus
```

### Integración con Prometheus

Agregar en `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: 'task-management-api'
    metrics_path: '/api/v1/actuator/prometheus'
    static_configs:
      - targets: ['localhost:8080']
```

### Dashboard con Grafana

Métricas recomendadas para dashboard:

- **JVM Memory Usage** - Uso de memoria heap/non-heap
- **HTTP Requests** - Tasa de peticiones por segundo
- **Response Times** - Latencia P50, P95, P99
- **Database Connections** - Pool HikariCP
- **Error Rate** - Porcentaje de errores 4xx/5xx
- **Rate Limiting** - Peticiones bloqueadas vs permitidas

---

## 🛡️ Rate Limiting

### Configuración

El sistema de Rate Limiting protege la API contra abuso y ataques DDoS.

**Configuración por defecto:**
- ✅ 100 peticiones por minuto por IP
- ✅ Algoritmo: Token Bucket
- ✅ Paths excluidos: `/actuator/**`, `/swagger-ui/**`

### Headers HTTP

Cada respuesta incluye headers informativos:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
```

### Respuesta cuando se excede el límite

**HTTP 429 Too Many Requests:**
```json
{
  "timestamp": "2025-11-15T10:30:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Has excedido el límite de peticiones. Intenta nuevamente más tarde.",
  "limit": 100,
  "retryAfter": "60 segundos"
}
```

### Configurar Rate Limiting

Editar `.env`:

```bash
# Habilitar/deshabilitar
RATE_LIMIT_ENABLED=true

# Límites
RATE_LIMIT_CAPACITY=100    # Burst máximo
RATE_LIMIT_TOKENS=100      # Tokens por período
RATE_LIMIT_PERIOD=1        # Minutos

# Por IP o global
RATE_LIMIT_PER_IP=true
```

**Documentación completa:** [RATE_LIMITING.md](RATE_LIMITING.md)

---

## 📝 Logging

### Niveles de Log

La aplicación utiliza **SLF4J con Logback**:

| Nivel | Descripción | Cuándo usar |
|-------|-------------|-------------|
| **TRACE** | Información muy detallada | Debugging profundo |
| **DEBUG** | Información de debugging | Desarrollo |
| **INFO** | Eventos informativos | General |
| **WARN** | Advertencias | Situaciones anormales |
| **ERROR** | Errores | Fallos en la aplicación |

### Configuración de Logs

**Variables de entorno:**
```bash
LOGGING_LEVEL_ROOT=INFO          # Nivel general
LOGGING_LEVEL_APP=DEBUG          # Nivel de la aplicación
```

**Logs por paquete:**
```yaml
logging:
  level:
    com.taskmanagement.api: DEBUG
    org.springframework.web: INFO
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

### Formato de Logs

```
2025-11-15 10:30:00 - [INFO ] com.taskmanagement.api.service.TaskServiceImpl - Creando nueva tarea con título: Implementar JWT
2025-11-15 10:30:01 - [DEBUG] org.hibernate.SQL - insert into tasks (title, description, status, created_at, updated_at) values (?, ?, ?, ?, ?)
2025-11-15 10:30:01 - [INFO ] com.taskmanagement.api.service.TaskServiceImpl - Tarea creada exitosamente con ID: 1
```

### Ver Logs en Tiempo Real

```bash
# Opción 1: Durante ejecución con Gradle
./gradlew bootRun

# Opción 2: Archivo de logs (si está configurado)
tail -f logs/task-management-api.log

# Opción 3: Docker logs
docker compose logs -f app
```

---

## ⚙️ Perfiles de Configuración

### Perfiles Disponibles

#### 1. Default (Desarrollo Local)

```bash
./gradlew bootRun
```

**Características:**
- Base de datos: `taskmanagement_db`
- DDL: `update` (actualiza esquema automáticamente)
- Logs: `DEBUG` para la aplicación
- Puerto: `8080`

#### 2. Dev (Desarrollo Activo)

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Características:**
- Base de datos: `taskmanagement_db_dev`
- DDL: `create-drop` (recrea esquema en cada inicio)
- Logs: `DEBUG` con SQL detallado
- Pool de conexiones: Reducido (5 conexiones)
- Detección agresiva de leaks

#### 3. Test (Testing)

```bash
./gradlew bootRun --args='--spring.profiles.active=test'
```

**Características:**
- Base de datos: `taskmanagement_db_test`
- DDL: `create-drop`
- Logs: `WARN` (reducidos)
- Pool de conexiones: Reducido (5 conexiones)
- Timeouts cortos

#### 4. Prod (Producción)

```bash
java -jar app.jar --spring.profiles.active=prod
```

**Características:**
- Base de datos: Desde variables de entorno
- DDL: `validate` (solo valida, NO modifica)
- Logs: `WARN` (mínimos)
- Configuración desde variables de entorno
- SSL habilitado (recomendado)

### Configurar Perfil

**Opción 1: Variable de entorno**
```bash
export SPRING_PROFILES_ACTIVE=prod
./gradlew bootRun
```

**Opción 2: Archivo .env**
```bash
SPRING_PROFILES_ACTIVE=dev
```

**Opción 3: Argumento al ejecutar**
```bash
java -jar app.jar --spring.profiles.active=prod
```

---

## 🔒 Seguridad

### Medidas de Seguridad Implementadas

#### 1. Autenticación JWT
- ✅ Tokens firmados con HS256
- ✅ Secret key configurable desde variables de entorno
- ✅ Expiración configurable (default: 24 horas)
- ✅ Stateless (no sesiones en servidor)

#### 2. Spring Security
- ✅ Autenticación basada en roles (RBAC)
- ✅ Endpoints públicos vs protegidos
- ✅ CSRF deshabilitado (API REST stateless)
- ✅ Session Management: STATELESS

#### 3. Encriptación de Contraseñas
- ✅ BCrypt con fuerza 10
- ✅ NUNCA se almacenan contraseñas en texto plano
- ✅ Salt aleatorio por cada contraseña

#### 4. Rate Limiting
- ✅ Protección contra ataques de fuerza bruta
- ✅ Prevención de DDoS
- ✅ Algoritmo Token Bucket

#### 5. CORS
- ✅ Configuración segura de orígenes permitidos
- ✅ Headers permitidos controlados
- ✅ Métodos HTTP específicos

#### 6. Validaciones
- ✅ Bean Validation en todos los DTOs
- ✅ Validación de negocio en servicios
- ✅ Sanitización de entrada

#### 7. Gestión de Secretos
- ✅ Variables de entorno para credenciales
- ✅ NUNCA secretos en código fuente
- ✅ `.env` en `.gitignore`

### Endpoints Públicos

Estos endpoints NO requieren autenticación:

- `/api/v1/auth/register`
- `/api/v1/auth/login`
- `/api/v1/swagger-ui/**`
- `/api/v1/v3/api-docs/**`
- `/api/v1/actuator/health`
- `/api/v1/actuator/info`

### Endpoints Protegidos

Todos los demás endpoints requieren:

```
Authorization: Bearer {jwt-token}
```

### Roles y Permisos

| Rol | Descripción | Permisos |
|-----|-------------|----------|
| `ROLE_USER` | Usuario normal | CRUD sobre sus propias tareas |
| `ROLE_ADMIN` | Administrador | CRUD sobre todas las tareas + endpoints admin |

**Documentación completa:** [SECRETS_MANAGEMENT.md](SECRETS_MANAGEMENT.md)

---

## 🗄️ Base de Datos

### Esquema de Base de Datos

#### Tabla: tasks

```sql
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices para optimización
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_created_at ON tasks(created_at DESC);
CREATE INDEX idx_tasks_title ON tasks USING GIN (to_tsvector('spanish', title));
```

#### Tabla: users

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,  -- Hash BCrypt
    full_name VARCHAR(100),
    enabled BOOLEAN DEFAULT true,
    account_non_locked BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### Tabla: roles

```sql
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);
```

#### Tabla: user_roles (Relación Many-to-Many)

```sql
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);
```

### Diagrama ER

```
┌─────────────┐
│    users    │
├─────────────┤
│ id (PK)     │
│ username    │
│ email       │
│ password    │
│ full_name   │
│ enabled     │
│ created_at  │
│ updated_at  │
└──────┬──────┘
       │
       │ 1:N
       │
┌──────┴──────┐
│ user_roles  │
├─────────────┤
│ user_id (FK)│
│ role_id (FK)│
└──────┬──────┘
       │
       │ N:1
       │
┌──────┴──────┐
│    roles    │
├─────────────┤
│ id (PK)     │
│ name        │
└─────────────┘

┌─────────────┐
│    tasks    │
├─────────────┤
│ id (PK)     │
│ title       │
│ description │
│ status      │
│ due_date    │
│ created_at  │
│ updated_at  │
└─────────────┘
```

### Pool de Conexiones HikariCP

**Configuración optimizada:**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # Máximo de conexiones
      minimum-idle: 10           # Mínimo idle
      connection-timeout: 20000  # 20 segundos
      idle-timeout: 300000       # 5 minutos
      max-lifetime: 1800000      # 30 minutos
```

**Monitorear conexiones:**
```bash
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/actuator/metrics/hikaricp.connections.active
```

**Documentación completa:** [POSTGRESQL_SETUP.md](POSTGRESQL_SETUP.md)

---

## 🧪 Testing

### Ejecutar Tests

```bash
# Todos los tests
./gradlew test

# Tests específicos
./gradlew test --tests TaskServiceImplTest

# Con cobertura
./gradlew test jacocoTestReport
```

### Estructura de Tests

```
src/test/java/com/taskmanagement/api/
├── service/
│   └── impl/
│       └── TaskServiceImplTest.java
├── controller/
│   └── TaskControllerTest.java
└── repository/
    └── TaskRepositoryTest.java
```

### Tipos de Tests

#### 1. Tests Unitarios
- Testean lógica de negocio aislada
- Usan mocks para dependencias
- Rápidos de ejecutar

#### 2. Tests de Integración
- Testean capas completas
- Usan base de datos de test
- TestContainers para PostgreSQL

#### 3. Tests de API
- Testean endpoints REST
- MockMvc para peticiones HTTP
- Validación de responses

---

## 🚀 Despliegue

### Docker

**Build:**
```bash
docker build -t task-management-api .
```

**Run:**
```bash
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/taskdb \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=postgres \
  -e JWT_SECRET=tu-secret-key \
  task-management-api
```

### Docker Compose

```bash
docker compose up -d
```

### Heroku

```bash
heroku create task-management-api
heroku addons:create heroku-postgresql:mini
heroku config:set JWT_SECRET=$(openssl rand -hex 64)
git push heroku main
```

### AWS Elastic Beanstalk

```bash
eb init -p docker task-management-api
eb create task-management-api-env
eb deploy
```

### Render.com

1. Conectar repositorio GitHub
2. Configurar variables de entorno
3. Deploy automático en cada push

---

## 🐛 Troubleshooting

### Error: No se puede conectar a PostgreSQL

**Problema:**
```
org.postgresql.util.PSQLException: Connection refused
```

**Solución:**
```bash
# Verificar que PostgreSQL está corriendo
docker compose ps

# Verificar credenciales en .env
cat .env | grep DATABASE

# Reiniciar PostgreSQL
docker compose restart postgres
```

### Error: Puerto 8080 en uso

**Problema:**
```
Web server failed to start. Port 8080 was already in use.
```

**Solución:**
```bash
# Opción 1: Matar proceso en puerto 8080
lsof -ti:8080 | xargs kill -9

# Opción 2: Cambiar puerto en .env
SERVER_PORT=8081
```

### Error: JWT Token inválido

**Problema:**
```
401 Unauthorized - Invalid JWT token
```

**Solución:**
1. Verificar que el token no haya expirado (24 horas)
2. Asegurarse de incluir "Bearer " antes del token
3. Verificar que `JWT_SECRET` es el mismo en toda la configuración
4. Hacer login nuevamente para obtener un token fresco

### Error: Rate Limiting bloqueando

**Problema:**
```
429 Too Many Requests
```

**Solución:**
```bash
# Opción 1: Aumentar límite temporalmente en .env
RATE_LIMIT_CAPACITY=500
RATE_LIMIT_TOKENS=500

# Opción 2: Deshabilitar rate limiting (solo desarrollo)
RATE_LIMIT_ENABLED=false

# Opción 3: Limpiar caché de rate limiting
curl -X POST -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/v1/admin/rate-limit/clear-cache
```

### Error: Out of Memory

**Problema:**
```
java.lang.OutOfMemoryError: Java heap space
```

**Solución:**
```bash
# Aumentar memoria JVM al ejecutar
java -Xmx1024m -Xms512m -jar app.jar

# O configurar en Gradle
GRADLE_OPTS="-Xmx1024m"
```

---

## 📚 Documentación Adicional

- 📖 [Configuración de PostgreSQL](POSTGRESQL_SETUP.md)
- 🔒 [Gestión de Secretos](SECRETS_MANAGEMENT.md)
- 🛡️ [Rate Limiting](RATE_LIMITING.md)
- 📝 [DTO Pattern](DTO_PATTERN.md)
- 🔍 [Auditoría](AUDITING.md)
- ✅ [Quality Checklist](QUALITY_CHECKLIST.md)

---

## 🤝 Contribuir

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

---

## 👤 Autor

**Tu Nombre**
- GitHub: [@tu-usuario](https://github.com/tu-usuario)
- LinkedIn: [tu-perfil](https://linkedin.com/in/tu-perfil)
- Email: tu-email@example.com

---

## 🙏 Agradecimientos

- Spring Boot Team por el excelente framework
- PostgreSQL por la base de datos robusta
- Comunidad open-source por las librerías utilizadas

---

**⭐ Si este proyecto te fue útil, considera darle una estrella en GitHub!**

---

<div align="center">
  <p>Hecho con ❤️ y ☕</p>
  <p>© 2025 Task Management API</p>
</div>
