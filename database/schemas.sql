DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS order_details CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS coupons CASCADE;
DROP TABLE IF EXISTS system_settings CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
CREATE TABLE system_settings(
	setting_key VARCHAR(50) PRIMARY KEY,
	setting_value TEXT NOT NULL,
	description TEXT,
	created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users(
	user_id SERIAL PRIMARY KEY,
	user_name VARCHAR(200) UNIQUE NOT NULL,
	user_password VARCHAR(200) NOT NULL,
	user_role VARCHAR(20) DEFAULT 'STAFF' CHECK (user_role IN ('ADMIN', 'STAFF')),
	created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	last_login TIMESTAMP
);

CREATE TABLE customers(
	customer_id SERIAL PRIMARY KEY,
	customer_name VARCHAR(100) NOT NULL,
	phone VARCHAR(15) UNIQUE NOT NULL,
	email VARCHAR(100),
	address TEXT,
	created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	is_active BOOLEAN DEFAULT TRUE

);

CREATE TABLE items(
	item_sku VARCHAR(50) PRIMARY KEY,
	item_name VARCHAR(100) NOT NULL,
	category VARCHAR(50) NOT NULL,
	unit_price NUMERIC(12,2) NOT NULL CHECK (unit_price > 0),
	stock_quantity INT CHECK (stock_quantity >=0),
	is_active BOOLEAN DEFAULT TRUE
	
);

CREATE TABLE coupons(
	coupon_code VARCHAR(50) PRIMARY KEY,
	discount_value NUMERIC(12,2) NOT NULL CHECK (discount_value >0),
	discount_type VARCHAR(20) CHECK (discount_type IN ('Percent', 'Fixed')),
	min_order_value NUMERIC(12,2) CHECK (min_order_value >=0),
	created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	expiry_date DATE NOT NULL,
	is_active BOOLEAN DEFAULT TRUE

);


CREATE TABLE orders(
	order_id SERIAL PRIMARY KEY,
	customer_id INT REFERENCES customers(customer_id) ON DELETE SET NULL,
	coupon_code VARCHAR(50) REFERENCES coupons(coupon_code),
	tax_rate NUMERIC(4,2) DEFAULT 8.00,
	discount_amount NUMERIC(15,2) DEFAULT 0 CHECK (discount_amount >=0),
	discount_info VARCHAR(50),
	status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'CANCELLED')),
	subtotal NUMERIC(15,2) NOT NULL CHECK (subtotal >0),
	final_total NUMERIC(15,2) NOT NULL CHECK (final_total >0),
	order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
	
	
);

CREATE TABLE order_details(
	order_detail_id SERIAL PRIMARY KEY,
	order_id INT REFERENCES orders(order_id) ON DELETE CASCADE,
	item_sku VARCHAR(50) REFERENCES items(item_sku),
	quantity INT NOT NULL CHECK (quantity > 0),
	price_at_time NUMERIC(12,2) NOT NULL CHECK (price_at_time > 0)
	
	
);

CREATE TABLE audit_logs(
	log_id SERIAL PRIMARY KEY,
	user_id INT REFERENCES users(user_id),
	actions VARCHAR(50) NOT NULL,
	target_type VARCHAR(50),
	target_id VARCHAR(50),
	created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);