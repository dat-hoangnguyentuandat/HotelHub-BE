package com.example.backend.controller;

import com.example.backend.dto.request.ReviewReplyRequest;
import com.example.backend.dto.request.ReviewRequest;
import com.example.backend.dto.request.UpdateReviewStatusRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.ReviewResponse;
import com.example.backend.dto.response.ReviewStatsResponse;
import com.example.backend.entity.ReviewStatus;
import com.example.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /* ═══════════════════════════════════════════════════════════════
       PUBLIC / KHÁCH HÀNG ENDPOINTS
    ═══════════════════════════════════════════════════════════════ */

    /**
     * POST /api/reviews
     * Gửi đánh giá mới (yêu cầu booking đã CHECKED_OUT).
     * Khách vãng lai (không token) vẫn được gửi nếu là chủ booking.
     */
    @PostMapping("/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        String userEmail = (principal != null) ? principal.getUsername() : null;
        ReviewResponse response = reviewService.createReview(request, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/reviews/booking/{bookingId}
     * Xem review của một booking cụ thể (khách xem lại đánh giá của mình).
     */
    @GetMapping("/reviews/booking/{bookingId}")
    public ResponseEntity<ReviewResponse> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(reviewService.getByBookingId(bookingId));
    }

    /**
     * GET /api/reviews/room/{roomType}?page=0&size=10
     * Xem review đã duyệt của một loại phòng (trang public cho khách đặt phòng).
     */
    @GetMapping("/reviews/room/{roomType}")
    public ResponseEntity<PagedResponse<ReviewResponse>> getApprovedByRoom(
            @PathVariable String roomType,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getApprovedByRoomType(roomType, page, size));
    }

    /**
     * GET /api/reviews/my?page=0&size=10
     * Danh sách đánh giá của tôi (user đã đăng nhập).
     */
    @GetMapping("/reviews/my")
    public ResponseEntity<PagedResponse<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                reviewService.getMyReviews(principal.getUsername(), page, size));
    }

    /* ═══════════════════════════════════════════════════════════════
       ADMIN ENDPOINTS
    ═══════════════════════════════════════════════════════════════ */

    /**
     * GET /api/admin/reviews?status=PENDING&rating=5&keyword=tốt&page=0&size=20
     * Tìm kiếm / lọc tất cả review.
     */
    @GetMapping("/admin/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<ReviewResponse>> adminSearch(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) Integer      rating,
            @RequestParam(required = false) String       keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                reviewService.adminSearch(status, rating, keyword, page, size));
    }

    /**
     * GET /api/admin/reviews/stats
     * Thống kê tổng hợp điểm đánh giá (overview card trên UI).
     */
    @GetMapping("/admin/reviews/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewStatsResponse> getStats() {
        return ResponseEntity.ok(reviewService.getStats());
    }

    /**
     * GET /api/admin/reviews/{id}
     * Chi tiết một review.
     */
    @GetMapping("/admin/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getById(id));
    }

    /**
     * PATCH /api/admin/reviews/{id}/status
     * Duyệt hoặc từ chối review.
     * Body: { "status": "APPROVED" | "REJECTED" }
     */
    @PatchMapping("/admin/reviews/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewStatusRequest request
    ) {
        return ResponseEntity.ok(reviewService.updateStatus(id, request));
    }

    /**
     * POST /api/admin/reviews/{id}/reply
     * Gửi hoặc cập nhật phản hồi của khách sạn.
     * Body: { "replyText": "..." }
     */
    @PostMapping("/admin/reviews/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> reply(
            @PathVariable Long id,
            @Valid @RequestBody ReviewReplyRequest request
    ) {
        return ResponseEntity.ok(reviewService.reply(id, request));
    }

    /**
     * DELETE /api/admin/reviews/{id}/reply
     * Xóa phản hồi của khách sạn.
     */
    @DeleteMapping("/admin/reviews/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewResponse> deleteReply(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.deleteReply(id));
    }

    /**
     * DELETE /api/admin/reviews/{id}
     * Xóa cứng review.
     */
    @DeleteMapping("/admin/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(Map.of(
                "message", "Đã xóa review #" + id + " thành công"));
    }
}
