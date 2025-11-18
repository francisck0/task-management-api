# Seguridad JWT - Task Management API

## 📋 Tabla de Contenidos

- [Introducción](#introducción)
- [Problema de Seguridad](#problema-de-seguridad)
- [Validación al Startup](#validación-al-startup)
- [Configuración Segura](#configuración-segura)
- [Guía Rápida](#guía-rápida)
- [Validaciones Implementadas](#validaciones-implementadas)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)

---

## Introducción

Este proyecto implementa **validación de seguridad JWT al startup** para prevenir el uso de secrets por defecto inseguros en producción.

### ⚠️ IMPORTANTE

La aplicación **NO arrancará** si detecta que estás usando el JWT secret por defecto. Esto es intencional y es una medida de seguridad crítica.

---

## Problema de Seguridad

### Secret por Defecto Inseguro

El código incluye un JWT secret por defecto **SOLO para facilitar el desarrollo local**:

```
404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

### ¿Por qué es peligroso?

1. **Conocido públicamente**: Está en el código fuente (repositorio público)
2. **Compromiso total**: Un atacante puede:
   - Generar tokens JWT válidos
   - Suplantar cualquier usuario
   - Acceso total a la API sin credenciales
   - Exfiltrar, modificar o eliminar datos

### Impacto en Producción

```
┌─────────────────────────────────────────────────────────┐
│  🚨 Si usas el secret por defecto en producción:       │
│                                                         │
│  ✗ Cualquiera puede generar tokens válidos            │
│  ✗ Zero seguridad en autenticación                     │
│  ✗ Compromiso completo del sistema                     │
│  ✗ Pérdida de confianza de usuarios                    │
│  ✗ Posibles implicaciones legales (GDPR, etc)          │
└─────────────────────────────────────────────────────────┘
```

---

## Validación al Startup

### JwtSecretValidator

La clase `JwtSecretValidator` se ejecuta **automáticamente al iniciar la aplicación** y realiza las siguientes validaciones:

#### ✅ Validación 1: Secret no vacío

```java
if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
    throw IllegalStateException("JWT secret no configurado");
}
```

#### ✅ Validación 2: No es el valor por defecto

```java
if (INSECURE_DEFAULT_SECRET.equals(jwtSecret)) {
    throw IllegalStateException("JWT secret por defecto detectado");
}
```

#### ✅ Validación 3: Longitud mínima

```java
if (jwtSecret.length() < 32) {
    throw IllegalStateException("JWT secret demasiado corto");
}
```

### Comportamiento al Detectar Problema

Si alguna validación falla, la aplicación:

1. ❌ **NO arranca** (lanza `IllegalStateException`)
2. 📝 Registra error detallado en logs
3. 📚 Muestra instrucciones de configuración
4. 🛡️ Previene exposición de seguridad

### Ejemplo de Error en Logs

```
╔════════════════════════════════════════════════════════════════╗
║  ⚠️  CONFIGURACIÓN DE SEGURIDAD INSEGURA DETECTADA  ⚠️        ║
╚════════════════════════════════════════════════════════════════╝

El JWT secret configurado es el valor por defecto.
Este valor es CONOCIDO PÚBLICAMENTE y NO debe usarse en producción.

═══════════════════════════════════════════════════════════════
RIESGO DE SEGURIDAD:
═══════════════════════════════════════════════════════════════
• Un atacante puede generar tokens JWT válidos
• Suplantar la identidad de cualquier usuario
• Acceso total a la API sin credenciales
• Compromiso completo de la seguridad del sistema

═══════════════════════════════════════════════════════════════
SOLUCIÓN - Configurar un JWT secret seguro:
═══════════════════════════════════════════════════════════════

1. Generar un secret fuerte:
   $ openssl rand -base64 64
   $ ./scripts/generate-secrets.sh

2. Configurar como variable de entorno:
   Linux/Mac:
     $ export JWT_SECRET="tu-secret-generado"

   Windows (PowerShell):
     $ $env:JWT_SECRET="tu-secret-generado"

   Docker Compose:
     environment:
       - JWT_SECRET=tu-secret-generado

   Kubernetes:
     kubectl create secret generic jwt-secret \
       --from-literal=JWT_SECRET=tu-secret-generado

3. Reiniciar la aplicación
```

---

## Configuración Segura

### Paso 1: Generar un Secret Fuerte

#### Opción 1: OpenSSL (Recomendado)

```bash
# Genera un secret de 64 caracteres en Base64
openssl rand -base64 64
```

**Ejemplo de output**:
```
X9mK2pL5vN8qR4tY6wE7sA9bC1dF3gH5jK8mP0qS2uV4xZ7aB9cD1eG3hJ6kM8nQ
```

#### Opción 2: Script del Proyecto

```bash
# Ejecutar script incluido en el proyecto
./scripts/generate-secrets.sh
```

Este script genera todos los secrets necesarios, incluyendo JWT secret.

#### Opción 3: Comando Python (si OpenSSL no disponible)

```bash
python3 -c "import secrets; print(secrets.token_urlsafe(64))"
```

---

### Paso 2: Configurar el Secret

Elige el método según tu entorno:

#### 🖥️ Desarrollo Local

**Opción A: Variable de Entorno**

```bash
# Linux/Mac
export JWT_SECRET="tu-secret-generado-aqui"

# Windows (PowerShell)
$env:JWT_SECRET="tu-secret-generado-aqui"

# Windows (CMD)
set JWT_SECRET=tu-secret-generado-aqui
```

**Opción B: Archivo .env** (Recomendado para desarrollo)

```bash
# Crear archivo .env en el root del proyecto
echo 'JWT_SECRET=tu-secret-generado-aqui' >> .env

# El archivo .env ya está en .gitignore (NO se commitea)
```

Luego usar una librería como `dotenv` o configurar tu IDE para cargar el .env.

---

#### 🐳 Docker / Docker Compose

**docker-compose.yml**:

```yaml
version: '3.8'
services:
  app:
    image: task-management-api:latest
    environment:
      - JWT_SECRET=${JWT_SECRET}  # Lee de variable de entorno del host
      # O hardcodeado (NO recomendado):
      # - JWT_SECRET=tu-secret-aqui
    env_file:
      - .env  # Carga desde archivo .env (recomendado)
```

**Ejecutar con variable de entorno**:

```bash
# Opción 1: Exportar variable antes
export JWT_SECRET="tu-secret-generado"
docker-compose up

# Opción 2: Inline
JWT_SECRET="tu-secret-generado" docker-compose up

# Opción 3: Archivo .env
# Crear .env con JWT_SECRET=...
docker-compose up
```

---

#### ☸️ Kubernetes

**Paso 1: Crear Secret en Kubernetes**

```bash
# Crear secret desde línea de comandos
kubectl create secret generic jwt-secret \
  --from-literal=JWT_SECRET='tu-secret-generado-aqui' \
  --namespace=production

# O desde archivo
echo -n 'tu-secret-generado-aqui' > /tmp/jwt-secret.txt
kubectl create secret generic jwt-secret \
  --from-file=JWT_SECRET=/tmp/jwt-secret.txt \
  --namespace=production
rm /tmp/jwt-secret.txt  # Limpiar archivo temporal
```

**Paso 2: Referenciar en Deployment**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: task-management-api
spec:
  template:
    spec:
      containers:
      - name: api
        image: task-management-api:latest
        env:
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: JWT_SECRET
```

---

#### ☁️ Cloud Providers

##### AWS (Secrets Manager)

```bash
# 1. Crear secret en AWS Secrets Manager
aws secretsmanager create-secret \
  --name task-management/jwt-secret \
  --secret-string "tu-secret-generado-aqui"

# 2. En tu aplicación (ECS, Lambda, EC2), configurar IAM role
# 3. Obtener secret en tiempo de ejecución o mediante variables de entorno
```

##### Azure (Key Vault)

```bash
# 1. Crear secret en Azure Key Vault
az keyvault secret set \
  --vault-name "mi-keyvault" \
  --name "jwt-secret" \
  --value "tu-secret-generado-aqui"

# 2. Configurar managed identity para acceso
# 3. Referenciar en Azure App Service configuration
```

##### Google Cloud (Secret Manager)

```bash
# 1. Crear secret
echo -n "tu-secret-generado-aqui" | \
  gcloud secrets create jwt-secret \
    --data-file=-

# 2. Referenciar en Cloud Run, GKE, etc.
```

---

#### 🔧 CI/CD (GitHub Actions)

**Configurar secret en GitHub**:

```bash
# Settings > Secrets and variables > Actions > New repository secret
# Name: JWT_SECRET
# Value: tu-secret-generado-aqui
```

**Usar en workflow**:

```yaml
# .github/workflows/deploy.yml
jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Deploy
        env:
          JWT_SECRET: ${{ secrets.JWT_SECRET }}
        run: |
          # Tu comando de deployment
```

---

### Paso 3: Verificar Configuración

#### Iniciar la aplicación

```bash
./gradlew bootRun
```

#### Verificar logs

Si el secret está **correctamente configurado**:

```
=============================================================
Validando configuración de seguridad JWT...
Perfil activo: dev
=============================================================
✅ JWT secret validado correctamente
   - Longitud: 88 caracteres
   - No es el valor por defecto: ✓
   - Cumple longitud mínima: ✓
=============================================================
Validación de seguridad JWT completada exitosamente
=============================================================
```

Si el secret es **inseguro** (por defecto):

```
╔════════════════════════════════════════════════════════════════╗
║  ⚠️  CONFIGURACIÓN DE SEGURIDAD INSEGURA DETECTADA  ⚠️        ║
╚════════════════════════════════════════════════════════════════╝

[ERROR] La aplicación NO arrancará con el secret por defecto
[INSTRUCCIONES detalladas en logs...]
```

---

## Guía Rápida

### TL;DR - Setup en 3 pasos

```bash
# 1. Generar secret
JWT_SECRET=$(openssl rand -base64 64)

# 2. Exportar variable
export JWT_SECRET

# 3. Iniciar aplicación
./gradlew bootRun
```

### Para Desarrollo Local

```bash
# Crear .env
echo "JWT_SECRET=$(openssl rand -base64 64)" > .env

# Cargar .env y ejecutar
source .env && ./gradlew bootRun
```

### Para Docker

```bash
# Generar y guardar en .env
echo "JWT_SECRET=$(openssl rand -base64 64)" > .env

# Ejecutar con docker-compose
docker-compose up
```

### Para Producción (Kubernetes)

```bash
# 1. Generar secret
JWT_SECRET=$(openssl rand -base64 64)

# 2. Crear secret en Kubernetes
kubectl create secret generic jwt-secret \
  --from-literal=JWT_SECRET="$JWT_SECRET" \
  --namespace=production

# 3. Deploy
kubectl apply -f k8s/deployment.yml
```

---

## Validaciones Implementadas

### Resumen de Validaciones

| Validación | Criterio | Acción si Falla |
|------------|----------|-----------------|
| Secret no vacío | `secret != null && secret != ""` | ❌ IllegalStateException |
| No es por defecto | `secret != "404E635..."` | ❌ IllegalStateException |
| Longitud mínima | `secret.length >= 32` | ❌ IllegalStateException |
| Perfil producción | `profile == "prod"` | ⚠️ Warning adicional |

### Archivos Involucrados

```
src/main/java/com/taskmanagement/api/
├── config/
│   └── JwtSecretValidator.java    ← Validador (ejecuta al startup)
├── service/
│   └── JwtService.java             ← Servicio que usa el secret
└── resources/
    └── application.yml             ← Configuración con documentación
```

### Diagrama de Flujo

```
┌─────────────────────┐
│  Aplicación Inicia  │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────────────┐
│  @PostConstruct ejecuta     │
│  JwtSecretValidator         │
└──────────┬──────────────────┘
           │
           ▼
     ┌────────────┐
     │ ¿Vacío?    │────Yes────► ❌ FALLA
     └─────┬──────┘
           No
           ▼
     ┌────────────┐
     │ ¿Default?  │────Yes────► ❌ FALLA
     └─────┬──────┘
           No
           ▼
     ┌────────────┐
     │ ¿Corto?    │────Yes────► ❌ FALLA
     └─────┬──────┘
           No
           ▼
     ┌────────────┐
     │ ✅ VÁLIDO  │
     └─────┬──────┘
           │
           ▼
┌─────────────────────┐
│  Aplicación Continúa│
└─────────────────────┘
```

---

## Troubleshooting

### ❌ Error: "JWT secret es el valor por defecto INSEGURO"

**Causa**: No has configurado la variable de entorno `JWT_SECRET`.

**Solución**:

```bash
# 1. Generar secret
openssl rand -base64 64

# 2. Configurar variable
export JWT_SECRET="secret-generado-en-paso-1"

# 3. Reiniciar aplicación
./gradlew bootRun
```

---

### ❌ Error: "JWT secret demasiado corto"

**Causa**: El secret configurado tiene menos de 32 caracteres.

**Solución**:

```bash
# Generar uno nuevo de 64 caracteres
export JWT_SECRET=$(openssl rand -base64 64)
```

---

### ❌ Error: "JWT secret no configurado"

**Causa**: La variable `JWT_SECRET` no está definida y no hay valor por defecto.

**Solución**:

```bash
# Verificar que la variable esté exportada
echo $JWT_SECRET

# Si está vacía, configurar
export JWT_SECRET=$(openssl rand -base64 64)
```

---

### ⚠️ Warning: "AMBIENTE DE PRODUCCIÓN DETECTADO"

**Causa**: La aplicación detectó perfil `prod` o `production`.

**Acción**: Esto es solo informativo. Asegúrate de:

- ✅ Secret está en sistema seguro (AWS Secrets, Azure Key Vault, etc.)
- ✅ No está hardcodeado en código
- ✅ Tiene permisos de acceso restringidos
- ✅ Se rota periódicamente

---

### 🐛 Debug: Ver valor del secret (desarrollo)

```bash
# Ver si la variable está configurada (SIN mostrar valor completo por seguridad)
if [ -z "$JWT_SECRET" ]; then
  echo "JWT_SECRET NO configurado"
else
  echo "JWT_SECRET configurado (${#JWT_SECRET} caracteres)"
fi

# Ver primeros 10 caracteres (para debug)
echo ${JWT_SECRET:0:10}...
```

**⚠️ NUNCA** imprimir el secret completo en logs o consola en producción.

---

## Best Practices

### ✅ DO - Hacer

1. **Generar secrets aleatorios**
   ```bash
   openssl rand -base64 64
   ```

2. **Usar variables de entorno**
   ```bash
   export JWT_SECRET="..."
   ```

3. **Diferentes secrets por ambiente**
   - DEV: un secret
   - STAGING: otro secret diferente
   - PROD: otro secret diferente

4. **Rotar secrets periódicamente**
   - Cada 90 días (recomendado)
   - Al detectar compromiso (inmediatamente)

5. **Almacenar en gestores de secrets**
   - AWS Secrets Manager
   - Azure Key Vault
   - Google Secret Manager
   - HashiCorp Vault

6. **Documentar ubicación del secret**
   ```
   Producción: AWS Secrets Manager → task-management/jwt-secret
   Staging: Kubernetes Secret → jwt-secret (namespace: staging)
   Dev: Variable de entorno local (.env)
   ```

7. **Limitar acceso al secret**
   - Solo personas autorizadas
   - Principio de privilegio mínimo
   - Auditar accesos

---

### ❌ DON'T - No Hacer

1. **❌ Commitear secrets en git**
   ```bash
   # MAL
   git add .env
   git commit -m "add config"
   ```

2. **❌ Hardcodear en código**
   ```java
   // MAL
   private String jwtSecret = "mi-secret-123";
   ```

3. **❌ Compartir por email/chat**
   ```
   # MAL
   Slack: "El JWT secret es: abc123..."
   ```

4. **❌ Reutilizar secrets entre aplicaciones**
   ```
   # MAL
   APP1_JWT_SECRET=abc123
   APP2_JWT_SECRET=abc123  # ❌ Usar diferente
   ```

5. **❌ Usar secrets simples o predecibles**
   ```bash
   # MAL
   export JWT_SECRET="password123"
   export JWT_SECRET="mi-empresa-2024"
   ```

6. **❌ Loggear el secret**
   ```java
   // MAL
   log.info("JWT Secret: {}", jwtSecret);
   ```

7. **❌ Nunca rotar secrets**
   - Rotación recomendada: cada 90 días

---

### 🔐 Gestión de Secrets en Producción

#### Estrategia Recomendada

```
┌─────────────────────────────────────────────────────────┐
│                 GESTIÓN DE SECRETS                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  1. GENERACIÓN                                          │
│     ├─ Generar con openssl/script                      │
│     └─ Mínimo 64 caracteres                            │
│                                                         │
│  2. ALMACENAMIENTO                                      │
│     ├─ Usar gestor de secrets (AWS, Azure, GCP)        │
│     └─ Encriptado at-rest                              │
│                                                         │
│  3. DISTRIBUCIÓN                                        │
│     ├─ Inyectar como variables de entorno              │
│     └─ Nunca en archivos de configuración              │
│                                                         │
│  4. ROTACIÓN                                            │
│     ├─ Cada 90 días (programado)                       │
│     └─ Inmediatamente si hay compromiso                │
│                                                         │
│  5. AUDITORÍA                                           │
│     ├─ Loggear accesos al secret manager               │
│     └─ Alertar en accesos sospechosos                  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

#### Ejemplo de Rotación

```bash
#!/bin/bash
# rotate-jwt-secret.sh

# 1. Generar nuevo secret
NEW_SECRET=$(openssl rand -base64 64)

# 2. Actualizar en secret manager
aws secretsmanager update-secret \
  --secret-id task-management/jwt-secret \
  --secret-string "$NEW_SECRET"

# 3. Notificar al equipo
echo "JWT secret rotado. Reiniciar pods de Kubernetes."

# 4. Rolling restart de pods (Kubernetes)
kubectl rollout restart deployment/task-management-api

# 5. Auditar
echo "$(date): JWT secret rotado" >> /var/log/security-audit.log
```

---

## Referencias

### Documentación Oficial

- [RFC 7519 - JSON Web Token (JWT)](https://datatracker.ietf.org/doc/html/rfc7519)
- [OWASP - JWT Security Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
- [Spring Security - JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)

### Herramientas

- [jwt.io](https://jwt.io/) - JWT Debugger
- [OpenSSL](https://www.openssl.org/) - Generación de secrets
- [AWS Secrets Manager](https://aws.amazon.com/secrets-manager/)
- [Azure Key Vault](https://azure.microsoft.com/en-us/products/key-vault)
- [Google Secret Manager](https://cloud.google.com/secret-manager)

### Archivos del Proyecto

- `src/main/java/com/taskmanagement/api/config/JwtSecretValidator.java` - Validador
- `src/main/java/com/taskmanagement/api/service/JwtService.java` - Servicio JWT
- `src/main/resources/application.yml` - Configuración
- `scripts/generate-secrets.sh` - Script de generación
- `docs/JWT_SECURITY.md` - Esta documentación

---

## Contacto y Soporte

Para preguntas sobre seguridad JWT:

1. **Revisar logs** de la aplicación al iniciar
2. **Consultar esta documentación**
3. **Verificar configuración** de variables de entorno
4. **Crear issue** en GitHub con label `security`

---

**Última actualización**: 2025-11-15

**Versión**: 1.0.0
