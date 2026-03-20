package com.example.backend.controller;

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
import com.example.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * PaymentController – REST API chức năng thanh toán trực tuyến.
 *
 * <p><b>Luồng thanh toán:</b></p>
 * <ol>
 *   <li>Client gọi {@code POST /api/payments/initiate} → nhận {@code paymentId} + {@code transactionRef}</li>
 *   <li>Client hiển thị UI thanh toán, người dùng xác nhận</li>
 *   <li>Client gọi {@code POST /api/payments/{id}/process} → nhận kết quả SUCCESS / FAILED</li>
 * </ol>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /* ══════════════════════════════════════════════════════
       CUSTOMER ENDPOINTS
    ══════════════════════════════════════════════════════ */

    /**
     * Bước 1: Khởi tạo phiên thanh toán.
     * Tạo bản ghi Payment PENDING, trả về transactionRef và thông tin tính tiền.
     *
     * <p>Cho phép cả guest (chưa đăng nhập) và user đã đăng nhập.</p>
     */
    @PostMapping("/payments/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : null;
        log.info("[API] POST /payments/initiate bookingId={} method={} user={}",
            request.getBookingId(), request.getMethod(), email);

        PaymentResponse response = paymentService.initiatePayment(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Bước 2: Xử lý / xác nhận thanh toán.
     * Gọi sau khi người dùng nhấn "Xác nhận thanh toán" trên UI.
     * Kết quả trả về: SUCCESS hoặc FAILED (throw 402).
     */
    @PostMapping("/payments/{paymentId}/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : null;
        log.info("[API] POST /payments/{}/process user={}", paymentId, email);

        PaymentResponse response = paymentService.processPayment(paymentId, email);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy chi tiết một giao dịch thanh toán.
     */
    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(paymentService.getPayment(paymentId, email));
    }

    /**
     * Lấy lịch sử thanh toán của một booking.
     */
    @GetMapping("/bookings/{bookingId}/payments")
    public ResponseEntity<PagedResponse<PaymentResponse>> getPaymentsByBooking(
            @PathVariable Long bookingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(
            paymentService.getPaymentsByBooking(bookingId, email, page, size));
    }

    /**
     * Lịch sử thanh toán của người dùng đang đăng nhập.
     */
    @GetMapping("/payments/my")
    public ResponseEntity<PagedResponse<PaymentResponse>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
            paymentService.getMyPayments(userDetails.getUsername(), page, size));
    }

    /* ══════════════════════════════════════════════════════
       PAYMENT-INFO  –  Thông tin thanh toán (lịch sử + thống kê cá nhân)
    ══════════════════════════════════════════════════════ */

    /**
     * Lịch sử thanh toán đầy đủ (kèm thông tin booking) – dành cho trang payment-info.
     *
     * <p>Hỗ trợ filter: status, method, keyword, from, to với phân trang.
     * Chỉ trả về giao dịch thuộc về người dùng hiện tại.</p>
     */
    @GetMapping("/payments/my/info")
    public ResponseEntity<PagedResponse<PaymentInfoResponse>> getMyPaymentsInfo(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8")  int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("[API] GET /payments/my/info user={} status={} method={} keyword={}",
            userDetails.getUsername(), status, method, keyword);

        return ResponseEntity.ok(
            paymentService.getMyPaymentsInfo(
                userDetails.getUsername(),
                status, method, keyword, from, to,
                page, size));
    }

    /**
     * Thống kê thanh toán cá nhân (4 stat-card trên trang payment-info).
     *
     * <p>Trả về: tổng GD, tổng tiêu, đang chờ, điểm tích lũy.</p>
     */
    @GetMapping("/payments/my/stats")
    public ResponseEntity<UserPaymentStatsResponse> getMyPaymentStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("[API] GET /payments/my/stats user={}", userDetails.getUsername());
        return ResponseEntity.ok(paymentService.getMyPaymentStats(userDetails.getUsername()));
    }

    /**
     * Chi tiết một giao dịch với đầy đủ thông tin booking – dành cho panel chi tiết payment-info.
     */
    @GetMapping("/payments/{paymentId}/info")
    public ResponseEntity<PaymentInfoResponse> getPaymentInfo(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : null;
        log.info("[API] GET /payments/{}/info user={}", paymentId, email);
        return ResponseEntity.ok(paymentService.getPaymentInfoById(paymentId, email));
    }

    /**
     * Hủy phiên thanh toán PENDING (người dùng tự hủy hoặc hết hạn).
     */
    @PatchMapping("/payments/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails != null ? userDetails.getUsername() : null;
        return ResponseEntity.ok(paymentService.cancelPayment(paymentId, email));
    }

    /* ══════════════════════════════════════════════════════
       PROMO CODE
    ══════════════════════════════════════════════════════ */

    /**
     * Xác thực mã khuyến mãi (public – không cần đăng nhập).
     * Frontend gọi khi người dùng nhấn "Áp dụng" mã promo.
     */
    @PostMapping("/payments/promo/validate")
    public ResponseEntity<PromoValidateResponse> validatePromo(
            @Valid @RequestBody PromoValidateRequest request) {

        return ResponseEntity.ok(paymentService.validatePromo(request));
    }

    /**
     * Tiện ích: lấy danh sách promo hợp lệ (chỉ để demo, thực tế không public).
     */
    @GetMapping("/payments/promo/demo-codes")
    public ResponseEntity<Map<String, String>> getDemoCodes() {
        return ResponseEntity.ok(Map.of(
            "HOTEL10",  "Giảm 10%",
            "SUMMER20", "Giảm 20% Hè 2026",
            "NEWGUEST", "Khách mới giảm 15%",
            "VIP30",    "VIP giảm 30%"
        ));
    }

    /* ══════════════════════════════════════════════════════
       ADMIN ENDPOINTS
    ══════════════════════════════════════════════════════ */

    /**
     * Admin: tìm kiếm & lọc tất cả giao dịch.
     */
    @GetMapping("/admin/payments")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_OWNER')")
    public ResponseEntity<PagedResponse<PaymentResponse>> adminSearchPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
            paymentService.adminSearchPayments(status, method, keyword, from, to, page, size));
    }

    /**
     * Admin: xem chi tiết một giao dịch.
     */
    @GetMapping("/admin/payments/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_OWNER')")
    public ResponseEntity<PaymentResponse> adminGetPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.adminGetPayment(paymentId));
    }

    /**
     * Admin: thống kê thanh toán (dashboard).
     */
    @GetMapping("/admin/payments/stats")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_OWNER')")
    public ResponseEntity<PaymentStatsResponse> adminGetStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(paymentService.adminGetStats(from, to));
    }

    /**
     * Admin: hoàn tiền giao dịch đã SUCCESS.
     */
    @PostMapping("/admin/payments/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> adminRefundPayment(
            @PathVariable Long paymentId,
            @RequestBody(required = false) Map<String, String> body) {

        String reason = (body != null) ? body.getOrDefault("reason", "Admin hoàn tiền") : "Admin hoàn tiền";
        log.info("[API] POST /admin/payments/{}/refund reason={}", paymentId, reason);
        return ResponseEntity.ok(paymentService.adminRefundPayment(paymentId, reason));
    }
}
