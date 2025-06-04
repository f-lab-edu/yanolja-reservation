package com.yanolja.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointTransactionType {
    EARN("적립"),
    USE("사용"),
    EXPIRE("만료"),
    CANCEL("취소");

    private final String description;
}