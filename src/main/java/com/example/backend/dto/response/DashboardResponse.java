package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response tổng hợp cho Dashboard Admin.
 * Bao gồm: tổng quan KPI, biểu đồ doanh thu, giao dịch gần nhất.
 */
@Getter
@Builder
public class DashboardResponse {

    /** Các chỉ số tổng quan (4 thẻ stat) */
    private StatsOverview stats;

    /** Doanh thu hàng tháng (biểu đồ cột trái) */
    private MonthlyRevenueChart monthlyRevenue;

    /** Doanh thu theo loại phòng (biểu đồ cột phải) */
    private RoomTypeRevenueChart roomTypeRevenue;

    /** Danh sách giao dịch gần nhất */
    private List<RecentTransaction> recentTransactions;

    /* ═══════════════════════════════════════
       NESTED DTOs
    ═══════════════════════════════════════ */

    /** 4 thẻ KPI trên đầu dashboard */
    @Getter
    @Builder
    public static class StatsOverview {
        /** Tỷ lệ lấp đầy (%) – số phòng đang ở / tổng phòng × 100 */
        private double occupancyRate;

        /** Chênh lệch occupancyRate so với tháng trước (điểm %) */
        private double occupancyRateChange;

        /**
         * RevPAR – Revenue Per Available Room (VND)
         * = tổng doanh thu tháng này / tổng số phòng
         */
        private BigDecimal revPar;

        /** Chênh lệch revPar so với tháng trước (%) */
        private double revParChange;

        /** Tổng lượt khách check-in tháng này */
        private long totalCheckIns;

        /** Chênh lệch totalCheckIns so với tháng trước */
        private long totalCheckInsChange;

        /** Số đặt phòng đang chờ xác nhận (PENDING) */
        private long pendingBookings;

        /** Chênh lệch pendingBookings so với tháng trước */
        private long pendingBookingsChange;
    }

    /** Dữ liệu cho biểu đồ doanh thu hàng tháng (6 tháng gần nhất) */
    @Getter
    @Builder
    public static class MonthlyRevenueChart {
        /** Tiêu đề tháng, vd: ["Tháng 10", "Tháng 11", ...] */
        private List<String> labels;

        /** Doanh thu mỗi tháng tương ứng (VND) */
        private List<BigDecimal> data;

        /** Tổng doanh thu tháng hiện tại (hiển thị trên header card) */
        private BigDecimal currentMonthTotal;
    }

    /** Dữ liệu cho biểu đồ doanh thu theo loại phòng (tháng hiện tại) */
    @Getter
    @Builder
    public static class RoomTypeRevenueChart {
        /** Loại phòng, vd: ["Phòng Đơn", "Phòng Đôi", "Phòng Gia Đình"] */
        private List<String> labels;

        /** Doanh thu mỗi loại phòng tương ứng (VND) */
        private List<BigDecimal> data;

        /** Tổng doanh thu tháng này của tất cả loại phòng */
        private BigDecimal total;

        /** Nhãn tháng, vd: "Tháng 3" */
        private String monthLabel;
    }

    /** Một dòng trong bảng "Giao dịch gần nhất" */
    @Getter
    @Builder
    public static class RecentTransaction {
        private Long   id;
        private String transactionCode; // "TXN" + zero-padded id
        private String guestName;
        private String roomType;
        private String checkIn;         // ISO date string yyyy-MM-dd
        private String checkOut;
        private BigDecimal totalAmount;
        private String status;
        private String statusLabel;
    }
}
