package com.example.backend.repository;

import com.example.backend.entity.GroupBookingRoom;
import com.example.backend.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupBookingRoomRepository extends JpaRepository<GroupBookingRoom, Long> {

    List<GroupBookingRoom> findByGroupBookingId(Long groupBookingId);

    List<GroupBookingRoom> findByGroupBookingIdAndStatus(Long groupBookingId, RoomStatus status);

    Long countByGroupBookingId(Long groupBookingId);
}
