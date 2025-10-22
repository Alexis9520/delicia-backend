package com.delicia.deliciabackend.dto;

public class UserRequest {
    private String email;
    private String name;
    private String password;
    private String role;
    private String phone; // Campo agregado

    public UserRequest() {}

    public UserRequest(String email, String name, String password, String role, String phone) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
        this.phone = phone;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}