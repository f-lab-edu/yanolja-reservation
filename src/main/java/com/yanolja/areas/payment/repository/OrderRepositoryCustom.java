package com.yanolja.areas.payment.repository;

import com.yanolja.areas.payment.entity.Order;
import com.yanolja.areas.payment.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {


    /**
     * 만료된 대기 중인 주문 조회
     */
    List<Order> findExpiredPendingOrders(LocalDateTime expiredTime);
} 