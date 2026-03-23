package com.example.backend.repository;

import com.example.backend.entity.Review;
import com.example.backend.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /* ── Kiểm tra booking đã có review chưa ── */
    boolean existsByBookingId(Long bookingId);

    /* ── Lấy review của 1 booking ── */
    Optional<Review> findByBookingId(Long bookingId);

    /* ── Review của một user (đã đăng nhập) ── */
    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /* ── Review đã được duyệt của một loại phòng ── */
    Page<Review> findByBookingRoomTypeAndStatusOrderByCreatedAtDesc(
        String roomType, ReviewStatus status, Pageable pageable
    );

    /* ── Tất cả review APPROVED (public, có lọc sao + keyword) ── */
    @Query("""
        SELECT r FROM Review r
        JOIN r.booking b
        WHERE r.status = 'APPROVED'
          AND (:rating  IS NULL OR r.rating = :rating)
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(b.guestName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.title)     LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.comment)   LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY r.createdAt DESC
        """)
    Page<Review> searchApprovedReviews(
        @Param("rating")  Integer rating,
        @Param("keyword") String  keyword,
        Pageable pageable
    );

    /* ════════════════════════════════════════════════════
       ADMIN – Tìm kiếm nâng cao
    ════════════════════════════════════════════════════ */
    @Query("""
        SELECT r FROM Review r
        JOIN r.booking b
        WHERE (:status  IS NULL OR r.status = :status)
          AND (:rating  IS NULL OR r.rating = :rating)
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(b.guestName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(b.guestEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.title)      LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.comment)    LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY r.createdAt DESC
        """)
    Page<Review> searchReviews(
        @Param("status")  ReviewStatus status,
        @Param("rating")  Integer      rating,
        @Param("keyword") String       keyword,
        Pageable pageable
    );

    /* ════════════════════════════════════════════════════
       STATS QUERIES
    ════════════════════════════════════════════════════ */

    long countByStatus(ReviewStatus status);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.replyText IS NOT NULL AND r.replyText <> ''")
    long countWithReply();

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.status = 'APPROVED'")
    double avgOverallRating();

    /* Đếm số review theo từng mức sao (chỉ tính APPROVED) */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.status = 'APPROVED' AND r.rating = :star")
    long countApprovedByStar(@Param("star") int star);

    /* Avg từng tiêu chí (chỉ APPROVED, loại null) */
    @Query("SELECT COALESCE(AVG(r.roomRating),         0.0) FROM Review r WHERE r.status = 'APPROVED' AND r.roomRating         IS NOT NULL")
    double avgRoomRating();

    @Query("SELECT COALESCE(AVG(r.serviceRating),      0.0) FROM Review r WHERE r.status = 'APPROVED' AND r.serviceRating      IS NOT NULL")
    double avgServiceRating();

    @Query("SELECT COALESCE(AVG(r.locationRating),     0.0) FROM Review r WHERE r.status = 'APPROVED' AND r.locationRating     IS NOT NULL")
    double avgLocationRating();

    @Query("SELECT COALESCE(AVG(r.cleanlinessRating),  0.0) FROM Review r WHERE r.status = 'APPROVED' AND r.cleanlinessRating  IS NOT NULL")
    double avgCleanlinessRating();

    @Query("SELECT COALESCE(AVG(r.amenitiesRating),    0.0) FROM Review r WHERE r.status = 'APPROVED' AND r.amenitiesRating    IS NOT NULL")
    double avgAmenitiesRating();

    @Query("SELECT COALESCE(AVG(r.valueRating),        0.0) FROM Review r WHERE r.status = 'APPROVED' AND r.valueRating        IS NOT NULL")
    double avgValueRating();
}
