package com.yanolja.areas.reservation.dto;

import com.yanolja.areas.reservation.entity.ReservationOption;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservationOptionDto {

    /**
     * 예약 옵션 생성 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    public static class Request {
        @NotNull(message = "옵션 ID는 필수입니다.")
        private Long optionId;

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        private Integer quantity;

        @NotNull(message = "가격은 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다.")
        private BigDecimal price;

        @Builder
        public Request(Long optionId, Integer quantity, BigDecimal price) {
            this.optionId = optionId;
            this.quantity = quantity;
            this.price = price;
        }
    }

    /**
     * 예약 옵션 응답 DTO
     */
    @Getter
    @Builder
    public static class Response {
        private Long id;
        private Long optionId;
        private String optionName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal totalPrice;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(ReservationOption reservationOption) {
            return Response.builder()
                    .id(reservationOption.getId())
                    .optionId(reservationOption.getOptionId())
                    .quantity(reservationOption.getQuantity())
                    .price(reservationOption.getPrice())
                    .totalPrice(reservationOption.getTotalPrice())
                    .createdAt(reservationOption.getCreatedAt())
                    .updatedAt(reservationOption.getUpdatedAt())
                    .build();
        }

        public static Response fromEntityWithOptionName(ReservationOption reservationOption, String optionName) {
            return Response.builder()
                    .id(reservationOption.getId())
                    .optionId(reservationOption.getOptionId())
                    .optionName(optionName)
                    .quantity(reservationOption.getQuantity())
                    .price(reservationOption.getPrice())
                    .totalPrice(reservationOption.getTotalPrice())
                    .createdAt(reservationOption.getCreatedAt())
                    .updatedAt(reservationOption.getUpdatedAt())
                    .build();
        }
    }

    /**
     * 예약 옵션 목록 응답 DTO
     */
    @Getter
    @Builder
    public static class ListResponse {
        private Long id;
        private Long optionId;
        private String optionName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal totalPrice;

        public static ListResponse fromEntity(ReservationOption reservationOption) {
            return ListResponse.builder()
                    .id(reservationOption.getId())
                    .optionId(reservationOption.getOptionId())
                    .quantity(reservationOption.getQuantity())
                    .price(reservationOption.getPrice())
                    .totalPrice(reservationOption.getTotalPrice())
                    .build();
        }

        public static ListResponse fromEntityWithOptionName(ReservationOption reservationOption, String optionName) {
            return ListResponse.builder()
                    .id(reservationOption.getId())
                    .optionId(reservationOption.getOptionId())
                    .optionName(optionName)
                    .quantity(reservationOption.getQuantity())
                    .price(reservationOption.getPrice())
                    .totalPrice(reservationOption.getTotalPrice())
                    .build();
        }
    }
} 