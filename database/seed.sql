-- 1. Làm sạch dữ liệu và reset bộ đếm ID tự động (Tránh lỗi Duplicate ID)
TRUNCATE TABLE audit_logs, order_details, orders, customers, users, coupons, items, system_settings RESTART IDENTITY CASCADE;

-- 2. Cấu hình hệ thống (system_settings)
INSERT INTO system_settings (setting_key, setting_value, description, created_date) VALUES 
('TAX_RATE', '8.00', 'Mức thuế VAT (%)', '2026-01-01 00:00:00'),
('STORE_NAME', 'TechWorld 2026', 'Thương hiệu hiển thị trên hóa đơn', '2026-01-01 00:00:00'),
('LOW_STOCK_LIMIT', '5', 'Ngưỡng báo động nhập hàng', '2026-01-01 00:00:00');

-- 3. Người dùng (users)
INSERT INTO users (user_name, user_password, user_role, created_date, last_login) VALUES 
('admin_1', 'dnaqr7AnyCW9mrq3iyNAcOcCdS9iW3UuVeVbSOYH41g=', 'ADMIN', '2025-10-15 08:00:00', '2026-01-31 09:00:00'),
('nv_hoang', 'tUZdeGormLvUuLeY2k+Gs0xS9k3Jo4K1DA/bD3P4uvE=', 'STAFF', '2025-11-20 09:15:00', '2026-01-31 08:30:00'),
('nv_thao', 'tUZdeGormLvUuLeY2k+Gs0xS9k3Jo4K1DA/bD3P4uvE=', 'STAFF', '2026-01-05 10:00:00', NULL),
('nv_linh', 'tUZdeGormLvUuLeY2k+Gs0xS9k3Jo4K1DA/bD3P4uvE=', 'STAFF', '2026-01-10 13:45:00', '2026-01-30 14:00:00'),
('nv_tuan', 'tUZdeGormLvUuLeY2k+Gs0xS9k3Jo4K1DA/bD3P4uvE=', 'STAFF', '2026-02-01 08:00:00', NULL),
('nv_mai', 'tUZdeGormLvUuLeY2k+Gs0xS9k3Jo4K1DA/bD3P4uvE=', 'STAFF', '2026-02-05 09:00:00', '2026-02-10 10:00:00');

-- 4. Khách hàng (customers)
INSERT INTO customers (customer_name, phone, email, address, created_date) VALUES 
('Nguyễn Văn An', '0912345678', 'an.nguyen@gmail.com', 'Cầu Giấy, Hà Nội', '2025-09-01 10:30:00'),
('Trần Thị Bình', '0988777666', 'binh.tt@yahoo.com', 'Quận 1, TP.HCM', '2025-12-15 15:20:00'),
('Lê Quang Cường', '0905111222', 'cuong.lq@outlook.com', 'Hải Châu, Đà Nẵng', '2026-01-20 09:00:00'),
('Khách Vãng Lai', '0000000000', 'visitor@store.com', 'Tại quầy', '2025-01-01 00:00:00'),
('Phạm Thu Hằng', '0977666555', 'hang.pt@gmail.com', 'Quận 3, TP.HCM', '2026-01-10 14:00:00'),
('Vũ Đức Trung', '0933222111', 'trung.vd@yahoo.com', 'Ba Đình, Hà Nội', '2026-02-01 09:30:00'),
('Đặng Ngọc Lan', '0944555666', 'lan.dn@outlook.com', 'Ninh Kiều, Cần Thơ', '2026-02-05 16:45:00'),
('Bùi Xuân Trường', '0888999000', 'truong.bx@company.vn', 'Hoàn Kiếm, Hà Nội', '2026-02-15 11:20:00'),
('Hồ Minh Đạt', '0966777888', 'dat.hm@gmail.com', 'Thanh Khê, Đà Nẵng', '2026-03-01 08:15:00'),
('Ngô Thanh Trúc', '0922333444', 'truc.nt@yahoo.vn', 'Gò Vấp, TP.HCM', '2026-03-10 10:50:00');

