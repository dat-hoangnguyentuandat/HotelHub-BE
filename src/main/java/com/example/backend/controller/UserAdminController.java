package com.example.backend.controller;

import com.example.backend.dto.request.AdminCreateUserRequest;
import com.example.backend.dto.response.CustomerStatsResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import com.example.backend.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller – Quản lý người dùng / khách hàng (dành cho ADMIN).
 *
 * Base path: /api/admin/users
 *
 * Endpoints:
 *   GET    /api/admin/users              – Danh sách (có filter keyword + role)
 *   GET    /api/admin/users/stats        – Thống kê
 *   GET    /api/admin/users/me           – Thông tin user đang đăng nhập
 *   GET    /api/admin/users/{id}         – Chi tiết 1 user
 *   POST   /api/admin/users              – Admin tạo user mới
 *   PATCH  /api/admin/users/{id}/role    – Đổi role
 *   DELETE /api/admin/users/{id}         – Xoá user
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final CustomerService  customerService;
    private final UserRepository   userRepository;   // dùng riêng cho /me
    private final JwtService       jwtService;

    /* ══════════════════════════════════════════════════════════════════════════
       GET /api/admin/users
       Danh sách tất cả user, lọc theo keyword (tên/email) và role.
    ══════════════════════════════════════════════════════════════════════════ */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> listUsers(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "role",    required = false) Role role
    ) {
        List<UserResponse> users = customerService.listUsers(keyword, role);
        return ResponseEntity.ok(users);
    }

    /* ══════════════════════════════════════════════════════════════════════════
       GET /api/admin/users/stats
       Thống kê tổng hợp: tổng user, từng role, mới trong tháng.
    ══════════════════════════════════════════════════════════════════════════ */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerStatsResponse> getStats() {
        return ResponseEntity.ok(customerService.getStats());
    }

    /* ══════════════════════════════════════════════════════════════════════════
       GET /api/admin/users/me
       Thông tin user đang đăng nhập (không yêu cầu ADMIN).
    ══════════════════════════════════════════════════════════════════════════ */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return ResponseEntity.ok(UserResponse.from(user));
    }

    /* ══════════════════════════════════════════════════════════════════════════
       GET /api/admin/users/{id}
       Chi tiết 1 user.
    ══════════════════════════════════════════════════════════════════════════ */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getUser(id));
    }

    /* ══════════════════════════════════════════════════════════════════════════
       POST /api/admin/users
       Admin tạo user mới (có thể chỉ định role, không cần confirmPassword).
    ══════════════════════════════════════════════════════════════════════════ */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request
    ) {
        UserResponse created = customerService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /* ══════════════════════════════════════════════════════════════════════════
       PATCH /api/admin/users/{id}/role
       Thay đổi role của 1 user.
    ══════════════════════════════════════════════════════════════════════════ */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable Long id,
            @RequestBody  ChangeRoleRequest body
    ) {
        UserResponse updated = customerService.changeRole(id, body.getRole());
        return ResponseEntity.ok(updated);
    }

    /* ══════════════════════════════════════════════════════════════════════════
       DELETE /api/admin/users/{id}
       Xoá user – tự động dọn LoyaltyAccount & LoyaltyTransaction trước.
    ══════════════════════════════════════════════════════════════════════════ */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        customerService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "Xoá khách hàng thành công"));
    }

    /* ── Inner DTO ─────────────────────────────────────────────────────────── */

    @Getter
    @Setter
    static class ChangeRoleRequest {
        @NotNull(message = "Vai trò không được trống")
        private Role role;
    }
}
