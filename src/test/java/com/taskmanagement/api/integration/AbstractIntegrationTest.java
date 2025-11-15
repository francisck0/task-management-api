package com.taskmanagement.api.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base abstracta para todos los tests de integración.
 *
 * ============================================================================
 * ¿QUÉ SON LOS TESTS DE INTEGRACIÓN?
 * ============================================================================
 * Los tests de integración verifican que múltiples componentes del sistema
 * funcionan correctamente cuando se integran entre sí.
 *
 * DIFERENCIAS: Tests Unitarios vs Tests de Integración
 *
 * ┌─────────────────────┬──────────────────────┬──────────────────────────┐
 * │   CARACTERÍSTICA    │   TESTS UNITARIOS    │   TESTS INTEGRACIÓN      │
 * ├─────────────────────┼──────────────────────┼──────────────────────────┤
 * │ Alcance             │ UNA unidad (método)  │ Múltiples componentes    │
 * │ Dependencias        │ Mockeadas            │ Reales                   │
 * │ Base de Datos       │ Mock/No hay          │ PostgreSQL REAL          │
 * │ Velocidad           │ Muy rápido (ms)      │ Lento (segundos)         │
 * │ Aislamiento         │ Total                │ Parcial                  │
 * │ Confiabilidad       │ Verifica lógica      │ Verifica integración     │
 * │ Cobertura           │ Código               │ Flujo completo           │
 * │ Mantenimiento       │ Fácil                │ Más complejo             │
 * │ Cuándo ejecutar     │ Siempre              │ Pre-commit/CI            │
 * └─────────────────────┴──────────────────────┴──────────────────────────┘
 *
 * EJEMPLO:
 * - Test Unitario: Verifica que TaskService.createTask() mapea correctamente
 * - Test Integración: Verifica que crear una tarea persiste en PostgreSQL
 *
 * ============================================================================
 * ¿QUÉ ES TESTCONTAINERS?
 * ============================================================================
 * TestContainers es una librería Java que permite ejecutar contenedores
 * Docker durante los tests.
 *
 * VENTAJAS DE TESTCONTAINERS:
 *
 * 1. BASE DE DATOS REAL:
 *    ✅ Usa PostgreSQL real (no H2 o HSQLDB)
 *    ✅ Detecta bugs específicos de PostgreSQL
 *    ✅ Prueba queries reales, tipos de datos, constraints
 *
 * 2. PARIDAD PRODUCCIÓN-TESTING:
 *    ✅ Misma BD en desarrollo, testing y producción
 *    ✅ Evita el problema: "Funciona en H2 pero falla en PostgreSQL"
 *    ✅ Comportamiento idéntico (transacciones, locks, índices)
 *
 * 3. AISLAMIENTO:
 *    ✅ Cada test suite puede tener su propia BD
 *    ✅ Estado limpio en cada ejecución
 *    ✅ No contamina la BD de desarrollo
 *
 * 4. PORTABILIDAD:
 *    ✅ Funciona en cualquier máquina con Docker
 *    ✅ No requiere instalación de PostgreSQL
 *    ✅ Mismo comportamiento en CI/CD
 *
 * 5. VERSIONADO:
 *    ✅ Puedes testear contra diferentes versiones de PostgreSQL
 *    ✅ Fácil actualizar versión de BD
 *
 * ============================================================================
 * TESTCONTAINERS vs BD EN MEMORIA (H2, HSQLDB):
 * ============================================================================
 *
 * PROBLEMAS CON BD EN MEMORIA:
 * ❌ Sintaxis SQL diferente (H2 vs PostgreSQL)
 * ❌ Tipos de datos diferentes
 * ❌ Funciones específicas no soportadas
 * ❌ Comportamiento de transacciones diferente
 * ❌ Constraints y validaciones diferentes
 * ❌ "Funciona en tests, falla en producción"
 *
 * EJEMPLO REAL:
 * ```sql
 * -- PostgreSQL: array_agg() existe
 * SELECT array_agg(id) FROM tasks;
 *
 * -- H2: array_agg() NO existe
 * -- Test pasa en H2, falla en producción! 😱
 * ```
 *
 * VENTAJAS DE TESTCONTAINERS:
 * ✅ PostgreSQL real = 100% paridad con producción
 * ✅ Detecta bugs específicos de PostgreSQL
 * ✅ Queries complejas funcionan igual
 * ✅ Mismos índices, constraints, tipos
 * ✅ Confianza total en los tests
 *
 * DESVENTAJAS DE TESTCONTAINERS:
 * ⚠️ Requiere Docker instalado
 * ⚠️ Más lento que BD en memoria (pero más confiable)
 * ⚠️ Consume más recursos (CPU, RAM)
 *
 * CONCLUSIÓN: Para aplicaciones serias en producción, TestContainers
 * es SUPERIOR a BD en memoria porque previene bugs críticos.
 *
 * ============================================================================
 * CONFIGURACIÓN DE TESTCONTAINERS EN ESTA CLASE:
 * ============================================================================
 *
 * @Testcontainers:
 *   - Activa el soporte de TestContainers en JUnit 5
 *   - Gestiona el ciclo de vida de los contenedores
 *
 * @Container:
 *   - Marca un contenedor para ser gestionado por TestContainers
 *   - El contenedor se inicia ANTES de los tests
 *   - Se detiene DESPUÉS de los tests
 *
 * @SpringBootTest:
 *   - Carga el contexto completo de Spring Boot
 *   - Configura toda la aplicación (como en producción)
 *   - Disponible en clases que hereden de esta
 *
 * @DynamicPropertySource:
 *   - Permite inyectar propiedades dinámicamente
 *   - Configura Spring para conectarse al contenedor PostgreSQL
 *   - Se ejecuta DESPUÉS de que el contenedor inicia
 *
 * ============================================================================
 * CICLO DE VIDA:
 * ============================================================================
 * 1. TestContainers inicia contenedor PostgreSQL
 * 2. PostgreSQL arranca en puerto aleatorio
 * 3. @DynamicPropertySource configura conexión
 * 4. Spring Boot se conecta a PostgreSQL del contenedor
 * 5. Se ejecutan los tests
 * 6. TestContainers detiene y elimina el contenedor
 *
 * ============================================================================
 * USO:
 * ============================================================================
 * Todos los tests de integración deben heredar de esta clase:
 *
 * ```java
 * class TaskRepositoryIntegrationTest extends AbstractIntegrationTest {
 *     // Los tests automáticamente usan PostgreSQL en contenedor
 * }
 * ```
 *
 * ============================================================================
 * OPTIMIZACIÓN: CONTENEDOR SINGLETON
 * ============================================================================
 * El contenedor es STATIC y se reutiliza entre tests para velocidad:
 * - Se inicia UNA VEZ al principio
 * - Se reutiliza en TODOS los tests
 * - Se detiene AL FINAL de todos los tests
 *
 * Esto hace los tests más rápidos (arrancar PostgreSQL toma ~3-5 segundos)
 *
 * ============================================================================
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    /**
     * Contenedor Docker con PostgreSQL para tests de integración.
     *
     * CONFIGURACIÓN:
     * - Imagen: postgres:16-alpine (versión ligera y rápida)
     * - Base de datos: testdb
     * - Usuario: testuser
     * - Contraseña: testpass
     *
     * STATIC: El contenedor se comparte entre todos los tests
     * para mejorar la velocidad (se inicia solo una vez)
     *
     * @Container: TestContainers gestiona el ciclo de vida automáticamente
     */
    @Container
    protected static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass")
                    .withReuse(true); // Reutilizar contenedor entre ejecuciones para velocidad

    /**
     * Configura las propiedades de Spring para conectarse al contenedor PostgreSQL.
     *
     * Este método se ejecuta DESPUÉS de que el contenedor PostgreSQL arranca
     * y ANTES de que Spring Boot inicie.
     *
     * IMPORTANTE:
     * - El contenedor usa un PUERTO ALEATORIO cada vez
     * - postgresContainer.getJdbcUrl() obtiene la URL correcta
     * - Sobrescribe las propiedades de application.yml
     *
     * @param registry Registro de propiedades dinámicas de Spring
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Configurar URL de conexión JDBC
        // Ejemplo: jdbc:postgresql://localhost:49153/testdb
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);

        // Configurar usuario de la base de datos
        registry.add("spring.datasource.username", postgresContainer::getUsername);

        // Configurar contraseña de la base de datos
        registry.add("spring.datasource.password", postgresContainer::getPassword);

        // Configurar Hibernate para crear/actualizar el esquema automáticamente
        // En tests de integración, queremos que Hibernate cree las tablas
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // Desactivar inicialización de datos (data.sql)
        // En tests de integración, controlamos los datos manualmente
        registry.add("spring.sql.init.mode", () -> "never");
    }

    /**
     * Hook que se puede sobrescribir en clases hijas para ejecutar
     * lógica antes de cada test.
     *
     * Útil para:
     * - Limpiar la base de datos
     * - Insertar datos de prueba comunes
     * - Resetear estado
     */
    protected void setUp() {
        // Sobrescribir en clases hijas si es necesario
    }

    /**
     * Hook que se puede sobrescribir en clases hijas para ejecutar
     * lógica después de cada test.
     *
     * Útil para:
     * - Limpiar datos
     * - Verificar estado final
     * - Cerrar recursos
     */
    protected void tearDown() {
        // Sobrescribir en clases hijas si es necesario
    }

    // =========================================================================
    // MÉTODOS AUXILIARES ÚTILES PARA TESTS
    // =========================================================================

    /**
     * Verifica que el contenedor PostgreSQL está corriendo.
     *
     * Útil para debugging si los tests fallan.
     *
     * @return true si el contenedor está corriendo, false si no
     */
    protected boolean isContainerRunning() {
        return postgresContainer.isRunning();
    }

    /**
     * Obtiene la URL JDBC del contenedor PostgreSQL.
     *
     * Útil para debugging y logs.
     *
     * @return URL JDBC del contenedor
     */
    protected String getDatabaseUrl() {
        return postgresContainer.getJdbcUrl();
    }

    /**
     * Obtiene el puerto del contenedor PostgreSQL.
     *
     * Útil para debugging.
     *
     * @return Puerto mapeado del contenedor
     */
    protected Integer getDatabasePort() {
        return postgresContainer.getFirstMappedPort();
    }
}
