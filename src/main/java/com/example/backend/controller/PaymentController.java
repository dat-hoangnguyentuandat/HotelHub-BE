package com.example.backend.controller;

import com.example.backend.dto.request.PaymentRequest;
import com.example.backend.dto.request.PromoValidateRequest;
import com.example.backend.dto.response.BookingResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.PaymentInfoResponse;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.dto.response.PaymentStatsResponse;
import com.example.backend.dto.response.PromoValidateResponse;
import com.example.backend.dto.response.UserPaymentStatsResponse;
import com.example.backend.dto.response.UserVoucherResponse;
import com.example.backend.dto.response.VoucherResponse;
import com.example.backend.entity.CancellationPolicy;
import com.example.backend.entity.PaymentMethod;
import com.example.backend.entity.PaymentStatus;
import com.example.backend.entity.PromoCode;
import com.example.backend.entity.User;
import com.example.backend.entity.UserVoucher;
import com.example.backend.entity.UserVoucherStatus;
import com.example.backend.entity.Voucher;
import com.example.backend.repository.PromoCodeRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserVoucherRepository;
import com.example.backend.repository.VoucherRepository;
import com.example.backend.service.BookingService;
import com.example.backend.service.CancellationService;
import com.example.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
    private final PromoCodeRepository promoCodeRepository;
    private final BookingService bookingService;
    private final CancellationService cancellationService;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;

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
     * Admin: lấy danh sách mã promo đang hoạt động từ DB.
     * Endpoint được bảo vệ bằng quyền ADMIN/HOTEL_OWNER.
     */
    @GetMapping("/admin/payments/promo-codes")
    @PreAuthorize("hasAnyRole('ADMIN','HOTEL_OWNER')")
    public ResponseEntity<List<Map<String, Object>>> getPromoCodes() {
        List<Map<String, Object>> codes = promoCodeRepository.findByActiveTrueOrderByCreatedAtDesc()
            .stream()
            .map(p -> Map.<String, Object>of(
                "code",         p.getCode(),
                "label",        p.getLabel(),
                "discountRate", p.getDiscountRate()
            ))
            .toList();
        return ResponseEntity.ok(codes);
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

    /**
     * User: yêu cầu hoàn tiền.
     * Body: { "reason": "...", "refundType": "CASH" | "VOUCHER" }
     * - CASH (mặc định): hủy booking, tạo cancellation request xử lý sau.
     * - VOUCHER: cấp voucher tương ứng ngay lập tức, đánh dấu payment REFUNDED.
     */
    @PostMapping("/payments/{paymentId}/refund")
    public ResponseEntity<?> requestRefund(
            @PathVariable Long paymentId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {

        String reason     = (body != null) ? body.getOrDefault("reason",     "Yêu cầu hoàn tiền") : "Yêu cầu hoàn tiền";
        String refundType = (body != null) ? body.getOrDefault("refundType", "CASH").toUpperCase() : "CASH";
        String username   = authentication.getName();

        if ("VOUCHER".equals(refundType)) {
            // ── Hoàn tiền bằng voucher ──────────────────────────────────────
            // 1. Lấy thông tin payment & tính refundAmount
            PaymentResponse payment = paymentService.getPayment(paymentId, username);
            BookingResponse booking = bookingService.getBookingById(payment.getBookingId());
            long refundAmount = calculateApplicableRefundAmount(booking, payment);

            // 2. Tìm voucher phù hợp
            List<Voucher> suggested = suggestVouchersForAmount(refundAmount);
            
            if (suggested.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Không tìm thấy voucher phù hợp để đổi"));
            }

            // 3. Lấy User entity
            User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + username));

            // 4. Cấp UserVoucher (không trừ điểm – đây là bồi hoàn)
            List<UserVoucherResponse> issued = new ArrayList<>();
            for (Voucher v : suggested) {
                String uniqueCode = v.getCode() + "-RF-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                UserVoucher uv = UserVoucher.builder()
                    .user(user)
                    .voucher(v)
                    .pointsSpent(0)
                    .redeemedCode(uniqueCode)
                    .status(UserVoucherStatus.ACTIVE)
                    .build();
                userVoucherRepository.save(uv);
                v.setRedeemedCount(v.getRedeemedCount() + 1);
                voucherRepository.save(v);
                issued.add(UserVoucherResponse.from(uv));
            }

            // 5. Hủy booking và đánh dấu payment REFUNDED
            bookingService.cancelBooking(booking.getId(), username);

            return ResponseEntity.ok(Map.of(
                "refundType", "VOUCHER",
                "vouchers",   issued,
                "totalVoucherValue", suggested.stream().mapToLong(Voucher::getValue).sum()
            ));
        } else {
            // ── Hoàn tiền tiền mặt (flow gốc) ──────────────────────────────
            PaymentResponse payment = paymentService.getPayment(paymentId, username);
            Long bookingId = payment.getBookingId();
            BookingResponse cancelledBooking = bookingService.cancelBooking(bookingId, username);
            return ResponseEntity.ok(cancelledBooking);
        }
    }

    /**
     * GET /payments/{paymentId}/refund-voucher-suggestion
     * Trả về danh sách voucher được gợi ý tương ứng với số tiền hoàn theo chính sách.
     */
    @GetMapping("/payments/{paymentId}/refund-voucher-suggestion")
    public ResponseEntity<Map<String, Object>> getRefundVoucherSuggestion(
            @PathVariable Long paymentId,
            Authentication authentication) {

        String username = authentication.getName();

        PaymentResponse payment = paymentService.getPayment(paymentId, username);
        BookingResponse booking = bookingService.getBookingById(payment.getBookingId());

        long refundAmount = calculateApplicableRefundAmount(booking, payment);

        // Nếu không tính được tiền hoàn theo policy (check-in đã qua, policy = 0%,
        // hoặc chưa cấu hình policy), dùng toàn bộ totalAmount làm cơ sở gợi ý
        // để người dùng vẫn có thể chọn voucher thay thế hoàn tiền mặt.
        long suggestionBasis = refundAmount > 0
                ? refundAmount
                : payment.getTotalAmount().longValue();

        List<Voucher> suggested = suggestVouchersForAmount(suggestionBasis);
        long totalVoucherValue  = suggested.stream().mapToLong(Voucher::getValue).sum();

        List<Map<String, Object>> voucherList = new ArrayList<>();
        for (Voucher v : suggested) {
            Map<String, Object> m = new HashMap<>();
            m.put("id",          v.getId());
            m.put("name",        v.getName());
            m.put("description", v.getDescription());
            m.put("value",       v.getValue());
            m.put("category",    v.getCategory());
            voucherList.add(m);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("refundAmount",      refundAmount);
        result.put("totalVoucherValue", totalVoucherValue);
        result.put("vouchers",          voucherList);
        result.put("available",         !suggested.isEmpty());

        return ResponseEntity.ok(result);
    }

    /**
     * Thuật toán greedy gợi ý voucher phù hợp với số tiền hoàn.
     * Ưu tiên: tìm voucher đơn có giá trị gần nhất (lớn hơn hoặc bằng).
     * Nếu không có, kết hợp nhiều voucher lớn nhất cho đến khi tổng >= refundAmount.
     */
    private List<Voucher> suggestVouchersForAmount(long refundAmount) {
        if (refundAmount <= 0) {
            return List.of();
        }

        List<Voucher> available = voucherRepository.findByActiveTrueOrderByValueDesc().stream()
            .filter(v -> v.getMaxRedemptions() == null || v.getRedeemedCount() < v.getMaxRedemptions())
            .toList();

        if (available.isEmpty()) {
            return List.of();
        }

        // Thử tìm voucher đơn có giá trị >= refundAmount (lấy nhỏ nhất trong số đó)
        Optional<Voucher> exactOrAbove = available.stream()
            .filter(v -> v.getValue() >= refundAmount)
            .min(Comparator.comparingLong(Voucher::getValue));
        if (exactOrAbove.isPresent()) {
            return List.of(exactOrAbove.get());
        }

        // Không có voucher đơn đủ lớn → gộp greedy từ lớn đến nhỏ
        List<Voucher> selected = new ArrayList<>();
        long accumulated = 0;
        for (Voucher v : available) {
            selected.add(v);
            accumulated += v.getValue();
            if (accumulated >= refundAmount) break;
        }
        
        return selected;
    }

    /**
     * Tính số tiền hoàn tiền thực tế theo chính sách áp dụng.
     */
    private long calculateApplicableRefundAmount(BookingResponse booking, PaymentResponse payment) {
        java.util.List<CancellationPolicy> dbPolicies = cancellationService.getPolicies();
        if (dbPolicies.isEmpty()) return 0L;

        long hoursUntilCheckIn = java.time.temporal.ChronoUnit.HOURS.between(
            java.time.LocalDateTime.now(),
            booking.getCheckIn().atStartOfDay()
        );

        dbPolicies.sort((a, b) -> Integer.compare(b.getMinHours(), a.getMinHours()));
        CancellationPolicy applicable = dbPolicies.stream()
            .filter(p -> hoursUntilCheckIn >= p.getMinHours())
            .findFirst()
            .orElse(dbPolicies.get(dbPolicies.size() - 1));

        double refundRate = applicable.getRefundRate() / 100.0;
        return Math.round(payment.getTotalAmount().doubleValue() * refundRate);
    }

    /**
     * User: tính toán số tiền hoàn trước khi hủy (preview).
     */
    @GetMapping("/payments/{paymentId}/refund-preview")
    public ResponseEntity<Map<String, Object>> getRefundPreview(
            @PathVariable Long paymentId,
            Authentication authentication) {

        String username = authentication.getName();
        
        // Lấy thông tin payment và booking
        PaymentResponse payment = paymentService.getPayment(paymentId, username);
        BookingResponse booking = bookingService.getBookingById(payment.getBookingId());
        
        // Tính toán refund dựa trên chính sách
        Map<String, Object> preview = new HashMap<>();
        preview.put("paymentId", paymentId);
        preview.put("bookingId", booking.getId());
        preview.put("totalAmount", payment.getTotalAmount());
        preview.put("checkInDate", booking.getCheckIn());
        preview.put("policies", calculateRefundPolicies(booking, payment));
        
        return ResponseEntity.ok(preview);
    }

    private java.util.List<Map<String, Object>> calculateRefundPolicies(BookingResponse booking, PaymentResponse payment) {
        java.util.List<Map<String, Object>> result = new java.util.ArrayList<>();
        
        // Lấy số giờ từ hiện tại đến ngày check-in
        long hoursUntilCheckIn = java.time.temporal.ChronoUnit.HOURS.between(
            java.time.LocalDateTime.now(), 
            booking.getCheckIn().atStartOfDay()
        );
        
        // Lấy chính sách từ database (đã được admin cấu hình)
        java.util.List<CancellationPolicy> dbPolicies = cancellationService.getPolicies();
        
        // Sắp xếp theo minHours giảm dần
        dbPolicies.sort((a, b) -> Integer.compare(b.getMinHours(), a.getMinHours()));
        
        // Tìm policy áp dụng (policy có minHours lớn nhất mà <= hoursUntilCheckIn)
        CancellationPolicy applicablePolicy = null;
        for (CancellationPolicy policy : dbPolicies) {
            if (hoursUntilCheckIn >= policy.getMinHours()) {
                applicablePolicy = policy;
                break; // Đã sort giảm dần nên policy đầu tiên thỏa mãn là policy tốt nhất
            }
        }
        
        // Nếu không tìm thấy (hoursUntilCheckIn < 0), lấy policy có minHours nhỏ nhất
        if (applicablePolicy == null && !dbPolicies.isEmpty()) {
            applicablePolicy = dbPolicies.get(dbPolicies.size() - 1);
        }
        
        // Tạo danh sách chính sách với trạng thái áp dụng
        for (CancellationPolicy policy : dbPolicies) {
            boolean isApplicable = (applicablePolicy != null && policy.getId().equals(applicablePolicy.getId()));
            
            Map<String, Object> policyMap = new HashMap<>();
            policyMap.put("description", policy.getLabel());
            policyMap.put("refundPercent", policy.getRefundRate());
            policyMap.put("minHours", policy.getMinHours());
            policyMap.put("refundAmount", payment.getTotalAmount()
                .multiply(java.math.BigDecimal.valueOf(policy.getRefundRate()))
                .divide(java.math.BigDecimal.valueOf(100)));
            policyMap.put("isApplicable", isApplicable);
            
            result.add(policyMap);
        }
        
        return result;
    }
}

