-- ================================================================
--  HotelHub – Admin seed data
--  Chạy 1 lần để tạo tài khoản admin mặc định
--  Password: Admin@123  (BCrypt hash bên dưới)
-- ================================================================

INSERT INTO users (full_name, email, password, role, created_at)
SELECT 'Administrator', 'admin@hotelhub.com',
       '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
       'ADMIN', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@hotelhub.com'
);
