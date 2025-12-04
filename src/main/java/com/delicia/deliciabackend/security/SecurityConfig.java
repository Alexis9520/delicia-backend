package com.delicia.deliciabackend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://37.60.233.86:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/products/**").permitAll()

                    .requestMatchers("/api/orders/all").hasAnyRole("TRABAJADOR", "ADMIN")
                    .requestMatchers("/api/orders/mostrador").hasAnyRole("TRABAJADOR", "ADMIN")

                    // Permitir acceso a endpoints de trabajador por ruta específica
                    .requestMatchers("/api/orders/worker/**").hasAnyRole("TRABAJADOR", "ADMIN")

                    // Permitir que trabajadores/administradores actualicen el estado del pedido
                    // Usar un matcher regex explícito para evitar patterns con ** en medio
                    .requestMatchers(new RegexRequestMatcher("^/api/orders/\\d+/status$", "PUT")).hasAnyRole("TRABAJADOR", "ADMIN")

                    // Permitir que trabajadores registren lotes de producción
                    .requestMatchers(HttpMethod.POST, "/api/inventario/lote-produccion").hasRole("TRABAJADOR")

                    .requestMatchers("/api/orders/**").hasRole("CLIENTE")

                    .requestMatchers("/api/payments/**").permitAll()
                    .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}