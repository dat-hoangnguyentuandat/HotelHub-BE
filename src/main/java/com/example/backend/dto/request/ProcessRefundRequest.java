package com.example.backend.dto.request;

import com.example.backend.entity.RefundStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body khi admin xử lý (approve / reject) một yêu cầu hoàn tiền.
 */
@Getter
@Setter
public class ProcessRefundRequest {

    /**
     * Trạng thái mới: REFUNDED hoặc REJECTED.
     * Không cho phép set PENDING_REFUND qua API này.
     */
    @NotNull(message = "Trạng thái hoàn tiền không được để trống")
    private RefundStatus status;

    /** Ghi chú của admin (bắt buộc khi REJECTED) */
    private String note;
}
