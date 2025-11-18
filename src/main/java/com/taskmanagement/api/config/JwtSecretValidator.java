package com.taskmanagement.api.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Validador de JWT Secret al inicio de la aplicación.
 *
 * SEGURIDAD CRÍTICA:
 * Esta clase verifica que NO se esté usando el JWT secret por defecto
 * que viene hardcodeado en la configuración. El uso del secret por defecto
 * es un riesgo de seguridad CRÍTICO en producción.
 *
 * FUNCIONAMIENTO:
 * - Se ejecuta automáticamente al iniciar la aplicación (@PostConstruct)
 * - Compara el secret configurado con el valor por defecto conocido
 * - Si coinciden, DETIENE el arranque de la aplicación con una excepción
 * - Proporciona instrucciones claras sobre cómo configurar un secret seguro
 *
 * CÓMO CONFIGURAR UN SECRET SEGURO:
 *
 * 1. Generar un secret fuerte:
 *    ```bash
 *    # Opción 1: OpenSSL (recomendado)
 *    openssl rand -base64 64
 *
 *    # Opción 2: Script del proyecto
 *    ./scripts/generate-secrets.sh
 *    ```
 *
 * 2. Configurar como variable de entorno:
 *    ```bash
 *    # Linux/Mac
 *    export JWT_SECRET="tu-secret-generado-aqui"
 *
 *    # Windows (PowerShell)
 *    $env:JWT_SECRET="tu-secret-generado-aqui"
 *
 *    # Docker Compose
 *    environment:
 *      - JWT_SECRET=tu-secret-generado-aqui
 *
 *    # Kubernetes Secret
 *    kubectl create secret generic jwt-secret --from-literal=JWT_SECRET=tu-secret-generado-aqui
 *    ```
 *
 * 3. Para desarrollo local (NO PRODUCCIÓN):
 *    - Crear archivo .env en el root del proyecto
 *    - Agregar: JWT_SECRET=tu-secret-generado-aqui
 *    - El archivo .env está en .gitignore (no se commitea)
 *
 * VALIDACIONES REALIZADAS:
 * - ✅ Secret no es el valor por defecto inseguro
 * - ✅ Secret tiene longitud mínima de 32 caracteres
 * - ✅ Secret no está vacío
 *
 * POR QUÉ ES IMPORTANTE:
 * - El secret se usa para firmar todos los JWT tokens
 * - Si un atacante conoce el secret, puede generar tokens válidos
 * - Puede suplantar cualquier usuario del sistema
 * - Acceso total a la API sin credenciales
 *
 * @see JwtService
 */
@Configuration
@Slf4j
public class JwtSecretValidator {

    /**
     * Secret configurado en la aplicación.
     * Se obtiene de la propiedad jwt.secret-key configurada en application.yml
     */
    @Value("${jwt.secret-key}")
    private String jwtSecret;

    /**
     * Perfil activo de Spring (dev, test, prod)
     */
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * Secret por defecto INSEGURO que viene en la configuración.
     * Este valor está hardcodeado aquí solo para comparación.
     * ⚠️ NUNCA usar este valor en producción
     */
    private static final String INSECURE_DEFAULT_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    /**
     * Longitud mínima recomendada para el secret (en caracteres)
     */
    private static final int MINIMUM_SECRET_LENGTH = 32;

    /**
     * Valida el JWT secret al inicio de la aplicación.
     *
     * Este método se ejecuta automáticamente después de que Spring
     * inicialice el bean, gracias a la anotación @PostConstruct.
     *
     * CRITERIOS DE VALIDACIÓN:
     * 1. El secret NO debe estar vacío
     * 2. El secret NO debe ser el valor por defecto inseguro
     * 3. El secret debe tener al menos 32 caracteres
     *
     * Si alguna validación falla:
     * - Se registra un ERROR en los logs
     * - Se lanza IllegalStateException
     * - La aplicación NO arranca (falla el startup)
     *
     * @throws IllegalStateException si el secret no cumple los criterios de seguridad
     */
    @PostConstruct
    public void validateJwtSecret() {
        log.info("=============================================================");
        log.info("Validando configuración de seguridad JWT...");
        log.info("Perfil activo: {}", activeProfile);
        log.info("=============================================================");

        // =====================================================================
        // VALIDACIÓN 1: Secret no debe estar vacío
        // =====================================================================
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            logSecurityError("JWT secret está vacío o no configurado");
            throw new IllegalStateException(
                "SEGURIDAD CRÍTICA: JWT secret no configurado. " +
                "Configura la variable de entorno JWT_SECRET antes de iniciar la aplicación."
            );
        }

