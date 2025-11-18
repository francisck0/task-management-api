-- ============================================================================
-- SCRIPTS ÚTILES DE POSTGRESQL - TASK MANAGEMENT API
-- ============================================================================
--
-- Este archivo contiene scripts SQL útiles para gestionar y monitorear
-- la base de datos PostgreSQL del proyecto.
--
-- Uso:
-- 1. Conectar a la BD: docker compose exec postgres psql -U postgres -d taskmanagement_db
-- 2. Ejecutar desde archivo: docker compose exec -T postgres psql -U postgres -d taskmanagement_db < postgres-utils.sql
-- 3. Copiar/pegar comandos individuales según necesidad
--
-- ============================================================================

-- ============================================================================
-- INFORMACIÓN BÁSICA
-- ============================================================================

-- Ver versión de PostgreSQL
SELECT version();

-- Ver bases de datos disponibles
\l

-- Ver tamaño de la base de datos actual
SELECT pg_size_pretty(pg_database_size(current_database())) as db_size;

-- Ver todas las tablas y sus tamaños
SELECT
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) AS table_size,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) -
                   pg_relation_size(schemaname||'.'||tablename)) AS external_size
FROM pg_tables
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

-- Ver estructura de la tabla tasks
\d tasks

-- ============================================================================
-- MONITOREO DE CONEXIONES
-- ============================================================================

-- Ver conexiones activas
SELECT
    pid,
    usename,
    application_name,
    client_addr,
    state,
    query_start,
    state_change,
    query
FROM pg_stat_activity
WHERE datname = 'taskmanagement_db'
ORDER BY query_start DESC;

-- Contar conexiones por estado
SELECT
    state,
    COUNT(*) as count
FROM pg_stat_activity
WHERE datname = 'taskmanagement_db'
GROUP BY state;

-- Ver conexiones idle (inactivas)
SELECT
    pid,
    usename,
    state,
    state_change,
    now() - state_change as idle_time
FROM pg_stat_activity
WHERE datname = 'taskmanagement_db'
  AND state = 'idle'
ORDER BY state_change;

-- Terminar una conexión específica (usar con cuidado)
-- SELECT pg_terminate_backend(pid) WHERE pid = <pid_number>;

-- Terminar todas las conexiones idle por más de 30 minutos
-- SELECT pg_terminate_backend(pid)
-- FROM pg_stat_activity
-- WHERE datname = 'taskmanagement_db'
--   AND state = 'idle'
--   AND state_change < now() - interval '30 minutes';

-- ============================================================================
-- MONITOREO DE QUERIES
-- ============================================================================

-- Ver queries en ejecución
SELECT
    pid,
    now() - query_start as duration,
    state,
    query
FROM pg_stat_activity
WHERE state != 'idle'
  AND datname = 'taskmanagement_db'
ORDER BY duration DESC;

-- Ver queries lentas (más de 1 segundo)
SELECT
    pid,
    now() - query_start as duration,
    state,
    LEFT(query, 100) as query_preview
FROM pg_stat_activity
WHERE state != 'idle'
  AND datname = 'taskmanagement_db'
  AND now() - query_start > interval '1 second'
ORDER BY duration DESC;

-- Cancelar una query específica (sin terminar conexión)
-- SELECT pg_cancel_backend(pid) WHERE pid = <pid_number>;

-- ============================================================================
-- ESTADÍSTICAS DE TABLAS
-- ============================================================================

-- Ver estadísticas de acceso a tablas
SELECT
    schemaname,
    tablename,
    seq_scan,                    -- Scans secuenciales
    seq_tup_read,                -- Filas leídas en scans secuenciales
    idx_scan,                    -- Scans usando índices
    idx_tup_fetch,               -- Filas obtenidas por índices
    n_tup_ins,                   -- Inserts
    n_tup_upd,                   -- Updates
    n_tup_del,                   -- Deletes
    n_live_tup,                  -- Filas vivas estimadas
    n_dead_tup,                  -- Filas muertas (necesitan VACUUM)
    last_vacuum,
    last_autovacuum
