package com.yanolja.areas.payment.controller;

import com.yanolja.areas.payment.dto.CouponDto;
import com.yanolja.areas.payment.service.CouponService;
import com.yanolja.common.response.ApiResponse;
import com.yanolja.areas.payment.entity.Coupon;
import com.yanolja.areas.payment.entity.CouponIssueType;
import com.yanolja.areas.payment.entity.CouponStatus;
import com.yanolja.areas.payment.entity.UserCoupon;
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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "쿠폰", description = "쿠폰 관리 API")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "쿠폰 생성", description = "새로운 쿠폰을 생성합니다.")
    public ApiResponse<Coupon> createCoupon(
            @Valid @RequestBody CouponDto.CreateCouponRequest request) {
        log.info("쿠폰 생성 요청 - code: {}, name: {}", request.getCode(), request.getName());
        
        Coupon response = couponService.createCoupon(request);
        
        return ApiResponse.success(response);
    }

    @PostMapping("/{couponCode}/issue/{userId}")
    @Operation(summary = "쿠폰 발급", description = "사용자에게 쿠폰을 발급합니다.")
    public ApiResponse<UserCoupon> issueCoupon(
            @Parameter(description = "쿠폰 코드") @PathVariable String couponCode,
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        log.info("쿠폰 발급 요청 - couponCode: {}, userId: {}", couponCode, userId);
        
        UserCoupon response = couponService.issueCouponToUser(userId, couponCode);
        
        return ApiResponse.success(response);
    }

    @PostMapping("/auto-issue/{userId}")
    @Operation(summary = "자동 쿠폰 발급", description = "특정 타입의 쿠폰을 자동 발급합니다.")
    public ApiResponse<Void> issueAutomaticCoupons(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "발급 타입") @RequestParam CouponIssueType issueType) {
        log.info("자동 쿠폰 발급 요청 - userId: {}, issueType: {}", userId, issueType);
        
        couponService.issueAutomaticCoupons(userId, issueType);
        
        return ApiResponse.success();
    }

    @GetMapping("/user/{userId}/available")
    @Operation(summary = "사용자 사용 가능 쿠폰 조회", description = "사용자의 사용 가능한 쿠폰 목록을 조회합니다.")
    public ApiResponse<List<UserCoupon>> getUserAvailableCoupons(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        log.info("사용자 사용 가능 쿠폰 조회 요청 - userId: {}", userId);
        
        List<UserCoupon> response = couponService.getUserAvailableCoupons(userId);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/user/{userId}/usable")
    @Operation(summary = "주문 금액에 사용 가능한 쿠폰 조회", description = "특정 주문 금액에 사용 가능한 쿠폰을 조회합니다.")
    public ApiResponse<List<UserCoupon>> getUsableCoupons(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "주문 금액") @RequestParam BigDecimal orderAmount) {
        log.info("사용 가능 쿠폰 조회 요청 - userId: {}, orderAmount: {}", userId, orderAmount);
        
        List<UserCoupon> response = couponService.getUsableCouponsForOrder(userId, orderAmount);
        
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "쿠폰 목록 조회", description = "쿠폰 목록을 조회합니다.")
    public ApiResponse<Page<Coupon>> getAllCoupons(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("쿠폰 목록 조회 요청");
        
        Page<Coupon> response = couponService.getAllCoupons(pageable);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/search")
    @Operation(summary = "쿠폰 검색", description = "키워드로 쿠폰을 검색합니다.")
    public ApiResponse<List<Coupon>> searchCoupons(
            @Parameter(description = "검색 키워드") @RequestParam String keyword) {
        log.info("쿠폰 검색 요청 - keyword: {}", keyword);
        
        List<Coupon> response = couponService.searchCoupons(keyword);
        
        return ApiResponse.success(response);
    }

    @PatchMapping("/{couponId}/status")
    @Operation(summary = "쿠폰 상태 변경", description = "쿠폰의 상태를 변경합니다.")
    public ApiResponse<Void> updateCouponStatus(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId,
            @Parameter(description = "변경할 상태") @RequestParam CouponStatus status) {
        log.info("쿠폰 상태 변경 요청 - couponId: {}, status: {}", couponId, status);
        
        couponService.updateCouponStatus(couponId, status);
        
        return ApiResponse.success();
    }

    @GetMapping("/{couponId}/statistics")
    @Operation(summary = "쿠폰 통계 조회", description = "쿠폰의 발급 통계를 조회합니다.")
    public ApiResponse<CouponDto.CouponStatistics> getCouponStatistics(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId) {
        log.info("쿠폰 통계 조회 요청 - couponId: {}", couponId);
        
        CouponDto.CouponStatistics response = couponService.getCouponStatistics(couponId);
        
        return ApiResponse.success(response);
    }

    @PostMapping("/expire")
    @Operation(summary = "만료된 쿠폰 처리", description = "만료된 쿠폰들을 일괄 처리합니다.")
    public ApiResponse<Void> expireCoupons() {
        log.info("만료된 쿠폰 처리 요청");
        
        couponService.expireCoupons();
        
        return ApiResponse.success();
    }

    @PostMapping("/discount/calculate")
    @Operation(summary = "쿠폰 할인 금액 계산", description = "쿠폰의 할인 금액을 계산합니다.")
    public ApiResponse<CouponDto.DiscountCalculateResponse> calculateDiscount(
            @Valid @RequestBody CouponDto.DiscountCalculateRequest request) {
        log.info("쿠폰 할인 금액 계산 요청 - couponId: {}, orderAmount: {}", 
                request.getCouponId(), request.getOrderAmount());
        
        CouponDto.DiscountCalculateResponse response = couponService.calculateDiscount(
                request.getCouponId(), request.getOrderAmount());
        
        return ApiResponse.success(response);
    }
} 