        // =====================================================================
        // VALIDACIÓN 2: Secret no debe ser el valor por defecto
        // =====================================================================
        if (INSECURE_DEFAULT_SECRET.equals(jwtSecret)) {
            logSecurityError("JWT secret es el valor por defecto INSEGURO");

            log.error("╔════════════════════════════════════════════════════════════════╗");
            log.error("║  ⚠️  CONFIGURACIÓN DE SEGURIDAD INSEGURA DETECTADA  ⚠️        ║");
            log.error("╚════════════════════════════════════════════════════════════════╝");
            log.error("");
            log.error("El JWT secret configurado es el valor por defecto.");
            log.error("Este valor es CONOCIDO PÚBLICAMENTE y NO debe usarse en producción.");
            log.error("");
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("RIESGO DE SEGURIDAD:");
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("• Un atacante puede generar tokens JWT válidos");
            log.error("• Suplantar la identidad de cualquier usuario");
            log.error("• Acceso total a la API sin credenciales");
            log.error("• Compromiso completo de la seguridad del sistema");
            log.error("");
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("SOLUCIÓN - Configurar un JWT secret seguro:");
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("");
            log.error("1. Generar un secret fuerte:");
            log.error("   $ openssl rand -base64 64");
            log.error("   $ ./scripts/generate-secrets.sh");
            log.error("");
            log.error("2. Configurar como variable de entorno:");
            log.error("   Linux/Mac:");
            log.error("     $ export JWT_SECRET=\"tu-secret-generado\"");
            log.error("");
            log.error("   Windows (PowerShell):");
            log.error("     $ $env:JWT_SECRET=\"tu-secret-generado\"");
            log.error("");
            log.error("   Docker Compose:");
            log.error("     environment:");
            log.error("       - JWT_SECRET=tu-secret-generado");
            log.error("");
            log.error("   Kubernetes:");
            log.error("     kubectl create secret generic jwt-secret \\");
            log.error("       --from-literal=JWT_SECRET=tu-secret-generado");
            log.error("");
            log.error("3. Reiniciar la aplicación");
            log.error("");
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("NOTA: Para desarrollo local, puedes usar archivo .env");
            log.error("      (NUNCA commitear el .env al repositorio)");
            log.error("═══════════════════════════════════════════════════════════════");
            log.error("");

            throw new IllegalStateException(
                "SEGURIDAD CRÍTICA: JWT secret por defecto detectado. " +
                "Configura un secret seguro mediante la variable de entorno JWT_SECRET. " +
                "Ver logs arriba para instrucciones detalladas."
            );
        }

        // =====================================================================
        // VALIDACIÓN 3: Secret debe tener longitud mínima
        // =====================================================================
        if (jwtSecret.length() < MINIMUM_SECRET_LENGTH) {
            logSecurityError(String.format(
                "JWT secret demasiado corto (%d caracteres). Mínimo recomendado: %d caracteres",
                jwtSecret.length(),
                MINIMUM_SECRET_LENGTH
            ));

            log.error("╔════════════════════════════════════════════════════════════════╗");
            log.error("║  ⚠️  JWT SECRET DEMASIADO CORTO  ⚠️                            ║");
            log.error("╚════════════════════════════════════════════════════════════════╝");
            log.error("");
            log.error("Longitud actual: {} caracteres", jwtSecret.length());
            log.error("Longitud mínima recomendada: {} caracteres", MINIMUM_SECRET_LENGTH);
            log.error("");
            log.error("Un secret corto es más vulnerable a ataques de fuerza bruta.");
            log.error("Genera un secret de al menos 64 caracteres:");
            log.error("  $ openssl rand -base64 64");
            log.error("");

            throw new IllegalStateException(
                String.format(
                    "SEGURIDAD: JWT secret demasiado corto (%d caracteres). " +
                    "Debe tener al menos %d caracteres. " +
                    "Genera uno nuevo con: openssl rand -base64 64",
                    jwtSecret.length(),
                    MINIMUM_SECRET_LENGTH
                )
            );
        }

        // =====================================================================
        // VALIDACIÓN EXITOSA
        // =====================================================================
        log.info("✅ JWT secret validado correctamente");
        log.info("   - Longitud: {} caracteres", jwtSecret.length());
        log.info("   - No es el valor por defecto: ✓");
        log.info("   - Cumple longitud mínima: ✓");

        // Log de advertencia si estamos en producción
        if ("prod".equalsIgnoreCase(activeProfile) || "production".equalsIgnoreCase(activeProfile)) {
            log.warn("╔════════════════════════════════════════════════════════════════╗");
            log.warn("║  AMBIENTE DE PRODUCCIÓN DETECTADO                             ║");
            log.warn("╚════════════════════════════════════════════════════════════════╝");
            log.warn("");
            log.warn("Asegúrate de que el JWT secret:");
            log.warn("  ✓ Está configurado mediante secretos seguros (Kubernetes Secrets, AWS Secrets Manager, etc.)");
            log.warn("  ✓ NO está hardcodeado en código o archivos de configuración");
            log.warn("  ✓ Tiene permisos de acceso restringidos");
            log.warn("  ✓ Se rota periódicamente (cada 90 días recomendado)");
            log.warn("  ✓ Nunca se registra en logs");
            log.warn("");
        }

        log.info("=============================================================");
        log.info("Validación de seguridad JWT completada exitosamente");
        log.info("=============================================================");
    }

    /**
     * Registra un error de seguridad crítico en los logs.
     *
     * @param message Mensaje de error
     */
    private void logSecurityError(String message) {
        log.error("");
        log.error("╔════════════════════════════════════════════════════════════════╗");
        log.error("║                  ⚠️  ERROR DE SEGURIDAD  ⚠️                    ║");
        log.error("╚════════════════════════════════════════════════════════════════╝");
        log.error("");
        log.error("DETALLE: {}", message);
        log.error("");
    }
}
