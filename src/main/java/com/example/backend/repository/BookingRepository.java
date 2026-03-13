package com.example.backend.repository;

import com.example.backend.entity.Booking;
import com.example.backend.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /* Booking của một user cụ thể */
    Page<Booking> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /* Tìm theo trạng thái (admin) */
    Page<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status, Pageable pageable);

    /* Tìm theo số điện thoại khách */
    List<Booking> findByGuestPhoneOrderByCreatedAtDesc(String guestPhone);

    /* Kiểm tra phòng bị trùng ngày (loại trừ CANCELLED) */
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
}
