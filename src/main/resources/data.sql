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

-- ================================================================
--  Seed dữ liệu mẫu cho bảng special_requests
-- ================================================================

INSERT INTO special_requests (guest_name, guest_phone, request_type, content, status, admin_note, booking_id, created_at, updated_at)
SELECT 'Nguyễn Văn Hùng', '0901234567', 'Không làm phiền',
       'Không dọn phòng trong suốt thời gian lưu trú.',
       'PENDING', NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM special_requests WHERE guest_name = 'Nguyễn Văn Hùng' AND request_type = 'Không làm phiền');

INSERT INTO special_requests (guest_name, guest_phone, request_type, content, status, admin_note, booking_id, created_at, updated_at)
SELECT 'Lê Nguyễn Thế Kiệt', '0907654321', 'Không làm phiền',
       'Không dọn phòng trong suốt thời gian lưu trú.',
       'DONE', 'Đã thông báo bộ phận buồng phòng.', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM special_requests WHERE guest_name = 'Lê Nguyễn Thế Kiệt' AND request_type = 'Không làm phiền');

INSERT INTO special_requests (guest_name, guest_phone, request_type, content, status, admin_note, booking_id, created_at, updated_at)
SELECT 'Nguyễn Tuấn Anh', '0912345678', 'Mang hành lý lên phòng',
       'Có nhiều hành lý, cần hỗ trợ vận chuyển.',
       'DONE', 'Nhân viên đã hỗ trợ.', NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM special_requests WHERE guest_name = 'Nguyễn Tuấn Anh' AND request_type = 'Mang hành lý lên phòng');

INSERT INTO special_requests (guest_name, guest_phone, request_type, content, status, admin_note, booking_id, created_at, updated_at)
SELECT 'Trần Thị Mai', '0923456789', 'Yêu cầu đặc biệt khác',
       'Cần thêm gối mềm và chăn mỏng cho trẻ em.',
       'PENDING', NULL, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM special_requests WHERE guest_name = 'Trần Thị Mai' AND request_type = 'Yêu cầu đặc biệt khác');

-- ═══════════════════════════════════════════════════════════════════
-- PROMO CODES – Mã khuyến mãi mặc định
-- ═══════════════════════════════════════════════════════════════════
INSERT INTO promo_codes (code, label, discount_rate, active, created_at, updated_at)
SELECT 'HOTEL10', 'Giảm 10%', 0.10, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM promo_codes WHERE code = 'HOTEL10');

INSERT INTO promo_codes (code, label, discount_rate, active, created_at, updated_at)
SELECT 'SUMMER20', 'Giảm 20% Hè 2026', 0.20, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM promo_codes WHERE code = 'SUMMER20');

INSERT INTO promo_codes (code, label, discount_rate, active, created_at, updated_at)
SELECT 'NEWGUEST', 'Khách mới giảm 15%', 0.15, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM promo_codes WHERE code = 'NEWGUEST');

INSERT INTO promo_codes (code, label, discount_rate, active, created_at, updated_at)
SELECT 'VIP30', 'VIP giảm 30%', 0.30, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM promo_codes WHERE code = 'VIP30');

-- ═══════════════════════════════════════════════════════════════════
-- CANCELLATION POLICIES – Chính sách hoàn tiền mặc định
-- ═══════════════════════════════════════════════════════════════════
INSERT INTO cancellation_policies (label, min_hours, refund_rate, display_order, created_at, updated_at)
SELECT 'Hủy trước 72 giờ hoàn 100%', 72, 100, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cancellation_policies WHERE min_hours = 72);

INSERT INTO cancellation_policies (label, min_hours, refund_rate, display_order, created_at, updated_at)
SELECT 'Hủy trước 48 giờ hoàn 70%', 48, 70, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cancellation_policies WHERE min_hours = 48);

INSERT INTO cancellation_policies (label, min_hours, refund_rate, display_order, created_at, updated_at)
SELECT 'Hủy trước 24 giờ hoàn 50%', 24, 50, 2, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cancellation_policies WHERE min_hours = 24);

INSERT INTO cancellation_policies (label, min_hours, refund_rate, display_order, created_at, updated_at)
SELECT 'Hủy trong ngày không hoàn (0%)', 0, 0, 3, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cancellation_policies WHERE min_hours = 0);

-- ================================================================
--  ROOMS – Danh sách phòng khách sạn
-- ================================================================

-- Tầng 1 – Standard
INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '101', 'Standard', 'Phòng tiêu chuẩn thoáng mát, view sân vườn, thích hợp cho 2 khách.', 800000, 2, 'Trống', 1,
       'WiFi miễn phí,TV 40 inch,Điều hoà,Tủ lạnh mini,Két an toàn',
       'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '101');

INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '102', 'Standard', 'Phòng tiêu chuẩn yên tĩnh, gần thang máy, view hồ bơi.', 800000, 2, 'Trống', 1,
       'WiFi miễn phí,TV 40 inch,Điều hoà,Tủ lạnh mini,Két an toàn',
       'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '102');

INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '103', 'Standard', 'Phòng tiêu chuẩn hướng đông, đón nắng sáng, thoáng sáng.', 800000, 2, 'Bảo Trì', 1,
       'WiFi miễn phí,TV 40 inch,Điều hoà,Tủ lạnh mini,Két an toàn',
       'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '103');

-- Tầng 2 – Standard & Deluxe
INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '201', 'Standard', 'Phòng tiêu chuẩn cao cấp, thiết kế hiện đại, view thành phố.', 850000, 2, 'Đã Đặt', 2,
       'WiFi miễn phí,TV 42 inch,Điều hoà,Tủ lạnh mini,Két an toàn,Bàn làm việc',
       'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '201');

INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '202', 'Deluxe', 'Phòng Deluxe rộng rãi, giường King size, bồn tắm riêng, view thành phố tuyệt đẹp.', 1200000, 2, 'Trống', 2,
       'WiFi miễn phí,TV 50 inch,Điều hoà,Tủ lạnh,Bồn tắm,Két an toàn,Bàn làm việc,Dép & áo choàng tắm',
       'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '202');

INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '203', 'Deluxe', 'Phòng Deluxe sang trọng, 2 giường đơn, phù hợp cho cặp đôi hoặc bạn bè.', 1200000, 2, 'Đang ở', 2,
       'WiFi miễn phí,TV 50 inch,Điều hoà,Tủ lạnh,Bồn tắm,Két an toàn,Bàn làm việc,Dép & áo choàng tắm',
       'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '203');

-- Tầng 3 – Deluxe
INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '301', 'Deluxe', 'Phòng Deluxe góc nhìn toàn cảnh, thiết kế tối giản cao cấp.', 1300000, 2, 'Trống', 3,
       'WiFi miễn phí,TV 50 inch,Điều hoà,Tủ lạnh,Bồn tắm,Két an toàn,Bàn làm việc,Dép & áo choàng tắm,Minibar',
       'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '301');

INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '302', 'Deluxe', 'Phòng Deluxe tầng 3, view hồ bơi sân thượng, ánh sáng tự nhiên.', 1300000, 2, 'Trống', 3,
       'WiFi miễn phí,TV 50 inch,Điều hoà,Tủ lạnh,Bồn tắm,Két an toàn,Bàn làm việc,Dép & áo choàng tắm,Minibar',
       'https://images.unsplash.com/photo-1566665797739-1674de7a421a?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '302');

