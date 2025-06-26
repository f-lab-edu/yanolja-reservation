package com.yanolja.areas.reservation.controller;

import com.yanolja.areas.reservation.dto.ReservationDto;
import com.yanolja.areas.reservation.dto.ReservationStatsDto;
import com.yanolja.areas.reservation.entity.ReservationStatus;
import com.yanolja.areas.reservation.service.ReservationService;
import com.yanolja.areas.user.domain.UserDetail;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 포털 사용자용 예약 관리 REST Controller
 */
@Tag(name = "포털 예약 관리", description = "포털 사용자용 예약 관리 API")
@RestController
@RequestMapping("/api/portal/reservations")
@RequiredArgsConstructor
public class PortalReservationController {

    private final ReservationService reservationService;

    /**
     * 예약 생성
     */
    @PostMapping
    @Operation(summary = "예약 생성", description = "새로운 예약을 생성합니다.")
    public ApiResponse<ReservationDto.Response> createReservation(
            @AuthenticationPrincipal UserDetail userDetail,
            @Valid @RequestBody ReservationDto.Request request) {
        
        ReservationDto.Response response = reservationService.createReservation(
                userDetail.getUser().getId(), request);
        
        return ApiResponse.success(response);
    }

    /**
     * 예약 상세 조회
     */
    @GetMapping("/{reservationId}")
    @Operation(summary = "예약 상세 조회", description = "예약 상세 정보를 조회합니다.")
    public ApiResponse<ReservationDto.Response> getReservation(
            @AuthenticationPrincipal UserDetail userDetail,
            @Parameter(description = "예약 ID") @PathVariable Long reservationId) {
        
        ReservationDto.Response response = reservationService.getReservation(
                reservationId, userDetail.getUser().getId());
        
        return ApiResponse.success(response);
    }

    /**
     * 사용자별 예약 목록 조회
     */
    @GetMapping
    @Operation(summary = "예약 목록 조회", description = "사용자의 예약 목록을 조회합니다.")
    public ApiResponse<Page<ReservationDto.ListResponse>> getUserReservations(
            @AuthenticationPrincipal UserDetail userDetail,
            @Parameter(description = "예약 상태") @RequestParam(required = false) ReservationStatus status,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReservationDto.ListResponse> response = reservationService.getUserReservations(
                userDetail.getUser().getId(), status, pageable);
        
        return ApiResponse.success(response);
    }

    /**
     * 고급 검색을 통한 예약 목록 조회
     */
    @PostMapping("/search")
    @Operation(summary = "예약 고급 검색", description = "복잡한 조건으로 예약을 검색합니다.")
    public ApiResponse<Page<ReservationDto.ListResponse>> searchReservations(
            @AuthenticationPrincipal UserDetail userDetail,
            @Valid @RequestBody ReservationDto.SearchCondition condition,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        
        // 사용자 ID 설정 (보안을 위해 현재 사용자만 조회 가능)
        condition = ReservationDto.SearchCondition.builder()
                .userId(userDetail.getUser().getId())
                .statuses(condition.getStatuses())
                .checkInDateFrom(condition.getCheckInDateFrom())
                .checkInDateTo(condition.getCheckInDateTo())
                .checkOutDateFrom(condition.getCheckOutDateFrom())
                .checkOutDateTo(condition.getCheckOutDateTo())
                .build();
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ReservationDto.ListResponse> response = reservationService.searchReservations(condition, pageable);
        
        return ApiResponse.success(response);
    }

    /**
     * 예약 상태 변경
     */
    @PatchMapping("/{reservationId}/status")
    @Operation(summary = "예약 상태 변경", description = "예약 상태를 변경합니다.")
    public ApiResponse<ReservationDto.Response> updateReservationStatus(
            @AuthenticationPrincipal UserDetail userDetail,
            @Parameter(description = "예약 ID") @PathVariable Long reservationId,
            @Valid @RequestBody ReservationDto.StatusUpdateRequest request) {
        
        ReservationDto.Response response = reservationService.updateReservationStatus(
                reservationId, userDetail.getUser().getId(), request);
        
        return ApiResponse.success(response);
    }

    /**
     * 예약 취소
     */
    @PatchMapping("/{reservationId}/cancel")
    @Operation(summary = "예약 취소", description = "예약을 취소합니다.")
    public ApiResponse<ReservationDto.Response> cancelReservation(
            @AuthenticationPrincipal UserDetail userDetail,
            @Parameter(description = "예약 ID") @PathVariable Long reservationId) {
        
        ReservationDto.Response response = reservationService.cancelReservation(
                reservationId, userDetail.getUser().getId());
        
        return ApiResponse.success(response);
    }

    /**
     * 사용자별 예약 통계 조회
     */
    @GetMapping("/stats")
    @Operation(summary = "예약 통계 조회", description = "사용자의 예약 통계를 조회합니다.")
    public ApiResponse<List<ReservationStatsDto>> getUserReservationStats(
            @AuthenticationPrincipal UserDetail userDetail) {
        
        List<ReservationStatsDto> stats = reservationService.getUserReservationStats(userDetail.getUser().getId());
        
        return ApiResponse.success(stats);
    }
} 