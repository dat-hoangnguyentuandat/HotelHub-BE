package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Thống kê tổng hợp về khách hàng cho trang admin.
 */
@Getter
@Builder
public class CustomerStatsResponse {

    private long totalUsers;      // Tổng số user
    private long totalGuests;     // Role = GUEST
    private long totalOwners;     // Role = HOTEL_OWNER
    private long totalAdmins;     // Role = ADMIN
    private long newThisMonth;    // Đăng ký trong tháng hiện tại
}
