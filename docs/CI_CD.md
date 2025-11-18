# CI/CD Pipeline - Task Management API

## 📋 Tabla de Contenidos

- [Introducción](#introducción)
- [Arquitectura del Pipeline](#arquitectura-del-pipeline)
- [Workflows de GitHub Actions](#workflows-de-github-actions)
  - [CI - Build and Test](#ci---build-and-test)
  - [Docker Build and Push](#docker-build-and-push)
  - [Deploy](#deploy)
- [Dependabot](#dependabot)
- [Configuración Inicial](#configuración-inicial)
- [Secrets y Variables](#secrets-y-variables)
- [Ambientes](#ambientes)
- [Guía de Uso](#guía-de-uso)
- [Troubleshooting](#troubleshooting)
- [Best Practices](#best-practices)

---

## Introducción

Este proyecto implementa un pipeline de CI/CD completo utilizando **GitHub Actions** para automatizar:

- ✅ **Continuous Integration (CI)**: Build, tests, quality checks, security scanning
- 🐳 **Container Building**: Construcción y publicación de imágenes Docker
- 🚀 **Continuous Deployment (CD)**: Deployment automático a staging y production
- 🔄 **Dependency Management**: Actualizaciones automáticas con Dependabot

---

## Arquitectura del Pipeline

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CÓDIGO FUENTE (GitHub)                      │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│  WORKFLOW 1: CI - Build and Test                                    │
│  ├─ Build con Gradle                                                │
│  ├─ Tests (Unitarios + Integración)                                 │
│  ├─ Coverage (JaCoCo)                                               │
│  ├─ Code Quality (Checkstyle, SpotBugs, PMD)                        │
│  └─ Security (OWASP Dependency Check)                               │
└────────────────────────────┬────────────────────────────────────────┘
                             │ (si pasa)
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│  WORKFLOW 2: Docker Build and Push                                  │
│  ├─ Build imagen Docker                                             │
│  ├─ Push a GitHub Container Registry (ghcr.io)                      │
│  ├─ Tagging automático                                              │
│  └─ Security scan con Trivy                                         │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                   ┌─────────┴─────────┐
                   ▼                   ▼
        ┌─────────────────┐ ┌─────────────────────┐
        │   STAGING       │ │    PRODUCTION       │
        │  (auto-deploy) │ │ (manual approval)  │
        └─────────────────┘ └─────────────────────┘
```

---

## Workflows de GitHub Actions

### CI - Build and Test

**Archivo**: `.github/workflows/ci.yml`

**Triggers**:
- Push a `main`, `develop`, `feature/**`, `hotfix/**`
- Pull requests a `main` o `develop`
- Manual (workflow_dispatch)

**Jobs**:

#### 1. Build and Test
- ✅ Compilación con Gradle
- ✅ Ejecución de tests con PostgreSQL 18
- ✅ Generación de coverage con JaCoCo
- ✅ Publicación de resultados de tests
- ✅ Archivado de reportes y artifacts

#### 2. Code Quality
- ✅ Checkstyle (estilo de código)
- ✅ SpotBugs (detección de bugs)
- ✅ PMD (análisis estático)

#### 3. Dependency Check
- ✅ OWASP Dependency Check (vulnerabilidades)

#### 4. Build Status
- ✅ Consolidación de resultados
- ✅ Notificación de estado final

**Duración aproximada**: 5-10 minutos

---

### Docker Build and Push

**Archivo**: `.github/workflows/docker-build.yml`

**Triggers**:
- Cuando CI pasa exitosamente en `main` o `develop`
- Creación de releases
- Manual (workflow_dispatch)

**Proceso**:

1. **Build multi-platform** (AMD64, ARM64 opcional)
2. **Tagging automático**:
   - `main-abc1234` (commit SHA)
   - `develop` (nombre de rama)
   - `v1.2.3`, `v1.2`, `v1` (releases)
   - `latest` (rama main)

3. **Push a Registry**:
   - GitHub Container Registry: `ghcr.io/usuario/task-management-api`

4. **Security Scan**:
   - Trivy vulnerability scanner
   - Resultados en GitHub Security

**Duración aproximada**: 3-5 minutos

---

### Deploy

**Archivo**: `.github/workflows/deploy.yml`

**Triggers**:
- Auto-deploy a **staging** cuando se hace push a `develop`
- Auto-deploy a **production** cuando se crea un release
- Manual con selección de ambiente y versión

**Ambientes**:

#### Staging
- **URL**: `https://staging.taskmanagement.example.com`
- **Auto-deploy**: Sí (desde `develop`)
- **Approvals**: No requeridos
- **Purpose**: Testing, QA, demos

#### Production
- **URL**: `https://taskmanagement.example.com`
- **Auto-deploy**: Solo desde releases
- **Approvals**: **Requeridos** (1-2 reviewers)
- **Purpose**: Producción

**Estrategias de Deploy**:

El workflow incluye ejemplos para:
- **Docker Compose** (VPS/servidor propio)
- **Kubernetes** (clusters K8s)
- **Cloud Platforms** (AWS ECS, Azure, GCP)

**Proceso de Deploy**:

1. Determinar ambiente y versión
2. Pull de imagen Docker desde registry
3. Deploy según estrategia configurada
4. Health checks y smoke tests
5. Notificaciones al equipo

**Duración aproximada**: 2-5 minutos

---

## Dependabot

**Archivo**: `.github/dependabot.yml`

Dependabot mantiene automáticamente las dependencias actualizadas.

**Configuración**:

### Gradle (Java/Spring Boot)
- **Frecuencia**: Semanal (lunes 9:00 AM)
- **Max PRs**: 10 simultáneos
- **Agrupación**: Spring, Testing, Development

### Docker
- **Frecuencia**: Semanal
- **Max PRs**: 5 simultáneos
- **Scope**: Imágenes base en Dockerfile

### GitHub Actions
- **Frecuencia**: Semanal
- **Max PRs**: 5 simultáneos
- **Scope**: Actions y workflows

**Beneficios**:
- ✅ Actualizaciones automáticas de seguridad
- ✅ PRs con changelogs detallados
- ✅ Detección de vulnerabilidades
- ✅ Reducción de deuda técnica

---

## Configuración Inicial

### 1. Habilitar GitHub Actions

```bash
# En tu repositorio de GitHub:
Settings > Actions > General > Allow all actions and reusable workflows
```

### 2. Configurar Secrets

```bash
# Settings > Secrets and variables > Actions > New repository secret
```

**Secrets requeridos**:

#### Para Docker Registry (GitHub Container Registry)
- `GITHUB_TOKEN` (automático, no requiere configuración)

#### Para Deploy SSH/Docker Compose (opcional)
- `SSH_PRIVATE_KEY`: Clave SSH para acceder al servidor
- `STAGING_HOST`: Host del servidor de staging
- `PRODUCTION_HOST`: Host del servidor de producción

#### Para Deploy Kubernetes (opcional)
- `K8S_STAGING_SERVER`: URL del cluster de staging
- `K8S_STAGING_TOKEN`: Token de autenticación
- `K8S_PRODUCTION_SERVER`: URL del cluster de producción
- `K8S_PRODUCTION_TOKEN`: Token de autenticación

#### Para Deploy Cloud (opcional)
- **AWS**:
  - `AWS_ACCESS_KEY_ID`
  - `AWS_SECRET_ACCESS_KEY`
  - `AWS_REGION`
- **Azure**:
  - `AZURE_CREDENTIALS`
- **GCP**:
  - `GCP_PROJECT_ID`
  - `GCP_SA_KEY`

#### Para Notificaciones (opcional)
- `SLACK_WEBHOOK_URL`: Webhook de Slack
- `DISCORD_WEBHOOK_URL`: Webhook de Discord

### 3. Configurar Ambientes

```bash
# Settings > Environments > New environment
```

**Staging**:
- Name: `staging`
- URL: `https://staging.taskmanagement.example.com`
- Protection rules: Ninguna (auto-deploy)

**Production**:
- Name: `production`
- URL: `https://taskmanagement.example.com`
- Protection rules:
  - ✅ Required reviewers: 1-2 personas
  - ✅ Wait timer: 5 minutos (opcional)
  - ✅ Deployment branches: `main` only

### 4. Habilitar Dependabot

```bash
# Settings > Code security and analysis
✓ Dependabot alerts: Enabled
✓ Dependabot security updates: Enabled
✓ Dependabot version updates: Enabled
```

### 5. Configurar Branch Protection

```bash
# Settings > Branches > Add rule
```

**Para `main`**:
- ✅ Require pull request reviews (1 approval)
- ✅ Require status checks to pass (CI workflow)
- ✅ Require conversation resolution
- ✅ Include administrators

**Para `develop`**:
- ✅ Require status checks to pass (CI workflow)
- ✅ Require conversation resolution

---

## Secrets y Variables

### Environment Variables (públicas)

```yaml
# Configurar en Settings > Secrets and variables > Actions > Variables

REGISTRY: ghcr.io
JAVA_VERSION: 21
POSTGRES_VERSION: 18
```

### Secrets (privados)

Ver sección [Configuración Inicial > Configurar Secrets](#2-configurar-secrets)

---

## Ambientes

### Staging

**Propósito**: Testing, QA, demos, desarrollo

**Características**:
- Auto-deploy desde `develop`
- Sin approvals requeridos
- Datos de prueba
- Logs detallados

**URL**: `https://staging.taskmanagement.example.com`

### Production

**Propósito**: Ambiente productivo

**Características**:
- Deploy manual o desde releases
- Approvals requeridos (1-2 reviewers)
- Datos reales
- Monitoreo y alertas

**URL**: `https://taskmanagement.example.com`

---

## Guía de Uso

### Flujo de Desarrollo Normal

```bash
# 1. Crear feature branch
git checkout -b feature/nueva-funcionalidad

# 2. Hacer cambios y commits
git add .
git commit -m "feat: implementar nueva funcionalidad"

# 3. Push a GitHub
git push origin feature/nueva-funcionalidad
```

**Resultado**:
- ✅ CI workflow se ejecuta automáticamente
- ✅ Build, tests, quality checks
- ✅ Feedback en PR

```bash
# 4. Merge a develop (después de review)
# → Auto-deploy a staging
```

**Resultado**:
- ✅ CI ejecuta
- ✅ Docker build y push
- ✅ Deploy automático a staging

```bash
# 5. Merge a main (después de testing en staging)
# → Preparar para production
```

**Resultado**:
- ✅ CI ejecuta
- ✅ Docker build con tag `latest`
- ✅ NO auto-deploy a production

```bash
# 6. Crear release para deploy a production
# GitHub > Releases > Create release
# Tag: v1.0.0
```

**Resultado**:
- ✅ Docker build con tags: `v1.0.0`, `v1.0`, `v1`, `latest`
- ✅ Deploy automático a production (con approval)

---

### Deploy Manual

#### Deploy a Staging (versión específica)

```bash
# GitHub UI:
Actions > Deploy to Environment > Run workflow
- Environment: staging
- Version: develop-abc1234
```

#### Deploy a Production (versión específica)

```bash
# GitHub UI:
Actions > Deploy to Environment > Run workflow
- Environment: production
- Version: v1.0.0
```

**Requiere**:
- ✅ Approval de 1-2 reviewers configurados
- ✅ Esperar 5 minutos (wait timer)

---

### Verificar Estado del Pipeline

#### Ver Workflows Activos

```bash
# GitHub UI:
Actions > All workflows

# CLI:
gh workflow list
```

#### Ver Runs de un Workflow

```bash
# CLI:
gh run list --workflow=ci.yml
gh run list --workflow=docker-build.yml
gh run list --workflow=deploy.yml
```

#### Ver Logs de un Run

```bash
# CLI:
gh run view <run-id> --log
```

---

### Pull de Imagen Docker

```bash
# Login a GitHub Container Registry
echo $GITHUB_TOKEN | docker login ghcr.io -u USERNAME --password-stdin

# Pull imagen
docker pull ghcr.io/usuario/task-management-api:latest
docker pull ghcr.io/usuario/task-management-api:v1.0.0
docker pull ghcr.io/usuario/task-management-api:develop-abc1234

# Run localmente
docker run -p 8080:8080 ghcr.io/usuario/task-management-api:latest
```

---

## Troubleshooting

### CI Falla en Tests

**Síntoma**: Tests fallan en CI pero pasan localmente

**Solución**:
```bash
# Verificar que PostgreSQL esté corriendo en CI
# Ver logs del job "Run tests" en GitHub Actions

# Ejecutar tests localmente con perfil test
./gradlew test -Dspring.profiles.active=test

# Verificar configuración de BD en application-test.yml
```

### Docker Build Falla

**Síntoma**: Docker build falla con error de permisos

**Solución**:
```bash
# Verificar que GITHUB_TOKEN tenga permisos de packages:write
# Settings > Actions > General > Workflow permissions > Read and write

# Verificar Dockerfile
docker build -t test .
```

### Deploy Falla

**Síntoma**: Deploy falla al conectar al servidor

**Solución**:
```bash
# Verificar secrets configurados correctamente
# Settings > Secrets and variables > Actions

# Probar conexión SSH manualmente
ssh -i $SSH_PRIVATE_KEY user@$STAGING_HOST

# Verificar health del servidor
curl https://staging.taskmanagement.example.com/actuator/health
```

### Dependabot PRs No Se Crean

**Síntoma**: Dependabot no crea PRs de actualización

**Solución**:
```bash
# Verificar configuración de Dependabot
# Settings > Code security and analysis > Dependabot

# Verificar .github/dependabot.yml syntax
# GitHub UI: .github/dependabot.yml > Insights > Dependabot

# Forzar check manual
# Settings > Code security > Dependabot > Check for updates
```

### Approval Bloqueado en Production

**Síntoma**: No puedo aprobar deploy a production

**Solución**:
```bash
# Verificar que estés configurado como reviewer
# Settings > Environments > production > Reviewers

# Aprobar deploy
# Actions > Deploy run > Review deployments > Approve and deploy
```

---

## Best Practices

### 1. Estrategia de Branching

```
main          ─────●─────●─────●─────●───── (production)
              ╱     ╱     ╱     ╱
develop  ─────●─────●─────●─────●────────── (staging)
         ╱    ╱    ╱    ╱    ╱
feature ●────●    │    │    │
hotfix       │    ●────●    │
release      │              ●────────────── (pre-production testing)
```

- **main**: Solo código estable y testeado
- **develop**: Integración continua
- **feature/**: Nuevas funcionalidades
- **hotfix/**: Fixes urgentes a production
- **release/**: Preparación de releases

### 2. Versionado Semántico

```
v1.2.3
│ │ └─ PATCH: Bug fixes
│ └─── MINOR: New features (backwards compatible)
└───── MAJOR: Breaking changes
```

### 3. Commit Messages

```bash
# Formato: <type>(<scope>): <subject>

feat(auth): add JWT token refresh
fix(tasks): resolve soft delete bug
docs(ci): update deployment guide
chore(deps): update Spring Boot to 3.5.8
```

### 4. Pull Request Workflow

1. ✅ Crear PR con descripción clara
2. ✅ Esperar que CI pase (automático)
3. ✅ Request review de al menos 1 persona
4. ✅ Resolver conversaciones
5. ✅ Merge usando **Squash and merge** (recomendado)

### 5. Testing

```bash
# Ejecutar tests antes de push
./gradlew test

# Verificar coverage
./gradlew jacocoTestReport
# Ver: build/reports/jacoco/test/html/index.html

# Ejecutar quality checks
./gradlew checkstyleMain spotbugsMain pmdMain
```

### 6. Security

- ✅ Nunca commitear secrets en código
- ✅ Usar GitHub Secrets para credenciales
- ✅ Habilitar Dependabot security updates
- ✅ Revisar security alerts regularmente
- ✅ Ejecutar OWASP dependency check
- ✅ Scan de imágenes Docker con Trivy

### 7. Monitoring Post-Deployment

```bash
# Health check
curl https://production.example.com/actuator/health

# Metrics
curl https://production.example.com/actuator/metrics

# Verificar logs
kubectl logs deployment/task-management-api -n production --tail=100
```

---

## Recursos Adicionales

### Documentación Oficial

- [GitHub Actions](https://docs.github.com/en/actions)
- [Dependabot](https://docs.github.com/en/code-security/dependabot)
- [Docker](https://docs.docker.com/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/index.html)

### Herramientas

- [act](https://github.com/nektos/act): Run GitHub Actions locally
- [gh CLI](https://cli.github.com/): GitHub command-line tool
- [Trivy](https://github.com/aquasecurity/trivy): Security scanner

### Monitoreo y Observabilidad

- [Prometheus](https://prometheus.io/): Metrics
- [Grafana](https://grafana.com/): Dashboards
- [ELK Stack](https://www.elastic.co/elastic-stack): Logs
- [Sentry](https://sentry.io/): Error tracking

---

## Contacto y Soporte

Para preguntas o problemas con CI/CD:

1. **Revisar logs** en GitHub Actions
2. **Consultar esta documentación**
3. **Crear issue** en GitHub con label `ci-cd`
4. **Contactar al equipo DevOps**

---

**Última actualización**: 2025-11-15

**Versión**: 1.0.0
