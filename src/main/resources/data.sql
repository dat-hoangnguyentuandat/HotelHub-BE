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

-- ================================================================
--  Seed dữ liệu mẫu cho bảng staff
-- ================================================================

INSERT INTO staff (full_name, role, phone, email, shift, status, note, avatar, created_at, updated_at)
SELECT 'Nguyễn Nhật Hào', 'Lễ tân', '0901111111', 'hao@hotelhub.com',
       'Ca sáng: 6 giờ sáng - 2 giờ chiều', 'WORKING', NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'hao@hotelhub.com');

INSERT INTO staff (full_name, role, phone, email, shift, status, note, avatar, created_at, updated_at)
SELECT 'Trần Đức', 'Buồng phòng', '0902222222', 'duc@hotelhub.com',
       'Ca chiều: 2 giờ chiều - 10 giờ tối', 'WORKING', NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'duc@hotelhub.com');

INSERT INTO staff (full_name, role, phone, email, shift, status, note, avatar, created_at, updated_at)
SELECT 'Bùi Trí Tài', 'Quản lý', '0903333333', 'tai@hotelhub.com',
       'Cả ngày: 9 giờ sáng - 5 giờ chiều', 'WORKING', NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'tai@hotelhub.com');

INSERT INTO staff (full_name, role, phone, email, shift, status, note, avatar, created_at, updated_at)
SELECT 'Lê Nguyễn Thế Kiệt', 'Lễ tân', '0904444444', 'kiet@hotelhub.com',
       'Ca đêm: 10 giờ tối - 6 giờ sáng', 'WORKING', NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'kiet@hotelhub.com');

INSERT INTO staff (full_name, role, phone, email, shift, status, note, avatar, created_at, updated_at)
SELECT 'Nguyễn Tuấn Anh', 'Buồng phòng', '0905555555', 'anh@hotelhub.com',
       'Ca sáng: 6 giờ sáng - 2 giờ chiều', 'ON_LEAVE', 'Nghỉ phép 1 tuần', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'anh@hotelhub.com');

INSERT INTO staff (full_name, role, phone, email, shift, status, note, avatar, created_at, updated_at)
SELECT 'Phạm Thị Lan', 'Lễ tân', '0906666666', 'lan@hotelhub.com',
       'Ca chiều: 2 giờ chiều - 10 giờ tối', 'WORKING', NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'lan@hotelhub.com');

INSERT INTO staff (full_name, role, phone, email, shift, status, note, avatar, created_at, updated_at)
SELECT 'Hoàng Minh Khoa', 'Bảo vệ', '0907777777', 'khoa@hotelhub.com',
       'Ca đêm: 10 giờ tối - 6 giờ sáng', 'WORKING', NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'khoa@hotelhub.com');

INSERT INTO staff (full_name, role, phone, email, shift, status, note, avatar, created_at, updated_at)
SELECT 'Đặng Thị Thu Hà', 'Bếp', '0908888888', 'ha@hotelhub.com',
       'Ca sáng: 6 giờ sáng - 2 giờ chiều', 'INACTIVE', 'Đã nghỉ việc', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'ha@hotelhub.com');

INSERT INTO staff (full_name, role, phone, email, shift, status, note, avatar, created_at, updated_at)
SELECT 'Vũ Đức Thắng', 'Quản lý', '0909999999', 'thang@hotelhub.com',
       'Cả ngày: 9 giờ sáng - 5 giờ chiều', 'WORKING', NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'thang@hotelhub.com');

INSERT INTO staff (full_name, role, phone, email, shift, status, note, avatar, created_at, updated_at)
SELECT 'Lý Thị Mỹ Dung', 'Bếp', '0910000000', 'dung@hotelhub.com',
       'Ca chiều: 2 giờ chiều - 10 giờ tối', 'ON_LEAVE', NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM staff WHERE email = 'dung@hotelhub.com');
