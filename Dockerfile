# ============================================================================
# MULTI-STAGE DOCKERFILE OPTIMIZADO CON SPRING BOOT LAYERED JARS
# ============================================================================
#
# ¿QUÉ ES MULTI-STAGE BUILD CON LAYERS?
# - Usa múltiples stages (FROM) en un solo Dockerfile
# - Separa el JAR en capas lógicas (dependencies, loader, application)
# - Docker cachea cada capa por separado
# - Solo reconstruye las capas que cambiaron
#
# VENTAJAS PRINCIPALES:
# ✅ Imágenes más pequeñas (solo runtime, sin SDK)
# ✅ Builds 10-100x más rápidos en cambios incrementales
# ✅ Mejor caché de Docker (dependencias separadas del código)
# ✅ Menor transferencia de datos a registry
# ✅ Más seguras (menos superficie de ataque)
#
# CAPAS DE SPRING BOOT:
# 1. dependencies: Dependencias externas (~145MB, raramente cambia)
# 2. spring-boot-loader: Spring Boot loader (~1MB, casi nunca cambia)
# 3. snapshot-dependencies: Dependencias SNAPSHOT (~variable)
# 4. application: Tu código fuente (~5MB, cambia frecuentemente)
#
# MEJORA DE PERFORMANCE:
# Antes (sin layers):
#   - Cambio en código → Rebuild completo de JAR (150MB)
#   - Docker push/pull: 150MB cada vez
#
# Ahora (con layers):
#   - Cambio en código → Solo rebuild capa 'application' (5MB)
#   - Docker push/pull: Solo 5MB
#   - Resto de capas (145MB) permanecen cacheadas
#
# ============================================================================

# ============================================================================
# STAGE 1: BUILD
# ============================================================================
# Compila la aplicación y extrae las capas del JAR
FROM gradle:8.5-jdk21-alpine AS builder

# Metadata
LABEL stage=builder
LABEL maintainer="Task Management API Team"
LABEL description="Build stage with Gradle and layered JAR extraction"

WORKDIR /app

# ============================================================================
# OPTIMIZACIÓN: CACHEAR DEPENDENCIAS DE GRADLE
# ============================================================================
# Copiar solo archivos de configuración primero
# Si estos archivos no cambian, Gradle usa su caché
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# Descargar dependencias (se cachea si build.gradle no cambia)
RUN gradle dependencies --no-daemon --no-watch-fs || true

# ============================================================================
# COMPILAR APLICACIÓN
# ============================================================================
# Copiar código fuente
COPY src ./src

# Compilar y generar JAR con capas habilitadas
# La configuración de layered JAR está en build.gradle
RUN gradle clean bootJar -x test --no-daemon --no-watch-fs

