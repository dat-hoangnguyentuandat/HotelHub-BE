package com.example.backend.service.impl;

import com.example.backend.dto.request.GroupBookingRequest;
import com.example.backend.dto.request.GroupBookingRoomRequest;
import com.example.backend.dto.request.UpdateGroupBookingStatusRequest;
import com.example.backend.dto.request.UpdateRoomStatusRequest;
import com.example.backend.dto.response.GroupBookingResponse;
import com.example.backend.dto.response.GroupBookingRoomResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.GroupBookingRepository;
import com.example.backend.repository.GroupBookingRoomRepository;
import com.example.backend.service.GroupBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupBookingServiceImpl implements GroupBookingService {

    private final GroupBookingRepository groupBookingRepository;
    private final GroupBookingRoomRepository groupBookingRoomRepository;

    @Override
    @Transactional
    public GroupBookingResponse createGroupBooking(GroupBookingRequest request) {
        validateDates(request.getCheckIn(), request.getCheckOut());

        GroupBooking groupBooking = GroupBooking.builder()
                .groupName(request.getGroupName())
                .contactPerson(request.getContactPerson())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .totalRooms(request.getTotalRooms())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .status(GroupBookingStatus.PENDING)
                .note(request.getNote())
                .build();

        GroupBooking saved = groupBookingRepository.save(groupBooking);

        if (request.getRooms() != null && !request.getRooms().isEmpty()) {
            for (GroupBookingRoomRequest roomRequest : request.getRooms()) {
                GroupBookingRoom room = GroupBookingRoom.builder()
                        .groupBooking(saved)
                        .guestName(roomRequest.getGuestName())
                        .roomType(roomRequest.getRoomType())
                        .roomNumber(roomRequest.getRoomNumber())
                        .status(RoomStatus.BOOKED)
                        .price(roomRequest.getPrice())
                        .note(roomRequest.getNote())
                        .build();
                saved.getRooms().add(room);
            }
            saved = groupBookingRepository.save(saved);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupBookingResponse getGroupBookingById(Long id) {
        GroupBooking groupBooking = groupBookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đoàn khách với ID: " + id));
        
        // Force load rooms to avoid lazy initialization exception
        groupBooking.getRooms().size();
        
        return mapToResponse(groupBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupBookingResponse> getAllGroupBookings(Pageable pageable) {
        Page<GroupBooking> page = groupBookingRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        // Force load rooms for each group booking
        page.getContent().forEach(gb -> gb.getRooms().size());
        
        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupBookingResponse> searchGroupBookings(
            String groupName,
            GroupBookingStatus status,
            LocalDate checkIn,
            LocalDate checkOut,
            Pageable pageable
    ) {
        Page<GroupBooking> page = groupBookingRepository.searchGroupBookings(
                groupName, status, checkIn, checkOut, pageable
        );
        
        // Force load rooms for each group booking
        page.getContent().forEach(gb -> gb.getRooms().size());
        
        return page.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public GroupBookingResponse updateGroupBooking(Long id, GroupBookingRequest request) {
        GroupBooking groupBooking = groupBookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đoàn khách với ID: " + id));

        validateDates(request.getCheckIn(), request.getCheckOut());

        groupBooking.setGroupName(request.getGroupName());
        groupBooking.setContactPerson(request.getContactPerson());
        groupBooking.setContactPhone(request.getContactPhone());
        groupBooking.setContactEmail(request.getContactEmail());
        groupBooking.setTotalRooms(request.getTotalRooms());
        groupBooking.setCheckIn(request.getCheckIn());
        groupBooking.setCheckOut(request.getCheckOut());
        groupBooking.setNote(request.getNote());

        GroupBooking updated = groupBookingRepository.save(groupBooking);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public GroupBookingResponse updateGroupBookingStatus(Long id, UpdateGroupBookingStatusRequest request) {
        GroupBooking groupBooking = groupBookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đoàn khách với ID: " + id));

        groupBooking.setStatus(request.getStatus());
        GroupBooking updated = groupBookingRepository.save(groupBooking);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteGroupBooking(Long id) {
        if (!groupBookingRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy đoàn khách với ID: " + id);
        }
        groupBookingRepository.deleteById(id);
    }

    @Override
    @Transactional
    public GroupBookingRoomResponse addRoomToGroup(Long groupBookingId, GroupBookingRoomRequest request) {
        GroupBooking groupBooking = groupBookingRepository.findById(groupBookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đoàn khách với ID: " + groupBookingId));

        GroupBookingRoom room = GroupBookingRoom.builder()
                .groupBooking(groupBooking)
                .guestName(request.getGuestName())
                .roomType(request.getRoomType())
                .roomNumber(request.getRoomNumber())
                .status(RoomStatus.BOOKED)
                .price(request.getPrice())
                .note(request.getNote())
                .build();

        GroupBookingRoom saved = groupBookingRoomRepository.save(room);
        return mapToRoomResponse(saved);
    }

    @Override
    @Transactional
    public GroupBookingRoomResponse updateRoom(Long roomId, GroupBookingRoomRequest request) {
        GroupBookingRoom room = groupBookingRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + roomId));

        room.setGuestName(request.getGuestName());
        room.setRoomType(request.getRoomType());
        room.setRoomNumber(request.getRoomNumber());
        room.setPrice(request.getPrice());
        room.setNote(request.getNote());

        GroupBookingRoom updated = groupBookingRoomRepository.save(room);
        return mapToRoomResponse(updated);
    }

    @Override
    @Transactional
    public GroupBookingRoomResponse updateRoomStatus(Long roomId, UpdateRoomStatusRequest request) {
        GroupBookingRoom room = groupBookingRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + roomId));

        room.setStatus(request.getStatus());
        GroupBookingRoom updated = groupBookingRoomRepository.save(room);
        return mapToRoomResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        if (!groupBookingRoomRepository.existsById(roomId)) {
            throw new RuntimeException("Không tìm thấy phòng với ID: " + roomId);
        }
        groupBookingRoomRepository.deleteById(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupBookingRoomResponse> getRoomsByGroupBookingId(Long groupBookingId) {
        return groupBookingRoomRepository.findByGroupBookingId(groupBookingId)
                .stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isAfter(checkOut)) {
            throw new RuntimeException("Ngày check-in phải trước ngày check-out");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày check-in không được là ngày trong quá khứ");
        }
    }

    private GroupBookingResponse mapToResponse(GroupBooking groupBooking) {
        List<GroupBookingRoomResponse> rooms = groupBooking.getRooms().stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());

        return GroupBookingResponse.builder()
                .id(groupBooking.getId())
                .groupName(groupBooking.getGroupName())
                .contactPerson(groupBooking.getContactPerson())
                .contactPhone(groupBooking.getContactPhone())
                .contactEmail(groupBooking.getContactEmail())
                .totalRooms(groupBooking.getTotalRooms())
                .checkIn(groupBooking.getCheckIn())
                .checkOut(groupBooking.getCheckOut())
                .status(groupBooking.getStatus())
                .note(groupBooking.getNote())
                .rooms(rooms)
                .createdAt(groupBooking.getCreatedAt())
                .updatedAt(groupBooking.getUpdatedAt())
                .build();
    }

    private GroupBookingRoomResponse mapToRoomResponse(GroupBookingRoom room) {
        return GroupBookingRoomResponse.builder()
                .id(room.getId())
                .guestName(room.getGuestName())
                .roomType(room.getRoomType())
                .roomNumber(room.getRoomNumber())
                .status(room.getStatus())
                .price(room.getPrice())
                .note(room.getNote())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
