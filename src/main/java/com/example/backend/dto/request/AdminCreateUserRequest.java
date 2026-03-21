package com.example.backend.dto.request;

import com.example.backend.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body khi admin tạo user mới.
 * Khác RegisterRequest ở chỗ:
 *  - Không cần confirmPassword
 *  - Không yêu cầu password phức tạp (admin tự quản lý)
 *  - Có thể chỉ định role bất kỳ (GUEST / HOTEL_OWNER / ADMIN)
 */
@Getter
@Setter
public class AdminCreateUserRequest {

    @NotBlank(message = "Họ và tên không được trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được trống")
    @Size(min = 6, max = 100, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @NotNull(message = "Vai trò không được trống")
    private Role role;
}
