package com.example.backend.repository;

import com.example.backend.entity.GroupBooking;
import com.example.backend.entity.GroupBookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GroupBookingRepository extends JpaRepository<GroupBooking, Long> {

    Page<GroupBooking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<GroupBooking> findByStatusOrderByCreatedAtDesc(GroupBookingStatus status, Pageable pageable);

    @Query("SELECT gb FROM GroupBooking gb WHERE " +
           "(:groupName IS NULL OR LOWER(gb.groupName) LIKE LOWER(CONCAT('%', :groupName, '%'))) AND " +
           "(:status IS NULL OR gb.status = :status) AND " +
           "(:checkIn IS NULL OR gb.checkIn >= :checkIn) AND " +
           "(:checkOut IS NULL OR gb.checkOut <= :checkOut)")
    Page<GroupBooking> searchGroupBookings(
            @Param("groupName") String groupName,
            @Param("status") GroupBookingStatus status,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            Pageable pageable
    );

    List<GroupBooking> findByCheckInBetween(LocalDate startDate, LocalDate endDate);
}
