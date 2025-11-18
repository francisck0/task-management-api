-- ============================================================================
-- SCRIPT DE VERIFICACIÓN DE ÍNDICES
-- ============================================================================
-- Este script crea las tablas manualmente y luego verifica que los índices
-- se crean correctamente desde el schema.sql
-- ============================================================================

-- Limpiar tablas existentes
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS tasks CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- ============================================================================
-- CREAR TABLA ROLES
-- ============================================================================
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- ============================================================================
-- CREAR TABLA USERS
-- ============================================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT true,
    account_non_locked BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Crear índices básicos en users (simulando lo que haría Hibernate)
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);

-- ============================================================================
-- CREAR TABLA TASKS
-- ============================================================================
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    due_date TIMESTAMP,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Crear índices básicos en tasks (simulando lo que haría Hibernate)
CREATE INDEX IF NOT EXISTS idx_task_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_task_user_id ON tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_task_due_date ON tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_task_created_at ON tasks(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_task_status_created ON tasks(status, created_at DESC);

-- ============================================================================
-- CREAR TABLA USER_ROLES (tabla de relación)
-- ============================================================================
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ============================================================================
-- CREAR ÍNDICES AVANZADOS (desde schema.sql)
-- ============================================================================

-- Índice GIN para búsqueda de texto completo en título
CREATE INDEX IF NOT EXISTS idx_task_title_fulltext
    ON tasks USING gin(to_tsvector('spanish', title));

-- Índice parcial para tareas con fecha límite
CREATE INDEX IF NOT EXISTS idx_task_due_date_partial
    ON tasks(due_date)
    WHERE due_date IS NOT NULL;

-- Índice parcial para usuarios activos
CREATE INDEX IF NOT EXISTS idx_user_enabled
    ON users(enabled)
    WHERE enabled = true;

-- Índice compuesto para relación muchos-a-muchos
CREATE INDEX IF NOT EXISTS idx_user_roles_composite
    ON user_roles(user_id, role_id);

-- ============================================================================
-- VERIFICAR ÍNDICES CREADOS
-- ============================================================================

\echo ''
\echo '============================================================================'
\echo 'ÍNDICES DE LA TABLA TASKS'
\echo '============================================================================'

SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'tasks'
ORDER BY indexname;

\echo ''
\echo '============================================================================'
\echo 'ÍNDICES DE LA TABLA USERS'
\echo '============================================================================'

SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'users'
ORDER BY indexname;

\echo ''
\echo '============================================================================'
\echo 'ÍNDICES DE LA TABLA USER_ROLES'
\echo '============================================================================'

SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'user_roles'
ORDER BY indexname;

\echo ''
\echo '============================================================================'
\echo 'TAMAÑO DE ÍNDICES'
\echo '============================================================================'

SELECT
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) as size
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
ORDER BY tablename, pg_relation_size(indexrelid) DESC;

\echo ''
\echo '============================================================================'
\echo 'RESUMEN DE ÍNDICES POR TABLA'
\echo '============================================================================'

SELECT
    tablename,
    COUNT(*) as num_indices,
    pg_size_pretty(SUM(pg_relation_size(indexrelid))) as total_size
FROM pg_stat_user_indexes
WHERE schemaname = 'public'
GROUP BY tablename
ORDER BY tablename;

\echo ''
\echo '============================================================================'
\echo 'VERIFICACIÓN COMPLETADA'
\echo '============================================================================'
