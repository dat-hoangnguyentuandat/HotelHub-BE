package com.example.backend.service;

import com.example.backend.dto.request.GroupBookingRequest;
import com.example.backend.dto.request.GroupBookingRoomRequest;
import com.example.backend.dto.request.UpdateGroupBookingStatusRequest;
import com.example.backend.dto.request.UpdateRoomStatusRequest;
import com.example.backend.dto.response.GroupBookingResponse;
import com.example.backend.dto.response.GroupBookingRoomResponse;
import com.example.backend.entity.GroupBookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface GroupBookingService {

    GroupBookingResponse createGroupBooking(GroupBookingRequest request);

    GroupBookingResponse getGroupBookingById(Long id);

    Page<GroupBookingResponse> getAllGroupBookings(Pageable pageable);

    Page<GroupBookingResponse> searchGroupBookings(
            String groupName,
            GroupBookingStatus status,
            LocalDate checkIn,
            LocalDate checkOut,
            Pageable pageable
    );

    GroupBookingResponse updateGroupBooking(Long id, GroupBookingRequest request);

    GroupBookingResponse updateGroupBookingStatus(Long id, UpdateGroupBookingStatusRequest request);

    void deleteGroupBooking(Long id);

    GroupBookingRoomResponse addRoomToGroup(Long groupBookingId, GroupBookingRoomRequest request);

    GroupBookingRoomResponse updateRoom(Long roomId, GroupBookingRoomRequest request);

    GroupBookingRoomResponse updateRoomStatus(Long roomId, UpdateRoomStatusRequest request);

    void deleteRoom(Long roomId);

    List<GroupBookingRoomResponse> getRoomsByGroupBookingId(Long groupBookingId);
}
