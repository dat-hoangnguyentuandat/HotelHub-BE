package com.example.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 150, message = "Họ và tên tối đa 150 ký tự")
    private String fullName;

    @NotBlank(message = "Vai trò không được để trống")
    @Size(max = 100, message = "Vai trò tối đa 100 ký tự")
    private String role;

    @Pattern(
        regexp = "^(\\+84|0)[0-9]{9}$",
        message = "Số điện thoại không hợp lệ"
    )
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "Ca làm việc không được để trống")
    @Size(max = 200, message = "Ca làm tối đa 200 ký tự")
    private String shift;

    /** WORKING | ON_LEAVE | INACTIVE */
    private String status;

    private String note;

    /** Base64 image string (optional) */
    private String avatar;
}
