package com.yanolja.areas.payment.dto;

import com.yanolja.areas.payment.entity.CouponIssueType;
import com.yanolja.areas.payment.entity.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CouponDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "쿠폰 생성 요청 DTO")
    public static class CreateCouponRequest {

        @NotBlank(message = "쿠폰 코드는 필수입니다.")
        @Schema(description = "쿠폰 코드", example = "WELCOME10")
        private String code;

        @NotBlank(message = "쿠폰 이름은 필수입니다.")
        @Schema(description = "쿠폰 이름", example = "신규 가입 쿠폰")
        private String name;

        @Schema(description = "쿠폰 설명", example = "신규 가입자 10% 할인")
        private String description;

        @NotNull(message = "할인 타입은 필수입니다.")
        @Schema(description = "할인 타입")
        private DiscountType discountType;

        @NotNull(message = "할인 값은 필수입니다.")
        @PositiveOrZero(message = "할인 값은 0 이상이어야 합니다.")
        @Schema(description = "할인 값 (정액: 원, 정률: %)", example = "10")
        private BigDecimal discountValue;

        @Schema(description = "최대 할인 금액", example = "10000")
        private BigDecimal maxDiscountAmount;

        @Schema(description = "최소 주문 금액", example = "50000")
        private BigDecimal minOrderAmount;

        @NotNull(message = "발급 수량은 필수입니다.")
        @PositiveOrZero(message = "발급 수량은 0 이상이어야 합니다.")
        @Schema(description = "발급 수량", example = "100")
        private Integer issueCount;

        @NotNull(message = "발급 시작일은 필수입니다.")
        @Schema(description = "발급 시작일시")
        private LocalDateTime issueStartAt;

        @NotNull(message = "발급 종료일은 필수입니다.")
        @Schema(description = "발급 종료일시")
        private LocalDateTime issueEndAt;

        @NotNull(message = "사용 시작일은 필수입니다.")
        @Schema(description = "사용 시작일시")
        private LocalDateTime validFrom;

        @NotNull(message = "사용 종료일은 필수입니다.")
        @Schema(description = "사용 종료일시")
        private LocalDateTime validUntil;

        @NotNull(message = "발급 타입은 필수입니다.")
        @Schema(description = "발급 타입")
        private CouponIssueType issueType;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "쿠폰 통계 응답 DTO")
    public static class CouponStatistics {

        @Schema(description = "쿠폰 ID")
        private Long couponId;

        @Schema(description = "총 발급 수량")
        private Integer totalIssued;

        @Schema(description = "남은 발급 수량")
        private Integer remainingIssue;

        @Schema(description = "사용 가능한 쿠폰 수")
        private Long availableCount;

        @Schema(description = "사용된 쿠폰 수")
        private Long usedCount;

        @Schema(description = "만료된 쿠폰 수")
        private Long expiredCount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "쿠폰 할인 계산 요청 DTO")
    public static class DiscountCalculateRequest {

        @NotNull(message = "쿠폰 ID는 필수입니다.")
        @Schema(description = "쿠폰 ID", example = "1")
        private Long couponId;

        @NotNull(message = "주문 금액은 필수입니다.")
        @Positive(message = "주문 금액은 양수여야 합니다.")
        @Schema(description = "주문 금액", example = "100000")
        private BigDecimal orderAmount;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "쿠폰 할인 계산 응답 DTO")
    public static class DiscountCalculateResponse {

        @Schema(description = "할인 금액", example = "10000")
        private BigDecimal discountAmount;

        @Schema(description = "최대 할인 한도 적용 여부", example = "false")
        private Boolean isMaxDiscountApplied;

        @Schema(description = "할인율 (%)", example = "10")
        private BigDecimal discountRate;
    }
} 