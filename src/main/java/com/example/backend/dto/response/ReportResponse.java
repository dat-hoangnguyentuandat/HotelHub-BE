package com.example.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportResponse {
    
    private ReportSummary summary;
    private List<ReportItem> items;
    private int totalItems;
    private int currentPage;
    private int totalPages;
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReportSummary {
        private BigDecimal totalRevenue;        // Tổng doanh thu
        private BigDecimal actualRevenue;       // Doanh thu thực tế (đã trừ giảm giá)
        private BigDecimal totalExpenses;       // Tổng công nợ
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ReportItem {
        private String bookingCode;       // Mã hóa đơn
        private LocalDate bookingDate;    // Ngày tạo
        private String guestName;         // Tên khách hàng
        private String roomNumber;        // Phòng
        private String source;            // Nguồn (OTA, Trực tiếp, Nhóm)
        private BigDecimal totalAmount;         // Tổng tiền (trước giảm giá)
        private BigDecimal discountAmount;      // Dịch vụ (giảm giá)
        private BigDecimal finalAmount;         // Còn lại (sau giảm giá)
        private String paymentStatus;     // Trạng thái thanh toán
        private String createdBy;         // Người tạo
        private String action;            // Xem chi tiết
    }
}
