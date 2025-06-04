package com.yanolja.domain.payment.repository;

import com.yanolja.domain.payment.entity.Point;
import com.yanolja.domain.payment.entity.PointStatus;
import com.yanolja.domain.payment.entity.PointTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long>, PointRepositoryCustom {

    /**
     * 사용자별 포인트 내역 조회 (페이징)
     */
    Page<Point> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자별 최신 포인트 잔액 조회
     */
    @Query("SELECT p FROM Point p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    Optional<Point> findLatestPointByUserId(@Param("userId") Long userId);

    /**
     * 사용자별 현재 포인트 잔액 조회
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p WHERE p.userId = :userId AND p.status = :status")
    Integer getCurrentPointBalance(@Param("userId") Long userId, @Param("status") PointStatus status);

    /**
     * 만료 예정 포인트 조회
     */
    @Query("SELECT p FROM Point p WHERE p.status = :status " +
           "AND p.transactionType = :transactionType " +
           "AND p.expiredAt BETWEEN :startTime AND :endTime")
    List<Point> findExpiringPoints(@Param("status") PointStatus status,
                                  @Param("transactionType") PointTransactionType transactionType,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 특정 기간 내 사용자별 포인트 적립/사용 내역
     */
    @Query("SELECT p FROM Point p WHERE p.userId = :userId " +
           "AND p.transactionType = :transactionType " +
           "AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Point> findPointHistoryByDateRange(@Param("userId") Long userId,
                                           @Param("transactionType") PointTransactionType transactionType,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * 주문별 포인트 내역 조회
     */
    List<Point> findByOrderId(Long orderId);

    /**
     * 사용자별 특정 상태 포인트 내역 조회
     */
    List<Point> findByUserIdAndStatus(Long userId, PointStatus status);

    /**
     * 사용자별 포인트 적립 총액 조회
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p " +
           "WHERE p.userId = :userId AND p.transactionType = :transactionType AND p.status = :status")
    Integer getTotalEarnedPoints(@Param("userId") Long userId,
                                @Param("transactionType") PointTransactionType transactionType,
                                @Param("status") PointStatus status);
} 