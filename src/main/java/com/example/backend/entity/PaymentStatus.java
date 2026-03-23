package com.example.backend.entity;

/**
 * Trạng thái giao dịch thanh toán.
 */
public enum PaymentStatus {

    /** Vừa khởi tạo, chưa xử lý */
    PENDING,

    /** Đang xử lý tại cổng thanh toán */
    PROCESSING,

    /** Thanh toán thành công */
    SUCCESS,

    /** Thanh toán thất bại */
    FAILED,

    /** Đã hoàn tiền */
    REFUNDED,

    /** Đã hủy trước khi xử lý */
    CANCELLED
}
