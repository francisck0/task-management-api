# Estructura del Proyecto

## Árbol de Directorios

```
task-project/
│
├── gradle/                          # Gradle Wrapper files
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
│
├── src/
│   ├── main/
│   │   ├── java/com/taskmanagement/api/
│   │   │   │
│   │   │   ├── TaskManagementApiApplication.java    # Clase principal de Spring Boot
│   │   │   │
│   │   │   ├── controller/                          # Capa de Presentación
│   │   │   │   └── TaskController.java              # Controlador REST de tareas
│   │   │   │
│   │   │   ├── service/                             # Capa de Negocio
│   │   │   │   ├── TaskService.java                 # Interfaz del servicio
│   │   │   │   ├── TaskStatisticsDto.java           # DTO para estadísticas
│   │   │   │   └── impl/
│   │   │   │       └── TaskServiceImpl.java         # Implementación del servicio
│   │   │   │
│   │   │   ├── repository/                          # Capa de Persistencia
│   │   │   │   └── TaskRepository.java              # Repositorio JPA de tareas
│   │   │   │
│   │   │   ├── model/                               # Capa de Dominio
│   │   │   │   ├── Task.java                        # Entidad Task (tabla tasks)
│   │   │   │   └── TaskStatus.java                  # Enum para estados de tareas
│   │   │   │
│   │   │   ├── dto/                                 # Data Transfer Objects
│   │   │   │   ├── TaskRequestDto.java              # DTO para crear/actualizar tareas
│   │   │   │   └── TaskResponseDto.java             # DTO para respuestas de tareas
│   │   │   │
│   │   │   ├── exception/                           # Manejo de Excepciones
│   │   │   │   ├── ResourceNotFoundException.java   # Excepción personalizada
│   │   │   │   ├── ErrorResponse.java               # DTO para respuestas de error
│   │   │   │   └── GlobalExceptionHandler.java      # Manejador global de excepciones
│   │   │   │
│   │   │   └── config/                              # Configuraciones
│   │   │       ├── CorsConfig.java                  # Configuración de CORS
│   │   │       └── OpenApiConfig.java               # Configuración de Swagger (preparada)
│   │   │
│   │   └── resources/
│   │       └── application.yml                      # Configuración de la aplicación
│   │
│   └── test/
│       └── java/com/taskmanagement/api/
│           └── TaskManagementApiApplicationTests.java  # Test básico
│
├── build.gradle                     # Configuración de Gradle y dependencias
├── settings.gradle                  # Configuración del proyecto Gradle
├── gradlew                          # Script de Gradle Wrapper (Unix/Linux/Mac)
├── gradlew.bat                      # Script de Gradle Wrapper (Windows)
│
├── docker-compose.yml               # Configuración de Docker para PostgreSQL
├── .env.example                     # Ejemplo de variables de entorno
├── .gitignore                       # Archivos ignorados por Git
│
├── README.md                        # Documentación completa del proyecto
├── QUICKSTART.md                    # Guía de inicio rápido
└── PROJECT_STRUCTURE.md             # Este archivo
```

## Estadísticas del Proyecto

- **Total de clases Java**: 16
- **Total de archivos de configuración**: 4
- **Total de archivos de documentación**: 3

## Desglose por Capa

### Controller (1 clase)
- TaskController: 8 endpoints REST

### Service (3 clases)
- TaskService: Interfaz con 8 métodos
- TaskServiceImpl: Implementación completa
- TaskStatisticsDto: DTO auxiliar

### Repository (1 clase)
- TaskRepository: 5 query methods personalizados

### Model (2 clases)
- Task: Entidad principal con 7 campos
- TaskStatus: Enum con 4 estados

### DTO (2 clases)
- TaskRequestDto: Validaciones incluidas
- TaskResponseDto: Respuesta completa

### Exception (3 clases)
- ResourceNotFoundException: Excepción personalizada
- ErrorResponse: DTO de error
- GlobalExceptionHandler: 4 handlers de excepciones

### Config (2 clases)
- CorsConfig: Configuración de CORS
- OpenApiConfig: Preparada para Swagger

## Convenciones de Nomenclatura

- **Clases**: PascalCase (ej: TaskController)
- **Métodos**: camelCase (ej: getAllTasks)
- **Constantes**: UPPER_SNAKE_CASE
- **Paquetes**: lowercase (ej: com.taskmanagement.api)
- **DTOs**: Sufijo "Dto" (ej: TaskRequestDto)
- **Implementaciones**: Sufijo "Impl" en paquete impl/

## Dependencias Principales

1. **Spring Boot Starter Web** - API REST
2. **Spring Boot Starter Data JPA** - Persistencia
3. **Spring Boot Starter Validation** - Validaciones
4. **PostgreSQL Driver** - Conector de BD
5. **Lombok** - Reducción de boilerplate
6. **Spring Boot Starter Test** - Testing

## Endpoints Disponibles

| # | Método | Endpoint | Descripción |
|---|--------|----------|-------------|
| 1 | POST | /api/v1/tasks | Crear tarea |
| 2 | GET | /api/v1/tasks | Listar todas |
| 3 | GET | /api/v1/tasks/{id} | Obtener por ID |
| 4 | GET | /api/v1/tasks/status/{status} | Filtrar por estado |
| 5 | GET | /api/v1/tasks/search?title={texto} | Buscar por título |
| 6 | GET | /api/v1/tasks/statistics | Obtener estadísticas |
| 7 | PUT | /api/v1/tasks/{id} | Actualizar tarea |
| 8 | DELETE | /api/v1/tasks/{id} | Eliminar tarea |

## Próximos Pasos Sugeridos

1. Ejecutar `./gradlew build` para compilar
2. Iniciar PostgreSQL con `docker-compose up -d`
3. Ejecutar la aplicación con `./gradlew bootRun`
4. Probar los endpoints con cURL o Postman
5. Agregar pruebas unitarias y de integración
6. Implementar autenticación con Spring Security
7. Agregar documentación Swagger
