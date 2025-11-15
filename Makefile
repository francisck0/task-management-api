# ============================================================================
# MAKEFILE PARA TASK MANAGEMENT API
# ============================================================================
#
# Este Makefile proporciona comandos convenientes para gestionar
# el entorno de desarrollo con Docker Compose.
#
# USO:
#   make <comando>
#
# EJEMPLOS:
#   make up        # Inicia todos los servicios
#   make logs      # Muestra logs en tiempo real
#   make stop      # Detiene todos los servicios
#
# ============================================================================

# ============================================================================
# VARIABLES
# ============================================================================

# Nombre del proyecto (se usa como prefijo en Docker)
PROJECT_NAME := task-project

# Nombre de la imagen de la aplicación
APP_IMAGE := task-management-api:latest

# Nombres de los contenedores
APP_CONTAINER := taskmanager-app
DB_CONTAINER := taskmanager-postgres
PGADMIN_CONTAINER := taskmanager-pgadmin

# Colores para output (hace el output más legible)
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m # No Color

# ============================================================================
# COMANDOS PRINCIPALES
# ============================================================================

.PHONY: help
help: ## Muestra esta ayuda
	@echo "$(GREEN)═══════════════════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  TASK MANAGEMENT API - Comandos Make Disponibles$(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════════════════$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)%-20s$(NC) %s\n", $$1, $$2}'
	@echo ""
	@echo "$(GREEN)═══════════════════════════════════════════════════════════════════$(NC)"

.PHONY: up
up: ## Inicia todos los servicios
	@echo "$(GREEN)🚀 Iniciando servicios...$(NC)"
	docker compose up -d
	@echo "$(GREEN)✅ Servicios iniciados$(NC)"
	@echo "$(YELLOW)📝 Aplicación disponible en: http://localhost:8080$(NC)"
	@echo "$(YELLOW)📝 Swagger UI en: http://localhost:8080/swagger-ui/index.html$(NC)"
	@echo "$(YELLOW)📝 pgAdmin en: http://localhost:5050$(NC)"

.PHONY: build
build: ## Construye las imágenes Docker
	@echo "$(GREEN)🔨 Construyendo imágenes...$(NC)"
	docker compose build --no-cache
	@echo "$(GREEN)✅ Imágenes construidas$(NC)"

.PHONY: rebuild
rebuild: ## Reconstruye y reinicia todos los servicios
	@echo "$(GREEN)🔨 Reconstruyendo servicios...$(NC)"
	docker compose up -d --build --force-recreate
	@echo "$(GREEN)✅ Servicios reconstruidos$(NC)"

.PHONY: down
down: ## Detiene y elimina contenedores (mantiene volúmenes)
	@echo "$(YELLOW)🛑 Deteniendo y eliminando contenedores...$(NC)"
	docker compose down
	@echo "$(GREEN)✅ Contenedores eliminados$(NC)"

.PHONY: stop
stop: ## Detiene todos los servicios (no elimina contenedores)
	@echo "$(YELLOW)⏸️  Deteniendo servicios...$(NC)"
	docker compose stop
	@echo "$(GREEN)✅ Servicios detenidos$(NC)"

.PHONY: start
start: ## Inicia servicios detenidos (sin rebuild)
	@echo "$(GREEN)▶️  Iniciando servicios...$(NC)"
	docker compose start
	@echo "$(GREEN)✅ Servicios iniciados$(NC)"

.PHONY: restart
restart: ## Reinicia todos los servicios
	@echo "$(YELLOW)🔄 Reiniciando servicios...$(NC)"
	docker compose restart
	@echo "$(GREEN)✅ Servicios reiniciados$(NC)"

.PHONY: destroy
destroy: ## Elimina TODO (contenedores, volúmenes, redes) - ¡CUIDADO!
	@echo "$(RED)⚠️  ADVERTENCIA: Esto eliminará TODOS los datos!$(NC)"
	@read -p "¿Estás seguro? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		echo "$(RED)🗑️  Eliminando todo...$(NC)"; \
		docker compose down -v; \
		echo "$(GREEN)✅ Todo eliminado$(NC)"; \
	else \
		echo "$(YELLOW)❌ Operación cancelada$(NC)"; \
	fi

