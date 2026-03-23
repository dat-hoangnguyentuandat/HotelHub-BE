package com.example.backend.service.impl;

import com.example.backend.dto.request.AdjustPointsRequest;
import com.example.backend.dto.request.RedeemPointsRequest;
import com.example.backend.dto.response.LoyaltyAccountResponse;
import com.example.backend.dto.response.LoyaltyTransactionResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.LoyaltyAccountRepository;
import com.example.backend.repository.LoyaltyTransactionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyServiceImpl implements LoyaltyService {

    /* ── Tỉ lệ tích điểm: 1 điểm / 100.000 VNĐ ── */
    private static final BigDecimal POINTS_PER_UNIT = BigDecimal.valueOf(100_000);

    private final LoyaltyAccountRepository     loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final UserRepository               userRepository;
    private final BookingRepository            bookingRepository;

    /* ══════════════════════════════════════════════════════════════
       GET MY ACCOUNT
    ══════════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public LoyaltyAccountResponse getMyLoyaltyAccount(String email) {
        LoyaltyAccount account = getOrCreateAccount(email);
        return LoyaltyAccountResponse.from(account);
    }

    /* ══════════════════════════════════════════════════════════════
       ADMIN – GET BY USER ID
    ══════════════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public LoyaltyAccountResponse getLoyaltyAccountByUserId(Long userId) {
        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản loyalty của user #" + userId));
        return LoyaltyAccountResponse.from(account);
    }

    /* ══════════════════════════════════════════════════════════════
       ADMIN – ALL ACCOUNTS
    ══════════════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LoyaltyAccountResponse> getAllLoyaltyAccounts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LoyaltyAccount> p = loyaltyAccountRepository.findAll(pageable);
        List<LoyaltyAccountResponse> content = p.getContent().stream()
                .map(LoyaltyAccountResponse::from)
                .toList();
        return buildPage(content, p);
    }

    /* ══════════════════════════════════════════════════════════════
       LỊCH SỬ GIAO DỊCH
    ══════════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public PagedResponse<LoyaltyTransactionResponse> getTransactionHistory(
            String email, TransactionType type, YearMonth month, int page, int size) {

        LoyaltyAccount account = getOrCreateAccount(email);
        return queryTransactions(account.getId(), type, month, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<LoyaltyTransactionResponse> getTransactionHistoryByUserId(
            Long userId, TransactionType type, YearMonth month, int page, int size) {

        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản loyalty của user #" + userId));
        return queryTransactions(account.getId(), type, month, page, size);
    }

    /* ══════════════════════════════════════════════════════════════
       EARN POINTS (khi booking CHECKED_OUT)
    ══════════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public void earnPointsForBooking(Long bookingId) {

        // Kiểm tra đã tích điểm booking này chưa
        if (loyaltyTransactionRepository.existsByBookingId(bookingId)) {
            log.info("Booking #{} đã được tích điểm trước đó, bỏ qua.", bookingId);
            return;
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking #" + bookingId));

        // Chỉ tích điểm cho booking của user đã đăng ký
        if (booking.getUser() == null) {
            log.info("Booking #{} là khách vãng lai, không tích điểm.", bookingId);
            return;
        }

        // Tính điểm: 1 điểm / 100.000 VNĐ, làm tròn xuống
        int earnedPoints = booking.getTotalAmount()
                .divide(POINTS_PER_UNIT, 0, java.math.RoundingMode.FLOOR)
                .intValue();

        if (earnedPoints <= 0) {
            log.info("Booking #{} tổng tiền quá nhỏ, không đủ để tích điểm.", bookingId);
            return;
        }

        LoyaltyAccount account = getOrCreateAccount(booking.getUser().getEmail());
        account.addPoints(earnedPoints);
        loyaltyAccountRepository.save(account);

        // Ghi log giao dịch
        String description = String.format("Tích điểm từ đặt phòng #BK-%d (%s)",
                bookingId, booking.getRoomType());

        LoyaltyTransaction tx = LoyaltyTransaction.builder()
                .loyaltyAccount(account)
                .booking(booking)
                .type(TransactionType.EARN)
                .points(earnedPoints)
                .balanceAfter(account.getCurrentPoints())
                .description(description)
                .build();

        loyaltyTransactionRepository.save(tx);
        log.info("Tích {} điểm cho user {} từ booking #{}",
                earnedPoints, booking.getUser().getEmail(), bookingId);
    }

    /* ══════════════════════════════════════════════════════════════
       REDEEM POINTS (khách tự đổi)
    ══════════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public LoyaltyAccountResponse redeemPoints(String email, RedeemPointsRequest request) {
        LoyaltyAccount account = getOrCreateAccount(email);
        account.deductPoints(request.getPoints());   // ném exception nếu không đủ điểm
        loyaltyAccountRepository.save(account);

        LoyaltyTransaction tx = LoyaltyTransaction.builder()
                .loyaltyAccount(account)
                .type(TransactionType.REDEEM)
                .points(request.getPoints())
                .balanceAfter(account.getCurrentPoints())
                .description(request.getDescription())
                .build();
        loyaltyTransactionRepository.save(tx);

        return LoyaltyAccountResponse.from(account);
    }

    /* ══════════════════════════════════════════════════════════════
       ADMIN – CỘNG ĐIỂM THỦ CÔNG
    ══════════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public LoyaltyAccountResponse adjustPoints(Long userId, AdjustPointsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user #" + userId));

        LoyaltyAccount account = loyaltyAccountRepository.findByUserId(userId)
                .orElseGet(() -> createNewAccount(user));

        account.addPoints(request.getPoints());
        loyaltyAccountRepository.save(account);

        LoyaltyTransaction tx = LoyaltyTransaction.builder()
                .loyaltyAccount(account)
                .type(TransactionType.EARN)
                .points(request.getPoints())
                .balanceAfter(account.getCurrentPoints())
                .description("[Admin] " + request.getDescription())
                .build();
        loyaltyTransactionRepository.save(tx);

        return LoyaltyAccountResponse.from(account);
    }

    /* ══════════════════════════════════════════════════════════════
       HELPERS
    ══════════════════════════════════════════════════════════════ */

    /** Lấy hoặc tự động tạo tài khoản loyalty lần đầu */
    private LoyaltyAccount getOrCreateAccount(String email) {
        return loyaltyAccountRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Không tìm thấy người dùng: " + email));
                    return loyaltyAccountRepository.save(createNewAccount(user));
                });
    }

    private LoyaltyAccount createNewAccount(User user) {
        return LoyaltyAccount.builder()
                .user(user)
                .currentPoints(0)
                .totalEarnedPoints(0)
                .tier(MembershipTier.SILVER)
                .build();
    }

    /** Query lịch sử có lọc type & month */
    private PagedResponse<LoyaltyTransactionResponse> queryTransactions(
            Long accountId, TransactionType type, YearMonth month, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        LocalDateTime from = (month != null) ? month.atDay(1).atStartOfDay() : null;
        LocalDateTime to   = (month != null) ? month.atEndOfMonth().plusDays(1).atStartOfDay() : null;

        Page<com.example.backend.entity.LoyaltyTransaction> p =
                loyaltyTransactionRepository.findFiltered(accountId, type, from, to, pageable);

        List<LoyaltyTransactionResponse> content = p.getContent().stream()
                .map(LoyaltyTransactionResponse::from)
                .toList();

        return buildPage(content, p);
    }

    private <T> PagedResponse<T> buildPage(List<T> content, Page<?> p) {
        return PagedResponse.<T>builder()
                .content(content)
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }
}
