package com.yanolja.areas.payment.service;

import com.yanolja.areas.payment.dto.PointDto;
import com.yanolja.areas.payment.entity.*;
import com.yanolja.areas.payment.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final DistributedLockService distributedLockService;

    /**
     * 포인트 적립 (동시성 안전)
     */
    @Transactional
    public Point earnPoints(Long userId, Integer amount, Long orderId, String description) {
        log.info("포인트 적립 시작 - userId: {}, amount: {}", userId, amount);

        if (amount <= 0) {
            throw new IllegalArgumentException("적립 포인트는 0보다 커야 합니다.");
        }

        String lockKey = "point:earn:" + userId;
        String lockValue = distributedLockService.tryLock(lockKey, 5, 30);
        
        if (lockValue == null) {
            throw new IllegalStateException("포인트 적립 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            // 현재 잔액 조회
            Integer currentBalance = getCurrentBalance(userId);
            Integer newBalance = currentBalance + amount;

            // 포인트 만료일 설정 (2년 후)
            LocalDateTime expiredAt = LocalDateTime.now().plusYears(2);

            // 포인트 적립 내역 생성
            Point point = Point.createEarnPoint(
                userId, 
                amount, 
                newBalance, 
                description, 
                orderId, 
                expiredAt
            );

            point = pointRepository.save(point);
            log.info("포인트 적립 완료 - userId: {}, amount: {}, newBalance: {}", userId, amount, newBalance);
            
            return point;
            
        } finally {
            distributedLockService.unlock(lockKey, lockValue);
        }
    }

    /**
     * 포인트 사용 (동시성 안전)
     */
    @Transactional
    public Point usePoints(Long userId, Integer amount, Long orderId, String description) {
        log.info("포인트 사용 시작 - userId: {}, amount: {}", userId, amount);

        if (amount <= 0) {
            throw new IllegalArgumentException("사용 포인트는 0보다 커야 합니다.");
        }

        String lockKey = "point:use:" + userId;
        String lockValue = distributedLockService.tryLock(lockKey, 5, 30);
        
        if (lockValue == null) {
            throw new IllegalStateException("포인트 사용 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            // 포인트 사용 검증
            validatePointUsage(userId, amount);

            // 현재 잔액 조회
            Integer currentBalance = getCurrentBalance(userId);
            Integer newBalance = currentBalance - amount;

            // 포인트 사용 내역 생성
            Point point = Point.createUsePoint(
                userId, 
                amount, 
                newBalance, 
                description, 
                orderId
            );

            point = pointRepository.save(point);
            log.info("포인트 사용 완료 - userId: {}, amount: {}, newBalance: {}", userId, amount, newBalance);
            
            return point;
            
        } finally {
            distributedLockService.unlock(lockKey, lockValue);
        }
    }

    /**
     * 포인트 환불 (주문 취소 시) - 동시성 안전
     */
    @Transactional
    public Point refundPoints(Long userId, Integer amount, Long orderId, String description) {
        log.info("포인트 환불 시작 - userId: {}, amount: {}", userId, amount);

        if (amount <= 0) {
            throw new IllegalArgumentException("환불 포인트는 0보다 커야 합니다.");
        }

        String lockKey = "point:refund:" + userId;
        String lockValue = distributedLockService.tryLock(lockKey, 5, 30);
        
        if (lockValue == null) {
            throw new IllegalStateException("포인트 환불 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            // 현재 잔액 조회
            Integer currentBalance = getCurrentBalance(userId);
            Integer newBalance = currentBalance + amount;

            // 포인트 환불 만료일 설정 (원래 사용했던 포인트의 만료일을 고려해야 하지만 간소화)
            LocalDateTime expiredAt = LocalDateTime.now().plusYears(2);

            // 포인트 환불 내역 생성
            Point point = Point.createEarnPoint(
                userId, 
                amount, 
                newBalance, 
                "환불: " + description, 
                orderId, 
                expiredAt
            );

            point = pointRepository.save(point);
            log.info("포인트 환불 완료 - userId: {}, amount: {}, newBalance: {}", userId, amount, newBalance);
            
            return point;
            
        } finally {
            distributedLockService.unlock(lockKey, lockValue);
        }
    }

    /**
     * 현재 포인트 잔액 조회
     */
    @Transactional(readOnly = true)
    public Integer getCurrentBalance(Long userId) {
        return pointRepository.getCurrentPointBalance(userId, PointStatus.ACTIVE);
    }

    /**
     * 사용자 포인트 내역 조회
     */
    @Transactional(readOnly = true)
    public Page<Point> getUserPointHistory(Long userId, Pageable pageable) {
        return pointRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 특정 기간 포인트 내역 조회
     */
    @Transactional(readOnly = true)
    public List<Point> getPointHistoryByDateRange(Long userId, PointTransactionType transactionType,
                                                  LocalDateTime startDate, LocalDateTime endDate) {
        return pointRepository.findPointHistoryByDateRange(userId, transactionType, startDate, endDate);
    }

    /**
     * 주문별 포인트 내역 조회
     */
    @Transactional(readOnly = true)
    public List<Point> getPointsByOrder(Long orderId) {
        return pointRepository.findByOrderId(orderId);
    }

    /**
     * 사용자별 총 적립 포인트 조회
     */
    @Transactional(readOnly = true)
    public Integer getTotalEarnedPoints(Long userId) {
        return pointRepository.getTotalEarnedPoints(
            userId, 
            PointTransactionType.EARN, 
            PointStatus.ACTIVE
        );
    }

    /**
     * 만료 예정 포인트 조회
     */
    @Transactional(readOnly = true)
    public List<Point> getExpiringPoints(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return pointRepository.findUserExpiringPoints(userId, startTime, endTime);
    }

    /**
     * 만료된 포인트 처리 (동시성 안전)
     */
    @Transactional
    public void expirePoints() {
        LocalDateTime now = LocalDateTime.now();
        
        // 만료 예정 포인트 조회 (오늘까지 만료되는 포인트)
        List<Point> expiringPoints = pointRepository.findExpiringPoints(
            PointStatus.ACTIVE,
            PointTransactionType.EARN,
            now.minusDays(1),
            now
        );

        int expiredCount = 0;
        for (Point point : expiringPoints) {
            String lockKey = "point:expire:" + point.getId();
            String lockValue = distributedLockService.tryLock(lockKey, 1, 10);
            
            if (lockValue != null) {
                try {
                    point.expire();
                    pointRepository.save(point);
                    expiredCount++;
                    
                    log.debug("포인트 만료 처리 - pointId: {}, userId: {}, amount: {}", 
                            point.getId(), point.getUserId(), point.getAmount());
                } finally {
                    distributedLockService.unlock(lockKey, lockValue);
                }
            }
        }

        log.info("포인트 만료 처리 완료 - 대상: {}개, 처리된 포인트: {}개", expiringPoints.size(), expiredCount);
    }

    /**
     * 만료 예정 포인트 알림 정보 조회 (N일 전)
     */
    @Transactional(readOnly = true)
    public List<PointDto.PointExpiryNotification> getPointExpiryNotifications(int daysBefore) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(daysBefore - 1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(daysBefore);
        
        List<Point> expiringPoints = pointRepository.findExpiringPoints(
            PointStatus.ACTIVE,
            PointTransactionType.EARN, 
            startTime,
            endTime
        );

        return expiringPoints.stream()
            .map(point -> PointDto.PointExpiryNotification.builder()
                .userId(point.getUserId())
                .expiringAmount(point.getAmount())
                .expiryDate(point.getExpiredAt().toLocalDate())
                .build())
            .toList();
    }

    /**
     * 사용자 포인트 통계
     */
    @Transactional(readOnly = true)
    public PointDto.PointStatistics getPointStatistics(Long userId) {
        Integer currentBalance = getCurrentBalance(userId);
        Integer totalEarned = getTotalEarnedPoints(userId);
        
        // 이번 달 적립 포인트
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime monthEnd = LocalDateTime.now();
        List<Point> monthlyPoints = getPointHistoryByDateRange(userId, PointTransactionType.EARN, monthStart, monthEnd);
        Integer monthlyEarned = monthlyPoints.stream()
            .mapToInt(Point::getAmount)
            .sum();

        // 30일 내 만료 예정 포인트
        LocalDateTime expiryStart = LocalDateTime.now();
        LocalDateTime expiryEnd = LocalDateTime.now().plusDays(30);
        List<Point> expiringPoints = getExpiringPoints(userId, expiryStart, expiryEnd);
        Integer expiringAmount = expiringPoints.stream()
            .mapToInt(Point::getAmount)
            .sum();

        return PointDto.PointStatistics.builder()
            .userId(userId)
            .currentBalance(currentBalance)
            .totalEarned(totalEarned)
            .monthlyEarned(monthlyEarned)
            .expiringAmount(expiringAmount)
            .build();
    }

    // === Private Methods ===

    private void validatePointUsage(Long userId, Integer amount) {
        Integer currentBalance = getCurrentBalance(userId);
        
        if (currentBalance < amount) {
            throw new IllegalArgumentException("사용 가능한 포인트가 부족합니다.");
        }
    }
} 