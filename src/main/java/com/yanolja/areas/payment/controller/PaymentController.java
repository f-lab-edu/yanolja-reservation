package com.yanolja.areas.payment.controller;

import com.yanolja.areas.payment.dto.PaymentDto;
import com.yanolja.areas.payment.service.PaymentService;
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

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "결제", description = "결제 관리 API")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "결제 처리", description = "주문에 대한 결제를 처리합니다.")
    public ApiResponse<PaymentDto.Response> processPayment(
            @Valid @RequestBody PaymentDto.Request request) {
        log.info("결제 처리 요청 - orderNumber: {}, method: {}", request.getOrderNumber(), request.getPaymentMethod());
        
        PaymentDto.Response response = paymentService.processPayment(request);
        
        return ApiResponse.success(response);
    }

    @PostMapping("/approve")
    @Operation(summary = "결제 승인", description = "PG사로부터 결제 승인을 처리합니다.")
    public ApiResponse<PaymentDto.Response> approvePayment(
            @Valid @RequestBody PaymentDto.ApproveRequest request) {
        log.info("결제 승인 요청 - paymentKey: {}", request.getPaymentKey());
        
        PaymentDto.Response response = paymentService.approvePayment(request);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/{paymentKey}")
    @Operation(summary = "결제 상세 조회", description = "결제 키로 결제 상세 정보를 조회합니다.")
    public ApiResponse<PaymentDto.Response> getPayment(
            @Parameter(description = "결제 키") @PathVariable String paymentKey) {
        log.info("결제 상세 조회 요청 - paymentKey: {}", paymentKey);
        
        PaymentDto.Response response = paymentService.getPayment(paymentKey);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/order/{orderNumber}")
    @Operation(summary = "주문별 결제 조회", description = "주문 번호로 결제 정보를 조회합니다.")
    public ApiResponse<PaymentDto.Response> getPaymentByOrder(
            @Parameter(description = "주문 번호") @PathVariable String orderNumber) {
        log.info("주문별 결제 조회 요청 - orderNumber: {}", orderNumber);
        
        PaymentDto.Response response = paymentService.getPaymentByOrder(orderNumber);
        
        return ApiResponse.success(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 결제 내역 조회", description = "사용자의 결제 내역을 조회합니다.")
    public ApiResponse<Page<PaymentDto.ListResponse>> getUserPayments(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("사용자 결제 내역 조회 요청 - userId: {}", userId);
        
        Page<PaymentDto.ListResponse> response = paymentService.getUserPayments(userId, pageable);
        
        return ApiResponse.success(response);
    }

    @PatchMapping("/{paymentKey}/cancel")
    @Operation(summary = "결제 취소", description = "결제를 취소합니다.")
    public ApiResponse<PaymentDto.Response> cancelPayment(
            @Parameter(description = "결제 키") @PathVariable String paymentKey,
            @Parameter(description = "취소 사유") @RequestParam(required = false) String reason) {
        log.info("결제 취소 요청 - paymentKey: {}, reason: {}", paymentKey, reason);
        
        PaymentDto.Response response = paymentService.cancelPayment(paymentKey, reason);
        
        return ApiResponse.success(response);
    }

    @PatchMapping("/{paymentKey}/refund")
    @Operation(summary = "결제 환불", description = "결제 금액을 환불합니다.")
    public ApiResponse<PaymentDto.Response> refundPayment(
            @Parameter(description = "결제 키") @PathVariable String paymentKey,
            @Parameter(description = "환불 금액") @RequestParam BigDecimal refundAmount,
            @Parameter(description = "환불 사유") @RequestParam(required = false) String reason) {
        log.info("결제 환불 요청 - paymentKey: {}, refundAmount: {}, reason: {}", 
                paymentKey, refundAmount, reason);
        
        PaymentDto.Response response = paymentService.refundPayment(paymentKey, refundAmount, reason);
        
        return ApiResponse.success(response);
    }

    @PostMapping("/retry-failed")
    @Operation(summary = "실패한 결제 재시도", description = "실패한 결제들을 재시도합니다.")
    public ApiResponse<Void> retryFailedPayments() {
        log.info("실패한 결제 재시도 요청");
        
        paymentService.retryFailedPayments();
        
        return ApiResponse.success();
    }

    @PostMapping("/webhook")
    @Operation(summary = "결제 웹훅", description = "PG사 웹훅을 처리합니다.")
    public ApiResponse<Void> handleWebhook(@RequestBody String webhookData) {
        log.info("결제 웹훅 수신");
        
        try {
            paymentService.handleWebhook(webhookData);
            log.info("웹훅 처리 성공");
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("웹훅 처리 실패", e);
            // 웹훅 실패 시에도 200 OK 반환 (PG사 재전송 방지)
            return ApiResponse.success();
        }
    }
} 