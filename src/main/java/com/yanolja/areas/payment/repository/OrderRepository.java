package com.yanolja.areas.payment.repository;

import com.yanolja.areas.payment.entity.Order;
import com.yanolja.areas.payment.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    /**
     * 주문 번호로 주문 조회
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 사용자별 주문 목록 조회 (페이징)
     */
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);


    /**
     * 사용자별 주문 존재 여부 확인
     */
    boolean existsByUserIdAndReservationId(Long userId, Long reservationId);
} 