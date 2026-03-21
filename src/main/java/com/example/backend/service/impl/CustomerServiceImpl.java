package com.example.backend.service.impl;

import com.example.backend.dto.request.AdminCreateUserRequest;
import com.example.backend.dto.response.CustomerStatsResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.EmailAlreadyExistsException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.LoyaltyAccountRepository;
import com.example.backend.repository.LoyaltyTransactionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository            userRepository;
    private final LoyaltyAccountRepository  loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final PasswordEncoder           passwordEncoder;

    /* ── Danh sách user ─────────────────────────────────────────────────────── */

    @Override
    public List<UserResponse> listUsers(String keyword, Role role) {
        String kw = (keyword == null) ? "" : keyword.trim();
        // Nếu không có keyword và không lọc role → trả toàn bộ nhanh hơn
        if (kw.isEmpty() && role == null) {
            return userRepository.findAllByOrderByCreatedAtDesc()
                    .stream()
                    .map(UserResponse::from)
                    .toList();
        }
        return userRepository.searchUsers(kw, role)
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    /* ── Chi tiết user ──────────────────────────────────────────────────────── */

    @Override
    public UserResponse getUser(Long id) {
        User user = findUserById(id);
        return UserResponse.from(user);
    }

    /* ── Tạo user (admin) ───────────────────────────────────────────────────── */

    @Override
    @Transactional
    public UserResponse createUser(AdminCreateUserRequest request) {
        // 1. Kiểm tra email trùng
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // 2. Build & persist
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.GUEST)
                .build();

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    /* ── Đổi role ───────────────────────────────────────────────────────────── */

    @Override
    @Transactional
    public UserResponse changeRole(Long id, Role newRole) {
        User user = findUserById(id);
        user.setRole(newRole);
        return UserResponse.from(userRepository.save(user));
    }

    /* ── Xoá user ───────────────────────────────────────────────────────────── */

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);

        // 1. Xoá LoyaltyTransaction → LoyaltyAccount (cascade theo thứ tự FK)
        loyaltyAccountRepository.findByUserId(id).ifPresent(acc -> {
            loyaltyTransactionRepository.deleteByLoyaltyAccountId(acc.getId());
            loyaltyAccountRepository.delete(acc);
        });

        // 2. Xoá user
        userRepository.delete(user);
    }

    /* ── Thống kê ───────────────────────────────────────────────────────────── */

    @Override
    public CustomerStatsResponse getStats() {
        LocalDateTime startOfMonth = LocalDateTime.now()
                .withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        return CustomerStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalGuests(userRepository.countByRole(Role.GUEST))
                .totalOwners(userRepository.countByRole(Role.HOTEL_OWNER))
                .totalAdmins(userRepository.countByRole(Role.ADMIN))
                .newThisMonth(userRepository.countByCreatedAtAfter(startOfMonth))
                .build();
    }

    /* ── Helper ─────────────────────────────────────────────────────────────── */

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng #" + id));
    }
}
