package com.example.backend.controller;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtService;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserRepository userRepository;
    private final JwtService     jwtService;

    /* ── Đổi role của user bất kỳ (chỉ ADMIN) ── */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> changeRole(
            @PathVariable Long id,
            @RequestBody ChangeRoleRequest body
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user #" + id));

        user.setRole(body.getRole());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Đổi role thành công",
                "userId",  id,
                "newRole", body.getRole().name()
        ));
    }

    /* ── Lấy thông tin user đang đăng nhập ── */
    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ResponseEntity.ok(Map.of(
                "id",       user.getId(),
                "fullName", user.getFullName(),
                "email",    user.getEmail(),
                "role",     user.getRole().name()
        ));
    }

    /* ── Danh sách tất cả user (ADMIN) ── */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listUsers() {
        var users = userRepository.findAll().stream().map(u -> Map.of(
                "id",       u.getId(),
                "fullName", u.getFullName(),
                "email",    u.getEmail(),
                "role",     u.getRole().name(),
                "createdAt",u.getCreatedAt().toString()
        )).toList();
        return ResponseEntity.ok(users);
    }

    @Getter @Setter
    static class ChangeRoleRequest {
        @NotNull
        private Role role;
    }
}
