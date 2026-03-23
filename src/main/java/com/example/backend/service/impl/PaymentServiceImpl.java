package com.example.backend.service.impl;

import com.example.backend.dto.request.PaymentRequest;
import com.example.backend.dto.request.PromoValidateRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.PaymentInfoResponse;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.dto.response.PaymentStatsResponse;
import com.example.backend.dto.response.PromoValidateResponse;
import com.example.backend.dto.response.UserPaymentStatsResponse;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.PromoCodeRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserVoucherRepository;
import com.example.backend.service.LoyaltyService;
import com.example.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository  paymentRepository;
    private final BookingRepository  bookingRepository;
    private final UserRepository     userRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final LoyaltyService loyaltyService;

    /* ── VAT cố định 10% ── */
    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    /* ── Điểm thưởng: 1 điểm / 100,000 VND (thống nhất với LoyaltyService) ── */
    private static final BigDecimal POINTS_PER_UNIT = BigDecimal.valueOf(100_000);

    /* ══════════════════════════════════════════════════════
       1. INITIATE PAYMENT
    ══════════════════════════════════════════════════════ */
    @Override
    public PaymentResponse initiatePayment(PaymentRequest req, String userEmail) {
        log.info("[Payment] initiatePayment bookingId={} method={} user={}",
            req.getBookingId(), req.getMethod(), userEmail);

        // 1. Lấy booking
        Booking booking = bookingRepository.findById(req.getBookingId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Không tìm thấy đặt phòng #" + req.getBookingId()));

        // 2. Kiểm tra quyền truy cập
        validateBookingOwnership(booking, userEmail);

        // 3. Booking phải ở trạng thái PENDING
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Đặt phòng không ở trạng thái chờ xác nhận (trạng thái hiện tại: "
                    + booking.getStatus() + ")");
        }

        // 4. Kiểm tra đã có payment thành công chưa
        paymentRepository.findByBookingIdAndStatus(booking.getId(), PaymentStatus.SUCCESS)
            .ifPresent(p -> { throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Đặt phòng này đã được thanh toán thành công (mã GD: " + p.getTransactionRef() + ")"); });

        // 5. Tính toán tiền
        BigDecimal subtotal = booking.getTotalAmount();      // = pricePerNight × nights × rooms

        // Tính discount: hỗ trợ cả promo code (%) và voucher đổi điểm (tiền cố định)
        BigDecimal[] promoResult = resolvePromoAndVoucherDiscount(
            req.getPromoCode(), req.getPromoDiscountRate(), subtotal);
        BigDecimal promoRate     = promoResult[0];
        BigDecimal promoDiscount = promoResult[1];

        BigDecimal loyaltyDiscount = resolveLoyaltyDiscount(req.getLoyaltyPointsUsed());

        BigDecimal afterDiscount = subtotal.subtract(promoDiscount).subtract(loyaltyDiscount)
            .max(BigDecimal.ZERO);

        BigDecimal vat   = afterDiscount.multiply(VAT_RATE).setScale(0, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(vat);

        // 6. Xây dựng Payment entity
        Payment payment = Payment.builder()
            .booking(booking)
            .method(req.getMethod())
            .status(PaymentStatus.PENDING)
            .subtotal(subtotal)
            .discountAmount(promoDiscount.add(loyaltyDiscount))
            .vatAmount(vat)
            .totalAmount(total)
            .promoCode(req.getPromoCode() != null ? req.getPromoCode().toUpperCase() : null)
            .promoDiscountRate(promoRate)
            .loyaltyPointsUsed(req.getLoyaltyPointsUsed())
            .loyaltyDiscount(loyaltyDiscount)
            .cardLastFour(req.getCardLastFour())
            .cardType(req.getCardType())
            .cardHolder(req.getCardHolder())
            .walletProvider(req.getWalletProvider())
            .transactionRef(generateTransactionRef())
            .note(req.getNote())
            .build();

        Payment saved = paymentRepository.save(payment);
        log.info("[Payment] Created paymentId={} ref={} total={}",
            saved.getId(), saved.getTransactionRef(), saved.getTotalAmount());

        return PaymentResponse.from(saved);
    }

    /* ══════════════════════════════════════════════════════
       2. PROCESS PAYMENT (mô phỏng gateway)
    ══════════════════════════════════════════════════════ */
    @Override
    public PaymentResponse processPayment(Long paymentId, String userEmail) {
        log.info("[Payment] processPayment paymentId={} user={}", paymentId, userEmail);

        Payment payment = getPaymentEntity(paymentId);
        validatePaymentOwnership(payment, userEmail);

        if (!payment.isProcessable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                payment.isExpired()
                    ? "Phiên thanh toán đã hết hạn. Vui lòng tạo lại."
                    : "Giao dịch không ở trạng thái có thể xử lý (trạng thái: " + payment.getStatus() + ")");
        }

        // Chuyển sang PROCESSING
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        // ── Gọi cổng thanh toán (mô phỏng) ──
        GatewayResult result = simulateGateway(payment);

        if (result.success()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.now());
            payment.setGatewayTransactionId(result.gatewayTxnId());
            payment.setGatewayResponseCode("00");
            payment.setGatewayMessage("Giao dịch thành công");

            // Tích điểm thưởng (1 điểm / 100,000 VND)
            int pointsEarned = payment.getTotalAmount()
                .divide(POINTS_PER_UNIT, 0, RoundingMode.FLOOR)
                .intValue();
            payment.setLoyaltyPointsEarned(pointsEarned);

            // Cộng điểm vào tài khoản loyalty của user (nếu có)
            Booking booking = payment.getBooking();
            if (booking.getUser() != null && pointsEarned > 0) {
                try {
                    loyaltyService.earnPointsForBooking(booking.getId());
                    log.info("[Payment] Earned {} loyalty points for user {} from booking #{}",
                        pointsEarned, booking.getUser().getEmail(), booking.getId());
                } catch (Exception e) {
                    log.warn("[Payment] Failed to add loyalty points for booking #{}: {}",
                        booking.getId(), e.getMessage());
                }
            }

            // Cập nhật trạng thái booking → CONFIRMED
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // Đánh dấu UserVoucher → USED (nếu đã dùng mã voucher đổi điểm)
            if (payment.getPromoCode() != null && !payment.getPromoCode().isBlank()) {
                userVoucherRepository
                    .findByRedeemedCodeIgnoreCaseAndStatus(
                        payment.getPromoCode(), UserVoucherStatus.ACTIVE)
                    .ifPresent(uv -> {
                        uv.setStatus(UserVoucherStatus.USED);
                        userVoucherRepository.save(uv);
                        log.info("[Payment] Voucher {} marked USED after SUCCESS paymentId={}",
                            uv.getRedeemedCode(), payment.getId());
                    });
            }

            log.info("[Payment] SUCCESS paymentId={} bookingId={} pointsEarned={}",
                payment.getId(), booking.getId(), pointsEarned);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setCompletedAt(LocalDateTime.now());
            payment.setGatewayResponseCode(result.code());
            payment.setGatewayMessage(result.message());
            log.warn("[Payment] FAILED paymentId={} code={} msg={}",
                payment.getId(), result.code(), result.message());
        }

        Payment saved = paymentRepository.save(payment);

        if (!result.success()) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
                "Thanh toán thất bại: " + result.message());
        }

        return PaymentResponse.from(saved);
    }

    /* ══════════════════════════════════════════════════════
       3. GET PAYMENT
    ══════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId, String userEmail) {
        Payment payment = getPaymentEntity(paymentId);
        validatePaymentOwnership(payment, userEmail);
        return PaymentResponse.from(payment);
    }

    /* ══════════════════════════════════════════════════════
       4. GET PAYMENTS BY BOOKING
    ══════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getPaymentsByBooking(
            Long bookingId, String userEmail, int page, int size) {

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng #" + bookingId));
        validateBookingOwnership(booking, userEmail);

        List<Payment> payments = paymentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        List<PaymentResponse> content = payments.stream().map(PaymentResponse::from).toList();

        return PagedResponse.<PaymentResponse>builder()
            .content(content)
            .page(0).size(content.size())
            .totalElements(content.size()).totalPages(1)
            .last(true)
            .build();
    }

    /* ══════════════════════════════════════════════════════
       5. GET MY PAYMENTS
    ══════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> getMyPayments(String userEmail, int page, int size) {
        User user = getUserByEmail(userEmail);
        Pageable pageable = PageRequest.of(page, size);

        // Lấy tất cả booking của user, rồi lấy payments
        // Dùng native approach: filter payments theo booking.user
        Page<Payment> paymentsPage = paymentRepository.searchPayments(
            null, null, userEmail, null, null, pageable);

        return buildPagedResponse(paymentsPage);
    }

    /* ══════════════════════════════════════════════════════
       5b. GET MY PAYMENTS INFO (payment-info page)
    ══════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentInfoResponse> getMyPaymentsInfo(
            String userEmail,
            PaymentStatus status,
            PaymentMethod method,
            String keyword,
            LocalDate from,
            LocalDate to,
            int page,
            int size) {

        if (userEmail == null || userEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Vui lòng đăng nhập để xem thông tin thanh toán");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> result = paymentRepository.findByUserEmailFiltered(
            userEmail, status, method,
            (keyword != null && !keyword.isBlank()) ? keyword.trim() : null,
            from, to, pageable);

        List<PaymentInfoResponse> content = result.getContent().stream()
            .map(PaymentInfoResponse::from)
            .toList();

        return PagedResponse.<PaymentInfoResponse>builder()
            .content(content)
            .page(result.getNumber())
            .size(result.getSize())
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .last(result.isLast())
            .build();
    }

    /* ══════════════════════════════════════════════════════
       5c. GET MY PAYMENT STATS
    ══════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public UserPaymentStatsResponse getMyPaymentStats(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Vui lòng đăng nhập để xem thống kê thanh toán");
        }

        long successCount   = paymentRepository.countByUserEmailAndStatus(userEmail, PaymentStatus.SUCCESS);
        long pendingCount   = paymentRepository.countByUserEmailAndStatus(userEmail, PaymentStatus.PENDING)
                            + paymentRepository.countByUserEmailAndStatus(userEmail, PaymentStatus.PROCESSING);
        long failedCount    = paymentRepository.countByUserEmailAndStatus(userEmail, PaymentStatus.FAILED);
        long cancelledCount = paymentRepository.countByUserEmailAndStatus(userEmail, PaymentStatus.CANCELLED)
                            + paymentRepository.countByUserEmailAndStatus(userEmail, PaymentStatus.REFUNDED);
        long totalCount     = successCount + pendingCount + failedCount + cancelledCount;

        BigDecimal totalSpend      = paymentRepository.sumSuccessAmountByUserEmail(userEmail);
        long       totalPoints     = paymentRepository.sumLoyaltyPointsEarnedByUserEmail(userEmail);

        return UserPaymentStatsResponse.builder()
            .totalCount(totalCount)
            .successCount(successCount)
            .pendingCount(pendingCount)
            .failedCount(failedCount)
            .cancelledCount(cancelledCount)
            .totalSpend(totalSpend != null ? totalSpend : BigDecimal.ZERO)
            .totalPointsEarned(totalPoints)
            .build();
    }

    /* ══════════════════════════════════════════════════════
       5d. GET PAYMENT INFO BY ID
    ══════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PaymentInfoResponse getPaymentInfoById(Long paymentId, String userEmail) {
        Payment payment = getPaymentEntity(paymentId);
        validatePaymentOwnership(payment, userEmail);
        return PaymentInfoResponse.from(payment);
    }

    /* ══════════════════════════════════════════════════════
       6. CANCEL PAYMENT
    ══════════════════════════════════════════════════════ */
    @Override
    public PaymentResponse cancelPayment(Long paymentId, String userEmail) {
        Payment payment = getPaymentEntity(paymentId);
        validatePaymentOwnership(payment, userEmail);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Chỉ có thể hủy giao dịch đang ở trạng thái PENDING");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setCompletedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        log.info("[Payment] CANCELLED paymentId={}", paymentId);
        return PaymentResponse.from(saved);
    }

    /* ══════════════════════════════════════════════════════
       7. VALIDATE PROMO
    ══════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PromoValidateResponse validatePromo(PromoValidateRequest req) {
        String code = req.getCode().toUpperCase().trim();
        log.info("[validatePromo] Validating code: {}, bookingId: {}", code, req.getBookingId());

        try {
            // ── Bước 1: Tìm trong bảng promo_codes truyền thống ──
            Optional<PromoCode> promoOpt = promoCodeRepository.findByCodeIgnoreCaseAndActiveTrue(code);
            if (promoOpt.isPresent()) {
                PromoCode promo = promoOpt.get();
                BigDecimal rate = promo.getDiscountRate();

                BigDecimal[] discountAmount = { BigDecimal.ZERO };
                if (req.getBookingId() != null) {
                    bookingRepository.findById(req.getBookingId()).ifPresent(b ->
                        discountAmount[0] = b.getTotalAmount()
                            .multiply(rate).setScale(0, RoundingMode.HALF_UP)
                    );
                }

                return PromoValidateResponse.builder()
                    .valid(true)
                    .code(promo.getCode())
                    .label(promo.getLabel())
                    .discountRate(rate)
                    .discountAmount(discountAmount[0])
                    .isVoucher(false)
                    .message("Mã hợp lệ – " + promo.getLabel())
                    .build();
            }

            // ── Bước 2: Tìm trong bảng user_vouchers (mã voucher đổi điểm) ──
            Optional<UserVoucher> uvOpt = userVoucherRepository
                .findByRedeemedCodeIgnoreCaseAndStatus(code, UserVoucherStatus.ACTIVE);
            if (uvOpt.isPresent()) {
                UserVoucher uv = uvOpt.get();
                Voucher voucher = uv.getVoucher();
                BigDecimal voucherValue = BigDecimal.valueOf(voucher.getValue());

                // Với voucher giảm tiền cố định, discountAmount = min(voucherValue, subtotal)
                BigDecimal discountAmount = voucherValue;
                if (req.getBookingId() != null) {
                    Optional<Booking> bookingOpt = bookingRepository.findById(req.getBookingId());
                    if (bookingOpt.isPresent()) {
                        BigDecimal subtotal = bookingOpt.get().getTotalAmount();
                        discountAmount = voucherValue.min(subtotal);
                    }
                }

                return PromoValidateResponse.builder()
                    .valid(true)
                    .code(uv.getRedeemedCode())
                    .label(voucher.getName())
                    .discountRate(BigDecimal.ZERO)   // không giảm theo %
                    .discountAmount(discountAmount)
                    .isVoucher(true)
                    .voucherValue(voucherValue)
                    .message("Voucher hợp lệ – " + voucher.getName()
                        + " (giảm " + String.format("%,d", voucher.getValue()) + "đ)")
                    .build();
            }

            // ── Không tìm thấy ──
            return PromoValidateResponse.builder()
                .valid(false)
                .code(code)
                .message("Mã khuyến mãi không hợp lệ hoặc đã hết hạn")
                .build();
                
        } catch (Exception e) {
            log.error("[validatePromo] Error validating code: {}", code, e);
            return PromoValidateResponse.builder()
                .valid(false)
                .code(code)
                .message("Lỗi khi kiểm tra mã: " + e.getMessage())
                .build();
        }
    }

    /* ══════════════════════════════════════════════════════
       8. ADMIN SEARCH
    ══════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<PaymentResponse> adminSearchPayments(
            PaymentStatus status, PaymentMethod method,
            String keyword, LocalDate from, LocalDate to,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> result = paymentRepository.searchPayments(
            status, method, keyword, from, to, pageable);
        return buildPagedResponse(result);
    }

    /* ══════════════════════════════════════════════════════
       9. ADMIN GET ONE
    ══════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse adminGetPayment(Long paymentId) {
        return PaymentResponse.from(getPaymentEntity(paymentId));
    }

    /* ══════════════════════════════════════════════════════
       10. ADMIN STATS
    ══════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PaymentStatsResponse adminGetStats(LocalDate from, LocalDate to) {
        LocalDate effectiveFrom = (from != null) ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveTo   = (to != null)   ? to   : LocalDate.now();

        BigDecimal revenue = paymentRepository.sumSuccessRevenueBetween(effectiveFrom, effectiveTo);
        long total   = paymentRepository.count();
        long success = paymentRepository.countByStatus(PaymentStatus.SUCCESS);
        long failed  = paymentRepository.countByStatus(PaymentStatus.FAILED);
        long pending = paymentRepository.countByStatus(PaymentStatus.PENDING)
                     + paymentRepository.countByStatus(PaymentStatus.PROCESSING);

        // Revenue by method
        Map<String, BigDecimal> revenueByMethod = new LinkedHashMap<>();
        Map<String, Long>       countByMethod   = new LinkedHashMap<>();
        paymentRepository.revenueByMethod(effectiveFrom, effectiveTo)
            .forEach(row -> {
                String m = row[0].toString();
                revenueByMethod.put(m, (BigDecimal) row[2]);
                countByMethod.put(m, (Long) row[1]);
            });

        // Recent payments
        Pageable top10 = PageRequest.of(0, 10);
        List<PaymentResponse> recent = paymentRepository
            .searchPayments(null, null, null, null, null, top10)
            .getContent()
            .stream().map(PaymentResponse::from).toList();

        double successRate = total > 0 ? (double) success / total * 100 : 0;

        return PaymentStatsResponse.builder()
            .totalPayments(total)
            .successPayments(success)
            .failedPayments(failed)
            .pendingPayments(pending)
            .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
            .revenueByMethod(revenueByMethod)
            .countByMethod(countByMethod)
            .successRate(Math.round(successRate * 10.0) / 10.0)
            .recentPayments(recent)
            .build();
    }

    /* ══════════════════════════════════════════════════════
       11. ADMIN REFUND
    ══════════════════════════════════════════════════════ */
    @Override
    public PaymentResponse adminRefundPayment(Long paymentId, String reason) {
        Payment payment = getPaymentEntity(paymentId);

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Chỉ có thể hoàn tiền giao dịch đã thành công");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setNote((payment.getNote() != null ? payment.getNote() + " | " : "")
            + "Hoàn tiền: " + reason);
        payment.setCompletedAt(LocalDateTime.now());

        // Cập nhật booking → CANCELLED
        Booking booking = payment.getBooking();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason("Hoàn tiền – " + reason);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);

        Payment saved = paymentRepository.save(payment);
        log.info("[Payment] REFUNDED paymentId={} bookingId={}", paymentId, booking.getId());
        return PaymentResponse.from(saved);
    }

    /* ══════════════════════════════════════════════════════
       12. EXPIRE STALE PAYMENTS (Scheduled)
    ══════════════════════════════════════════════════════ */
    @Override
    @Scheduled(fixedDelay = 60_000) // mỗi 1 phút
    public void expireStalePayments() {
        List<Payment> stale = paymentRepository.findExpiredPending(LocalDateTime.now());
        if (!stale.isEmpty()) {
            stale.forEach(p -> p.setStatus(PaymentStatus.CANCELLED));
            paymentRepository.saveAll(stale);
            log.info("[Payment] Expired {} stale payment(s)", stale.size());
        }
    }

    /* ══════════════════════════════════════════════════════
       PRIVATE HELPERS
    ══════════════════════════════════════════════════════ */

    private Payment getPaymentEntity(Long id) {
        return paymentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch #" + id));
    }

    private User getUserByEmail(String email) {
        if (email == null) return null;
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * Kiểm tra người dùng có quyền truy cập booking này không.
     * Admin / HOTEL_OWNER được phép tất cả.
     */
    private void validateBookingOwnership(Booking booking, String userEmail) {
        if (userEmail == null) return; // guest / walk-in
        User user = getUserByEmail(userEmail);
        if (user == null) return;
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.HOTEL_OWNER) return;
        if (booking.getUser() != null && !booking.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập đặt phòng này");
        }
    }

    private void validatePaymentOwnership(Payment payment, String userEmail) {
        validateBookingOwnership(payment.getBooking(), userEmail);
    }

    /**
     * Lấy tỉ lệ giảm giá từ promo code HOẶC số tiền giảm từ voucher đổi điểm.
     * Trả về BigDecimal[2]: [0] = discountRate (0.0-1.0), [1] = fixedDiscountAmount (VND)
     * Ưu tiên: DB promo → DB user_voucher → client rate.
     */
    private BigDecimal[] resolvePromoAndVoucherDiscount(String code, BigDecimal clientRate, BigDecimal subtotal) {
        // [0] = rate, [1] = fixed amount
        if (code != null && !code.isBlank()) {
            String trimmed = code.trim();

            // Tìm trong promo_codes
            BigDecimal dbRate = promoCodeRepository
                .findByCodeIgnoreCaseAndActiveTrue(trimmed)
                .map(PromoCode::getDiscountRate)
                .orElse(null);
            if (dbRate != null) {
                BigDecimal promoDiscount = subtotal.multiply(dbRate).setScale(0, RoundingMode.HALF_UP);
                return new BigDecimal[]{dbRate, promoDiscount};
            }

            // Tìm trong user_vouchers
            Optional<UserVoucher> uvOpt = userVoucherRepository
                .findByRedeemedCodeIgnoreCaseAndStatus(trimmed, UserVoucherStatus.ACTIVE);
            if (uvOpt.isPresent()) {
                BigDecimal voucherValue = BigDecimal.valueOf(uvOpt.get().getVoucher().getValue());
                BigDecimal fixedDiscount = voucherValue.min(subtotal);  // không giảm vượt quá tổng tiền
                return new BigDecimal[]{BigDecimal.ZERO, fixedDiscount};
            }
        }

        // Fallback: dùng rate từ client (tối đa 50%)
        if (clientRate != null && clientRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal rate = clientRate.min(new BigDecimal("0.50"));
            BigDecimal promoDiscount = subtotal.multiply(rate).setScale(0, RoundingMode.HALF_UP);
            return new BigDecimal[]{rate, promoDiscount};
        }

        return new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO};
    }

    /**
     * Lấy tỉ lệ giảm giá từ promo code trong DB.
     * Ưu tiên code từ DB, nếu không hợp lệ dùng rate client gửi lên (tối đa 0.50).
     * @deprecated dùng resolvePromoAndVoucherDiscount thay thế
     */
    @Deprecated
    private BigDecimal resolvePromoRate(String code, BigDecimal clientRate) {
        if (code != null && !code.isBlank()) {
            BigDecimal dbRate = promoCodeRepository
                .findByCodeIgnoreCaseAndActiveTrue(code.trim())
                .map(PromoCode::getDiscountRate)
                .orElse(null);
            if (dbRate != null) return dbRate;
        }
        if (clientRate != null && clientRate.compareTo(BigDecimal.ZERO) > 0) {
            return clientRate.min(new BigDecimal("0.50"));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Tính số tiền giảm từ điểm loyalty.
     * 1 điểm = 100 VND (theo quy định chung)
     */
    private BigDecimal resolveLoyaltyDiscount(int points) {
        if (points <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf((long) points * 100);  // 1 điểm = 100 VND
    }

    /**
     * Tạo mã giao dịch nội bộ: HTH-YYYYMMDD-XXXX
     * Dùng số lượng payment trong ngày từ DB để tránh trùng lặp sau khi restart server.
     */
    private String generateTransactionRef() {
        LocalDate today = LocalDate.now();
        String date = today.format(DateTimeFormatter.BASIC_ISO_DATE); // 20260319
        long countToday = paymentRepository.countCreatedOnDate(today);
        String seq = String.format("%04d", countToday + 1);
        return "HTH-" + date + "-" + seq;
    }

    /**
     * Mô phỏng kết quả từ cổng thanh toán.
     * Thực tế: gọi REST API của Stripe / VNPay / MoMo với SDK riêng.
     * CASH luôn thành công (thanh toán khi check-in).
     */
    private GatewayResult simulateGateway(Payment payment) {
        if (payment.getMethod() == PaymentMethod.CASH) {
            return new GatewayResult(true, "00", "Đặt phòng thành công, thanh toán tại quầy",
                "CASH-" + System.currentTimeMillis());
        }
        // Mô phỏng 95% thành công
        boolean success = Math.random() < 0.95;
        if (success) {
            return new GatewayResult(true, "00", "Giao dịch thành công",
                "SIM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        } else {
            return new GatewayResult(false, "51", "Tài khoản không đủ số dư", null);
        }
    }

    private PagedResponse<PaymentResponse> buildPagedResponse(Page<Payment> page) {
        return PagedResponse.<PaymentResponse>builder()
            .content(page.getContent().stream().map(PaymentResponse::from).toList())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .build();
    }

    /** Value object kết quả từ gateway */
    private record GatewayResult(boolean success, String code, String message, String gatewayTxnId) {}
}
