package com.yanolja.areas.reservation.controller;

import com.yanolja.areas.reservation.dto.ReservationDto;
import com.yanolja.areas.reservation.dto.ReservationStatusDto;
import com.yanolja.areas.reservation.service.ReservationService;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 관리자용 예약 관리 REST Controller
 */
@Tag(name = "관리자 예약 관리", description = "관리자용 예약 관리 API")
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 예약 확정 (관리자용)
     */
    @PatchMapping("/{reservationId}/confirm")
    @Operation(summary = "예약 확정", description = "관리자가 예약을 확정합니다.")
    public ApiResponse<ReservationDto.Response> confirmReservation(
            @Parameter(description = "예약 ID") @PathVariable Long reservationId) {
        
        ReservationDto.Response response = reservationService.confirmReservation(reservationId);
        
        return ApiResponse.success(response);
    }

    /**
     * 만료된 예약 정리 (스케줄러용)
     */
    @PostMapping("/cleanup")
    @Operation(summary = "만료된 예약 정리", description = "만료된 PENDING 상태의 예약을 정리합니다.")
    public ApiResponse<Void> cleanupExpiredReservations() {
        
        reservationService.cleanupExpiredReservations();
        
        return ApiResponse.success();
    }

    /**
     * 객실별 예약 현황 조회 (관리자용)
     */
    @GetMapping("/rooms/{roomId}/status")
    @Operation(summary = "객실 예약 현황 조회", description = "관리자가 특정 객실의 예약 현황을 조회합니다.")
    public ApiResponse<List<ReservationStatusDto>> getRoomReservationStatus(
            @Parameter(description = "객실 ID") @PathVariable Long roomId,
            @Parameter(description = "시작 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료 날짜") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<ReservationStatusDto> status = reservationService.getRoomReservationStatus(roomId, startDate, endDate);
        
        return ApiResponse.success(status);
    }
} 