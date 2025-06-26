package com.yanolja.areas.payment.dto;

import com.yanolja.areas.payment.entity.Order;
import com.yanolja.areas.payment.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "주문 생성 요청 DTO")
    public static class CreateRequest {

        @NotNull(message = "사용자 ID는 필수입니다.")
        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @NotNull(message = "예약 ID는 필수입니다.")
        @Schema(description = "예약 ID", example = "1")
        private Long reservationId;

        @NotNull(message = "원본 금액은 필수입니다.")
        @Positive(message = "원본 금액은 0보다 커야 합니다.")
        @Schema(description = "원본 금액", example = "100000")
        private BigDecimal originalAmount;

        @Schema(description = "사용할 쿠폰 ID 목록")
        private List<Long> couponIds;

        @Builder.Default
        @Schema(description = "사용할 포인트", example = "5000")
        private Integer pointsUsed = 0;

        // Getter 메서드들
        public Long getUserId() { return userId; }
        public Long getReservationId() { return reservationId; }
        public BigDecimal getOriginalAmount() { return originalAmount; }
        public List<Long> getCouponIds() { return couponIds; }
        public Integer getPointsUsed() { return pointsUsed; }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "주문 응답 DTO")
    public static class Response {

        @Schema(description = "주문 ID")
        private Long id;

        @Schema(description = "주문 번호")
        private String orderNumber;

        @Schema(description = "사용자 ID")
        private Long userId;

        @Schema(description = "예약 ID")
        private Long reservationId;

        @Schema(description = "원본 금액")
        private BigDecimal originalAmount;

        @Schema(description = "할인 금액")
        private BigDecimal discountAmount;

        @Schema(description = "사용한 포인트")
        private Integer pointsUsed;

        @Schema(description = "최종 결제 금액")
        private BigDecimal finalAmount;

        @Schema(description = "주문 상태")
        private OrderStatus status;

        @Schema(description = "주문 만료 시간")
        private LocalDateTime expiredAt;

        @Schema(description = "생성 시간")
        private LocalDateTime createdAt;

        @Schema(description = "사용된 쿠폰 목록")
        private List<OrderCouponDto.Response> usedCoupons;

        public void setUsedCoupons(List<OrderCouponDto.Response> usedCoupons) {
            this.usedCoupons = usedCoupons;
        }

        public static Response fromEntity(Order order) {
            return Response.builder()
                    .id(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .userId(order.getUserId())
                    .reservationId(order.getReservationId())
                    .originalAmount(order.getOriginalAmount())
                    .discountAmount(order.getDiscountAmount())
                    .pointsUsed(order.getPointsUsed())
                    .finalAmount(order.getFinalAmount())
                    .status(order.getStatus())
                    .expiredAt(order.getExpiredAt())
                    .createdAt(order.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "주문 목록 응답 DTO")
    public static class ListResponse {

        @Schema(description = "주문 ID")
        private Long id;

        @Schema(description = "주문 번호")
        private String orderNumber;

        @Schema(description = "예약 ID")
        private Long reservationId;

        @Schema(description = "최종 결제 금액")
        private BigDecimal finalAmount;

        @Schema(description = "주문 상태")
        private OrderStatus status;

        @Schema(description = "생성 시간")
        private LocalDateTime createdAt;

        public static ListResponse fromEntity(Order order) {
            return ListResponse.builder()
                    .id(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .reservationId(order.getReservationId())
                    .finalAmount(order.getFinalAmount())
                    .status(order.getStatus())
                    .createdAt(order.getCreatedAt())
                    .build();
        }
    }
} 