# ============================================================================
# LOGS Y MONITOREO
# ============================================================================

.PHONY: logs
logs: ## Muestra logs en tiempo real de todos los servicios
	docker compose logs -f

.PHONY: logs-app
logs-app: ## Muestra logs de la aplicación
	docker compose logs -f app

.PHONY: logs-db
logs-db: ## Muestra logs de PostgreSQL
	docker compose logs -f postgres

.PHONY: logs-pgadmin
logs-pgadmin: ## Muestra logs de pgAdmin
	docker compose logs -f pgadmin

.PHONY: ps
ps: ## Muestra estado de los servicios
	@docker compose ps

.PHONY: stats
stats: ## Muestra uso de recursos de los contenedores
	docker stats

.PHONY: health
health: ## Verifica el health status de los servicios
	@echo "$(GREEN)🏥 Verificando salud de los servicios...$(NC)"
	@echo ""
	@echo "$(YELLOW)Aplicación:$(NC)"
	@docker inspect --format='{{.State.Health.Status}}' $(APP_CONTAINER) 2>/dev/null || echo "No disponible"
	@echo ""
	@echo "$(YELLOW)PostgreSQL:$(NC)"
	@docker inspect --format='{{.State.Health.Status}}' $(DB_CONTAINER) 2>/dev/null || echo "No disponible"
	@echo ""
	@echo "$(YELLOW)Docker Compose Services:$(NC)"
	@docker compose ps

# ============================================================================
# GESTIÓN DE SERVICIOS INDIVIDUALES
# ============================================================================

.PHONY: up-app
up-app: ## Inicia solo la aplicación
	@echo "$(GREEN)🚀 Iniciando aplicación...$(NC)"
	docker compose up -d app
	@echo "$(GREEN)✅ Aplicación iniciada$(NC)"

.PHONY: up-db
up-db: ## Inicia solo PostgreSQL
	@echo "$(GREEN)🚀 Iniciando PostgreSQL...$(NC)"
	docker compose up -d postgres
	@echo "$(GREEN)✅ PostgreSQL iniciado$(NC)"

.PHONY: restart-app
restart-app: ## Reinicia solo la aplicación
	@echo "$(YELLOW)🔄 Reiniciando aplicación...$(NC)"
	docker compose restart app
	@echo "$(GREEN)✅ Aplicación reiniciada$(NC)"

.PHONY: restart-db
restart-db: ## Reinicia solo PostgreSQL
	@echo "$(YELLOW)🔄 Reiniciando PostgreSQL...$(NC)"
	docker compose restart postgres
	@echo "$(GREEN)✅ PostgreSQL reiniciado$(NC)"

# ============================================================================
# SHELL Y COMANDOS EN CONTENEDORES
# ============================================================================

.PHONY: shell-app
shell-app: ## Abre shell en el contenedor de la aplicación
	docker compose exec app sh

.PHONY: shell-db
shell-db: ## Abre shell de PostgreSQL (psql)
	docker compose exec postgres psql -U postgres -d taskmanagement_db

.PHONY: exec-app
exec-app: ## Ejecuta comando en la aplicación (uso: make exec-app CMD="comando")
	docker compose exec app $(CMD)

# ============================================================================
# BASE DE DATOS
# ============================================================================

.PHONY: db-backup
db-backup: ## Crea backup de la base de datos
	@echo "$(GREEN)💾 Creando backup...$(NC)"
	@mkdir -p backups
	docker compose exec postgres pg_dump -U postgres taskmanagement_db > backups/backup-$$(date +%Y%m%d-%H%M%S).sql
	@echo "$(GREEN)✅ Backup creado en backups/$(NC)"

