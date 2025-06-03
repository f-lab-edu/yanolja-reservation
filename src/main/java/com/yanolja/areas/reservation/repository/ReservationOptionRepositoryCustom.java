package com.yanolja.areas.reservation.repository;

import com.yanolja.areas.reservation.entity.ReservationOption;

import java.util.List;

/**
 * QueryDSL을 사용한 ReservationOption 커스텀 리포지토리 인터페이스
 */
public interface ReservationOptionRepositoryCustom {

    /**
     * 예약 옵션을 예약과 함께 조회 (QueryDSL)
     */
    List<ReservationOption> findByReservationIdWithReservation(Long reservationId);

    /**
     * 특정 옵션 ID의 사용 통계 조회 (QueryDSL)
     */
    List<Object[]> getOptionUsageStats(Long optionId);

    /**
     * 기간별 옵션 사용량 조회 (QueryDSL)
     */
    List<Object[]> getOptionUsageByPeriod(java.time.LocalDate startDate, java.time.LocalDate endDate);
} 