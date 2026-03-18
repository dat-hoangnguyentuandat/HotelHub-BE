package com.example.backend.service;

import com.example.backend.dto.request.SpecialRequestRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.SpecialRequestResponse;
import com.example.backend.entity.Booking;
import com.example.backend.entity.SpecialRequest;
import com.example.backend.entity.SpecialRequestStatus;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.SpecialRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpecialRequestService {

    private final SpecialRequestRepository repo;
    private final BookingRepository bookingRepo;

    /* ─────────────────────────────────────────────
       GET ALL (với filter + search + pagination)
    ───────────────────────────────────────────── */
    @Transactional(readOnly = true)
    public PagedResponse<SpecialRequestResponse> getAll(
            SpecialRequestStatus status, String keyword, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<SpecialRequest> pageResult;

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasStatus  = status != null;

        if (hasKeyword) {
            /* search() dùng JPQL với :status nullable */
            pageResult = repo.search(status, keyword.trim(), pageable);
        } else if (hasStatus) {
            pageResult = repo.findByStatus(status, pageable);
        } else {
            pageResult = repo.findAll(pageable);
        }

        return PagedResponse.<SpecialRequestResponse>builder()
                .content(pageResult.getContent().stream().map(SpecialRequestResponse::from).toList())
                .page(pageResult.getNumber())
                .size(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    /* ─────────────────────────────────────────────
       GET BY ID
    ───────────────────────────────────────────── */
    @Transactional(readOnly = true)
    public SpecialRequestResponse getById(Long id) {
        return SpecialRequestResponse.from(findOrThrow(id));
    }

    /* ─────────────────────────────────────────────
       CREATE
    ───────────────────────────────────────────── */
    @Transactional
    public SpecialRequestResponse create(SpecialRequestRequest req) {
        Booking booking = null;
        if (req.getBookingId() != null) {
            booking = bookingRepo.findById(req.getBookingId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Không tìm thấy booking #" + req.getBookingId()));
        }

        SpecialRequest entity = SpecialRequest.builder()
                .guestName(req.getGuestName())
                .guestPhone(req.getGuestPhone())
                .requestType(req.getRequestType())
                .content(req.getContent())
                .booking(booking)
                .build();

        return SpecialRequestResponse.from(repo.save(entity));
    }

    /* ─────────────────────────────────────────────
       UPDATE STATUS  (APPROVE / REJECT / DONE)
    ───────────────────────────────────────────── */
    @Transactional
    public SpecialRequestResponse updateStatus(Long id, String statusStr, String adminNote) {
        SpecialRequest entity = findOrThrow(id);

        SpecialRequestStatus newStatus;
        try {
            newStatus = SpecialRequestStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Trạng thái không hợp lệ: " + statusStr);
        }

        entity.setStatus(newStatus);
        if (adminNote != null) entity.setAdminNote(adminNote);

        return SpecialRequestResponse.from(repo.save(entity));
    }

    /* ─────────────────────────────────────────────
       DELETE
    ───────────────────────────────────────────── */
    @Transactional
    public void delete(Long id) {
        SpecialRequest entity = findOrThrow(id);
        repo.delete(entity);
    }

    /* ─────────────────────────────────────────────
       STATS (số lượng theo từng trạng thái)
    ───────────────────────────────────────────── */
    @Transactional(readOnly = true)
    public Map<String, Long> getStats() {
        return Map.of(
                "total",    repo.count(),
                "pending",  repo.countByStatus(SpecialRequestStatus.PENDING),
                "approved", repo.countByStatus(SpecialRequestStatus.APPROVED),
                "done",     repo.countByStatus(SpecialRequestStatus.DONE),
                "rejected", repo.countByStatus(SpecialRequestStatus.REJECTED)
        );
    }

    /* ─────────────────────────────────────────────
       PRIVATE HELPER
    ───────────────────────────────────────────── */
    private SpecialRequest findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu đặc biệt #" + id));
    }
}
