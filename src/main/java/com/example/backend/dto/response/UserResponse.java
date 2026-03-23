package com.example.backend.dto.response;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin người dùng cho admin.
 * Không bao giờ expose password.
 */
@Getter
@Builder
public class UserResponse {

    private Long          id;
    private String        fullName;
    private String        email;
    private String        role;          // tên enum dạng String (GUEST / HOTEL_OWNER / ADMIN)
    private String        roleLabel;     // tên hiển thị tiếng Việt
    private LocalDateTime createdAt;

    /* ── Factory ────────────────────────────────────────────────────────────── */

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .roleLabel(roleLabelOf(user.getRole()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    /* ── Helper ─────────────────────────────────────────────────────────────── */

    private static String roleLabelOf(Role role) {
        return switch (role) {
            case GUEST       -> "Khách hàng";
            case HOTEL_OWNER -> "Chủ khách sạn";
            case ADMIN       -> "Quản trị viên";
        };
    }
}
