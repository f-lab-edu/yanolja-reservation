package com.yanolja.domain.payment.entity;

import com.yanolja.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Table(name = "order_coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("주문 쿠폰 ID")
    private Long id;

    @Column(name = "order_id", nullable = false)
    @Comment("주문 ID")
    private Long orderId;

    @Column(name = "user_coupon_id", nullable = false)
    @Comment("사용자 쿠폰 ID")
    private Long userCouponId;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    @Comment("할인 금액")
    private BigDecimal discountAmount;

    @Builder
    private OrderCoupon(Long orderId, Long userCouponId, BigDecimal discountAmount) {
        this.orderId = orderId;
        this.userCouponId = userCouponId;
        this.discountAmount = discountAmount;
    }

    /**
     * 주문 쿠폰 생성
     */
    public static OrderCoupon create(Long orderId, Long userCouponId, BigDecimal discountAmount) {
        return OrderCoupon.builder()
                .orderId(orderId)
                .userCouponId(userCouponId)
                .discountAmount(discountAmount)
                .build();
    }

    /**
     * 사용자 쿠폰 ID 조회
     */
    public Long getUserCouponId() {
        return this.userCouponId;
    }
} 