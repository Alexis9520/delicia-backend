package com.delicia.deliciabackend.controller;

import com.delicia.deliciabackend.dto.UserRequest;
import com.delicia.deliciabackend.dto.UserResponse;
import com.delicia.deliciabackend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<UserResponse> getAll(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search
    ) {
        if (role != null || search != null) {
            return usuarioService.search(role, search);
        } else {
            return usuarioService.getAll();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponse getById(@PathVariable Long id) {
        return usuarioService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponse create(@RequestBody UserRequest req) {
        return usuarioService.create(req);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponse updateRole(@PathVariable Long id, @RequestBody UserRequest req) {
        return usuarioService.updateRole(id, req.getRole());
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody UserRequest req) {
        usuarioService.resetPassword(id, req.getPassword());
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponse update(@PathVariable Long id, @RequestBody UserRequest req) {
        return usuarioService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado"));
    }
}