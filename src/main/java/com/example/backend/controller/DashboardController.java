package com.example.backend.controller;

import com.example.backend.dto.response.DashboardResponse;
import com.example.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * GET /api/admin/dashboard
     * Trả về toàn bộ dữ liệu cho trang Dashboard admin.
     * Yêu cầu role ADMIN hoặc HOTEL_OWNER.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboardData());
    }
}
