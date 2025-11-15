package com.taskmanagement.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Servicio para generar y validar tokens JWT.
 *
 * JWT (JSON Web Token) es un estándar (RFC 7519) para transmitir información
 * de forma segura entre partes como un objeto JSON.
 *
 * COMPONENTES DE UN JWT:
 * 1. Header: Metadatos del token (algoritmo, tipo)
 * 2. Payload: Claims (datos del usuario, roles, expiración, etc)
 * 3. Signature: Firma para verificar que no ha sido modificado
 *
 * VENTAJAS DE JWT:
 * - Stateless: No necesita almacenar sesiones en el servidor
 * - Escalable: Perfecto para microservicios y APIs REST
 * - Portable: Se puede usar en web, mobile, etc
 * - Auto-contenido: Toda la info necesaria está en el token
 *
 * SEGURIDAD:
 * - La clave secreta NUNCA debe estar hardcodeada
 * - Debe estar en variables de entorno o archivos de configuración seguros
 * - Los tokens tienen fecha de expiración para limitar el riesgo
 */
@Service
public class JwtService {

    /**
     * Clave secreta para firmar los tokens JWT
     * DEBE ser lo suficientemente larga y compleja
     * En producción, usar variables de entorno
     */
    @Value("${jwt.secret-key:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    /**
     * Tiempo de expiración del token en milisegundos
     * Por defecto: 24 horas (86400000 ms)
     */
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    /**
     * Genera un token JWT para un usuario
     *
     * @param userDetails Detalles del usuario (de Spring Security)
     * @return Token JWT como String
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Genera un token JWT con claims adicionales
     *
     * @param extraClaims Claims adicionales a incluir en el token
     * @param userDetails Detalles del usuario
     * @return Token JWT como String
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrae el username del token JWT
     *
     * @param token Token JWT
     * @return Username extraído del token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token
     *
     * @param token Token JWT
     * @param claimsResolver Función para extraer el claim deseado
     * @return Valor del claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Valida si un token es válido para un usuario específico
     *
     * @param token Token JWT
     * @param userDetails Detalles del usuario
     * @return true si el token es válido, false si no
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Verifica si un token ha expirado
     *
     * @param token Token JWT
     * @return true si ha expirado, false si aún es válido
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrae la fecha de expiración del token
     *
     * @param token Token JWT
     * @return Fecha de expiración
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae todos los claims del token
     *
     * @param token Token JWT
     * @return Claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Obtiene la clave de firma a partir de la clave secreta
     *
     * @return Clave de firma
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
