-- ============================================================================
-- SCHEMA AVANZADO - ÍNDICES ESPECIALES PARA TASK MANAGEMENT API
-- ============================================================================
--
-- Este archivo contiene índices avanzados que JPA/Hibernate no puede crear
-- mediante anotaciones @Index. Se ejecuta automáticamente después de que
-- Hibernate cree las tablas básicas.
--
-- IMPORTANTE:
-- - Los índices básicos (status, user_id, due_date, created_at) se crean
--   automáticamente mediante las anotaciones @Index en las entidades JPA
-- - Este archivo solo contiene índices avanzados que requieren SQL nativo
--
-- EJECUCIÓN:
-- - Spring Boot ejecuta este archivo automáticamente al inicio
-- - Se ejecuta DESPUÉS de las operaciones DDL de Hibernate
-- - Usa IF NOT EXISTS para evitar errores si el índice ya existe
--
-- ============================================================================

-- ============================================================================
-- ÍNDICES AVANZADOS PARA TABLA TASKS
-- ============================================================================

-- ----------------------------------------------------------------------------
-- ÍNDICE GIN PARA BÚSQUEDA DE TEXTO COMPLETO EN TÍTULO
-- ----------------------------------------------------------------------------
-- Índice GIN (Generalized Inverted Index) para búsqueda de texto completo
--
-- QUÉ ES GIN:
-- - Tipo de índice especializado para búsqueda de texto en PostgreSQL
-- - Mucho más eficiente que LIKE '%texto%' para búsquedas de texto
-- - Soporta búsqueda en español con stemming y stop words
--
-- CUÁNDO SE USA:
-- - TaskRepository.findByTitleContainingIgnoreCase(String title)
-- - Búsquedas de tareas por palabras clave en el título
--
-- PERFORMANCE:
-- - Sin índice: O(n) - Escanea todas las filas
-- - Con índice GIN: O(log n) - Búsqueda casi instantánea
-- - Mejora: 10-1000x dependiendo del tamaño de la tabla
--
-- TRADE-OFF:
-- - Ventaja: Búsquedas de texto extremadamente rápidas
-- - Desventaja: Ocupa más espacio que un índice B-tree (~3x)
-- - Desventaja: Inserciones/updates ~10-15% más lentas
--
-- ALTERNATIVA:
-- - Si la búsqueda de texto no es crítica, se puede eliminar este índice
-- - Para búsqueda exacta, el índice B-tree del título sería suficiente
--
CREATE INDEX IF NOT EXISTS idx_task_title_fulltext
    ON tasks
    USING gin(to_tsvector('spanish', title));

-- ----------------------------------------------------------------------------
-- ÍNDICE PARCIAL PARA TAREAS CON FECHA LÍMITE
-- ----------------------------------------------------------------------------
-- Índice parcial (partial index) que solo indexa tareas que tienen due_date
--
-- QUÉ ES UN ÍNDICE PARCIAL:
-- - Índice que solo incluye filas que cumplen una condición (WHERE)
-- - Ocupa menos espacio que un índice completo
-- - Más rápido en queries que usan la misma condición
--
-- CUÁNDO SE USA:
-- - Búsquedas de tareas por vencer
-- - Búsquedas de tareas vencidas
-- - Queries que filtran por due_date IS NOT NULL
--
-- VENTAJAS:
-- - Más pequeño: Solo indexa tareas con fecha límite (~60-80% de las tareas)
-- - Más rápido: Menos entradas = búsquedas más rápidas
-- - Menos mantenimiento: Solo se actualiza cuando due_date cambia
--
-- NOTA:
-- - Este índice complementa (no reemplaza) idx_task_due_date de JPA
-- - JPA crea un índice completo, este es parcial para optimizar queries específicas
--
CREATE INDEX IF NOT EXISTS idx_task_due_date_partial
    ON tasks(due_date)
    WHERE due_date IS NOT NULL;

-- ============================================================================
-- ÍNDICES ADICIONALES PARA TABLA USERS
-- ============================================================================

-- NOTA: Los índices únicos en username y email se crean automáticamente
-- por Hibernate debido a la constraint unique=true en las columnas.
-- No es necesario crearlos manualmente aquí.

-- ----------------------------------------------------------------------------
-- ÍNDICE PARA USUARIOS ACTIVOS
-- ----------------------------------------------------------------------------
-- Índice parcial para usuarios activos (enabled = true)
--
-- CUÁNDO SE USA:
-- - Login y autenticación (Spring Security)
-- - Listados de usuarios activos
-- - Estadísticas de usuarios habilitados
--
-- VENTAJA:
-- - Más pequeño: Solo usuarios activos
-- - Más rápido: Menos entradas a escanear
--
CREATE INDEX IF NOT EXISTS idx_user_enabled
    ON users(enabled)
    WHERE enabled = true;

-- ============================================================================
-- ÍNDICES PARA TABLA USER_ROLES (RELACIÓN MUCHOS-A-MUCHOS)
-- ============================================================================

