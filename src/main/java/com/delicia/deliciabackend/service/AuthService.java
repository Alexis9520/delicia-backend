package com.delicia.deliciabackend.service;

import com.delicia.deliciabackend.model.Usuario;
import com.delicia.deliciabackend.repository.UsuarioRepository;
import com.delicia.deliciabackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(String email, String password) {
        Optional<Usuario> optionalUsuario = usuarioRepository.findByEmail(email);
        if (optionalUsuario.isPresent() && passwordEncoder.matches(password, optionalUsuario.get().getPassword())) {
            Usuario usuario = optionalUsuario.get();
            // Rol en formato Spring: "ROLE_ADMIN", "ROLE_TRABAJADOR", etc.
            String springRole = getSpringRole(usuario.getRole());
            // CORRECTO: Usamos solo .signWith(signingKey) via JwtUtil
            return jwtUtil.generateToken(usuario.getEmail(), springRole, usuario.getName());
        }
        return null;
    }

    public Usuario register(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRole("ROLE_CLIENTE"); // Solo registro público como cliente
        return usuarioRepository.save(usuario);
    }

    public Usuario registerWithRole(Usuario usuario, String role) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRole(getSpringRole(role));
        return usuarioRepository.save(usuario);
    }

    // Convierte cualquier rol a formato Spring ("ROLE_ADMIN", etc.)
    private String getSpringRole(String role) {
        if (role.startsWith("ROLE_")) {
            return role;
        }
        return "ROLE_" + role.toUpperCase();
    }
}