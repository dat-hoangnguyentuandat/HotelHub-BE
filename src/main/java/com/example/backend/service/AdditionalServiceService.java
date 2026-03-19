package com.example.backend.service;

import com.example.backend.dto.request.AdditionalServiceRequest;
import com.example.backend.dto.response.AdditionalServiceResponse;
import com.example.backend.dto.response.PagedResponse;

import java.util.List;

public interface AdditionalServiceService {

    /** Lấy tất cả dịch vụ đang ACTIVE (public – cho trang booking) */
    List<AdditionalServiceResponse> getActiveServices();

    /** Lấy tất cả dịch vụ không phân trang (admin – list nhỏ) */
    List<AdditionalServiceResponse> getAllServices();

    /** Tìm kiếm / lọc có phân trang (admin) */
    PagedResponse<AdditionalServiceResponse> searchServices(
            String status, String category, String keyword, int page, int size);

    /** Lấy 1 dịch vụ theo id */
    AdditionalServiceResponse getById(Long id);

    /** Tạo mới */
    AdditionalServiceResponse create(AdditionalServiceRequest request);

    /** Cập nhật */
    AdditionalServiceResponse update(Long id, AdditionalServiceRequest request);

    /** Xóa */
    void delete(Long id);
}
