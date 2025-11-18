# Índices de Base de Datos - Task Management API

## 📋 Tabla de Contenidos

- [Resumen Ejecutivo](#resumen-ejecutivo)
- [Índices Implementados](#índices-implementados)
- [Rendimiento Esperado](#rendimiento-esperado)
- [Cómo Verificar los Índices](#cómo-verificar-los-índices)
- [Mantenimiento](#mantenimiento)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Resumen Ejecutivo

Esta API implementa una estrategia completa de indexación en PostgreSQL para optimizar el rendimiento de las consultas más frecuentes. Los índices se crean automáticamente al iniciar la aplicación mediante dos mecanismos:

1. **Hibernate/JPA**: Crea índices básicos desde anotaciones `@Index` en las entidades
2. **schema.sql**: Crea índices avanzados (GIN, parciales, compuestos) mediante SQL nativo

### Mejoras de Rendimiento Esperadas

| Tamaño de BD | Mejora en Búsquedas | Mejora en Ordenamiento |
|--------------|---------------------|------------------------|
| 1,000 tareas | ~10x más rápido     | ~5x más rápido         |
| 100,000 tareas | ~100x más rápido  | ~50x más rápido        |
| 1,000,000 tareas | ~1000x más rápido | ~500x más rápido     |

---

## 📊 Índices Implementados

### Tabla: `tasks`

#### Índices Básicos (Creados por Hibernate)

| Nombre del Índice | Columnas | Tipo | Propósito |
|-------------------|----------|------|-----------|
| `idx_task_status` | `status` | B-tree | Filtrado por estado (PENDING, IN_PROGRESS, etc.) |
| `idx_task_user_id` | `user_id` | B-tree | **CRÍTICO** - Verificación de ownership |
| `idx_task_due_date` | `due_date` | B-tree | Filtrado por fecha límite |
| `idx_task_created_at` | `created_at DESC` | B-tree | Ordenamiento por fecha de creación |
| `idx_task_status_created` | `status, created_at DESC` | B-tree (Compuesto) | Filtrado por estado + ordenamiento |

#### Índices Avanzados (Creados por schema.sql)

| Nombre del Índice | Columnas | Tipo | Propósito |
|-------------------|----------|------|-----------|
| `idx_task_title_fulltext` | `to_tsvector('spanish', title)` | GIN | Búsqueda de texto completo en título |
| `idx_task_due_date_partial` | `due_date WHERE due_date IS NOT NULL` | B-tree (Parcial) | Optimiza queries de tareas con fecha límite |

### Tabla: `users`

| Nombre del Índice | Columnas | Tipo | Propósito |
|-------------------|----------|------|-----------|
| `UK_username` | `username` | B-tree (Único) | **CRÍTICO** - Login y autenticación |
| `UK_email` | `email` | B-tree (Único) | Búsqueda por email, verificación de duplicados |
| `idx_user_enabled` | `enabled WHERE enabled = true` | B-tree (Parcial) | Filtrado de usuarios activos |

### Tabla: `user_roles`

| Nombre del Índice | Columnas | Tipo | Propósito |
|-------------------|----------|------|-----------|
| `idx_user_roles_composite` | `user_id, role_id` | B-tree (Compuesto) | Optimiza JOIN entre users y roles |

---

## ⚡ Rendimiento Esperado

### Consultas Optimizadas

#### 1. Listar todas las tareas (ordenadas por fecha)
```sql
SELECT * FROM tasks ORDER BY created_at DESC;
```
- **Sin índice**: Table scan O(n) + Sort O(n log n)
- **Con idx_task_created_at**: Index scan O(log n)
- **Mejora**: ~100x en 100k registros

#### 2. Buscar tareas por estado
```sql
SELECT * FROM tasks WHERE status = 'PENDING';
```
- **Sin índice**: Table scan O(n)
- **Con idx_task_status**: Index scan O(log n)
- **Mejora**: ~100x en 100k registros

#### 3. Verificar ownership (cada request)
```sql
SELECT * FROM tasks WHERE id = ? AND user_id = ?;
```
- **Sin índice**: Table scan O(n)
- **Con idx_task_user_id**: Index scan O(log n)
- **Mejora**: **CRÍTICA** - Sin este índice, la app sería inutilizable

#### 4. Buscar tareas por título
```sql
SELECT * FROM tasks WHERE to_tsvector('spanish', title) @@ to_tsquery('spanish', 'palabra');
```
- **Sin índice GIN**: Table scan O(n) + procesamiento de texto
- **Con idx_task_title_fulltext**: GIN index scan O(log n)
- **Mejora**: ~500x en 100k registros

#### 5. Filtrar por estado y ordenar por fecha (paginación)
```sql
SELECT * FROM tasks WHERE status = 'PENDING' ORDER BY created_at DESC LIMIT 20;
```
- **Sin índice compuesto**: Index scan en status + Sort
- **Con idx_task_status_created**: Index scan directo (sin sort)
- **Mejora**: ~10x (evita ordenamiento)

---

## 🔍 Cómo Verificar los Índices

### 1. Conectar a la Base de Datos

```bash
# Opción 1: Docker Compose
docker compose exec postgres psql -U postgres -d taskmanagement_db

# Opción 2: Cliente psql local
psql -h localhost -p 5432 -U postgres -d taskmanagement_db
```

### 2. Ver Todos los Índices de la Tabla Tasks

```sql
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'tasks'
ORDER BY indexname;
```

**Salida esperada:**
```
indexname                  | indexdef
---------------------------+-----------------------------------------------
idx_task_created_at        | CREATE INDEX ... ON tasks (created_at DESC)
idx_task_due_date          | CREATE INDEX ... ON tasks (due_date)
idx_task_due_date_partial  | CREATE INDEX ... ON tasks (due_date) WHERE ...
idx_task_status            | CREATE INDEX ... ON tasks (status)
idx_task_status_created    | CREATE INDEX ... ON tasks (status, created_at DESC)
idx_task_title_fulltext    | CREATE INDEX ... ON tasks USING gin(...)
idx_task_user_id           | CREATE INDEX ... ON tasks (user_id)
tasks_pkey                 | CREATE UNIQUE INDEX ... ON tasks (id)
```

### 3. Ver Tamaño de Índices

```sql
SELECT
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) as size
FROM pg_stat_user_indexes
WHERE schemaname = 'public' AND relname = 'tasks'
ORDER BY pg_relation_size(indexrelid) DESC;
```

### 4. Ver Uso de Índices

```sql
SELECT
    indexname,
    idx_scan as scans,
    idx_tup_read as tuples_read,
    idx_tup_fetch as tuples_fetched
FROM pg_stat_user_indexes
WHERE schemaname = 'public' AND relname = 'tasks'
ORDER BY idx_scan DESC;
```

**Interpretación:**
- `scans`: Número de veces que el índice fue usado
- Si `scans = 0`: El índice nunca se ha usado (candidato para eliminar)
- Índices más usados: `idx_task_user_id`, `idx_task_status`, `idx_task_created_at`

### 5. Verificar Plan de Ejecución de Queries

```sql
-- Plan de ejecución (sin ejecutar)
EXPLAIN
SELECT * FROM tasks WHERE status = 'PENDING';

-- Plan de ejecución + tiempos reales
EXPLAIN ANALYZE
SELECT * FROM tasks WHERE status = 'PENDING' ORDER BY created_at DESC;
```

**Buscar en la salida:**
- ✅ `Index Scan using idx_task_status` - Índice usado correctamente
- ❌ `Seq Scan on tasks` - Tabla scan (sin índice)

---

## 🛠 Mantenimiento

### Actualizar Estadísticas (Recomendado: Semanal)

```sql
ANALYZE tasks;
ANALYZE users;
ANALYZE user_roles;
```

**¿Por qué?** PostgreSQL usa estadísticas para elegir el mejor plan de ejecución. Estadísticas desactualizadas pueden causar queries lentas.

### Vacuum (Recomendado: Mensual)

```sql
-- Vacuum ligero (no bloquea tabla)
VACUUM ANALYZE tasks;

-- Vacuum completo (bloquea tabla - usar solo en mantenimiento)
VACUUM FULL tasks;
```

**¿Por qué?** Limpia filas muertas (deleted/updated) y recupera espacio en disco.

### Reindexar (Solo si hay corrupción)

```sql
-- Reindexar tabla completa
REINDEX TABLE tasks;

-- Reindexar índice específico
REINDEX INDEX idx_task_status;
```

**¿Cuándo?** Solo si hay corrupción o después de operaciones masivas.

### Monitoreo de Índices No Utilizados

```sql
SELECT
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(indexrelid)) as size,
    idx_scan as scans
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND schemaname = 'public'
  AND indexname NOT LIKE 'pg_toast%'
ORDER BY pg_relation_size(indexrelid) DESC;
```

**Acción:** Si un índice no se usa después de 1 mes en producción, considerar eliminarlo.

---

## 🐛 Troubleshooting

### Problema: Los índices no se crean automáticamente

**Síntomas:**
- Al ejecutar `\d tasks` en psql, no aparecen los índices
- Queries lentas incluso con pocos datos

**Solución:**

1. Verificar que la aplicación inició correctamente:
   ```bash
   docker compose logs app | grep -i "index"
   ```

2. Verificar configuración en `application.yml`:
   ```yaml
   spring:
     jpa:
       hibernate:
         ddl-auto: update  # Debe ser update, create-drop o create
     sql:
       init:
         mode: always  # Debe ser always para ejecutar schema.sql
   ```

3. Ejecutar manualmente schema.sql:
   ```bash
   docker compose exec -T postgres psql -U postgres -d taskmanagement_db < src/main/resources/schema.sql
   ```

### Problema: Error al crear índices GIN

**Error:**
```
ERROR: could not create index "idx_task_title_fulltext"
```

**Causa:** Extensión `pg_trgm` no instalada (necesaria para texto en español)

**Solución:**
```sql
-- Conectar como superusuario
psql -U postgres -d taskmanagement_db

-- Crear extensión
CREATE EXTENSION IF NOT EXISTS pg_trgm;
```

### Problema: Queries lentas incluso con índices

**Diagnóstico:**

1. Verificar que el índice existe:
   ```sql
   \d tasks
   ```

2. Verificar plan de ejecución:
   ```sql
   EXPLAIN ANALYZE SELECT * FROM tasks WHERE status = 'PENDING';
   ```

3. Si muestra `Seq Scan` en lugar de `Index Scan`:

   **Causas posibles:**
   - Tabla muy pequeña (< 1000 filas) - PostgreSQL prefiere table scan
   - Estadísticas desactualizadas - Ejecutar `ANALYZE tasks;`
   - Índice corrupto - Ejecutar `REINDEX TABLE tasks;`

### Problema: Espacio en disco insuficiente

**Síntomas:**
- Error al crear índices
- BD lenta

**Verificar espacio:**
```sql
SELECT
    pg_size_pretty(pg_database_size(current_database())) as db_size;

SELECT
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS total_size,
    pg_size_pretty(pg_relation_size(schemaname||'.'||tablename)) AS table_size,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename) -
                   pg_relation_size(schemaname||'.'||tablename)) AS index_size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

**Solución:**
- Eliminar índices no utilizados
- Ejecutar `VACUUM FULL` para recuperar espacio
- Aumentar espacio en disco

---

## 📚 Referencias

- **Documentación oficial de PostgreSQL**: https://www.postgresql.org/docs/current/indexes.html
- **GIN Indexes**: https://www.postgresql.org/docs/current/gin.html
- **Partial Indexes**: https://www.postgresql.org/docs/current/indexes-partial.html
- **Índices en Spring Data JPA**: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

---

## 📝 Notas Finales

### Trade-offs de los Índices

**Ventajas:**
- ✅ Queries 10-1000x más rápidas
- ✅ Mejor experiencia de usuario
- ✅ Menor carga en CPU
- ✅ Escalabilidad mejorada

**Desventajas:**
- ❌ Inserciones/updates ~5-15% más lentas
- ❌ Espacio en disco adicional (~10-30%)
- ❌ Complejidad de mantenimiento

### ¿Cuándo Eliminar Índices?

- Índice no usado en 30+ días (verificar con `idx_scan`)
- Tabla con pocas filas (< 100) - no aportan valor
- Columnas que nunca se filtran/ordenan
- Múltiples índices redundantes (ej: índice en `A` y índice en `A,B`)

### Recomendaciones para Producción

1. **Monitoreo**: Configurar alertas para slow queries
2. **Estadísticas**: Ejecutar `ANALYZE` automáticamente (cron job)
3. **Vacuum**: Configurar autovacuum apropiadamente
4. **Backup**: Respaldar antes de reindexar
5. **Testing**: Probar performance con datos reales antes de deploy

---

**Autor**: Claude Code
**Fecha**: 2025-11-15
**Versión**: 1.0.0
