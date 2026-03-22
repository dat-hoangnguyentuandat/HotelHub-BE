package com.example.backend.entity;

public enum GroupBookingStatus {
    PENDING,      // Chờ xác nhận
    CONFIRMED,    // Đã xác nhận
    CHECKED_IN,   // Đã check-in
    COMPLETED,    // Hoàn thành
    CANCELLED     // Đã hủy
}
