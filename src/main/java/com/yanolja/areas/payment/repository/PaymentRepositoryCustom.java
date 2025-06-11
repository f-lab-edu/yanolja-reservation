package com.yanolja.areas.payment.repository;

import com.yanolja.areas.payment.dto.PaymentStatisticsDto;
import com.yanolja.areas.payment.entity.Payment;
import com.yanolja.areas.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepositoryCustom {


    /**
     * 실패한 결제 목록 조회 (재시도 대상)
     */
    List<Payment> findFailedPaymentsForRetry(LocalDateTime beforeTime);

    /**
     * 사용자별 결제 내역 조회 (페이징)
     */
    Page<Payment> findPaymentsByUserId(Long userId, Pageable pageable);

    /**
     * 결제 상태별 통계
     */
    List<PaymentStatisticsDto.PaymentStatusStats> findPaymentStatsByStatus(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 시스템 상태 체크용 대기 중인 주문 수
     */
    Long countPendingPayments();

    /**
     * 시스템 상태 체크용 실패한 결제 수
     */
    Long countFailedPaymentsInLastHour();

    /**
     * 결제 키로 결제 조회
     */
    Optional<Payment> findByPaymentKey(String paymentKey);
} 