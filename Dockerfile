# ============================================================================
# MULTI-STAGE DOCKERFILE PARA TASK MANAGEMENT API
# ============================================================================
#
# ¿QUÉ ES MULTI-STAGE BUILD?
# - Permite usar múltiples imágenes base (FROM) en un solo Dockerfile
# - Cada stage puede copiar artefactos de stages anteriores
# - La imagen final solo contiene lo necesario para ejecutar (no herramientas de build)
#
# VENTAJAS:
# ✅ Imágenes más pequeñas (solo runtime, sin SDK ni dependencias de build)
# ✅ Más seguras (menos superficie de ataque)
# ✅ Builds más rápidos con caché de Docker
# ✅ Separación clara entre build y runtime
#
# ESTE DOCKERFILE TIENE 2 STAGES:
# 1. builder: Compila la aplicación con Gradle y JDK
# 2. runtime: Ejecuta la aplicación con JRE optimizado
#
# ============================================================================

# ============================================================================
# STAGE 1: BUILD
# ============================================================================
# Usa imagen con JDK 21 y Alpine Linux (muy ligera)
# Alpine es una distribución Linux minimalista (~5MB vs ~100MB de Ubuntu)
FROM gradle:8.5-jdk21-alpine AS builder

# Metadata de la imagen (buena práctica)
# Se puede consultar con: docker inspect <image>
LABEL maintainer="Task Management API Team"
LABEL description="Build stage for Task Management API with Spring Boot"
LABEL version="1.0"

# Establecer directorio de trabajo dentro del contenedor
WORKDIR /app

# ============================================================================
# OPTIMIZACIÓN: CACHEAR DEPENDENCIAS
# ============================================================================
# Copiar solo archivos de configuración de Gradle primero
# Docker cachea cada capa (COPY, RUN, etc) por separado
# Si estos archivos no cambian, Docker usa caché y no descarga dependencias de nuevo
# Esto hace builds subsecuentes MUCHO más rápidos

# Copiar archivos de configuración de Gradle
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Descargar dependencias (se cachea si build.gradle no cambia)
# --no-daemon: No usar Gradle daemon (no necesario en contenedor)
# --no-watch-fs: No monitorear sistema de archivos (no necesario en build)
RUN gradle dependencies --no-daemon --no-watch-fs || true

# ============================================================================
# COPIAR CÓDIGO FUENTE Y COMPILAR
# ============================================================================
# Ahora sí, copiar el código fuente
# Esta capa se invalida cada vez que el código cambia
# Pero la capa de dependencias sigue cacheada
COPY src ./src

# Compilar la aplicación
# clean: Limpia builds anteriores
# bootJar: Crea un JAR ejecutable con todas las dependencias (fat JAR)
# -x test: Omite tests (ya se ejecutaron en CI/CD)
# --no-daemon: No usar daemon de Gradle
# --no-watch-fs: No monitorear filesystem
# -Pprofile=prod: Activa perfil de producción
RUN gradle clean bootJar -x test --no-daemon --no-watch-fs

# El JAR se genera en: build/libs/api-0.0.1-SNAPSHOT.jar
# Podemos verificar su ubicación con: ls -la build/libs/

# ============================================================================
# STAGE 2: RUNTIME
# ============================================================================
# Usa imagen con solo JRE (Java Runtime Environment)
# No incluye compilador ni herramientas de desarrollo
# JRE es ~50% más pequeño que JDK

# Eclipse Temurin es la distribución OpenJDK recomendada (anteriormente AdoptOpenJDK)
# Alpine hace la imagen más ligera (~150MB vs ~350MB con base Ubuntu)
FROM eclipse-temurin:21-jre-alpine AS runtime

# Metadata
LABEL maintainer="Task Management API Team"
LABEL description="Runtime stage for Task Management API with Spring Boot"
LABEL version="1.0"

# ============================================================================
# BUENAS PRÁCTICAS DE SEGURIDAD
# ============================================================================

# 1. NO EJECUTAR COMO ROOT
# Por defecto, los contenedores ejecutan procesos como root (UID 0)
# Si el contenedor es comprometido, el atacante tiene acceso root
# SOLUCIÓN: Crear usuario no privilegiado

# Crear grupo y usuario 'springboot' sin privilegios
# -S: Usuario de sistema (sin shell login)
# -G: Grupo primario
# -h: Home directory
RUN addgroup -S springboot && adduser -S springboot -G springboot

# 2. INSTALAR HERRAMIENTAS DE MONITOREO (opcional pero recomendado)
# curl: Para health checks desde fuera del contenedor
# tzdata: Para manejar zonas horarias correctamente
RUN apk add --no-cache curl tzdata

