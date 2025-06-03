package com.yanolja.areas.reservation.repository;

import com.yanolja.areas.reservation.entity.Reservation;
import com.yanolja.areas.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * QueryDSL을 사용한 Reservation 커스텀 리포지토리 인터페이스
 */
public interface ReservationRepositoryCustom {

    /**
     * 객실별 특정 날짜 범위 예약 조회 (QueryDSL)
     */
    List<Reservation> findOverlappingReservations(Long roomId, LocalDate checkIn, LocalDate checkOut);

    /**
     * 만료된 PENDING 상태 예약 조회 (QueryDSL)
     */
    List<Reservation> findExpiredPendingReservations(LocalDateTime expiredTime);

    /**
     * 특정 기간 예약 조회 (QueryDSL)
     */
    List<Reservation> findByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 복잡한 조건으로 예약 검색 (QueryDSL)
     */
    Page<Reservation> findReservationsWithComplexConditions(
            Long userId, 
            List<ReservationStatus> statuses,
            LocalDate checkInFrom,
            LocalDate checkInTo,
            LocalDate checkOutFrom,
            LocalDate checkOutTo,
            Pageable pageable
    );

    /**
     * 사용자별 예약 통계 조회 (QueryDSL)
     */
    List<Object[]> getReservationStatsByUser(Long userId);

    /**
     * 객실별 예약 현황 조회 (QueryDSL)
     */
    List<Object[]> getReservationStatusByRoom(Long roomId, LocalDate startDate, LocalDate endDate);
} 