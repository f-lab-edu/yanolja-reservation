# 야놀자 예약 시스템 API 가이드

## 📋 개요

이 문서는 야놀자 예약 시스템의 REST API 사용법을 설명합니다.

## 🔐 인증

모든 API 요청은 JWT 토큰을 통한 인증이 필요합니다.

```http
Authorization: Bearer {accessToken}
```

## 🌟 API 분류

### 📱 포털 API (일반 사용자용)

- **경로**: `/api/portal/reservations`
- **대상**: 일반 사용자 (포털 서비스)
- **기능**: 예약 생성, 조회, 취소, 통계 등

### 🔧 관리자 API

- **경로**: `/api/v1/reservations`
- **대상**: 관리자, 시스템
- **기능**: 예약 확정, 시스템 관리, 전체 현황 등

## 📡 포털 API 엔드포인트

### 1. 예약 생성

**POST** `/api/portal/reservations`

#### 요청 본문

```json
{
  "roomId": 1,
  "checkInDate": "2024-03-01",
  "checkOutDate": "2024-03-02",
  "totalPrice": 89000,
  "options": [
    {
      "optionId": 1,
      "quantity": 2,
      "price": 10000
    }
  ]
}
```

#### 응답

```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "roomId": 1,
    "checkInDate": "2024-03-01",
    "checkOutDate": "2024-03-02",
    "nights": 1,
    "totalPrice": 89000,
    "status": "PENDING",
    "paymentStatus": "PENDING",
    "canCancel": true,
    "canModify": true,
    "options": [
      {
        "id": 1,
        "optionId": 1,
        "quantity": 2,
        "price": 10000,
        "totalPrice": 20000
      }
    ],
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

### 2. 예약 상세 조회

**GET** `/api/portal/reservations/{reservationId}`

#### 응답

```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "roomId": 1,
    "roomName": "스탠다드 더블",
    "accommodationName": "서울 시티 호텔",
    "accommodationAddress": "서울시 강남구 테헤란로 123",
    "accommodationImage": "https://cdn.yanolja.com/image1.jpg",
    "checkInDate": "2024-03-01",
    "checkOutDate": "2024-03-02",
    "nights": 1,
    "totalPrice": 89000,
    "status": "CONFIRMED",
    "paymentStatus": "COMPLETED",
    "canCancel": true,
    "canModify": false,
    "options": [
      {
        "id": 1,
        "optionId": 1,
        "optionName": "조식",
        "quantity": 2,
        "price": 10000,
        "totalPrice": 20000
      }
    ],
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

### 3. 예약 목록 조회

**GET** `/api/portal/reservations?status=CONFIRMED&page=0&size=10`

#### 쿼리 파라미터

- `status` (optional): 예약 상태 필터
- `page` (default: 0): 페이지 번호
- `size` (default: 10): 페이지 크기

