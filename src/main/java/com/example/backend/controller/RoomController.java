package com.example.backend.controller;

import com.example.backend.dto.request.RoomRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.RoomResponse;
import com.example.backend.entity.Room;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.RoomRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomRepository roomRepository;

    /* ─────────────────────────────────────────────────────────
       PUBLIC – Danh sách phòng (cho trang booking khách)
    ───────────────────────────────────────────────────────── */
    @GetMapping("/api/rooms")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms() {
        List<RoomResponse> rooms = roomRepository.findAllByOrderByFloorAscRoomNameAsc()
                .stream()
                .filter(r -> !r.isMaintenance())
                .map(RoomResponse::from)
                .toList();
        return ResponseEntity.ok(rooms);
    }

    /* ─────────────────────────────────────────────────────────
       ADMIN – CRUD
    ───────────────────────────────────────────────────────── */

    /** GET /api/admin/rooms?status=Trống&keyword=deluxe&page=0&size=20 */
    @GetMapping("/api/admin/rooms")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<PagedResponse<RoomResponse>> getAllRooms(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<Room> p = roomRepository.search(
                (status  != null && !status.isBlank())  ? status  : null,
                (keyword != null && !keyword.isBlank()) ? keyword : null,
                PageRequest.of(page, size)
        );

        PagedResponse<RoomResponse> body = PagedResponse.<RoomResponse>builder()
                .content(p.getContent().stream().map(RoomResponse::from).toList())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();

        return ResponseEntity.ok(body);
    }

    /** GET /api/admin/rooms/{id} */
    @GetMapping("/api/admin/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng #" + id));
        return ResponseEntity.ok(RoomResponse.from(room));
    }

    /** POST /api/admin/rooms */
    @PostMapping("/api/admin/rooms")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest req) {
        Room room = Room.builder()
                .roomName(req.getRoomName())
                .roomType(req.getRoomType())
                .description(req.getDescription())
                .price(req.getPrice())
                .capacity(req.getCapacity())
                .status(req.getStatus())
                .schedule(req.getSchedule())
                .maintenance(req.isMaintenance())
                .floor(req.getFloor())
                .build();
        room.setAmenitiesList(req.getAmenities());

        Room saved = roomRepository.save(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(RoomResponse.from(saved));
    }

    /** PUT /api/admin/rooms/{id} */
    @PutMapping("/api/admin/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest req
    ) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng #" + id));

        room.setRoomName(req.getRoomName());
        room.setRoomType(req.getRoomType());
        room.setDescription(req.getDescription());
        room.setPrice(req.getPrice());
        room.setCapacity(req.getCapacity());
        room.setStatus(req.getStatus());
        room.setSchedule(req.getSchedule());
        room.setMaintenance(req.isMaintenance());
        room.setFloor(req.getFloor());
        room.setAmenitiesList(req.getAmenities());

        return ResponseEntity.ok(RoomResponse.from(roomRepository.save(room)));
    }

    /** DELETE /api/admin/rooms/{id} */
    @DeleteMapping("/api/admin/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_OWNER')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        if (!roomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy phòng #" + id);
        }
        roomRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
