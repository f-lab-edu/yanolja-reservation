package com.yanolja.areas.reservation.repository;

import com.yanolja.areas.reservation.entity.Reservation;
import com.yanolja.areas.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {

    /**
     * 사용자별 예약 목록 조회 (페이징)
     */
    Page<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자별 특정 상태 예약 목록 조회 (페이징)
     */
    Page<Reservation> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ReservationStatus status, Pageable pageable);

    /**
     * 사용자별 예약 목록 조회 (상태 목록으로 필터링)
     */
    Page<Reservation> findByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, List<ReservationStatus> statuses, Pageable pageable);

    /**
     * 예약 ID와 사용자 ID로 예약 조회
     */
    Optional<Reservation> findByIdAndUserId(Long id, Long userId);

    /**
     * 체크인 날짜 기준 예약 조회
     */
    List<Reservation> findByCheckInDateAndStatus(LocalDate checkInDate, ReservationStatus status);

    /**
     * 사용자별 예약 건수 조회
     */
    long countByUserId(Long userId);

    /**
     * 객실별 예약 건수 조회
     */
    long countByRoomIdAndStatus(Long roomId, ReservationStatus status);

    /**
     * 예약 ID로 예약 조회 (주문과 연관된 예약 조회용)
     */
    Optional<Reservation> findById(Long reservationId);
} 