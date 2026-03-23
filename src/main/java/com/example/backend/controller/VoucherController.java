package com.example.backend.controller;

import com.example.backend.dto.request.RedeemVoucherRequest;
import com.example.backend.dto.request.VoucherRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.UserVoucherResponse;
import com.example.backend.dto.response.VoucherResponse;
import com.example.backend.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Voucher.
 *
 * Public  : GET /api/vouchers                   – danh sách voucher active
 * Customer: POST /api/vouchers/redeem            – đổi điểm lấy voucher
 *           GET  /api/vouchers/my                – voucher đã đổi
 * Admin   : GET/POST/PATCH/DELETE /api/admin/vouchers/**
 */
@RestController
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    /* ═══════════════════════════════════════════════════════
       PUBLIC – Danh sách voucher để khách xem/đổi
    ═══════════════════════════════════════════════════════ */

    /**
     * GET /api/vouchers
     * Trả danh sách voucher đang active (không cần đăng nhập).
     */
    @GetMapping("/api/vouchers")
    public ResponseEntity<List<VoucherResponse>> getActiveVouchers() {
        return ResponseEntity.ok(voucherService.getActiveVouchers());
    }

    /* ═══════════════════════════════════════════════════════
       CUSTOMER (đã đăng nhập)
    ═══════════════════════════════════════════════════════ */

    /**
     * POST /api/vouchers/redeem
     * Đổi điểm lấy voucher.
     * Body: { "voucherId": 1 }
     */
    @PostMapping("/api/vouchers/redeem")
    public ResponseEntity<UserVoucherResponse> redeemVoucher(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RedeemVoucherRequest request
    ) {
        return ResponseEntity.ok(voucherService.redeemVoucher(principal.getUsername(), request));
    }

    /**
     * GET /api/vouchers/my?page=0&size=10
     * Lịch sử voucher đã đổi của khách hàng đang đăng nhập.
     */
    @GetMapping("/api/vouchers/my")
    public ResponseEntity<PagedResponse<UserVoucherResponse>> getMyVouchers(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                voucherService.getMyVouchers(principal.getUsername(), page, size));
    }

    /* ═══════════════════════════════════════════════════════
       ADMIN
    ═══════════════════════════════════════════════════════ */

    /**
     * GET /api/admin/vouchers?keyword=&active=true&page=0&size=10
     */
    @GetMapping("/api/admin/vouchers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<PagedResponse<VoucherResponse>> getAllVouchers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(voucherService.getAllVouchers(keyword, active, page, size));
    }

    /**
     * POST /api/admin/vouchers
     */
    @PostMapping("/api/admin/vouchers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<VoucherResponse> createVoucher(
            @Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(voucherService.createVoucher(request));
    }

    /**
     * PATCH /api/admin/vouchers/{id}
     */
    @PatchMapping("/api/admin/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, request));
    }

    /**
     * DELETE /api/admin/vouchers/{id}
     */
    @DeleteMapping("/api/admin/vouchers/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }
}
