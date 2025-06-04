package com.yanolja.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointStatus {
    ACTIVE("활성"),
    EXPIRED("만료"),
    CANCELLED("취소됨");

    private final String description;
} 