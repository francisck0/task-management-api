package com.taskmanagement.api.security;

import com.taskmanagement.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT que intercepta todas las peticiones HTTP.
 *
 * PATRÓN: Chain of Responsibility
 * Este filtro es parte de la cadena de filtros de Spring Security.
 * Se ejecuta una vez por petición (OncePerRequestFilter).
 *
 * FLUJO:
 * 1. Extrae el token JWT del header "Authorization"
 * 2. Valida el token y extrae el username
 * 3. Carga los detalles del usuario desde la BD
 * 4. Establece la autenticación en el SecurityContext
 * 5. Permite que la petición continúe
 *
 * FORMATO DEL HEADER:
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extraer el header Authorization
        final String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer ", continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extraer el token (quitando el prefijo "Bearer ")
            final String jwt = authHeader.substring(7);

            // 3. Extraer el username del token
            final String username = jwtService.extractUsername(jwt);

            // 4. Si hay username y el usuario no está ya autenticado
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Cargar los detalles del usuario desde la BD
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 6. Validar el token
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // 7. Crear el objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // 8. Agregar detalles de la petición
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 9. Establecer la autenticación en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Usuario '{}' autenticado exitosamente mediante JWT", username);
                }
            }

        } catch (Exception e) {
            log.error("Error al procesar el token JWT: {}", e.getMessage());
            // No lanzamos la excepción, solo la registramos
            // La petición continuará pero sin autenticación
        }

        // 10. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}
