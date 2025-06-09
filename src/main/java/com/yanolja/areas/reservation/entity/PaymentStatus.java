package com.yanolja.areas.reservation.entity;

/**
 * 결제 상태 열거형
 */
public enum PaymentStatus {
    PENDING("결제 대기"),
    PROCESSING("결제 처리중"),
    COMPLETED("결제 완료"),
    FAILED("결제 실패"),
    CANCELLED("결제 취소"),
    REFUNDED("환불 완료");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 