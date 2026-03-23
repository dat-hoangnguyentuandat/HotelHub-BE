package com.example.backend.repository;

import com.example.backend.entity.ServiceBooking;
import com.example.backend.entity.ServiceBookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceBookingRepository extends JpaRepository<ServiceBooking, Long> {

    @Query("SELECT sb FROM ServiceBooking sb " +
           "LEFT JOIN FETCH sb.service s " +
           "LEFT JOIN FETCH sb.user u " +
           "WHERE (:status IS NULL OR sb.status = :status) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(sb.bookingCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(sb.guestName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(sb.guestPhone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:fromDate IS NULL OR CAST(sb.createdAt AS date) >= :fromDate) " +
           "AND (:toDate IS NULL OR CAST(sb.createdAt AS date) <= :toDate) " +
           "ORDER BY sb.createdAt DESC")
    Page<ServiceBooking> searchBookings(
            @Param("status") ServiceBookingStatus status,
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    List<ServiceBooking> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByStatus(ServiceBookingStatus status);
}
