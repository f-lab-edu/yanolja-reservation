package com.yanolja.areas.payment.dto;

import com.yanolja.domain.payment.entity.OrderCoupon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderCouponDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "주문 쿠폰 응답 DTO")
    public static class Response {

        @Schema(description = "주문 쿠폰 ID")
        private Long id;

        @Schema(description = "쿠폰 코드")
        private String couponCode;

        @Schema(description = "쿠폰명")
        private String couponName;

        @Schema(description = "할인 금액")
        private BigDecimal discountAmount;

        @Schema(description = "적용 시간")
        private LocalDateTime createdAt;

        public static Response fromEntity(OrderCoupon orderCoupon) {
            return Response.builder()
                    .id(orderCoupon.getId())
                    .couponCode(orderCoupon.getUserCoupon().getCoupon().getCode())
                    .couponName(orderCoupon.getUserCoupon().getCoupon().getName())
                    .discountAmount(orderCoupon.getDiscountAmount())
                    .createdAt(orderCoupon.getCreatedAt())
                    .build();
        }
    }
} 