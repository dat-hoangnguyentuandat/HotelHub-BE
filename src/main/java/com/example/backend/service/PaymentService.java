package com.example.backend.service;

import com.example.backend.dto.request.PaymentRequest;
import com.example.backend.dto.request.PromoValidateRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.dto.response.PaymentStatsResponse;
import com.example.backend.dto.response.PromoValidateResponse;
import com.example.backend.entity.PaymentMethod;
import com.example.backend.entity.PaymentStatus;

import java.time.LocalDate;

public interface PaymentService {

    /**
     * Khởi tạo phiên thanh toán mới cho một booking.
     * Tạo bản ghi Payment với trạng thái PENDING và trả về thông tin
     * cùng mã giao dịch để frontend hiển thị.
     *
     * @param request   thông tin thanh toán từ client
     * @param userEmail email người dùng (null nếu guest/walk-in)
     */
    PaymentResponse initiatePayment(PaymentRequest request, String userEmail);

    /**
     * Xác nhận và xử lý thanh toán (mô phỏng gateway).
     * Trong thực tế, đây là nơi gọi API Stripe / VNPay / MoMo ...
     *
     * @param paymentId ID của Payment đã tạo ở bước initiate
     * @param userEmail email người dùng
     */
    PaymentResponse processPayment(Long paymentId, String userEmail);

    /**
     * Lấy thông tin một giao dịch thanh toán (người dùng hoặc admin).
     */
    PaymentResponse getPayment(Long paymentId, String userEmail);

    /**
     * Lấy lịch sử thanh toán của booking.
     */
    PagedResponse<PaymentResponse> getPaymentsByBooking(Long bookingId, String userEmail, int page, int size);

    /**
     * Lấy lịch sử thanh toán của người dùng hiện tại.
     */
    PagedResponse<PaymentResponse> getMyPayments(String userEmail, int page, int size);

    /**
     * Hủy phiên thanh toán đang PENDING (hết hạn hoặc người dùng tự hủy).
     */
    PaymentResponse cancelPayment(Long paymentId, String userEmail);

    /* ── Promo ── */

    /**
     * Xác thực mã khuyến mãi và trả về phần trăm giảm giá.
     */
    PromoValidateResponse validatePromo(PromoValidateRequest request);

    /* ── Admin ── */

    /**
     * Admin: tìm kiếm / lọc tất cả giao dịch.
     */
    PagedResponse<PaymentResponse> adminSearchPayments(
        PaymentStatus status,
        PaymentMethod method,
        String keyword,
        LocalDate from,
        LocalDate to,
        int page,
        int size
    );

    /**
     * Admin: xem chi tiết một giao dịch.
     */
    PaymentResponse adminGetPayment(Long paymentId);

    /**
     * Admin: thống kê thanh toán theo khoảng thời gian.
     */
    PaymentStatsResponse adminGetStats(LocalDate from, LocalDate to);

    /**
     * Admin: hoàn tiền thủ công cho giao dịch đã SUCCESS.
     */
    PaymentResponse adminRefundPayment(Long paymentId, String reason);

    /**
     * Dọn dẹp các phiên PENDING đã hết hạn (chạy scheduled).
     */
    void expireStalePayments();
}
