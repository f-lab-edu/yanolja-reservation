package com.yanolja.areas.reservation.dto;

import com.yanolja.areas.reservation.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 객실별 예약 현황 조회 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "객실별 예약 현황 DTO")
public class ReservationStatusDto {
    
    @Schema(description = "체크인 날짜")
    private LocalDate checkInDate;
    
    @Schema(description = "체크아웃 날짜")
    private LocalDate checkOutDate;
    
    @Schema(description = "예약 상태")
    private ReservationStatus status;
    
    @Schema(description = "해당 조건의 예약 건수")
    private Long count;
} 