-- 5. Sản phẩm điện tử (items)
INSERT INTO items (item_sku, item_name, category, unit_price, stock_quantity) VALUES 
('IP15PM-256', 'iPhone 15 Pro Max 256GB', 'Điện thoại', 29500000.00, 15),
('MAC-M3-16', 'MacBook Air M3 16GB/512GB', 'Laptop', 32990000.00, 8),
('SONY-XM5', 'Sony WH-1000XM5 Wireless', 'Tai nghe', 6490000.00, 12),
('LG-MX-3S', 'Logitech MX Master 3S', 'Phụ kiện', 2350000.00, 4), -- Cảnh báo kho thấp
('SS-4K-27', 'Samsung 27 inch 4K UHD', 'Màn hình', 8900000.00, 2),  -- Cảnh báo kho thấp
('SS-S24U-512', 'Samsung Galaxy S24 Ultra 512GB', 'Điện thoại', 33990000.00, 10),
('DELL-XPS-15', 'Dell XPS 15 9530 Core i7', 'Laptop', 45000000.00, 5),
('ASUS-ROG-Z', 'Asus ROG Zephyrus G14', 'Laptop', 39990000.00, 7),
('APL-W-S9', 'Apple Watch Series 9 GPS', 'Smartwatch', 9500000.00, 20),
('KEY-CHERRY', 'Bàn phím cơ Cherry MX Board 3.0', 'Phụ kiện', 2100000.00, 15),
('IP14-128', 'iPhone 14 128GB', 'Điện thoại', 18500000.00, 25),
('IPAD-PRO-11', 'iPad Pro 11 inch M2', 'Máy tính bảng', 21990000.00, 12),
('SAM-TAB-S9', 'Samsung Galaxy Tab S9 WiFi', 'Máy tính bảng', 16500000.00, 8),
('JBL-FLIP-6', 'Loa Bluetooth JBL Flip 6', 'Loa', 2900000.00, 30),
('ANKER-737', 'Sạc dự phòng Anker 737 24000mAh', 'Phụ kiện', 3500000.00, 18);

-- 6. Mã giảm giá (coupons)
INSERT INTO coupons (coupon_code, discount_value, discount_type, min_order_value, created_date, expiry_date, is_active) VALUES 
('GREET2026', 10.00, 'Percent', 5000000.00, '2026-01-01 00:00:00', '2026-12-31', TRUE),
('GIAM500K', 500000.00, 'Fixed', 15000000.00, '2026-01-15 08:00:00', '2026-06-30', TRUE),
('STUDENT', 5.00, 'Percent', 0.00, '2026-01-01 00:00:00', '2026-12-31', TRUE),
('SALE50', 50000.00, 'Fixed', 200000.00, '2026-02-01 00:00:00', '2026-03-31', TRUE),
('FREESHIP', 30000.00, 'Fixed', 500000.00, '2026-01-01 00:00:00', '2026-12-31', TRUE),
('VIP20', 20.00, 'Percent', 20000000.00, '2026-01-01 00:00:00', '2026-06-30', TRUE),
('NEWUSER', 100000.00, 'Fixed', 1000000.00, '2026-01-01 00:00:00', '2026-12-31', TRUE);

