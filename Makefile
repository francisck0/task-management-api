# Makefile para Task Management API
# Comandos útiles para el proyecto

.PHONY: help build run test clean docker-up docker-down docker-logs

# Comando por defecto: mostrar ayuda
help:
	@echo "Task Management API - Comandos disponibles:"
	@echo ""
	@echo "  make build         - Compilar el proyecto"
	@echo "  make run          - Ejecutar la aplicación"
	@echo "  make test         - Ejecutar pruebas"
	@echo "  make clean        - Limpiar archivos compilados"
	@echo ""
	@echo "  make docker-up    - Iniciar PostgreSQL con Docker"
	@echo "  make docker-down  - Detener PostgreSQL"
	@echo "  make docker-logs  - Ver logs de PostgreSQL"
	@echo ""
	@echo "  make all          - Build + Docker + Run"
	@echo ""

# Compilar el proyecto
build:
	@echo "Compilando el proyecto..."
	./gradlew build

# Ejecutar la aplicación
run:
	@echo "Ejecutando la aplicación..."
	./gradlew bootRun

# Ejecutar pruebas
test:
	@echo "Ejecutando pruebas..."
	./gradlew test

# Limpiar archivos compilados
clean:
	@echo "Limpiando archivos compilados..."
	./gradlew clean

# Iniciar PostgreSQL con Docker Compose
docker-up:
	@echo "Iniciando PostgreSQL..."
	docker compose up -d
	@echo "PostgreSQL iniciado en puerto 5432"
	@echo "pgAdmin disponible en http://localhost:5050"

# Detener PostgreSQL
docker-down:
	@echo "Deteniendo PostgreSQL..."
	docker compose down

# Ver logs de PostgreSQL
docker-logs:
	docker compose logs -f postgres

# Reiniciar PostgreSQL
docker-restart: docker-down docker-up

# Flujo completo: build, docker, run
all: docker-up build
	@echo "Todo listo! Ahora ejecuta: make run"

# Verificar estado de PostgreSQL
docker-status:
	docker compose ps
