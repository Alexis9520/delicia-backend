package com.delicia.deliciabackend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT con logs informativos (temporal).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String rawAuth = request.getHeader("Authorization");
            logger.info("REQ {} {} - Authorization header present? {}", request.getMethod(), request.getRequestURI(), rawAuth != null);

            final String authHeader = rawAuth;
            String email = null;
            String jwt = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7).trim();
                logger.info("Raw JWT (first 20 chars): {}", jwt.length() > 20 ? jwt.substring(0, 20) + "..." : jwt);
                try {
                    email = jwtUtil.extractEmail(jwt);
                    logger.info("extractEmail -> {}", email);
                } catch (ExpiredJwtException e) {
                    logger.info("JWT expirado para request {}: {}", request.getRequestURI(), e.getMessage());
                } catch (JwtException e) {
                    logger.warn("Token JWT inválido en request {}: {}", request.getRequestURI(), e.getMessage());
                } catch (Exception e) {
                    logger.error("Error procesando token JWT en request {}: {}", request.getRequestURI(), e.getMessage());
                }
            } else {
                logger.info("No Authorization header o no comienza con Bearer.");
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
                    logger.info("UserDetails cargado para email {} -> authorities: {}", email, userDetails.getAuthorities());

                    boolean valid = jwtUtil.isTokenValid(jwt, email);
                    logger.info("jwtUtil.isTokenValid -> {}", valid);

                    if (valid) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.info("Authentication establecida en SecurityContext para {}", email);
                    } else {
                        logger.info("JWT no válido para usuario {} en request {}", email, request.getRequestURI());
                    }
                } catch (Exception ex) {
                    logger.warn("No se pudo cargar UserDetails para email {}: {}", email, ex.getMessage());
                }
            }

            logger.info("SecurityContext authentication presente? {}", SecurityContextHolder.getContext().getAuthentication() != null);
        } catch (Throwable t) {
            logger.error("Error inesperado en JwtAuthenticationFilter: {}", t.getMessage(), t);
        }

        filterChain.doFilter(request, response);
    }
}