# 3. CONFIGURAR ZONA HORARIA
# Establecer zona horaria del contenedor
ENV TZ=America/Mexico_City
RUN cp /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# ============================================================================
# CONFIGURACIÓN DE LA APLICACIÓN
# ============================================================================

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR desde el stage de build
# --from=builder: Copia desde el stage llamado 'builder'
# --chown: Establece el propietario del archivo
COPY --from=builder --chown=springboot:springboot /app/build/libs/*.jar app.jar

# Cambiar al usuario no privilegiado
# Todos los comandos siguientes se ejecutan como 'springboot'
USER springboot

# ============================================================================
# VARIABLES DE ENTORNO
# ============================================================================
# Estas variables se pueden sobrescribir en docker-compose.yml o al ejecutar
# docker run -e VARIABLE=valor

# Perfil de Spring Boot (dev, test, prod)
ENV SPRING_PROFILES_ACTIVE=prod

# Configuración de JVM
# JAVA_OPTS permite pasar opciones personalizadas a la JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom"

# Explicación de JAVA_OPTS:
# -XX:+UseContainerSupport: JVM detecta límites de memoria del contenedor
# -XX:MaxRAMPercentage=75.0: Usa máximo 75% de RAM del contenedor
# -XX:InitialRAMPercentage=50.0: Inicia con 50% de RAM del contenedor
# -XX:+UseG1GC: Usa Garbage Collector G1 (recomendado para apps grandes)
# -XX:+UseStringDeduplication: Reduce uso de memoria eliminando Strings duplicados
# -Djava.security.egd=file:/dev/./urandom: Mejora rendimiento de generación de random

# Puerto en el que la aplicación escucha
# Spring Boot usa 8080 por defecto
ENV SERVER_PORT=8080

# ============================================================================
# HEALTH CHECK
# ============================================================================
# Docker puede verificar automáticamente si la aplicación está saludable
# Si falla, puede reiniciar el contenedor automáticamente

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/api/v1/actuator/health || exit 1

# Explicación:
# --interval=30s: Verifica cada 30 segundos
# --timeout=3s: Timeout de 3 segundos para cada check
# --start-period=60s: Espera 60s antes de empezar (para que la app inicie)
# --retries=3: 3 fallos consecutivos = unhealthy
# CMD: Comando que ejecuta (exit 0 = healthy, exit 1 = unhealthy)

# ============================================================================
# EXPONER PUERTO
# ============================================================================
# Documenta qué puerto usa la aplicación
# NOTA: Esto NO publica el puerto, solo es documentación
# Para publicar: docker run -p 8080:8080 o en docker-compose.yml

EXPOSE ${SERVER_PORT}

# ============================================================================
# COMANDO DE INICIO
# ============================================================================
# ENTRYPOINT vs CMD:
# - ENTRYPOINT: Define el ejecutable principal (no se puede sobrescribir fácilmente)
# - CMD: Argumentos por defecto (se pueden sobrescribir al ejecutar)
# - Juntos: ENTRYPOINT ejecuta, CMD proporciona args por defecto

# Usar exec form (array JSON) en lugar de shell form
# Ventajas:
# - Señales (SIGTERM, SIGINT) se pasan correctamente a la aplicación
# - No crea proceso shell innecesario
# - Mejor para graceful shutdown

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ============================================================================
# CÓMO CONSTRUIR Y EJECUTAR ESTA IMAGEN
# ============================================================================
#
# Construir imagen:
#   docker build -t task-management-api:latest .
#
# Construir sin caché (forzar rebuild completo):
#   docker build --no-cache -t task-management-api:latest .
#
# Ejecutar contenedor (standalone):
#   docker run -d \
#     --name task-api \
#     -p 8080:8080 \
#     -e SPRING_PROFILES_ACTIVE=prod \
#     -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/taskmanagement_db \
#     -e SPRING_DATASOURCE_USERNAME=postgres \
#     -e SPRING_DATASOURCE_PASSWORD=postgres \
#     task-management-api:latest
#
# Ver logs:
#   docker logs -f task-api
#
# Ejecutar comando dentro del contenedor:
#   docker exec -it task-api sh
#
# Detener contenedor:
#   docker stop task-api
#
# Eliminar contenedor:
#   docker rm task-api
#
# ============================================================================
# OPTIMIZACIONES ADICIONALES (AVANZADO)
# ============================================================================
#
# 1. USAR SPRING BOOT LAYERED JARS (para mejor caché de Docker):
#    - Spring Boot puede separar el JAR en capas (dependencias, clases, recursos)
#    - Cambios en código no invalidan caché de dependencias
#    - Ver: https://docs.spring.io/spring-boot/docs/current/reference/html/container-images.html#container-images.dockerfiles
#
# 2. USAR CLOUD NATIVE BUILDPACKS:
#    - ./gradlew bootBuildImage
#    - Crea imágenes optimizadas sin escribir Dockerfile
#
# 3. USAR JLINK PARA CREAR JRE PERSONALIZADO:
#    - Incluye solo módulos de Java necesarios
#    - Reduce imagen a ~50-100MB
#
# 4. COMPILACIÓN NATIVA CON GRAALVM:
#    - Compila a binario nativo (no necesita JVM)
#    - Startup instantáneo (<100ms)
#    - Memoria muy reducida (~30MB)
#    - Requiere configuración adicional
#
# ============================================================================
