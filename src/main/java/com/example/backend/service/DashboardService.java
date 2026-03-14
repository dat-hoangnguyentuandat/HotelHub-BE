package com.example.backend.service;

import com.example.backend.dto.response.DashboardResponse;

public interface DashboardService {

    /**
     * Tổng hợp toàn bộ dữ liệu cần thiết cho trang Dashboard admin:
     * - 4 thẻ KPI (tổng quan)
     * - Biểu đồ doanh thu hàng tháng (6 tháng gần nhất)
     * - Biểu đồ doanh thu theo loại phòng (tháng hiện tại)
     * - 5 giao dịch gần nhất
     */
    DashboardResponse getDashboardData();
}
