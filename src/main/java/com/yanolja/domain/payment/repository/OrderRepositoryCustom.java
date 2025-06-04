package com.yanolja.domain.payment.repository;

import com.yanolja.domain.payment.entity.Order;
import com.yanolja.domain.payment.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {

    /**
     * 만료된 주문 조회
     */
    List<Order> findExpiredOrders(OrderStatus status, LocalDateTime currentTime);

    /**
     * 특정 기간 내 주문 통계
     */
    Long countOrdersByStatusAndDateRange(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 사용자별 특정 기간 내 주문 목록 조회
     */
    List<Order> findOrdersByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 상태별 주문 개수 조회
     */
    Long countOrdersByStatus(OrderStatus status);
} 