.PHONY: db-restore
db-restore: ## Restaura backup (uso: make db-restore FILE=backup.sql)
	@echo "$(YELLOW)📥 Restaurando backup...$(NC)"
	@if [ -z "$(FILE)" ]; then \
		echo "$(RED)❌ Error: Especifica el archivo con FILE=ruta/archivo.sql$(NC)"; \
		exit 1; \
	fi
	docker compose exec -T postgres psql -U postgres taskmanagement_db < $(FILE)
	@echo "$(GREEN)✅ Backup restaurado$(NC)"

.PHONY: db-reset
db-reset: ## Reinicia la base de datos (borra datos)
	@echo "$(RED)⚠️  ADVERTENCIA: Esto eliminará todos los datos de la BD!$(NC)"
	@read -p "¿Estás seguro? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		echo "$(YELLOW)🗑️  Eliminando volumen de PostgreSQL...$(NC)"; \
		docker compose down -v postgres; \
		docker compose up -d postgres; \
		echo "$(GREEN)✅ Base de datos reiniciada$(NC)"; \
	else \
		echo "$(YELLOW)❌ Operación cancelada$(NC)"; \
	fi

.PHONY: db-migrations
db-migrations: ## Ejecuta migraciones de Flyway/Liquibase (si están configuradas)
	@echo "$(GREEN)🔄 Ejecutando migraciones...$(NC)"
	docker compose exec app sh -c "java -jar app.jar --spring.liquibase.enabled=true"
	@echo "$(GREEN)✅ Migraciones completadas$(NC)"

# ============================================================================
# TESTING Y DESARROLLO
# ============================================================================

.PHONY: test
test: ## Ejecuta tests localmente (requiere Gradle instalado)
	@echo "$(GREEN)🧪 Ejecutando tests localmente...$(NC)"
	./gradlew test

.PHONY: test-docker
test-docker: ## Ejecuta tests dentro del contenedor
	@echo "$(GREEN)🧪 Ejecutando tests en Docker...$(NC)"
	docker compose run --rm app sh -c "gradle test"

.PHONY: clean
clean: ## Limpia archivos de build locales
	@echo "$(YELLOW)🧹 Limpiando archivos de build...$(NC)"
	./gradlew clean
	@echo "$(GREEN)✅ Limpieza completada$(NC)"

.PHONY: check-health
check-health: ## Verifica el endpoint de health de la aplicación
	@echo "$(GREEN)🏥 Verificando health endpoint...$(NC)"
	@curl -s http://localhost:8080/api/v1/actuator/health | jq . || echo "$(RED)❌ Aplicación no disponible$(NC)"

.PHONY: check-metrics
check-metrics: ## Muestra métricas de Actuator
	@echo "$(GREEN)📊 Métricas de la aplicación:$(NC)"
	@curl -s http://localhost:8080/api/v1/actuator/metrics | jq . || echo "$(RED)❌ Métricas no disponibles$(NC)"

.PHONY: login-test
login-test: ## Prueba login con usuario de prueba
	@echo "$(GREEN)🔑 Probando login...$(NC)"
	@curl -X POST http://localhost:8080/api/v1/auth/login \
		-H "Content-Type: application/json" \
		-d '{"username":"admin","password":"admin123"}' | jq . || echo "$(RED)❌ Login falló$(NC)"

# ============================================================================
# DOCKER CLEANUP
# ============================================================================

.PHONY: prune
prune: ## Limpia recursos Docker no usados (imágenes, contenedores, redes)
	@echo "$(YELLOW)🧹 Limpiando recursos Docker...$(NC)"
	docker system prune -f
	@echo "$(GREEN)✅ Limpieza completada$(NC)"

.PHONY: prune-all
prune-all: ## Limpia TODO en Docker incluyendo volúmenes - ¡CUIDADO!
	@echo "$(RED)⚠️  ADVERTENCIA: Esto eliminará TODOS los datos Docker!$(NC)"
	@read -p "¿Estás seguro? [y/N] " -n 1 -r; \
	echo; \
	if [[ $$REPLY =~ ^[Yy]$$ ]]; then \
		echo "$(RED)🗑️  Limpiando todo...$(NC)"; \
		docker system prune -a --volumes -f; \
		echo "$(GREEN)✅ Limpieza completa$(NC)"; \
	else \
		echo "$(YELLOW)❌ Operación cancelada$(NC)"; \
	fi

