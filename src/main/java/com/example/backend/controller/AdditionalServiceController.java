package com.example.backend.controller;

import com.example.backend.dto.request.AdditionalServiceRequest;
import com.example.backend.dto.request.ServiceBookingRequest;
import com.example.backend.dto.response.AdditionalServiceResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.ServiceBookingResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.AdditionalServiceRepository;
import com.example.backend.repository.ServiceBookingRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AdditionalServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','HOTEL_OWNER')")
public class AdditionalServiceController {

    private final AdditionalServiceService service;
    private final ServiceBookingRepository serviceBookingRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final UserRepository userRepository;

    /* ═══════════════════════════════════════════════════════════
       PUBLIC – Khách hàng xem danh sách dịch vụ đang bán
       (override PreAuthorize ở class bằng permitAll trong SecurityConfig)
    ═══════════════════════════════════════════════════════════ */

    /**
     * GET /api/services
     * Trả về tất cả dịch vụ ACTIVE (không cần đăng nhập)
     */
    @GetMapping("/api/services")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<AdditionalServiceResponse>> getActiveServices() {
        return ResponseEntity.ok(service.getActiveServices());
    }

    /**
     * GET /api/services/my-bookings
     * Lấy danh sách đặt dịch vụ của user hiện tại (cần đăng nhập)
     */
    @GetMapping("/api/services/my-bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServiceBookingResponse>> getMyServiceBookings(
            @AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername()).orElse(null);
        if (user == null) return ResponseEntity.ok(List.of());
        List<ServiceBooking> bookings = serviceBookingRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());
        List<ServiceBookingResponse> result = bookings.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/services/book
     * Đặt dịch vụ (cần đăng nhập)
     */
    @PostMapping("/api/services/book")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceBookingResponse> bookService(
            @Valid @RequestBody ServiceBookingRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        // Tạo mã đặt dịch vụ
        String bookingCode = generateServiceBookingCode();
        
        // Lấy user
        User user = userRepository.findByEmail(principal.getUsername()).orElse(null);
        
        // Lấy service
        AdditionalService additionalService = additionalServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ"));
        
        // Tạo ServiceBooking entity
        ServiceBooking booking = ServiceBooking.builder()
                .bookingCode(bookingCode)
                .service(additionalService)
                .user(user)
                .quantity(request.getQuantity())
                .guestName(request.getGuestName())
                .guestPhone(request.getGuestPhone())
                .guestEmail(request.getGuestEmail())
                .note(request.getNote())
                .totalAmount(request.getTotalAmount())
                .status(ServiceBookingStatus.PENDING)
                .build();
        
        ServiceBooking saved = serviceBookingRepository.save(booking);
        
        ServiceBookingResponse response = ServiceBookingResponse.builder()
                .id(saved.getId())
                .bookingCode(saved.getBookingCode())
                .serviceId(saved.getService().getId())
                .serviceName(saved.getService().getName())
                .quantity(saved.getQuantity())
                .guestName(saved.getGuestName())
                .guestPhone(saved.getGuestPhone())
                .guestEmail(saved.getGuestEmail())
                .note(saved.getNote())
                .totalAmount(saved.getTotalAmount())
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .message("Đặt dịch vụ thành công! Chúng tôi sẽ liên hệ với bạn sớm nhất.")
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * GET /api/admin/service-bookings
     * Lấy danh sách yêu cầu đặt dịch vụ (Admin)
     */
    @GetMapping("/api/admin/service-bookings")
    public ResponseEntity<PagedResponse<ServiceBookingResponse>> getServiceBookings(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        ServiceBookingStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = ServiceBookingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ServiceBooking> result = serviceBookingRepository.searchBookings(
                statusEnum, keyword, fromDate, toDate, pageable);
        
        List<ServiceBookingResponse> content = result.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        PagedResponse<ServiceBookingResponse> response = PagedResponse.<ServiceBookingResponse>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/admin/service-bookings/{id}
     * Chi tiết yêu cầu đặt dịch vụ
     */
    @GetMapping("/api/admin/service-bookings/{id}")
    public ResponseEntity<ServiceBookingResponse> getServiceBookingById(@PathVariable Long id) {
        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu đặt dịch vụ"));
        return ResponseEntity.ok(toResponse(booking));
    }
    
    /**
     * PATCH /api/admin/service-bookings/{id}/status
     * Cập nhật trạng thái yêu cầu
     */
    @PatchMapping("/api/admin/service-bookings/{id}/status")
    public ResponseEntity<ServiceBookingResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String adminNote) {
        
        ServiceBooking booking = serviceBookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu đặt dịch vụ"));
        
        try {
            ServiceBookingStatus newStatus = ServiceBookingStatus.valueOf(status.toUpperCase());
            booking.setStatus(newStatus);
            if (adminNote != null && !adminNote.isBlank()) {
                booking.setAdminNote(adminNote);
            }
            if (newStatus == ServiceBookingStatus.CONFIRMED || newStatus == ServiceBookingStatus.COMPLETED) {
                booking.setProcessedAt(LocalDateTime.now());
            }
            ServiceBooking saved = serviceBookingRepository.save(booking);
            return ResponseEntity.ok(toResponse(saved));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái không hợp lệ");
        }
    }
    
    private ServiceBookingResponse toResponse(ServiceBooking booking) {
        return ServiceBookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .serviceId(booking.getService().getId())
                .serviceName(booking.getService().getName())
                .quantity(booking.getQuantity())
                .guestName(booking.getGuestName())
                .guestPhone(booking.getGuestPhone())
                .guestEmail(booking.getGuestEmail())
                .note(booking.getNote())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .build();
    }
    
    private String generateServiceBookingCode() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return "SVC-" + date + "-" + random;
    }

    /* ═══════════════════════════════════════════════════════════
       ADMIN – CRUD
    ═══════════════════════════════════════════════════════════ */

    /**
     * GET /api/admin/services
     * Lấy danh sách tất cả dịch vụ (có tìm kiếm, lọc, phân trang)
     * ?status=ACTIVE&category=Gói ưu đãi&keyword=spa&page=0&size=20
     */
    @GetMapping("/api/admin/services")
    public ResponseEntity<?> getAllServices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        // Nếu không truyền page/size → trả toàn bộ list (cho frontend hiện tại)
        if (page == null && size == null) {
            return ResponseEntity.ok(service.getAllServices());
        }
        PagedResponse<AdditionalServiceResponse> paged =
                service.searchServices(status, category, keyword,
                        page != null ? page : 0,
                        size != null ? size : 20);
        return ResponseEntity.ok(paged);
    }

    /**
     * GET /api/admin/services/{id}
     */
    @GetMapping("/api/admin/services/{id}")
    public ResponseEntity<AdditionalServiceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    /**
     * POST /api/admin/services
     */
    @PostMapping("/api/admin/services")
    public ResponseEntity<AdditionalServiceResponse> create(
            @Valid @RequestBody AdditionalServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    /**
     * PUT /api/admin/services/{id}
     */
    @PutMapping("/api/admin/services/{id}")
    public ResponseEntity<AdditionalServiceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AdditionalServiceRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * DELETE /api/admin/services/{id}
     */
    @DeleteMapping("/api/admin/services/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
