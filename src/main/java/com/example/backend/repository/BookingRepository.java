package com.example.backend.repository;

import com.example.backend.entity.Booking;
import com.example.backend.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /* ── Booking của một user cụ thể ── */
    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /* ── Tìm theo trạng thái (admin) ── */
    Page<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status, Pageable pageable);

    /* ── Tìm theo số điện thoại khách ── */
    List<Booking> findByGuestPhoneOrderByCreatedAtDesc(String guestPhone);

    /* ── Kiểm tra phòng bị trùng ngày (loại trừ CANCELLED) ── */
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.roomType = :roomType
          AND b.status NOT IN (com.example.backend.entity.BookingStatus.CANCELLED)
          AND b.checkIn  < :checkOut
          AND b.checkOut > :checkIn
        """)
    boolean existsConflict(
        @Param("roomType")  String    roomType,
        @Param("checkIn")   LocalDate checkIn,
        @Param("checkOut")  LocalDate checkOut
    );

    /* ══════════════════════════════════════════════════════════
       DASHBOARD QUERIES
    ══════════════════════════════════════════════════════════ */

    /**
     * Tổng doanh thu trong khoảng thời gian (chỉ tính booking CHECKED_IN hoặc CHECKED_OUT).
     * Lọc theo checkIn (ngày khách bắt đầu ở) để phản ánh doanh thu theo kỳ lưu trú.
     */
    @Query("""
        SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b
        WHERE b.status IN (
            com.example.backend.entity.BookingStatus.CHECKED_IN,
            com.example.backend.entity.BookingStatus.CHECKED_OUT
        )
          AND b.checkIn >= :from
          AND b.checkIn <  :to
        """)
    BigDecimal sumRevenueBetween(
        @Param("from") LocalDate from,
        @Param("to")   LocalDate to
    );

    /**
     * Đếm lượt check-in (booking CHECKED_IN hoặc CHECKED_OUT) trong khoảng thời gian.
     * Lọc theo checkIn để đếm đúng số lượt khách nhận phòng.
     */
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.status IN (
            com.example.backend.entity.BookingStatus.CHECKED_IN,
            com.example.backend.entity.BookingStatus.CHECKED_OUT
        )
          AND b.checkIn >= :from
          AND b.checkIn <  :to
        """)
    long countCheckedInBetween(
        @Param("from") LocalDate from,
        @Param("to")   LocalDate to
    );

    /**
     * Đếm số phòng đang bị chiếm dụng hôm nay:
     * booking CONFIRMED hoặc CHECKED_IN mà checkIn <= today < checkOut.
     */
    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.status IN (
            com.example.backend.entity.BookingStatus.CONFIRMED,
            com.example.backend.entity.BookingStatus.CHECKED_IN
        )
          AND b.checkIn  <= :today
          AND b.checkOut >  :today
        """)
    long countOccupiedRoomsToday(@Param("today") LocalDate today);

    /**
     * Đếm booking theo trạng thái.
     */
    long countByStatus(BookingStatus status);

    /**
     * Doanh thu theo từng loại phòng trong khoảng thời gian.
     * Lọc theo checkIn để phản ánh doanh thu theo kỳ lưu trú.
     * Trả về: [roomType, totalRevenue]
     */
    @Query("""
        SELECT b.roomType, COALESCE(SUM(b.totalAmount), 0)
        FROM Booking b
        WHERE b.status IN (
            com.example.backend.entity.BookingStatus.CHECKED_IN,
            com.example.backend.entity.BookingStatus.CHECKED_OUT
        )
          AND b.checkIn >= :from
          AND b.checkIn <  :to
        GROUP BY b.roomType
        ORDER BY SUM(b.totalAmount) DESC
        """)
    List<Object[]> revenueByRoomType(
        @Param("from") LocalDate from,
        @Param("to")   LocalDate to
    );

    /**
     * 5 giao dịch gần nhất (tất cả trạng thái trừ CANCELLED),
     * sắp xếp theo ngày tạo mới nhất.
     */
    @Query("""
        SELECT b FROM Booking b
        WHERE b.status != com.example.backend.entity.BookingStatus.CANCELLED
        ORDER BY b.createdAt DESC
        """)
    List<Booking> findTop5RecentTransactions(Pageable pageable);
}
