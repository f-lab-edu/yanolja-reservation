# 🏨 야놀자 스타일 숙박 예약 서비스

> Next.js + Spring Boot 기반의 풀스택 숙박 예약 플랫폼

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![MariaDB](https://img.shields.io/badge/MariaDB-10.x-blue.svg)](https://mariadb.org/)
[![Redis](https://img.shields.io/badge/Redis-6.x-red.svg)](https://redis.io/)

## 📋 프로젝트 소개

야놀자를 벤치마킹한 숙박 예약 서비스로, 사용자가 편리하게 숙소를 검색하고 예약할 수 있는 플랫폼입니다.
관리자는 숙소와 예약을 효율적으로 관리할 수 있으며, 실시간 결제 및 쿠폰 시스템을 통해 완성도 높은 예약 서비스를 제공합니다.

### 🎯 프로젝트 목표

- 실제 운영 가능한 수준의 숙박 예약 플랫폼 구현
- 마이크로서비스 아키텍처 기반의 확장 가능한 시스템 설계
- 사용자 경험(UX)을 고려한 직관적인 인터페이스 제공
- 안전하고 신뢰할 수 있는 결제 시스템 구축

## ✨ 주요 기능

### 👤 사용자 관리

- **회원가입/로그인**: 이메일 기반 가입, 소셜 로그인 (Google, Kakao)
- **JWT 인증**: Access Token + Refresh Token 기반 보안 인증
- **프로필 관리**: 개인정보 수정, 예약 내역 관리
- **회원등급 시스템**: 예약 금액 기준 등급별 혜택 제공

### 🏨 숙소 관리

- **숙소 검색**: 지역, 날짜, 인원별 실시간 검색
- **상세 필터링**: 가격, 편의시설, 숙소 유형별 필터
- **숙소 상세정보**: 이미지 갤러리, 객실 정보, 편의시설, 위치 정보
- **실시간 재고 관리**: 날짜별 예약 가능 객실 현황

### 💳 예약 시스템

- **실시간 예약**: 객실 선택부터 결제까지 원스톱 예약
- **예약 상태 관리**: PENDING → CONFIRMED → COMPLETED 단계별 관리
- **자동 취소 처리**: 결제 미완료 시 10분 후 자동 취소
- **예약 변경/취소**: 정책에 따른 유연한 변경 및 환불 처리

### 💰 결제 시스템

- **다양한 결제 수단**: 카드결제, 계좌이체, 간편결제 (토스페이먼츠 연동)
- **쿠폰 시스템**: 정액/정률 할인, 신규회원/생일/이벤트 쿠폰
- **포인트 적립**: 결제 금액의 1-5% 등급별 차등 적립
- **자동 환불**: 결제 취소 시 예약 자동 취소 및 환불 처리

### 📝 리뷰 시스템

- **숙박 후 리뷰**: 실제 투숙객만 작성 가능한 진정성 있는 리뷰
- **사진 리뷰**: 최대 5장까지 이미지 첨부 가능

### 👨‍💼 관리자 시스템

- **숙소 관리**: 숙소 등록/수정/삭제, 객실 및 가격 관리
- **예약 관리**: 실시간 예약 현황, 승인/취소 처리
- **회원 관리**: 회원 정보 조회/수정, 등급 관리

---

## 🔧 **기술적 구현**

### **데이터베이스 최적화**
- **QueryDSL**: 동적 쿼리 및 복잡한 검색 조건 처리
- **JPA Auditing**: 생성일시/수정일시 자동 관리
- **페이징 처리**: 대용량 데이터 효율적 조회

### **동시성 제어**
- **Redis 분산 락**: UUID 기반 안전한 락 메커니즘
- **락 타임아웃/리스 타임 설정**: 데드락 방지
- **예약 중복 방지**: 동일 객실 동시 예약 차단

### **보안 구현**
- **Spring Security**: JWT 필터 체인 구성
- **BCrypt 암호화**: 비밀번호 해싱
- **CORS 설정**: 프론트엔드 도메인 허용

### **외부 API 연동**
- **토스페이먼츠**: 결제 승인, 취소
- **OAuth 2.0**: 소셜 로그인 토큰 처리
- **파일 스토리지**: 이미지 업로드/다운로드

---


## 🏗️ 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │   Backend       │
│   (Next.js)     │◄──►│   (AWS ALB)     │◄──►│ (Spring Boot)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                       ┌─────────────────┐             │
                       │     Redis       │◄────────────┤
                       │   (Cache/Session)│             │
                       └─────────────────┘             │
                                                        │
                       ┌─────────────────┐             │
                       │    MariaDB      │◄────────────┘
                       │   (Main DB)     │
                       └─────────────────┘
```

## 🚀 프로젝트 실행 방법

### 📋 사전 준비사항

1. **Java 17** 설치
2. **Node.js 18+** 설치 (프론트엔드용)
3. **MariaDB 10.x** 설치 및 실행
4. **Redis 6.x** 설치 및 실행
5. **Git** 설치

### 🗄️ 데이터베이스 설정

```bash
# MariaDB 접속
mysql -u root -p

# 데이터베이스 생성
CREATE DATABASE yanolja CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 사용자 생성 및 권한 부여
CREATE USER 'yanolja_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON yanolja.* TO 'yanolja_user'@'localhost';
FLUSH PRIVILEGES;
```

### ⚙️ 백엔드 실행

1. **저장소 클론**

```bash
git clone https://github.com/your-username/yanolja-reservation-client-server.git
cd yanolja-reservation2
```

2. **환경변수 설정**

```bash
# application.yml에서 다음 값들을 실제 값으로 변경
# - 데이터베이스 접속 정보
# - JWT 시크릿 키
# - OAuth 클라이언트 정보
# - 결제 API 키
```

3. **빌드 및 실행**

```bash
# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

4. **API 문서 확인**

```
http://localhost:8080/swagger-ui.html
```

### 🎨 프론트엔드 실행 (별도 저장소)

1. **프론트엔드 저장소 클론**

```bash
git clone https://github.com/your-username/yanolja-frontend.git
cd yanolja-frontend
```

2. **의존성 설치**

```bash
npm install
# 또는
yarn install
```

3. **환경변수 설정**

```bash
# .env.local 파일 생성
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_GOOGLE_CLIENT_ID=your_google_client_id
NEXT_PUBLIC_KAKAO_CLIENT_ID=your_kakao_client_id
```

4. **개발 서버 실행**

```bash
npm run dev
# 또는
yarn dev
```

5. **브라우저에서 확인**

```
http://localhost:3000
```

## 📡 주요 API 엔드포인트

### 인증 API

- `POST /api/v1/auth/signup` - 회원가입
- `POST /api/v1/auth/login` - 로그인
- `POST /api/v1/auth/refresh` - 토큰 갱신

### 숙소 API

- `GET /api/v1/accommodations` - 숙소 목록 조회
- `GET /api/v1/accommodations/{id}` - 숙소 상세 조회
- `GET /api/v1/accommodations/search` - 숙소 검색

### 예약 API

- `POST /api/portal/reservations` - 예약 생성
- `GET /api/portal/reservations` - 예약 목록 조회
- `PATCH /api/portal/reservations/{id}/cancel` - 예약 취소

### 결제 API

- `POST /api/v1/payments` - 결제 처리
- `POST /api/v1/payments/cancel` - 결제 취소

## 📊 프로젝트 구조

```
src/main/java/com/yanolja/
├── areas/                          # 도메인별 패키지
│   ├── accommodation/              # 숙소 관리
│   ├── auth/                       # 인증/인가
│   ├── payment/                    # 결제/쿠폰
│   ├── reservation/                # 예약 관리
│   ├── reviews/                    # 리뷰 시스템
│   ├── room/                       # 객실 관리
│   └── user/                       # 사용자 관리
├── common/                         # 공통 컴포넌트
│   ├── config/                     # 설정 클래스
│   ├── exception/                  # 예외 처리
│   ├── jwt/                        # JWT 관련
│   └── response/                   # 응답 DTO
└── YanoljaApplication.java         # 메인 클래스
```

## 🔧 주요 설정

### 데이터베이스 설정

- **Connection Pool**: HikariCP
- **Transaction**: @Transactional 기반 선언적 트랜잭션
- **Auditing**: 생성일시/수정일시 자동 관리

### 보안 설정

- **CORS**: 프론트엔드 도메인 허용
- **JWT**: stateless 인증 방식
- **Password**: BCrypt 해싱

### 캐시 설정

- **Redis**: 세션 저장, 인기 숙소 캐싱
- **분산 락**: 동시성 제어

## 📈 성능 최적화

- **Database Indexing**: 검색 쿼리 최적화
- **Query Optimization**: N+1 문제 해결, 배치 처리
- **Caching Strategy**: Redis 기반 캐싱
- **Connection Pooling**: 효율적인 DB 연결 관리
- **Image Optimization**: 이미지 압축 및 CDN 활용

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "AccommodationServiceTest"

# 통합 테스트 실행
./gradlew integrationTest
```

## 📁 관련 문서

- [기능 명세서](docs/FUNCTIONAL_SPECIFICATION.md)
- [API 가이드](docs/RESERVATION_API_GUIDE.md)
- [ERD](https://www.erdcloud.com/d/oTxP6FzFZtCwx4NYt)

---

⭐️ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요!
