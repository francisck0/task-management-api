# Inicio Rápido - Task Management API

Guía para poner en marcha el proyecto en menos de 5 minutos.

## Paso 1: Configurar Variables de Entorno

```bash
cp .env.example .env
```

## Paso 2: Levantar PostgreSQL 18 con Docker

```bash
docker compose up -d
```

Esto iniciará:
- **PostgreSQL 18** en el puerto 5432 con configuración optimizada
- **pgAdmin 4** en http://localhost:5050 (interfaz web para administración)

## Paso 3: Ejecutar la aplicación

```bash
./gradlew bootRun
```

## Paso 4: Probar la API

### Crear una tarea
```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Mi primera tarea",
    "description": "Probar la API",
    "status": "PENDING"
  }'
```

### Ver todas las tareas
```bash
curl http://localhost:8080/api/v1/tasks
```

### Ver estadísticas
```bash
curl http://localhost:8080/api/v1/tasks/statistics
```

## ¡Listo! 🎉

Tu API está funcionando en: **http://localhost:8080/api/v1**

## Detener servicios

```bash
# Detener la aplicación Spring Boot: Ctrl+C

# Detener PostgreSQL
docker compose down

# O usar el Makefile
make docker-down
```

## Acceso a pgAdmin (Opcional)

1. Abrir http://localhost:5050
2. Login: admin@admin.com / admin
3. Agregar servidor:
   - Host: postgres
   - Port: 5432
   - User: postgres
   - Password: postgres

## Siguiente paso

Consulta el [README.md](README.md) para documentación completa.
