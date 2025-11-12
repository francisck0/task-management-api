package com.taskmanagement.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing).
 *
 * Capa CONFIG: Contiene clases de configuración de Spring Boot.
 *
 * CORS es un mecanismo de seguridad que permite o restringe que aplicaciones
 * web desde otros dominios accedan a los recursos de tu API.
 *
 * @Configuration: Indica que esta clase contiene definiciones de beans de Spring
 *
 * Escenario de uso:
 * - Frontend en http://localhost:3000 (React, Angular, Vue)
 * - Backend en http://localhost:8080 (Spring Boot)
 * - Sin CORS, el navegador bloquearía las peticiones del frontend al backend
 */
@Configuration
public class CorsConfig {

    /**
     * Configura el filtro CORS para permitir peticiones cross-origin.
     *
     * @Bean: Indica que este método produce un bean gestionado por Spring
     *
     * IMPORTANTE: Esta es una configuración permisiva para DESARROLLO.
     * En PRODUCCIÓN, debes restringir los orígenes permitidos a dominios específicos.
     *
     * @return CorsFilter configurado
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permitir credenciales (cookies, headers de autenticación)
        config.setAllowCredentials(true);

        // Orígenes permitidos
        // DESARROLLO: Permite localhost en diferentes puertos
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",    // React default
                "http://localhost:4200",    // Angular default
                "http://localhost:5173",    // Vite default
                "http://localhost:8081"     // Otro puerto común
        ));

        // PRODUCCIÓN: Especifica tus dominios exactos
        // config.setAllowedOrigins(List.of("https://tudominio.com"));

        // Headers permitidos en las peticiones
        config.setAllowedHeaders(Arrays.asList(
                "Origin",
                "Content-Type",
                "Accept",
                "Authorization",
                "X-Requested-With"
        ));

        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "PATCH"
        ));

        // Headers expuestos en las respuestas (que el cliente puede leer)
        config.setExposedHeaders(List.of("Authorization"));

        // Tiempo en segundos que el navegador puede cachear la respuesta preflight
        config.setMaxAge(3600L);

        // Aplicar configuración a todos los endpoints
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