#### 응답

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "roomId": 1,
        "roomName": "스탠다드 더블",
        "accommodationName": "서울 시티 호텔",
        "accommodationImage": "https://cdn.yanolja.com/image1.jpg",
        "checkInDate": "2024-03-01",
        "checkOutDate": "2024-03-02",
        "nights": 1,
        "totalPrice": 89000,
        "status": "CONFIRMED",
        "paymentStatus": "COMPLETED",
        "canCancel": true,
        "canModify": false,
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 15,
    "totalPages": 2,
    "size": 10,
    "number": 0
  }
}
```

### 4. 예약 고급 검색

**POST** `/api/portal/reservations/search?page=0&size=10`

#### 요청 본문

```json
{
  "statuses": ["CONFIRMED", "COMPLETED"],
  "checkInDateFrom": "2024-03-01",
  "checkInDateTo": "2024-03-31",
  "checkOutDateFrom": "2024-03-02",
  "checkOutDateTo": "2024-04-01"
}
```

### 5. 예약 취소

**PATCH** `/api/portal/reservations/{reservationId}/cancel`

#### 응답

```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "CANCELLED",
    "paymentStatus": "CANCELLED",
    "canCancel": false,
    "canModify": false
  }
}
```

### 6. 예약 상태 변경

**PATCH** `/api/portal/reservations/{reservationId}/status`

#### 요청 본문

```json
{
  "status": "COMPLETED",
  "reason": "숙박 완료"
}
```

### 7. 사용자 예약 통계

**GET** `/api/portal/reservations/stats`

#### 응답

```json
{
  "success": true,
  "data": [
    ["CONFIRMED", 5, 450000],
    ["COMPLETED", 3, 270000],
    ["CANCELLED", 1, 90000]
  ]
}
```

## 🔧 관리자 API 엔드포인트

### 1. 예약 확정

**PATCH** `/api/v1/reservations/{reservationId}/confirm`

#### 응답

```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "CONFIRMED",
    "paymentStatus": "COMPLETED"
  }
}
```

### 2. 만료된 예약 정리

**POST** `/api/v1/reservations/cleanup`

#### 응답

```json
{
  "success": true,
  "data": null
}
```

### 3. 객실별 예약 현황

**GET** `/api/v1/reservations/rooms/{roomId}/status?startDate=2024-03-01&endDate=2024-03-31`

#### 쿼리 파라미터

- `startDate`: 조회 시작 날짜 (required)
- `endDate`: 조회 종료 날짜 (required)

#### 응답

```json
{
  "success": true,
  "data": [
    ["2024-03-01", "2024-03-02", "CONFIRMED", 1],
    ["2024-03-05", "2024-03-06", "COMPLETED", 1],
    ["2024-03-10", "2024-03-12", "PENDING", 1]
  ]
}
```

## 📊 예약 상태

| 상태        | 설명                       |
| ----------- | -------------------------- |
| `PENDING`   | 결제 대기 (10분 자동 취소) |
| `CONFIRMED` | 예약 확정                  |
| `CANCELLED` | 예약 취소                  |
| `REJECTED`  | 숙소 거절                  |
| `COMPLETED` | 숙박 완료                  |
| `NO_SHOW`   | 노쇼                       |

## 💳 결제 상태

| 상태         | 설명        |
| ------------ | ----------- |
| `PENDING`    | 결제 대기   |
| `PROCESSING` | 결제 처리중 |
| `COMPLETED`  | 결제 완료   |
| `FAILED`     | 결제 실패   |
| `CANCELLED`  | 결제 취소   |
| `REFUNDED`   | 환불 완료   |

## ⚠️ 에러 코드

| HTTP 상태 | 에러 코드               | 설명        |
| --------- | ----------------------- | ----------- |
| 400       | `BAD_REQUEST`           | 잘못된 요청 |
| 401       | `UNAUTHORIZED`          | 인증 실패   |
| 403       | `FORBIDDEN`             | 권한 없음   |
| 404       | `NOT_FOUND`             | 리소스 없음 |
| 409       | `CONFLICT`              | 예약 충돌   |
| 500       | `INTERNAL_SERVER_ERROR` | 서버 에러   |

## 🔄 비즈니스 규칙

### 예약 생성 규칙

- 체크인 날짜는 오늘 이후여야 함
- 체크아웃 날짜는 체크인 날짜보다 이후여야 함
- 최대 숙박 기간: 30일
- 객실 중복 예약 불가

### 예약 취소 규칙

- `PENDING`, `CONFIRMED` 상태에서만 취소 가능
- 체크인 3일 전: 100% 환불
- 체크인 1-2일 전: 50% 환불
- 체크인 당일: 환불 불가

### 예약 변경 규칙

- `PENDING`, `CONFIRMED` 상태에서만 변경 가능
- 체크인 1일 전까지 1회 무료 변경

## 🕒 자동 처리

### 만료된 예약 정리

- 매 5분마다 `PENDING` 상태로 10분 이상 지난 예약 자동 취소
- 체크인 시간 2시간 경과 시 자동 노쇼 처리

## 📝 사용 예시

### JavaScript 예시 (포털)

```javascript
// 예약 생성
const createReservation = async (reservationData) => {
  const response = await fetch("/api/portal/reservations", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(reservationData),
  });

  return await response.json();
};

// 예약 목록 조회
const getReservations = async (status = null, page = 0, size = 10) => {
  const params = new URLSearchParams({
    page: page,
    size: size,
  });

  if (status) {
    params.append("status", status);
  }

  const response = await fetch(`/api/portal/reservations?${params}`, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  return await response.json();
};

// 예약 취소
const cancelReservation = async (reservationId) => {
  const response = await fetch(
    `/api/portal/reservations/${reservationId}/cancel`,
    {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    }
  );

  return await response.json();
};
```

### cURL 예시

#### 포털 API

```bash
# 예약 생성
curl -X POST '/api/portal/reservations' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer your_token_here' \
  -d '{
    "roomId": 1,
    "checkInDate": "2024-03-01",
    "checkOutDate": "2024-03-02",
    "totalPrice": 89000
  }'

# 예약 목록 조회
curl -X GET '/api/portal/reservations?status=CONFIRMED&page=0&size=10' \
  -H 'Authorization: Bearer your_token_here'

# 예약 취소
curl -X PATCH '/api/portal/reservations/1/cancel' \
  -H 'Authorization: Bearer your_token_here'
```

#### 관리자 API

```bash
# 예약 확정
curl -X PATCH '/api/v1/reservations/1/confirm' \
  -H 'Authorization: Bearer admin_token_here'

# 객실 예약 현황
curl -X GET '/api/v1/reservations/rooms/1/status?startDate=2024-03-01&endDate=2024-03-31' \
  -H 'Authorization: Bearer admin_token_here'

# 만료된 예약 정리
curl -X POST '/api/v1/reservations/cleanup' \
  -H 'Authorization: Bearer admin_token_here'
```