.PHONY: images
images: ## Lista imágenes Docker del proyecto
	@docker images | grep -E "(REPOSITORY|$(PROJECT_NAME)|task-management)"

# ============================================================================
# INFORMACIÓN Y DEBUGGING
# ============================================================================

.PHONY: info
info: ## Muestra información del entorno
	@echo "$(GREEN)═══════════════════════════════════════════════════════════════════$(NC)"
	@echo "$(GREEN)  TASK MANAGEMENT API - Información del Entorno$(NC)"
	@echo "$(GREEN)═══════════════════════════════════════════════════════════════════$(NC)"
	@echo ""
	@echo "$(YELLOW)📦 Proyecto:$(NC) $(PROJECT_NAME)"
	@echo "$(YELLOW)🐳 Imagen App:$(NC) $(APP_IMAGE)"
	@echo ""
	@echo "$(YELLOW)📝 URLs:$(NC)"
	@echo "  - Aplicación: http://localhost:8080"
	@echo "  - Swagger UI: http://localhost:8080/swagger-ui/index.html"
	@echo "  - OpenAPI Docs: http://localhost:8080/v3/api-docs"
	@echo "  - Health Check: http://localhost:8080/api/v1/actuator/health"
	@echo "  - Metrics: http://localhost:8080/api/v1/actuator/metrics"
	@echo "  - Prometheus: http://localhost:8080/api/v1/actuator/prometheus"
	@echo "  - pgAdmin: http://localhost:5050"
	@echo ""
	@echo "$(YELLOW)👤 Usuarios de prueba:$(NC)"
	@echo "  - Admin: admin / admin123"
	@echo "  - User: testuser / test123"
	@echo ""
	@echo "$(YELLOW)🗄️  Base de datos:$(NC)"
	@echo "  - Host: localhost:5432"
	@echo "  - Database: taskmanagement_db"
	@echo "  - User: postgres"
	@echo "  - Password: postgres"
	@echo ""
	@echo "$(GREEN)═══════════════════════════════════════════════════════════════════$(NC)"

.PHONY: config
config: ## Muestra configuración de docker-compose
	docker compose config

.PHONY: inspect-app
inspect-app: ## Inspecciona el contenedor de la aplicación
	docker inspect $(APP_CONTAINER)

.PHONY: inspect-db
inspect-db: ## Inspecciona el contenedor de PostgreSQL
	docker inspect $(DB_CONTAINER)

.PHONY: network
network: ## Muestra información de la red Docker
	docker network inspect $(PROJECT_NAME)_taskmanager-network || echo "$(RED)Red no existe$(NC)"

# ============================================================================
# ATAJOS Y ALIASES
# ============================================================================

.PHONY: dev
dev: up logs ## Inicia servicios y muestra logs (modo desarrollo)

.PHONY: quick-start
quick-start: build up info ## Build, up e info (inicio rápido)

.PHONY: full-restart
full-restart: down build up info ## Restart completo con rebuild

# ============================================================================
# PRODUCCIÓN
# ============================================================================

.PHONY: prod-build
prod-build: ## Build para producción
	@echo "$(GREEN)🔨 Construyendo para producción...$(NC)"
	SPRING_PROFILES_ACTIVE=prod docker compose build --no-cache
	@echo "$(GREEN)✅ Build de producción completado$(NC)"

.PHONY: prod-up
prod-up: ## Inicia servicios en modo producción
	@echo "$(GREEN)🚀 Iniciando en modo producción...$(NC)"
	SPRING_PROFILES_ACTIVE=prod docker compose up -d
	@echo "$(GREEN)✅ Servicios en producción iniciados$(NC)"

# ============================================================================
# DEFAULT TARGET
# ============================================================================

.DEFAULT_GOAL := help
