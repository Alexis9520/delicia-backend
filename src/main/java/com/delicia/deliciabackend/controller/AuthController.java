package com.delicia.deliciabackend.controller;

import com.delicia.deliciabackend.dto.AuthRequest;
import com.delicia.deliciabackend.dto.AuthResponse;
import com.delicia.deliciabackend.model.Usuario;
import com.delicia.deliciabackend.repository.UsuarioRepository;
import com.delicia.deliciabackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            String token = authService.login(request.getEmail(), request.getPassword());
            if (token == null) {
                // Credenciales inválidas -> devolver 401 con body JSON
                return ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas"));
            }
            return ResponseEntity.ok(new AuthResponse(token)); // <-- Asegura enviar correctamente el token
        } catch (Exception ex) {
            // Manejo defensivo: devolver 500 con mensaje genérico (no leaks de stack en prod)
            return ResponseEntity.status(500).body(Map.of("error", "Error interno al iniciar sesión"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            return ResponseEntity.status(400).body(Map.of("error", "El correo ya está registrado."));
        }
        Usuario created = authService.register(usuario);
        return ResponseEntity.ok(created);
    }
}