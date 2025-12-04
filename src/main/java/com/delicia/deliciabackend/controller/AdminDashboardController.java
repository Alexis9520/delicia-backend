package com.delicia.deliciabackend.controller;

import com.delicia.deliciabackend.dto.admin.AdminDashboardResponse;
import com.delicia.deliciabackend.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public AdminDashboardResponse getDashboard() {
        return adminDashboardService.getDashboard();
    }
}