-- NOTA: Hibernate crea automáticamente índices en las columnas FK (user_id, role_id)
-- debido a las constraints de foreign key. No necesitamos crearlos manualmente.

-- Sin embargo, podemos crear un índice compuesto para mejorar queries bidireccionales:

CREATE INDEX IF NOT EXISTS idx_user_roles_composite
    ON user_roles(user_id, role_id);

-- ============================================================================
-- ANÁLISIS Y MANTENIMIENTO
-- ============================================================================

-- Actualizar estadísticas de las tablas para el query planner
-- Esto ayuda a PostgreSQL a elegir el mejor plan de ejecución

ANALYZE tasks;
ANALYZE users;
ANALYZE user_roles;
ANALYZE roles;

-- ============================================================================
-- VERIFICACIÓN DE ÍNDICES (QUERIES ÚTILES)
-- ============================================================================
--
-- Para verificar que los índices se crearon correctamente, ejecutar:
--
-- 1. Ver todos los índices de la tabla tasks:
-- SELECT indexname, indexdef
-- FROM pg_indexes
-- WHERE tablename = 'tasks'
-- ORDER BY indexname;
--
-- 2. Ver tamaño de índices:
-- SELECT
--     indexname,
--     pg_size_pretty(pg_relation_size(indexrelid)) as size
-- FROM pg_stat_user_indexes
-- WHERE schemaname = 'public' AND relname = 'tasks'
-- ORDER BY pg_relation_size(indexrelid) DESC;
--
-- 3. Ver uso de índices:
-- SELECT
--     indexname,
--     idx_scan as scans,
--     idx_tup_read as tuples_read,
--     idx_tup_fetch as tuples_fetched
-- FROM pg_stat_user_indexes
-- WHERE schemaname = 'public' AND relname = 'tasks'
-- ORDER BY idx_scan DESC;
--
-- ============================================================================
-- NOTAS IMPORTANTES
-- ============================================================================
--
-- COMPATIBILIDAD:
-- - Estos índices están optimizados para PostgreSQL 12+
-- - El índice GIN con to_tsvector es específico de PostgreSQL
-- - Si migras a otra BD (MySQL, Oracle), estos índices deben adaptarse
--
-- MANTENIMIENTO:
-- - Los índices se mantienen automáticamente por PostgreSQL
-- - Ejecutar VACUUM ANALYZE periódicamente para optimizar
-- - Monitorear uso de índices y eliminar los no utilizados
--
-- TESTING:
-- - Usar EXPLAIN ANALYZE para verificar que los índices se usan
-- - Ejemplo:
--   EXPLAIN ANALYZE
--   SELECT * FROM tasks WHERE to_tsvector('spanish', title) @@ to_tsquery('spanish', 'importante');
--
-- PERFORMANCE EN DESARROLLO:
-- - Con pocos datos, los índices pueden no ser utilizados (table scan es más rápido)
-- - Los beneficios se ven con +1000 registros
--
-- ============================================================================

-- ============================================================================
-- TABLA DE AUDITORÍA (AUDIT_LOG)
-- ============================================================================
--
-- Tabla para registrar todas las operaciones críticas del sistema.
--
-- PROPÓSITO:
-- - Cumplimiento normativo (GDPR, SOX, HIPAA)
-- - Trazabilidad completa de cambios
-- - Detección de fraudes y anomalías
-- - Análisis forense en caso de incidentes
--
-- RETENCIÓN DE DATOS:
-- - Configurar según requisitos legales (típicamente 1-7 años)
-- - Implementar job periódico para archivar datos antiguos
-- - Considerar particionamiento por fecha para grandes volúmenes
--
-- ============================================================================

-- La tabla audit_log se crea automáticamente por Hibernate mediante la entidad AuditLog.
-- Aquí solo creamos índices adicionales que no se pueden crear con anotaciones JPA.

-- ----------------------------------------------------------------------------
-- ÍNDICE COMPUESTO PARA CONSULTAS POR USUARIO Y FECHA
-- ----------------------------------------------------------------------------
-- El índice más común en consultas de auditoría: "¿Qué hizo el usuario X entre fechas Y y Z?"
--
CREATE INDEX IF NOT EXISTS idx_audit_username_timestamp
    ON audit_log(username, timestamp DESC);

-- ----------------------------------------------------------------------------
-- ÍNDICE COMPUESTO PARA CONSULTAS POR ACCIÓN Y ESTADO
-- ----------------------------------------------------------------------------
-- Para buscar operaciones específicas que fallaron o tuvieron éxito
-- Ejemplo: "Todos los DELETE que fallaron"
--
CREATE INDEX IF NOT EXISTS idx_audit_action_status
    ON audit_log(action, status);

