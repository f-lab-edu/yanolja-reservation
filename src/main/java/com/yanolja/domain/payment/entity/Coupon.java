package com.yanolja.domain.payment.entity;

import com.yanolja.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("쿠폰 ID")
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    @Comment("쿠폰 코드")
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    @Comment("쿠폰명")
    private String name;

    @Column(name = "description", length = 500)
    @Comment("쿠폰 설명")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    @Comment("할인 타입")
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    @Comment("할인 값")
    private BigDecimal discountValue;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    @Comment("최대 할인 금액")
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount", precision = 12, scale = 2)
    @Comment("최소 주문 금액")
    private BigDecimal minOrderAmount;

    @Column(name = "issue_count", nullable = false)
    @Comment("발급 수량")
    private Integer issueCount;

    @Column(name = "used_count", nullable = false)
    @Comment("사용된 수량")
    private Integer usedCount = 0;

    @Column(name = "issue_start_at", nullable = false)
    @Comment("발급 시작 시간")
    private LocalDateTime issueStartAt;

    @Column(name = "issue_end_at", nullable = false)
    @Comment("발급 종료 시간")
    private LocalDateTime issueEndAt;

    @Column(name = "valid_from", nullable = false)
    @Comment("사용 시작일")
    private LocalDateTime validFrom;

    @Column(name = "valid_until", nullable = false)
    @Comment("사용 종료일")
    private LocalDateTime validUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("쿠폰 상태")
    private CouponStatus status = CouponStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false)
    @Comment("발급 타입")
    private CouponIssueType issueType;

    @Builder
    private Coupon(String code, String name, String description, DiscountType discountType,
                   BigDecimal discountValue, BigDecimal maxDiscountAmount, BigDecimal minOrderAmount,
                   Integer issueCount, LocalDateTime issueStartAt, LocalDateTime issueEndAt,
                   LocalDateTime validFrom, LocalDateTime validUntil, CouponIssueType issueType) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderAmount = minOrderAmount;
        this.issueCount = issueCount;
        this.issueStartAt = issueStartAt;
        this.issueEndAt = issueEndAt;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.issueType = issueType;
    }

    /**
     * 쿠폰 생성
     */
    public static Coupon createCoupon(String code, String name, String description,
                                     DiscountType discountType, BigDecimal discountValue,
                                     BigDecimal maxDiscountAmount, BigDecimal minOrderAmount,
                                     Integer issueCount, LocalDateTime issueStartAt, LocalDateTime issueEndAt,
                                     LocalDateTime validFrom, LocalDateTime validUntil, CouponIssueType issueType) {
        return Coupon.builder()
                .code(code)
                .name(name)
                .description(description)
                .discountType(discountType)
                .discountValue(discountValue)
                .maxDiscountAmount(maxDiscountAmount)
                .minOrderAmount(minOrderAmount)
                .issueCount(issueCount)
                .issueStartAt(issueStartAt)
                .issueEndAt(issueEndAt)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .issueType(issueType)
                .build();
    }

    /**
     * 할인 금액 계산
     */
    public BigDecimal calculateDiscountAmount(BigDecimal orderAmount) {
        if (!canUse(orderAmount)) {
            return BigDecimal.ZERO;
        }

        BigDecimal discountAmount;
        if (discountType == DiscountType.FIXED) {
            discountAmount = discountValue;
        } else {
            discountAmount = orderAmount.multiply(discountValue.divide(BigDecimal.valueOf(100)));
        }

        // 최대 할인 금액 체크
        if (maxDiscountAmount != null && discountAmount.compareTo(maxDiscountAmount) > 0) {
            discountAmount = maxDiscountAmount;
        }

        return discountAmount;
    }

    /**
     * 쿠폰 사용 가능 여부 확인
     */
    public boolean canUse(BigDecimal orderAmount) {
        LocalDateTime now = LocalDateTime.now();
        
        // 상태 확인
        if (status != CouponStatus.ACTIVE) {
            return false;
        }
        
        // 사용 기간 확인
        if (now.isBefore(validFrom) || now.isAfter(validUntil)) {
            return false;
        }
        
        // 최소 주문 금액 확인
        if (minOrderAmount != null && orderAmount.compareTo(minOrderAmount) < 0) {
            return false;
        }
        
        // 재고 확인
        if (usedCount >= issueCount) {
            return false;
        }
        
        return true;
    }

    /**
     * 쿠폰 사용
     */
    public void use() {
        this.usedCount++;
        if (this.usedCount >= this.issueCount) {
            this.status = CouponStatus.SOLD_OUT;
        }
    }

    /**
     * 쿠폰 상태 변경
     */
    public void updateStatus(CouponStatus status) {
        this.status = status;
    }

    /**
     * 사용된 수량 증가
     */
    public void increaseUsedCount() {
        this.usedCount++;
        if (this.usedCount >= this.issueCount) {
            this.status = CouponStatus.SOLD_OUT;
        }
    }
} 