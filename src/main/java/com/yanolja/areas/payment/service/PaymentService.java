package com.yanolja.areas.payment.service;

import com.yanolja.areas.payment.dto.PaymentDto;
import com.yanolja.areas.payment.entity.*;
import com.yanolja.areas.payment.repository.OrderRepository;
import com.yanolja.areas.payment.repository.PaymentRepository;
import com.yanolja.areas.reservation.service.ReservationService;
import com.yanolja.common.service.DistributedLockService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final ReservationService reservationService;
    private final PgService pgService;
    private final DistributedLockService distributedLockService;

    /**
     * 결제 처리 (동시성 안전)
     */
    @Transactional
    public PaymentDto.Response processPayment(PaymentDto.Request request) {
        log.info("결제 처리 시작 - orderNumber: {}, paymentMethod: {}", 
                request.getOrderNumber(), request.getPaymentMethod());

        String lockKey = "payment:process:" + request.getOrderNumber();
        String lockValue = distributedLockService.tryLock(lockKey, 10, 60);
        
        if (lockValue == null) {
            throw new IllegalStateException("결제 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            // 1. 주문 조회 및 검증
            Order order = findOrderByNumber(request.getOrderNumber());
            validateOrderForPayment(order);

            // 2. 결제 키 생성
            String paymentKey = generatePaymentKey();

            // 3. 결제 엔티티 생성
            Payment payment = Payment.createPayment(
                paymentKey,
                order,
                request.getPaymentMethod(),
                order.getFinalAmount()
            );

            // 4. 카드 결제인 경우 카드 정보 설정
            if (request.getPaymentMethod() == PaymentMethod.CARD && request.getCardInfo() != null) {
                PaymentDto.Request.CardInfo cardInfo = request.getCardInfo();
                payment.setCardInfo(
                    maskCardNumber(cardInfo.getCardNumber()),
                    cardInfo.getCardType(),
                    cardInfo.getInstallmentMonths()
                );
            }

            payment = paymentRepository.save(payment);

            try {
                // 5. PG사 결제 요청 (실제 구현에서는 외부 API 호출)
                PaymentResult pgResult = requestPaymentToPG(payment, request);

                if (pgResult.isSuccess()) {
                    // 결제 성공 처리 (동시성 안전)
                    boolean success = payment.tryApprovePayment(
                        pgResult.getPgTransactionId(),
                        pgResult.getApprovalNumber(),
                        pgResult.getReceiptUrl()
                    );

                    if (success) {
                        // 주문 확정
                        orderService.confirmOrder(order.getOrderNumber());
                        
                        log.info("결제 성공 - paymentKey: {}, pgTransactionId: {}", 
                                paymentKey, pgResult.getPgTransactionId());
                    } else {
                        log.warn("결제 승인 처리 실패 - paymentKey: {}, status: {}", 
                                paymentKey, payment.getStatus());
                    }
                } else {
                    // 결제 실패 처리 (동시성 안전)
                    payment.tryFailPayment(pgResult.getFailureReason());
                    log.warn("결제 실패 - paymentKey: {}, reason: {}", paymentKey, pgResult.getFailureReason());
                }

            } catch (Exception e) {
                // 결제 처리 중 예외 발생
                payment.tryFailPayment("결제 처리 중 오류 발생: " + e.getMessage());
                log.error("결제 처리 예외 - paymentKey: {}", paymentKey, e);
            }

            paymentRepository.save(payment);
            return PaymentDto.Response.fromEntity(payment);
            
        } finally {
            distributedLockService.unlock(lockKey, lockValue);
        }
    }

    /**
     * 결제 승인 (PG사 콜백) - 동시성 안전
     */
    @Transactional
    public PaymentDto.Response approvePayment(PaymentDto.ApproveRequest request) {
        log.info("결제 승인 처리 - paymentKey: {}", request.getPaymentKey());

        Payment payment = findPaymentByKey(request.getPaymentKey());
        
        // 결제 승인 처리 (동시성 안전)
        boolean success = payment.tryApprovePayment(
            request.getPgTransactionId(),
            request.getApprovalNumber(),
            request.getReceiptUrl()
        );

        if (success) {
            // 주문 확정
            orderService.confirmOrder(payment.getOrder().getOrderNumber());
            log.info("결제 승인 완료 - paymentKey: {}", request.getPaymentKey());
        } else {
            log.warn("결제 승인 실패 - paymentKey: {}, status: {}", 
                    request.getPaymentKey(), payment.getStatus());
        }

        paymentRepository.save(payment);
        return PaymentDto.Response.fromEntity(payment);
    }

    /**
     * 결제 취소 (동시성 안전)
     */
    @Transactional
    public PaymentDto.Response cancelPayment(String paymentKey, String reason) {
        log.info("결제 취소 시작 - paymentKey: {}, reason: {}", paymentKey, reason);

        String lockKey = "payment:cancel:" + paymentKey;
        String lockValue = distributedLockService.tryLock(lockKey, 5, 30);
        
        if (lockValue == null) {
            throw new IllegalStateException("결제 취소 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            Payment payment = findPaymentByKey(paymentKey);
            
            boolean success = payment.tryCancelPayment();
            if (!success) {
                throw new IllegalStateException("취소할 수 없는 결제 상태입니다: " + payment.getStatus());
            }

            // 결제 상태 변경은 먼저 저장
            paymentRepository.save(payment);

            try {
                // PG사 취소 요청
                boolean cancelSuccess = requestCancelToPG(payment, reason);
                
                if (cancelSuccess) {
                    // 주문 취소
                    orderService.cancelOrder(payment.getOrder().getOrderNumber(), reason);
                    
                    // 예약 취소 (주문의 reservationId를 사용)
                    reservationService.cancelReservationsByOrder(payment.getOrder().getReservationId(), reason);
                    
                    log.info("결제 및 주문, 예약 취소 완료 - paymentKey: {}", paymentKey);
                } else {
                    log.warn("PG사 취소 요청 실패했지만 내부 결제 상태는 취소됨 - paymentKey: {}", paymentKey);
                }

            } catch (Exception e) {
                log.error("결제 취소 중 오류 발생했지만 내부 결제 상태는 취소됨 - paymentKey: {}, error: {}", paymentKey, e.getMessage());
                // PG 취소나 주문/예약 취소 실패해도 결제 상태는 취소됨으로 유지
            }

            return PaymentDto.Response.fromEntity(payment);
            
        } finally {
            distributedLockService.unlock(lockKey, lockValue);
        }
    }

    /**
     * 부분 환불 (동시성 안전)
     */
    @Transactional
    public PaymentDto.Response refundPayment(String paymentKey, BigDecimal refundAmount, String reason) {
        log.info("부분 환불 시작 - paymentKey: {}, refundAmount: {}", paymentKey, refundAmount);

        String lockKey = "payment:refund:" + paymentKey;
        String lockValue = distributedLockService.tryLock(lockKey, 5, 30);
        
        if (lockValue == null) {
            throw new IllegalStateException("환불 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            Payment payment = findPaymentByKey(paymentKey);
            
            boolean success = payment.tryRefundPayment(refundAmount);
            if (!success) {
                throw new IllegalStateException("환불할 수 없는 상태입니다.");
            }

            // 결제 상태 변경 먼저 저장
            paymentRepository.save(payment);

            try {
                // PG사 부분 환불 요청
                boolean refundSuccess = requestRefundToPG(payment, refundAmount, reason);
                
                if (refundSuccess) {
                    log.info("PG사 환불 요청 성공 - paymentKey: {}, refundAmount: {}", paymentKey, refundAmount);
                } else {
                    log.warn("PG사 환불 요청 실패했지만 내부 결제 상태는 환불됨 - paymentKey: {}", paymentKey);
                }

                // 전액 환불인 경우 주문 및 예약 취소 처리
                if (payment.getStatus() == com.yanolja.areas.payment.entity.PaymentStatus.REFUNDED) {
                    log.info("전액 환불 - 주문 및 예약 취소 처리 시작 - paymentKey: {}", paymentKey);
                    
                    try {
                        // 주문 취소
                        orderService.cancelOrder(payment.getOrder().getOrderNumber(), "전액 환불: " + reason);
                        
                        // 예약 취소
                        reservationService.cancelReservationsByOrder(payment.getOrder().getReservationId(), "전액 환불: " + reason);
                        
                        log.info("전액 환불로 인한 주문 및 예약 취소 완료 - paymentKey: {}", paymentKey);
                    } catch (Exception e) {
                        log.error("전액 환불 시 주문/예약 취소 중 오류 발생 - paymentKey: {}, error: {}", paymentKey, e.getMessage());
                    }
                }

            } catch (Exception e) {
                log.error("환불 처리 중 오류 발생했지만 내부 결제 상태는 환불됨 - paymentKey: {}, error: {}", paymentKey, e.getMessage());
                // PG 환불 실패해도 결제 상태는 환불됨으로 유지
            }

            return PaymentDto.Response.fromEntity(payment);
            
        } finally {
            distributedLockService.unlock(lockKey, lockValue);
        }
    }

    /**
     * 결제 상세 조회
     */
    @Transactional(readOnly = true)
    public PaymentDto.Response getPayment(String paymentKey) {
        Payment payment = findPaymentByKey(paymentKey);
        return PaymentDto.Response.fromEntity(payment);
    }

    /**
     * 주문별 결제 조회
     */
    @Transactional(readOnly = true)
    public PaymentDto.Response getPaymentByOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다: " + orderNumber));
        
        List<Payment> payments = paymentRepository.findByOrder_IdOrderByCreatedAtDesc(order.getId());
        
        if (payments.isEmpty()) {
            throw new EntityNotFoundException("결제 정보를 찾을 수 없습니다: " + orderNumber);
        }
        
        // 가장 최근 결제 반환
        Payment payment = payments.get(0);
        return PaymentDto.Response.fromEntity(payment);
    }

    /**
     * 사용자별 결제 내역 조회
     */
    @Transactional(readOnly = true)
    public Page<PaymentDto.ListResponse> getUserPayments(Long userId, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findPaymentsByUserId(userId, pageable);
        
        return payments.map(payment -> PaymentDto.ListResponse.builder()
                .paymentKey(payment.getPaymentKey())
                .orderNumber(payment.getOrder().getOrderNumber())
                .paymentMethod(payment.getPaymentMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .refundedAmount(payment.getRefundedAmount())
                .build());
    }

    /**
     * 실패한 결제 재시도
     */
    @Transactional
    public void retryFailedPayments() {
        List<Payment> failedPayments = paymentRepository.findFailedPaymentsForRetry(
            LocalDateTime.now().minusMinutes(10)
        );

        for (Payment payment : failedPayments) {
            try {
                log.info("결제 재시도 - paymentKey: {}", payment.getPaymentKey());
                
                // 재시도 로직 (간소화)
                PaymentResult retryResult = retryPaymentToPG(payment);
                
                if (retryResult.isSuccess()) {
                    payment.tryApprovePayment(
                        retryResult.getPgTransactionId(),
                        retryResult.getApprovalNumber(),
                        retryResult.getReceiptUrl()
                    );
                    
                    orderService.confirmOrder(payment.getOrder().getOrderNumber());
                    log.info("결제 재시도 성공 - paymentKey: {}", payment.getPaymentKey());
                }
                
                paymentRepository.save(payment);
                
            } catch (Exception e) {
                log.error("결제 재시도 실패 - paymentKey: {}", payment.getPaymentKey(), e);
            }
        }
    }

    // === PG사 연동 메서드들 (Mock 구현) ===
    
    private PaymentResult requestPaymentToPG(Payment payment, PaymentDto.Request request) {
        // 실제 PG사 연동
        PgService.PgResult pgResult = pgService.requestPayment(payment, request);
        
        return PaymentResult.builder()
                .success(pgResult.isSuccess())
                .pgTransactionId(pgResult.getPgTransactionId())
                .approvalNumber(pgResult.getApprovalNumber())
                .receiptUrl(pgResult.getReceiptUrl())
                .failureReason(pgResult.getFailureReason())
                .build();
    }

    private boolean requestCancelToPG(Payment payment, String reason) {
        // 실제 PG사 연동
        return pgService.cancelTossPayment(payment, reason);
    }

    private boolean requestRefundToPG(Payment payment, BigDecimal refundAmount, String reason) {
        // 실제 PG사 연동
        return pgService.refundTossPayment(payment, refundAmount, reason);
    }

    private PaymentResult retryPaymentToPG(Payment payment) {
        log.info("PG사 결제 재시도 - paymentKey: {}", payment.getPaymentKey());
        
        return PaymentResult.builder()
            .success(true)
            .pgTransactionId("PG_RETRY_" + System.currentTimeMillis())
            .approvalNumber("APPR_RETRY_" + System.currentTimeMillis())
            .receiptUrl("https://receipt.example.com/retry/" + payment.getPaymentKey())
            .build();
    }

    // === PG사 웹훅 처리 ===

    /**
     * PG사 웹훅 처리
     * PG사에서 결제 상태 변경 시 자동으로 호출됩니다.
     */
    @Transactional
    public void handleWebhook(String webhookData) {
        try {
            log.info("웹훅 데이터 수신: {}", webhookData);
            
            // 1. 웹훅 데이터 파싱
            WebhookData parsedData = parseWebhookData(webhookData);
            
            // 2. 웹훅 서명 검증 (보안)
            if (!verifyWebhookSignature(webhookData, parsedData.getSignature())) {
                log.error("웹훅 서명 검증 실패");
                throw new SecurityException("웹훅 서명이 유효하지 않습니다.");
            }
            
            // 3. 결제 정보 조회
            Payment payment = paymentRepository.findByPaymentKey(parsedData.getPaymentKey())
                    .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다: " + parsedData.getPaymentKey()));
            
            // 4. 웹훅 이벤트 타입에 따른 처리
            switch (parsedData.getEventType()) {
                case PAYMENT_APPROVED:
                    handlePaymentApprovedWebhook(payment, parsedData);
                    break;
                case PAYMENT_FAILED:
                    handlePaymentFailedWebhook(payment, parsedData);
                    break;
                case PAYMENT_CANCELLED:
                    handlePaymentCancelledWebhook(payment, parsedData);
                    break;
                case PAYMENT_REFUNDED:
                    handlePaymentRefundedWebhook(payment, parsedData);
                    break;
                default:
                    log.warn("알 수 없는 웹훅 이벤트 타입: {}", parsedData.getEventType());
            }
            
            log.info("웹훅 처리 완료 - paymentKey: {}, eventType: {}", 
                    parsedData.getPaymentKey(), parsedData.getEventType());
            
        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생: {}", webhookData, e);
            throw new RuntimeException("웹훅 처리 실패", e);
        }
    }

    /**
     * 결제 승인 웹훅 처리
     */
    private void handlePaymentApprovedWebhook(Payment payment, WebhookData webhookData) {
        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.warn("이미 처리된 결제입니다 - paymentKey: {}, status: {}", 
                    payment.getPaymentKey(), payment.getStatus());
            return;
        }

        payment.tryApprovePayment(
                webhookData.getPgTransactionId(),
                webhookData.getApprovalNumber(),
                webhookData.getReceiptUrl()
        );

        // 주문 확정 처리
        orderService.confirmOrder(payment.getOrder().getOrderNumber());
        
        log.info("웹훅으로 결제 승인 처리 완료 - paymentKey: {}", payment.getPaymentKey());
    }

    /**
     * 결제 실패 웹훅 처리
     */
    private void handlePaymentFailedWebhook(Payment payment, WebhookData webhookData) {
        payment.tryFailPayment(webhookData.getFailureReason());
        
        try {
            // 주문 취소 처리
            orderService.cancelOrder(payment.getOrder().getOrderNumber(), "결제 실패 (웹훅)");
            
            // 예약 취소 처리
            reservationService.cancelReservationsByOrder(payment.getOrder().getReservationId(), "결제 실패 (웹훅)");
            
            log.info("웹훅으로 결제 실패 및 주문/예약 취소 완료 - paymentKey: {}", payment.getPaymentKey());
        } catch (Exception e) {
            log.error("웹훅 실패 처리 시 주문/예약 취소 실패 - paymentKey: {}, error: {}", payment.getPaymentKey(), e.getMessage());
        }
    }

    /**
     * 결제 취소 웹훅 처리
     */
    private void handlePaymentCancelledWebhook(Payment payment, WebhookData webhookData) {
        payment.tryCancelPayment();
        
        try {
            // 주문 취소 및 쿠폰/포인트 복원
            orderService.cancelOrder(payment.getOrder().getOrderNumber(), "결제 취소 (웹훅)");
            
            // 예약 취소
            reservationService.cancelReservationsByOrder(payment.getOrder().getReservationId(), "결제 취소 (웹훅)");
            
            log.info("웹훅으로 결제 취소 및 주문/예약 취소 완료 - paymentKey: {}", payment.getPaymentKey());
        } catch (Exception e) {
            log.error("웹훅 취소 처리 시 주문/예약 취소 실패 - paymentKey: {}, error: {}", payment.getPaymentKey(), e.getMessage());
        }
    }

    /**
     * 결제 환불 웹훅 처리
     */
    private void handlePaymentRefundedWebhook(Payment payment, WebhookData webhookData) {
        BigDecimal refundAmount = webhookData.getRefundAmount();
        payment.tryRefundPayment(refundAmount);
        
        // 부분 환불이 아닌 전액 환불인 경우 주문 및 예약 취소
        if (payment.getRefundedAmount().compareTo(payment.getAmount()) >= 0) {
            try {
                orderService.cancelOrder(payment.getOrder().getOrderNumber(), "전액 환불 (웹훅)");
                reservationService.cancelReservationsByOrder(payment.getOrder().getReservationId(), "전액 환불 (웹훅)");
                log.info("웹훅으로 전액 환불 - 주문 및 예약 취소 완료 - paymentKey: {}", payment.getPaymentKey());
            } catch (Exception e) {
                log.error("웹훅 환불 처리 시 주문/예약 취소 실패 - paymentKey: {}, error: {}", payment.getPaymentKey(), e.getMessage());
            }
        }
        
        log.info("웹훅으로 환불 처리 완료 - paymentKey: {}, refundAmount: {}", 
                payment.getPaymentKey(), refundAmount);
    }

    /**
     * 웹훅 데이터 파싱
     */
    private WebhookData parseWebhookData(String webhookData) {
        try {
            // 실제 구현에서는 JSON 파싱 또는 Form 데이터 파싱
            // 여기서는 예시로 간단한 파싱
            
            // JSON 예시: {"eventType":"PAYMENT_APPROVED","paymentKey":"PAY_123","pgTransactionId":"PG_456",...}
            // 실제로는 ObjectMapper 등을 사용하여 파싱
            
            return WebhookData.builder()
                    .eventType(WebhookEventType.PAYMENT_APPROVED) // 예시
                    .paymentKey("PAY_" + System.currentTimeMillis()) // 예시
                    .pgTransactionId("PG_" + System.currentTimeMillis()) // 예시
                    .approvalNumber("APPR_" + System.currentTimeMillis()) // 예시
                    .receiptUrl("https://receipt.example.com/webhook") // 예시
                    .signature("webhook_signature") // 예시
                    .build();
            
        } catch (Exception e) {
            log.error("웹훅 데이터 파싱 실패: {}", webhookData, e);
            throw new IllegalArgumentException("웹훅 데이터 형식이 올바르지 않습니다.", e);
        }
    }

    /**
     * 웹훅 서명 검증 (보안)
     */
    private boolean verifyWebhookSignature(String webhookData, String signature) {
        // 실제 PG사 웹훅 서명 검증
        return pgService.verifyWebhookSignature(webhookData, signature);
    }

    // === Inner Classes ===
    
    @lombok.Builder
    @lombok.Getter
    private static class PaymentResult {
        private boolean success;
        private String pgTransactionId;
        private String approvalNumber;
        private String receiptUrl;
        private String failureReason;
    }

    @lombok.Builder
    @lombok.Getter
    private static class WebhookData {
        private WebhookEventType eventType;
        private String paymentKey;
        private String pgTransactionId;
        private String approvalNumber;
        private String receiptUrl;
        private String failureReason;
        private BigDecimal refundAmount;
        private String signature;
    }

    private enum WebhookEventType {
        PAYMENT_APPROVED,    // 결제 승인
        PAYMENT_FAILED,      // 결제 실패
        PAYMENT_CANCELLED,   // 결제 취소
        PAYMENT_REFUNDED     // 결제 환불
    }

    /**
     * 토스페이먼츠 결제 정보 저장
     */
    @Transactional
    public void savePaymentInfo(Map<String, Object> tossResponse, String orderNumber) {
        log.info("결제 정보 저장 시작 - orderNumber: {}", orderNumber);
        
        // 주문 조회
        Order order = findOrderByNumber(orderNumber);
        
        // 이미 결제 정보가 저장되어 있는지 확인
        boolean paymentExists = paymentRepository.existsByOrder_IdAndStatus(order.getId(), PaymentStatus.SUCCESS);
        if (paymentExists) {
            log.info("이미 결제 정보가 저장되어 있음 - orderNumber: {}", orderNumber);
            return;
        }
        
        try {
            // 토스 응답에서 필요한 정보 추출
            String paymentKey = (String) tossResponse.get("paymentKey");
            String pgTransactionId = (String) tossResponse.get("transactionId");
            String approvalNumber = (String) tossResponse.get("approvalNumber");
            String receiptUrl = (String) tossResponse.get("receiptUrl");
            
            // 결제 방법 정보 추출
            PaymentMethod paymentMethod = PaymentMethod.CARD; // 기본값
            String methodStr = (String) tossResponse.get("method");
            if (methodStr != null) {
                switch (methodStr.toUpperCase()) {
                    case "CARD":
                        paymentMethod = PaymentMethod.CARD;
                        break;
                    case "VIRTUAL_ACCOUNT":
                        paymentMethod = PaymentMethod.VIRTUAL_ACCOUNT;
                        break;
                    case "TRANSFER":
                    case "BANK_TRANSFER":
                        paymentMethod = PaymentMethod.BANK_TRANSFER;
                        break;
                    case "PAYCO":
                        paymentMethod = PaymentMethod.PAYCO;
                        break;
                    case "TOSS":
                        paymentMethod = PaymentMethod.TOSS;
                        break;
                    case "POINT":
                        paymentMethod = PaymentMethod.POINT;
                        break;
                    default:
                        paymentMethod = PaymentMethod.CARD;
                }
            }
            
            // Payment 엔티티 생성
            Payment payment = Payment.createPayment(
                paymentKey != null ? paymentKey : "TOSS_" + System.currentTimeMillis(),
                order,
                paymentMethod,
                order.getFinalAmount()
            );
            
            // 결제 승인 처리
            payment.tryApprovePayment(pgTransactionId, approvalNumber, receiptUrl);
            
            // 카드 정보 설정 (있는 경우)
            Object cardInfo = tossResponse.get("card");
            if (cardInfo instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> card = (Map<String, Object>) cardInfo;
                String cardNumber = (String) card.get("number");
                String cardType = (String) card.get("cardType");
                Integer installmentMonths = (Integer) card.get("installmentPlanMonths");
                
                if (cardNumber != null) {
                    payment.setCardInfo(maskCardNumber(cardNumber), cardType, installmentMonths);
                }
            }
            
            // 결제 정보 저장
            paymentRepository.save(payment);
            
            log.info("결제 정보 저장 완료 - orderNumber: {}, paymentKey: {}", orderNumber, paymentKey);
            
        } catch (Exception e) {
            log.error("결제 정보 저장 중 오류 발생 - orderNumber: {}", orderNumber, e);
            throw new RuntimeException("결제 정보 저장에 실패했습니다.", e);
        }
    }

    // === Private Methods ===

    private Order findOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다: " + orderNumber));
    }

    private Payment findPaymentByKey(String paymentKey) {
        return paymentRepository.findByPaymentKey(paymentKey)
            .orElseThrow(() -> new EntityNotFoundException("결제 정보를 찾을 수 없습니다: " + paymentKey));
    }

    private void validateOrderForPayment(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("결제할 수 없는 주문 상태입니다: " + order.getStatus());
        }

        if (order.isExpired()) {
            throw new IllegalStateException("만료된 주문입니다.");
        }

        // 이미 성공한 결제가 있는지 확인
        boolean hasSuccessPayment = paymentRepository.existsByOrder_IdAndStatus(
            order.getId(), PaymentStatus.SUCCESS
        );
        
        if (hasSuccessPayment) {
            throw new IllegalStateException("이미 결제가 완료된 주문입니다.");
        }
    }

    private String generatePaymentKey() {
        return "PAY_" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return cardNumber;
        }
        
        String cleaned = cardNumber.replaceAll("[^0-9]", "");
        if (cleaned.length() >= 8) {
            return cleaned.substring(0, 4) + "****" + cleaned.substring(cleaned.length() - 4);
        }
        
        return cardNumber;
    }
} 