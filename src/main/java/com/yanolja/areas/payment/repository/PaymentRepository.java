package com.yanolja.areas.payment.repository;

import com.yanolja.areas.payment.entity.Payment;
import com.yanolja.areas.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentRepositoryCustom {

    /**
     * 결제 키로 결제 조회
     */
    Optional<Payment> findByPaymentKey(String paymentKey);

    /**
     * 주문별 결제 목록 조회
     */
    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    /**
     * 주문별 성공 결제 조회
     */
    Optional<Payment> findSuccessPaymentByOrderId(Long orderId, PaymentStatus status);

    /**
     * PG사 거래 ID로 결제 조회
     */
    Optional<Payment> findByPgTransactionId(String pgTransactionId);

    /**
     * 특정 상태의 결제 목록 조회
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * 주문별 결제 존재 여부 확인
     */
    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);
} 