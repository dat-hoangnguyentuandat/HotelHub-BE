package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Thống kê thanh toán cho Admin Dashboard.
 */
@Getter
@Builder
public class PaymentStatsResponse {

    /* ── Tổng quan ── */
    private long totalPayments;
    private long successPayments;
    private long failedPayments;
    private long pendingPayments;
    private BigDecimal totalRevenue;

    /* ── Theo phương thức ── */
    private Map<String, BigDecimal> revenueByMethod;
    private Map<String, Long>       countByMethod;

    /* ── Tỉ lệ thành công ── */
    private double successRate;

    /* ── Giao dịch gần đây ── */
    private List<PaymentResponse> recentPayments;
}
