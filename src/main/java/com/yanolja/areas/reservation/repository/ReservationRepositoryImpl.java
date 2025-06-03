package com.yanolja.areas.reservation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.reservation.entity.QReservation;
import com.yanolja.areas.reservation.entity.Reservation;
import com.yanolja.areas.reservation.entity.ReservationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * QueryDSL을 사용한 Reservation 커스텀 리포지토리 구현체
 */
@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QReservation reservation = QReservation.reservation;

    @Override
    public List<Reservation> findOverlappingReservations(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return queryFactory
                .selectFrom(reservation)
                .where(
                        reservation.roomId.eq(roomId)
                                .and(reservation.status.in(ReservationStatus.CONFIRMED, ReservationStatus.COMPLETED))
                                .and(
                                        reservation.checkInDate.loe(checkOut)
                                                .and(reservation.checkOutDate.gt(checkIn))
                                )
                )
                .fetch();
    }

    @Override
    public List<Reservation> findExpiredPendingReservations(LocalDateTime expiredTime) {
        return queryFactory
                .selectFrom(reservation)
                .where(
                        reservation.status.eq(ReservationStatus.PENDING)
                                .and(reservation.createdAt.lt(expiredTime))
                )
                .fetch();
    }

    @Override
    public List<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return queryFactory
                .selectFrom(reservation)
                .where(
                        reservation.checkInDate.goe(startDate)
                                .and(reservation.checkOutDate.loe(endDate))
                )
                .fetch();
    }

    @Override
    public Page<Reservation> findReservationsWithComplexConditions(
            Long userId,
            List<ReservationStatus> statuses,
            LocalDate checkInFrom,
            LocalDate checkInTo,
            LocalDate checkOutFrom,
            LocalDate checkOutTo,
            Pageable pageable) {

        BooleanBuilder builder = new BooleanBuilder();

        // 사용자 ID 조건
        if (userId != null) {
            builder.and(reservation.userId.eq(userId));
        }

        // 상태 조건
        if (statuses != null && !statuses.isEmpty()) {
            builder.and(reservation.status.in(statuses));
        }

        // 체크인 날짜 범위
        if (checkInFrom != null) {
            builder.and(reservation.checkInDate.goe(checkInFrom));
        }
        if (checkInTo != null) {
            builder.and(reservation.checkInDate.loe(checkInTo));
        }

        // 체크아웃 날짜 범위
        if (checkOutFrom != null) {
            builder.and(reservation.checkOutDate.goe(checkOutFrom));
        }
        if (checkOutTo != null) {
            builder.and(reservation.checkOutDate.loe(checkOutTo));
        }

        JPAQuery<Reservation> query = queryFactory
                .selectFrom(reservation)
                .where(builder)
                .orderBy(reservation.createdAt.desc());

        List<Reservation> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .selectFrom(reservation)
                .where(builder)
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Object[]> getReservationStatsByUser(Long userId) {
        return queryFactory
                .select(
                        reservation.status,
                        reservation.count(),
                        reservation.totalPrice.sum()
                )
                .from(reservation)
                .where(reservation.userId.eq(userId))
                .groupBy(reservation.status)
                .fetch()
                .stream()
                .map(tuple -> new Object[]{
                        tuple.get(reservation.status),
                        tuple.get(reservation.count()),
                        tuple.get(reservation.totalPrice.sum())
                })
                .toList();
    }

    @Override
    public List<Object[]> getReservationStatusByRoom(Long roomId, LocalDate startDate, LocalDate endDate) {
        return queryFactory
                .select(
                        reservation.checkInDate,
                        reservation.checkOutDate,
                        reservation.status,
                        reservation.count()
                )
                .from(reservation)
                .where(
                        reservation.roomId.eq(roomId)
                                .and(reservation.checkInDate.goe(startDate))
                                .and(reservation.checkOutDate.loe(endDate))
                )
                .groupBy(
                        reservation.checkInDate,
                        reservation.checkOutDate,
                        reservation.status
                )
                .orderBy(reservation.checkInDate.asc())
                .fetch()
                .stream()
                .map(tuple -> new Object[]{
                        tuple.get(reservation.checkInDate),
                        tuple.get(reservation.checkOutDate),
                        tuple.get(reservation.status),
                        tuple.get(reservation.count())
                })
                .toList();
    }
} 