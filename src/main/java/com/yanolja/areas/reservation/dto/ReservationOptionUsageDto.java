package com.yanolja.areas.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 기간별 예약 옵션 사용량 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "기간별 예약 옵션 사용량 DTO")
public class ReservationOptionUsageDto {
    
    @Schema(description = "옵션 ID")
    private Long optionId;
    
    @Schema(description = "총 사용 수량")
    private Long totalQuantity;
    
    @Schema(description = "총 사용 금액")
    private BigDecimal totalAmount;
    
    @Schema(description = "사용 건수")
    private Long usageCount;
} 