package com.example.backend.controller;

import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.RegisterRequest;
import com.example.backend.dto.response.AuthResponse;
import com.example.backend.entity.Role;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * DEV-ONLY: Đổi role của user thành ADMIN bằng email.
     * Gọi: POST /api/auth/make-admin  body: { "email": "...", "secret": "hotelhub-dev" }
     * Xoá endpoint này trước khi deploy production.
     */
    @PostMapping("/make-admin")
    public ResponseEntity<?> makeAdmin(@RequestBody Map<String, String> body) {
        if (!"hotelhub-dev".equals(body.get("secret"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid secret"));
        }
        String email = body.get("email");
        return userRepository.findByEmail(email).map(user -> {
            user.setRole(Role.ADMIN);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                    "message", "Đổi role thành công",
                    "email",   email,
                    "role",    "ADMIN"
            ));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Không tìm thấy user: " + email)));
    }
}
