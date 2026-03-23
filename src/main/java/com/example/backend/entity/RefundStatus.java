package com.example.backend.entity;

/**
 * Trạng thái hoàn tiền của một yêu cầu hủy phòng.
 */
public enum RefundStatus {

    /** Đang chờ admin xét duyệt */
    PENDING_REFUND,

    /** Đã hoàn tiền thành công */
    REFUNDED,

    /** Từ chối hoàn tiền */
    REJECTED
}