-- ----------------------------------------------------------------------------
-- ÍNDICE COMPUESTO PARA HISTORIAL DE UN RECURSO ESPECÍFICO
-- ----------------------------------------------------------------------------
-- Para rastrear todos los cambios de una entidad específica
-- Ejemplo: "Historial completo de la tarea con ID 123"
--
CREATE INDEX IF NOT EXISTS idx_audit_resource_id
    ON audit_log(resource, resource_id, timestamp DESC)
    WHERE resource_id IS NOT NULL;

-- ----------------------------------------------------------------------------
-- ÍNDICE PARCIAL PARA OPERACIONES FALLIDAS
-- ----------------------------------------------------------------------------
-- Solo indexa operaciones con status = 'FAILURE'
-- Útil para detectar intentos de acceso no autorizado o problemas del sistema
--
CREATE INDEX IF NOT EXISTS idx_audit_failures
    ON audit_log(timestamp DESC, username, action)
    WHERE status = 'FAILURE';

-- ----------------------------------------------------------------------------
-- ÍNDICE GIN PARA BÚSQUEDA DE TEXTO EN DESCRIPCIÓN
-- ----------------------------------------------------------------------------
-- Para buscar en las descripciones de auditoría (opcional)
-- Útil si se hacen búsquedas frecuentes en las descripciones
--
CREATE INDEX IF NOT EXISTS idx_audit_description_fulltext
    ON audit_log
    USING gin(to_tsvector('spanish', COALESCE(description, '')))
    WHERE description IS NOT NULL;

-- Actualizar estadísticas de la tabla audit_log
ANALYZE audit_log;

-- ============================================================================
-- TABLA DE REFRESH TOKENS (REFRESH_TOKENS)
-- ============================================================================
--
-- Tabla para gestionar Refresh Tokens de larga duración.
--
-- PROPÓSITO:
-- - Permitir renovación de Access Tokens sin reautenticación
-- - Gestión de sesiones por dispositivo
-- - Detección de tokens comprometidos (rotation)
-- - Control de sesiones simultáneas
--
-- SEGURIDAD:
-- - Refresh Token Rotation detecta tokens robados
-- - Límite de dispositivos por usuario
-- - Revocación masiva al cambiar contraseña
--
-- ============================================================================

-- La tabla refresh_tokens se crea automáticamente por Hibernate.
-- Aquí solo creamos índices adicionales que no se pueden crear con anotaciones JPA.

-- ----------------------------------------------------------------------------
-- ÍNDICE PARCIAL PARA TOKENS VÁLIDOS
-- ----------------------------------------------------------------------------
-- Solo indexa tokens que no están expirados ni revocados
-- Optimiza búsquedas de tokens activos
--
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_valid
    ON refresh_tokens(token, user_id)
    WHERE revoked = false AND expiry_date > NOW();

-- ----------------------------------------------------------------------------
-- ÍNDICE PARA LIMPIEZA DE TOKENS EXPIRADOS
-- ----------------------------------------------------------------------------
-- Facilita la eliminación periódica de tokens expirados
--
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expired
    ON refresh_tokens(expiry_date)
    WHERE expiry_date < NOW();

-- ----------------------------------------------------------------------------
-- ÍNDICE PARA DETECTAR MÚLTIPLES SESIONES DE UNA IP
-- ----------------------------------------------------------------------------
-- Útil para detectar ataques de fuerza bruta o comportamiento sospechoso
--
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_ip_created
    ON refresh_tokens(ip_address, created_at)
    WHERE revoked = false;

-- Actualizar estadísticas de la tabla refresh_tokens
ANALYZE refresh_tokens;

-- ============================================================================
-- PARTICIONAMIENTO DE AUDIT_LOG (OPCIONAL - PARA GRANDES VOLÚMENES)
-- ============================================================================
--
-- Si se esperan grandes volúmenes de datos (millones de registros), considerar
-- particionar la tabla por rango de fechas (por mes o trimestre).
--
-- EJEMPLO DE PARTICIONAMIENTO (comentado por defecto):
--
-- -- 1. Convertir tabla existente a particionada:
-- ALTER TABLE audit_log RENAME TO audit_log_old;
--
-- CREATE TABLE audit_log (
--     LIKE audit_log_old INCLUDING ALL
-- ) PARTITION BY RANGE (timestamp);
--
-- -- 2. Crear particiones por mes (ejemplo):
-- CREATE TABLE audit_log_2025_01 PARTITION OF audit_log
--     FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
--
-- CREATE TABLE audit_log_2025_02 PARTITION OF audit_log
--     FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
--
-- -- ... y así sucesivamente
--
-- -- 3. Migrar datos antiguos:
-- INSERT INTO audit_log SELECT * FROM audit_log_old;
-- DROP TABLE audit_log_old;
--
-- BENEFICIOS:
-- - Queries más rápidas (solo escanean particiones relevantes)
-- - Limpieza más eficiente (DROP partition vs DELETE masivo)
-- - Mejor gestión de espacio en disco
--
-- TRADE-OFF:
-- - Mayor complejidad de mantenimiento
-- - Necesita job automático para crear particiones futuras
--
-- ============================================================================
