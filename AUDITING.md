# 🔍 Auditoría Automática con Spring Data JPA

## ✅ Implementación completada

Este proyecto implementa auditoría automática usando **Spring Data JPA Auditing**, siguiendo las mejores prácticas de Spring Boot.

---

## 📚 ¿Qué es la auditoría?

La auditoría permite rastrear automáticamente:
- **Cuándo** se creó un registro (`@CreatedDate`)
- **Cuándo** se modificó por última vez (`@LastModifiedDate`)
- **Quién** lo creó (`@CreatedBy`) - Opcional, requiere autenticación
- **Quién** lo modificó (`@LastModifiedBy`) - Opcional, requiere autenticación

---

## 🏗️ Arquitectura implementada

### 1. Clase base `Auditable` (modelo/Auditable.java)

Clase abstracta que define los campos de auditoría comunes para todas las entidades.

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Campos opcionales (comentados por defecto):
    // @CreatedBy
    // private String createdBy;

    // @LastModifiedBy
    // private String lastModifiedBy;
}
```

**Ventajas:**
- ✅ Reutilizable en todas las entidades
- ✅ Un solo lugar para definir auditoría
- ✅ Fácil de mantener y extender

### 2. Entidad `Task` extiende `Auditable`

```java
@Entity
@Table(name = "tasks")
public class Task extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... otros campos

    // Ya no necesitas definir createdAt ni updatedAt aquí
    // Se heredan automáticamente de Auditable
}
```

### 3. Habilitar auditoría en la aplicación

```java
@SpringBootApplication
@EnableJpaAuditing  // ← Habilita la auditoría automática
public class TaskManagementApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApiApplication.class, args);
    }
}
```

---

## 🆚 Comparación: Hibernate vs Spring Data JPA

### ❌ Enfoque anterior (Hibernate - no recomendado)

```java
@CreationTimestamp
@Column(name = "created_at")
private LocalDateTime createdAt;

@UpdateTimestamp
@Column(name = "updated_at")
private LocalDateTime updatedAt;
```

**Desventajas:**
- Acoplado a Hibernate (proveedor específico)
- No permite auditoría de usuarios
- Menos extensible

### ✅ Enfoque actual (Spring Data JPA - mejor práctica)

```java
@CreatedDate
@Column(name = "created_at")
private LocalDateTime createdAt;

@LastModifiedDate
@Column(name = "updated_at")
private LocalDateTime updatedAt;
```

**Ventajas:**
- ✅ Estándar de Spring (independiente del proveedor JPA)
- ✅ Permite auditoría completa con usuarios
- ✅ Más extensible y configurable
- ✅ Mejor integración con el ecosistema Spring

---

## 🚀 Cómo funciona

### Creación de una tarea

```bash
POST /api/v1/tasks
{
  "title": "Nueva tarea",
  "description": "Descripción",
  "status": "PENDING"
}
```

**Resultado:**
```json
{
  "id": 1,
  "title": "Nueva tarea",
  "createdAt": "2025-11-12T15:11:13.488068",  ← Se rellena automáticamente
  "updatedAt": "2025-11-12T15:11:13.488068"   ← Igual que createdAt al crear
}
```

### Actualización de una tarea

```bash
PATCH /api/v1/tasks/1
{
  "status": "COMPLETED"
}
```

**Resultado:**
```json
{
  "id": 1,
  "title": "Nueva tarea",
  "createdAt": "2025-11-12T15:11:13.488068",  ← Se mantiene igual
  "updatedAt": "2025-11-12T15:11:32.566949"   ← Se actualiza automáticamente
}
```

---

## 📋 Archivos modificados/creados

1. **`src/main/java/com/taskmanagement/api/model/Auditable.java`** (NUEVO)
   - Clase base abstracta con campos de auditoría

2. **`src/main/java/com/taskmanagement/api/model/Task.java`** (MODIFICADO)
   - Ahora extiende de `Auditable`
   - Se eliminaron `@CreationTimestamp` y `@UpdateTimestamp`
   - Se eliminaron los campos `createdAt` y `updatedAt` (se heredan)

3. **`src/main/java/com/taskmanagement/api/TaskManagementApiApplication.java`** (MODIFICADO)
   - Se agregó `@EnableJpaAuditing`

4. **`src/main/java/com/taskmanagement/api/config/AuditorAwareConfig.java`** (NUEVO - OPCIONAL)
   - Configuración para auditoría de usuarios
   - Comentado por defecto (requiere autenticación)

---

## 🔮 Auditoría de usuarios (opcional)

### Cuándo habilitar

Habilita la auditoría de usuarios cuando implementes autenticación (Spring Security, JWT, etc.).

### Cómo habilitar

1. **Descomentar en `Auditable.java`:**
```java
@CreatedBy
@Column(name = "created_by", updatable = false)
private String createdBy;