-- Tầng 4 – Junior Suite
INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '401', 'Junior Suite', 'Junior Suite rộng 45m², phòng khách riêng, bồn tắm đứng và bể ngâm, view thành phố 180°.', 2200000, 3, 'Trống', 4,
       'WiFi miễn phí,TV 55 inch,Điều hoà,Tủ lạnh,Bồn tắm & vòi sen,Két an toàn,Bàn làm việc,Dép & áo choàng tắm,Minibar,Phòng khách riêng,Bàn trang điểm',
       'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '401');

INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '402', 'Junior Suite', 'Junior Suite hướng tây, ngắm hoàng hôn, phòng ngủ và phòng khách riêng biệt.', 2200000, 3, 'Đã Đặt', 4,
       'WiFi miễn phí,TV 55 inch,Điều hoà,Tủ lạnh,Bồn tắm & vòi sen,Két an toàn,Bàn làm việc,Dép & áo choàng tắm,Minibar,Phòng khách riêng,Bàn trang điểm',
       'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '402');

-- Tầng 5 – Suite
INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '501', 'Suite', 'Suite cao cấp 60m², phòng ngủ chính + phòng khách + bếp nhỏ, ban công riêng view biển.', 3500000, 4, 'Trống', 5,
       'WiFi miễn phí,TV 65 inch,Điều hoà,Tủ lạnh lớn,Bồn tắm Jacuzzi,Két an toàn,Bàn làm việc,Dép & áo choàng tắm,Minibar đầy đủ,Phòng khách riêng,Bếp nhỏ,Ban công riêng,Máy pha cà phê',
       'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '501');

INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '502', 'Suite', 'Suite sang trọng 55m², thiết kế Đông Dương, không gian thư giãn cao cấp.', 3500000, 4, 'Trống', 5,
       'WiFi miễn phí,TV 65 inch,Điều hoà,Tủ lạnh lớn,Bồn tắm Jacuzzi,Két an toàn,Bàn làm việc,Dép & áo choàng tắm,Minibar đầy đủ,Phòng khách riêng,Bếp nhỏ,Ban công riêng,Máy pha cà phê',
       'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '502');

-- Tầng 6 – Family & Presidential
INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '601', 'Family', 'Phòng gia đình rộng 70m², 1 giường King + 2 giường đơn, phù hợp gia đình 4-5 người.', 2800000, 5, 'Trống', 6,
       'WiFi miễn phí,TV 60 inch,Điều hoà,Tủ lạnh,Bồn tắm & vòi sen,Két an toàn,Bàn làm việc,Dép & áo choàng tắm,Minibar,Khu vực chơi cho trẻ em,Máy pha cà phê',
       'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '601');

INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '602', 'Family', 'Phòng gia đình góc, view 2 chiều, thiết kế thân thiện cho trẻ em, cầu trượt mini.', 2800000, 5, 'Trống', 6,
       'WiFi miễn phí,TV 60 inch,Điều hoà,Tủ lạnh,Bồn tắm & vòi sen,Két an toàn,Bàn làm việc,Dép & áo choàng tắm,Minibar,Khu vực chơi cho trẻ em,Máy pha cà phê',
       'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '602');

INSERT INTO rooms (room_name, room_type, description, price, capacity, status, floor, amenities, image_url, maintenance, created_at, updated_at)
SELECT '701', 'Presidential Suite', 'Presidential Suite độc quyền 120m², 2 phòng ngủ, phòng khách rộng, nhà bếp đầy đủ, butler riêng 24/7.', 8500000, 6, 'Trống', 7,
       'WiFi miễn phí tốc độ cao,TV 75 inch,Điều hoà,Tủ lạnh lớn,Bồn tắm Jacuzzi & vòi sen mưa,Két an toàn,Bàn làm việc,Dép & áo choàng tắm cao cấp,Minibar cao cấp,Phòng khách rộng,Bếp đầy đủ,Ban công panorama,Máy pha cà phê Nespresso,Butler riêng,Hoa tươi hàng ngày,Đưa đón sân bay',
       'https://images.unsplash.com/photo-1631049552240-59c37f38802b?w=800', false, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM rooms WHERE room_name = '701');

-- ================================================================
--  ADDITIONAL SERVICES – Dịch vụ bổ sung
-- ================================================================

-- Gói ưu đãi
INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Gói Tuần Trăng Mật', 'Gói ưu đãi', 1500000, 'gói',
       'Bao gồm: hoa tươi trang trí phòng, rượu vang đỏ, trái cây theo mùa, nến thơm romantic và bữa sáng đôi tại phòng.',
       'https://images.unsplash.com/photo-1549294413-26f195471e99?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Gói Tuần Trăng Mật');

INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Gói Sinh Nhật Đặc Biệt', 'Gói ưu đãi', 800000, 'gói',
       'Bao gồm: bánh sinh nhật cá nhân hoá, bóng bay trang trí phòng, thiệp chúc mừng thủ công và 1 ly champagne.',
       'https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Gói Sinh Nhật Đặc Biệt');

INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Gói Chào Mừng VIP', 'Gói ưu đãi', 600000, 'gói',
       'Bao gồm: giỏ trái cây, nước khoáng cao cấp, bánh ngọt handmade, thiệp chào mừng từ ban quản lý khách sạn.',
       'https://images.unsplash.com/photo-1559650901-8d99b41dd8e2?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Gói Chào Mừng VIP');

-- Vé tham quan
INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Vé Tham Quan Phố Cổ Hội An', 'Vé tham quan', 120000, 'người',
       'Vé tham quan khu phố cổ Hội An UNESCO, bao gồm 5 điểm tham quan tự chọn. Có hướng dẫn viên tiếng Việt/Anh.',
       'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Vé Tham Quan Phố Cổ Hội An');

INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Tour Cù Lao Chàm 1 Ngày', 'Vé tham quan', 850000, 'người',
       'Tour tham quan đảo Cù Lao Chàm, snorkeling, câu mực đêm, ăn hải sản tươi. Khởi hành 7h sáng, về 18h.',
       'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Tour Cù Lao Chàm 1 Ngày');

INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Vé Thuyền Ngắm Hoàng Hôn', 'Vé tham quan', 350000, 'người',
       'Chuyến thuyền 2 tiếng ngắm hoàng hôn trên sông Thu Bồn. Bao gồm đèn lồng thả sông và đồ uống nhẹ.',
       'https://images.unsplash.com/photo-1477587458883-47145ed94245?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Vé Thuyền Ngắm Hoàng Hôn');

-- Ẩm thực
INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Bữa Sáng Tại Phòng', 'Ẩm thực', 180000, 'suất',
       'Bữa sáng phục vụ tại phòng: bánh mì, trứng các kiểu, nước ép trái cây tươi, cà phê/trà. Phục vụ 6h-10h.',
       'https://images.unsplash.com/photo-1533089860892-a7c6f0a88666?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Bữa Sáng Tại Phòng');

INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Bữa Tối Lãng Mạn Riêng Tư', 'Ẩm thực', 1200000, 'cặp',
       'Bữa tối 3 món cho 2 người tại ban công hoặc sân thượng riêng tư. Nến, hoa tươi và rượu vang trắng đi kèm.',
       'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Bữa Tối Lãng Mạn Riêng Tư');

INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Giỏ Trái Cây Cao Cấp', 'Ẩm thực', 250000, 'giỏ',
       'Giỏ trái cây tươi theo mùa cao cấp: xoài, vải, dưa hấu, nho ngoại, dứa. Chuẩn bị sẵn trong phòng khi nhận.',
       'https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Giỏ Trái Cây Cao Cấp');

