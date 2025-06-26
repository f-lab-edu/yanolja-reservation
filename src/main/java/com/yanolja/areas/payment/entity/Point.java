package com.yanolja.areas.payment.entity;

import com.yanolja.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(name = "points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("포인트 ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    @Comment("거래 타입")
    private PointTransactionType transactionType;

    @Column(name = "amount", nullable = false)
    @Comment("포인트 금액")
    private Integer amount;

    @Column(name = "balance", nullable = false)
    @Comment("거래 후 잔액")
    private Integer balance;

    @Column(name = "description", length = 500)
    @Comment("설명")
    private String description;

    @Column(name = "order_id")
    @Comment("주문 ID")
    private Long orderId;

    @Column(name = "expired_at")
    @Comment("만료일")
    private LocalDateTime expiredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("상태")
    private PointStatus status = PointStatus.ACTIVE;

    @Builder
    private Point(Long userId, PointTransactionType transactionType, Integer amount, 
                  Integer balance, String description, Long orderId, LocalDateTime expiredAt) {
        this.userId = userId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balance = balance;
        this.description = description;
        this.orderId = orderId;
        this.expiredAt = expiredAt;
    }

    /**
     * 포인트 적립
     */
    public static Point createEarnPoint(Long userId, Integer amount, Integer balance, 
                                       String description, Long orderId, LocalDateTime expiredAt) {
        return Point.builder()
                .userId(userId)
                .transactionType(PointTransactionType.EARN)
                .amount(amount)
                .balance(balance)
                .description(description)
                .orderId(orderId)
                .expiredAt(expiredAt)
                .build();
    }

    /**
     * 포인트 사용
     */
    public static Point createUsePoint(Long userId, Integer amount, Integer balance, 
                                      String description, Long orderId) {
        return Point.builder()
                .userId(userId)
                .transactionType(PointTransactionType.USE)
                .amount(-amount) // 사용은 음수로 저장
                .balance(balance)
                .description(description)
                .orderId(orderId)
                .build();
    }

    /**
     * 포인트 만료
     */
    public void expire() {
        this.status = PointStatus.EXPIRED;
    }

    /**
     * 포인트 취소
     */
    public void cancel() {
        this.status = PointStatus.CANCELLED;
    }
} 