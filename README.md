# Task Management API

API REST para gestión de tareas desarrollada con Spring Boot 3.5.7 y Java 21.

## Tecnologías Utilizadas

- **Java 21** - Última versión LTS de Java
- **Spring Boot 3.5.7** - Framework principal
- **Spring Data JPA** - Capa de persistencia
- **PostgreSQL 18** - Base de datos relacional de última generación
- **HikariCP** - Pool de conexiones de alto rendimiento
- **Lombok** - Reducción de código boilerplate
- **Gradle** - Herramienta de construcción
- **Bean Validation** - Validación de datos
- **Docker & Docker Compose** - Contenedorización

## Arquitectura del Proyecto

El proyecto sigue una **arquitectura en capas** (Layered Architecture), que separa las responsabilidades en diferentes capas:

```
src/main/java/com/taskmanagement/api/
├── controller/          # Capa de Presentación - Endpoints REST
├── service/            # Capa de Negocio - Lógica de negocio
│   └── impl/          # Implementaciones de servicios
├── repository/         # Capa de Persistencia - Acceso a datos
├── model/             # Capa de Dominio - Entidades JPA
├── dto/               # Data Transfer Objects - Transferencia de datos
├── config/            # Configuraciones de Spring
└── exception/         # Manejo de excepciones personalizado
```

### Explicación de Capas

#### 1. Controller (Capa de Presentación)
- **Responsabilidad**: Recibir peticiones HTTP y devolver respuestas
- **Características**:
  - Endpoints REST con métodos HTTP (GET, POST, PUT, DELETE)
  - Validación de entrada con `@Valid`
  - Delegación de lógica a la capa de servicio
  - Manejo de códigos de estado HTTP apropiados

#### 2. Service (Capa de Negocio)
- **Responsabilidad**: Implementar la lógica de negocio
- **Características**:
  - Transacciones con `@Transactional`
  - Validaciones de negocio
  - Coordinación entre repositorios
  - Mapeo entre entidades y DTOs

#### 3. Repository (Capa de Persistencia)
- **Responsabilidad**: Acceso a la base de datos
- **Características**:
  - Extiende `JpaRepository` para operaciones CRUD
  - Query methods derivados de nombres de métodos
  - Abstracción de la base de datos

#### 4. Model (Capa de Dominio)
- **Responsabilidad**: Representar entidades del dominio
- **Características**:
  - Anotaciones JPA (`@Entity`, `@Table`, `@Column`)
  - Mapeo con tablas de la base de datos
  - Relaciones entre entidades

#### 5. DTO (Data Transfer Objects)
- **Responsabilidad**: Transferir datos entre capas
- **Características**:
  - Desacoplamiento del modelo de dominio
  - Validaciones con Bean Validation
  - Control de datos expuestos en la API

#### 6. Exception (Manejo de Excepciones)
- **Responsabilidad**: Gestión centralizada de errores
- **Características**:
  - `@RestControllerAdvice` para manejo global
  - Respuestas de error consistentes
  - Logging de errores

## Estructura de la Base de Datos

### Tabla: tasks

| Campo       | Tipo           | Descripción                          |
|-------------|----------------|--------------------------------------|
| id          | BIGSERIAL      | Primary Key, auto-incremental        |
| title       | VARCHAR(100)   | Título de la tarea (obligatorio)     |
| description | TEXT           | Descripción detallada (opcional)     |
| status      | VARCHAR(20)    | Estado: PENDING, IN_PROGRESS, etc.   |
| due_date    | TIMESTAMP      | Fecha límite (opcional)              |
| created_at  | TIMESTAMP      | Fecha de creación (auto)             |
| updated_at  | TIMESTAMP      | Fecha de actualización (auto)        |

## Requisitos Previos

- Java 21 JDK instalado
- Docker y Docker Compose (recomendado)
- O PostgreSQL 18 instalado localmente
- Gradle 8.x (incluido en el wrapper)

## Configuración de la Base de Datos

### Opción 1: Docker Compose (Recomendado)

La forma más fácil de ejecutar PostgreSQL 18 con configuración optimizada:

```bash
# Copiar variables de entorno
cp .env.example .env

# Iniciar PostgreSQL 18 + pgAdmin
docker compose up -d

# Verificar que está corriendo
docker compose ps
```

