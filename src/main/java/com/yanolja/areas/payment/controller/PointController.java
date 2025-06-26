package com.yanolja.areas.payment.controller;

import com.yanolja.areas.payment.dto.PointDto;
import com.yanolja.areas.payment.service.PointService;
import com.yanolja.common.response.ApiResponse;
import com.yanolja.areas.payment.entity.Point;
import com.yanolja.areas.payment.entity.PointTransactionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
@Tag(name = "포인트", description = "포인트 관리 API")
public class PointController {

    private final PointService pointService;

    @PostMapping("/earn")
    @Operation(summary = "포인트 적립", description = "포인트를 적립합니다.")
    public ApiResponse<Point> earnPoints(@Valid @RequestBody PointDto.EarnRequest request) {
        log.info("포인트 적립 요청 - userId: {}, amount: {}", request.getUserId(), request.getAmount());
        
        String description = request.getDescription() != null ? request.getDescription() : "포인트 적립";
        Point response = pointService.earnPoints(request.getUserId(), request.getAmount(), 
                                                request.getOrderId(), description);
        
        return ApiResponse.success(response);
    }

    @PostMapping("/use")
    @Operation(summary = "포인트 사용", description = "포인트를 사용합니다.")
    public ApiResponse<Point> usePoints(@Valid @RequestBody PointDto.UseRequest request) {
        log.info("포인트 사용 요청 - userId: {}, amount: {}", request.getUserId(), request.getAmount());
        
        String description = request.getDescription() != null ? request.getDescription() : "포인트 사용";
        Point response = pointService.usePoints(request.getUserId(), request.getAmount(), 
                                               request.getOrderId(), description);
        
        return ApiResponse.success(response);
    }

    @PostMapping("/refund")
    @Operation(summary = "포인트 환불", description = "포인트를 환불합니다.")
    public ApiResponse<Point> refundPoints(@Valid @RequestBody PointDto.RefundRequest request) {
        log.info("포인트 환불 요청 - userId: {}, amount: {}", request.getUserId(), request.getAmount());
        
        String description = request.getDescription() != null ? request.getDescription() : "포인트 환불";
        Point response = pointService.refundPoints(request.getUserId(), request.getAmount(), 
                                                  request.getOrderId(), description);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/balance/{userId}")
    @Operation(summary = "포인트 잔액 조회", description = "사용자의 현재 포인트 잔액을 조회합니다.")
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
        
        Page<Point> response = pointService.getUserPointHistory(userId, pageable);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/history/{userId}/by-date")
    @Operation(summary = "기간별 포인트 내역 조회", description = "특정 기간의 포인트 내역을 조회합니다.")
    public ApiResponse<List<Point>> getPointHistoryByDateRange(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "거래 타입") @RequestParam PointTransactionType transactionType,
            @Parameter(description = "시작일시") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료일시") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("기간별 포인트 내역 조회 요청 - userId: {}, type: {}, startDate: {}, endDate: {}", 
                userId, transactionType, startDate, endDate);
        
        List<Point> response = pointService.getPointHistoryByDateRange(userId, transactionType, startDate, endDate);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "주문별 포인트 내역 조회", description = "특정 주문의 포인트 내역을 조회합니다.")
    public ApiResponse<List<Point>> getPointsByOrder(
            @Parameter(description = "주문 ID") @PathVariable Long orderId) {
        log.info("주문별 포인트 내역 조회 요청 - orderId: {}", orderId);
        
        List<Point> response = pointService.getPointsByOrder(orderId);
        
        return ApiResponse.success(response);
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
    @Operation(summary = "만료 예정 포인트 조회", description = "사용자의 만료 예정 포인트를 조회합니다.")
    public ApiResponse<List<Point>> getExpiringPoints(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "시작일시") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "종료일시") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        log.info("만료 예정 포인트 조회 요청 - userId: {}, startTime: {}, endTime: {}", userId, startTime, endTime);
        
        List<Point> response = pointService.getExpiringPoints(userId, startTime, endTime);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/expiry-notifications")
    @Operation(summary = "포인트 만료 알림 조회", description = "만료 예정 포인트 알림 정보를 조회합니다.")
    public ApiResponse<List<PointDto.PointExpiryNotification>> getPointExpiryNotifications(
            @Parameter(description = "며칠 전", example = "7") @RequestParam(defaultValue = "7") int daysBefore) {
        log.info("포인트 만료 알림 조회 요청 - daysBefore: {}", daysBefore);
        
        List<PointDto.PointExpiryNotification> response = pointService.getPointExpiryNotifications(daysBefore);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/statistics/{userId}")
    @Operation(summary = "포인트 통계 조회", description = "사용자의 포인트 통계를 조회합니다.")
    public ApiResponse<PointDto.PointStatistics> getPointStatistics(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        log.info("포인트 통계 조회 요청 - userId: {}", userId);
        
        PointDto.PointStatistics response = pointService.getPointStatistics(userId);
        
        return ApiResponse.success(response);
    }

    @PostMapping("/expire")
    @Operation(summary = "만료된 포인트 처리", description = "만료된 포인트들을 일괄 처리합니다.")
    public ApiResponse<Void> expirePoints() {
        log.info("만료된 포인트 처리 요청");
        
        pointService.expirePoints();
        
        return ApiResponse.success();
    }
} 