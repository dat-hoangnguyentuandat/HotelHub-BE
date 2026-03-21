package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Thống kê tổng hợp điểm đánh giá – dùng cho trang admin-reviews.
 */
@Getter
@Builder
public class ReviewStatsResponse {

    /* ── Tổng quan ── */
    private long   totalReviews;
    private long   pendingCount;
    private long   approvedCount;
    private long   rejectedCount;
    private long   withReplyCount;
    private double responseRate;     // % review đã được phản hồi

    /* ── Điểm tổng thể ── */
    private double overallAvg;

    /* ── Phân phối theo số sao (0.0 – 100.0 %) ── */
    private double pct5Star;
    private double pct4Star;
    private double pct3Star;
    private double pct2Star;
    private double pct1Star;

    /* ── Điểm trung bình từng tiêu chí ── */
    private double avgRoom;
    private double avgService;
    private double avgLocation;
    private double avgCleanliness;
    private double avgAmenities;
    private double avgValue;
}
