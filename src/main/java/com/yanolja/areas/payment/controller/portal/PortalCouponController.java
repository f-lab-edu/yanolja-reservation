package com.yanolja.areas.payment.controller.portal;

import com.yanolja.areas.payment.dto.CouponDto;
import com.yanolja.areas.payment.service.CouponService;
import com.yanolja.common.response.ApiResponse;
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
@RequestMapping("/api/portal/coupons")
@RequiredArgsConstructor
@Tag(name = "포털 쿠폰", description = "포털 사용자 쿠폰 관리 API")
public class PortalCouponController {

    private final CouponService couponService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 쿠폰 목록 조회", description = "사용자가 보유한 쿠폰 목록을 조회합니다.")
    public ApiResponse<Page<CouponDto.UserCouponResponse>> getUserCoupons(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("사용자 쿠폰 목록 조회 요청 - userId: {}", userId);
        
        Page<CouponDto.UserCouponResponse> coupons = couponService.getUserCouponsDto(userId, pageable);
        
        return ApiResponse.success(coupons);
    }

    @GetMapping("/user/{userId}/usable")
    @Operation(summary = "사용 가능한 쿠폰 조회", description = "특정 금액에 사용 가능한 쿠폰을 조회합니다.")
    public ApiResponse<List<CouponDto.UserCouponResponse>> getUsableCoupons(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "주문 금액") @RequestParam BigDecimal orderAmount) {
        log.info("사용 가능한 쿠폰 조회 요청 - userId: {}, orderAmount: {}", userId, orderAmount);
        
        List<CouponDto.UserCouponResponse> coupons = couponService.getUsableCouponsDto(userId, orderAmount);
        
        return ApiResponse.success(coupons);
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