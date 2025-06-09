package com.yanolja.areas.reservation.repository;

import com.yanolja.areas.reservation.entity.ReservationOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationOptionRepository extends JpaRepository<ReservationOption, Long>, ReservationOptionRepositoryCustom {

    /**
     * 예약별 옵션 목록 조회
     */
    List<ReservationOption> findByReservationId(Long reservationId);

    /**
     * 특정 옵션 ID로 예약 옵션 조회
     */
    List<ReservationOption> findByOptionId(Long optionId);

    /**
     * 예약과 옵션 ID로 예약 옵션 조회
     */
    List<ReservationOption> findByReservationIdAndOptionId(Long reservationId, Long optionId);

    /**
     * 예약별 옵션 삭제
     */
    void deleteByReservationId(Long reservationId);

    /**
     * 예약과 옵션 ID로 예약 옵션 삭제
     */
    void deleteByReservationIdAndOptionId(Long reservationId, Long optionId);
} 