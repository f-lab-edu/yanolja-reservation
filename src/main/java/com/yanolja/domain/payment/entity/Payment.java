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
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("결제 ID")
    private Long id;

    @Column(name = "payment_key", nullable = false, unique = true, length = 100)
    @Comment("결제 키")
    private String paymentKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @Comment("주문")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    @Comment("결제 수단")
    private PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    @Comment("결제 금액")
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("결제 상태")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "pg_provider", length = 50)
    @Comment("PG사")
    private String pgProvider;

    @Column(name = "pg_transaction_id", length = 200)
    @Comment("PG사 거래 ID")
    private String pgTransactionId;

    @Column(name = "approval_number", length = 100)
    @Comment("승인 번호")
    private String approvalNumber;

    @Column(name = "card_number", length = 20)
    @Comment("카드 번호 (마스킹)")
    private String cardNumber;

    @Column(name = "card_type", length = 50)
    @Comment("카드 타입")
    private String cardType;

    @Column(name = "installment_months")
    @Comment("할부 개월")
    private Integer installmentMonths;

    @Column(name = "paid_at")
    @Comment("결제 완료 시간")
    private LocalDateTime paidAt;

    @Column(name = "failure_reason", length = 500)
    @Comment("결제 실패 사유")
    private String failureReason;

    @Column(name = "receipt_url", length = 500)
    @Comment("영수증 URL")
    private String receiptUrl;

    @Column(name = "refunded_amount", precision = 12, scale = 2)
    @Comment("환불 금액")
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Builder
    private Payment(String paymentKey, Order order, PaymentMethod paymentMethod, 
                    BigDecimal amount, String pgProvider) {
        this.paymentKey = paymentKey;
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.pgProvider = pgProvider;
    }

    /**
     * 결제 생성
     */
    public static Payment createPayment(String paymentKey, Order order, 
                                       PaymentMethod paymentMethod, BigDecimal amount) {
        return Payment.builder()
                .paymentKey(paymentKey)
                .order(order)
                .paymentMethod(paymentMethod)
                .amount(amount)
                .build();
    }

    /**
     * 결제 승인
     */
    public void approvePayment(String pgTransactionId, String approvalNumber, String receiptUrl) {
        this.status = PaymentStatus.SUCCESS;
        this.pgTransactionId = pgTransactionId;
        this.approvalNumber = approvalNumber;
        this.receiptUrl = receiptUrl;
        this.paidAt = LocalDateTime.now();
    }

    /**
     * 결제 실패
     */
    public void failPayment(String failureReason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
    }

    /**
     * 결제 취소
     */
    public void cancelPayment() {
        this.status = PaymentStatus.CANCELLED;
    }

    /**
     * 부분 환불
     */
    public void refundPayment(BigDecimal refundAmount) {
        this.refundedAmount = this.refundedAmount.add(refundAmount);
        
        // 전액 환불인 경우 상태 변경
        if (this.refundedAmount.compareTo(this.amount) >= 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIAL_REFUNDED;
        }
    }

    /**
     * 환불된 금액 조회
     */
    public BigDecimal getRefundedAmount() {
        return this.refundedAmount != null ? this.refundedAmount : BigDecimal.ZERO;
    }

    /**
     * 카드 정보 설정
     */
    public void setCardInfo(String cardNumber, String cardType, Integer installmentMonths) {
        this.cardNumber = cardNumber;
        this.cardType = cardType;
        this.installmentMonths = installmentMonths;
    }
} 