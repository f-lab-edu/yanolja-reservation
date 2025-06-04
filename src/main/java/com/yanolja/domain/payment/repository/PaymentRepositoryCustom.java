package com.yanolja.domain.payment.repository;

import com.yanolja.domain.payment.entity.Payment;
import com.yanolja.domain.payment.entity.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepositoryCustom {

    /**
     * 주문별 성공 결제 조회
     */
    Optional<Payment> findSuccessPaymentByOrderId(Long orderId, PaymentStatus status);

    /**
     * 사용자별 결제 내역 조회 (기간별)
     */
    List<Payment> findPaymentsByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 결제 수단별 통계
     */
    Long countPaymentsByMethodAndStatus(String paymentMethod, PaymentStatus status);

    /**
     * 실패한 결제 목록 조회 (재시도 대상)
     */
    List<Payment> findFailedPaymentsForRetry(LocalDateTime beforeTime);
} 