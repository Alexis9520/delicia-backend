package com.delicia.deliciabackend.controller;

import com.delicia.deliciabackend.model.Usuario;
import com.delicia.deliciabackend.repository.UsuarioRepository;
import com.delicia.deliciabackend.service.AuthService;
import com.delicia.deliciabackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    @Autowired
    private AuthService authService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Usuario register(@RequestBody Usuario usuario, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("No autorizado");
        }
        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);

        if (!"admin".equals(role) && !"administrador".equals(role)) {
            throw new RuntimeException("Solo admin puede crear usuarios trabajadores o admin");
        }
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El correo ya está registrado.");
        }
        // Acepta role enviado por el admin (trabajador o admin)
        return authService.registerWithRole(usuario, usuario.getRole());
    }
}