FROM pg_stat_user_tables
WHERE schemaname = 'public'
ORDER BY seq_scan + idx_scan DESC;

-- Ver tablas que necesitan VACUUM urgente
SELECT
    schemaname,
    tablename,
    n_live_tup,
    n_dead_tup,
    ROUND(100.0 * n_dead_tup / NULLIF(n_live_tup + n_dead_tup, 0), 2) as dead_ratio,
    last_autovacuum
FROM pg_stat_user_tables
WHERE n_dead_tup > 1000
  AND schemaname = 'public'
ORDER BY n_dead_tup DESC;

-- ============================================================================
-- ÍNDICES
-- ============================================================================

-- Ver todos los índices de la tabla tasks
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'tasks'
ORDER BY indexname;

-- Ver todos los índices de la tabla users
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'users'
ORDER BY indexname;

-- Ver uso de índices
SELECT
    schemaname,
    tablename,
    indexname,
    idx_scan,                    -- Número de veces usado
    idx_tup_read,                -- Filas leídas
    idx_tup_fetch                -- Filas obtenidas
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY idx_scan DESC;

-- Ver índices NO utilizados (candidatos para eliminar)
SELECT
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) as index_size
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND schemaname = 'public'
  AND indexname NOT LIKE 'pg_toast%'
ORDER BY pg_relation_size(indexrelid) DESC;

-- ============================================================================
-- INFORMACIÓN SOBRE ÍNDICES ACTUALES
-- ============================================================================
--
-- ÍNDICES CREADOS AUTOMÁTICAMENTE POR HIBERNATE (desde anotaciones JPA):
-- -----------------------------------------------------------------------
-- Tabla: tasks
--   1. idx_task_status (status)
--   2. idx_task_user_id (user_id)
--   3. idx_task_due_date (due_date)
--   4. idx_task_created_at (created_at DESC)
--   5. idx_task_status_created (status, created_at DESC)
--
-- Tabla: users
--   1. UK_username (username) - Índice único automático
--   2. UK_email (email) - Índice único automático
--
-- ÍNDICES CREADOS POR SCHEMA.SQL (índices avanzados):
-- ----------------------------------------------------
-- Tabla: tasks
--   1. idx_task_title_fulltext (GIN para búsqueda de texto)
--   2. idx_task_due_date_partial (Índice parcial con WHERE)
--
-- Tabla: users
--   1. idx_user_enabled (Índice parcial para usuarios activos)
--
-- Tabla: user_roles
--   1. idx_user_roles_composite (user_id, role_id)
--
-- ============================================================================
-- COMANDOS PARA CREAR/RECREAR ÍNDICES MANUALMENTE (si es necesario)
-- ============================================================================
--
-- IMPORTANTE: Estos índices normalmente se crean automáticamente.
-- Solo ejecutar manualmente si:
-- 1. Necesitas recrear un índice corrupto
-- 2. Estás migrando desde una BD sin índices
-- 3. Quieres crear índices en una BD de producción existente
--
-- ============================================================================

-- ÍNDICES BÁSICOS (normalmente creados por Hibernate)
-- ----------------------------------------------------

-- Tabla TASKS - Índice para búsqueda por estado
-- CREATE INDEX IF NOT EXISTS idx_task_status ON tasks(status);

-- Tabla TASKS - Índice crítico para ownership verification
-- CREATE INDEX IF NOT EXISTS idx_task_user_id ON tasks(user_id);

-- Tabla TASKS - Índice para búsqueda por fecha límite
-- CREATE INDEX IF NOT EXISTS idx_task_due_date ON tasks(due_date);

-- Tabla TASKS - Índice para ordenamiento por fecha de creación
-- CREATE INDEX IF NOT EXISTS idx_task_created_at ON tasks(created_at DESC);