-- Spa
INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Massage Thư Giãn Toàn Thân 60 Phút', 'Spa', 500000, 'lần',
       'Massage toàn thân với tinh dầu thiên nhiên, kỹ thuật Thái truyền thống. Bao gồm: ngâm chân thảo dược và trà thảo mộc.',
       'https://images.unsplash.com/photo-1600334089648-b0d9d3028eb2?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Massage Thư Giãn Toàn Thân 60 Phút');

INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Gói Chăm Sóc Da Mặt Cao Cấp', 'Spa', 650000, 'lần',
       'Liệu trình chăm sóc da mặt 75 phút với sản phẩm Dermalogica, bao gồm tẩy tế bào chết, đắp mặt nạ và massage mặt.',
       'https://images.unsplash.com/photo-1570172619644-dfd03ed5d881?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Gói Chăm Sóc Da Mặt Cao Cấp');

INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Gói Spa Đôi Đặc Biệt', 'Spa', 1800000, 'cặp',
       'Trải nghiệm spa 2 tiếng cho 2 người: massage toàn thân, tắm thảo dược, chăm sóc da mặt và thưởng thức bánh nhẹ.',
       'https://images.unsplash.com/photo-1544161515-4ab6ce6db874?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Gói Spa Đôi Đặc Biệt');

-- Vận chuyển
INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Đưa Đón Sân Bay (1 chiều)', 'Vận chuyển', 350000, 'lượt',
       'Dịch vụ đưa đón sân bay Đà Nẵng bằng xe 4 chỗ có điều hoà. Bao gồm nước uống và khăn lạnh trên xe.',
       'https://images.unsplash.com/photo-1449965408869-eaa3f722e40d?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Đưa Đón Sân Bay (1 chiều)');

INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Thuê Xe Máy (theo ngày)', 'Vận chuyển', 150000, 'ngày',
       'Thuê xe máy tự lái khám phá thành phố. Xe Honda Wave/Vision mới, có mũ bảo hiểm, bản đồ và hỗ trợ 24/7.',
       'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Thuê Xe Máy (theo ngày)');

INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Thuê Xe Đạp (theo ngày)', 'Vận chuyển', 80000, 'ngày',
       'Thuê xe đạp tham quan phố cổ, thân thiện môi trường. Bao gồm mũ bảo hiểm, giỏ xe và khóa chống trộm.',
       'https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=800', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Thuê Xe Đạp (theo ngày)');

INSERT INTO additional_services (name, category, price, unit, description, image_url, status, created_at, updated_at)
SELECT 'Giặt Là Nhanh (Express)', 'Gói ưu đãi', 120000, 'kg',
       'Dịch vụ giặt là nhanh 4 tiếng. Nhận đồ trước 10h trả trước 14h. Bao gồm ủi phẳng và đóng gói cẩn thận.',
       'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800', 'INACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM additional_services WHERE name = 'Giặt Là Nhanh (Express)');

-- ================================================================
--  USERS – Khách hàng mẫu
--  Password chung: Password@123
--  BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh13
-- ================================================================

INSERT INTO users (full_name, email, password, role, created_at)
SELECT 'Nguyễn Văn An', 'an.nguyen@gmail.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh13',
       'GUEST', '2025-10-01 08:00:00'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'an.nguyen@gmail.com');

INSERT INTO users (full_name, email, password, role, created_at)
SELECT 'Trần Thị Bình', 'binh.tran@gmail.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh13',
       'GUEST', '2025-10-15 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'binh.tran@gmail.com');

INSERT INTO users (full_name, email, password, role, created_at)
SELECT 'Lê Minh Châu', 'chau.le@gmail.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh13',
       'GUEST', '2025-11-05 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'chau.le@gmail.com');

INSERT INTO users (full_name, email, password, role, created_at)
SELECT 'Phạm Quốc Dũng', 'dung.pham@gmail.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh13',
       'GUEST', '2025-11-20 14:00:00'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'dung.pham@gmail.com');

INSERT INTO users (full_name, email, password, role, created_at)
SELECT 'Hoàng Thị Emm', 'emm.hoang@gmail.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh13',
       'GUEST', '2025-12-01 11:00:00'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'emm.hoang@gmail.com');

INSERT INTO users (full_name, email, password, role, created_at)
SELECT 'Vũ Thanh Phong', 'phong.vu@gmail.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh13',
       'GUEST', '2025-12-10 16:00:00'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'phong.vu@gmail.com');

INSERT INTO users (full_name, email, password, role, created_at)
SELECT 'Đặng Thị Giang', 'giang.dang@gmail.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh13',
       'GUEST', '2026-01-05 08:30:00'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'giang.dang@gmail.com');

INSERT INTO users (full_name, email, password, role, created_at)
SELECT 'Bùi Văn Hải', 'hai.bui@gmail.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh13',
       'GUEST', '2026-01-20 13:00:00'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'hai.bui@gmail.com');

-- ================================================================
--  BOOKINGS – Đặt phòng mẫu (quá khứ CHECKED_OUT, hiện tại, tương lai)
-- ================================================================

-- Booking đã check-out (để gắn review & payment)
INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Nguyễn Văn An', '0901111001', 'an.nguyen@gmail.com', 'Deluxe', 1200000,
       '2026-01-05', '2026-01-08', 3, 1, 2, 0, 3600000, 'CHECKED_OUT', NULL,
       (SELECT id FROM users WHERE email = 'an.nguyen@gmail.com'), '2025-12-20 10:00:00', '2026-01-08 12:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'an.nguyen@gmail.com' AND check_in = '2026-01-05');

INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Trần Thị Bình', '0902222002', 'binh.tran@gmail.com', 'Suite', 3500000,
       '2026-01-10', '2026-01-15', 5, 1, 2, 1, 17500000, 'CHECKED_OUT', 'Tuần trăng mật, cần hoa trang trí phòng.',
       (SELECT id FROM users WHERE email = 'binh.tran@gmail.com'), '2025-12-25 09:00:00', '2026-01-15 11:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'binh.tran@gmail.com' AND check_in = '2026-01-10');

INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Lê Minh Châu', '0903333003', 'chau.le@gmail.com', 'Standard', 800000,
       '2026-01-20', '2026-01-23', 3, 1, 1, 0, 2400000, 'CHECKED_OUT', NULL,
       (SELECT id FROM users WHERE email = 'chau.le@gmail.com'), '2026-01-10 14:00:00', '2026-01-23 12:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'chau.le@gmail.com' AND check_in = '2026-01-20');

INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Phạm Quốc Dũng', '0904444004', 'dung.pham@gmail.com', 'Junior Suite', 2200000,
       '2026-02-01', '2026-02-04', 3, 1, 2, 0, 6600000, 'CHECKED_OUT', 'Cần phòng yên tĩnh để làm việc.',
       (SELECT id FROM users WHERE email = 'dung.pham@gmail.com'), '2026-01-15 08:00:00', '2026-02-04 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'dung.pham@gmail.com' AND check_in = '2026-02-01');

INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Hoàng Thị Emm', '0905555005', 'emm.hoang@gmail.com', 'Family', 2800000,
       '2026-02-10', '2026-02-15', 5, 1, 2, 2, 14000000, 'CHECKED_OUT', 'Gia đình có 2 trẻ nhỏ, cần cũi cho em bé.',
       (SELECT id FROM users WHERE email = 'emm.hoang@gmail.com'), '2026-01-28 11:00:00', '2026-02-15 11:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'emm.hoang@gmail.com' AND check_in = '2026-02-10');

INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Vũ Thanh Phong', '0906666006', 'phong.vu@gmail.com', 'Deluxe', 1300000,
       '2026-02-20', '2026-02-25', 5, 1, 2, 0, 6500000, 'CHECKED_OUT', NULL,
       (SELECT id FROM users WHERE email = 'phong.vu@gmail.com'), '2026-02-05 15:00:00', '2026-02-25 12:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'phong.vu@gmail.com' AND check_in = '2026-02-20');

INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Đặng Thị Giang', '0907777007', 'giang.dang@gmail.com', 'Suite', 3500000,
       '2026-03-01', '2026-03-05', 4, 1, 2, 0, 14000000, 'CHECKED_OUT', 'Kỷ niệm ngày cưới lần thứ 5.',
       (SELECT id FROM users WHERE email = 'giang.dang@gmail.com'), '2026-02-15 09:00:00', '2026-03-05 11:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'giang.dang@gmail.com' AND check_in = '2026-03-01');

INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Bùi Văn Hải', '0908888008', 'hai.bui@gmail.com', 'Standard', 850000,
       '2026-03-10', '2026-03-13', 3, 1, 1, 0, 2550000, 'CHECKED_OUT', NULL,
       (SELECT id FROM users WHERE email = 'hai.bui@gmail.com'), '2026-02-28 10:00:00', '2026-03-13 12:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'hai.bui@gmail.com' AND check_in = '2026-03-10');

-- Booking đang ở (CHECKED_IN)
INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Nguyễn Văn An', '0901111001', 'an.nguyen@gmail.com', 'Junior Suite', 2200000,
       '2026-03-20', '2026-03-24', 4, 1, 2, 0, 8800000, 'CHECKED_IN', NULL,
       (SELECT id FROM users WHERE email = 'an.nguyen@gmail.com'), '2026-03-10 08:00:00', '2026-03-20 14:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'an.nguyen@gmail.com' AND check_in = '2026-03-20');

INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Lê Minh Châu', '0903333003', 'chau.le@gmail.com', 'Deluxe', 1200000,
       '2026-03-19', '2026-03-23', 4, 1, 2, 0, 4800000, 'CHECKED_IN', 'Cần phòng có view tốt.',
       (SELECT id FROM users WHERE email = 'chau.le@gmail.com'), '2026-03-05 11:00:00', '2026-03-19 15:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'chau.le@gmail.com' AND check_in = '2026-03-19');

-- Booking đã xác nhận (CONFIRMED – sắp tới)
INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Phạm Quốc Dũng', '0904444004', 'dung.pham@gmail.com', 'Suite', 3500000,
       '2026-04-01', '2026-04-05', 4, 1, 2, 0, 14000000, 'CONFIRMED', NULL,
       (SELECT id FROM users WHERE email = 'dung.pham@gmail.com'), '2026-03-15 09:00:00', '2026-03-15 09:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'dung.pham@gmail.com' AND check_in = '2026-04-01');

INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Hoàng Thị Emm', '0905555005', 'emm.hoang@gmail.com', 'Family', 2800000,
       '2026-04-10', '2026-04-15', 5, 1, 2, 2, 14000000, 'CONFIRMED', 'Gia đình 4 người, cần giường phụ.',
       (SELECT id FROM users WHERE email = 'emm.hoang@gmail.com'), '2026-03-18 14:00:00', '2026-03-18 14:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'emm.hoang@gmail.com' AND check_in = '2026-04-10');

-- Booking chờ xử lý (PENDING)
INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, user_id, created_at, updated_at)
SELECT 'Vũ Thanh Phong', '0906666006', 'phong.vu@gmail.com', 'Presidential Suite', 8500000,
       '2026-05-01', '2026-05-05', 4, 1, 2, 0, 34000000, 'PENDING', 'Muốn thuê thêm xe đưa đón sân bay.',
       (SELECT id FROM users WHERE email = 'phong.vu@gmail.com'), '2026-03-21 16:00:00', '2026-03-21 16:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'phong.vu@gmail.com' AND check_in = '2026-05-01');

-- Booking đã hủy (CANCELLED) – khách hủy sớm hoàn 100%
INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, cancel_reason, cancelled_at, refund_rate, refund_amount, refund_status, process_note, applied_policy, user_id, created_at, updated_at)
SELECT 'Bùi Văn Hải', '0908888008', 'hai.bui@gmail.com', 'Deluxe', 1300000,
       '2026-02-28', '2026-03-03', 3, 1, 2, 0, 3900000, 'CANCELLED',
       NULL, 'Thay đổi kế hoạch công tác đột xuất.',
       '2026-02-20 10:00:00', 100.00, 3900000, 'REFUNDED',
       'Khách hủy trước 8 ngày, hoàn 100% theo chính sách.', 'Hủy trước 72 giờ hoàn 100%',
       (SELECT id FROM users WHERE email = 'hai.bui@gmail.com'), '2026-02-15 11:00:00', '2026-02-20 10:30:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'hai.bui@gmail.com' AND check_in = '2026-02-28');

-- Booking đã hủy – khách vãng lai hủy trong ngày, không hoàn
INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, cancel_reason, cancelled_at, refund_rate, refund_amount, refund_status, process_note, applied_policy, user_id, created_at, updated_at)
SELECT 'Khách Vãng Lai', '0999888777', NULL, 'Standard', 800000,
       '2026-03-18', '2026-03-20', 2, 1, 1, 0, 1600000, 'CANCELLED',
       NULL, 'Khách không đến nhận phòng.',
       '2026-03-18 14:00:00', 0.00, 0, 'REJECTED',
       'Hủy trong ngày check-in, không hoàn tiền theo chính sách.', 'Hủy trong ngày không hoàn (0%)',
       NULL, '2026-03-17 20:00:00', '2026-03-18 14:00:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_name = 'Khách Vãng Lai' AND check_in = '2026-03-18');

-- Booking đã hủy – hoàn 70%
INSERT INTO bookings (guest_name, guest_phone, guest_email, room_type, price_per_night, check_in, check_out, nights, rooms, adults, children, total_amount, status, note, cancel_reason, cancelled_at, refund_rate, refund_amount, refund_status, process_note, applied_policy, user_id, created_at, updated_at)
SELECT 'Đặng Thị Giang', '0907777007', 'giang.dang@gmail.com', 'Deluxe', 1300000,
       '2026-04-20', '2026-04-23', 3, 1, 2, 0, 3900000, 'CANCELLED',
       NULL, 'Bận việc đột xuất không đi được.',
       '2026-04-18 09:00:00', 70.00, 2730000, 'PENDING_REFUND',
       'Hủy trước 48 giờ, hoàn 70% đang xử lý.', 'Hủy trước 48 giờ hoàn 70%',
       (SELECT id FROM users WHERE email = 'giang.dang@gmail.com'), '2026-04-10 08:00:00', '2026-04-18 09:30:00'
WHERE NOT EXISTS (SELECT 1 FROM bookings WHERE guest_email = 'giang.dang@gmail.com' AND check_in = '2026-04-20');

-- ================================================================
--  PAYMENTS – Thanh toán cho các booking CHECKED_OUT
-- ================================================================

INSERT INTO payments (booking_id, subtotal, discount_amount, vat_amount, total_amount, method, status, loyalty_points_used, loyalty_discount, loyalty_points_earned, transaction_ref, gateway_response_code, gateway_message, created_at, completed_at, expires_at)
SELECT b.id, 3600000, 0, 360000, 3960000, 'CASH', 'SUCCESS', 0, 0, 3600, 'HTH-20260105-C001', '00', 'Thanh toán thành công', '2026-01-05 14:00:00', '2026-01-05 14:05:00', '2026-01-05 14:30:00'
FROM bookings b WHERE b.guest_email = 'an.nguyen@gmail.com' AND b.check_in = '2026-01-05'
AND NOT EXISTS (SELECT 1 FROM payments WHERE transaction_ref = 'HTH-20260105-C001');

