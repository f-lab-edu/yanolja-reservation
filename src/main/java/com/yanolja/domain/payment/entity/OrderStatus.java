package com.yanolja.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("결제 대기"),
    CONFIRMED("주문 확정"),
    CANCELLED("주문 취소"),
    COMPLETED("주문 완료"),
    EXPIRED("주문 만료");

    private final String description;
} 