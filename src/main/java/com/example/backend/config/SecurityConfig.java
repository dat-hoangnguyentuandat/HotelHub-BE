package com.example.backend.config;

import com.example.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Preflight CORS – phải permit trước mọi rule khác
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                // Serve ảnh đã upload – public
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                // Khách vãng lai được đặt phòng + xem danh sách phòng
                .requestMatchers(HttpMethod.POST, "/api/bookings").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/rooms").permitAll()
                // Khách vãng lai xem dịch vụ bổ sung đang bán
                .requestMatchers(HttpMethod.GET,  "/api/services").permitAll()
                // User đã đăng nhập xem lịch sử đặt dịch vụ của mình
                .requestMatchers(HttpMethod.GET,  "/api/services/my-bookings").authenticated()
                // ── Thanh toán ──
                // Guest / walk-in được phép khởi tạo & xử lý thanh toán (không cần đăng nhập)
                .requestMatchers(HttpMethod.POST, "/api/payments/initiate").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/payments/*/process").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/payments/*").permitAll()
                // Xác thực promo code – public
                .requestMatchers(HttpMethod.POST, "/api/payments/promo/validate").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/payments/promo/demo-codes").permitAll()
                // Lấy payments theo booking – public (kiểm tra quyền trong service)
                .requestMatchers(HttpMethod.GET,  "/api/bookings/*/payments").permitAll()
                // Hủy payment – cho phép cả guest
                .requestMatchers(HttpMethod.PATCH, "/api/payments/*/cancel").permitAll()
                // ── Thông tin thanh toán (payment-info page) – yêu cầu đăng nhập ──
                .requestMatchers(HttpMethod.GET, "/api/payments/my/info").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/payments/my/stats").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/payments/*/info").authenticated()
                // Gợi ý voucher hoàn tiền – cần đăng nhập
                .requestMatchers(HttpMethod.GET, "/api/payments/*/refund-voucher-suggestion").authenticated()
                // Loyalty: user đăng nhập tự xem / đổi điểm
                .requestMatchers("/api/loyalty/me", "/api/loyalty/me/**").authenticated()
                // Loyalty admin: phân quyền tại @PreAuthorize trong Controller
                .requestMatchers("/api/loyalty/admin/**").authenticated()
                // Voucher: danh sách active – public
                .requestMatchers(HttpMethod.GET, "/api/vouchers").permitAll()
                // Voucher: đổi điểm & xem của mình – cần đăng nhập
                .requestMatchers("/api/vouchers/redeem", "/api/vouchers/my").authenticated()
                // Voucher admin: phân quyền tại @PreAuthorize trong Controller
                .requestMatchers("/api/admin/vouchers/**").authenticated()
                // ── Reviews ──
                // Khách vãng lai được gửi review + xem review public
                .requestMatchers(HttpMethod.POST, "/api/reviews").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/reviews/public").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/reviews/public/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/reviews/booking/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/reviews/room/**").permitAll()
                // User đã đăng nhập xem review của mình
                .requestMatchers(HttpMethod.GET,  "/api/reviews/my").authenticated()
                // Admin quản lý review: phân quyền tại @PreAuthorize trong Controller
                .requestMatchers("/api/admin/reviews/**").authenticated()
                // ── Yêu cầu đặc biệt từ user (public – không cần đăng nhập) ──
                .requestMatchers(HttpMethod.POST, "/api/special-requests/public").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Parse allowed origins from environment variable (comma-separated)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);
        config.setAllowedOriginPatterns(List.of("*")); // Allow all origins temporarily for Railway
        
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // Cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
