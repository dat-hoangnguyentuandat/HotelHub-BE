package com.example.backend.service;

import com.example.backend.dto.request.ReviewReplyRequest;
import com.example.backend.dto.request.ReviewRequest;
import com.example.backend.dto.request.UpdateReviewStatusRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.ReviewResponse;
import com.example.backend.dto.response.ReviewStatsResponse;
import com.example.backend.entity.ReviewStatus;

public interface ReviewService {

    /* ══════════════════════════════════════════════════
       KHÁCH HÀNG
    ══════════════════════════════════════════════════ */

    /** Gửi đánh giá mới sau khi check-out (khách vãng lai + user). */
    ReviewResponse createReview(ReviewRequest request, String userEmail);

    /** Xem review theo booking (khách xem lại review của mình). */
    ReviewResponse getByBookingId(Long bookingId);

    /** Danh sách review của tôi (user đã đăng nhập). */
    PagedResponse<ReviewResponse> getMyReviews(String userEmail, int page, int size);

    /** Review đã duyệt của một loại phòng (public). */
    PagedResponse<ReviewResponse> getApprovedByRoomType(String roomType, int page, int size);

    /** Tất cả review đã duyệt (public – trang đánh giá user). */
    PagedResponse<ReviewResponse> getApprovedReviews(Integer rating, String keyword, int page, int size);

    /** Stats công khai (chỉ dựa trên APPROVED). */
    ReviewStatsResponse getPublicStats();

    /* ══════════════════════════════════════════════════
       ADMIN
    ══════════════════════════════════════════════════ */

    /** Admin tìm kiếm / lọc tất cả review. */
    PagedResponse<ReviewResponse> adminSearch(
        ReviewStatus status, Integer rating, String keyword, int page, int size
    );

    /** Admin xem chi tiết một review. */
    ReviewResponse getById(Long id);

    /** Admin cập nhật trạng thái (APPROVED / REJECTED). */
    ReviewResponse updateStatus(Long id, UpdateReviewStatusRequest request);

    /** Admin gửi / cập nhật phản hồi. */
    ReviewResponse reply(Long id, ReviewReplyRequest request);

    /** Admin xóa phản hồi. */
    ReviewResponse deleteReply(Long id);

    /** Admin xóa review (chỉ REJECTED hoặc có lý do đặc biệt). */
    void deleteReview(Long id);

    /** Thống kê tổng hợp điểm đánh giá. */
    ReviewStatsResponse getStats();
}