-- Tabla TASKS - Índice compuesto para filtrado por estado y fecha
-- CREATE INDEX IF NOT EXISTS idx_task_status_created ON tasks(status, created_at DESC);

-- ÍNDICES AVANZADOS (normalmente creados por schema.sql)
-- -------------------------------------------------------

-- Tabla TASKS - Índice GIN para búsqueda de texto completo en título
-- CREATE INDEX IF NOT EXISTS idx_task_title_fulltext
--     ON tasks USING gin(to_tsvector('spanish', title));

-- Tabla TASKS - Índice parcial para tareas con fecha límite
-- CREATE INDEX IF NOT EXISTS idx_task_due_date_partial
--     ON tasks(due_date)
--     WHERE due_date IS NOT NULL;

-- Tabla USERS - Índice parcial para usuarios activos
-- CREATE INDEX IF NOT EXISTS idx_user_enabled
--     ON users(enabled)
--     WHERE enabled = true;

-- Tabla USER_ROLES - Índice compuesto para relación muchos-a-muchos
-- CREATE INDEX IF NOT EXISTS idx_user_roles_composite
--     ON user_roles(user_id, role_id);

-- ============================================================================
-- OPTIMIZACIÓN Y MANTENIMIENTO
-- ============================================================================

-- VACUUM ANALYZE en tabla tasks
VACUUM ANALYZE tasks;

-- VACUUM completo (más agresivo, bloquea tabla)
-- VACUUM FULL tasks;

-- Reindexar tabla tasks (reconstruye índices)
REINDEX TABLE tasks;

-- Actualizar estadísticas para el query planner
ANALYZE tasks;

-- ============================================================================
-- ANÁLISIS DE QUERIES
-- ============================================================================

-- EXPLAIN básico (plan sin ejecutar)
EXPLAIN
SELECT * FROM tasks WHERE status = 'PENDING';

-- EXPLAIN ANALYZE (ejecuta y muestra tiempos reales)
EXPLAIN ANALYZE
SELECT * FROM tasks WHERE status = 'PENDING'
ORDER BY created_at DESC;

-- EXPLAIN con detalles completos
EXPLAIN (ANALYZE, BUFFERS, VERBOSE)
SELECT * FROM tasks
WHERE status = 'PENDING'
  AND created_at > now() - interval '7 days';

-- ============================================================================
-- QUERIES DE EJEMPLO PARA TESTING
-- ============================================================================

-- Insertar tarea de prueba
INSERT INTO tasks (title, description, status, due_date, created_at, updated_at)
VALUES (
    'Tarea de Prueba',
    'Esta es una tarea de ejemplo',
    'PENDING',
    now() + interval '7 days',
    now(),
    now()
);

-- Ver todas las tareas
SELECT
    id,
    title,
    status,
    due_date,
    created_at
FROM tasks
ORDER BY created_at DESC
LIMIT 10;

-- Contar tareas por estado
SELECT
    status,
    COUNT(*) as count
FROM tasks
GROUP BY status
ORDER BY count DESC;

-- Ver tareas próximas a vencer (siguientes 3 días)
SELECT
    id,
    title,
    status,
    due_date,
    due_date - now() as time_until_due
FROM tasks
WHERE due_date IS NOT NULL
  AND due_date > now()
  AND due_date < now() + interval '3 days'
ORDER BY due_date;

-- Ver tareas atrasadas
SELECT
    id,
    title,
    status,
    due_date,
    now() - due_date as overdue_by
FROM tasks
WHERE due_date IS NOT NULL
  AND due_date < now()
  AND status != 'COMPLETED'
ORDER BY due_date;

-- ============================================================================
-- BACKUP Y RESTORE
-- ============================================================================

-- Crear backup completo (ejecutar desde terminal, no psql)
-- docker compose exec postgres pg_dump -U postgres -F c -b -v -f /tmp/backup.dump taskmanagement_db

