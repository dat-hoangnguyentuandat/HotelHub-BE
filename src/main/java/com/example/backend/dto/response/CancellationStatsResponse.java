package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Thống kê tổng quan cho trang Hủy phòng & Hoàn tiền.
 */
@Getter
@Builder
public class CancellationStatsResponse {

    /** Tổng số yêu cầu hủy phòng */
    private long total;

    /** Số yêu cầu đang chờ xử lý */
    private long pending;

    /** Số yêu cầu đã hoàn tiền */
    private long refunded;

    /** Số yêu cầu bị từ chối */
    private long rejected;

    /** Tổng số tiền đã hoàn (chỉ tính REFUNDED) */
    private BigDecimal totalRefundAmount;
}
