package com.yanolja.areas.reservation.dto;

import com.yanolja.areas.reservation.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 사용자별 예약 통계 조회 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자별 예약 통계 DTO")
public class ReservationStatsDto {
    
    @Schema(description = "예약 상태")
    private ReservationStatus status;
    
    @Schema(description = "해당 상태의 예약 건수")
    private Long count;
    
    @Schema(description = "해당 상태의 총 금액")
    private BigDecimal totalAmount;
} 