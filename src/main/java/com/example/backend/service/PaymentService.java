package com.example.backend.service;

import com.example.backend.dto.request.PaymentRequest;
import com.example.backend.dto.request.PromoValidateRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.PaymentInfoResponse;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.dto.response.PaymentStatsResponse;
import com.example.backend.dto.response.PromoValidateResponse;
import com.example.backend.dto.response.UserPaymentStatsResponse;
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
     * Lấy lịch sử thanh toán của người dùng với đầy đủ booking details,
     * hỗ trợ filter nhiều tiêu chí – dành riêng cho trang payment-info.
     *
     * @param userEmail  email người dùng hiện tại
     * @param status     lọc theo trạng thái (null = tất cả)
     * @param method     lọc theo phương thức (null = tất cả)
     * @param keyword    tìm theo mã GD / loại phòng (null = bỏ qua)
     * @param from       từ ngày (null = không giới hạn)
     * @param to         đến ngày (null = không giới hạn)
     * @param page       số trang (0-based)
     * @param size       kích thước trang
     */
    PagedResponse<PaymentInfoResponse> getMyPaymentsInfo(
        String userEmail,
        PaymentStatus status,
        PaymentMethod method,
        String keyword,
        LocalDate from,
        LocalDate to,
        int page,
        int size
    );

    /**
     * Thống kê thanh toán cá nhân – cho 4 stat-card trên trang payment-info.
     *
     * @param userEmail email người dùng hiện tại
     */
    UserPaymentStatsResponse getMyPaymentStats(String userEmail);

    /**
     * Lấy chi tiết giao dịch kèm thông tin booking đầy đủ – dùng cho panel chi tiết payment-info.
     *
     * @param paymentId ID giao dịch
     * @param userEmail email người dùng (kiểm tra quyền)
     */
    PaymentInfoResponse getPaymentInfoById(Long paymentId, String userEmail);

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
