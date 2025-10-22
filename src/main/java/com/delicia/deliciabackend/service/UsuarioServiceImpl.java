package com.delicia.deliciabackend.service;

import com.delicia.deliciabackend.dto.UserRequest;
import com.delicia.deliciabackend.dto.UserResponse;
import com.delicia.deliciabackend.model.Usuario;
import com.delicia.deliciabackend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> getAll() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        return usuarios.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public UserResponse getById(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        return toResponse(usuario);
    }

    @Override
    public UserResponse create(UserRequest req) {
        if (usuarioRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email ya registrado");
        }
        Usuario usuario = new Usuario();
        usuario.setEmail(req.getEmail());
        usuario.setName(req.getName());
        usuario.setRole(req.getRole());
        usuario.setPhone(req.getPhone());
        usuario.setPassword(passwordEncoder.encode(req.getPassword()));
        usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    @Override
    public UserResponse updateRole(Long id, String role) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setRole(role);
        usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
    }

    @Override
    public UserResponse update(Long id, UserRequest req) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setName(req.getName());
        usuario.setEmail(req.getEmail());
        usuario.setPhone(req.getPhone());
        usuario.setRole(req.getRole());
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    @Override
    public void delete(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    public List<UserResponse> search(String role, String search) {
        List<Usuario> usuarios;

        if (role != null && !role.isEmpty()) {
            usuarios = usuarioRepository.findByRole(role);
        } else {
            usuarios = usuarioRepository.findAll();
        }

        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            usuarios = usuarios.stream()
                    .filter(u -> u.getName().toLowerCase().contains(searchLower)
                            || u.getEmail().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        return usuarios.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private UserResponse toResponse(Usuario usuario) {
        return new UserResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getName(),
                usuario.getRole()
        );
    }
}