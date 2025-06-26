package com.yanolja.areas.payment.entity;

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
@Table(name = "user_coupons",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "coupon_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("사용자 쿠폰 ID")
    private Long id;

    @Version
    @Comment("낙관적 락 버전")
    private Long version;

    @Column(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    @Comment("쿠폰")
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("쿠폰 상태")
    private UserCouponStatus status = UserCouponStatus.AVAILABLE;

    @Column(name = "used_at")
    @Comment("사용 일시")
    private LocalDateTime usedAt;

    @Column(name = "order_id")
    @Comment("사용된 주문 ID")
    private Long orderId;

    @Builder
    private UserCoupon(Long userId, Coupon coupon) {
        this.userId = userId;
        this.coupon = coupon;
    }

    /**
     * 사용자 쿠폰 생성
     */
    public static UserCoupon createUserCoupon(Long userId, Coupon coupon) {
        return UserCoupon.builder()
                .userId(userId)
                .coupon(coupon)
                .build();
    }

    /**
     * 사용자에게 쿠폰 발급
     */
    public static UserCoupon issueTo(Long userId, Coupon coupon) {
        return UserCoupon.builder()
                .userId(userId)
                .coupon(coupon)
                .build();
    }

    /**
     * 쿠폰 사용
     */
    public boolean tryUse() {
        if (this.status == UserCouponStatus.AVAILABLE) {
            this.status = UserCouponStatus.USED;
            this.usedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * 쿠폰 복원 (주문 취소 시)
     */
    public void restore() {
        this.status = UserCouponStatus.AVAILABLE;
        this.usedAt = null;
        this.orderId = null;
    }

    /**
     * 쿠폰 만료
     */
    public boolean tryExpire() {
        if (this.status == UserCouponStatus.AVAILABLE) {
            this.status = UserCouponStatus.EXPIRED;
            return true;
        }
        return false;
    }

    /**
     * 쿠폰 사용 가능 여부 확인
     */
    public boolean canUse() {
        LocalDateTime now = LocalDateTime.now();
        return status == UserCouponStatus.AVAILABLE &&
               coupon.canUse(BigDecimal.ZERO) && // 기본 금액으로 확인
               now.isBefore(coupon.getValidUntil());
    }
} 