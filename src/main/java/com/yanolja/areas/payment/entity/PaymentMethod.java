package com.yanolja.areas.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("신용카드"),
    BANK_TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("무통장입금"),
    PAYCO("페이코"),
    TOSS("토스"),
    POINT("포인트");

    private final String description;
} 