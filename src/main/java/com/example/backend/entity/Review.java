package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Đánh giá & nhận xét của khách hàng sau khi check-out.
 * <p>
 * Một booking chỉ có tối đa 1 review (unique constraint trên booking_id).
 * Khách vãng lai (không có tài khoản) vẫn được gửi review thông qua booking.
 */
@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_review_booking",
        columnNames = "booking_id"
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ── Liên kết booking (bắt buộc, 1 booking – 1 review) ── */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    /* ── Liên kết user (nullable – khách vãng lai không có account) ── */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /* ── Điểm tổng thể (1-5) ── */
    @Column(nullable = false)
    private Integer rating;

    /* ── Tiêu đề nhận xét ── */
    @Column(length = 255)
    private String title;

    /* ── Nội dung nhận xét ── */
    @Column(columnDefinition = "TEXT")
    private String comment;

    /* ── Điểm chi tiết theo từng tiêu chí (1-5) ── */
    private Integer roomRating;         // Phòng ở
    private Integer serviceRating;      // Dịch vụ
    private Integer locationRating;     // Vị trí
    private Integer cleanlinessRating;  // Sạch sẽ
    private Integer amenitiesRating;    // Tiện nghi
    private Integer valueRating;        // Giá trị

    /* ── Phản hồi của khách sạn ── */
    @Column(columnDefinition = "TEXT")
    private String replyText;

    private LocalDateTime repliedAt;

    /* ── Trạng thái kiểm duyệt ── */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

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
