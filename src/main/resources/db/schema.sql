-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS yanolja;
USE yanolja;

-- 사용자 테이블
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'USER',
    profile_image_url VARCHAR(255),
    social_provider VARCHAR(20),
    social_id VARCHAR(255),
    withdrawal_yn CHAR(1) DEFAULT 'N',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- 숙소 테이블
CREATE TABLE accommodations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version BIGINT DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    price_per_night DECIMAL(10, 2) NOT NULL,
    rating DECIMAL(3, 2),
    review_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    deleted_yn TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- 숙소 이미지 테이블
CREATE TABLE accommodation_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    accommodation_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_main BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(id) ON DELETE CASCADE
);

-- 객실 테이블
CREATE TABLE rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    accommodation_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    capacity INT NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    deleted_yn CHAR(1) DEFAULT 'N',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(id) ON DELETE CASCADE
);

-- 객실 이미지 테이블
CREATE TABLE room_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    is_main BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

-- 예약 테이블
CREATE TABLE reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id)
);

-- 리뷰 테이블
CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    accommodation_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

-- 리뷰 이미지 테이블
CREATE TABLE review_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    review_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
);

-- 찜 목록 테이블
CREATE TABLE wishlists (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    accommodation_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(id),
    UNIQUE KEY unique_wishlist (user_id, accommodation_id)
);

-- =====================================================
-- 결제 시스템 테이블들
-- =====================================================

-- 주문 테이블
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version BIGINT DEFAULT 0,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    reservation_id BIGINT NOT NULL,
    original_amount DECIMAL(12, 2) NOT NULL,
    discount_amount DECIMAL(12, 2) DEFAULT 0,
    points_used INT DEFAULT 0,
    final_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expired_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    INDEX idx_order_number (order_number),
    INDEX idx_user_id (user_id),
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_status (status),
    INDEX idx_expired_at (expired_at)
);

-- 결제 테이블
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version BIGINT DEFAULT 0,
    payment_key VARCHAR(100) NOT NULL UNIQUE,
    order_id BIGINT NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    pg_provider VARCHAR(50),
    pg_transaction_id VARCHAR(200),
    approval_number VARCHAR(100),
    card_number VARCHAR(20),
    card_type VARCHAR(50),
    installment_months INT,
    paid_at TIMESTAMP NULL,
    failure_reason VARCHAR(500),
    receipt_url VARCHAR(500),
    refunded_amount DECIMAL(10,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_payment_key (payment_key),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status),
    INDEX idx_pg_transaction_id (pg_transaction_id)
);

-- 쿠폰 테이블
CREATE TABLE coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version BIGINT DEFAULT 0,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(12, 2) NOT NULL,
    max_discount_amount DECIMAL(12, 2),
    min_order_amount DECIMAL(12, 2),
    issue_count INT NOT NULL,
    used_count INT NOT NULL DEFAULT 0,
    issue_start_at TIMESTAMP NOT NULL,
    issue_end_at TIMESTAMP NOT NULL,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    issue_type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    INDEX idx_code (code),
    INDEX idx_status (status),
    INDEX idx_issue_type (issue_type),
    INDEX idx_valid_period (valid_from, valid_until),
    INDEX idx_issue_period (issue_start_at, issue_end_at)
);

-- 사용자 쿠폰 테이블
CREATE TABLE user_coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    version BIGINT DEFAULT 0,
    user_id BIGINT NOT NULL,
    coupon_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    used_at TIMESTAMP NULL,
    order_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    UNIQUE KEY unique_user_coupon (user_id, coupon_id),
    INDEX idx_user_id (user_id),
    INDEX idx_coupon_id (coupon_id),
    INDEX idx_status (status)
);

-- 주문 쿠폰 테이블
CREATE TABLE order_coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    user_coupon_id BIGINT NOT NULL,
    discount_amount DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_coupon_id) REFERENCES user_coupons(id),
    INDEX idx_order_id (order_id),
    INDEX idx_user_coupon_id (user_coupon_id)
);

-- 포인트 테이블
CREATE TABLE points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount INT NOT NULL,
    balance INT NOT NULL,
    description VARCHAR(500),
    order_id BIGINT NULL,
    expired_at TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_user_id (user_id),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_status (status),
    INDEX idx_expired_at (expired_at),
    INDEX idx_order_id (order_id)
);

-- =====================================================
-- 기존 테이블들 (편의시설, 객실 옵션 등)
-- =====================================================

-- 숙소 편의시설 테이블
CREATE TABLE amenities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    icon_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- 숙소-편의시설 매핑 테이블
CREATE TABLE accommodation_amenities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    accommodation_id BIGINT NOT NULL,
    amenity_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (accommodation_id) REFERENCES accommodations(id) ON DELETE CASCADE,
    FOREIGN KEY (amenity_id) REFERENCES amenities(id) ON DELETE CASCADE
);

-- 객실 옵션 테이블
CREATE TABLE room_options (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- 객실-옵션 매핑 테이블
CREATE TABLE room_option_mappings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES room_options(id) ON DELETE CASCADE
);

-- 예약 옵션 테이블
CREATE TABLE reservation_options (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (option_id) REFERENCES room_options(id)
); 