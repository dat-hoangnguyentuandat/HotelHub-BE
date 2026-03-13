package com.example.backend.service.impl;

import com.example.backend.dto.request.BookingRequest;
import com.example.backend.dto.request.UpdateBookingStatusRequest;
import com.example.backend.dto.response.BookingResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.BookingStatus;
import com.example.backend.entity.User;
import com.example.backend.exception.BookingConflictException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository    userRepository;

    /* ───────────────────────────────────────────────────────────────
       TẠO BOOKING
    _______________________________________________________________ */
    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest req, String userEmail) {

        // 1. Validate ngày
        if (!req.getCheckOut().isAfter(req.getCheckIn())) {
            throw new IllegalArgumentException("Ngày trả phòng phải sau ngày nhận phòng");
        }

        // 2. Tính số đêm
        int nights = (int) ChronoUnit.DAYS.between(req.getCheckIn(), req.getCheckOut());

        // 3. Tính tổng tiền
        BigDecimal total = req.getPricePerNight()
                .multiply(BigDecimal.valueOf((long) nights * req.getRooms()));

        // 4. Tìm user (nếu đã đăng nhập)
        User user = null;
        if (userEmail != null) {
            user = userRepository.findByEmail(userEmail).orElse(null);
        }

        // 5. Lưu
        Booking booking = Booking.builder()
                .guestName(req.getGuestName())
                .guestPhone(req.getGuestPhone())
                .guestEmail(req.getGuestEmail())
                .roomType(req.getRoomType())
                .pricePerNight(req.getPricePerNight())
                .checkIn(req.getCheckIn())
                .checkOut(req.getCheckOut())
                .nights(nights)
                .rooms(req.getRooms())
                .adults(req.getAdults())
                .children(req.getChildren())
                .totalAmount(total)
                .note(req.getNote())
                .status(BookingStatus.PENDING)
                .user(user)
                .build();

        return BookingResponse.from(bookingRepository.save(booking));
    }

    /* ───────────────────────────────────────────────────────────────
       BOOKING CỦA TÔI
    _______________________________________________________________ */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getMyBookings(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> p = bookingRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        return toPagedResponse(p);
    }

    /* ───────────────────────────────────────────────────────────────
       HUỶ BOOKING
    _______________________________________________________________ */
    @Override
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking #" + bookingId));

        // Chỉ chủ booking mới được huỷ
        if (booking.getUser() == null || !booking.getUser().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Bạn không có quyền huỷ booking này");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking đã được huỷ trước đó");
        }
        if (booking.getStatus() == BookingStatus.CHECKED_IN ||
            booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Không thể huỷ booking ở trạng thái " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return BookingResponse.from(bookingRepository.save(booking));
    }

    /* ───────────────────────────────────────────────────────────────
       ADMIN – TẤT CẢ BOOKING
    _______________________________________________________________ */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingResponse> getAllBookings(BookingStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> p = (status != null)
                ? bookingRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : bookingRepository.findAll(pageable);

        return toPagedResponse(p);
    }

    /* ───────────────────────────────────────────────────────────────
       ADMIN – CẬP NHẬT TRẠNG THÁI
    _______________________________________________________________ */
    @Override
    @Transactional
    public BookingResponse updateStatus(Long bookingId, UpdateBookingStatusRequest req) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking #" + bookingId));

        booking.setStatus(req.getStatus());
        return BookingResponse.from(bookingRepository.save(booking));
    }

    /* ───────────────────────────────────────────────────────────────
       HELPER
    _______________________________________________________________ */
    private PagedResponse<BookingResponse> toPagedResponse(Page<Booking> p) {
        List<BookingResponse> content = p.getContent()
                .stream()
                .map(BookingResponse::from)
                .toList();

        return PagedResponse.<BookingResponse>builder()
                .content(content)
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }
}
