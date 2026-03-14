package com.example.backend.service.impl;

import com.example.backend.dto.request.StaffRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.StaffResponse;
import com.example.backend.entity.Staff;
import com.example.backend.entity.StaffStatus;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.StaffRepository;
import com.example.backend.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;

    /* ══════════════════════════════════════════════════════════
       1. DANH SÁCH (filter + phân trang)
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StaffResponse> getAllStaff(
            String keyword, String statusStr, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        boolean hasKeyword = StringUtils.hasText(keyword);
        boolean hasStatus  = StringUtils.hasText(statusStr);

        Page<Staff> pageResult;

        if (hasStatus) {
            StaffStatus status = parseStatus(statusStr);
            pageResult = hasKeyword
                    ? staffRepository.findByStatusAndFullNameContainingIgnoreCase(status, keyword.trim(), pageable)
                    : staffRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            pageResult = hasKeyword
                    ? staffRepository.findByFullNameContainingIgnoreCaseOrderByCreatedAtDesc(keyword.trim(), pageable)
                    : staffRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return toPagedResponse(pageResult);
    }

    /* ══════════════════════════════════════════════════════════
       2. CHI TIẾT
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public StaffResponse getById(Long id) {
        Staff staff = findOrThrow(id);
        return StaffResponse.from(staff);
    }

    /* ══════════════════════════════════════════════════════════
       3. TẠO MỚI
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public StaffResponse createStaff(StaffRequest request) {
        Staff staff = Staff.builder()
                .fullName(request.getFullName().trim())
                .role(request.getRole().trim())
                .phone(trimOrNull(request.getPhone()))
                .email(trimOrNull(request.getEmail()))
                .shift(request.getShift().trim())
                .status(parseStatusOrDefault(request.getStatus()))
                .note(trimOrNull(request.getNote()))
                .avatar(request.getAvatar())
                .build();

        return StaffResponse.from(staffRepository.save(staff));
    }

    /* ══════════════════════════════════════════════════════════
       4. CẬP NHẬT
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public StaffResponse updateStaff(Long id, StaffRequest request) {
        Staff staff = findOrThrow(id);

        staff.setFullName(request.getFullName().trim());
        staff.setRole(request.getRole().trim());
        staff.setPhone(trimOrNull(request.getPhone()));
        staff.setEmail(trimOrNull(request.getEmail()));
        staff.setShift(request.getShift().trim());
        staff.setStatus(parseStatusOrDefault(request.getStatus()));
        staff.setNote(trimOrNull(request.getNote()));

        // Chỉ cập nhật avatar nếu client gửi lên (null → giữ nguyên)
        if (request.getAvatar() != null) {
            staff.setAvatar(request.getAvatar());
        }

        return StaffResponse.from(staffRepository.save(staff));
    }

    /* ══════════════════════════════════════════════════════════
       5. XOÁ
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public void deleteStaff(Long id) {
        if (!staffRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy nhân viên #" + id);
        }
        staffRepository.deleteById(id);
    }

    /* ── Helpers ─────────────────────────────────────────── */
    private Staff findOrThrow(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên #" + id));
    }

    private StaffStatus parseStatus(String value) {
        try {
            return StaffStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + value);
        }
    }

    private StaffStatus parseStatusOrDefault(String value) {
        if (!StringUtils.hasText(value)) return StaffStatus.WORKING;
        try {
            return StaffStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return StaffStatus.WORKING;
        }
    }

    private String trimOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private PagedResponse<StaffResponse> toPagedResponse(Page<Staff> page) {
        return PagedResponse.<StaffResponse>builder()
                .content(page.getContent().stream().map(StaffResponse::from).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
