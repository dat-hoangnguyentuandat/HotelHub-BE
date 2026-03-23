package com.example.backend.service;

import com.example.backend.dto.request.AdminCreateUserRequest;
import com.example.backend.dto.response.CustomerStatsResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.Role;

import java.util.List;

/**
 * Service quản lý khách hàng dành cho admin.
 */
public interface CustomerService {

    /** Lấy danh sách user, có thể lọc theo keyword & role. */
    List<UserResponse> listUsers(String keyword, Role role);

    /** Lấy chi tiết một user. */
    UserResponse getUser(Long id);

    /** Admin tạo user mới với role tùy chọn. */
    UserResponse createUser(AdminCreateUserRequest request);

    /** Đổi role của user. */
    UserResponse changeRole(Long id, Role newRole);

    /** Xoá user – xử lý các ràng buộc FK trước khi xoá. */
    void deleteUser(Long id);

    /** Số liệu thống kê tổng hợp. */
    CustomerStatsResponse getStats();
}
