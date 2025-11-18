# Sistema de Auditoría AOP - Documentación Completa

## 📋 Índice

1. [Introducción](#introducción)
2. [Arquitectura](#arquitectura)
3. [Componentes](#componentes)
4. [Uso](#uso)
5. [Endpoints](#endpoints)
6. [Base de Datos](#base-de-datos)
7. [Ejemplos](#ejemplos)
8. [Mejores Prácticas](#mejores-prácticas)

---

## Introducción

El sistema de auditoría AOP (Aspect-Oriented Programming) proporciona trazabilidad completa de todas las operaciones críticas del sistema. Permite:

- ✅ **Cumplimiento normativo** (GDPR, SOX, HIPAA)
- ✅ **Trazabilidad** de quién hizo qué y cuándo
- ✅ **Detección de fraudes** y actividad sospechosa
- ✅ **Análisis forense** en caso de incidentes de seguridad
- ✅ **Reportes** de actividad de usuarios

### ¿Qué se audita automáticamente?

- Todas las operaciones CRUD sobre recursos críticos
- Intentos de acceso no autorizado
- Cambios en configuración del sistema
- Operaciones administrativas
- Errores y excepciones

---

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                      USUARIO                                │
│                      Realiza una acción                     │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   CONTROLLER                                │
│   Método anotado con @Auditable                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   AUDITASPECT (AOP)                         │
│   - Intercepta la ejecución                                │
│   - Captura contexto (usuario, timestamp, etc.)            │
│   - Ejecuta el método                                      │
│   - Persiste en BD                                         │
└────────────────────────┬────────────────────────────────────┘
                         │
         ┌───────────────┴───────────────┐
         ▼                               ▼
┌──────────────────┐           ┌──────────────────┐
│    LOGS          │           │   BASE DE DATOS  │
│  (SLF4J/Logback) │           │   (audit_log)    │
└──────────────────┘           └──────────────────┘
```

---

## Componentes

### 1. **@Auditable** (Anotación)

Marca métodos que requieren auditoría.

**Ubicación:** `src/main/java/com/taskmanagement/api/aspect/Auditable.java`

**Parámetros:**
- `action`: Acción realizada (obligatorio)
- `resource`: Tipo de recurso (opcional)
- `description`: Descripción adicional (opcional)
- `logParameters`: Si registrar parámetros (default: false)
- `logResult`: Si registrar resultado (default: false)

### 2. **AuditAspect**

Aspecto de AOP que procesa la anotación @Auditable.

**Ubicación:** `src/main/java/com/taskmanagement/api/aspect/AuditAspect.java`

**Responsabilidades:**
- Interceptar métodos anotados con @Auditable
- Capturar información del contexto (usuario, timestamp)
- Loggear en archivo de logs
- Persistir en base de datos

### 3. **AuditLog** (Entidad JPA)

Entidad que representa un registro de auditoría.

**Ubicación:** `src/main/java/com/taskmanagement/api/model/AuditLog.java`

**Campos principales:**
- `username`: Usuario que realizó la acción
- `action`: Tipo de acción (CREATE, UPDATE, DELETE, etc.)
- `resource`: Tipo de recurso afectado
- `resourceId`: ID del recurso (opcional)
- `status`: SUCCESS o FAILURE
- `timestamp`: Cuándo se realizó
- `durationMs`: Tiempo de ejecución

### 4. **AuditLogRepository**

Repositorio JPA con queries especializadas.

**Ubicación:** `src/main/java/com/taskmanagement/api/repository/AuditLogRepository.java`

**Métodos destacados:**
- `findByUsername()`: Buscar por usuario
- `findByAction()`: Buscar por tipo de acción
- `findByTimestampBetween()`: Buscar por rango de fechas
- `findByResourceAndResourceId()`: Historial de un recurso específico
- `countFailuresSince()`: Contar fallos recientes

### 5. **AuditLogService**

Capa de servicio para lógica de negocio de auditoría.

**Ubicación:** `src/main/java/com/taskmanagement/api/service/impl/AuditLogServiceImpl.java`

**Funcionalidades:**
- Consultas de auditoría
- Estadísticas y reportes
- Detección de actividad sospechosa
- Limpieza de registros antiguos

### 6. **AuditLogController**

Controlador REST para consultar auditoría.

**Ubicación:** `src/main/java/com/taskmanagement/api/controller/AuditLogController.java`

**Seguridad:** Solo accesible por usuarios con rol ADMIN.

---

## Uso

### Anotar un método para auditoría

```java
@PostMapping
@Auditable(
    action = "CREATE_TASK",
    resource = "Task",
    description = "Usuario crea nueva tarea",
    logParameters = true,
    logResult = false
)
public ResponseEntity<TaskResponseDto> createTask(@RequestBody TaskRequestDto dto) {
    TaskResponseDto task = taskService.createTask(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(task);
}
```

### Ejemplo completo en TaskCommandController

```java
@PutMapping("/{id}")
@Auditable(
    action = "UPDATE_TASK",
    resource = "Task",
    description = "Usuario actualiza tarea existente",
    logParameters = true,
    logResult = false
)
public ResponseEntity<TaskResponseDto> updateTask(
        @PathVariable Long id,
        @RequestBody TaskRequestDto dto) {
    TaskResponseDto task = taskService.updateTask(id, dto);
    return ResponseEntity.ok(task);
}

@DeleteMapping("/{id}")
@Auditable(
    action = "DELETE_TASK",
    resource = "Task",
    description = "Usuario elimina tarea",
    logParameters = true,
    logResult = false
)
public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
    taskService.deleteTask(id);
    return ResponseEntity.noContent().build();
}
```

---

## Endpoints

Todos los endpoints requieren autenticación JWT y rol ADMIN.

### 1. **Obtener todos los registros de auditoría**

```
GET /api/v1/audit?page=0&size=20
```

### 2. **Obtener auditoría de un usuario**

```
GET /api/v1/audit/user/{username}?page=0&size=20
```

**Ejemplo:**
```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/audit/user/john.doe?page=0&size=20"
```

### 3. **Obtener auditoría por tipo de acción**

```
GET /api/v1/audit/action/{action}?page=0&size=20
```

**Ejemplo:**
```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/audit/action/DELETE_TASK"
```

### 4. **Obtener auditoría en rango de fechas**

```
GET /api/v1/audit/date-range?startDate=2025-01-01T00:00:00&endDate=2025-01-31T23:59:59
```

### 5. **Obtener historial de un recurso específico**

```
GET /api/v1/audit/resource/{resource}/{resourceId}
```

**Ejemplo:**
```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/audit/resource/Task/123"
```

### 6. **Obtener operaciones fallidas**

```
GET /api/v1/audit/failures?page=0&size=20
```

### 7. **Contar fallos recientes**

```
GET /api/v1/audit/failures/count?hours=24
```

### 8. **Obtener estadísticas**

```
GET /api/v1/audit/statistics
```

**Respuesta:**
```json
{
  "totalOperations": 15234,
  "operationsLast24Hours": 423,
  "failuresLast24Hours": 12,
  "successRate": "97.16%",
  "mostFrequentActions": {
    "CREATE_TASK": 5432,
    "UPDATE_TASK": 3214,
    "DELETE_TASK": 1234
  },
  "mostActiveUsers": {
    "john.doe": 456,
    "jane.smith": 234
  }
}
```

### 9. **Detectar actividad sospechosa**

```
GET /api/v1/audit/suspicious-activity?hours=24
```

**Respuesta:**
```json
{
  "highFailureRate": {
    "count": 45,
    "threshold": 10,
    "severity": "WARNING",
    "message": "Se detectaron 45 operaciones fallidas en las últimas 24 horas"
  },
  "suspiciousUser_john.doe": {
    "username": "john.doe",
    "recentActions": 150,
    "threshold": 100,
    "severity": "INFO",
    "message": "Usuario john.doe realizó 150 acciones en las últimas 24 horas"
  }
}
```

---

## Base de Datos

### Tabla: audit_log

```sql
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100),
    resource_id BIGINT,
    description VARCHAR(500),
    class_name VARCHAR(255),
    method_name VARCHAR(100),
    parameters TEXT,
    status VARCHAR(20) NOT NULL,
    error_message VARCHAR(1000),
    exception_type VARCHAR(255),
    duration_ms BIGINT,
    timestamp TIMESTAMP NOT NULL,
    client_ip VARCHAR(45),
    user_agent VARCHAR(500)
);
```

### Índices creados automáticamente

- `idx_audit_username`: Para búsquedas por usuario
- `idx_audit_action`: Para búsquedas por acción
- `idx_audit_timestamp`: Para búsquedas por fecha
- `idx_audit_username_timestamp`: Índice compuesto (más eficiente)
- `idx_audit_action_status`: Para búsquedas de fallos por acción
- `idx_audit_resource_id`: Para historial de recursos
- `idx_audit_failures`: Índice parcial solo para fallos

---

## Ejemplos

### Ejemplo 1: Investigar quién modificó una tarea

```bash
# Obtener historial completo de la tarea con ID 123
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/audit/resource/Task/123"
```

**Respuesta:**
```json
{
  "content": [
    {
      "id": 1001,
      "username": "john.doe",
      "action": "UPDATE_TASK",
      "resource": "Task",
      "resourceId": 123,
      "description": "Usuario actualiza tarea existente",
      "status": "SUCCESS",
      "timestamp": "2025-01-15T14:30:00",
      "durationMs": 45
    },
    {
      "id": 789,
      "username": "jane.smith",
      "action": "CREATE_TASK",
      "resource": "Task",
      "resourceId": 123,
      "description": "Usuario crea nueva tarea",
      "status": "SUCCESS",
      "timestamp": "2025-01-10T09:15:00",
      "durationMs": 123
    }
  ],
  "totalElements": 2,
  "totalPages": 1
}
```

### Ejemplo 2: Detectar intentos de acceso no autorizado

```bash
# Ver todos los fallos en las últimas 24 horas
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/audit/failures?page=0&size=50"
```

### Ejemplo 3: Generar reporte mensual de un usuario

```bash
# Actividad de john.doe en enero 2025
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/audit/date-range?startDate=2025-01-01T00:00:00&endDate=2025-01-31T23:59:59&page=0&size=100"
```

---

## Mejores Prácticas

### 1. **Uso de @Auditable**

✅ **SÍ auditar:**
- Operaciones CRUD sobre recursos críticos
- Operaciones administrativas
- Cambios de configuración
- Exportación de datos
- Operaciones de seguridad (login, cambio de contraseña)

❌ **NO auditar:**
- Operaciones de consulta (GET) simples
- Health checks
- Endpoints públicos sin lógica de negocio

### 2. **Parámetros sensibles**

⚠️ **NUNCA loggear:**
- Contraseñas
- Tokens de autenticación
- Datos de tarjetas de crédito
- Información personal sensible

```java
// ❌ MAL - logParameters = true con contraseña
@Auditable(
    action = "CHANGE_PASSWORD",
    logParameters = true  // ¡Expondrá la contraseña!
)

// ✅ BIEN - logParameters = false
@Auditable(
    action = "CHANGE_PASSWORD",
    logParameters = false
)
```

### 3. **Retención de datos**

- Configurar política de retención según requisitos legales
- Típicamente: 1-7 años
- Implementar job periódico para archivar datos antiguos

```java
// Job para limpiar registros antiguos (configurar en @Scheduled)
@Scheduled(cron = "0 0 2 * * ?")  // Cada día a las 2 AM
public void cleanupOldAuditLogs() {
    int daysToKeep = 365;  // 1 año
    int deleted = auditLogService.cleanupOldRecords(daysToKeep);
    log.info("Limpieza de auditoría: {} registros eliminados", deleted);
}
```

### 4. **Monitoreo proactivo**

Configurar alertas automáticas:

```java
@Scheduled(fixedRate = 3600000)  // Cada hora
public void checkSuspiciousActivity() {
    Map<String, Object> alerts = auditLogService.detectSuspiciousActivity(1);

    if (!alerts.isEmpty() && !alerts.containsKey("status")) {
        // Enviar alerta a Slack, email, PagerDuty, etc.
        alertService.send("Actividad sospechosa detectada", alerts);
    }
}
```

### 5. **Performance**

- Los registros de auditoría se persisten de forma **síncrona**
- Para aplicaciones de alto rendimiento, considerar auditoría **asíncrona**
- Usar índices apropiados (ya creados en schema.sql)
- Particionar la tabla para grandes volúmenes (ver schema.sql)

---

## Troubleshooting

### Los registros no se están guardando en BD

1. Verificar que la tabla `audit_log` existe:
   ```sql
   SELECT * FROM audit_log LIMIT 1;
   ```

2. Verificar logs de la aplicación:
   ```bash
   grep "ERROR al persistir auditoría" logs/application.log
   ```

3. Verificar que AOP está habilitado:
   - Verificar que `spring-boot-starter-aop` está en `build.gradle`
   - Verificar que `@EnableAspectJAutoProxy` no está deshabilitado

### No puedo acceder a los endpoints de auditoría

1. Verificar autenticación:
   ```bash
   # Debe incluir header Authorization
   curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     "http://localhost:8080/api/v1/audit"
   ```

2. Verificar que el usuario tiene rol ADMIN:
   ```sql
   SELECT u.username, r.name
   FROM users u
   JOIN user_roles ur ON u.id = ur.user_id
   JOIN roles r ON ur.role_id = r.id
   WHERE u.username = 'tu_usuario';
   ```

---

## Conclusión

El sistema de auditoría AOP está completamente implementado y listo para usar. Proporciona:

✅ Trazabilidad completa de operaciones
✅ Cumplimiento normativo
✅ Detección de fraudes
✅ Análisis forense
✅ Reportes de actividad

**Próximos pasos:**

1. Configurar política de retención de datos
2. Implementar alertas automáticas
3. Integrar con sistema de monitoreo (Grafana, ELK, etc.)
4. Considerar auditoría asíncrona para mejor performance
5. Exportar auditoría a SIEM para análisis de seguridad avanzado
