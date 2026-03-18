package com.example.backend.controller;

import com.example.backend.dto.request.CancelBookingRequest;
import com.example.backend.dto.request.PolicyRequest;
import com.example.backend.dto.request.ProcessRefundRequest;
import com.example.backend.dto.response.CancellationResponse;
import com.example.backend.dto.response.CancellationStatsResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.CancellationPolicy;
import com.example.backend.entity.RefundStatus;
import com.example.backend.service.CancellationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller – Hủy phòng & Hoàn tiền.
 *
 * Base path: /api/admin/cancellations
 * Requires:  ROLE_ADMIN hoặc ROLE_HOTEL_OWNER
 */
@RestController
@RequestMapping("/api/admin/cancellations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
public class CancellationController {

    private final CancellationService cancellationService;

    /* ══════════════════════════════════════════════════════════════════
       DANH SÁCH & CHI TIẾT
    ══════════════════════════════════════════════════════════════════ */

    /**
     * GET /api/admin/cancellations
     * ?refundStatus=PENDING_REFUND&keyword=nguyen&from=2026-01-01&to=2026-12-31&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<PagedResponse<CancellationResponse>> getCancellations(
            @RequestParam(required = false) RefundStatus refundStatus,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                cancellationService.getCancellations(refundStatus, keyword, from, to, page, size));
    }

    /**
     * GET /api/admin/cancellations/{id}
     * Chi tiết một yêu cầu hủy phòng.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CancellationResponse> getCancellationById(@PathVariable Long id) {
        return ResponseEntity.ok(cancellationService.getCancellationById(id));
    }

    /* ══════════════════════════════════════════════════════════════════
       THỐNG KÊ
    ══════════════════════════════════════════════════════════════════ */

    /**
     * GET /api/admin/cancellations/stats
     * Trả về: total, pending, refunded, rejected, totalRefundAmount
     */
    @GetMapping("/stats")
    public ResponseEntity<CancellationStatsResponse> getStats() {
        return ResponseEntity.ok(cancellationService.getStats());
    }

    /* ══════════════════════════════════════════════════════════════════
       XỬ LÝ HOÀN TIỀN
    ══════════════════════════════════════════════════════════════════ */

    /**
     * PATCH /api/admin/cancellations/{id}/status
     * Admin duyệt hoàn tiền (REFUNDED) hoặc từ chối (REJECTED).
     *
     * Body: { "status": "REFUNDED", "note": "Ghi chú" }
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<CancellationResponse> processRefund(
            @PathVariable Long id,
            @Valid @RequestBody ProcessRefundRequest request
    ) {
        return ResponseEntity.ok(cancellationService.processRefund(id, request));
    }

    /* ══════════════════════════════════════════════════════════════════
       HỦY PHÒNG TỪ ADMIN
    ══════════════════════════════════════════════════════════════════ */

    /**
     * POST /api/admin/cancellations/cancel/{bookingId}
     * Admin chủ động hủy một booking và tính refund tự động theo policy.
     *
     * Body: { "reason": "Lý do hủy" }
     */
    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<CancellationResponse> cancelBookingByAdmin(
            @PathVariable Long bookingId,
            @RequestBody(required = false) CancelBookingRequest request
    ) {
        if (request == null) request = new CancelBookingRequest();
        return ResponseEntity.ok(cancellationService.cancelBookingByAdmin(bookingId, request));
    }

    /* ══════════════════════════════════════════════════════════════════
       XÓA
    ══════════════════════════════════════════════════════════════════ */

    /**
     * DELETE /api/admin/cancellations/{id}
     * Xóa bản ghi hủy phòng (chỉ khi đã REFUNDED hoặc REJECTED).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCancellation(@PathVariable Long id) {
        cancellationService.deleteCancellation(id);
        return ResponseEntity.ok(Map.of("message", "Đã xóa yêu cầu hủy phòng #" + id));
    }

    /* ══════════════════════════════════════════════════════════════════
       CHÍNH SÁCH HOÀN TIỀN
    ══════════════════════════════════════════════════════════════════ */

    /**
     * GET /api/admin/cancellations/policies
     * Lấy danh sách tất cả chính sách hoàn tiền.
     */
    @GetMapping("/policies")
    public ResponseEntity<List<CancellationPolicy>> getPolicies() {
        return ResponseEntity.ok(cancellationService.getPolicies());
    }

    /**
     * PUT /api/admin/cancellations/policies
     * Thay thế toàn bộ danh sách chính sách.
     *
     * Body: [ { "label": "...", "minHours": 48, "refundRate": 100 }, ... ]
     */
    @PutMapping("/policies")
    public ResponseEntity<List<CancellationPolicy>> savePolicies(
            @Valid @RequestBody List<PolicyRequest> requests
    ) {
        return ResponseEntity.ok(cancellationService.savePolicies(requests));
    }

    /**
     * POST /api/admin/cancellations/policies
     * Thêm một chính sách mới.
     */
    @PostMapping("/policies")
    public ResponseEntity<CancellationPolicy> addPolicy(
            @Valid @RequestBody PolicyRequest request
    ) {
        return ResponseEntity.ok(cancellationService.addPolicy(request));
    }

    /**
     * PUT /api/admin/cancellations/policies/{policyId}
     * Sửa một chính sách.
     */
    @PutMapping("/policies/{policyId}")
    public ResponseEntity<CancellationPolicy> updatePolicy(
            @PathVariable Long policyId,
            @Valid @RequestBody PolicyRequest request
    ) {
        return ResponseEntity.ok(cancellationService.updatePolicy(policyId, request));
    }

    /**
     * DELETE /api/admin/cancellations/policies/{policyId}
     * Xóa một chính sách.
     */
    @DeleteMapping("/policies/{policyId}")
    public ResponseEntity<Map<String, String>> deletePolicy(@PathVariable Long policyId) {
        cancellationService.deletePolicy(policyId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa chính sách #" + policyId));
    }
}
