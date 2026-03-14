package com.example.backend.dto.response;

import com.example.backend.entity.Staff;
import com.example.backend.entity.StaffStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StaffResponse {

    private Long   id;
    private String fullName;
    private String role;
    private String phone;
    private String email;
    private String shift;

    /** Enum value: WORKING | ON_LEAVE | INACTIVE */
    private String status;

    /** Nhãn tiếng Việt */
    private String statusLabel;

    private String note;
    private String avatar;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ── Factory method ── */
    public static StaffResponse from(Staff s) {
        return StaffResponse.builder()
                .id(s.getId())
                .fullName(s.getFullName())
                .role(s.getRole())
                .phone(s.getPhone())
                .email(s.getEmail())
                .shift(s.getShift())
                .status(s.getStatus().name())
                .statusLabel(statusLabel(s.getStatus()))
                .note(s.getNote())
                .avatar(s.getAvatar())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    /* ── Helper: enum → nhãn tiếng Việt ── */
    private static String statusLabel(StaffStatus status) {
        return switch (status) {
            case WORKING  -> "Đang làm việc";
            case ON_LEAVE -> "Đang nghỉ phép";
            case INACTIVE -> "Ngừng làm việc";
        };
    }
}
