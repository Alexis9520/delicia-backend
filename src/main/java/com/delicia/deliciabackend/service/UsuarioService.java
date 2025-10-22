package com.delicia.deliciabackend.service;

import com.delicia.deliciabackend.dto.UserRequest;
import com.delicia.deliciabackend.dto.UserResponse;

import java.util.List;

public interface UsuarioService {
    List<UserResponse> getAll();
    UserResponse getById(Long id);
    UserResponse create(UserRequest req);
    UserResponse updateRole(Long id, String role);
    void resetPassword(Long id, String newPassword);
    UserResponse update(Long id, UserRequest req); // <-- Nuevo
    void delete(Long id); // <-- Nuevo
    List<UserResponse> search(String role, String search); // <-- Opcional para filtros
}