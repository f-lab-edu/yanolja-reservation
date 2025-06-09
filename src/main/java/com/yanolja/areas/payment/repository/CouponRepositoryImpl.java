package com.yanolja.areas.payment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.payment.entity.Coupon;
import com.yanolja.areas.payment.entity.CouponStatus;
import com.yanolja.areas.payment.entity.UserCouponStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.yanolja.areas.payment.entity.QCoupon.coupon;
import static com.yanolja.areas.payment.entity.QUserCoupon.userCoupon;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Coupon> findAvailableForIssue(CouponStatus status, LocalDateTime currentTime) {
        return queryFactory
                .selectFrom(coupon)
                .where(
                        coupon.status.eq(status),
                        coupon.issueStartAt.loe(currentTime),
                        coupon.issueEndAt.goe(currentTime),
                        coupon.usedCount.lt(coupon.issueCount)
                )
                .fetch();
    }

    @Override
    public List<Coupon> findExpiredCoupons(CouponStatus status, LocalDateTime currentTime) {
        return queryFactory
                .selectFrom(coupon)
                .where(
                        coupon.status.eq(status),
                        coupon.validUntil.lt(currentTime)
                )
                .fetch();
    }

    @Override
    public List<Coupon> findUsableCouponsForUser(Long userId, LocalDateTime currentTime) {
        return queryFactory
                .selectFrom(coupon)
                .join(userCoupon).on(userCoupon.coupon.eq(coupon))
                .where(
                        userCoupon.userId.eq(userId),
                        userCoupon.status.eq(UserCouponStatus.AVAILABLE),
                        coupon.validFrom.loe(currentTime),
                        coupon.validUntil.goe(currentTime),
                        coupon.status.eq(CouponStatus.ACTIVE)
                )
                .fetch();
    }

    @Override
    public List<Coupon> searchCoupons(String keyword) {
        return queryFactory
                .selectFrom(coupon)
                .where(
                        coupon.name.containsIgnoreCase(keyword)
                                .or(coupon.code.containsIgnoreCase(keyword))
                                .or(coupon.description.containsIgnoreCase(keyword))
                )
                .orderBy(coupon.createdAt.desc())
                .fetch();
    }
} 