INSERT INTO payments (booking_id, subtotal, discount_amount, vat_amount, total_amount, method, status, promo_code, promo_discount_rate, loyalty_points_used, loyalty_discount, loyalty_points_earned, transaction_ref, gateway_response_code, gateway_message, created_at, completed_at, expires_at)
SELECT b.id, 17500000, 3500000, 1400000, 15400000, 'CARD', 'SUCCESS', 'VIP30', 0.30, 0, 0, 17500, 'HTH-20260110-C002', '00', 'Thanh toán thành công', '2026-01-10 15:00:00', '2026-01-10 15:03:00', '2026-01-10 15:30:00'
FROM bookings b WHERE b.guest_email = 'binh.tran@gmail.com' AND b.check_in = '2026-01-10'
AND NOT EXISTS (SELECT 1 FROM payments WHERE transaction_ref = 'HTH-20260110-C002');

INSERT INTO payments (booking_id, subtotal, discount_amount, vat_amount, total_amount, method, status, loyalty_points_used, loyalty_discount, loyalty_points_earned, transaction_ref, gateway_response_code, gateway_message, created_at, completed_at, expires_at)
SELECT b.id, 2400000, 0, 240000, 2640000, 'QR', 'SUCCESS', 0, 0, 2400, 'HTH-20260120-C003', '00', 'Thanh toán thành công', '2026-01-20 16:00:00', '2026-01-20 16:01:00', '2026-01-20 16:30:00'
FROM bookings b WHERE b.guest_email = 'chau.le@gmail.com' AND b.check_in = '2026-01-20'
AND NOT EXISTS (SELECT 1 FROM payments WHERE transaction_ref = 'HTH-20260120-C003');

INSERT INTO payments (booking_id, subtotal, discount_amount, vat_amount, total_amount, method, status, promo_code, promo_discount_rate, loyalty_points_used, loyalty_discount, loyalty_points_earned, transaction_ref, gateway_response_code, gateway_message, created_at, completed_at, expires_at)
SELECT b.id, 6600000, 990000, 561000, 6171000, 'WALLET', 'SUCCESS', 'HOTEL10', 0.10, 0, 0, 6600, 'HTH-20260201-C004', '00', 'Thanh toán thành công', '2026-02-01 14:00:00', '2026-02-01 14:02:00', '2026-02-01 14:30:00'
FROM bookings b WHERE b.guest_email = 'dung.pham@gmail.com' AND b.check_in = '2026-02-01'
AND NOT EXISTS (SELECT 1 FROM payments WHERE transaction_ref = 'HTH-20260201-C004');

INSERT INTO payments (booking_id, subtotal, discount_amount, vat_amount, total_amount, method, status, loyalty_points_used, loyalty_discount, loyalty_points_earned, transaction_ref, gateway_response_code, gateway_message, created_at, completed_at, expires_at)
SELECT b.id, 14000000, 0, 1400000, 15400000, 'CARD', 'SUCCESS', 0, 0, 14000, 'HTH-20260210-C005', '00', 'Thanh toán thành công', '2026-02-10 15:00:00', '2026-02-10 15:04:00', '2026-02-10 15:30:00'
FROM bookings b WHERE b.guest_email = 'emm.hoang@gmail.com' AND b.check_in = '2026-02-10'
AND NOT EXISTS (SELECT 1 FROM payments WHERE transaction_ref = 'HTH-20260210-C005');

INSERT INTO payments (booking_id, subtotal, discount_amount, vat_amount, total_amount, method, status, promo_code, promo_discount_rate, loyalty_points_used, loyalty_discount, loyalty_points_earned, transaction_ref, gateway_response_code, gateway_message, created_at, completed_at, expires_at)
SELECT b.id, 6500000, 975000, 552500, 6077500, 'QR', 'SUCCESS', 'NEWGUEST', 0.15, 0, 0, 6500, 'HTH-20260220-C006', '00', 'Thanh toán thành công', '2026-02-20 16:00:00', '2026-02-20 16:00:30', '2026-02-20 16:30:00'
FROM bookings b WHERE b.guest_email = 'phong.vu@gmail.com' AND b.check_in = '2026-02-20'
AND NOT EXISTS (SELECT 1 FROM payments WHERE transaction_ref = 'HTH-20260220-C006');

INSERT INTO payments (booking_id, subtotal, discount_amount, vat_amount, total_amount, method, status, loyalty_points_used, loyalty_discount, loyalty_points_earned, transaction_ref, gateway_response_code, gateway_message, created_at, completed_at, expires_at)
SELECT b.id, 14000000, 0, 1400000, 15400000, 'CARD', 'SUCCESS', 0, 0, 14000, 'HTH-20260301-C007', '00', 'Thanh toán thành công', '2026-03-01 15:00:00', '2026-03-01 15:02:00', '2026-03-01 15:30:00'
FROM bookings b WHERE b.guest_email = 'giang.dang@gmail.com' AND b.check_in = '2026-03-01'
AND NOT EXISTS (SELECT 1 FROM payments WHERE transaction_ref = 'HTH-20260301-C007');

INSERT INTO payments (booking_id, subtotal, discount_amount, vat_amount, total_amount, method, status, loyalty_points_used, loyalty_discount, loyalty_points_earned, transaction_ref, gateway_response_code, gateway_message, created_at, completed_at, expires_at)
SELECT b.id, 2550000, 0, 255000, 2805000, 'CASH', 'SUCCESS', 0, 0, 2550, 'HTH-20260310-C008', '00', 'Thanh toán thành công', '2026-03-10 16:00:00', '2026-03-10 16:05:00', '2026-03-10 16:30:00'
FROM bookings b WHERE b.guest_email = 'hai.bui@gmail.com' AND b.check_in = '2026-03-10'
AND NOT EXISTS (SELECT 1 FROM payments WHERE transaction_ref = 'HTH-20260310-C008');

-- ================================================================
--  REVIEWS – Đánh giá (gắn vào booking CHECKED_OUT, có user)
-- ================================================================

INSERT INTO reviews (booking_id, user_id, rating, title, comment, room_rating, service_rating, location_rating, cleanliness_rating, amenities_rating, value_rating, status, created_at, updated_at)
SELECT b.id, (SELECT id FROM users WHERE email = 'an.nguyen@gmail.com'),
       4, 'Phòng đẹp, dịch vụ tốt',
       'Phòng Deluxe rất thoáng mát và sạch sẽ. Nhân viên lễ tân thân thiện và hỗ trợ nhiệt tình. Bữa sáng phong phú. Sẽ quay lại lần sau.',
       4, 5, 4, 4, 4, 4, 'APPROVED', '2026-01-09 10:00:00', '2026-01-09 10:00:00'
FROM bookings b WHERE b.guest_email = 'an.nguyen@gmail.com' AND b.check_in = '2026-01-05'
AND NOT EXISTS (SELECT 1 FROM reviews WHERE booking_id = b.id);

INSERT INTO reviews (booking_id, user_id, rating, title, comment, room_rating, service_rating, location_rating, cleanliness_rating, amenities_rating, value_rating, reply_text, replied_at, status, created_at, updated_at)
SELECT b.id, (SELECT id FROM users WHERE email = 'binh.tran@gmail.com'),
       5, 'Kỳ nghỉ hoàn hảo không thể quên',
       'Suite cực kỳ sang trọng, bồn tắm Jacuzzi tuyệt vời. Khách sạn đã chuẩn bị hoa trang trí và rượu vang cho ngày đặc biệt của chúng tôi. Cảm ơn đội ngũ nhân viên!',
       5, 5, 5, 5, 5, 5,
       'Cảm ơn quý khách đã tin tưởng lựa chọn HotelHub cho kỳ nghỉ tuần trăng mật. Chúc mừng hạnh phúc!', '2026-01-16 09:00:00',
       'APPROVED', '2026-01-16 08:00:00', '2026-01-16 09:00:00'
