package com.yanolja.areas.payment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.payment.entity.Order;
import com.yanolja.areas.payment.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.yanolja.areas.payment.entity.QOrder.order;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<Order> findExpiredPendingOrders(LocalDateTime expiredTime) {
        return queryFactory
                .selectFrom(order)
                .where(
                        order.status.eq(OrderStatus.PENDING),
                        order.createdAt.lt(expiredTime)
                )
                .orderBy(order.createdAt.asc())
                .fetch();
    }
} 