# Gestión de Secretos y Variables de Entorno

## 📋 Índice
- [Introducción](#introducción)
- [Configuración Inicial](#configuración-inicial)
- [Variables de Entorno](#variables-de-entorno)
- [Seguridad](#seguridad)
- [Scripts Disponibles](#scripts-disponibles)
- [Entornos](#entornos)
- [Troubleshooting](#troubleshooting)
- [Mejores Prácticas](#mejores-prácticas)

---

## 🎯 Introducción

Este proyecto utiliza **variables de entorno** para gestionar configuraciones sensibles y secretos, siguiendo las mejores prácticas de seguridad recomendadas por [The Twelve-Factor App](https://12factor.net/config).

### ¿Por qué Variables de Entorno?

✅ **Seguridad**: Los secretos no se almacenan en el código fuente
✅ **Flexibilidad**: Diferentes configuraciones por entorno sin cambiar código
✅ **Separación**: Configuración separada del código
✅ **Escalabilidad**: Fácil integración con plataformas cloud y orquestadores
✅ **Auditoría**: Control de acceso a secretos independiente del código

---

## 🚀 Configuración Inicial

### Paso 1: Copiar archivo de ejemplo

```bash
# Copiar el archivo de ejemplo
cp .env.example .env

# Asignar permisos restrictivos (solo el propietario puede leer/escribir)
chmod 600 .env
```

### Paso 2: Configurar valores

Editar `.env` con tus configuraciones:

```bash
# Opción 1: Usar editor de texto
nano .env
# o
vim .env

# Opción 2: Generar secretos automáticamente
./scripts/generate-secrets.sh
```

### Paso 3: Cargar variables

```bash
# Cargar variables en la sesión actual
source scripts/load-env.sh

# Verificar que se cargaron
echo $DATABASE_URL
```

### Paso 4: Ejecutar la aplicación

```bash
# Opción 1: Con Gradle (carga automática de .env con plugin)
./gradlew bootRun

# Opción 2: Con variables cargadas manualmente
source scripts/load-env.sh
./gradlew bootRun

# Opción 3: Especificar archivo .env personalizado
ENV_FILE=.env.production ./gradlew bootRun
```

---

## 📝 Variables de Entorno

### Categorías de Variables

#### 🗄️ Base de Datos (Obligatorias)

| Variable | Descripción | Ejemplo | Default |
|----------|-------------|---------|---------|
| `DATABASE_URL` | URL de conexión JDBC | `jdbc:postgresql://localhost:5432/taskdb` | jdbc:postgresql://localhost:5432/taskmanagement_db |
| `DATABASE_USERNAME` | Usuario de PostgreSQL | `taskmanager_user` | postgres |
| `DATABASE_PASSWORD` | Contraseña de PostgreSQL | `***` | postgres |
| `DB_POOL_SIZE` | Tamaño del pool de conexiones | `20` | 20 |
| `DB_POOL_MIN_IDLE` | Conexiones idle mínimas | `10` | 10 |

#### 🚀 Servidor (Opcionales)

| Variable | Descripción | Ejemplo | Default |
|----------|-------------|---------|---------|
| `SERVER_PORT` | Puerto del servidor | `8080` | 8080 |
| `TOMCAT_THREADS_MAX` | Threads máximos | `200` | 200 |
| `TOMCAT_THREADS_MIN` | Threads mínimos | `10` | 10 |

#### 🔐 Seguridad (Obligatorias en Producción)

| Variable | Descripción | Ejemplo | Default |
|----------|-------------|---------|---------|
| `JWT_SECRET` | Clave secreta para JWT | `***` | (ver .env.example) |
| `JWT_EXPIRATION` | Duración del token (ms) | `3600000` | 86400000 |

#### 🛡️ Rate Limiting (Opcionales)

| Variable | Descripción | Ejemplo | Default |
|----------|-------------|---------|---------|
| `RATE_LIMIT_ENABLED` | Activar rate limiting | `true` | true |
| `RATE_LIMIT_CAPACITY` | Capacidad del bucket | `100` | 100 |
| `RATE_LIMIT_TOKENS` | Tokens por período | `100` | 100 |
| `RATE_LIMIT_PERIOD` | Período en minutos | `1` | 1 |
| `RATE_LIMIT_PER_IP` | Por IP o global | `true` | true |

#### 🌐 CORS (Opcionales)

| Variable | Descripción | Ejemplo | Default |
|----------|-------------|---------|---------|
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos | `http://localhost:3000` | localhost:3000,4200,5173 |

#### 📊 Logging (Opcionales)

| Variable | Descripción | Ejemplo | Default |
|----------|-------------|---------|---------|
| `LOGGING_LEVEL_ROOT` | Nivel de log global | `WARN` | INFO |
| `LOGGING_LEVEL_APP` | Nivel de log de la app | `INFO` | DEBUG |
| `LOG_FILE` | Ruta del archivo de logs | `/var/log/app.log` | logs/app.log |

#### 🎯 Perfiles (Opcionales)

| Variable | Descripción | Valores | Default |
|----------|-------------|---------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil de Spring Boot | `dev`, `test`, `prod` | (ninguno) |

---

## 🔒 Seguridad

### Reglas de Oro

⛔ **NUNCA** hacer commit de archivos `.env` al repositorio
⛔ **NUNCA** compartir secretos por email, Slack o chat
⛔ **NUNCA** usar contraseñas simples en producción
⛔ **NUNCA** reutilizar secretos entre entornos

✅ **SIEMPRE** usar contraseñas fuertes (mínimo 16 caracteres)
✅ **SIEMPRE** rotar secretos periódicamente (cada 90 días)
✅ **SIEMPRE** usar gestores de secretos en producción
✅ **SIEMPRE** limitar permisos del archivo `.env` (chmod 600)

### Generación de Secretos Seguros

#### Opción 1: Script automático

```bash
./scripts/generate-secrets.sh
```

#### Opción 2: Comandos manuales

```bash
# Password seguro (32 caracteres)
openssl rand -base64 32 | tr -d "=+/" | cut -c1-32

# JWT Secret (128 caracteres hexadecimales)
openssl rand -hex 64

# UUID (para API keys)
uuidgen
```

### Protección del Archivo .env

```bash
# Permisos restrictivos (solo propietario puede leer/escribir)
chmod 600 .env

# Verificar permisos
ls -la .env
# Debería mostrar: -rw------- 1 usuario grupo ...

# Verificar que está en .gitignore
grep ".env" .gitignore
```

### Verificación de Seguridad

```bash
# Verificar que .env NO está en Git
git status

# Buscar posibles secretos en el historial de Git
git log --all --full-history --source --  .env

# Si encuentras .env en Git, eliminarlo del historial:
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch .env" \
  --prune-empty --tag-name-filter cat -- --all
```

---

## 🛠️ Scripts Disponibles

### 1. load-env.sh

Carga variables de entorno desde `.env` en la sesión actual.

```bash
# Uso
source scripts/load-env.sh

# o
. scripts/load-env.sh

# Verificar variables cargadas
echo $DATABASE_URL
```

**Características:**
- ✅ Valida que `.env` existe
- ✅ Ignora comentarios y líneas vacías
- ✅ Oculta valores sensibles en el output
- ✅ Muestra resumen de configuración

### 2. generate-secrets.sh

Genera secretos aleatorios y seguros.

```bash
# Uso
./scripts/generate-secrets.sh
```

**Genera:**
- 🔑 Contraseña de base de datos (32 caracteres)
- 🔑 JWT Secret (128 caracteres hex)
- 🔑 Contraseña de pgAdmin (24 caracteres)

---

## 🌍 Entornos

### Desarrollo Local

```bash
# .env
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=jdbc:postgresql://localhost:5432/taskmanagement_db_dev
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
JWT_SECRET=development-secret-key-not-for-production
RATE_LIMIT_ENABLED=false
LOGGING_LEVEL_APP=DEBUG
```

### Testing

```bash
# .env.test
SPRING_PROFILES_ACTIVE=test
DATABASE_URL=jdbc:postgresql://localhost:5432/taskmanagement_db_test
DATABASE_USERNAME=test_user
DATABASE_PASSWORD=test_password
JWT_SECRET=test-secret-key
LOGGING_LEVEL_ROOT=WARN
```

### Staging

```bash
# .env.staging
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://staging-db.example.com:5432/taskdb
DATABASE_USERNAME=staging_user
DATABASE_PASSWORD=***SECRETO_FUERTE***
JWT_SECRET=***SECRETO_GENERADO_CON_OPENSSL***
JWT_EXPIRATION=3600000
RATE_LIMIT_ENABLED=true
RATE_LIMIT_CAPACITY=200
LOGGING_LEVEL_ROOT=INFO
```

### Producción

```bash
# Variables de entorno en el servidor (no usar archivo .env)
# Configurar en el sistema o gestor de secretos

export DATABASE_URL="jdbc:postgresql://prod-db.example.com:5432/taskdb"
export DATABASE_USERNAME="prod_user"
export DATABASE_PASSWORD="***SECRETO_FUERTE***"
export JWT_SECRET="***SECRETO_GENERADO_CON_OPENSSL***"
export JWT_EXPIRATION="3600000"
export RATE_LIMIT_ENABLED="true"
export LOGGING_LEVEL_ROOT="WARN"
export LOGGING_LEVEL_APP="INFO"
```

---

## 🐛 Troubleshooting

### Problema: Variables no se cargan

```bash
# Verificar que el archivo existe
ls -la .env

# Verificar sintaxis del archivo
cat .env | grep "="

# Intentar cargar manualmente
source scripts/load-env.sh

# Verificar variables
env | grep DATABASE
```

### Problema: Aplicación no encuentra variables

```bash
# Verificar que están exportadas
echo $DATABASE_URL

# Si está vacío, cargar nuevamente
source scripts/load-env.sh

# Verificar en la aplicación
./gradlew bootRun --info | grep DATABASE
```

### Problema: Error de permisos en .env

```bash
# Verificar permisos actuales
ls -la .env

# Corregir permisos
chmod 600 .env

# Verificar propietario
whoami
ls -la .env
# Deberían coincidir
```

### Problema: .env fue commiteado a Git

```bash
# Ver si está en Git
git status

# Si aparece staged, remover
git reset HEAD .env

# Si ya fue commiteado, ver historial
git log --all -- .env

# Remover del historial (PELIGROSO - respaldar primero)
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch .env" \
  --prune-empty --tag-name-filter cat -- --all

# Forzar push (coordinar con equipo)
git push origin --force --all
```

---

## 🎯 Mejores Prácticas

### ✅ Desarrollo

```bash
# 1. Nunca usar credenciales de producción localmente
DATABASE_PASSWORD=local_dev_password  # ✅ OK

# 2. Usar valores de ejemplo para testing
JWT_SECRET=dev-secret-not-for-production  # ✅ OK

# 3. Deshabilitar seguridad estricta en dev
RATE_LIMIT_ENABLED=false  # ✅ OK para desarrollo
```

### ✅ Producción

```bash
# 1. Usar gestores de secretos
# AWS Secrets Manager, HashiCorp Vault, Azure Key Vault, etc.

# 2. Rotar secretos periódicamente
# JWT_SECRET: cada 90 días
# DATABASE_PASSWORD: cada 90 días

# 3. Usar contraseñas fuertes (mínimo 16 caracteres)
DATABASE_PASSWORD=$(openssl rand -base64 32)

# 4. Limitar acceso a secretos
# Solo el equipo DevOps debe tener acceso

# 5. Auditar cambios
# Registrar quién y cuándo accede a secretos
```

### ✅ CI/CD

```bash
# GitHub Actions
env:
  DATABASE_URL: ${{ secrets.DATABASE_URL }}
  JWT_SECRET: ${{ secrets.JWT_SECRET }}

# GitLab CI
variables:
  DATABASE_URL: ${DATABASE_URL}
  JWT_SECRET: ${JWT_SECRET}

# Jenkins
withCredentials([string(credentialsId: 'db-password', variable: 'DATABASE_PASSWORD')]) {
  sh './gradlew bootRun'
}
```

### ✅ Docker

```bash
# docker-compose.yml
services:
  app:
    environment:
      - DATABASE_URL=${DATABASE_URL}
      - JWT_SECRET=${JWT_SECRET}
    env_file:
      - .env

# O usar Docker secrets (recomendado)
secrets:
  db_password:
    external: true
```

### ✅ Kubernetes

```yaml
# ConfigMap para valores no sensibles
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  SERVER_PORT: "8080"
  RATE_LIMIT_ENABLED: "true"

---
# Secret para valores sensibles
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
stringData:
  DATABASE_PASSWORD: "***"
  JWT_SECRET: "***"
```

---

## 📚 Referencias

- [The Twelve-Factor App - Config](https://12factor.net/config)
- [OWASP - Secrets Management](https://owasp.org/www-community/vulnerabilities/Use_of_hard-coded_password)
- [Spring Boot - Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [HashiCorp Vault](https://www.vaultproject.io/)
- [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/)

---

## ✅ Checklist de Seguridad

Antes de ir a producción, verificar:

- [ ] Archivo `.env` está en `.gitignore`
- [ ] No hay archivos `.env` en el historial de Git
- [ ] Todas las contraseñas son fuertes (mínimo 16 caracteres)
- [ ] JWT secret es único y generado con `openssl rand -hex 64`
- [ ] Permisos de `.env` son restrictivos (`chmod 600`)
- [ ] Secretos están en gestor de secretos (no en archivos)
- [ ] Variables de producción difieren de desarrollo
- [ ] Credenciales de base de datos son únicas por entorno
- [ ] Rate limiting está habilitado en producción
- [ ] Logs no muestran secretos (contraseñas, tokens, etc.)
- [ ] SSL/TLS está habilitado para conexiones a BD
- [ ] Backup de secretos está en lugar seguro
- [ ] Plan de rotación de secretos está definido
- [ ] Solo personal autorizado tiene acceso a secretos
- [ ] Hay auditoría de acceso a secretos

---

**Implementado en:** Task Management API v1.0.0
**Fecha:** 2025-11-15
**Tecnología:** Spring Boot 3.5.7 + PostgreSQL 18
