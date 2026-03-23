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

    /* ── Admin: tìm kiếm kết hợp keyword + status + khoảng ngày ── */
    @Query("""
        SELECT b FROM Booking b
        WHERE (:status IS NULL OR b.status = :status)
          AND (:keyword IS NULL OR :keyword = ''
               OR LOWER(b.guestName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(b.guestPhone) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(b.guestEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(b.roomType)   LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
          AND (:checkInFrom IS NULL  OR b.checkIn  >= :checkInFrom)
          AND (:checkInTo   IS NULL  OR b.checkIn  <= :checkInTo)
        ORDER BY b.createdAt DESC
        """)
    Page<Booking> searchBookings(
        @Param("status")      BookingStatus status,
        @Param("keyword")     String        keyword,
        @Param("checkInFrom") LocalDate     checkInFrom,
        @Param("checkInTo")   LocalDate     checkInTo,
        Pageable pageable
    );

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

    /* ── Kiểm tra trùng lịch loại trừ booking hiện tại (dùng khi update) ── */
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.roomType = :roomType
          AND b.id       != :excludeId
          AND b.status NOT IN (com.example.backend.entity.BookingStatus.CANCELLED)
          AND b.checkIn  < :checkOut
          AND b.checkOut > :checkIn
        """)
    boolean existsConflictExcluding(
        @Param("roomType")  String    roomType,
        @Param("checkIn")   LocalDate checkIn,
        @Param("checkOut")  LocalDate checkOut,
        @Param("excludeId") Long      excludeId
    );

    /* ══════════════════════════════════════════════════════════
       DASHBOARD QUERIES
    ══════════════════════════════════════════════════════════ */

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

    long countByStatus(BookingStatus status);

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

    @Query("""
        SELECT b FROM Booking b
        WHERE b.status != com.example.backend.entity.BookingStatus.CANCELLED
        ORDER BY b.createdAt DESC
        """)
    List<Booking> findTop5RecentTransactions(Pageable pageable);
}