**Incluye:**
- PostgreSQL 18 con configuración optimizada de performance
- pgAdmin 4 (interfaz web en http://localhost:5050)
- Volúmenes persistentes
- Healthchecks automáticos

**Documentación detallada:** Ver [POSTGRESQL_SETUP.md](POSTGRESQL_SETUP.md)

### Opción 2: PostgreSQL Local

Si prefieres instalar PostgreSQL localmente:

1. **Instalar PostgreSQL 18**

2. **Crear la base de datos**:
```sql
CREATE DATABASE taskmanagement_db;
```

3. **Configurar credenciales** en `.env`:
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/taskmanagement_db
DATABASE_USERNAME=tu_usuario
DATABASE_PASSWORD=tu_contraseña
```

## Instalación y Ejecución

### 1. Clonar o descargar el proyecto

### 2. Compilar el proyecto
```bash
./gradlew build
```

### 3. Ejecutar la aplicación
```bash
./gradlew bootRun
```

O usando el JAR generado:
```bash
java -jar build/libs/task-management-api-0.0.1-SNAPSHOT.jar
```

### 4. Verificar que la aplicación está corriendo

La aplicación estará disponible en: **http://localhost:8080/api/v1**

## API Endpoints

Base URL: `http://localhost:8080/api/v1`

### Tareas

| Método | Endpoint                  | Descripción                          |
|--------|---------------------------|--------------------------------------|
| GET    | /tasks                    | Obtener todas las tareas             |
| GET    | /tasks/{id}               | Obtener una tarea por ID             |
| GET    | /tasks/status/{status}    | Obtener tareas por estado            |
| GET    | /tasks/search?title={txt} | Buscar tareas por título             |
| GET    | /tasks/statistics         | Obtener estadísticas de tareas       |
| POST   | /tasks                    | Crear una nueva tarea                |
| PUT    | /tasks/{id}               | Actualizar una tarea existente       |
| DELETE | /tasks/{id}               | Eliminar una tarea                   |

### Ejemplos de Uso con cURL

#### Crear una tarea
```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Completar proyecto",
    "description": "Finalizar el desarrollo de la API",
    "status": "PENDING",
    "dueDate": "2025-12-31T23:59:59"
  }'
```

#### Obtener todas las tareas
```bash
curl http://localhost:8080/api/v1/tasks
```

#### Obtener una tarea específica
```bash
curl http://localhost:8080/api/v1/tasks/1
```

#### Actualizar una tarea
```bash
curl -X PUT http://localhost:8080/api/v1/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Completar proyecto - ACTUALIZADO",
    "description": "Finalizar el desarrollo de la API REST",
    "status": "IN_PROGRESS",
    "dueDate": "2025-12-31T23:59:59"
  }'
```

#### Eliminar una tarea
```bash
curl -X DELETE http://localhost:8080/api/v1/tasks/1
```

#### Buscar por estado
```bash
curl http://localhost:8080/api/v1/tasks/status/PENDING
```

#### Buscar por título
```bash
curl http://localhost:8080/api/v1/tasks/search?title=proyecto
```

#### Obtener estadísticas
```bash
curl http://localhost:8080/api/v1/tasks/statistics
```

## Estados de Tareas

- `PENDING` - Tarea pendiente de iniciar
- `IN_PROGRESS` - Tarea en progreso
- `COMPLETED` - Tarea completada
- `CANCELLED` - Tarea cancelada

## Formato de Respuestas

### Respuesta exitosa (Tarea)
```json
{
  "id": 1,
  "title": "Completar proyecto",
  "description": "Finalizar el desarrollo de la API",
  "status": "PENDING",
  "dueDate": "2025-12-31T23:59:59",
  "createdAt": "2025-11-12T10:30:00",
  "updatedAt": "2025-11-12T10:30:00"
}
```

### Respuesta de error
```json
{
  "timestamp": "2025-11-12T10:35:00",
  "status": 404,
  "error": "Not Found",
  "message": "Tarea no encontrada con ID: 999",
  "path": "/api/v1/tasks/999"
}
```

### Respuesta de validación
```json
{
  "timestamp": "2025-11-12T10:40:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los datos proporcionados",
  "path": "/api/v1/tasks",
  "errors": [
    "title: El título es obligatorio",
    "status: El estado es obligatorio"
  ]
}
```

## Perfiles de Configuración

El proyecto incluye tres perfiles:

### Default (Desarrollo local)
```bash
./gradlew bootRun
```

### Perfil dev
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```
- Base de datos: `taskmanagement_db_dev`
- DDL: `create-drop` (recrea el esquema en cada inicio)

### Perfil prod
```bash
java -jar app.jar --spring.profiles.active=prod
```
- Configuración desde variables de entorno
- DDL: `validate` (solo valida, no modifica)
- Logs reducidos

## Características Implementadas

- ✅ Operaciones CRUD completas
- ✅ Validación de datos con Bean Validation
- ✅ Manejo centralizado de excepciones
- ✅ Respuestas de error estandarizadas
- ✅ Logging con SLF4J
- ✅ Configuración de CORS
- ✅ Múltiples perfiles (dev, prod)
- ✅ Timestamps automáticos (createdAt, updatedAt)
- ✅ Búsqueda y filtrado de tareas
- ✅ Estadísticas de tareas

## Mejoras Futuras (Opcionales)

- [ ] Autenticación y autorización con Spring Security
- [ ] Documentación con Swagger/OpenAPI
- [ ] Pruebas unitarias y de integración
- [ ] Paginación y ordenamiento
- [ ] Auditoría con Spring Data JPA Auditing
- [ ] Cache con Redis
- [ ] Containerización con Docker
- [ ] CI/CD pipeline

## Buenas Prácticas Implementadas

1. **Separación de responsabilidades**: Arquitectura en capas bien definida
2. **DTOs para desacoplamiento**: No exponer entidades directamente
3. **Validación en múltiples niveles**: Bean Validation + validaciones de negocio
4. **Manejo robusto de errores**: Excepciones personalizadas y GlobalExceptionHandler
5. **Inyección de dependencias por constructor**: Inmutabilidad y testabilidad
6. **Logging apropiado**: Información útil para debugging y monitoreo
7. **Configuración externalizada**: application.yml con múltiples perfiles
8. **Código autodocumentado**: Comentarios explicativos en todo el código
9. **Uso de Lombok**: Reducción de código boilerplate
10. **Convenciones de Spring**: Nombres y patrones estándar

## Solución de Problemas

### Error de conexión a PostgreSQL
- Verificar que PostgreSQL esté corriendo
- Comprobar credenciales en `application.yml`
- Verificar que la base de datos existe

### Puerto 8080 en uso
Cambiar el puerto en `application.yml`:
```yaml
server:
  port: 8081
```

### Errores de compilación
Limpiar y recompilar:
```bash
./gradlew clean build
```

## Contacto y Soporte

Para preguntas, problemas o sugerencias, por favor abre un issue en el repositorio.

## Licencia

Este proyecto es de código abierto y está disponible bajo la licencia MIT.
