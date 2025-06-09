# 야놀자 예약관리 시스템 기능 명세서

## 📋 목차

1. [시스템 개요](#시스템-개요)
2. [핵심 도메인](#핵심-도메인)
3. [API 명세](#api-명세)
4. [데이터베이스 설계](#데이터베이스-설계)
5. [비즈니스 규칙](#비즈니스-규칙)
6. [성능 요구사항](#성능-요구사항)

---

## 🎯 시스템 개요

### 프로젝트 개요

- **시스템명**: 야놀자 예약관리 시스템
- **목적**: 숙박시설 예약, 관리, 운영을 위한 통합 플랫폼
- **대상 사용자**: 일반 고객, 숙박업체 관리자, 시스템 관리자
- **기술 스택**: Spring Boot, JPA/Hibernate, MySQL, Redis, AWS

### 시스템 아키텍처

```
Frontend (React/Vue) ↔ API Gateway ↔ Backend Services ↔ Database
                                   ↓
                              External APIs (결제, 알림톡, SMS)
```

---

## 🏗️ 핵심 도메인

### 1. 사용자 관리 (User Management)

#### 1.1 회원가입

- **기능**: 이메일 기반 회원가입, 소셜 로그인 연동
- **검증 규칙**:
  - 이메일: RFC 5322 표준 준수, 중복 불가
  - 비밀번호: 최소 8자, 영문+숫자+특수문자 조합
  - 전화번호: 국내 휴대폰 번호 형식 (010-XXXX-XXXX)
- **프로세스**:
  1. 이메일 중복 체크 → 2. 이메일 인증 → 3. 프로필 정보 입력 → 4. 가입 완료

#### 1.2 인증/인가

- **JWT 토큰 기반 인증**: Access Token (30분) + Refresh Token (2주)
- **권한 관리**: ROLE_USER, ROLE_PARTNER, ROLE_ADMIN
- **소셜 로그인**: 카카오, 네이버, 구글 OAuth 2.0

#### 1.3 프로필 관리

- **개인정보**: 이름, 전화번호, 생년월일, 성별
- **선호 설정**: 알림 설정, 마케팅 수신 동의
- **회원 등급**: 브론즈/실버/골드/다이아몬드 (예약 금액 기준)

### 2. 숙소 관리 (Accommodation Management)

#### 2.1 숙소 정보

- **기본 정보**: 숙소명, 주소, 전화번호, 체크인/아웃 시간
- **분류**: 호텔, 펜션, 리조트, 모텔, 게스트하우스
- **편의시설**: WiFi, 주차장, 수영장, 피트니스, 조식, 반려동물 허용 등
- **위치 정보**: 위도/경도, 지하철역 거리, 주요 관광지 접근성

#### 2.2 객실 관리

- **객실 정보**: 객실명, 최대인원, 침대 타입, 면적
- **객실 편의시설**: 에어컨, TV, 냉장고, 드라이어, 욕조/샤워 등
- **이미지 관리**: 대표 이미지, 상세 이미지 (최대 20장)
- **가격 정책**: 기본 요금, 성수기/비수기 요금, 요일별 차등 요금

#### 2.3 재고 관리

- **실시간 재고**: 날짜별 예약 가능 객실 수
- **예약 제한**: 최소/최대 숙박일수, 사전 예약 마감시간
- **특가 상품**: 타임딜, 조기예약 할인, 연박 할인

### 3. 예약 시스템 (Reservation System)

#### 3.1 예약 프로세스

```
숙소 검색 → 객실 선택 → 예약 정보 입력 → 결제 → 예약 확정 → 숙박 → 체크아웃 → 리뷰 작성
```

#### 3.2 예약 상태 관리

- **PENDING**: 결제 대기 (10분 자동 취소)
- **CONFIRMED**: 예약 확정
- **CANCELLED**: 고객 취소
- **REJECTED**: 숙소 거절
- **COMPLETED**: 숙박 완료
- **NO_SHOW**: 노쇼 처리

#### 3.3 예약 정책

- **취소/환불 정책**:
  - 3일 전: 100% 환불
  - 1-2일 전: 50% 환불
  - 당일: 환불 불가
- **변경 정책**: 체크인 1일 전까지 1회 무료 변경
- **노쇼 정책**: 체크인 시간 2시간 경과 시 자동 노쇼 처리

### 4. 결제 시스템 (Payment System)

#### 4.1 결제 수단

- **일반 결제**: 신용카드, 체크카드, 계좌이체, 무통장입금
- **간편 결제**: 카카오페이, 네이버페이, 페이코, 토스
- **포인트**: 야놀자 적립금, 각종 제휴 포인트

#### 4.2 쿠폰/할인

- **쿠폰 유형**: 정액 할인, 정률 할인, 배송비 무료
- **적용 조건**: 최소 주문 금액, 특정 숙소/지역 제한, 유효기간
- **발급 방식**: 회원가입, 이벤트 참여, 리뷰 작성, 생일 쿠폰

#### 4.3 적립금/포인트

- **적립 정책**: 결제 금액의 1-5% (회원 등급별 차등)
- **사용 규칙**: 최소 사용 금액 1,000원, 전체 금액의 50%까지 사용 가능
- **유효기간**: 적립일로부터 2년

### 5. 리뷰 시스템 (Review System)

#### 5.1 리뷰 작성

- **작성 권한**: 실제 숙박 완료 고객만
- **작성 기간**: 체크아웃 후 30일 이내
- **리뷰 요소**:
  - 종합 만족도 (5점 척도)
  - 세부 평가: 청결도, 서비스, 편의시설, 가성비, 위치
  - 텍스트 리뷰 (최대 1,000자)
  - 사진 첨부 (최대 5장)
  - 추천 태그 (깨끗해요, 친절해요, 교통이 편해요 등)

#### 5.2 리뷰 관리

- **신고 기능**: 부적절한 리뷰, 허위 리뷰 신고
- **리뷰 답글**: 숙소 운영자 답글 기능
- **리뷰 혜택**: 리뷰 작성 시 적립금 지급

### 6. 검색 및 필터링 (Search & Filtering)

#### 6.1 검색 기능

- **지역 검색**: 시/도, 구/군, 동/읍/면 단위 검색
- **키워드 검색**: 숙소명, 지역명, 랜드마크 검색
- **지도 검색**: 지도 기반 위치 검색

#### 6.2 필터링 옵션

- **기본 조건**: 체크인/아웃 날짜, 숙박 인원
- **가격 범위**: 슬라이더 기반 가격 필터
- **숙소 유형**: 호텔, 펜션, 리조트 등
- **편의시설**: 주차장, WiFi, 수영장, 조식 등
- **평점**: 4.0점 이상, 4.5점 이상 등

#### 6.3 정렬 옵션

- **추천순**: 예약 빈도, 리뷰 점수, 가격 등 종합 점수
- **낮은 가격순/높은 가격순**
- **평점 높은순**
- **리뷰 많은순**
- **거리순**: 지정된 위치 기준

---

## 📱 API 명세

### 1. 인증/사용자 관리 API

#### 1.1 회원가입

```http
POST /api/v1/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "name": "김야놀",
  "phoneNumber": "010-1234-5678",
  "birthDate": "1990-01-01",
  "gender": "M",
  "marketingConsent": true
}

Response: 201 Created
{
  "status": "success",
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": 12345,
    "email": "user@example.com",
    "verificationRequired": true
  }
}
```

#### 1.2 로그인

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}

Response: 200 OK
{
  "status": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 12345,
      "email": "user@example.com",
      "name": "김야놀",
      "role": "USER",
      "grade": "BRONZE"
    }
  }
}
```

### 2. 숙소 검색/조회 API

#### 2.1 숙소 검색

```http
GET /api/v1/accommodations/search?region=서울&checkIn=2024-03-01&checkOut=2024-03-02&guests=2&minPrice=50000&maxPrice=200000&amenities=parking,wifi&sort=price_asc&page=0&size=20

Response: 200 OK
{
  "status": "success",
  "data": {
    "content": [
      {
        "id": 1001,
        "name": "서울 시티 호텔",
        "address": "서울시 강남구 테헤란로 123",
        "rating": 4.5,
        "reviewCount": 1250,
        "minPrice": 89000,
        "images": ["https://cdn.yanolja.com/image1.jpg"],
        "amenities": ["parking", "wifi", "breakfast"],
        "distance": 1.2,
        "isWishListed": false
      }
    ],
    "totalElements": 156,
    "totalPages": 8,
    "size": 20,
    "number": 0
  }
}
```

#### 2.2 숙소 상세 조회

```http
GET /api/v1/accommodations/1001?checkIn=2024-03-01&checkOut=2024-03-02&guests=2

Response: 200 OK
{
  "status": "success",
  "data": {
    "id": 1001,
    "name": "서울 시티 호텔",
    "description": "강남 중심가에 위치한 비즈니스 호텔",
    "address": "서울시 강남구 테헤란로 123",
    "phone": "02-1234-5678",
    "checkInTime": "15:00",
    "checkOutTime": "11:00",
    "rating": 4.5,
    "reviewCount": 1250,
    "amenities": [...],
    "location": {
      "latitude": 37.5665,
      "longitude": 126.9780
    },
    "images": [...],
    "rooms": [
      {
        "id": 10001,
        "name": "스탠다드 더블",
        "maxOccupancy": 2,
        "bedType": "더블베드 1개",
        "size": 25,
        "price": 89000,
        "originalPrice": 120000,
        "discountRate": 26,
        "amenities": ["TV", "에어컨", "냉장고"],
        "images": [...],
        "available": true,
        "availableRooms": 3
      }
    ],
    "nearbyPlaces": [...],
    "policies": {
      "cancellation": "체크인 3일 전까지 무료 취소",
      "children": "12세 이하 어린이 무료",
      "pets": false
    }
  }
}
```

### 3. 예약 관리 API

#### 3.1 예약 생성

```http
POST /api/v1/reservations
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "accommodationId": 1001,
  "roomId": 10001,
  "checkInDate": "2024-03-01",
  "checkOutDate": "2024-03-02",
  "guests": {
    "adults": 2,
    "children": 0
  },
  "guestInfo": {
    "name": "김야놀",
    "phoneNumber": "010-1234-5678",
    "email": "user@example.com"
  },
  "specialRequests": "금연실 요청",
  "couponId": 5001,
  "usePoints": 10000
}

Response: 201 Created
{
  "status": "success",
  "data": {
    "reservationId": "R20240301001",
    "status": "PENDING",
    "paymentAmount": 79000,
    "paymentDeadline": "2024-02-29T23:59:59"
  }
}
```

#### 3.2 예약 목록 조회

```http
GET /api/v1/reservations?status=CONFIRMED&page=0&size=10
Authorization: Bearer {accessToken}

Response: 200 OK
{
  "status": "success",
  "data": {
    "content": [
      {
        "reservationId": "R20240301001",
        "accommodationName": "서울 시티 호텔",
        "roomName": "스탠다드 더블",
        "checkInDate": "2024-03-01",
        "checkOutDate": "2024-03-02",
        "status": "CONFIRMED",
        "totalAmount": 79000,
        "canCancel": true,
        "canModify": true,
        "accommodationImage": "https://cdn.yanolja.com/image1.jpg"
      }
    ],
    "totalElements": 15,
    "totalPages": 2
  }
}
```

### 4. 결제 API

#### 4.1 결제 처리

```http
POST /api/v1/payments
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "reservationId": "R20240301001",
  "paymentMethod": "CARD",
  "cardInfo": {
    "cardNumber": "1234-5678-9012-3456",
    "expiryMonth": "12",
    "expiryYear": "25",
    "cvc": "123",
    "cardHolderName": "김야놀"
  },
  "installment": 0
}

Response: 200 OK
{
  "status": "success",
  "data": {
    "paymentId": "P20240301001",
    "reservationId": "R20240301001",
    "status": "SUCCESS",
    "paidAmount": 79000,
    "paidAt": "2024-02-29T14:30:00",
    "receiptUrl": "https://receipt.yanolja.com/P20240301001"
  }
}
```

### 5. 리뷰 API

#### 5.1 리뷰 작성

```http
POST /api/v1/reviews
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data

{
  "reservationId": "R20240301001",
  "overallRating": 4.5,
  "ratings": {
    "cleanliness": 5.0,
    "service": 4.0,
    "facilities": 4.5,
    "valueForMoney": 4.0,
    "location": 5.0
  },
  "content": "깨끗하고 위치가 좋았습니다.",
  "tags": ["깨끗해요", "위치가 좋아요"],
  "images": [file1, file2]
}

Response: 201 Created
{
  "status": "success",
  "data": {
    "reviewId": 12345,
    "pointsEarned": 500
  }
}
```

---

## 🗄️ 데이터베이스 설계

### 주요 테이블 구조

#### 1. users (사용자)

```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(100) NOT NULL,
  phone_number VARCHAR(20),
  birth_date DATE,
  gender ENUM('M', 'F'),
  role ENUM('USER', 'PARTNER', 'ADMIN') DEFAULT 'USER',
  grade ENUM('BRONZE', 'SILVER', 'GOLD', 'DIAMOND') DEFAULT 'BRONZE',
  points INT DEFAULT 0,
  marketing_consent BOOLEAN DEFAULT FALSE,
  email_verified BOOLEAN DEFAULT FALSE,
  status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_email (email),
  INDEX idx_phone_number (phone_number),
  INDEX idx_status (status)
);
```

#### 2. accommodations (숙소)

```sql
CREATE TABLE accommodations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  partner_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  type ENUM('HOTEL', 'PENSION', 'RESORT', 'MOTEL', 'GUESTHOUSE'),
  address VARCHAR(500) NOT NULL,
  latitude DECIMAL(10, 8),
  longitude DECIMAL(11, 8),
  phone_number VARCHAR(20),
  check_in_time TIME DEFAULT '15:00:00',
  check_out_time TIME DEFAULT '11:00:00',
  status ENUM('ACTIVE', 'INACTIVE', 'PENDING') DEFAULT 'PENDING',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (partner_id) REFERENCES users(id),
  INDEX idx_type (type),
  INDEX idx_status (status),
  INDEX idx_location (latitude, longitude)
);
```

#### 3. rooms (객실)

```sql
CREATE TABLE rooms (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  accommodation_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  max_occupancy INT NOT NULL,
  bed_type VARCHAR(100),
  size INT, -- 평방미터
  base_price DECIMAL(10, 2) NOT NULL,
  weekend_price DECIMAL(10, 2),
  peak_season_price DECIMAL(10, 2),
  status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (accommodation_id) REFERENCES accommodations(id),
  INDEX idx_accommodation_id (accommodation_id),
  INDEX idx_status (status)
);
```

#### 4. reservations (예약)

```sql
CREATE TABLE reservations (
  id VARCHAR(20) PRIMARY KEY, -- R20240301001 형태
  user_id BIGINT NOT NULL,
  accommodation_id BIGINT NOT NULL,
  room_id BIGINT NOT NULL,
  check_in_date DATE NOT NULL,
  check_out_date DATE NOT NULL,
  adults INT NOT NULL DEFAULT 1,
  children INT DEFAULT 0,
  guest_name VARCHAR(100) NOT NULL,
  guest_phone VARCHAR(20) NOT NULL,
  guest_email VARCHAR(255) NOT NULL,
  special_requests TEXT,
  original_amount DECIMAL(10, 2) NOT NULL,
  discount_amount DECIMAL(10, 2) DEFAULT 0,
  points_used INT DEFAULT 0,
  final_amount DECIMAL(10, 2) NOT NULL,
  status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'REJECTED', 'COMPLETED', 'NO_SHOW') DEFAULT 'PENDING',
  cancelled_at TIMESTAMP NULL,
  cancellation_reason TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (accommodation_id) REFERENCES accommodations(id),
  FOREIGN KEY (room_id) REFERENCES rooms(id),
  INDEX idx_user_id (user_id),
  INDEX idx_check_in_date (check_in_date),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
);
```

#### 5. payments (결제)

```sql
CREATE TABLE payments (
  id VARCHAR(20) PRIMARY KEY, -- P20240301001 형태
  reservation_id VARCHAR(20) NOT NULL,
  payment_method ENUM('CARD', 'BANK_TRANSFER', 'KAKAO_PAY', 'NAVER_PAY', 'TOSS') NOT NULL,
  amount DECIMAL(10, 2) NOT NULL,
  status ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED', 'REFUNDED') DEFAULT 'PENDING',
  pg_transaction_id VARCHAR(255),
  paid_at TIMESTAMP NULL,
  refunded_at TIMESTAMP NULL,
  refund_amount DECIMAL(10, 2) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (reservation_id) REFERENCES reservations(id),
  INDEX idx_reservation_id (reservation_id),
  INDEX idx_status (status),
  INDEX idx_paid_at (paid_at)
);
```

#### 6. reviews (리뷰)

```sql
CREATE TABLE reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reservation_id VARCHAR(20) NOT NULL,
  user_id BIGINT NOT NULL,
  accommodation_id BIGINT NOT NULL,
  overall_rating DECIMAL(2, 1) NOT NULL,
  cleanliness_rating DECIMAL(2, 1),
  service_rating DECIMAL(2, 1),
  facilities_rating DECIMAL(2, 1),
  value_rating DECIMAL(2, 1),
  location_rating DECIMAL(2, 1),
  content TEXT,
  status ENUM('ACTIVE', 'REPORTED', 'HIDDEN') DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (reservation_id) REFERENCES reservations(id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (accommodation_id) REFERENCES accommodations(id),
  INDEX idx_accommodation_id (accommodation_id),
  INDEX idx_overall_rating (overall_rating),
  INDEX idx_created_at (created_at)
);
```

---

## 📋 비즈니스 규칙

### 1. 예약 관련 규칙

#### 1.1 예약 가능 조건

- 체크인 날짜는 오늘 날짜 이후여야 함
- 체크인 날짜 < 체크아웃 날짜
- 최대 숙박 기간: 30일
- 최대 예약 가능 기간: 1년 후까지

#### 1.2 재고 관리

- 실시간 재고 차감 (동시 예약 방지를 위한 분산 락 적용)
- 결제 미완료 시 10분 후 자동 재고 복구
- 오버부킹 방지를 위한 안전재고 관리

#### 1.3 가격 정책

- 성수기/비수기 요금 자동 적용
- 요일별 차등 요금 (주말 할증)
- 조기예약 할인 (30일 전: 10%, 60일 전: 15%)
- 연박 할인 (3박 이상: 5%, 7박 이상: 10%)

### 2. 결제 관련 규칙

#### 2.1 결제 정책

- 예약 시점 전액 선결제
- 결제 실패 시 최대 3회 재시도
- 부분 환불 시 PG사 수수료 고객 부담

#### 2.2 환불 정책

- 자동 환불: 시스템 오류, 숙소 사정으로 인한 취소
- 수동 환불: 고객 변심, 부분 취소
- 환불 처리 기간: 카드 3-5영업일, 계좌이체 1-2영업일

### 3. 포인트/쿠폰 규칙

#### 3.1 포인트 적립

- 기본 적립률: 1%
- 회원등급별 적립률: 브론즈 1%, 실버 2%, 골드 3%, 다이아몬드 5%
- 리뷰 작성 시 500포인트 추가 적립
- 포인트 유효기간: 2년

#### 3.2 쿠폰 정책

- 쿠폰 중복 사용 불가
- 할인 한도: 결제 금액의 50%
- 부분 취소 시 쿠폰 복원 불가

### 4. 리뷰 관련 규칙

#### 4.1 리뷰 작성 조건

- 실제 숙박 완료 고객만 작성 가능
- 체크아웃 후 30일 이내 작성
- 1회 숙박당 1개 리뷰만 작성 가능

#### 4.2 리뷰 관리

- 욕설, 개인정보 포함 시 자동 필터링
- 허위 리뷰 신고 시 관리자 검토
- 리뷰 삭제 시 적립된 포인트 차감

---

## ⚡ 성능 요구사항

### 1. 응답 시간

- **검색 API**: 평균 200ms 이하
- **예약 API**: 평균 500ms 이하
- **결제 API**: 평균 1초 이하
- **일반 조회 API**: 평균 100ms 이하

### 2. 처리량

- **동시 사용자**: 10,000명
- **예약 TPS**: 100건/초
- **검색 TPS**: 1,000건/초

### 3. 가용성

- **시스템 가동률**: 99.9% 이상
- **계획된 점검**: 월 1회, 새벽 2-4시
- **장애 복구 시간**: 30분 이내

### 4. 확장성

- **수평 확장**: Auto Scaling 적용
- **데이터베이스**: Read Replica 구성
- **캐싱**: Redis 클러스터 구성
- **CDN**: 이미지/정적 파일 캐싱

### 5. 보안 요구사항

- **개인정보 암호화**: AES-256
- **통신 암호화**: TLS 1.3
- **API 보안**: Rate Limiting, JWT 토큰
- **데이터 백업**: 일 1회 자동 백업, 30일 보관

---

## 🚀 개발 우선순위

### Phase 1 (MVP)

1. 사용자 관리 (회원가입, 로그인, 프로필)
2. 숙소 기본 등록/조회
3. 기본 예약 기능
4. 기본 결제 연동

### Phase 2

1. 고급 검색/필터링
2. 리뷰 시스템
3. 포인트/쿠폰 시스템
4. 관리자 기능

### Phase 3

1. 모바일 최적화
2. 알림 시스템
3. 성능 최적화
4. 모니터링 구축

---
