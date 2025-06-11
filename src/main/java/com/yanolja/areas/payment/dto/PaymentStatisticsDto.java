package com.yanolja.areas.payment.dto;

import com.yanolja.areas.payment.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentStatisticsDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "사용자별 결제 통계 DTO")
    public static class UserPaymentStats {

        @Schema(description = "결제 건수")
        private Long paymentCount;

        @Schema(description = "총 결제 금액")
        private BigDecimal totalAmount;

        @Schema(description = "평균 결제 금액")
        private BigDecimal averageAmount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결제 상태별 통계 DTO")
    public static class PaymentStatusStats {

        @Schema(description = "결제 상태")
        private PaymentStatus status;

        @Schema(description = "결제 건수")
        private Long count;

        @Schema(description = "총 결제 금액")
        private BigDecimal totalAmount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "결제 방법별 통계 DTO")
    public static class PaymentMethodStats {

        @Schema(description = "결제 방법")
        private String paymentMethod;

        @Schema(description = "결제 건수")
        private Long paymentCount;

        @Schema(description = "총 결제 금액")
        private BigDecimal totalAmount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "일별 결제 통계 DTO")
    public static class DailyPaymentStats {

        @Schema(description = "날짜")
        private LocalDate date;

        @Schema(description = "결제 건수")
        private Long paymentCount;

        @Schema(description = "총 결제 금액")
        private BigDecimal totalAmount;

        @Schema(description = "평균 결제 금액")
        private BigDecimal averageAmount;
    }
} 