-- Crear backup en formato SQL (más legible)
-- docker compose exec postgres pg_dump -U postgres taskmanagement_db > backup.sql

-- Restaurar desde backup custom format
-- docker compose exec -T postgres pg_restore -U postgres -d taskmanagement_db -v /tmp/backup.dump

-- Restaurar desde backup SQL
-- docker compose exec -T postgres psql -U postgres taskmanagement_db < backup.sql

-- Backup solo de datos (sin esquema)
-- docker compose exec postgres pg_dump -U postgres --data-only taskmanagement_db > data-only.sql

-- Backup solo de esquema (sin datos)
-- docker compose exec postgres pg_dump -U postgres --schema-only taskmanagement_db > schema-only.sql

-- ============================================================================
-- SEGURIDAD
-- ============================================================================

-- Ver usuarios de la base de datos
\du

-- Ver permisos de la tabla tasks
\z tasks

-- Crear usuario con permisos limitados (ejecutar como postgres)
-- CREATE USER taskmanager_readonly WITH PASSWORD 'secure_password';
-- GRANT CONNECT ON DATABASE taskmanagement_db TO taskmanager_readonly;
-- GRANT USAGE ON SCHEMA public TO taskmanager_readonly;
-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO taskmanager_readonly;

-- Revocar permisos
-- REVOKE ALL PRIVILEGES ON DATABASE taskmanagement_db FROM taskmanager_readonly;

-- ============================================================================
-- LIMPIEZA
-- ============================================================================

-- Eliminar todas las tareas completadas (usar con cuidado)
-- DELETE FROM tasks WHERE status = 'COMPLETED';

-- Eliminar tareas antiguas (más de 6 meses)
-- DELETE FROM tasks
-- WHERE created_at < now() - interval '6 months'
--   AND status IN ('COMPLETED', 'CANCELLED');

-- Truncar tabla (elimina todos los datos, resetea secuencias)
-- TRUNCATE TABLE tasks RESTART IDENTITY CASCADE;

-- ============================================================================
-- INFORMACIÓN DEL SISTEMA
-- ============================================================================

-- Ver configuración de PostgreSQL
SELECT name, setting, unit, short_desc
FROM pg_settings
WHERE name IN (
    'shared_buffers',
    'effective_cache_size',
    'work_mem',
    'maintenance_work_mem',
    'max_connections',
    'checkpoint_completion_target',
    'wal_buffers',
    'random_page_cost'
)
ORDER BY name;

-- Ver uso de memoria
SELECT
    pg_size_pretty(pg_database_size(current_database())) as database_size,
    pg_size_pretty(sum(pg_total_relation_size(schemaname||'.'||tablename))) as tables_size
FROM pg_tables
WHERE schemaname = 'public';

-- Ver cache hit ratio (debe ser > 99%)
SELECT
    sum(heap_blks_read) as heap_read,
    sum(heap_blks_hit)  as heap_hit,
    sum(heap_blks_hit) / (sum(heap_blks_hit) + sum(heap_blks_read)) * 100 as cache_hit_ratio
FROM pg_statio_user_tables;

-- ============================================================================
-- NOTAS
-- ============================================================================
--
-- FRECUENCIA RECOMENDADA:
-- - Diario: Revisar conexiones activas, queries lentas
-- - Semanal: Revisar uso de índices, necesidad de VACUUM
-- - Mensual: Analizar crecimiento de BD, revisar backups
--
-- BUENAS PRÁCTICAS:
-- - Siempre hacer backup antes de operaciones destructivas
-- - Probar queries con EXPLAIN antes de ejecutarlas en producción
-- - Mantener estadísticas actualizadas con ANALYZE regular
-- - Monitorear cache hit ratio (debe ser > 99%)
-- - Revisar logs de PostgreSQL regularmente
--
-- ============================================================================
