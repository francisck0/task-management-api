# Configuración de PostgreSQL 18 para Task Management API

Este documento detalla la configuración de PostgreSQL 18 para el proyecto, explicando las decisiones de performance y seguridad.

## Tabla de Contenidos

- [Inicio Rápido](#inicio-rápido)
- [Configuración de Docker](#configuración-de-docker)
- [Configuración de Spring Boot](#configuración-de-spring-boot)
- [Pool de Conexiones (HikariCP)](#pool-de-conexiones-hikaricp)
- [Seguridad](#seguridad)
- [Performance y Optimización](#performance-y-optimización)
- [Monitoreo](#monitoreo)
- [Troubleshooting](#troubleshooting)

## Inicio Rápido

### 1. Levantar PostgreSQL 18 con Docker

```bash
# Copiar variables de entorno
cp .env.example .env

# Iniciar PostgreSQL
docker compose up -d

# Verificar que está corriendo
docker compose ps
```

### 2. Ejecutar la aplicación

```bash
./gradlew bootRun
```

La aplicación creará automáticamente las tablas necesarias en la base de datos.

## Configuración de Docker

### PostgreSQL 18

**¿Por qué PostgreSQL 18?**
- **Performance mejorado**: Mejor particionado y paralelización de queries
- **Nuevas características**: Window functions mejoradas, JSON path queries
- **Seguridad**: Parches de seguridad más recientes
- **Estabilidad**: Versión estable y madura

### Configuraciones Importantes

#### 1. Recursos del Contenedor

```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'      # Máximo 2 CPUs
      memory: 2G       # Máximo 2GB RAM
```

**Justificación:**
- Evita que PostgreSQL consuma todos los recursos del host
- En desarrollo, 2GB es suficiente para operaciones típicas
- En producción, ajustar según carga real

#### 2. Parámetros de PostgreSQL

##### shared_buffers (512MB)
```bash
-c shared_buffers=512MB
```
- **Qué hace**: Caché de datos en memoria
- **Recomendación**: 25% de RAM disponible
- **Impacto**: Mayor = menos lecturas de disco = mejor performance

##### effective_cache_size (1536MB)
```bash
-c effective_cache_size=1536MB
```
- **Qué hace**: Estimación de memoria disponible para caché del SO
- **Recomendación**: 50-75% de RAM total
- **Impacto**: Ayuda al planificador de queries a tomar mejores decisiones

##### work_mem (16MB)
```bash
-c work_mem=16MB
```
- **Qué hace**: Memoria para operaciones de ordenamiento y hash
- **Fórmula**: RAM / max_connections / 4
- **Impacto**: Reduce uso de disco temporal para sorts
- **Cuidado**: Se asigna POR operación, no total

##### maintenance_work_mem (128MB)
```bash
-c maintenance_work_mem=128MB
```
- **Qué hace**: Memoria para VACUUM, CREATE INDEX, ALTER TABLE
- **Recomendación**: 64MB - 1GB
- **Impacto**: Operaciones de mantenimiento más rápidas

##### max_connections (100)
```bash
-c max_connections=100
```
- **Qué hace**: Número máximo de conexiones simultáneas
- **Recomendación**: Debe coincidir con el pool de HikariCP
- **Cuidado**: Más conexiones = más memoria consumida

##### checkpoint_completion_target (0.9)
```bash
-c checkpoint_completion_target=0.9
```
- **Qué hace**: Distribuye escrituras de checkpoint en el tiempo
- **Rango**: 0.0 - 1.0
- **Impacto**: Reduce picos de I/O, mejora performance de escritura

##### random_page_cost (1.1)
```bash
-c random_page_cost=1.1
```
- **Qué hace**: Costo estimado de lecturas aleatorias
- **SSD**: 1.1 - 1.5
- **HDD**: 4.0 (default)
- **Impacto**: Afecta decisiones del query planner

##### effective_io_concurrency (200)
```bash
-c effective_io_concurrency=200
```
- **Qué hace**: Número de operaciones I/O concurrentes que el disco puede manejar
- **SSD**: 200-300
- **HDD**: 2-4
- **Impacto**: Mejora scans paralelos en SSD

## Configuración de Spring Boot

### application.yml

El archivo `application.yml` contiene tres perfiles:

#### 1. Default (Desarrollo Local)
- **ddl-auto**: `update` - Actualiza esquema sin borrar datos
- **show-sql**: `true` - Muestra queries para debugging
- **Pool size**: 20 conexiones

#### 2. Dev (Desarrollo Activo)
- **ddl-auto**: `create-drop` - Recrea esquema en cada inicio
- **Base de datos**: `taskmanagement_db_dev`
- **Pool size**: 5 conexiones (suficiente para desarrollo)

#### 3. Prod (Producción)
- **ddl-auto**: `validate` - Solo valida, NUNCA modifica esquema
- **show-sql**: `false` - No loguear queries
- **Credenciales**: Variables de entorno
- **SSL**: Habilitado (recomendado)

### Activar Perfiles

```bash
# Desarrollo
./gradlew bootRun --args='--spring.profiles.active=dev'

# Producción
java -jar app.jar --spring.profiles.active=prod
```

## Pool de Conexiones (HikariCP)

HikariCP es el pool de conexiones más rápido y eficiente para Java.

### Configuraciones Clave

#### maximum-pool-size (20)
```yaml
maximum-pool-size: 20
```

**¿Por qué 20?**
- Fórmula recomendada: `(core_count * 2) + effective_spindle_count`
- Para CPU de 8 cores: (8 * 2) + 1 = 17 ≈ 20
- **Más NO es mejor**: Demasiadas conexiones causan context switching

**¿Cuándo ajustar?**
- Monitor: Si siempre hay conexiones disponibles → reducir
- Monitor: Si hay timeouts frecuentes → aumentar (pero revisar queries primero)

#### minimum-idle (10)
```yaml
minimum-idle: 10
```

**Estrategias:**
- **Carga estable**: Igual a maximum-pool-size (evita crear/destruir conexiones)
- **Carga variable**: 50% de maximum-pool-size (ahorra recursos en idle)

#### connection-timeout (20000ms)
```yaml
connection-timeout: 20000
```

**Justificación:**
- 20 segundos es suficiente para obtener una conexión
- Si se alcanza, indica problema (pool agotado o BD lenta)
- Fallar rápido es mejor que colgar la aplicación

#### max-lifetime (1800000ms = 30min)
```yaml
max-lifetime: 1800000
```

**¿Por qué limitar vida de conexiones?**
- **Seguridad**: Previene uso de conexiones comprometidas
- **Firewalls**: Evita cierre por firewalls intermedios
- **Load balancers**: Permite redistribución de conexiones
- **Debe ser menor** que el timeout de PostgreSQL (2 horas por defecto)

#### leak-detection-threshold (60000ms)
```yaml
leak-detection-threshold: 60000
```

**En desarrollo:**
- Habilitado (30-60 segundos)
- Detecta conexiones no cerradas (connection leaks)
- Si se detecta: revisar que todos los métodos de servicio tengan `@Transactional`

**En producción:**
- Deshabilitado (0)
- Pequeño overhead de performance
- Los leaks deberían estar resueltos en desarrollo

## Seguridad

### 1. Credenciales

#### ❌ NO HACER:
```yaml
# Hardcodear en application.yml
username: postgres
password: supersecret123
```

#### ✅ HACER:
```yaml
# Usar variables de entorno
username: ${DATABASE_USERNAME}
password: ${DATABASE_PASSWORD}
```

```bash
# Generar contraseña segura
openssl rand -base64 32
```

### 2. SSL/TLS

#### En Producción (OBLIGATORIO):

**docker-compose.yml:**
```yaml
environment:
  POSTGRES_INITDB_ARGS: "--data-checksums"
  # Montar certificados SSL
volumes:
  - ./ssl/server.crt:/var/lib/postgresql/server.crt
  - ./ssl/server.key:/var/lib/postgresql/server.key
command:
  - "postgres"
  - "-c"
  - "ssl=on"
  - "-c"
  - "ssl_cert_file=/var/lib/postgresql/server.crt"
  - "-c"
  - "ssl_key_file=/var/lib/postgresql/server.key"
```

**application.yml:**
```yaml
datasource:
  url: jdbc:postgresql://host:5432/db?ssl=true&sslmode=require
```

### 3. Usuario con Privilegios Mínimos

#### ❌ NO usar 'postgres' en producción

#### ✅ Crear usuario específico:
```sql
-- Conectar como postgres
CREATE USER taskmanager_app WITH PASSWORD 'strong_password_here';
CREATE DATABASE taskmanagement_db OWNER taskmanager_app;

-- Otorgar solo permisos necesarios
GRANT CONNECT ON DATABASE taskmanagement_db TO taskmanager_app;
GRANT USAGE ON SCHEMA public TO taskmanager_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO taskmanager_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO taskmanager_app;

-- Para tablas futuras
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO taskmanager_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO taskmanager_app;
```

### 4. pg_hba.conf

Configurar acceso por IP:

```
# Desarrollo (localhost)
host    all    all    127.0.0.1/32    md5

# Producción (red específica)
hostssl all    taskmanager_app    10.0.1.0/24    scram-sha-256
```

## Performance y Optimización

### 1. Índices

Spring Boot/Hibernate NO crea índices automáticamente (excepto PKs).

**Crear índices para queries frecuentes:**

```sql
-- Índice para búsqueda por estado (usado en findByStatus)
CREATE INDEX idx_tasks_status ON tasks(status);

-- Índice para búsqueda por título (usado en findByTitleContaining)
CREATE INDEX idx_tasks_title ON tasks USING gin(to_tsvector('spanish', title));

-- Índice para ordenamiento por fecha
CREATE INDEX idx_tasks_created_at ON tasks(created_at DESC);

-- Índice compuesto si se filtra por estado Y fecha
CREATE INDEX idx_tasks_status_created ON tasks(status, created_at DESC);
```

### 2. Batch Processing

**En TaskServiceImpl:**
```yaml
# Ya configurado en application.yml
hibernate:
  jdbc:
    batch_size: 20
    order_inserts: true
    order_updates: true
```

**Uso en código:**
```java
// Guardar múltiples tareas eficientemente
@Transactional
public void saveMultipleTasks(List<Task> tasks) {
    int batchSize = 20;
    for (int i = 0; i < tasks.size(); i++) {
        taskRepository.save(tasks.get(i));
        if (i > 0 && i % batchSize == 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }
}
```

### 3. Paginación

**Implementar para listas grandes:**

```java
// En TaskRepository
Page<Task> findByStatus(TaskStatus status, Pageable pageable);

// En TaskController
@GetMapping
public ResponseEntity<Page<TaskResponseDto>> getAllTasks(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PageRequest.of(page, size,
        Sort.by("createdAt").descending());
    // ...
}
```

### 4. N+1 Query Problem

**Problema:**
```java
// Esto genera N+1 queries si Task tiene relaciones
List<Task> tasks = taskRepository.findAll();
tasks.forEach(task -> {
    task.getUser().getName(); // Query adicional por cada task
});
```

**Solución:**
```java
@Query("SELECT t FROM Task t LEFT JOIN FETCH t.user")
List<Task> findAllWithUser();
```

## Monitoreo

### 1. HikariCP JMX

**Habilitar en application.yml:**
```yaml
hikari:
  register-mbeans: true
```

**Visualizar con JConsole:**
```bash
# Obtener PID de la aplicación
jps

# Conectar JConsole
jconsole <PID>
```

**Métricas clave:**
- Active connections
- Idle connections
- Pending threads
- Connection creation time

### 2. Spring Boot Actuator

**Agregar dependencia:**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

**Configurar en application.yml:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,hikaricp
```

**Consultar métricas:**
```bash
# Health check
curl http://localhost:8080/api/v1/actuator/health

# Métricas de HikariCP
curl http://localhost:8080/api/v1/actuator/metrics/hikaricp.connections
```

### 3. PostgreSQL Queries Lentas

**Ver queries lentas:**
```sql
-- Habilitar logging de queries lentas (ya está en docker-compose.yml)
-- log_min_duration_statement = 1000ms

-- Ver queries más lentas
SELECT query, mean_exec_time, calls
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;
```

**Analizar query específica:**
```sql
EXPLAIN ANALYZE
SELECT * FROM tasks WHERE status = 'PENDING';
```

## Troubleshooting

### Error: "Connection refused"

**Síntomas:**
```
org.postgresql.util.PSQLException: Connection to localhost:5432 refused
```

**Solución:**
```bash
# Verificar que PostgreSQL está corriendo
docker compose ps

# Ver logs de PostgreSQL
docker compose logs postgres

# Reiniciar PostgreSQL
docker compose restart postgres
```

### Error: "Pool exhausted"

**Síntomas:**
```
HikariPool - Connection is not available, request timed out after 20000ms
```

**Causas:**
1. **Connection leaks** - Conexiones no cerradas
2. **Pool muy pequeño** - Aumentar maximum-pool-size
3. **Queries lentas** - Optimizar queries

**Diagnóstico:**
```bash
# Ver conexiones activas en PostgreSQL
docker compose exec postgres psql -U postgres -d taskmanagement_db -c \
  "SELECT count(*) FROM pg_stat_activity WHERE datname = 'taskmanagement_db';"

# Si el número es cercano a maximum-pool-size, aumentar pool
# Si es mucho menor, hay connection leaks
```

**Solución connection leaks:**
- Asegurar que todos los métodos de servicio tengan `@Transactional`
- Revisar leak-detection-threshold en logs

### Error: "Too many connections"

**Síntomas:**
```
FATAL: sorry, too many clients already
```

**Solución:**
```bash
# Aumentar max_connections en PostgreSQL
docker compose down
# Editar docker-compose.yml:
# -c max_connections=200

docker compose up -d
```

### Performance Degradado

**Diagnóstico:**

```sql
-- Ver tablas que necesitan VACUUM
SELECT schemaname, tablename, last_vacuum, last_autovacuum
FROM pg_stat_user_tables
ORDER BY last_autovacuum NULLS FIRST;

-- Ejecutar VACUUM si es necesario
VACUUM ANALYZE tasks;

-- Ver índices no utilizados
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
WHERE idx_scan = 0 AND indexname NOT LIKE 'pg_toast%';
```

## Checklist de Producción

Antes de ir a producción, verificar:

- [ ] Cambiar todas las contraseñas por defecto
- [ ] Habilitar SSL/TLS
- [ ] Configurar usuario de BD con privilegios mínimos
- [ ] Configurar pg_hba.conf apropiadamente
- [ ] Ajustar pool de conexiones según carga real
- [ ] Implementar backups automáticos
- [ ] Configurar monitoreo y alertas
- [ ] Probar plan de recuperación ante desastres
- [ ] Documentar configuración específica de producción
- [ ] Configurar rotación de logs
- [ ] Implementar rate limiting
- [ ] Auditar accesos a la BD
- [ ] Usar variables de entorno para credenciales
- [ ] Configurar firewalls apropiadamente
- [ ] Probar performance bajo carga (load testing)

## Recursos Adicionales

- [PostgreSQL 18 Documentation](https://www.postgresql.org/docs/18/)
- [HikariCP Configuration Guide](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [PostgreSQL Performance Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)
- [Spring Boot Database Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html#data.sql.datasource)
