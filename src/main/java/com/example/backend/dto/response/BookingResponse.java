package com.example.backend.dto.response;

import com.example.backend.entity.Booking;
import com.example.backend.entity.BookingStatus;
import com.example.backend.entity.RefundStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class BookingResponse {

    private Long            id;
    private String          guestName;
    private String          guestPhone;
    private String          guestEmail;
    private String          roomType;
    private BigDecimal      pricePerNight;
    private LocalDate       checkIn;
    private LocalDate       checkOut;
    private int             nights;
    private int             rooms;
    private int             adults;
    private int             children;
    private BigDecimal      totalAmount;
    private BookingStatus   status;
    private String          statusLabel;
    private String          note;
    private Long            userId;
    private String          userFullName;

    /* ── Thông tin hủy & hoàn tiền ── */
    private String          cancelReason;
    private LocalDateTime   cancelledAt;
    private BigDecimal      refundRate;
    private BigDecimal      refundAmount;
    private RefundStatus    refundStatus;
    private String          refundStatusLabel;
    private String          processNote;
    private String          appliedPolicy;

    private LocalDateTime   createdAt;
    private LocalDateTime   updatedAt;

    public static BookingResponse from(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .guestName(b.getGuestName())
                .guestPhone(b.getGuestPhone())
                .guestEmail(b.getGuestEmail())
                .roomType(b.getRoomType())
                .pricePerNight(b.getPricePerNight())
                .checkIn(b.getCheckIn())
                .checkOut(b.getCheckOut())
                .nights(b.getNights())
                .rooms(b.getRooms())
                .adults(b.getAdults())
                .children(b.getChildren())
                .totalAmount(b.getTotalAmount())
                .status(b.getStatus())
                .statusLabel(statusLabel(b.getStatus()))
                .note(b.getNote())
                .userId(b.getUser() != null ? b.getUser().getId() : null)
                .userFullName(b.getUser() != null ? b.getUser().getFullName() : null)
                .cancelReason(b.getCancelReason())
                .cancelledAt(b.getCancelledAt())
                .refundRate(b.getRefundRate())
                .refundAmount(b.getRefundAmount())
                .refundStatus(b.getRefundStatus())
                .refundStatusLabel(refundLabel(b.getRefundStatus()))
                .processNote(b.getProcessNote())
                .appliedPolicy(b.getAppliedPolicy())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }

    private static String statusLabel(BookingStatus s) {
        return switch (s) {
            case PENDING     -> "Chờ xác nhận";
            case CONFIRMED   -> "Đã xác nhận";
            case CHECKED_IN  -> "Đã nhận phòng";
            case CHECKED_OUT -> "Đã trả phòng";
            case CANCELLED   -> "Đã huỷ";
        };
    }

    private static String refundLabel(RefundStatus s) {
        if (s == null) return null;
        return switch (s) {
            case PENDING_REFUND -> "Chờ xử lý";
            case REFUNDED       -> "Đã hoàn tiền";
            case REJECTED       -> "Từ chối hoàn";
        };
    }
}
