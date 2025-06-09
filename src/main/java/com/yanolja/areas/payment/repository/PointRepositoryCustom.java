package com.yanolja.areas.payment.repository;

import com.yanolja.areas.payment.entity.Point;
import com.yanolja.areas.payment.entity.PointStatus;
import com.yanolja.areas.payment.entity.PointTransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PointRepositoryCustom {

    /**
     * 사용자별 최신 포인트 잔액 조회
     */
    Optional<Point> findLatestPointByUserId(Long userId);

    /**
     * 사용자별 현재 포인트 잔액 조회
     */
    Integer getCurrentPointBalance(Long userId, PointStatus status);

    /**
     * 만료 예정 포인트 조회
     */
    List<Point> findExpiringPoints(PointStatus status, PointTransactionType transactionType, 
                                  LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 특정 기간 내 사용자별 포인트 적립/사용 내역
     */
    List<Point> findPointHistoryByDateRange(Long userId, PointTransactionType transactionType,
                                           LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 사용자별 포인트 적립 총액 조회
     */
    Integer getTotalEarnedPoints(Long userId, PointTransactionType transactionType, PointStatus status);

    /**
     * 사용자별 만료 예정 포인트 조회
     */
    List<Point> findUserExpiringPoints(Long userId, LocalDateTime startTime, LocalDateTime endTime);
} 