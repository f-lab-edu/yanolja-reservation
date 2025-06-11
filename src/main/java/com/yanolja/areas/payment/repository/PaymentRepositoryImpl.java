package com.yanolja.areas.payment.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.payment.dto.PaymentStatisticsDto;
import com.yanolja.areas.payment.entity.Payment;
import com.yanolja.areas.payment.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.yanolja.areas.payment.entity.QPayment.payment;
import static com.yanolja.areas.payment.entity.QOrder.order;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Payment> findPaymentsByUserId(Long userId, Pageable pageable) {
        JPAQuery<Payment> query = queryFactory
                .selectFrom(payment)
                .join(payment.order, order)
                .where(order.userId.eq(userId))
                .orderBy(payment.createdAt.desc());

        // 총 개수 조회
        long total = query.fetchCount();

        // 페이징 적용
        List<Payment> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<PaymentStatisticsDto.PaymentStatusStats> findPaymentStatsByStatus(LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
                .select(
                        payment.status,
                        payment.count(),
                        payment.amount.sum()
                )
                .from(payment)
                .where(payment.createdAt.between(startDate, endDate))
                .groupBy(payment.status)
                .fetch()
                .stream()
                .map(tuple -> PaymentStatisticsDto.PaymentStatusStats.builder()
                        .status(tuple.get(payment.status))
                        .count(tuple.get(payment.count()))
                        .totalAmount(tuple.get(payment.amount.sum()) != null ? 
                                    tuple.get(payment.amount.sum()) : BigDecimal.ZERO)
                        .build())
                .toList();
    }

    @Override
    public Long countPendingPayments() {
        return queryFactory
                .select(payment.count())
                .from(payment)
                .where(payment.status.eq(PaymentStatus.PENDING))
                .fetchOne();
    }

    @Override
    public Long countFailedPaymentsInLastHour() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return queryFactory
                .select(payment.count())
                .from(payment)
                .where(payment.status.eq(PaymentStatus.FAILED)
                        .and(payment.createdAt.after(oneHourAgo)))
                .fetchOne();
    }


    @Override
    public List<Payment> findFailedPaymentsForRetry(LocalDateTime beforeTime) {
        return queryFactory
                .selectFrom(payment)
                .where(payment.status.eq(PaymentStatus.FAILED)
                        .and(payment.createdAt.after(beforeTime)))
                .orderBy(payment.createdAt.asc())
                .fetch();
    }



    @Override
    public Optional<Payment> findByPaymentKey(String paymentKey) {
        Payment result = queryFactory
                .selectFrom(payment)
                .where(payment.paymentKey.eq(paymentKey))
                .fetchOne();
        return Optional.ofNullable(result);
    }
} 