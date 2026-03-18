package com.example.backend.controller;

import com.example.backend.dto.request.AdjustPointsRequest;
import com.example.backend.dto.request.RedeemPointsRequest;
import com.example.backend.dto.response.LoyaltyAccountResponse;
import com.example.backend.dto.response.LoyaltyTransactionResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.TransactionType;
import com.example.backend.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;

/**
 * REST Controller cho chức năng Khách hàng thân thiết.
 *
 * Base URL: /api/loyalty
 */
@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    /* ═══════════════════════════════════════════════════════════════
       KHÁCH HÀNG (đã đăng nhập)
    ═══════════════════════════════════════════════════════════════ */

    /**
     * GET /api/loyalty/me
     * Lấy thông tin tài khoản loyalty của bản thân.
     */
    @GetMapping("/me")
    public ResponseEntity<LoyaltyAccountResponse> getMyAccount(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(loyaltyService.getMyLoyaltyAccount(principal.getUsername()));
    }

    /**
     * GET /api/loyalty/me/transactions?type=EARN&month=2026-03&page=0&size=10
     * Lịch sử giao dịch điểm của bản thân.
     */
    @GetMapping("/me/transactions")
    public ResponseEntity<PagedResponse<LoyaltyTransactionResponse>> getMyTransactions(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagedResponse<LoyaltyTransactionResponse> data =
                loyaltyService.getTransactionHistory(principal.getUsername(), type, month, page, size);
        return ResponseEntity.ok(data);
    }

    /**
     * POST /api/loyalty/me/redeem
     * Đổi điểm thưởng (khách tự đổi).
     * Body: { "points": 100, "description": "Đổi điểm giảm giá phòng" }
     */
    @PostMapping("/me/redeem")
    public ResponseEntity<LoyaltyAccountResponse> redeemPoints(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RedeemPointsRequest request
    ) {
        return ResponseEntity.ok(loyaltyService.redeemPoints(principal.getUsername(), request));
    }

    /* ═══════════════════════════════════════════════════════════════
       ADMIN ENDPOINTS
    ═══════════════════════════════════════════════════════════════ */

    /**
     * GET /api/admin/loyalty?page=0&size=20
     * Danh sách tất cả tài khoản loyalty (phân trang).
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<PagedResponse<LoyaltyAccountResponse>> getAllAccounts(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(loyaltyService.getAllLoyaltyAccounts(page, size));
    }

    /**
     * GET /api/admin/loyalty/users/{userId}
     * Thông tin loyalty của 1 user cụ thể.
     */
    @GetMapping("/admin/users/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<LoyaltyAccountResponse> getAccountByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(loyaltyService.getLoyaltyAccountByUserId(userId));
    }

    /**
     * GET /api/admin/loyalty/users/{userId}/transactions?type=EARN&month=2026-03&page=0&size=10
     * Lịch sử giao dịch của 1 user.
     */
    @GetMapping("/admin/users/{userId}/transactions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<PagedResponse<LoyaltyTransactionResponse>> getUserTransactions(
            @PathVariable Long userId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                loyaltyService.getTransactionHistoryByUserId(userId, type, month, page, size));
    }

    /**
     * POST /api/admin/loyalty/users/{userId}/adjust
     * Cộng điểm thủ công cho user (Admin).
     * Body: { "points": 50, "description": "Thưởng sinh nhật" }
     */
    @PostMapping("/admin/users/{userId}/adjust")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<LoyaltyAccountResponse> adjustPoints(
            @PathVariable Long userId,
            @Valid @RequestBody AdjustPointsRequest request
    ) {
        return ResponseEntity.ok(loyaltyService.adjustPoints(userId, request));
    }
}
