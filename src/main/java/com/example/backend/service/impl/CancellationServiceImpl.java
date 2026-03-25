package com.example.backend.service.impl;

import com.example.backend.dto.request.CancelBookingRequest;
import com.example.backend.dto.request.PolicyRequest;
import com.example.backend.dto.request.ProcessRefundRequest;
import com.example.backend.dto.response.CancellationResponse;
import com.example.backend.dto.response.CancellationStatsResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.CancellationPolicyRepository;
import com.example.backend.repository.CancellationRepository;
import com.example.backend.service.CancellationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CancellationServiceImpl implements CancellationService {

    private final CancellationRepository  cancellationRepository;
    private final CancellationPolicyRepository policyRepository;
    private final BookingRepository        bookingRepository;

    /* ══════════════════════════════════════════════════════════════════
       DANH SÁCH & CHI TIẾT
    ══════════════════════════════════════════════════════════════════ */

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CancellationResponse> getCancellations(
            RefundStatus refundStatus,
            String keyword,
            LocalDate from,
            LocalDate to,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        LocalDateTime cancelFrom = (from != null) ? from.atStartOfDay() : null;
        LocalDateTime cancelTo   = (to   != null) ? to.atTime(23, 59, 59) : null;

        Page<Booking> bookingPage = cancellationRepository.searchCancellations(
                refundStatus, kw, cancelFrom, cancelTo, pageable);

        List<CancellationResponse> content = bookingPage.getContent()
                .stream()
                .map(CancellationResponse::from)
                .toList();

        return PagedResponse.<CancellationResponse>builder()
                .content(content)
                .page(bookingPage.getNumber())
                .size(bookingPage.getSize())
                .totalElements(bookingPage.getTotalElements())
                .totalPages(bookingPage.getTotalPages())
                .last(bookingPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CancellationResponse getCancellationById(Long bookingId) {
        Booking booking = findCancelledBooking(bookingId);
        return CancellationResponse.from(booking);
    }

    /* ══════════════════════════════════════════════════════════════════
       THỐNG KÊ
    ══════════════════════════════════════════════════════════════════ */

    @Override
    @Transactional(readOnly = true)
    public CancellationStatsResponse getStats() {
        long total    = cancellationRepository.countAllCancellations();
        long pending  = cancellationRepository.countByRefundStatus(RefundStatus.PENDING_REFUND);
        long refunded = cancellationRepository.countByRefundStatus(RefundStatus.REFUNDED);
        long rejected = cancellationRepository.countByRefundStatus(RefundStatus.REJECTED);
        BigDecimal totalRefundAmount = cancellationRepository.sumRefundedAmount();

        return CancellationStatsResponse.builder()
                .total(total)
                .pending(pending)
                .refunded(refunded)
                .rejected(rejected)
                .totalRefundAmount(totalRefundAmount != null ? totalRefundAmount : BigDecimal.ZERO)
                .build();
    }

    /* ══════════════════════════════════════════════════════════════════
       XỬ LÝ HOÀN TIỀN (ADMIN APPROVE / REJECT)
    ══════════════════════════════════════════════════════════════════ */

    @Override
    @Transactional
    public CancellationResponse processRefund(Long bookingId, ProcessRefundRequest request) {
        Booking booking = findCancelledBooking(bookingId);

        // Chỉ xử lý khi đang ở trạng thái chờ
        if (booking.getRefundStatus() != RefundStatus.PENDING_REFUND) {
            throw new IllegalStateException(
                    "Yêu cầu hoàn tiền đã được xử lý (trạng thái hiện tại: "
                    + booking.getRefundStatus() + ")");
        }

        // Validate: phải có ghi chú khi từ chối
        if (request.getStatus() == RefundStatus.REJECTED
                && (request.getNote() == null || request.getNote().isBlank())) {
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối hoàn tiền");
        }

        // Không cho phép set PENDING_REFUND lại
        if (request.getStatus() == RefundStatus.PENDING_REFUND) {
            throw new IllegalArgumentException("Không thể đặt trạng thái về Chờ xử lý");
        }

        booking.setRefundStatus(request.getStatus());
        booking.setProcessNote(request.getNote());
        booking.setUpdatedAt(LocalDateTime.now()); // trigger onUpdate manually nếu cần

        Booking saved = cancellationRepository.save(booking);
        log.info("Admin processed refund for booking #{}: {}", bookingId, request.getStatus());

        return CancellationResponse.from(saved);
    }

    /* ══════════════════════════════════════════════════════════════════
       HỦY PHÒNG TỪ ADMIN
    ══════════════════════════════════════════════════════════════════ */

    @Override
    @Transactional
    public CancellationResponse cancelBookingByAdmin(Long bookingId, CancelBookingRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng #" + bookingId));

        // Không hủy booking đang check-in hoặc đã check-out
        if (booking.getStatus() == BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Không thể hủy booking đang trong trạng thái đã nhận phòng");
        }
        if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Không thể hủy booking đã trả phòng");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking đã được hủy trước đó");
        }

        // Tính refund theo policy
        PolicyCalculation calc = calculateRefund(booking);

        // Cập nhật trường hủy
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(request.getReason());
        booking.setCancelledAt(LocalDateTime.now());
        booking.setRefundRate(BigDecimal.valueOf(calc.rate()));
        booking.setRefundAmount(calc.amount());
        booking.setAppliedPolicy(calc.policyLabel());
        booking.setRefundStatus(RefundStatus.PENDING_REFUND);

        Booking saved = bookingRepository.save(booking);
        log.info("Admin cancelled booking #{}: refund={}% ({}đ), policy='{}'",
                bookingId, calc.rate(), calc.amount(), calc.policyLabel());

        return CancellationResponse.from(saved);
    }

    /* ══════════════════════════════════════════════════════════════════
       XÓA
    ══════════════════════════════════════════════════════════════════ */

    @Override
    @Transactional
    public void deleteCancellation(Long bookingId) {
        Booking booking = findCancelledBooking(bookingId);

        if (booking.getRefundStatus() == RefundStatus.PENDING_REFUND) {
            throw new IllegalStateException(
                    "Không thể xóa yêu cầu đang chờ xử lý. Vui lòng xử lý trước khi xóa.");
        }

        // Soft delete: Chỉ xóa thông tin cancellation, giữ lại booking
        booking.setCancelReason(null);
        booking.setCancelledAt(null);
        booking.setRefundRate(null);
        booking.setRefundAmount(null);
        booking.setRefundStatus(null);
        booking.setProcessNote(null);
        booking.setAppliedPolicy(null);
        
        // Đổi status về CONFIRMED nếu chưa quá ngày check-in
        if (booking.getCheckIn().isAfter(LocalDate.now())) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }
        
        bookingRepository.save(booking);
        log.info("Deleted cancellation record for booking #{} (soft delete)", bookingId);
    }

    /* ══════════════════════════════════════════════════════════════════
       CHÍNH SÁCH HOÀN TIỀN
    ══════════════════════════════════════════════════════════════════ */

    @Override
    @Transactional(readOnly = true)
    public List<CancellationPolicy> getPolicies() {
        return policyRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional
    public List<CancellationPolicy> savePolicies(List<PolicyRequest> requests) {
        // Xóa hết policies cũ và thay thế toàn bộ
        policyRepository.deleteAll();
        List<CancellationPolicy> newPolicies = new ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            PolicyRequest req = requests.get(i);
            CancellationPolicy policy = CancellationPolicy.builder()
                    .label(req.getLabel())
                    .minHours(req.getMinHours())
                    .refundRate(req.getRefundRate())
                    .displayOrder(i)
                    .build();
            newPolicies.add(policyRepository.save(policy));
        }

        log.info("Saved {} cancellation policies", newPolicies.size());
        return newPolicies;
    }

    @Override
    @Transactional
    public CancellationPolicy addPolicy(PolicyRequest request) {
        int order = (int) policyRepository.count();
        CancellationPolicy policy = CancellationPolicy.builder()
                .label(request.getLabel())
                .minHours(request.getMinHours())
                .refundRate(request.getRefundRate())
                .displayOrder(request.getDisplayOrder() > 0 ? request.getDisplayOrder() : order)
                .build();
        return policyRepository.save(policy);
    }

    @Override
    @Transactional
    public CancellationPolicy updatePolicy(Long policyId, PolicyRequest request) {
        CancellationPolicy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chính sách #" + policyId));
        policy.setLabel(request.getLabel());
        policy.setMinHours(request.getMinHours());
        policy.setRefundRate(request.getRefundRate());
        policy.setDisplayOrder(request.getDisplayOrder());
        return policyRepository.save(policy);
    }

    @Override
    @Transactional
    public void deletePolicy(Long policyId) {
        if (!policyRepository.existsById(policyId)) {
            throw new ResourceNotFoundException("Không tìm thấy chính sách #" + policyId);
        }
        policyRepository.deleteById(policyId);
    }

    /* ══════════════════════════════════════════════════════════════════
       HELPER – TÍNH HOÀN TIỀN THEO POLICY
    ══════════════════════════════════════════════════════════════════ */

    /**
     * Tính tỷ lệ và số tiền hoàn dựa trên danh sách chính sách và
     * số giờ còn lại trước ngày check-in.
     *
     * Thuật toán: chọn chính sách có minHours lớn nhất mà số giờ thực tế >= minHours.
     * Nếu không có policy phù hợp → lấy policy có minHours nhỏ nhất.
     */
    private PolicyCalculation calculateRefund(Booking booking) {
        List<CancellationPolicy> policies = policyRepository.findAllByOrderByMinHoursDesc();

        long hoursUntilCheckIn = ChronoUnit.HOURS.between(
                LocalDateTime.now(),
                booking.getCheckIn().atStartOfDay()
        );

        // Nếu chưa có policy nào trong DB, dùng policy mặc định đơn giản
        if (policies.isEmpty()) {
            int     bestRate;
            String  bestLabel;
            
            if (hoursUntilCheckIn >= 48) {
                bestRate  = 100;
                bestLabel = "Hủy trước 48h hoàn 100%";
            } else if (hoursUntilCheckIn >= 24) {
                bestRate  = 50;
                bestLabel = "Hủy trước 24h hoàn 50%";
            } else {
                bestRate  = 0;
                bestLabel = "Không đủ điều kiện hoàn tiền";
            }
            
            BigDecimal total  = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
            BigDecimal amount = total
                    .multiply(BigDecimal.valueOf(bestRate))
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

            return new PolicyCalculation(bestRate, amount, bestLabel);
        }

        // Tìm policy áp dụng (policy có minHours lớn nhất mà <= hoursUntilCheckIn)
        CancellationPolicy applicablePolicy = null;
        for (CancellationPolicy p : policies) {
            if (hoursUntilCheckIn >= p.getMinHours()) {
                applicablePolicy = p;
                break; // Đã sort giảm dần nên policy đầu tiên thỏa mãn là policy tốt nhất
            }
        }

        // Nếu không tìm thấy policy nào phù hợp, lấy policy có minHours nhỏ nhất (policy cuối cùng)
        if (applicablePolicy == null) {
            applicablePolicy = policies.get(policies.size() - 1);
        }

        BigDecimal total  = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal amount = total
                .multiply(BigDecimal.valueOf(applicablePolicy.getRefundRate()))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        return new PolicyCalculation(applicablePolicy.getRefundRate(), amount, applicablePolicy.getLabel());
    }

    /** Helper record để trả về kết quả tính refund */
    private record PolicyCalculation(int rate, BigDecimal amount, String policyLabel) {}

    /* ══════════════════════════════════════════════════════════════════
       HELPER – TÌM BOOKING ĐÃ HỦY
    ══════════════════════════════════════════════════════════════════ */

    private Booking findCancelledBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đặt phòng #" + bookingId));
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            throw new IllegalStateException("Đặt phòng #" + bookingId + " chưa được hủy");
        }
        return booking;
    }
}
