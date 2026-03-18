package com.example.backend.service;

import com.example.backend.dto.request.CancelBookingRequest;
import com.example.backend.dto.request.PolicyRequest;
import com.example.backend.dto.request.ProcessRefundRequest;
import com.example.backend.dto.response.CancellationResponse;
import com.example.backend.dto.response.CancellationStatsResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.CancellationPolicy;
import com.example.backend.entity.RefundStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Service xử lý nghiệp vụ Hủy phòng & Hoàn tiền.
 */
public interface CancellationService {

    // ── Danh sách & chi tiết ────────────────────────────────────────────

    /**
     * Lấy danh sách yêu cầu hủy phòng với filter.
     *
     * @param refundStatus lọc theo trạng thái hoàn tiền (null = tất cả)
     * @param keyword      tìm theo mã đặt phòng, tên, sđt
     * @param from         lọc từ ngày hủy (yyyy-MM-dd)
     * @param to           lọc đến ngày hủy (yyyy-MM-dd)
     * @param page         trang (0-based)
     * @param size         kích thước trang
     */
    PagedResponse<CancellationResponse> getCancellations(
            RefundStatus refundStatus,
            String keyword,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    );

    /**
     * Lấy chi tiết một yêu cầu hủy theo id booking.
     */
    CancellationResponse getCancellationById(Long bookingId);

    // ── Thống kê ────────────────────────────────────────────────────────

    /** Thống kê tổng quan (total, pending, refunded, rejected, totalRefundAmount) */
    CancellationStatsResponse getStats();

    // ── Xử lý hoàn tiền ─────────────────────────────────────────────────

    /**
     * Admin xử lý: duyệt hoàn tiền hoặc từ chối.
     *
     * @param bookingId id booking đã CANCELLED
     * @param request   { status: REFUNDED | REJECTED, note: "..." }
     */
    CancellationResponse processRefund(Long bookingId, ProcessRefundRequest request);

    // ── Hủy phòng từ admin ───────────────────────────────────────────────

    /**
     * Admin chủ động hủy một booking (bất kể trạng thái hiện tại,
     * trừ CHECKED_IN / CHECKED_OUT) và tự động tính refund theo policy.
     *
     * @param bookingId id booking cần hủy
     * @param request   lý do hủy
     */
    CancellationResponse cancelBookingByAdmin(Long bookingId, CancelBookingRequest request);

    // ── Xóa ─────────────────────────────────────────────────────────────

    /**
     * Xóa một bản ghi hủy phòng (chỉ xóa khi đã REFUNDED hoặc REJECTED).
     */
    void deleteCancellation(Long bookingId);

    // ── Chính sách hoàn tiền ─────────────────────────────────────────────

    /** Lấy toàn bộ danh sách chính sách */
    List<CancellationPolicy> getPolicies();

    /** Thay thế toàn bộ danh sách chính sách */
    List<CancellationPolicy> savePolicies(List<PolicyRequest> requests);

    /** Thêm một chính sách */
    CancellationPolicy addPolicy(PolicyRequest request);

    /** Sửa một chính sách */
    CancellationPolicy updatePolicy(Long policyId, PolicyRequest request);

    /** Xóa một chính sách */
    void deletePolicy(Long policyId);
}