# ============================================================================
# EXTRAER CAPAS DEL JAR
# ============================================================================
# Spring Boot incluye una herramienta (jarmode=layertools) para extraer capas
# Esto separa el JAR en directorios: dependencies/, spring-boot-loader/,
# snapshot-dependencies/, application/
RUN java -Djarmode=layertools -jar build/libs/*.jar extract --destination extracted

# Verificar capas extraídas (útil para debugging)
RUN ls -la extracted/

# ============================================================================
# STAGE 2: RUNTIME
# ============================================================================
# Imagen mínima con solo JRE para ejecutar la aplicación
FROM eclipse-temurin:21-jre-alpine AS runtime

# Metadata
LABEL maintainer="Task Management API Team"
LABEL description="Optimized runtime with Spring Boot layered JARs"
LABEL version="2.0"

# ============================================================================
# SEGURIDAD: USUARIO NO PRIVILEGIADO
# ============================================================================
# Crear usuario sin privilegios para ejecutar la aplicación
RUN addgroup -S springboot && adduser -S springboot -G springboot

# Instalar herramientas esenciales
# curl: Health checks
# tzdata: Zonas horarias
RUN apk add --no-cache curl tzdata

# Configurar zona horaria
ENV TZ=America/Mexico_City
RUN cp /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# ============================================================================
# COPIAR CAPAS EN ORDEN DE ESTABILIDAD
# ============================================================================
# IMPORTANTE: Las capas más estables van primero
# Docker cachea cada instrucción COPY como una capa separada
# Si una capa no cambia, Docker la reutiliza del caché
#
# ORDEN ESTRATÉGICO:
# 1. dependencies: Casi nunca cambia → Mejor caché
# 2. spring-boot-loader: Casi nunca cambia
# 3. snapshot-dependencies: Puede cambiar (dependencias en desarrollo)
# 4. application: Cambia con cada commit → Peor caché
#
# Este orden maximiza la reutilización del caché de Docker

WORKDIR /app

# Capa 1: Dependencias externas (más estable)
# Contiene: JAR files de Maven/Gradle dependencies
# Tamaño típico: ~145MB
# Frecuencia de cambio: Baja (solo cuando actualizas dependencias en build.gradle)
COPY --from=builder --chown=springboot:springboot /app/extracted/dependencies/ ./

# Capa 2: Spring Boot Loader (muy estable)
# Contiene: Clases del loader de Spring Boot
# Tamaño típico: ~1MB
# Frecuencia de cambio: Muy baja (solo cuando actualizas Spring Boot version)
COPY --from=builder --chown=springboot:springboot /app/extracted/spring-boot-loader/ ./

# Capa 3: Dependencias SNAPSHOT (opcional, menos estable que dependencies)
# Contiene: Dependencias con versión SNAPSHOT (en desarrollo)
# Tamaño típico: Variable
# Frecuencia de cambio: Media
COPY --from=builder --chown=springboot:springboot /app/extracted/snapshot-dependencies/ ./

# Capa 4: Código de la aplicación (menos estable)
# Contiene: Tus clases compiladas (.class files)
# Tamaño típico: ~5MB
# Frecuencia de cambio: Alta (cada commit con cambios de código)
COPY --from=builder --chown=springboot:springboot /app/extracted/application/ ./

# Cambiar a usuario no privilegiado
USER springboot

# ============================================================================
# VARIABLES DE ENTORNO
# ============================================================================
ENV SPRING_PROFILES_ACTIVE=prod

# Configuración optimizada de JVM para contenedores
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom"

ENV SERVER_PORT=8080

# ============================================================================
# HEALTH CHECK
# ============================================================================
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:${SERVER_PORT}/api/v1/actuator/health || exit 1

# ============================================================================
# EXPONER PUERTO
# ============================================================================
EXPOSE ${SERVER_PORT}

# ============================================================================
# COMANDO DE INICIO
# ============================================================================
# Ejecutar usando el Spring Boot Launcher (JarLauncher)
# Este launcher es parte de spring-boot-loader que copiamos antes
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]

# ============================================================================
# CÓMO USAR ESTE DOCKERFILE
# ============================================================================
#
# 1. Construir imagen:
#    docker build -t task-management-api:latest .
#
# 2. Primera construcción:
#    - Descarga todas las dependencias
#    - Compila la aplicación
#    - Extrae las capas
#    - Tiempo: ~2-5 minutos
#
# 3. Reconstrucción después de cambiar CÓDIGO:
#    - Reutiliza capas: dependencies, spring-boot-loader, snapshot-dependencies
#    - Solo reconstruye capa: application
#    - Tiempo: ~30 segundos (10x más rápido)
#
# 4. Reconstrucción después de cambiar DEPENDENCIAS (build.gradle):
#    - Reutiliza capas: spring-boot-loader
#    - Reconstruye: dependencies, snapshot-dependencies, application
#    - Tiempo: ~1-2 minutos
#
# 5. Ejecutar contenedor:
#    docker run -d \
#      --name task-api \
#      -p 8080:8080 \
#      -e SPRING_PROFILES_ACTIVE=prod \
#      -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/taskmanagement_db \
#      task-management-api:latest
#
# 6. Ver logs:
#    docker logs -f task-api
#
# 7. Ver tamaño de imagen:
#    docker images task-management-api
#
# 8. Ver capas de la imagen:
#    docker history task-management-api:latest
#
# ============================================================================
# COMPARACIÓN DE TAMAÑOS
# ============================================================================
#
# Sin optimizaciones:
#   - Imagen completa: ~500MB (incluye JDK, Gradle, build tools)
#   - Push a registry: ~500MB cada vez
#
# Con multi-stage (sin layers):
#   - Imagen completa: ~200MB (solo JRE + JAR)
#   - Push a registry: ~200MB cada vez
#
# Con multi-stage + layers (este Dockerfile):
#   - Imagen completa: ~200MB (solo JRE + capas)
#   - Push a registry (cambio de código): ~5MB (solo capa application)
#   - Mejora: 40x menos transferencia de datos
#
# ============================================================================
# OPTIMIZACIONES FUTURAS
# ============================================================================
#
# 1. JLINK (Custom JRE):
#    - Crear JRE personalizado con solo módulos necesarios
#    - Reduce imagen a ~80-100MB
#
# 2. GRAALVM NATIVE IMAGE:
#    - Compilación nativa (sin JVM)
#    - Imagen final: ~30-50MB
#    - Startup: <100ms (vs ~3-5s con JVM)
#    - Requiere configuración adicional
#
# 3. DISTROLESS IMAGES:
#    - Usar imágenes base sin shell ni herramientas
#    - Más seguras (menor superficie de ataque)
#    - Más pequeñas
#
# ============================================================================
