# Rate Limiting - Documentación Técnica

## 📋 Índice
- [Qué es Rate Limiting](#qué-es-rate-limiting)
- [Implementación](#implementación)
- [Configuración](#configuración)
- [Algoritmo Token Bucket](#algoritmo-token-bucket)
- [Pruebas](#pruebas)
- [Headers HTTP](#headers-http)
- [Endpoints de Administración](#endpoints-de-administración)
- [Casos de Uso](#casos-de-uso)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Qué es Rate Limiting

**Rate Limiting** es una técnica de seguridad que limita el número de peticiones que un cliente puede hacer a una API en un período de tiempo determinado.

### Beneficios

✅ **Protección contra ataques DDoS**: Limita el impacto de ataques de denegación de servicio
✅ **Uso justo de recursos**: Garantiza que ningún cliente monopolice los recursos del servidor
✅ **Prevención de abuso**: Evita que bots o scripts maliciosos sobrecarguen la API
✅ **Estabilidad del sistema**: Mantiene la API respondiendo de manera predecible
✅ **Control de costos**: Reduce el uso innecesario de recursos computacionales

---

## 🏗️ Implementación

La implementación de Rate Limiting en este proyecto utiliza:

- **Biblioteca**: Bucket4j 8.10.1
- **Algoritmo**: Token Bucket
- **Estrategia**: Por IP del cliente (configurable)
- **Almacenamiento**: En memoria (ConcurrentHashMap)

### Componentes Principales

```
src/main/java/com/taskmanagement/api/
├── config/
│   └── RateLimitProperties.java          # Propiedades de configuración
├── service/
│   └── RateLimitService.java             # Lógica de rate limiting
├── filter/
│   └── RateLimitFilter.java              # Filtro HTTP
└── controller/
    └── RateLimitAdminController.java     # Endpoints de administración
```

---

## ⚙️ Configuración

### application.yml

```yaml
rate-limit:
  # Habilitar/deshabilitar rate limiting
  enabled: true

  # Capacidad máxima del bucket (tokens)
  capacity: 100

  # Tokens que se rellenan por período
  tokens: 100

  # Período de rellenado (en minutos)
  period: 1

  # Rate limiting por IP (true) o global (false)
  per-ip: true

  # Paths excluidos del rate limiting
  excluded-paths:
    - /actuator/**
    - /swagger-ui/**
    - /v3/api-docs/**
    - /favicon.ico
```

### Variables de Entorno

Puedes sobrescribir la configuración usando variables de entorno:

```bash
# Habilitar/deshabilitar
export RATE_LIMIT_ENABLED=true

# Límite de peticiones
export RATE_LIMIT_CAPACITY=100
export RATE_LIMIT_TOKENS=100

# Período (en minutos)
export RATE_LIMIT_PERIOD=1

# Por IP o global
export RATE_LIMIT_PER_IP=true
```

---

## 🪣 Algoritmo Token Bucket

### ¿Cómo Funciona?

1. **Cada cliente tiene un "bucket" (cubo) virtual con tokens**
2. **Cada petición consume 1 token**
3. **Los tokens se rellenan automáticamente a una tasa constante**
4. **Si no hay tokens disponibles, la petición es rechazada (HTTP 429)**

### Ejemplo Visual

```
Configuración: capacity=5, tokens=5, period=1 minuto

Tiempo 0s:  [●●●●●]  5 tokens disponibles
Petición 1: [●●●●○]  Éxito - 4 tokens restantes
Petición 2: [●●●○○]  Éxito - 3 tokens restantes
Petición 3: [●●○○○]  Éxito - 2 tokens restantes
Petición 4: [●○○○○]  Éxito - 1 token restante
Petición 5: [○○○○○]  Éxito - 0 tokens restantes
Petición 6: [○○○○○]  ❌ RECHAZADA (429 Too Many Requests)

Tiempo 60s: [●●●●●]  Tokens rellenados - 5 tokens disponibles
```

### Ventajas del Algoritmo

✅ **Permite bursts controlados**: Puedes hacer varias peticiones rápidas hasta la capacidad
✅ **Tasa constante a largo plazo**: Garantiza un promedio de peticiones por período
✅ **Justo y predecible**: Fácil de entender para los clientes de la API
✅ **Eficiente**: Bajo overhead computacional y de memoria

---

## 🧪 Pruebas

### Prueba 1: Verificar Rate Limiting Básico

```bash
# Hacer 5 peticiones rápidas (deberían pasar todas)
for i in {1..5}; do
  curl -i http://localhost:8080/api/v1/tasks
done

# Si capacity=5, la 6ta petición debería fallar con 429
curl -i http://localhost:8080/api/v1/tasks
```

### Prueba 2: Verificar Headers de Rate Limit

```bash
curl -i http://localhost:8080/api/v1/tasks
```

**Headers esperados:**
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99
```

### Prueba 3: Exceder el Límite

```bash
# Script para exceder el límite
for i in {1..150}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/tasks
done
```

**Respuestas esperadas:**
- Primeras 100: `200 OK`
- Siguientes: `429 Too Many Requests`

### Prueba 4: Verificar Respuesta 429

```bash
# Hacer muchas peticiones para exceder el límite
for i in {1..150}; do curl -s http://localhost:8080/api/v1/tasks > /dev/null; done

# La siguiente debería retornar 429 con detalles
curl -i http://localhost:8080/api/v1/tasks
```

**Respuesta esperada:**
```json
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
Retry-After: 60

{
  "timestamp": "2025-11-15T19:30:00",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Has excedido el límite de peticiones. Intenta nuevamente más tarde.",
  "limit": 100,
  "retryAfter": "60 segundos"
}
```

### Prueba 5: Paths Excluidos

```bash
# Actuator no debería tener rate limiting
for i in {1..200}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/api/v1/actuator/health
done
# Todas deberían retornar 200
```

---

## 📡 Headers HTTP

### Headers en Respuestas Exitosas

| Header | Descripción | Ejemplo |
|--------|-------------|---------|
| `X-RateLimit-Limit` | Límite total de peticiones | `100` |
| `X-RateLimit-Remaining` | Peticiones restantes | `95` |

### Headers en Respuesta 429 (Límite Excedido)

| Header | Descripción | Ejemplo |
|--------|-------------|---------|
| `X-RateLimit-Limit` | Límite total de peticiones | `100` |
| `X-RateLimit-Remaining` | Siempre 0 cuando se excede | `0` |
| `Retry-After` | Segundos hasta que se rellenen tokens | `60` |

---

## 🔧 Endpoints de Administración

### GET /admin/rate-limit/info

Obtiene la configuración actual del rate limiting.

```bash
curl http://localhost:8080/api/v1/admin/rate-limit/info
```

**Respuesta:**
```json
{
  "enabled": true,
  "capacity": 100,
  "tokens": 100,
  "periodMinutes": 1,
  "perIp": true,
  "excludedPaths": ["/actuator/**", "/swagger-ui/**"],
  "description": "Permite 100 peticiones cada 1 minuto(s) por IP"
}
```

### GET /admin/rate-limit/stats

Obtiene estadísticas del sistema.

```bash
curl http://localhost:8080/api/v1/admin/rate-limit/stats
```

**Respuesta:**
```json
{
  "activeBuckets": 42,
  "enabled": true,
  "capacity": 100,
  "tokensPerPeriod": 100,
  "periodMinutes": 1
}
```

### POST /admin/rate-limit/clear-cache

Limpia la caché de buckets (resetea todos los límites).

```bash
curl -X POST http://localhost:8080/api/v1/admin/rate-limit/clear-cache
```

**Respuesta:**
```json
{
  "message": "Caché de rate limiting limpiada exitosamente",
  "bucketsCleared": 42
}
```

⚠️ **ADVERTENCIA:** Esto resetea los límites de todos los clientes.

---

## 💡 Casos de Uso

### 1. API Pública Gratuita (Restrictiva)

```yaml
rate-limit:
  capacity: 50
  tokens: 50
  period: 1
```
**Resultado:** 50 peticiones/minuto (restrictivo)

### 2. API con Autenticación (Moderada)

```yaml
rate-limit:
  capacity: 200
  tokens: 200
  period: 1
```
**Resultado:** 200 peticiones/minuto (moderado)

### 3. API Interna (Generosa)

```yaml
rate-limit:
  capacity: 1000
  tokens: 1000
  period: 1
```
**Resultado:** 1000 peticiones/minuto (generoso)

### 4. Rate Limiting por Hora

```yaml
rate-limit:
  capacity: 5000
  tokens: 5000
  period: 60
```
**Resultado:** 5000 peticiones/hora

### 5. Diferentes Límites por Entorno

**Desarrollo:**
```yaml
rate-limit:
  enabled: false  # Deshabilitado para facilitar desarrollo
```

**Staging:**
```yaml
rate-limit:
  enabled: true
  capacity: 500
  tokens: 500
```

**Producción:**
```yaml
rate-limit:
  enabled: true
  capacity: 100
  tokens: 100
```

---

## 🐛 Troubleshooting

### Problema: Rate limiting no funciona

**Verificar:**
1. ¿Está habilitado en la configuración?
   ```bash
   curl http://localhost:8080/api/v1/admin/rate-limit/info
   ```

2. ¿El path está excluido?
   - Verificar `excluded-paths` en application.yml

3. ¿Hay errores en los logs?
   ```bash
   grep "RateLimit" logs/application.log
   ```

### Problema: Límite se excede muy rápido

**Solución:**
- Aumentar `capacity` y `tokens`
- Aumentar `period` (ej: de 1 a 5 minutos)

```yaml
rate-limit:
  capacity: 500
  tokens: 500
  period: 5
```

### Problema: Clientes legítimos siendo bloqueados

**Soluciones:**

1. **Aumentar límites:**
   ```yaml
   rate-limit:
     capacity: 200
     tokens: 200
   ```

2. **Excluir paths específicos:**
   ```yaml
   rate-limit:
     excluded-paths:
       - /actuator/**
       - /auth/login
       - /public/**
   ```

3. **Limpiar caché temporalmente:**
   ```bash
   curl -X POST http://localhost:8080/api/v1/admin/rate-limit/clear-cache
   ```

### Problema: Muchos buckets en memoria

**Causa:** Muchos clientes diferentes (IPs) accediendo a la API

**Solución:** Implementar limpieza periódica de buckets inactivos (no incluido en esta versión)

**Workaround temporal:**
```bash
# Limpiar caché periódicamente
curl -X POST http://localhost:8080/api/v1/admin/rate-limit/clear-cache
```

### Problema: Rate limiting por IP no funciona detrás de proxy

**Causa:** El filtro obtiene la IP del proxy en lugar del cliente real

**Solución:** Configurar headers de proxy correctamente

El filtro ya está preparado para leer:
- `X-Forwarded-For`
- `X-Real-IP`

Asegurar que el proxy/load balancer envía estos headers.

---

## 🔒 Seguridad

### Recomendaciones de Producción

1. ✅ **Habilitar rate limiting:**
   ```yaml
   rate-limit:
     enabled: true
   ```

2. ✅ **Proteger endpoints de administración:**
   - Agregar autenticación (Spring Security)
   - Limitar por IP (firewall)
   - Usar API keys

3. ✅ **Configurar límites apropiados:**
   - No demasiado restrictivo (usuarios legítimos)
   - No demasiado generoso (ataques efectivos)

4. ✅ **Monitorear métricas:**
   - Número de peticiones rechazadas (429)
   - IPs con más rechazos
   - Endpoints más afectados

5. ✅ **Configurar alertas:**
   - Alerta cuando muchas peticiones son rechazadas
   - Alerta cuando un cliente específico es bloqueado repetidamente

---

## 📊 Monitoreo

### Métricas Disponibles

**Vía Actuator Prometheus:**
```bash
curl http://localhost:8080/api/v1/actuator/prometheus | grep rate_limit
```

### Logs

El sistema registra eventos importantes:

```
INFO  - Cliente 192.168.1.100 - Tokens restantes: 95
WARN  - Rate limit excedido para cliente: 192.168.1.100 - Límite: 100/1min
INFO  - Caché de rate limiting limpiada
```

---

## 🚀 Mejoras Futuras

Posibles mejoras para considerar:

1. **Rate limiting por usuario autenticado** (además de IP)
2. **Diferentes límites por endpoint** (ej: login más restrictivo)
3. **Límites por tier/plan** (gratuito, premium, enterprise)
4. **Almacenamiento distribuido** (Redis) para múltiples instancias
5. **Limpieza automática de buckets inactivos**
6. **Métricas más detalladas** (por endpoint, por usuario, etc.)
7. **Dashboard de monitoreo** en tiempo real
8. **Whitelist de IPs** (sin rate limiting)
9. **Blacklist de IPs** (bloqueo permanente)
10. **Rate limiting adaptativo** (basado en carga del servidor)

---

## 📚 Referencias

- [Bucket4j Documentation](https://bucket4j.com/)
- [Token Bucket Algorithm](https://en.wikipedia.org/wiki/Token_bucket)
- [HTTP 429 Too Many Requests](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status/429)
- [Rate Limiting Best Practices](https://cloud.google.com/architecture/rate-limiting-strategies-techniques)

---

**Implementado en:** Task Management API v1.0.0
**Fecha:** 2025-11-15
**Tecnología:** Spring Boot 3.5.7 + Bucket4j 8.10.1
