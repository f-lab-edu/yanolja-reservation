package com.yanolja.areas.payment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.payment.entity.UserCoupon;
import com.yanolja.areas.payment.entity.UserCouponStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.yanolja.areas.payment.entity.QUserCoupon.userCoupon;
import static com.yanolja.areas.payment.entity.QCoupon.coupon;

@Repository
@RequiredArgsConstructor
public class UserCouponRepositoryImpl implements UserCouponRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserCoupon> findAvailableUserCoupons(Long userId, UserCouponStatus status, LocalDateTime currentTime) {
        return queryFactory
                .selectFrom(userCoupon)
                .join(userCoupon.coupon, coupon).fetchJoin()
                .where(
                        userCoupon.userId.eq(userId),
                        userCoupon.status.eq(status),
                        coupon.validFrom.loe(currentTime),
                        coupon.validUntil.goe(currentTime)
                )
                .orderBy(coupon.validUntil.asc())
                .fetch();
    }

    @Override
    public List<UserCoupon> findExpiringUserCoupons(UserCouponStatus status, LocalDateTime startTime, LocalDateTime endTime) {
        return queryFactory
                .selectFrom(userCoupon)
                .join(userCoupon.coupon, coupon)
                .where(
                        userCoupon.status.eq(status),
                        coupon.validUntil.between(startTime, endTime)
                )
                .fetch();
    }

    @Override
    public Long countByUserIdAndStatus(Long userId, UserCouponStatus status) {
        return queryFactory
                .select(userCoupon.count())
                .from(userCoupon)
                .where(
                        userCoupon.userId.eq(userId),
                        userCoupon.status.eq(status)
                )
                .fetchOne();
    }

    @Override
    public List<UserCoupon> findUsableCouponsForAmount(Long userId, Long orderAmount, LocalDateTime currentTime) {
        BigDecimal orderAmountDecimal = BigDecimal.valueOf(orderAmount);
        
        return queryFactory
                .selectFrom(userCoupon)
                .join(userCoupon.coupon, coupon).fetchJoin()
                .where(
                        userCoupon.userId.eq(userId),
                        userCoupon.status.eq(UserCouponStatus.AVAILABLE),
                        coupon.validFrom.loe(currentTime),
                        coupon.validUntil.goe(currentTime),
                        coupon.minOrderAmount.isNull()
                                .or(coupon.minOrderAmount.loe(orderAmountDecimal))
                )
                .orderBy(coupon.validUntil.asc())
                .fetch();
    }
} 