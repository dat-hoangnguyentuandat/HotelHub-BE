package com.example.backend.dto.response;

import com.example.backend.entity.Booking;
import com.example.backend.entity.BookingStatus;
import com.example.backend.entity.RefundStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO cho một yêu cầu hủy phòng & hoàn tiền.
 * Map từ Booking có status = CANCELLED.
 */
@Getter
@Builder
public class CancellationResponse {

    private Long          id;

    /* ── Booking info ── */
    private String        bookingCode;   // "DP" + padded id
    private String        guestName;
    private String        guestPhone;
    private String        guestEmail;
    private String        roomType;
    private LocalDate     checkIn;
    private LocalDate     checkOut;
    private int           nights;
    private BigDecimal    totalAmount;
    private String        note;

    /* ── Cancel info ── */
    private LocalDateTime bookingDate;   // createdAt
    private LocalDateTime cancelDate;    // cancelledAt
    private String        reason;        // cancelReason

    /* ── Refund info ── */
    private String        policy;        // appliedPolicy
    private BigDecimal    refundRate;    // 0-100
    private BigDecimal    refundAmount;
    private RefundStatus  refundStatus;
    private String        refundStatusLabel;
    private String        processNote;

    /* ── Booking status ── */
    private BookingStatus bookingStatus;

    /* ── User info ── */
    private Long          userId;
    private String        userFullName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Factory method ──────────────────────────────────────────
    public static CancellationResponse from(Booking b) {
        return CancellationResponse.builder()
                .id(b.getId())
                .bookingCode("DP" + String.format("%05d", b.getId()))
                .guestName(b.getGuestName())
                .guestPhone(b.getGuestPhone())
                .guestEmail(b.getGuestEmail())
                .roomType(b.getRoomType())
                .checkIn(b.getCheckIn())
                .checkOut(b.getCheckOut())
                .nights(b.getNights())
                .totalAmount(b.getTotalAmount())
                .note(b.getNote())
                .bookingDate(b.getCreatedAt())
                .cancelDate(b.getCancelledAt())
                .reason(b.getCancelReason())
                .policy(b.getAppliedPolicy())
                .refundRate(b.getRefundRate())
                .refundAmount(b.getRefundAmount())
                .refundStatus(b.getRefundStatus())
                .refundStatusLabel(refundLabel(b.getRefundStatus()))
                .processNote(b.getProcessNote())
                .bookingStatus(b.getStatus())
                .userId(b.getUser() != null ? b.getUser().getId() : null)
                .userFullName(b.getUser() != null ? b.getUser().getFullName() : null)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    private static String refundLabel(RefundStatus s) {
        if (s == null) return "Chờ xử lý";
        return switch (s) {
            case PENDING_REFUND -> "Chờ xử lý";
            case REFUNDED       -> "Đã hoàn tiền";
            case REJECTED       -> "Từ chối hoàn";
        };
    }
}
