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
-- 4. CREAR TAREAS DE EJEMPLO
-- ============================================================================
-- Creamos tareas de ejemplo asignadas a los usuarios de prueba
-- para demostrar la funcionalidad de ownership
-- ============================================================================

-- Tareas del usuario ADMIN
INSERT INTO tasks (title, description, status, due_date, user_id, created_at, updated_at)
SELECT
    'Revisar documentación del proyecto',
    'Revisar y actualizar toda la documentación técnica del proyecto Task Management API',
    'IN_PROGRESS',
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    (SELECT id FROM users WHERE username = 'admin'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tasks WHERE title = 'Revisar documentación del proyecto'
);

INSERT INTO tasks (title, description, status, due_date, user_id, created_at, updated_at)
SELECT
    'Configurar servidor de producción',
    'Configurar el entorno de producción con Docker y Kubernetes',
    'PENDING',
    CURRENT_TIMESTAMP + INTERVAL '14 days',
    (SELECT id FROM users WHERE username = 'admin'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tasks WHERE title = 'Configurar servidor de producción'
);

INSERT INTO tasks (title, description, status, due_date, user_id, created_at, updated_at)
SELECT
    'Implementar monitoreo con Prometheus',
    'Configurar Prometheus y Grafana para monitoreo de la aplicación',
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    (SELECT id FROM users WHERE username = 'admin'),
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
WHERE NOT EXISTS (
    SELECT 1 FROM tasks WHERE title = 'Implementar monitoreo con Prometheus'
);

-- Tareas del usuario TESTUSER
INSERT INTO tasks (title, description, status, due_date, user_id, created_at, updated_at)
SELECT
    'Comprar ingredientes para la cena',
    'Comprar tomates, cebolla, ajo y pasta en el supermercado',
    'PENDING',
    CURRENT_TIMESTAMP + INTERVAL '1 day',
    (SELECT id FROM users WHERE username = 'testuser'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tasks WHERE title = 'Comprar ingredientes para la cena'
);

INSERT INTO tasks (title, description, status, due_date, user_id, created_at, updated_at)
SELECT
    'Hacer ejercicio',
    'Ir al gimnasio por 1 hora - enfocarse en cardio y pesas',
    'IN_PROGRESS',
    CURRENT_TIMESTAMP + INTERVAL '1 day',
    (SELECT id FROM users WHERE username = 'testuser'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM tasks WHERE title = 'Hacer ejercicio'
);

INSERT INTO tasks (title, description, status, due_date, user_id, created_at, updated_at)
SELECT
    'Leer capítulo 5 del libro de Java',
    'Estudiar el capítulo sobre concurrencia y threads en Java',
    'COMPLETED',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    (SELECT id FROM users WHERE username = 'testuser'),
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
WHERE NOT EXISTS (
    SELECT 1 FROM tasks WHERE title = 'Leer capítulo 5 del libro de Java'
);

-- ============================================================================
-- RESUMEN DE DATOS INSERTADOS:
-- ============================================================================
--
-- USUARIOS CREADOS:
-- -----------------
-- 1. Usuario Administrador:
--    - Username: admin
--    - Password: admin123
--    - Email: admin@taskmanagement.com
--    - Roles: ROLE_ADMIN, ROLE_USER
--    - Tareas: 3 (1 PENDING, 1 IN_PROGRESS, 1 COMPLETED)
--
-- 2. Usuario Normal:
--    - Username: testuser
--    - Password: test123
--    - Email: test@taskmanagement.com
--    - Roles: ROLE_USER
--    - Tareas: 3 (1 PENDING, 1 IN_PROGRESS, 1 COMPLETED)
--
-- TAREAS CREADAS:
-- ---------------
-- Total: 6 tareas
-- - 2 PENDING
-- - 2 IN_PROGRESS
-- - 2 COMPLETED
--
-- SEGURIDAD:
-- ----------
-- - Cada tarea está asignada a un usuario específico (user_id)
-- - Solo el propietario puede ver/modificar sus tareas
-- - La verificación de ownership se realiza en TaskServiceImpl
-- ============================================================================
