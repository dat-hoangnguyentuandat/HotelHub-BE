package com.example.backend.dto.response;

import com.example.backend.entity.SpecialRequest;
import com.example.backend.entity.SpecialRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SpecialRequestResponse {

    private Long   id;
    private String guestName;
    private String guestPhone;
    private String requestType;
    private String content;

    /** Enum value: PENDING | APPROVED | REJECTED | DONE */
    private String status;

    /** Nhãn tiếng Việt */
    private String statusLabel;

    private String adminNote;
    private Long   bookingId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ── Factory method ── */
    public static SpecialRequestResponse from(SpecialRequest sr) {
        return SpecialRequestResponse.builder()
                .id(sr.getId())
                .guestName(sr.getGuestName())
                .guestPhone(sr.getGuestPhone())
                .requestType(sr.getRequestType())
                .content(sr.getContent())
                .status(sr.getStatus().name())
                .statusLabel(statusLabel(sr.getStatus()))
                .adminNote(sr.getAdminNote())
                .bookingId(sr.getBooking() != null ? sr.getBooking().getId() : null)
                .createdAt(sr.getCreatedAt())
                .updatedAt(sr.getUpdatedAt())
                .build();
    }

    /* ── Helper: enum → nhãn tiếng Việt ── */
    private static String statusLabel(SpecialRequestStatus status) {
        return switch (status) {
            case PENDING  -> "Chờ xử lý";
            case APPROVED -> "Đã duyệt";
            case REJECTED -> "Từ chối";
            case DONE     -> "Đã xử lý";
        };
    }
}