@LastModifiedBy
@Column(name = "last_modified_by")
private String lastModifiedBy;
```

2. **Descomentar en `AuditorAwareConfig.java`:**
```java
@Configuration  // Descomentar
public class AuditorAwareConfig {

    @Bean  // Descomentar
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }
}
```

3. **Implementar lógica para obtener el usuario actual:**

**Con Spring Security:**
```java
@Override
public Optional<String> getCurrentAuditor() {
    return Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getName);
}
```

**Con JWT:**
```java
@Override
public Optional<String> getCurrentAuditor() {
    // Extraer usuario del token JWT
    String username = jwtTokenProvider.getUsernameFromToken(token);
    return Optional.ofNullable(username);
}
```

4. **Resultado con auditoría de usuarios:**
```json
{
  "id": 1,
  "title": "Nueva tarea",
  "createdAt": "2025-11-12T15:11:13",
  "updatedAt": "2025-11-12T15:11:32",
  "createdBy": "juan.perez",      ← Usuario que la creó
  "lastModifiedBy": "maria.lopez"  ← Usuario que la modificó
}
```

---

## 🎯 Aplicar auditoría a otras entidades

Para agregar auditoría a cualquier otra entidad:

```java
@Entity
@Table(name = "mi_entidad")
public class MiEntidad extends Auditable {  // ← Solo extiende Auditable

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ... tus campos

    // Los campos de auditoría se heredan automáticamente
}
```

¡Así de fácil! 🎉

---

## 📊 Base de datos

### Estructura de tabla con auditoría

```sql
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,     -- @CreatedDate
    updated_at TIMESTAMP NOT NULL,     -- @LastModifiedDate
    -- Opcional (para auditoría de usuarios):
    -- created_by VARCHAR(255),       -- @CreatedBy
    -- last_modified_by VARCHAR(255)  -- @LastModifiedBy
);
```

### Consulta para ver auditoría

```sql
SELECT
    id,
    title,
    created_at,
    updated_at,
    (updated_at - created_at) as time_since_creation
FROM tasks
ORDER BY updated_at DESC;
```

---

## 🔧 Configuración adicional

### Configurar zona horaria

Por defecto, Spring Data JPA usa la zona horaria del sistema. Para especificar una zona horaria:

```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          time_zone: UTC  # o Europe/Madrid, America/New_York, etc.
```

### Deshabilitar auditoría temporalmente

Si necesitas crear/actualizar entidades sin actualizar la auditoría:

```java
@EnableJpaAuditing(modifyOnCreate = false)  // No modifica en creación
// o
@EnableJpaAuditing(setDates = false)  // No establece fechas
```

---

## ✅ Checklist de implementación

- [x] Crear clase `Auditable` con `@CreatedDate` y `@LastModifiedDate`
- [x] Agregar `@EntityListeners(AuditingEntityListener.class)` a `Auditable`
- [x] Hacer que `Task` extienda `Auditable`
- [x] Habilitar `@EnableJpaAuditing` en la aplicación principal
- [x] Eliminar anotaciones Hibernate (`@CreationTimestamp`, `@UpdateTimestamp`)
- [x] Probar creación y actualización de tareas
- [ ] (Opcional) Implementar auditoría de usuarios con `@CreatedBy` y `@LastModifiedBy`
- [ ] (Opcional) Implementar `AuditorAware` para usuarios

---

## 🎓 Recursos adicionales

- [Spring Data JPA - Auditing](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#auditing)
- [Baeldung - JPA Auditing](https://www.baeldung.com/database-auditing-jpa)
- [Spring Boot Reference - JPA](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#data.sql.jpa-and-spring-data)

---

## 💡 Mejores prácticas

1. ✅ **Usar Spring Data JPA Auditing** en lugar de Hibernate Timestamps
2. ✅ **Crear clase base `Auditable`** para reutilización
3. ✅ **Marcar `createdAt` como `updatable = false`**
4. ✅ **Habilitar auditoría de usuarios** cuando tengas autenticación
5. ✅ **Usar `LocalDateTime`** para fechas (en lugar de `Date`)
6. ✅ **Documentar** los campos de auditoría en tu API

---

¡Auditoría implementada con éxito! 🎉
