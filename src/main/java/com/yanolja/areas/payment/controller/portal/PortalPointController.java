package com.yanolja.areas.payment.controller.portal;

import com.yanolja.areas.payment.dto.PointDto;
import com.yanolja.areas.payment.entity.Point;
import com.yanolja.areas.payment.entity.PointTransactionType;
import com.yanolja.areas.payment.service.PointService;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/portal/points")
@RequiredArgsConstructor
@Tag(name = "포털 포인트", description = "포털 사용자 포인트 관리 API")
public class PortalPointController {

    private final PointService pointService;

    @GetMapping("/balance/{userId}")
    @Operation(summary = "현재 포인트 잔액 조회", description = "사용자의 현재 포인트 잔액을 조회합니다.")
    public ApiResponse<Integer> getCurrentBalance(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        log.info("포인트 잔액 조회 요청 - userId: {}", userId);
        
        Integer balance = pointService.getCurrentBalance(userId);
        
        return ApiResponse.success(balance);
    }

    @GetMapping("/history/{userId}")
    @Operation(summary = "포인트 내역 조회", description = "사용자의 포인트 내역을 조회합니다.")
    public ApiResponse<Page<Point>> getPointHistory(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("포인트 내역 조회 요청 - userId: {}", userId);
        
        Page<Point> points = pointService.getUserPointHistory(userId, pageable);
        
        return ApiResponse.success(points);
    }

    @GetMapping("/history/{userId}/range")
    @Operation(summary = "기간별 포인트 내역 조회", description = "특정 기간의 포인트 내역을 조회합니다.")
    public ApiResponse<List<Point>> getPointHistoryByDateRange(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "거래 타입") @RequestParam(required = false) PointTransactionType transactionType,
            @Parameter(description = "시작일") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startDate,
            @Parameter(description = "종료일") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endDate) {
        log.info("기간별 포인트 내역 조회 요청 - userId: {}, type: {}, startDate: {}, endDate: {}", 
                userId, transactionType, startDate, endDate);
        
        List<Point> points = pointService.getPointHistoryByDateRange(userId, transactionType, startDate, endDate);
        
        return ApiResponse.success(points);
    }

    @GetMapping("/total-earned/{userId}")
    @Operation(summary = "총 적립 포인트 조회", description = "사용자의 총 적립 포인트를 조회합니다.")
    public ApiResponse<Integer> getTotalEarnedPoints(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        log.info("총 적립 포인트 조회 요청 - userId: {}", userId);
        
        Integer totalEarned = pointService.getTotalEarnedPoints(userId);
        
        return ApiResponse.success(totalEarned);
    }

    @GetMapping("/expiring/{userId}")
    @Operation(summary = "만료 예정 포인트 조회", description = "만료 예정인 포인트를 조회합니다.")
    public ApiResponse<List<Point>> getExpiringPoints(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "시작일") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "종료일") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime endTime) {
        log.info("만료 예정 포인트 조회 요청 - userId: {}, startTime: {}, endTime: {}", userId, startTime, endTime);
        
        List<Point> points = pointService.getExpiringPoints(userId, startTime, endTime);
        
        return ApiResponse.success(points);
    }

    @GetMapping("/statistics/{userId}")
    @Operation(summary = "포인트 통계 조회", description = "사용자의 포인트 통계를 조회합니다.")
    public ApiResponse<PointDto.PointStatistics> getPointStatistics(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        log.info("포인트 통계 조회 요청 - userId: {}", userId);
        
        PointDto.PointStatistics statistics = pointService.getPointStatistics(userId);
        
        return ApiResponse.success(statistics);
    }
} 