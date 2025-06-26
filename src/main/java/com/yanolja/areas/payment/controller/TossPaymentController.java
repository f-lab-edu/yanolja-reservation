package com.yanolja.areas.payment.controller;

import com.yanolja.areas.payment.service.TossPaymentService;
import com.yanolja.areas.payment.service.OrderService;
import com.yanolja.areas.payment.service.PaymentService;
import com.yanolja.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/toss-payments")
@RequiredArgsConstructor
public class TossPaymentController {

    private final TossPaymentService tossPaymentService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    /**
     * 토스 페이먼츠 결제 승인
     */
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approvePayment(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String paymentKey = (String) requestBody.get("paymentKey");
            String orderId = (String) requestBody.get("orderId");
            Integer amount = (Integer) requestBody.get("amount");

            log.info("토스 페이먼츠 결제 승인 요청 - paymentKey: {}, orderId: {}, amount: {}", 
                    paymentKey, orderId, amount);

            // 1. 토스 페이먼츠 결제 승인
            Map<String, Object> tossResponse = tossPaymentService.approvePayment(
                    paymentKey, orderId, amount.longValue());

            // 2. 주문 상태 업데이트 (예약도 함께 확정됨)
            orderService.confirmOrder(orderId);

            // 3. 결제 정보 저장
            try {
                paymentService.savePaymentInfo(tossResponse, orderId);
                log.info("결제 정보 저장 완료 - orderId: {}", orderId);
            } catch (Exception e) {
                log.error("결제 정보 저장 실패 - orderId: {}, error: {}", orderId, e.getMessage());
                // 결제는 성공했지만 정보 저장에 실패한 경우 - 로그만 남기고 진행
            }

            log.info("토스 페이먼츠 결제 승인 완료 - orderId: {}", orderId);

            return ResponseEntity.ok(ApiResponse.success(tossResponse));

        } catch (Exception e) {
            log.error("토스 페이먼츠 결제 승인 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("E004", "결제 승인에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 토스 페이먼츠 결제 취소
     */
    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            String cancelReason = (String) requestBody.get("cancelReason");
            Integer cancelAmount = (Integer) requestBody.get("cancelAmount");

            log.info("토스 페이먼츠 결제 취소 요청 - paymentKey: {}, reason: {}, amount: {}", 
                    paymentKey, cancelReason, cancelAmount);

            // 1. 토스 페이먼츠 결제 취소
            Map<String, Object> tossResponse = tossPaymentService.cancelPayment(
                    paymentKey, cancelReason, cancelAmount != null ? cancelAmount.longValue() : null);

            // 2. 주문 상태 업데이트 (Optional - 필요에 따라 구현)
            // orderService.cancelOrder(orderId, cancelReason);

            log.info("토스 페이먼츠 결제 취소 완료 - paymentKey: {}", paymentKey);

            return ResponseEntity.ok(ApiResponse.success(tossResponse));

        } catch (Exception e) {
            log.error("토스 페이먼츠 결제 취소 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("E005", "결제 취소에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 토스 페이먼츠 결제 조회
     */
    @GetMapping("/{paymentKey}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPayment(
            @PathVariable String paymentKey) {
        
        try {
            log.info("토스 페이먼츠 결제 조회 요청 - paymentKey: {}", paymentKey);

            Map<String, Object> tossResponse = tossPaymentService.getPayment(paymentKey);

            return ResponseEntity.ok(ApiResponse.success(tossResponse));

        } catch (Exception e) {
            log.error("토스 페이먼츠 결제 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("E006", "결제 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 토스 페이먼츠 웹훅 (결제 상태 변경 알림)
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestBody Map<String, Object> webhookData) {
        try {
            log.info("토스 페이먼츠 웹훅 수신: {}", webhookData);

            // 웹훅 데이터 처리 로직 구현
            // 1. 서명 검증
            // 2. 결제 상태 업데이트
            // 3. 비즈니스 로직 처리

            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("토스 페이먼츠 웹훅 처리 실패", e);
            return ResponseEntity.badRequest().body("FAIL");
        }
    }
} 