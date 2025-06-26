package com.yanolja.areas.payment.repository;

import com.yanolja.areas.payment.entity.Point;
import com.yanolja.areas.payment.entity.PointStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long>, PointRepositoryCustom {

    /**
     * 사용자별 포인트 내역 조회 (페이징)
     */
    Page<Point> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 주문별 포인트 내역 조회
     */
    List<Point> findByOrderId(Long orderId);


} 