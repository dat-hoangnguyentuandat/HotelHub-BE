package com.example.backend.service;

import com.example.backend.dto.request.RedeemVoucherRequest;
import com.example.backend.dto.request.VoucherRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.UserVoucherResponse;
import com.example.backend.dto.response.VoucherResponse;

import java.util.List;

public interface VoucherService {

    // ── ADMIN ──────────────────────────────────────────────────

    /** Lấy danh sách voucher (admin, có lọc & phân trang) */
    PagedResponse<VoucherResponse> getAllVouchers(String keyword, Boolean active, int page, int size);

    /** Tạo voucher mới */
    VoucherResponse createVoucher(VoucherRequest request);

    /** Cập nhật voucher */
    VoucherResponse updateVoucher(Long id, VoucherRequest request);

    /** Xóa voucher */
    void deleteVoucher(Long id);

    // ── CUSTOMER ───────────────────────────────────────────────

    /** Danh sách voucher đang active để khách xem/đổi */
    List<VoucherResponse> getActiveVouchers();

    /** Khách dùng điểm đổi voucher */
    UserVoucherResponse redeemVoucher(String email, RedeemVoucherRequest request);

    /** Lịch sử voucher đã đổi của khách */
    PagedResponse<UserVoucherResponse> getMyVouchers(String email, int page, int size);
}
