package com.delicia.deliciabackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilidad JWT para jjwt 0.11.x.
 * Usará HS256 siempre, usando tu secret key.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:}")
    private String secretConfig;

    @Value("${jwt.secret-base64:false}")
    private boolean secretIsBase64;

    @Value("${jwt.expiration-ms:43200000}") // 12 horas por defecto
    private long expirationMs;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        if (secretConfig == null || secretConfig.isBlank()) {
            throw new IllegalStateException("JWT secret no configurado. Define 'jwt.secret' en application.properties o como variable de entorno.");
        }

        byte[] keyBytes;
        if (secretIsBase64) {
            keyBytes = Decoders.BASE64.decode(secretConfig);
        } else {
            keyBytes = secretConfig.getBytes(StandardCharsets.UTF_8);
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);

        if (keyBytes.length < 32) {
            System.err.println("WARNING: jwt.secret tiene menos de 32 bytes. Para HS256 se recomiendan 256 bits (32 bytes) como mínimo.");
        }
    }

    public String generateToken(String email, String role, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("email", email);
        claims.put("name", name);

        // ¡POR DEFECTO usa HS256 aquí! No fuerces HS384, usa solo signingKey
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey) // ← HS256 automático
                .compact();
    }

    public Claims extractClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) throws JwtException {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) throws JwtException {
        Object v = extractClaims(token).get("role");
        return v != null ? String.valueOf(v) : null;
    }

    public boolean isTokenValid(String token, String email) {
        try {
            final String username = extractEmail(token);
            return (username != null && username.equals(email) && !isTokenExpired(token));
        } catch (JwtException ex) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date exp = extractClaims(token).getExpiration();
            return exp.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}