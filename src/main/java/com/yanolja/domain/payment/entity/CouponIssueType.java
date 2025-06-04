package com.yanolja.domain.payment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponIssueType {
    SIGNUP("회원가입"),
    EVENT("이벤트"),
    REVIEW("리뷰 작성"),
    BIRTHDAY("생일"),
    MANUAL("수동 발급");

    private final String description;
} 