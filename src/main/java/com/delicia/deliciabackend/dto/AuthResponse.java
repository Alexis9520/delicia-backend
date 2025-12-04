package com.delicia.deliciabackend.dto;

public class AuthResponse {
    private String token;

    // Constructor vacío requerido por Jackson/Spring para serializar y deserializar correctamente
    public AuthResponse() {}

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}