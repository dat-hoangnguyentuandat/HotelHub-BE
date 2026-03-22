package com.example.backend.service.impl;

import com.example.backend.dto.request.RedeemVoucherRequest;
import com.example.backend.dto.request.VoucherRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.UserVoucherResponse;
import com.example.backend.dto.response.VoucherResponse;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.*;
import com.example.backend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository               voucherRepository;
    private final UserVoucherRepository           userVoucherRepository;
    private final LoyaltyAccountRepository        loyaltyAccountRepository;
    private final LoyaltyTransactionRepository    loyaltyTransactionRepository;
    private final UserRepository                  userRepository;

    /* ══════════════════════════════════════════════════════════
       ADMIN – DANH SÁCH CÓ LỌC
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VoucherResponse> getAllVouchers(
            String keyword, Boolean active, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<Voucher> p = voucherRepository.findByFilters(kw, active, pageable);

        List<VoucherResponse> content = p.getContent().stream()
                .map(VoucherResponse::from).toList();

        return buildPage(content, p);
    }

    /* ══════════════════════════════════════════════════════════
       ADMIN – TẠO VOUCHER
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public VoucherResponse createVoucher(VoucherRequest request) {
        String code = generateCode(request.getName());

        Voucher voucher = Voucher.builder()
                .name(request.getName())
                .description(request.getDescription())
                .pointsRequired(request.getPointsRequired())
                .value(request.getValue())
                .code(code)
                .category(request.getCategory() != null ? request.getCategory() : "Giảm giá")
                .active(request.getActive() != null ? request.getActive() : true)
                .maxRedemptions(request.getMaxRedemptions())
                .build();

        voucher = voucherRepository.save(voucher);
        log.info("Admin tạo voucher mới: {} ({})", voucher.getName(), voucher.getCode());
        return VoucherResponse.from(voucher);
    }

    /* ══════════════════════════════════════════════════════════
       ADMIN – CẬP NHẬT VOUCHER
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public VoucherResponse updateVoucher(Long id, VoucherRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher #" + id));

        voucher.setName(request.getName());
        voucher.setDescription(request.getDescription());
        voucher.setPointsRequired(request.getPointsRequired());
        voucher.setValue(request.getValue());
        if (request.getCategory() != null) voucher.setCategory(request.getCategory());
        if (request.getActive() != null)   voucher.setActive(request.getActive());
        voucher.setMaxRedemptions(request.getMaxRedemptions());

        voucher = voucherRepository.save(voucher);
        log.info("Admin cập nhật voucher #{}: {}", id, voucher.getName());
        return VoucherResponse.from(voucher);
    }

    /* ══════════════════════════════════════════════════════════
       ADMIN – XÓA VOUCHER
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher #" + id));
        voucherRepository.delete(voucher);
        log.info("Admin xóa voucher #{}", id);
    }

    /* ══════════════════════════════════════════════════════════
       CUSTOMER – DANH SÁCH VOUCHER ACTIVE
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getActiveVouchers() {
        return voucherRepository.findByActiveTrueOrderByPointsRequiredAsc()
                .stream().map(VoucherResponse::from).toList();
    }

    /* ══════════════════════════════════════════════════════════
       CUSTOMER – ĐỔI ĐIỂM LẤY VOUCHER
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional
    public UserVoucherResponse redeemVoucher(String email, RedeemVoucherRequest request) {
        // 1. Lấy voucher
        Voucher voucher = voucherRepository.findByIdAndActiveTrue(request.getVoucherId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Voucher không tồn tại hoặc đã ngừng hoạt động"));

        // 2. Kiểm tra giới hạn số lượng
        if (voucher.getMaxRedemptions() != null
                && voucher.getRedeemedCount() >= voucher.getMaxRedemptions()) {
            throw new IllegalStateException("Voucher này đã hết lượt đổi");
        }

        // 3. Lấy tài khoản loyalty của user
        LoyaltyAccount account = loyaltyAccountRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tài khoản loyalty của: " + email));

        // 4. Kiểm tra đủ điểm không
        if (account.getCurrentPoints() < voucher.getPointsRequired()) {
            throw new IllegalStateException(
                    "Không đủ điểm. Cần " + voucher.getPointsRequired()
                    + " điểm, bạn có " + account.getCurrentPoints() + " điểm");
        }

        // 5. Trừ điểm
        account.deductPoints(voucher.getPointsRequired());
        loyaltyAccountRepository.save(account);

        // 6. Ghi giao dịch điểm
        LoyaltyTransaction tx = LoyaltyTransaction.builder()
                .loyaltyAccount(account)
                .type(TransactionType.REDEEM)
                .points(voucher.getPointsRequired())
                .balanceAfter(account.getCurrentPoints())
                .description("Đổi điểm lấy voucher: " + voucher.getName())
                .build();
        loyaltyTransactionRepository.save(tx);

        // 7. Tạo mã voucher riêng cho user này
        String uniqueCode = voucher.getCode() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // 8. Lưu UserVoucher
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + email));

        UserVoucher uv = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .pointsSpent(voucher.getPointsRequired())
                .redeemedCode(uniqueCode)
                .status(UserVoucherStatus.ACTIVE)
                .build();
        userVoucherRepository.save(uv);

        // 9. Tăng redeemedCount
        voucher.setRedeemedCount(voucher.getRedeemedCount() + 1);
        voucherRepository.save(voucher);

        log.info("User {} đổi {} điểm lấy voucher '{}' (mã: {})",
                email, voucher.getPointsRequired(), voucher.getName(), uniqueCode);

        return UserVoucherResponse.from(uv);
    }

    /* ══════════════════════════════════════════════════════════
       CUSTOMER – LỊCH SỬ VOUCHER ĐÃ ĐỔI
    ══════════════════════════════════════════════════════════ */
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserVoucherResponse> getMyVouchers(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user: " + email));

        Pageable pageable = PageRequest.of(page, size);
        Page<UserVoucher> p = userVoucherRepository.findByUserIdOrderByRedeemedAtDesc(
                user.getId(), pageable);

        List<UserVoucherResponse> content = p.getContent().stream()
                .map(UserVoucherResponse::from).toList();

        return buildPage(content, p);
    }

    /* ── Helpers ── */
    private String generateCode(String name) {
        // VD: "Giảm 50.000đ" → "VOUCHER-A3F9B2"
        return "VOUCHER-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private <T> PagedResponse<T> buildPage(List<T> content, Page<?> p) {
        return PagedResponse.<T>builder()
                .content(content)
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }
}