FROM bookings b WHERE b.guest_email = 'binh.tran@gmail.com' AND b.check_in = '2026-01-10'
AND NOT EXISTS (SELECT 1 FROM reviews WHERE booking_id = b.id);

INSERT INTO reviews (booking_id, user_id, rating, title, comment, room_rating, service_rating, location_rating, cleanliness_rating, amenities_rating, value_rating, status, created_at, updated_at)
SELECT b.id, (SELECT id FROM users WHERE email = 'chau.le@gmail.com'),
       3, 'Tạm ổn nhưng cần cải thiện',
       'Phòng Standard khá nhỏ so với giá tiền. Wifi chập chờn, điều hòa có tiếng ồn vào ban đêm. Tuy nhiên vị trí thuận tiện và nhân viên thân thiện.',
       3, 4, 5, 3, 3, 3, 'APPROVED', '2026-01-24 14:00:00', '2026-01-24 14:00:00'
FROM bookings b WHERE b.guest_email = 'chau.le@gmail.com' AND b.check_in = '2026-01-20'
AND NOT EXISTS (SELECT 1 FROM reviews WHERE booking_id = b.id);

INSERT INTO reviews (booking_id, user_id, rating, title, comment, room_rating, service_rating, location_rating, cleanliness_rating, amenities_rating, value_rating, reply_text, replied_at, status, created_at, updated_at)
SELECT b.id, (SELECT id FROM users WHERE email = 'dung.pham@gmail.com'),
       4, 'Junior Suite xứng đáng với giá tiền',
       'Phòng rộng rãi và trang bị đầy đủ cho công tác. Tốc độ WiFi tốt, bàn làm việc tiện nghi. Check-in nhanh gọn. Nhược điểm nhỏ là tiếng ồn từ hành lang.',
       4, 4, 4, 5, 4, 4,
       'Cảm ơn phản hồi quý giá của quý khách. Chúng tôi sẽ cải thiện vấn đề cách âm hành lang. Hẹn gặp lại!', '2026-02-06 10:00:00',
       'APPROVED', '2026-02-05 16:00:00', '2026-02-06 10:00:00'
FROM bookings b WHERE b.guest_email = 'dung.pham@gmail.com' AND b.check_in = '2026-02-01'
AND NOT EXISTS (SELECT 1 FROM reviews WHERE booking_id = b.id);

INSERT INTO reviews (booking_id, user_id, rating, title, comment, room_rating, service_rating, location_rating, cleanliness_rating, amenities_rating, value_rating, status, created_at, updated_at)
SELECT b.id, (SELECT id FROM users WHERE email = 'emm.hoang@gmail.com'),
       5, 'Hoàn hảo cho gia đình, con trẻ rất thích!',
       'Phòng Family rộng rãi, có khu vực chơi cho trẻ em rất hay. Nhân viên cung cấp cũi và đồ chơi cho bé rất chu đáo. Hồ bơi sạch sẽ. Cả nhà đều hài lòng!',
       5, 5, 5, 5, 5, 5, 'APPROVED', '2026-02-16 09:00:00', '2026-02-16 09:00:00'
FROM bookings b WHERE b.guest_email = 'emm.hoang@gmail.com' AND b.check_in = '2026-02-10'
AND NOT EXISTS (SELECT 1 FROM reviews WHERE booking_id = b.id);

INSERT INTO reviews (booking_id, user_id, rating, title, comment, room_rating, service_rating, location_rating, cleanliness_rating, amenities_rating, value_rating, status, created_at, updated_at)
SELECT b.id, (SELECT id FROM users WHERE email = 'phong.vu@gmail.com'),
       4, 'Phòng Deluxe tầng 3 rất đáng tiền',
       'View hồ bơi sân thượng rất đẹp, đặc biệt vào ban đêm. Minibar đủ đồ uống. Chỉ tiếc là bữa sáng không nhiều lựa chọn cho người ăn chay.',
       5, 4, 4, 4, 4, 3, 'APPROVED', '2026-02-26 11:00:00', '2026-02-26 11:00:00'
FROM bookings b WHERE b.guest_email = 'phong.vu@gmail.com' AND b.check_in = '2026-02-20'
AND NOT EXISTS (SELECT 1 FROM reviews WHERE booking_id = b.id);

INSERT INTO reviews (booking_id, user_id, rating, title, comment, room_rating, service_rating, location_rating, cleanliness_rating, amenities_rating, value_rating, reply_text, replied_at, status, created_at, updated_at)
SELECT b.id, (SELECT id FROM users WHERE email = 'giang.dang@gmail.com'),
       5, 'Suite hoàn hảo cho dịp đặc biệt',
       'Tuyệt vời! Suite rộng và sang trọng, ban công nhìn ra thành phố rất đẹp. Khách sạn tặng thêm chai rượu vang và bưu thiếp kỷ niệm ngày cưới. Nhân viên chuyên nghiệp và ân cần.',
       5, 5, 5, 5, 5, 5,
       'HotelHub trân trọng cảm ơn quý khách đã tin tưởng chọn chúng tôi cho ngày kỷ niệm ý nghĩa. Chúc quý vị hạnh phúc mãi!', '2026-03-06 09:00:00',
       'APPROVED', '2026-03-06 08:00:00', '2026-03-06 09:00:00'
FROM bookings b WHERE b.guest_email = 'giang.dang@gmail.com' AND b.check_in = '2026-03-01'
AND NOT EXISTS (SELECT 1 FROM reviews WHERE booking_id = b.id);

INSERT INTO reviews (booking_id, user_id, rating, title, comment, room_rating, service_rating, location_rating, cleanliness_rating, amenities_rating, value_rating, status, created_at, updated_at)
SELECT b.id, (SELECT id FROM users WHERE email = 'hai.bui@gmail.com'),
       4, 'Lưu trú ngắn ngày nhưng rất hài lòng',
       'Check-in nhanh, phòng sạch sẽ gọn gàng. Vị trí trung tâm tiện di chuyển. Nhân viên niềm nở. Sẽ chọn HotelHub cho các chuyến công tác tiếp theo.',
       4, 4, 5, 4, 4, 4, 'PENDING', '2026-03-14 10:00:00', '2026-03-14 10:00:00'
FROM bookings b WHERE b.guest_email = 'hai.bui@gmail.com' AND b.check_in = '2026-03-10'
AND NOT EXISTS (SELECT 1 FROM reviews WHERE booking_id = b.id);

-- ================================================================
--  LOYALTY ACCOUNTS & TRANSACTIONS – Tích điểm khách hàng
-- ================================================================

INSERT INTO loyalty_accounts (user_id, current_points, total_earned_points, tier, created_at, updated_at)
SELECT id, 3600, 3600, 'GOLD', NOW(), NOW() FROM users WHERE email = 'an.nguyen@gmail.com'
AND NOT EXISTS (SELECT 1 FROM loyalty_accounts WHERE user_id = (SELECT id FROM users WHERE email = 'an.nguyen@gmail.com'));

INSERT INTO loyalty_accounts (user_id, current_points, total_earned_points, tier, created_at, updated_at)
SELECT id, 31500, 31500, 'PLATINUM', NOW(), NOW() FROM users WHERE email = 'binh.tran@gmail.com'
AND NOT EXISTS (SELECT 1 FROM loyalty_accounts WHERE user_id = (SELECT id FROM users WHERE email = 'binh.tran@gmail.com'));

