package com.delicia.deliciabackend.controller;

import com.delicia.deliciabackend.dto.AuthRequest;
import com.delicia.deliciabackend.dto.AuthResponse;
import com.delicia.deliciabackend.model.Usuario;
import com.delicia.deliciabackend.repository.UsuarioRepository;
import com.delicia.deliciabackend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        if (token == null) {
            throw new RuntimeException("Credenciales inválidas");
        }
        return new AuthResponse(token);
    }

    @PostMapping("/register")
    public Usuario register(@RequestBody Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El correo ya está registrado.");
        }
        return authService.register(usuario);
    }
}