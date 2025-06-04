package com.yanolja.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponStatus {
    ACTIVE("사용 가능"),
    INACTIVE("사용 불가"),
    EXPIRED("만료됨"),
    SOLD_OUT("소진됨");

    private final String description;
} 