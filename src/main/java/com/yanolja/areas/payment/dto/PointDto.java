package com.yanolja.areas.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class PointDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "포인트 적립 요청 DTO")
    public static class EarnRequest {

        @NotNull(message = "사용자 ID는 필수입니다.")
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @NotNull(message = "적립 금액은 필수입니다.")
        @Positive(message = "적립 금액은 양수여야 합니다.")
        @Schema(description = "적립 금액", example = "1000")
        private Integer amount;

        @NotNull(message = "주문 ID는 필수입니다.")
        @Schema(description = "주문 ID", example = "123")
        private Long orderId;

        @Schema(description = "적립 설명", example = "주문 완료 적립")
        private String description;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "포인트 사용 요청 DTO")
    public static class UseRequest {

        @NotNull(message = "사용자 ID는 필수입니다.")
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @NotNull(message = "사용 금액은 필수입니다.")
        @Positive(message = "사용 금액은 양수여야 합니다.")
        @Schema(description = "사용 금액", example = "500")
        private Integer amount;

        @NotNull(message = "주문 ID는 필수입니다.")
        @Schema(description = "주문 ID", example = "456")
        private Long orderId;

        @Schema(description = "사용 설명", example = "주문 결제")
        private String description;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "포인트 환불 요청 DTO")
    public static class RefundRequest {

        @NotNull(message = "사용자 ID는 필수입니다.")
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @NotNull(message = "환불 금액은 필수입니다.")
        @Positive(message = "환불 금액은 양수여야 합니다.")
        @Schema(description = "환불 금액", example = "500")
        private Integer amount;

        @NotNull(message = "주문 ID는 필수입니다.")
        @Schema(description = "주문 ID", example = "789")
        private Long orderId;

        @Schema(description = "환불 설명", example = "주문 취소로 인한 환불")
        private String description;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "포인트 만료 알림 DTO")
    public static class PointExpiryNotification {

        @Schema(description = "사용자 ID")
        private Long userId;

        @Schema(description = "만료 예정 포인트 금액")
        private Integer expiringAmount;

        @Schema(description = "만료일")
        private LocalDate expiryDate;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "포인트 통계 응답 DTO")
    public static class PointStatistics {

        @Schema(description = "사용자 ID")
        private Long userId;

        @Schema(description = "현재 포인트 잔액")
        private Integer currentBalance;

        @Schema(description = "총 적립 포인트")
        private Integer totalEarned;

        @Schema(description = "월간 적립 포인트")
        private Integer monthlyEarned;

        @Schema(description = "만료 예정 포인트")
        private Integer expiringAmount;
    }
} 