-- 7. Đơn hàng (orders)
INSERT INTO orders (customer_id, coupon_code, tax_rate, discount_amount, discount_info, status, subtotal, final_total, order_date) VALUES 
(1, 'GREET2026', 8.00, 2950000.00, '10%', 'PAID', 29500000.00, 28674000.00,'2026-01-25 14:30:00'),
(2, NULL, 8.00, 0.00, 'Không áp dụng', 'PENDING', 32990000.00, 35629200.00,'2026-01-28 10:15:00'),
(3, 'GIAM500K', 8.00, 500000.00, '500,000 VNĐ', 'PAID',  8790000.00, 8953200.00,'2026-01-30 16:45:00'),
(1, NULL, 8.00, 0.00, 'Không áp dụng', 'CANCELLED', 2350000.00, 2538000.00,'2026-01-31 11:20:00'),
(5, 'NEWUSER', 8.00, 100000.00, '100,000 VNĐ', 'PAID', 9500000.00, 10152000.00, '2026-02-05 09:30:00'),
(6, 'VIP20', 8.00, 9000000.00, '20%', 'PAID', 45000000.00, 38880000.00, '2026-02-10 14:45:00'),
(7, NULL, 8.00, 0.00, 'Không áp dụng', 'PENDING', 33990000.00, 36709200.00, '2026-02-12 11:00:00'),
(8, 'FREESHIP', 8.00, 30000.00, '30,000 VNĐ', 'PAID', 2900000.00, 3099600.00, '2026-02-20 16:20:00'),
(9, 'STUDENT', 8.00, 925000.00, '5%', 'CANCELLED', 18500000.00, 18981000.00, '2026-03-05 10:10:00'),
(10, 'GIAM500K', 8.00, 500000.00, '500,000 VNĐ', 'PAID', 21990000.00, 23209200.00, '2026-03-12 13:30:00'),
(2, NULL, 8.00, 0.00, 'Không áp dụng', 'PAID', 2100000.00, 2268000.00, '2026-03-15 09:00:00'),
(4, NULL, 8.00, 0.00, 'Không áp dụng', 'PAID', 3500000.00, 3780000.00, '2026-04-01 15:45:00'),
(6, 'SALE50', 8.00, 50000.00, '50,000 VNĐ', 'PENDING', 2900000.00, 3078000.00, '2026-04-05 10:20:00'),
(9, 'GREET2026', 8.00, 3999000.00, '10%', 'PAID', 39990000.00, 38870280.00, '2026-04-10 11:00:00');

-- 8. Chi tiết đơn hàng (order_details)
INSERT INTO order_details (order_id, item_sku, quantity, price_at_time) VALUES 
(1, 'IP15PM-256', 1, 29500000.00),
(2, 'MAC-M3-16', 1, 32990000.00),
(3, 'SONY-XM5', 1, 6440000.00), 
(3, 'LG-MX-3S', 1, 2350000.00),
(4, 'LG-MX-3S', 1, 2350000.00),
(5, 'APL-W-S9', 1, 9500000.00),
(6, 'DELL-XPS-15', 1, 45000000.00),
(7, 'SS-S24U-512', 1, 33990000.00),
(8, 'JBL-FLIP-6', 1, 2900000.00),
(9, 'IP14-128', 1, 18500000.00),
(10, 'IPAD-PRO-11', 1, 21990000.00),
(11, 'KEY-CHERRY', 1, 2100000.00),
(12, 'ANKER-737', 1, 3500000.00),
(13, 'JBL-FLIP-6', 1, 2900000.00),
(14, 'ASUS-ROG-Z', 1, 39990000.00);

-- 9. Nhật ký hệ thống (audit_logs)
INSERT INTO audit_logs (user_id, actions, target_type, target_id, created_date ) VALUES 
(1, 'LOGIN', 'USER', '1', '2026-01-31 09:00:01'),
(2, 'CREATE_ORDER', 'ORDER', '1', '2026-01-25 14:30:05'),
(1, 'UPDATE_STOCK', 'ITEM', 'IP15PM-256', '2026-01-26 10:00:00'),
(2, 'CANCEL_ORDER', 'ORDER', '4', '2026-01-31 11:20:30'),
(3, 'LOGIN', 'USER', '3', '2026-02-05 08:30:00'),
(3, 'CREATE_ORDER', 'ORDER', '5', '2026-02-05 09:35:00'),
(1, 'UPDATE_COUPON', 'COUPON', 'SALE50', '2026-02-01 09:00:00'),
(4, 'LOGIN', 'USER', '4', '2026-02-10 14:00:00'),
(4, 'CREATE_ORDER', 'ORDER', '6', '2026-02-10 14:50:00'),
(5, 'LOGIN', 'USER', '5', '2026-02-20 16:00:00'),
(5, 'CREATE_ORDER', 'ORDER', '8', '2026-02-20 16:25:00'),
(1, 'CANCEL_ORDER', 'ORDER', '9', '2026-03-05 15:00:00'),
(6, 'LOGIN', 'USER', '6', '2026-03-12 13:00:00'),
(6, 'CREATE_ORDER', 'ORDER', '10', '2026-03-12 13:35:00'),
(1, 'UPDATE_STOCK', 'ITEM', 'ASUS-ROG-Z', '2026-04-10 10:00:00');