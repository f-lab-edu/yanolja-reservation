package com.yanolja.areas.reservation.dto;

import com.yanolja.areas.reservation.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 예약 옵션 사용 통계 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "예약 옵션 사용 통계 DTO")
public class ReservationOptionStatsDto {
    
    @Schema(description = "예약 상태")
    private ReservationStatus status;
    
    @Schema(description = "총 사용 수량")
    private Long totalQuantity;
    
    @Schema(description = "총 사용 금액")
    private BigDecimal totalAmount;
} 