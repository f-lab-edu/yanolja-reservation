package com.yanolja.areas.payment.controller;

import com.yanolja.areas.payment.dto.OrderDto;
import com.yanolja.areas.payment.service.OrderService;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "주문", description = "주문 관리 API")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "주문 생성", description = "새로운 주문을 생성합니다.")
    public ApiResponse<OrderDto.Response> createOrder(
            @Valid @RequestBody OrderDto.CreateRequest request) {
        log.info("주문 생성 요청 - userId: {}, reservationId: {}", request.getUserId(), request.getReservationId());
        
        OrderDto.Response response = orderService.createOrder(request);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/{orderNumber}")
    @Operation(summary = "주문 상세 조회", description = "주문 번호로 주문 상세 정보를 조회합니다.")
    public ApiResponse<OrderDto.Response> getOrder(
            @Parameter(description = "주문 번호") @PathVariable String orderNumber) {
        log.info("주문 상세 조회 요청 - orderNumber: {}", orderNumber);
        
        OrderDto.Response response = orderService.getOrder(orderNumber);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 주문 목록 조회", description = "사용자의 주문 목록을 조회합니다.")
    public ApiResponse<Page<OrderDto.ListResponse>> getUserOrders(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("사용자 주문 목록 조회 요청 - userId: {}", userId);
        
        Page<OrderDto.ListResponse> response = orderService.getUserOrders(userId, pageable);
        
        return ApiResponse.success(response);
    }

    @PatchMapping("/{orderNumber}/confirm")
    @Operation(summary = "주문 확정", description = "주문을 확정 상태로 변경합니다.")
    public ApiResponse<OrderDto.Response> confirmOrder(
            @Parameter(description = "주문 번호") @PathVariable String orderNumber) {
        log.info("주문 확정 요청 - orderNumber: {}", orderNumber);
        
        orderService.confirmOrder(orderNumber);
        OrderDto.Response response = orderService.getOrder(orderNumber);
        
        return ApiResponse.success(response);
    }

    @PatchMapping("/{orderNumber}/cancel")
    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    public ApiResponse<OrderDto.Response> cancelOrder(
            @Parameter(description = "주문 번호") @PathVariable String orderNumber,
            @Parameter(description = "취소 사유") @RequestParam(required = false) String reason) {
        log.info("주문 취소 요청 - orderNumber: {}, reason: {}", orderNumber, reason);
        
        orderService.cancelOrder(orderNumber, reason);
        OrderDto.Response response = orderService.getOrder(orderNumber);
        
        return ApiResponse.success(response);
    }

    @PatchMapping("/{orderNumber}/complete")
    @Operation(summary = "주문 완료", description = "주문을 완료 처리합니다.")
    public ApiResponse<OrderDto.Response> completeOrder(
            @Parameter(description = "주문 번호") @PathVariable String orderNumber) {
        log.info("주문 완료 요청 - orderNumber: {}", orderNumber);
        
        orderService.completeOrder(orderNumber);
        OrderDto.Response response = orderService.getOrder(orderNumber);
        
        return ApiResponse.success(response);
    }

    @PostMapping("/expire")
    @Operation(summary = "만료된 주문 처리", description = "만료된 주문들을 일괄 처리합니다.")
    public ApiResponse<Void> expireOrders() {
        log.info("만료된 주문 처리 요청");
        
        orderService.expireOrders();
        
        return ApiResponse.success();
    }
} 