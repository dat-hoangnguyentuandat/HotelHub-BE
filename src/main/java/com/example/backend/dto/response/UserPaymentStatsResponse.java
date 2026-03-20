package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Thống kê thanh toán cá nhân – dùng cho 4 thẻ stat trên trang payment-info.
 *
 * <ul>
 *   <li>{@code totalCount}        – Tổng số giao dịch</li>
 *   <li>{@code successCount}      – Số giao dịch thành công</li>
 *   <li>{@code pendingCount}      – Số giao dịch đang chờ / đang xử lý</li>
 *   <li>{@code failedCount}       – Số giao dịch thất bại</li>
 *   <li>{@code cancelledCount}    – Số giao dịch đã hủy / hoàn tiền</li>
 *   <li>{@code totalSpend}        – Tổng tiền đã chi (chỉ tính SUCCESS)</li>
 *   <li>{@code totalPointsEarned} – Tổng điểm thưởng đã tích lũy</li>
 * </ul>
 */
@Getter
@Builder
public class UserPaymentStatsResponse {

    /** Tổng số giao dịch */
    private long totalCount;

    /** Số giao dịch thành công */
    private long successCount;

    /** Số giao dịch đang chờ / đang xử lý */
    private long pendingCount;

    /** Số giao dịch thất bại */
    private long failedCount;

    /** Số giao dịch đã hủy + hoàn tiền */
    private long cancelledCount;

    /** Tổng số tiền đã thanh toán thành công */
    private BigDecimal totalSpend;

    /** Tổng điểm thưởng tích lũy từ tất cả giao dịch SUCCESS */
    private long totalPointsEarned;
}
