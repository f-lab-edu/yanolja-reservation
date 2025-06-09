package com.yanolja.areas.payment.repository;

import com.yanolja.areas.payment.entity.Coupon;
import com.yanolja.areas.payment.entity.CouponStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepositoryCustom {

    /**
     * 현재 발급 가능한 쿠폰 조회
     */
    List<Coupon> findAvailableForIssue(CouponStatus status, LocalDateTime currentTime);

    /**
     * 만료된 쿠폰 조회
     */
    List<Coupon> findExpiredCoupons(CouponStatus status, LocalDateTime currentTime);

    /**
     * 특정 사용자가 사용 가능한 쿠폰 조회
     */
    List<Coupon> findUsableCouponsForUser(Long userId, LocalDateTime currentTime);

    /**
     * 쿠폰 검색 (이름, 코드, 설명으로 검색)
     */
    List<Coupon> searchCoupons(String keyword);
} 