package com.taskmanagement.api.security;

import com.taskmanagement.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad de Spring Security.
 *
 * Esta clase configura:
 * - Autenticación JWT
 * - Autorización basada en roles
 * - Rutas públicas vs protegidas
 * - Codificación de contraseñas
 * - Políticas de sesión (stateless para JWT)
 *
 * PATRÓN: Configuration Pattern
 * Centraliza toda la configuración de seguridad en un solo lugar.
 *
 * @EnableWebSecurity: Habilita Spring Security
 * @EnableMethodSecurity: Permite usar @PreAuthorize, @Secured, etc en métodos
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Configura la cadena de filtros de seguridad
     *
     * Define qué endpoints son públicos y cuáles requieren autenticación
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF (no necesario para APIs REST stateless con JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Habilitar CORS
                .cors(cors -> cors.configure(http))

                // Configurar autorización de peticiones
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas (no requieren autenticación)
                        .requestMatchers(
                                "/v1/auth/**",           // Endpoints de autenticación
                                "/swagger-ui/**",            // Swagger UI
                                "/v3/api-docs/**",           // OpenAPI docs
                                "/swagger-resources/**",     // Swagger resources
                                "/webjars/**",               // Webjars (para Swagger)
                                "/v1/actuator/health",   // Health check público
                                "/v1/actuator/info"      // Info público
                        ).permitAll()

                        // Actuator endpoints protegidos (requieren autenticación)
                        .requestMatchers("/v1/actuator/**").authenticated()

                        // Todas las demás rutas requieren autenticación
                        .anyRequest().authenticated()
                )

                // Configurar política de sesión (STATELESS para JWT)
                // No creamos ni usamos sesiones HTTP
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configurar el proveedor de autenticación
                .authenticationProvider(authenticationProvider())

                // Agregar el filtro JWT ANTES del filtro de autenticación estándar
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Proveedor de autenticación que usa UserDetailsService y PasswordEncoder
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * AuthenticationManager para autenticar usuarios
     *
     * Lo usaremos en el servicio de autenticación para validar credenciales
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Codificador de contraseñas BCrypt
     *
     * BCrypt es un algoritmo de hashing diseñado para ser lento
     * (protege contra ataques de fuerza bruta)
     *
     * NUNCA guardar contraseñas en texto plano en la BD
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
