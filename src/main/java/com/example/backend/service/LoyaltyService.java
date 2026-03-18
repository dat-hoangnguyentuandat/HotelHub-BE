package com.example.backend.service;

import com.example.backend.dto.request.AdjustPointsRequest;
import com.example.backend.dto.request.RedeemPointsRequest;
import com.example.backend.dto.response.LoyaltyAccountResponse;
import com.example.backend.dto.response.LoyaltyTransactionResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.TransactionType;

import java.time.YearMonth;

public interface LoyaltyService {

    /** Lấy thông tin loyalty của user đang đăng nhập */
    LoyaltyAccountResponse getMyLoyaltyAccount(String email);

    /** Admin lấy thông tin loyalty của 1 user bất kỳ (by userId) */
    LoyaltyAccountResponse getLoyaltyAccountByUserId(Long userId);

    /** Admin: danh sách tất cả tài khoản loyalty (phân trang) */
    PagedResponse<LoyaltyAccountResponse> getAllLoyaltyAccounts(int page, int size);

    /** Lịch sử giao dịch (lọc theo type & tháng) */
    PagedResponse<LoyaltyTransactionResponse> getTransactionHistory(
            String email,
            TransactionType type,
            YearMonth month,
            int page, int size);

    /** Admin: lịch sử của 1 user bất kỳ */
    PagedResponse<LoyaltyTransactionResponse> getTransactionHistoryByUserId(
            Long userId,
            TransactionType type,
            YearMonth month,
            int page, int size);

    /** Tự động tích điểm khi booking CHECKED_OUT (gọi từ BookingService) */
    void earnPointsForBooking(Long bookingId);

    /** Khách tự đổi điểm */
    LoyaltyAccountResponse redeemPoints(String email, RedeemPointsRequest request);

    /** Admin: cộng điểm thủ công cho 1 user */
    LoyaltyAccountResponse adjustPoints(Long userId, AdjustPointsRequest request);
}
