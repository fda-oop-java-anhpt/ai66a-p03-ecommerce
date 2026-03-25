-- 1. Làm sạch dữ liệu và reset bộ đếm ID tự động (Tránh lỗi Duplicate ID)
TRUNCATE TABLE audit_logs, order_details, orders, customers, users, coupons, items, system_settings RESTART IDENTITY CASCADE;

-- 2. Cấu hình hệ thống (system_settings)
INSERT INTO system_settings (setting_key, setting_value, description, created_date) VALUES 
('TAX_RATE', '8.00', 'Mức thuế VAT (%)', '2026-01-01 00:00:00'),
('STORE_NAME', 'TechWorld 2026', 'Thương hiệu hiển thị trên hóa đơn', '2026-01-01 00:00:00'),
('LOW_STOCK_LIMIT', '5', 'Ngưỡng báo động nhập hàng', '2026-01-01 00:00:00');

-- 3. Người dùng (users)
INSERT INTO users (user_name, user_password, user_role, created_date, last_login) VALUES 
('admin_1', 'admin@123', 'ADMIN', '2025-10-15 08:00:00', '2026-01-31 09:00:00'),
('nv_hoang', 'staff@123', 'STAFF', '2025-11-20 09:15:00', '2026-01-31 08:30:00'),
('nv_thao', 'staff@123', 'STAFF', '2026-01-05 10:00:00', NULL),
('nv_linh', 'staff@123', 'STAFF', '2026-01-10 13:45:00', '2026-01-30 14:00:00');

-- 4. Khách hàng (customers)
INSERT INTO customers (customer_name, phone, email, address, created_date) VALUES 
('Nguyễn Văn An', '0912345678', 'an.nguyen@gmail.com', 'Cầu Giấy, Hà Nội', '2025-09-01 10:30:00'),
('Trần Thị Bình', '0988777666', 'binh.tt@yahoo.com', 'Quận 1, TP.HCM', '2025-12-15 15:20:00'),
('Lê Quang Cường', '0905111222', 'cuong.lq@outlook.com', 'Hải Châu, Đà Nẵng', '2026-01-20 09:00:00'),
('Khách Vãng Lai', '0000000000', 'visitor@store.com', 'Tại quầy', '2025-01-01 00:00:00');

-- 5. Sản phẩm điện tử (items)
INSERT INTO items (item_sku, item_name, category, unit_price, stock_quantity) VALUES 
('IP15PM-256', 'iPhone 15 Pro Max 256GB', 'Điện thoại', 29500000.00, 15),
('MAC-M3-16', 'MacBook Air M3 16GB/512GB', 'Laptop', 32990000.00, 8),
('SONY-XM5', 'Sony WH-1000XM5 Wireless', 'Tai nghe', 6490000.00, 12),
('LG-MX-3S', 'Logitech MX Master 3S', 'Phụ kiện', 2350000.00, 4), -- Cảnh báo kho thấp
('SS-4K-27', 'Samsung 27 inch 4K UHD', 'Màn hình', 8900000.00, 2);  -- Cảnh báo kho thấp

-- 6. Mã giảm giá (coupons)
INSERT INTO coupons (coupon_code, discount_value, discount_type, min_order_value, created_date, expiry_date, is_active) VALUES 
('GREET2026', 10.00, 'Percent', 5000000.00, '2026-01-01 00:00:00', '2026-12-31', TRUE),
('GIAM500K', 500000.00, 'Fixed', 15000000.00, '2026-01-15 08:00:00', '2026-06-30', TRUE),
('STUDENT', 5.00, 'Percent', 0.00, '2026-01-01 00:00:00', '2026-12-31', TRUE);

-- 7. Đơn hàng (orders)
-- Snapshot tài chính: discount_amount lưu số tiền giảm, discount_info lưu cách thức giảm
INSERT INTO orders (customer_id, coupon_code, tax_rate, discount_amount, discount_info, status, subtotal, final_total, order_date) VALUES 
-- Đơn hàng dùng mã giảm 10%: (29.5M - 2.95M) * 1.08 = 28,674,000
(1, 'GREET2026', 8.00, 2950000.00, '10%', 'PAID', 29500000.00, 28674000.00,'2026-01-25 14:30:00'),
-- Đơn hàng không giảm giá: 32.99M * 1.08 = 35,629,200
(2, NULL, 8.00, 0.00, 'Không áp dụng', 'PENDING', 32990000.00, 35629200.00,'2026-01-28 10:15:00'),
-- Đơn hàng giảm tiền cố định 500k: (8.79M - 500k) * 1.08 = 8,953,200
(3, 'GIAM500K', 8.00, 500000.00, '500,000 VNĐ', 'PAID',  8790000.00, 8953200.00,'2026-01-30 16:45:00'),
-- Đơn hàng bị hủy
(1, NULL, 8.00, 0.00, 'Không áp dụng', 'CANCELLED', 2350000.00, 2538000.00,'2026-01-31 11:20:00');

-- 8. Chi tiết đơn hàng (order_details)
INSERT INTO order_details (order_id, item_sku, quantity, price_at_time) VALUES 
(1, 'IP15PM-256', 1, 29500000.00),
(2, 'MAC-M3-16', 1, 32990000.00),
(3, 'SONY-XM5', 1, 6440000.00), 
(3, 'LG-MX-3S', 1, 2350000.00),
(4, 'LG-MX-3S', 1, 2350000.00);

-- 9. Nhật ký hệ thống (audit_logs)
INSERT INTO audit_logs (user_id, actions, target_type, target_id, created_date ) VALUES 
(1, 'LOGIN', 'USER', '1', '2026-01-31 09:00:01'),
(2, 'CREATE_ORDER', 'ORDER', '1', '2026-01-25 14:30:05'),
(1, 'UPDATE_STOCK', 'ITEM', 'IP15PM-256', '2026-01-26 10:00:00'),
(2, 'CANCEL_ORDER', 'ORDER', '4', '2026-01-31 11:20:30');