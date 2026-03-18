package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "special_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ── Thông tin khách ── */
    @Column(nullable = false, length = 150)
    private String guestName;

    @Column(length = 20)
    private String guestPhone;

    /* ── Loại yêu cầu ── */
    @Column(nullable = false, length = 100)
    private String requestType;

    /* ── Nội dung ── */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /* ── Trạng thái ── */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SpecialRequestStatus status = SpecialRequestStatus.PENDING;

    /* ── Ghi chú xử lý ── */
    @Column(columnDefinition = "TEXT")
    private String adminNote;

    /* ── Liên kết booking (tuỳ chọn) ── */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    /* ── Timestamps ── */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
