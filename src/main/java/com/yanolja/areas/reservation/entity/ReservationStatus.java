package com.yanolja.areas.reservation.entity;

/**
 * 예약 상태 열거형
 */
public enum ReservationStatus {
    PENDING("결제 대기"),
    CONFIRMED("예약 확정"),
    CANCELLED("예약 취소"),
    REJECTED("숙소 거절"),
    COMPLETED("숙박 완료"),
    NO_SHOW("노쇼");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 