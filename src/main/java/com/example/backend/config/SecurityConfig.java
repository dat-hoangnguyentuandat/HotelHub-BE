package com.example.backend.config;

import com.example.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

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
                // Loyalty: user đăng nhập tự xem / đổi điểm
                .requestMatchers("/api/loyalty/me", "/api/loyalty/me/**").authenticated()
                // Loyalty admin: phân quyền tại @PreAuthorize trong Controller
                .requestMatchers("/api/loyalty/admin/**").authenticated()
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
        config.setAllowedOrigins(List.of(
            "http://localhost:8080",
            "http://127.0.0.1:8080",
            "http://localhost:8081",
            "http://127.0.0.1:8081",
            "http://localhost:3000",
            "http://127.0.0.1:3000"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
