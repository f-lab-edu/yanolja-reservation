package com.yanolja.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DiscountType {
    FIXED("정액 할인"),
    PERCENTAGE("정률 할인");

    private final String description;
} 