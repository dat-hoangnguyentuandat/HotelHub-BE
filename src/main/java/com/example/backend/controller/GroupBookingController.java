package com.example.backend.controller;

import com.example.backend.dto.request.GroupBookingRequest;
import com.example.backend.dto.request.GroupBookingRoomRequest;
import com.example.backend.dto.request.UpdateGroupBookingStatusRequest;
import com.example.backend.dto.request.UpdateRoomStatusRequest;
import com.example.backend.dto.response.GroupBookingResponse;
import com.example.backend.dto.response.GroupBookingRoomResponse;
import com.example.backend.entity.GroupBookingStatus;
import com.example.backend.service.GroupBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/group-bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GroupBookingController {

    private final GroupBookingService groupBookingService;

    @PostMapping
    public ResponseEntity<GroupBookingResponse> createGroupBooking(@Valid @RequestBody GroupBookingRequest request) {
        GroupBookingResponse response = groupBookingService.createGroupBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupBookingResponse> getGroupBookingById(@PathVariable Long id) {
        GroupBookingResponse response = groupBookingService.getGroupBookingById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<GroupBookingResponse>> getAllGroupBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GroupBookingResponse> response = groupBookingService.getAllGroupBookings(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<GroupBookingResponse>> searchGroupBookings(
            @RequestParam(required = false) String groupName,
            @RequestParam(required = false) GroupBookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GroupBookingResponse> response = groupBookingService.searchGroupBookings(
                groupName, status, checkIn, checkOut, pageable
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupBookingResponse> updateGroupBooking(
            @PathVariable Long id,
            @Valid @RequestBody GroupBookingRequest request
    ) {
        GroupBookingResponse response = groupBookingService.updateGroupBooking(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<GroupBookingResponse> updateGroupBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateGroupBookingStatusRequest request
    ) {
        GroupBookingResponse response = groupBookingService.updateGroupBookingStatus(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroupBooking(@PathVariable Long id) {
        groupBookingService.deleteGroupBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupBookingId}/rooms")
    public ResponseEntity<GroupBookingRoomResponse> addRoomToGroup(
            @PathVariable Long groupBookingId,
            @Valid @RequestBody GroupBookingRoomRequest request
    ) {
        GroupBookingRoomResponse response = groupBookingService.addRoomToGroup(groupBookingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupBookingId}/rooms")
    public ResponseEntity<List<GroupBookingRoomResponse>> getRoomsByGroupBookingId(@PathVariable Long groupBookingId) {
        List<GroupBookingRoomResponse> response = groupBookingService.getRoomsByGroupBookingId(groupBookingId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<GroupBookingRoomResponse> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody GroupBookingRoomRequest request
    ) {
        GroupBookingRoomResponse response = groupBookingService.updateRoom(roomId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/rooms/{roomId}/status")
    public ResponseEntity<GroupBookingRoomResponse> updateRoomStatus(
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateRoomStatusRequest request
    ) {
        GroupBookingRoomResponse response = groupBookingService.updateRoomStatus(roomId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        groupBookingService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
}
