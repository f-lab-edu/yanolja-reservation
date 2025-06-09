package com.yanolja.areas.payment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.payment.entity.Payment;
import com.yanolja.areas.payment.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.yanolja.domain.payment.entity.QPayment.payment;
import static com.yanolja.domain.payment.entity.QOrder.order;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Payment> findSuccessPaymentByOrderId(Long orderId, PaymentStatus status) {
        Payment result = queryFactory
                .selectFrom(payment)
                .where(
                        payment.order.id.eq(orderId),
                        payment.status.eq(status)
                )
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public List<Payment> findPaymentsByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
                .selectFrom(payment)
                .join(payment.order, order)
                .where(
                        order.userId.eq(userId),
                        payment.createdAt.between(startDate, endDate)
                )
                .orderBy(payment.createdAt.desc())
                .fetch();
    }

    @Override
    public Long countPaymentsByMethodAndStatus(String paymentMethod, PaymentStatus status) {
        return queryFactory
                .select(payment.count())
                .from(payment)
                .where(
                        payment.paymentMethod.stringValue().eq(paymentMethod),
                        payment.status.eq(status)
                )
                .fetchOne();
    }

    @Override
    public List<Payment> findFailedPaymentsForRetry(LocalDateTime beforeTime) {
        return queryFactory
                .selectFrom(payment)
                .where(
                        payment.status.eq(PaymentStatus.FAILED),
                        payment.createdAt.before(beforeTime)
                )
                .orderBy(payment.createdAt.asc())
                .fetch();
    }
} 