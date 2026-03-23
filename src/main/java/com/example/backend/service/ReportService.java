package com.example.backend.service;

import com.example.backend.dto.response.ReportResponse;

import java.time.LocalDate;

public interface ReportService {
    
    /**
     * Lấy báo cáo doanh thu và công nợ chi tiết
     * @param startDate Từ ngày (optional)
     * @param endDate Đến ngày (optional)
     * @param source Nguồn đặt phòng (optional)
     * @param paymentStatus Trạng thái thanh toán (optional)
     * @param page Trang hiện tại
     * @param size Số item mỗi trang
     * @return ReportResponse
     */
    ReportResponse getRevenueReport(
            LocalDate startDate,
            LocalDate endDate,
            String source,
            String paymentStatus,
            int page,
            int size
    );
}
