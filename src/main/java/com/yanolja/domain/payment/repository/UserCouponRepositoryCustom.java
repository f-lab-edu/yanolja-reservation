package com.yanolja.domain.payment.repository;

import com.yanolja.domain.payment.entity.UserCoupon;
import com.yanolja.domain.payment.entity.UserCouponStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCouponRepositoryCustom {

    /**
     * 사용자별 사용 가능한 쿠폰 조회
     */
    List<UserCoupon> findAvailableUserCoupons(Long userId, UserCouponStatus status, LocalDateTime currentTime);

    /**
     * 만료 예정 쿠폰 조회
     */
    List<UserCoupon> findExpiringUserCoupons(UserCouponStatus status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 사용자별 쿠폰 개수 조회
     */
    Long countByUserIdAndStatus(Long userId, UserCouponStatus status);

    /**
     * 특정 금액 이상 사용 가능한 쿠폰 조회
     */
    List<UserCoupon> findUsableCouponsForAmount(Long userId, Long orderAmount, LocalDateTime currentTime);
} 