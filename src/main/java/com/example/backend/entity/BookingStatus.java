package com.example.backend.entity;

public enum BookingStatus {
    PENDING,      // Chờ xác nhận
    CONFIRMED,    // Đã xác nhận
    CHECKED_IN,   // Đã nhận phòng
    CHECKED_OUT,  // Đã trả phòng
    CANCELLED     // Đã huỷ
}
