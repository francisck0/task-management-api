-- ============================================================================
-- Script de inicialización de datos para Task Management API
-- ============================================================================
-- Este script se ejecuta automáticamente al iniciar la aplicación
-- (solo si las tablas están vacías)
--
-- IMPORTANTE:
-- - Spring Boot ejecuta este archivo automáticamente en cada inicio
-- - Para evitar duplicados, verificamos si los datos ya existen
-- ============================================================================

-- ============================================================================
-- 1. INSERTAR ROLES
-- ============================================================================

-- Rol para usuarios normales
INSERT INTO roles (name)
SELECT 'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_USER');

-- Rol para administradores
INSERT INTO roles (name)
SELECT 'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN');

-- ============================================================================
-- 2. CREAR USUARIO DE PRUEBA - ADMIN
-- ============================================================================
-- Usuario: admin
-- Contraseña: admin123
-- Email: admin@taskmanagement.com
-- Roles: ROLE_ADMIN, ROLE_USER
--
-- NOTA: La contraseña está encriptada con BCrypt (fuerza 10)
-- Hash BCrypt de "admin123": $2a$10$N9qo8uLOickgx2ZMRZoMye7Fd2J8DTOvBhYh8gvKr1JdV8z6wmQWO
-- ============================================================================

INSERT INTO users (username, email, password, full_name, enabled, account_non_locked, created_at, updated_at)
SELECT
    'admin',
    'admin@taskmanagement.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye7Fd2J8DTOvBhYh8gvKr1JdV8z6wmQWO',
    'Administrador del Sistema',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Asignar rol ROLE_USER al usuario admin
INSERT INTO user_roles (user_id, role_id)
SELECT
    (SELECT id FROM users WHERE username = 'admin'),
    (SELECT id FROM roles WHERE name = 'ROLE_USER')
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles
    WHERE user_id = (SELECT id FROM users WHERE username = 'admin')
    AND role_id = (SELECT id FROM roles WHERE name = 'ROLE_USER')
);

-- Asignar rol ROLE_ADMIN al usuario admin
INSERT INTO user_roles (user_id, role_id)
SELECT
    (SELECT id FROM users WHERE username = 'admin'),
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles
    WHERE user_id = (SELECT id FROM users WHERE username = 'admin')
    AND role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
);

-- ============================================================================
-- 3. CREAR USUARIO DE PRUEBA - USER
-- ============================================================================
-- Usuario: testuser
-- Contraseña: test123
-- Email: test@taskmanagement.com
-- Roles: ROLE_USER
--
-- Hash BCrypt de "test123": $2a$10$slYQmyNdGzTn7ZMvKzpELeEj0pRYOB1VfRJGbpOcRc6VdBKKVkJK2
-- ============================================================================

INSERT INTO users (username, email, password, full_name, enabled, account_non_locked, created_at, updated_at)
SELECT
    'testuser',
    'test@taskmanagement.com',
    '$2a$10$slYQmyNdGzTn7ZMvKzpELeEj0pRYOB1VfRJGbpOcRc6VdBKKVkJK2',
    'Usuario de Prueba',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'testuser');

-- Asignar rol ROLE_USER al usuario testuser
INSERT INTO user_roles (user_id, role_id)
SELECT
    (SELECT id FROM users WHERE username = 'testuser'),
    (SELECT id FROM roles WHERE name = 'ROLE_USER')
WHERE NOT EXISTS (
    SELECT 1 FROM user_roles
    WHERE user_id = (SELECT id FROM users WHERE username = 'testuser')
    AND role_id = (SELECT id FROM roles WHERE name = 'ROLE_USER')
);

-- ============================================================================
-- RESUMEN DE USUARIOS CREADOS:
-- ============================================================================
-- 1. Usuario Administrador:
--    - Username: admin
--    - Password: admin123
--    - Email: admin@taskmanagement.com
--    - Roles: ROLE_ADMIN, ROLE_USER
--
-- 2. Usuario Normal:
--    - Username: testuser
--    - Password: test123
--    - Email: test@taskmanagement.com
--    - Roles: ROLE_USER
-- ============================================================================