INSERT INTO loyalty_accounts (user_id, current_points, total_earned_points, tier, created_at, updated_at)
SELECT id, 2400, 2400, 'GOLD', NOW(), NOW() FROM users WHERE email = 'chau.le@gmail.com'
AND NOT EXISTS (SELECT 1 FROM loyalty_accounts WHERE user_id = (SELECT id FROM users WHERE email = 'chau.le@gmail.com'));

INSERT INTO loyalty_accounts (user_id, current_points, total_earned_points, tier, created_at, updated_at)
SELECT id, 9000, 9000, 'PLATINUM', NOW(), NOW() FROM users WHERE email = 'dung.pham@gmail.com'
AND NOT EXISTS (SELECT 1 FROM loyalty_accounts WHERE user_id = (SELECT id FROM users WHERE email = 'dung.pham@gmail.com'));

INSERT INTO loyalty_accounts (user_id, current_points, total_earned_points, tier, created_at, updated_at)
SELECT id, 14000, 14000, 'PLATINUM', NOW(), NOW() FROM users WHERE email = 'emm.hoang@gmail.com'
AND NOT EXISTS (SELECT 1 FROM loyalty_accounts WHERE user_id = (SELECT id FROM users WHERE email = 'emm.hoang@gmail.com'));

INSERT INTO loyalty_accounts (user_id, current_points, total_earned_points, tier, created_at, updated_at)
SELECT id, 6500, 6500, 'PLATINUM', NOW(), NOW() FROM users WHERE email = 'phong.vu@gmail.com'
AND NOT EXISTS (SELECT 1 FROM loyalty_accounts WHERE user_id = (SELECT id FROM users WHERE email = 'phong.vu@gmail.com'));

INSERT INTO loyalty_accounts (user_id, current_points, total_earned_points, tier, created_at, updated_at)
SELECT id, 14000, 14000, 'PLATINUM', NOW(), NOW() FROM users WHERE email = 'giang.dang@gmail.com'
AND NOT EXISTS (SELECT 1 FROM loyalty_accounts WHERE user_id = (SELECT id FROM users WHERE email = 'giang.dang@gmail.com'));

INSERT INTO loyalty_accounts (user_id, current_points, total_earned_points, tier, created_at, updated_at)
SELECT id, 2550, 2550, 'GOLD', NOW(), NOW() FROM users WHERE email = 'hai.bui@gmail.com'
AND NOT EXISTS (SELECT 1 FROM loyalty_accounts WHERE user_id = (SELECT id FROM users WHERE email = 'hai.bui@gmail.com'));

-- ================================================================
--  SPECIAL REQUESTS – Bổ sung thêm yêu cầu đặc biệt đa dạng
-- ================================================================

INSERT INTO special_requests (guest_name, guest_phone, request_type, content, status, admin_note, booking_id, created_at, updated_at)
SELECT 'Trần Thị Bình', '0902222002', 'Trang trí phòng đặc biệt',
       'Cần chuẩn bị hoa hồng đỏ và nến thơm trong phòng trước khi khách đến. Đây là kỳ nghỉ tuần trăng mật.',
       'DONE', 'Đã chuẩn bị đầy đủ theo yêu cầu, khách rất hài lòng.',
       (SELECT id FROM bookings WHERE guest_email = 'binh.tran@gmail.com' AND check_in = '2026-01-10'),
       '2026-01-08 09:00:00', '2026-01-10 13:00:00'
WHERE NOT EXISTS (SELECT 1 FROM special_requests WHERE guest_name = 'Trần Thị Bình' AND request_type = 'Trang trí phòng đặc biệt');

INSERT INTO special_requests (guest_name, guest_phone, request_type, content, status, admin_note, booking_id, created_at, updated_at)
SELECT 'Hoàng Thị Emm', '0905555005', 'Thiết bị cho trẻ em',
       'Cần 1 cái cũi cho em bé 8 tháng tuổi và thêm 1 gối mềm nhỏ. Phòng Family phòng 601.',
       'DONE', 'Đã chuẩn bị cũi và gối cho bé trước khi khách nhận phòng.',
       (SELECT id FROM bookings WHERE guest_email = 'emm.hoang@gmail.com' AND check_in = '2026-02-10'),
       '2026-02-08 14:00:00', '2026-02-10 13:00:00'
WHERE NOT EXISTS (SELECT 1 FROM special_requests WHERE guest_name = 'Hoàng Thị Emm' AND request_type = 'Thiết bị cho trẻ em');

INSERT INTO special_requests (guest_name, guest_phone, request_type, content, status, admin_note, booking_id, created_at, updated_at)
SELECT 'Nguyễn Văn An', '0901111001', 'Check-in sớm',
       'Chuyến bay đến lúc 8 giờ sáng, mong khách sạn hỗ trợ check-in sớm trước 12 giờ nếu phòng trống.',
       'APPROVED', 'Đã sắp xếp phòng sẵn, khách có thể check-in từ 10 giờ sáng.',
       (SELECT id FROM bookings WHERE guest_email = 'an.nguyen@gmail.com' AND check_in = '2026-03-20'),
       '2026-03-18 20:00:00', '2026-03-19 08:00:00'
WHERE NOT EXISTS (SELECT 1 FROM special_requests WHERE guest_name = 'Nguyễn Văn An' AND request_type = 'Check-in sớm');

INSERT INTO special_requests (guest_name, guest_phone, request_type, content, status, admin_note, booking_id, created_at, updated_at)
SELECT 'Phạm Quốc Dũng', '0904444004', 'Dịch vụ phòng đặc biệt',
       'Cần bổ sung thêm 2 chai nước khoáng loại 1.5 lít mỗi ngày và 1 hộp cà phê hòa tan trong phòng trong suốt thời gian lưu trú.',
       'PENDING', NULL,
       (SELECT id FROM bookings WHERE guest_email = 'dung.pham@gmail.com' AND check_in = '2026-04-01'),
       '2026-03-20 10:00:00', '2026-03-20 10:00:00'
WHERE NOT EXISTS (SELECT 1 FROM special_requests WHERE guest_name = 'Phạm Quốc Dũng' AND request_type = 'Dịch vụ phòng đặc biệt');

INSERT INTO special_requests (guest_name, guest_phone, request_type, content, status, admin_note, booking_id, created_at, updated_at)
SELECT 'Vũ Thanh Phong', '0906666006', 'Yêu cầu đặc biệt khác',
       'Cần đặt trước 1 bó hoa tulip vàng và 1 bộ đồ ăn tối riêng tư tại ban công Presidential Suite cho tối đầu tiên.',
       'APPROVED', 'Đã liên hệ bộ phận bếp và đặt hoa. Tất cả sẵn sàng cho ngày 01/05.',
       (SELECT id FROM bookings WHERE guest_email = 'phong.vu@gmail.com' AND check_in = '2026-05-01'),
       '2026-03-22 09:00:00', '2026-03-22 11:00:00'
WHERE NOT EXISTS (SELECT 1 FROM special_requests WHERE guest_name = 'Vũ Thanh Phong' AND request_type = 'Yêu cầu đặc biệt khác');

INSERT INTO special_requests (guest_name, guest_phone, request_type, content, status, admin_note, booking_id, created_at, updated_at)
SELECT 'Lê Minh Châu', '0903333003', 'Phòng không hút thuốc',
       'Yêu cầu tuyệt đối không xếp phòng gần khu vực hút thuốc. Có dị ứng với khói thuốc.',
       'DONE', 'Đã ghi chú và sắp xếp phòng 302 tầng 3, xa hoàn toàn khu hút thuốc.',
       NULL, '2026-03-04 16:00:00', '2026-03-05 08:00:00'
