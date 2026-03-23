package com.example.backend.service.impl;

import com.example.backend.dto.request.ReviewReplyRequest;
import com.example.backend.dto.request.ReviewRequest;
import com.example.backend.dto.request.UpdateReviewStatusRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.ReviewResponse;
import com.example.backend.dto.response.ReviewStatsResponse;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository  reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository    userRepository;

    /* ══════════════════════════════════════════════════
       KHÁCH HÀNG – GỬI ĐÁNH GIÁ
    ══════════════════════════════════════════════════ */

    @Override
    @Transactional
    public ReviewResponse createReview(ReviewRequest req, String userEmail) {

        // 1. Lấy booking
        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy booking #" + req.getBookingId()));

        // 2. Chỉ cho phép đánh giá khi đã check-out
        if (booking.getStatus() != BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException(
                    "Chỉ có thể đánh giá sau khi đã trả phòng (CHECKED_OUT)");
        }

        // 3. Một booking chỉ được 1 review
        if (reviewRepository.existsByBookingId(req.getBookingId())) {
            throw new IllegalStateException(
                    "Booking này đã được đánh giá trước đó");
        }

        // 4. Xác minh quyền sở hữu booking (nếu user đã đăng nhập)
        User user = null;
        if (userEmail != null) {
            user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null && booking.getUser() != null
                    && !booking.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("Bạn không phải chủ booking này");
            }
        }

        // 5. Lưu review
        Review review = Review.builder()
                .booking(booking)
                .user(user)
                .rating(req.getRating())
                .title(req.getTitle())
                .comment(req.getComment())
                .roomRating(req.getRoomRating())
                .serviceRating(req.getServiceRating())
                .locationRating(req.getLocationRating())
                .cleanlinessRating(req.getCleanlinessRating())
                .amenitiesRating(req.getAmenitiesRating())
                .valueRating(req.getValueRating())
                .status(ReviewStatus.PENDING)
                .build();

        return ReviewResponse.from(reviewRepository.save(review));
    }

    /* ══════════════════════════════════════════════════
       KHÁCH HÀNG – XEM REVIEW
    ══════════════════════════════════════════════════ */

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getByBookingId(Long bookingId) {
        Review review = reviewRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chưa có đánh giá nào cho booking #" + bookingId));
        return ReviewResponse.from(review);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getMyReviews(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> p = reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return toPagedResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getApprovedByRoomType(String roomType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> p = reviewRepository
                .findByBookingRoomTypeAndStatusOrderByCreatedAtDesc(
                        roomType, ReviewStatus.APPROVED, pageable);
        return toPagedResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getApprovedReviews(Integer rating, String keyword, int page, int size) {
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> p = reviewRepository.searchApprovedReviews(rating, kw, pageable);
        return toPagedResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewStatsResponse getPublicStats() {
        long approved = reviewRepository.countByStatus(ReviewStatus.APPROVED);
        long cnt5 = reviewRepository.countApprovedByStar(5);
        long cnt4 = reviewRepository.countApprovedByStar(4);
        long cnt3 = reviewRepository.countApprovedByStar(3);
        long cnt2 = reviewRepository.countApprovedByStar(2);
        long cnt1 = reviewRepository.countApprovedByStar(1);
        double pctBase = (approved > 0) ? (double) approved : 1.0;

        return ReviewStatsResponse.builder()
                .totalReviews(approved)
                .approvedCount(approved)
                .pendingCount(0)
                .rejectedCount(0)
                .withReplyCount(0)
                .responseRate(0)
                .overallAvg(round2(reviewRepository.avgOverallRating()))
                .pct5Star(round1(cnt5 * 100.0 / pctBase))
                .pct4Star(round1(cnt4 * 100.0 / pctBase))
                .pct3Star(round1(cnt3 * 100.0 / pctBase))
                .pct2Star(round1(cnt2 * 100.0 / pctBase))
                .pct1Star(round1(cnt1 * 100.0 / pctBase))
                .avgRoom(round2(reviewRepository.avgRoomRating()))
                .avgService(round2(reviewRepository.avgServiceRating()))
                .avgLocation(round2(reviewRepository.avgLocationRating()))
                .avgCleanliness(round2(reviewRepository.avgCleanlinessRating()))
                .avgAmenities(round2(reviewRepository.avgAmenitiesRating()))
                .avgValue(round2(reviewRepository.avgValueRating()))
                .build();
    }

    /* ══════════════════════════════════════════════════
       ADMIN – TÌM KIẾM
    ══════════════════════════════════════════════════ */

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> adminSearch(
            ReviewStatus status, Integer rating, String keyword, int page, int size) {

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Pageable pageable = PageRequest.of(page, size);
        Page<Review> p = reviewRepository.searchReviews(status, rating, kw, pageable);
        return toPagedResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getById(Long id) {
        return ReviewResponse.from(findOrThrow(id));
    }

    /* ══════════════════════════════════════════════════
       ADMIN – CẬP NHẬT TRẠNG THÁI
    ══════════════════════════════════════════════════ */

    @Override
    @Transactional
    public ReviewResponse updateStatus(Long id, UpdateReviewStatusRequest req) {
        Review review = findOrThrow(id);

        if (review.getStatus() == req.getStatus()) {
            throw new IllegalStateException(
                    "Review đã ở trạng thái " + req.getStatus().name());
        }

        review.setStatus(req.getStatus());
        log.info("Admin cập nhật trạng thái review #{} → {}", id, req.getStatus());
        return ReviewResponse.from(reviewRepository.save(review));
    }

    /* ══════════════════════════════════════════════════
       ADMIN – PHẢN HỒI
    ══════════════════════════════════════════════════ */

    @Override
    @Transactional
    public ReviewResponse reply(Long id, ReviewReplyRequest req) {
        Review review = findOrThrow(id);

        review.setReplyText(req.getReplyText());
        review.setRepliedAt(LocalDateTime.now());

        log.info("Admin gửi/cập nhật phản hồi cho review #{}", id);
        return ReviewResponse.from(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public ReviewResponse deleteReply(Long id) {
        Review review = findOrThrow(id);

        if (review.getReplyText() == null || review.getReplyText().isBlank()) {
            throw new IllegalStateException("Review #" + id + " chưa có phản hồi nào");
        }

        review.setReplyText(null);
        review.setRepliedAt(null);

        log.info("Admin xóa phản hồi của review #{}", id);
        return ReviewResponse.from(reviewRepository.save(review));
    }

    /* ══════════════════════════════════════════════════
       ADMIN – XÓA REVIEW
    ══════════════════════════════════════════════════ */

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = findOrThrow(id);
        reviewRepository.delete(review);
        log.info("Admin đã xóa review #{}", id);
    }

    /* ══════════════════════════════════════════════════
       THỐNG KÊ
    ══════════════════════════════════════════════════ */

    @Override
    @Transactional(readOnly = true)
    public ReviewStatsResponse getStats() {
        long total    = reviewRepository.count();
        long pending  = reviewRepository.countByStatus(ReviewStatus.PENDING);
        long approved = reviewRepository.countByStatus(ReviewStatus.APPROVED);
        long rejected = reviewRepository.countByStatus(ReviewStatus.REJECTED);
        long withReply = reviewRepository.countWithReply();

        double responseRate = (total > 0)
                ? Math.round((withReply * 100.0 / total) * 10.0) / 10.0
                : 0.0;

        /* Phân phối theo sao (dựa trên approved) */
        long cnt5 = reviewRepository.countApprovedByStar(5);
        long cnt4 = reviewRepository.countApprovedByStar(4);
        long cnt3 = reviewRepository.countApprovedByStar(3);
        long cnt2 = reviewRepository.countApprovedByStar(2);
        long cnt1 = reviewRepository.countApprovedByStar(1);
        double pctBase = (approved > 0) ? (double) approved : 1.0;

        return ReviewStatsResponse.builder()
                .totalReviews(total)
                .pendingCount(pending)
                .approvedCount(approved)
                .rejectedCount(rejected)
                .withReplyCount(withReply)
                .responseRate(responseRate)
                .overallAvg(round2(reviewRepository.avgOverallRating()))
                .pct5Star(round1(cnt5 * 100.0 / pctBase))
                .pct4Star(round1(cnt4 * 100.0 / pctBase))
                .pct3Star(round1(cnt3 * 100.0 / pctBase))
                .pct2Star(round1(cnt2 * 100.0 / pctBase))
                .pct1Star(round1(cnt1 * 100.0 / pctBase))
                .avgRoom(round2(reviewRepository.avgRoomRating()))
                .avgService(round2(reviewRepository.avgServiceRating()))
                .avgLocation(round2(reviewRepository.avgLocationRating()))
                .avgCleanliness(round2(reviewRepository.avgCleanlinessRating()))
                .avgAmenities(round2(reviewRepository.avgAmenitiesRating()))
                .avgValue(round2(reviewRepository.avgValueRating()))
                .build();
    }

    /* ══════════════════════════════════════════════════
       HELPERS
    ══════════════════════════════════════════════════ */

    private Review findOrThrow(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy review #" + id));
    }

    private PagedResponse<ReviewResponse> toPagedResponse(Page<Review> p) {
        List<ReviewResponse> content = p.getContent()
                .stream()
                .map(ReviewResponse::from)
                .toList();

        return PagedResponse.<ReviewResponse>builder()
                .content(content)
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }

    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }
    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
