package com.example.backend.service;

import com.example.backend.dto.request.StaffRequest;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.dto.response.StaffResponse;

public interface StaffService {

    /** Lấy danh sách nhân viên (filter + phân trang) */
    PagedResponse<StaffResponse> getAllStaff(String keyword, String status, int page, int size);

    /** Lấy chi tiết một nhân viên */
    StaffResponse getById(Long id);

    /** Thêm nhân viên mới */
    StaffResponse createStaff(StaffRequest request);

    /** Cập nhật thông tin nhân viên */
    StaffResponse updateStaff(Long id, StaffRequest request);

    /** Xoá nhân viên */
    void deleteStaff(Long id);
}