WHERE NOT EXISTS (SELECT 1 FROM special_requests WHERE guest_name = 'Lê Minh Châu' AND request_type = 'Phòng không hút thuốc');

INSERT INTO special_requests (guest_name, guest_phone, request_type, content, status, admin_note, booking_id, created_at, updated_at)
SELECT 'Khách Đặt Online', '0988776655', 'Đón tại sân bay',
       'Chuyến bay VN123 từ Hà Nội hạ cánh lúc 16h30 ngày 25/03. Nhờ khách sạn sắp xếp xe đón 1 người, 2 vali lớn.',
       'PENDING', NULL, NULL, '2026-03-21 20:00:00', '2026-03-21 20:00:00'
WHERE NOT EXISTS (SELECT 1 FROM special_requests WHERE guest_name = 'Khách Đặt Online' AND request_type = 'Đón tại sân bay');

-- ================================================================
--  VOUCHERS – Voucher đổi điểm thưởng
-- ================================================================

INSERT IGNORE INTO vouchers (name, description, points_required, value, code, category, active, max_redemptions, redeemed_count, created_at, updated_at)
VALUES ('Voucher Giảm 50.000đ', 'Giảm 50.000đ cho đơn hàng từ 500.000đ', 500, 50000, 'VOUCHER50K', 'Giảm giá', true, 100, 0, NOW(), NOW());

INSERT IGNORE INTO vouchers (name, description, points_required, value, code, category, active, max_redemptions, redeemed_count, created_at, updated_at)
VALUES ('Voucher Giảm 100.000đ', 'Giảm 100.000đ cho đơn hàng từ 1.000.000đ', 1000, 100000, 'VOUCHER100K', 'Giảm giá', true, 50, 0, NOW(), NOW());

INSERT IGNORE INTO vouchers (name, description, points_required, value, code, category, active, max_redemptions, redeemed_count, created_at, updated_at)
VALUES ('Voucher Giảm 200.000đ', 'Giảm 200.000đ cho đơn hàng từ 2.000.000đ', 2000, 200000, 'VOUCHER200K', 'Giảm giá', true, 30, 0, NOW(), NOW());

INSERT IGNORE INTO vouchers (name, description, points_required, value, code, category, active, max_redemptions, redeemed_count, created_at, updated_at)
VALUES ('Voucher Giảm 500.000đ', 'Giảm 500.000đ cho đơn hàng từ 5.000.000đ', 5000, 500000, 'VOUCHER500K', 'Giảm giá', true, 20, 0, NOW(), NOW());

INSERT IGNORE INTO vouchers (name, description, points_required, value, code, category, active, max_redemptions, redeemed_count, created_at, updated_at)
VALUES ('Voucher VIP 1.000.000đ', 'Voucher VIP giảm 1.000.000đ cho đơn hàng từ 10.000.000đ', 10000, 1000000, 'VOUCHERVIP1M', 'Giảm giá', true, 10, 0, NOW(), NOW());

INSERT IGNORE INTO vouchers (name, description, points_required, value, code, category, active, max_redemptions, redeemed_count, created_at, updated_at)
VALUES ('Voucher Spa 300.000đ', 'Giảm 300.000đ cho dịch vụ Spa', 3000, 300000, 'VOUCHERSPA300K', 'Dịch vụ', true, 15, 0, NOW(), NOW());

INSERT IGNORE INTO vouchers (name, description, points_required, value, code, category, active, max_redemptions, redeemed_count, created_at, updated_at)
VALUES ('Voucher Ẩm Thực 150.000đ', 'Giảm 150.000đ cho dịch vụ ẩm thực tại khách sạn', 1500, 150000, 'VOUCHERFOOD150K', 'Ẩm thực', true, 25, 0, NOW(), NOW());

INSERT IGNORE INTO vouchers (name, description, points_required, value, code, category, active, max_redemptions, redeemed_count, created_at, updated_at)
VALUES ('Voucher Đặc Biệt Tết', 'Voucher giảm 800.000đ dịp Tết Nguyên Đán', 8000, 800000, 'VOUCHERTET800K', 'Sự kiện', false, 5, 0, NOW(), NOW());


-- ================================================================
--  GROUP BOOKINGS – Đặt phòng khách đoàn mẫu
-- ================================================================

INSERT IGNORE INTO group_bookings (group_name, contact_person, contact_phone, contact_email, total_rooms, check_in, check_out, status, note, created_at, updated_at)
VALUES ('Công ty Du lịch XYZ', 'Nguyễn Văn A', '0901234567', 'nguyenvana@xyz.com', 15, '2026-07-10', '2026-07-12', 'PENDING', 'Đoàn khách công ty, cần phòng họp buổi sáng', NOW(), NOW());

INSERT IGNORE INTO group_bookings (group_name, contact_person, contact_phone, contact_email, total_rooms, check_in, check_out, status, note, created_at, updated_at)
VALUES ('Doanh nghiệp ABC', 'Trần Thị B', '0912345678', 'tranthib@abc.com', 20, '2026-07-15', '2026-07-17', 'CONFIRMED', 'Hội nghị khách hàng, cần dịch vụ ăn uống cao cấp', NOW(), NOW());

-- Phòng cho đoàn Công ty Du lịch XYZ
INSERT IGNORE INTO group_booking_rooms (group_booking_id, guest_name, room_type, room_number, status, price, note, created_at, updated_at)
VALUES (
    (SELECT id FROM group_bookings WHERE group_name = 'Công ty Du lịch XYZ' LIMIT 1),
    'Lê Văn Tâm', 'Deluxe', '101', 'BOOKED', 1200000, NULL, NOW(), NOW()
);

INSERT IGNORE INTO group_booking_rooms (group_booking_id, guest_name, room_type, room_number, status, price, note, created_at, updated_at)
VALUES (
    (SELECT id FROM group_bookings WHERE group_name = 'Công ty Du lịch XYZ' LIMIT 1),
    'Phạm Thị Thảo', 'Superior', '202', 'BOOKED', 1500000, NULL, NOW(), NOW()
);

INSERT IGNORE INTO group_booking_rooms (group_booking_id, guest_name, room_type, room_number, status, price, note, created_at, updated_at)
VALUES (
    (SELECT id FROM group_bookings WHERE group_name = 'Công ty Du lịch XYZ' LIMIT 1),
    'Hoàng Minh Khôi', 'Suite', '301', 'BOOKED', 3500000, 'Trưởng đoàn', NOW(), NOW()
);

-- Phòng cho đoàn Doanh nghiệp ABC
INSERT IGNORE INTO group_booking_rooms (group_booking_id, guest_name, room_type, room_number, status, price, note, created_at, updated_at)
VALUES (
    (SELECT id FROM group_bookings WHERE group_name = 'Doanh nghiệp ABC' LIMIT 1),
    'Đặng Quốc Bảo', 'Deluxe', '102', 'CHECKED_IN', 1200000, NULL, NOW(), NOW()
);

INSERT IGNORE INTO group_booking_rooms (group_booking_id, guest_name, room_type, room_number, status, price, note, created_at, updated_at)
VALUES (
    (SELECT id FROM group_bookings WHERE group_name = 'Doanh nghiệp ABC' LIMIT 1),
    'Trần Ngọc Mai', 'Superior', '203', 'CHECKED_IN', 1500000, NULL, NOW(), NOW()
);
