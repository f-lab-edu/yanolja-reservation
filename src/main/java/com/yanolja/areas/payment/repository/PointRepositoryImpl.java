package com.yanolja.areas.payment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.payment.entity.Point;
import com.yanolja.areas.payment.entity.PointStatus;
import com.yanolja.areas.payment.entity.PointTransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.yanolja.areas.payment.entity.QPoint.point;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Point> findLatestPointByUserId(Long userId) {
        Point result = queryFactory
                .selectFrom(point)
                .where(point.userId.eq(userId))
                .orderBy(point.createdAt.desc())
                .fetchFirst();
        return Optional.ofNullable(result);
    }

    @Override
    public Integer getCurrentPointBalance(Long userId, PointStatus status) {
        Integer result = queryFactory
                .select(point.amount.sum().coalesce(0))
                .from(point)
                .where(
                        point.userId.eq(userId),
                        point.status.eq(status)
                )
                .fetchOne();
        return result != null ? result : 0;
    }

    @Override
    public List<Point> findExpiringPoints(PointStatus status, PointTransactionType transactionType, 
                                         LocalDateTime startTime, LocalDateTime endTime) {
        return queryFactory
                .selectFrom(point)
                .where(
                        point.status.eq(status),
                        point.transactionType.eq(transactionType),
                        point.expiredAt.between(startTime, endTime)
                )
                .fetch();
    }

    @Override
    public List<Point> findPointHistoryByDateRange(Long userId, PointTransactionType transactionType,
                                                   LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
                .selectFrom(point)
                .where(
                        point.userId.eq(userId),
                        point.transactionType.eq(transactionType),
                        point.createdAt.between(startDate, endDate)
                )
                .orderBy(point.createdAt.desc())
                .fetch();
    }

    @Override
    public Integer getTotalEarnedPoints(Long userId, PointTransactionType transactionType, PointStatus status) {
        Integer result = queryFactory
                .select(point.amount.sum().coalesce(0))
                .from(point)
                .where(
                        point.userId.eq(userId),
                        point.transactionType.eq(transactionType),
                        point.status.eq(status)
                )
                .fetchOne();
        return result != null ? result : 0;
    }

    @Override
    public List<Point> findUserExpiringPoints(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return queryFactory
                .selectFrom(point)
                .where(
                        point.userId.eq(userId),
                        point.status.eq(PointStatus.ACTIVE),
                        point.transactionType.eq(PointTransactionType.EARN),
                        point.expiredAt.between(startTime, endTime)
                )
                .orderBy(point.expiredAt.asc())
                .fetch();
    }
} 