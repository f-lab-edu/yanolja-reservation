package com.yanolja.areas.payment.dto;

import com.yanolja.areas.payment.entity.Payment;
import com.yanolja.areas.payment.entity.PaymentMethod;
import com.yanolja.areas.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결제 요청 DTO")
    public static class Request {

        @NotNull(message = "주문 번호는 필수입니다.")
        @Schema(description = "주문 번호", example = "ORD123456789")
        private String orderNumber;

        @NotNull(message = "결제 수단은 필수입니다.")
        @Schema(description = "결제 수단")
        private PaymentMethod paymentMethod;

        @NotNull(message = "결제 금액은 필수입니다.")
        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        @Schema(description = "결제 금액", example = "95000")
        private BigDecimal amount;

        @Schema(description = "PG사", example = "toss")
        private String pgProvider;

        @Schema(description = "카드 정보")
        private CardInfo cardInfo;

        // Getter 메서드들
        public String getOrderNumber() { return orderNumber; }
        public PaymentMethod getPaymentMethod() { return paymentMethod; }
        public BigDecimal getAmount() { return amount; }
        public String getPgProvider() { return pgProvider; }
        public CardInfo getCardInfo() { return cardInfo; }

        @Getter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @Schema(description = "카드 정보")
        public static class CardInfo {
            @Schema(description = "카드 번호", example = "1234-5678-9012-3456")
            private String cardNumber;

            @Schema(description = "카드 타입", example = "신용카드")
            private String cardType;

            @Schema(description = "할부 개월", example = "0")
            private Integer installmentMonths;

            // Getter 메서드들
            public String getCardNumber() { return cardNumber; }
            public String getCardType() { return cardType; }
            public Integer getInstallmentMonths() { return installmentMonths; }
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결제 승인 요청 DTO")
    public static class ApproveRequest {

        @NotNull(message = "결제 키는 필수입니다.")
        @Schema(description = "결제 키")
        private String paymentKey;

        @Schema(description = "PG사 거래 ID")
        private String pgTransactionId;

        @Schema(description = "승인 번호")
        private String approvalNumber;

        @Schema(description = "영수증 URL")
        private String receiptUrl;

        // Getter 메서드들
        public String getPaymentKey() { return paymentKey; }
        public String getPgTransactionId() { return pgTransactionId; }
        public String getApprovalNumber() { return approvalNumber; }
        public String getReceiptUrl() { return receiptUrl; }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결제 응답 DTO")
    public static class Response {

        @Schema(description = "결제 ID")
        private Long id;

        @Schema(description = "결제 키")
        private String paymentKey;

        @Schema(description = "주문 ID")
        private Long orderId;

        @Schema(description = "결제 수단")
        private PaymentMethod paymentMethod;

        @Schema(description = "결제 금액")
        private BigDecimal amount;

        @Schema(description = "결제 상태")
        private PaymentStatus status;

        @Schema(description = "PG사")
        private String pgProvider;

        @Schema(description = "PG사 거래 ID")
        private String pgTransactionId;

        @Schema(description = "승인 번호")
        private String approvalNumber;

        @Schema(description = "카드 번호 (마스킹)")
        private String cardNumber;

        @Schema(description = "카드 타입")
        private String cardType;

        @Schema(description = "할부 개월")
        private Integer installmentMonths;

        @Schema(description = "결제 완료 시간")
        private LocalDateTime paidAt;

        @Schema(description = "영수증 URL")
        private String receiptUrl;

        @Schema(description = "실패 사유")
        private String failureReason;

        @Schema(description = "생성 시간")
        private LocalDateTime createdAt;

        public static Response fromEntity(Payment payment) {
            return Response.builder()
                    .id(payment.getId())
                    .paymentKey(payment.getPaymentKey())
                    .orderId(payment.getOrder().getId())
                    .paymentMethod(payment.getPaymentMethod())
                    .amount(payment.getAmount())
                    .status(payment.getStatus())
                    .pgProvider(payment.getPgProvider())
                    .pgTransactionId(payment.getPgTransactionId())
                    .approvalNumber(payment.getApprovalNumber())
                    .cardNumber(payment.getCardNumber())
                    .cardType(payment.getCardType())
                    .installmentMonths(payment.getInstallmentMonths())
                    .paidAt(payment.getPaidAt())
                    .receiptUrl(payment.getReceiptUrl())
                    .failureReason(payment.getFailureReason())
                    .createdAt(payment.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결제 목록 응답 DTO")
    public static class ListResponse {

        @Schema(description = "결제 ID")
        private Long id;

        @Schema(description = "결제 키")
        private String paymentKey;

        @Schema(description = "주문 번호")
        private String orderNumber;

        @Schema(description = "결제 수단")
        private PaymentMethod paymentMethod;

        @Schema(description = "결제 금액")
        private BigDecimal amount;

        @Schema(description = "결제 상태")
        private PaymentStatus status;

        @Schema(description = "결제 완료 시간")
        private LocalDateTime paidAt;

        @Schema(description = "환불 금액")
        private BigDecimal refundedAmount;

        @Schema(description = "생성 시간")
        private LocalDateTime createdAt;

        public static ListResponse fromEntity(Payment payment) {
            return ListResponse.builder()
                    .id(payment.getId())
                    .paymentKey(payment.getPaymentKey())
                    .orderNumber(payment.getOrder().getOrderNumber())
                    .paymentMethod(payment.getPaymentMethod())
                    .amount(payment.getAmount())
                    .status(payment.getStatus())
                    .paidAt(payment.getPaidAt())
                    .refundedAmount(payment.getRefundedAmount())
                    .createdAt(payment.getCreatedAt())
                    .build();
        }
    }
} 