package com.example.backend.repository;

import com.example.backend.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findAllByOrderByFloorAscRoomNameAsc();

    Page<Room> findByStatusOrderByFloorAscRoomNameAsc(String status, Pageable pageable);

    @Query("""
        SELECT r FROM Room r
        WHERE (:status IS NULL OR r.status = :status)
          AND (:keyword IS NULL OR LOWER(r.roomName) LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(r.roomType) LIKE LOWER(CONCAT('%',:keyword,'%')))
        ORDER BY r.floor ASC, r.roomName ASC
        """)
    Page<Room> search(
        @Param("status")  String status,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
