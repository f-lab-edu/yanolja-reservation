package com.yanolja.domain.payment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.domain.payment.entity.Order;
import com.yanolja.domain.payment.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.yanolja.domain.payment.entity.QOrder.order;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Order> findExpiredOrders(OrderStatus status, LocalDateTime currentTime) {
        return queryFactory
                .selectFrom(order)
                .where(
                        order.status.eq(status),
                        order.expiredAt.lt(currentTime)
                )
                .fetch();
    }

    @Override
    public Long countOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
                .select(order.count())
                .from(order)
                .where(
                        order.status.eq(status),
                        order.createdAt.between(startDate, endDate)
                )
                .fetchOne();
    }

    @Override
    public List<Order> findOrdersByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
                .selectFrom(order)
                .where(
                        order.userId.eq(userId),
                        order.createdAt.between(startDate, endDate)
                )
                .orderBy(order.createdAt.desc())
                .fetch();
    }

    @Override
    public Long countOrdersByStatus(OrderStatus status) {
        return queryFactory
                .select(order.count())
                .from(order)
                .where(order.status.eq(status))
                .fetchOne();
    }
} 