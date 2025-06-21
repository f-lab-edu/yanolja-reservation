package com.yanolja.areas.payment.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @Comment("주문")
    private Order order;

    @Column(name = "user_coupon_id", nullable = false)
    @Comment("사용자 쿠폰 ID")
    private Long userCouponId;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    @Comment("할인 금액")
    private BigDecimal discountAmount;

    @Builder
    private OrderCoupon(Order order, Long userCouponId, BigDecimal discountAmount) {
        this.order = order;
        this.userCouponId = userCouponId;
        this.discountAmount = discountAmount;
    }

    /**
     * 주문 쿠폰 생성
     */
    public static OrderCoupon create(Order order, Long userCouponId, BigDecimal discountAmount) {
        return OrderCoupon.builder()
                .order(order)
                .userCouponId(userCouponId)
                .discountAmount(discountAmount)
                .build();
    }

    /**
     * 주문 ID 조회 (편의 메서드)
     */
    public Long getOrderId() {
        return this.order != null ? this.order.getId() : null;
    }

    /**
     * 사용자 쿠폰 ID 조회
     */
    public Long getUserCouponId() {
        return this.userCouponId;
    }
} 