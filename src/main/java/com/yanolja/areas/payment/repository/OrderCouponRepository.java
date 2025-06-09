package com.yanolja.areas.payment.repository;

import com.yanolja.areas.payment.entity.OrderCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderCouponRepository extends JpaRepository<OrderCoupon, Long> {

    /**
     * 주문별 사용된 쿠폰 목록 조회
     */
    List<OrderCoupon> findByOrderId(Long orderId);

    /**
     * 사용자 쿠폰별 주문 목록 조회
     */
    List<OrderCoupon> findByUserCouponId(Long userCouponId);
} 