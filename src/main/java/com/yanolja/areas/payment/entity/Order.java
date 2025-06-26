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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("주문 ID")
    private Long id;

    @Version
    @Comment("낙관적 락 버전")
    private Long version;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    @Comment("주문 번호")
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private Long userId;

    @Column(name = "reservation_id", nullable = false)
    @Comment("예약 ID")
    private Long reservationId;

    @Column(name = "original_amount", nullable = false, precision = 12, scale = 2)
    @Comment("원래 금액")
    private BigDecimal originalAmount;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Comment("할인 금액")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "points_used", nullable = false)
    @Comment("사용한 포인트")
    private Integer pointsUsed = 0;

    @Column(name = "final_amount", nullable = false, precision = 12, scale = 2)
    @Comment("최종 결제 금액")
    private BigDecimal finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("주문 상태")
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "expired_at")
    @Comment("주문 만료 시간")
    private LocalDateTime expiredAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Comment("주문 쿠폰 사용 내역")
    private List<OrderCoupon> orderCoupons = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Comment("주문 결제 내역")
    private List<Payment> payments = new ArrayList<>();

    @Builder
    private Order(String orderNumber, Long userId, Long reservationId, 
                  BigDecimal originalAmount, BigDecimal discountAmount, 
                  Integer pointsUsed, BigDecimal finalAmount, LocalDateTime expiredAt) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.reservationId = reservationId;
        this.originalAmount = originalAmount;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.pointsUsed = pointsUsed != null ? pointsUsed : 0;
        this.finalAmount = finalAmount;
        this.expiredAt = expiredAt;
    }

    /**
     * 주문 생성 (할인과 포인트 적용)
     */
    public static Order createOrder(String orderNumber, Long userId, Long reservationId, 
                                   BigDecimal originalAmount, BigDecimal discountAmount, 
                                   Integer pointsUsed, BigDecimal finalAmount) {
        return Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .reservationId(reservationId)
                .originalAmount(originalAmount)
                .discountAmount(discountAmount)
                .pointsUsed(pointsUsed)
                .finalAmount(finalAmount)
                .expiredAt(LocalDateTime.now().plusMinutes(10)) // 10분 후 만료
                .build();
    }

    /**
     * 주문 확정
     */
    public boolean tryConfirm() {
        if (this.status == OrderStatus.PENDING) {
            this.status = OrderStatus.CONFIRMED;
            return true;
        }
        return false;
    }

    /**
     * 주문 취소 가능 여부 확인
     */
    public boolean canCancel() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.CONFIRMED;
    }

    /**
     * 주문 만료 처리
     */
    public boolean tryExpire() {
        if (this.status == OrderStatus.PENDING) {
            this.status = OrderStatus.EXPIRED;
            return true;
        }
        return false;
    }

    /**
     * 쿠폰 적용
     */
    public void applyCoupon(BigDecimal discountAmount) {
        this.discountAmount = this.discountAmount.add(discountAmount);
        this.finalAmount = this.originalAmount.subtract(this.discountAmount).subtract(BigDecimal.valueOf(this.pointsUsed));
    }

    /**
     * 포인트 사용
     */
    public void usePoints(Integer points) {
        this.pointsUsed = points;
        this.finalAmount = this.originalAmount.subtract(this.discountAmount).subtract(BigDecimal.valueOf(points));
    }

    /**
     * 주문 상태 변경
     */
    public boolean tryUpdateStatus(OrderStatus newStatus) {
        if (canTransitionTo(newStatus)) {
            this.status = newStatus;
            return true;
        }
        return false;
    }

    /**
     * 주문 취소
     */
    public boolean tryCancel() {
        if (canCancel()) {
            this.status = OrderStatus.CANCELLED;
            return true;
        }
        return false;
    }

    /**
     * 주문 완료
     */
    public boolean tryComplete() {
        if (this.status == OrderStatus.CONFIRMED) {
            this.status = OrderStatus.COMPLETED;
            return true;
        }
        return false;
    }

    /**
     * 상태 전환 가능 여부 확인
     */
    private boolean canTransitionTo(OrderStatus newStatus) {
        switch (this.status) {
            case PENDING:
                return newStatus == OrderStatus.CONFIRMED || 
                       newStatus == OrderStatus.CANCELLED || 
                       newStatus == OrderStatus.EXPIRED;
            case CONFIRMED:
                return newStatus == OrderStatus.COMPLETED || 
                       newStatus == OrderStatus.CANCELLED;
            default:
                return false;
        }
    }

    /**
     * 주문 만료 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiredAt);
    }
} 