package com.taskmanagement.api.service;

import com.taskmanagement.api.dto.AuthResponse;
import com.taskmanagement.api.dto.LoginRequest;
import com.taskmanagement.api.dto.RegisterRequest;
import com.taskmanagement.api.model.Role;
import com.taskmanagement.api.model.User;
import com.taskmanagement.api.repository.RoleRepository;
import com.taskmanagement.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio de autenticación y registro de usuarios.
 *
 * Capa SERVICE: Contiene la lógica de negocio para autenticación.
 *
 * Responsabilidades:
 * - Registrar nuevos usuarios
 * - Autenticar usuarios existentes
 * - Generar tokens JWT
 * - Validar credenciales
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registra un nuevo usuario en el sistema
     *
     * @param request Datos del nuevo usuario
     * @return Respuesta con token JWT y datos del usuario
     * @throws RuntimeException si el username o email ya existen
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Intento de registro de nuevo usuario: {}", request.getUsername());

        // Validar que el username no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está en uso");
        }

        // Obtener el rol USER (todos los nuevos usuarios son USER por defecto)
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException(
                        "Error: Rol ROLE_USER no encontrado. ¿Ejecutaste el script de inicialización?"
                ));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        // Crear el nuevo usuario
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encriptar contraseña
        user.setFullName(request.getFullName());
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setRoles(roles);

        // Guardar en la base de datos
        User savedUser = userRepository.save(user);

        log.info("Usuario registrado exitosamente: {}", savedUser.getUsername());

        // Generar token JWT
        String jwt = jwtService.generateToken(savedUser);

        // Preparar respuesta
        List<String> roleNames = savedUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new AuthResponse(
                jwt,
                savedUser.getUsername(),
                savedUser.getEmail(),
                roleNames
        );
    }

    /**
     * Autentica un usuario y genera un token JWT
     *
     * @param request Credenciales de login
     * @return Respuesta con token JWT y datos del usuario
     * @throws RuntimeException si las credenciales son inválidas
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Intento de login de usuario: {}", request.getUsername());

        // Autenticar con Spring Security
        // Si las credenciales son inválidas, lanzará una excepción automáticamente
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Si llegamos aquí, las credenciales son válidas
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        log.info("Usuario autenticado exitosamente: {}", user.getUsername());

        // Generar token JWT
        String jwt = jwtService.generateToken(user);

        // Preparar respuesta
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return new AuthResponse(
                jwt,
                user.getUsername(),
                user.getEmail(),
                roleNames
        );
    }
}
