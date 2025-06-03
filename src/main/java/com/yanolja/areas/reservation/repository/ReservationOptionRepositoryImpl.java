package com.yanolja.areas.reservation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.reservation.entity.QReservation;
import com.yanolja.areas.reservation.entity.QReservationOption;
import com.yanolja.areas.reservation.entity.ReservationOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * QueryDSL을 사용한 ReservationOption 커스텀 리포지토리 구현체
 */
@Repository
@RequiredArgsConstructor
public class ReservationOptionRepositoryImpl implements ReservationOptionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QReservationOption reservationOption = QReservationOption.reservationOption;
    private final QReservation reservation = QReservation.reservation;

    @Override
    public List<ReservationOption> findByReservationIdWithReservation(Long reservationId) {
        return queryFactory
                .selectFrom(reservationOption)
                .join(reservationOption.reservation, reservation)
                .fetchJoin()
                .where(reservationOption.reservation.id.eq(reservationId))
                .fetch();
    }

    @Override
    public List<Object[]> getOptionUsageStats(Long optionId) {
        return queryFactory
                .select(
                        reservationOption.reservation.status,
                        reservationOption.quantity.sum(),
                        reservationOption.price.multiply(reservationOption.quantity).sum()
                )
                .from(reservationOption)
                .join(reservationOption.reservation, reservation)
                .where(reservationOption.optionId.eq(optionId))
                .groupBy(reservationOption.reservation.status)
                .fetch()
                .stream()
                .map(tuple -> new Object[]{
                        tuple.get(reservationOption.reservation.status),
                        tuple.get(reservationOption.quantity.sum()),
                        tuple.get(reservationOption.price.multiply(reservationOption.quantity).sum())
                })
                .toList();
    }

    @Override
    public List<Object[]> getOptionUsageByPeriod(LocalDate startDate, LocalDate endDate) {
        return queryFactory
                .select(
                        reservationOption.optionId,
                        reservationOption.quantity.sum(),
                        reservationOption.price.multiply(reservationOption.quantity).sum(),
                        reservationOption.count()
                )
                .from(reservationOption)
                .join(reservationOption.reservation, reservation)
                .where(
                        reservation.checkInDate.goe(startDate)
                                .and(reservation.checkOutDate.loe(endDate))
                )
                .groupBy(reservationOption.optionId)
                .orderBy(reservationOption.quantity.sum().desc())
                .fetch()
                .stream()
                .map(tuple -> new Object[]{
                        tuple.get(reservationOption.optionId),
                        tuple.get(reservationOption.quantity.sum()),
                        tuple.get(reservationOption.price.multiply(reservationOption.quantity).sum()),
                        tuple.get(reservationOption.count())
                })
                .toList();
    }
} 