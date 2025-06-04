package com.yanolja.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    PENDING("결제 대기"),
    SUCCESS("결제 성공"),
    FAILED("결제 실패"),
    CANCELLED("결제 취소"),
    REFUNDED("환불 완료"),
    PARTIAL_REFUNDED("부분 환불");

    private final String description;
} 