package com.example.backend.dto.response;

import com.example.backend.entity.Review;
import com.example.backend.entity.ReviewStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse {

    private Long          id;

    /* ── Booking info ── */
    private Long          bookingId;
    private String        guestName;
    private String        guestEmail;
    private String        roomType;

    /* ── User info (nullable) ── */
    private Long          userId;
    private String        userFullName;

    /* ── Nội dung review ── */
    private Integer       rating;
    private String        title;
    private String        comment;

    /* ── Điểm chi tiết ── */
    private Integer       roomRating;
    private Integer       serviceRating;
    private Integer       locationRating;
    private Integer       cleanlinessRating;
    private Integer       amenitiesRating;
    private Integer       valueRating;

    /* ── Phản hồi của khách sạn ── */
    private String        replyText;
    private LocalDateTime repliedAt;
    private boolean       hasReply;

    /* ── Trạng thái ── */
    private ReviewStatus  status;
    private String        statusLabel;

    /* ── Timestamps ── */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* ────────────────────────────────────────────────── */
    public static ReviewResponse from(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .bookingId(r.getBooking().getId())
                .guestName(r.getBooking().getGuestName())
                .guestEmail(r.getBooking().getGuestEmail())
                .roomType(r.getBooking().getRoomType())
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .userFullName(r.getUser() != null ? r.getUser().getFullName() : null)
                .rating(r.getRating())
                .title(r.getTitle())
                .comment(r.getComment())
                .roomRating(r.getRoomRating())
                .serviceRating(r.getServiceRating())
                .locationRating(r.getLocationRating())
                .cleanlinessRating(r.getCleanlinessRating())
                .amenitiesRating(r.getAmenitiesRating())
                .valueRating(r.getValueRating())
                .replyText(r.getReplyText())
                .repliedAt(r.getRepliedAt())
                .hasReply(r.getReplyText() != null && !r.getReplyText().isBlank())
                .status(r.getStatus())
                .statusLabel(statusLabel(r.getStatus()))
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }

    private static String statusLabel(ReviewStatus s) {
        return switch (s) {
            case PENDING  -> "Chờ duyệt";
            case APPROVED -> "Đã duyệt";
            case REJECTED -> "Đã từ chối";
        };
    }
}
