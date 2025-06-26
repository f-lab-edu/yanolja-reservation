package com.yanolja.areas.reservation.service;

import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import com.yanolja.areas.reservation.dto.ReservationDto;
import com.yanolja.areas.reservation.dto.ReservationOptionDto;
import com.yanolja.areas.reservation.entity.Reservation;
import com.yanolja.areas.reservation.entity.ReservationOption;
import com.yanolja.areas.reservation.entity.ReservationStatus;
import com.yanolja.areas.reservation.repository.ReservationOptionRepository;
import com.yanolja.areas.reservation.repository.ReservationRepository;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.areas.room.entity.RoomOption;
import com.yanolja.areas.room.repository.RoomOptionRepository;
import com.yanolja.areas.room.repository.RoomRepository;
import com.yanolja.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.yanolja.areas.reservation.dto.ReservationStatsDto;
import com.yanolja.areas.reservation.dto.ReservationStatusDto;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationOptionRepository reservationOptionRepository;
    private final RoomRepository roomRepository;
    private final RoomOptionRepository roomOptionRepository;
    private final AccommodationRepository accommodationRepository;

    /**
     * 예약 생성
     */
    public ReservationDto.Response createReservation(Long userId, ReservationDto.Request request) {
        log.info("Creating reservation for user: {}, room: {}", userId, request.getRoomId());

        // 1. 유효성 검증
        validateReservationRequest(request);

        // 2. 객실 조회 및 검증
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("객실을 찾을 수 없습니다: " + request.getRoomId()));

        // 3. 예약 가능 여부 확인 (QueryDSL 사용)
        validateRoomAvailability(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate());

        // 4. 예약 생성
        Reservation reservation = Reservation.createReservation(
                userId,
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getTotalPrice()
        );

        // 5. 예약 옵션 추가
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (ReservationOptionDto.Request optionRequest : request.getOptions()) {
                validateRoomOption(room.getId(), optionRequest.getOptionId());
                
                ReservationOption reservationOption = ReservationOption.createReservationOption(
                        optionRequest.getOptionId(),
                        optionRequest.getQuantity(),
                        optionRequest.getPrice()
                );
                reservation.addOption(reservationOption);
            }
        }

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reservation created successfully: {}", savedReservation.getId());

        return ReservationDto.Response.fromEntity(savedReservation);
    }

    /**
     * 예약 상세 조회
     */
    @Transactional(readOnly = true)
    public ReservationDto.Response getReservation(Long reservationId, Long userId) {
        log.info("Getting reservation: {} for user: {}", reservationId, userId);

        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다: " + reservationId));

        // 추가 정보 조회
        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new EntityNotFoundException("객실 정보를 찾을 수 없습니다."));

        Accommodation accommodation = accommodationRepository.findById(room.getAccommodationId())
                .orElseThrow(() -> new EntityNotFoundException("숙소 정보를 찾을 수 없습니다."));

        return ReservationDto.Response.fromEntityWithDetails(
                reservation,
                room.getName(),
                accommodation.getName(),
                accommodation.getAddress(),
                null // 이미지는 별도 서비스에서 조회
        );
    }

    /**
     * 사용자별 예약 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ReservationDto.ListResponse> getUserReservations(Long userId, ReservationStatus status, Pageable pageable) {
        log.info("Getting reservations for user: {}, status: {}", userId, status);

        Page<Reservation> reservations;
        if (status != null) {
            reservations = reservationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else {
            reservations = reservationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return reservations.map(reservation -> {
            Room room = roomRepository.findById(reservation.getRoomId()).orElse(null);
            Accommodation accommodation = null;
            if (room != null) {
                accommodation = accommodationRepository.findById(room.getAccommodationId()).orElse(null);
            }

            return ReservationDto.ListResponse.fromEntityWithDetails(
                    reservation,
                    room != null ? room.getName() : "알 수 없음",
                    accommodation != null ? accommodation.getName() : "알 수 없음",
                    null // 이미지는 별도 서비스에서 조회
            );
        });
    }

    /**
     * 고급 검색을 통한 예약 목록 조회 (QueryDSL 사용)
     */
    @Transactional(readOnly = true)
    public Page<ReservationDto.ListResponse> searchReservations(ReservationDto.SearchCondition condition, Pageable pageable) {
        log.info("Searching reservations with conditions: {}", condition);

        Page<Reservation> reservations = reservationRepository.findReservationsWithComplexConditions(
                condition.getUserId(),
                condition.getStatuses(),
                condition.getCheckInDateFrom(),
                condition.getCheckInDateTo(),
                condition.getCheckOutDateFrom(),
                condition.getCheckOutDateTo(),
                pageable
        );

        return reservations.map(reservation -> {
            Room room = roomRepository.findById(reservation.getRoomId()).orElse(null);
            Accommodation accommodation = null;
            if (room != null) {
                accommodation = accommodationRepository.findById(room.getAccommodationId()).orElse(null);
            }

            return ReservationDto.ListResponse.fromEntityWithDetails(
                    reservation,
                    room != null ? room.getName() : "알 수 없음",
                    accommodation != null ? accommodation.getName() : "알 수 없음",
                    null
            );
        });
    }

    /**
     * 예약 상태 변경
     */
    public ReservationDto.Response updateReservationStatus(Long reservationId, Long userId, 
                                                         ReservationDto.StatusUpdateRequest request) {
        log.info("Updating reservation status: {} to {}", reservationId, request.getStatus());

        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다: " + reservationId));

        // 상태 변경 가능 여부 확인
        validateStatusChange(reservation, request.getStatus());

        reservation.updateStatus(request.getStatus());
        Reservation updatedReservation = reservationRepository.save(reservation);

        log.info("Reservation status updated successfully: {} -> {}", reservationId, request.getStatus());
        return ReservationDto.Response.fromEntity(updatedReservation);
    }

    /**
     * 예약 취소
     */
    public ReservationDto.Response cancelReservation(Long reservationId, Long userId) {
        log.info("Cancelling reservation: {} for user: {}", reservationId, userId);

        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다: " + reservationId));

        if (!reservation.canCancel()) {
            throw new IllegalStateException("취소할 수 없는 예약입니다.");
        }

        reservation.cancel();
        Reservation cancelledReservation = reservationRepository.save(reservation);

        log.info("Reservation cancelled successfully: {}", reservationId);
        return ReservationDto.Response.fromEntity(cancelledReservation);
    }

    /**
     * 예약 확정
     */
    public ReservationDto.Response confirmReservation(Long reservationId) {
        log.info("Confirming reservation: {}", reservationId);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("예약을 찾을 수 없습니다: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("확정할 수 없는 예약 상태입니다: " + reservation.getStatus());
        }

        reservation.confirm();
        Reservation confirmedReservation = reservationRepository.save(reservation);

        log.info("Reservation confirmed successfully: {}", reservationId);
        return ReservationDto.Response.fromEntity(confirmedReservation);
    }

    /**
     * 만료된 PENDING 예약 정리 (QueryDSL 사용)
     */
    public void cleanupExpiredReservations() {
        log.info("Cleaning up expired reservations");

        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(10); // 10분 전
        List<Reservation> expiredReservations = reservationRepository.findExpiredPendingReservations(expiredTime);

        for (Reservation reservation : expiredReservations) {
            reservation.cancel();
            reservationRepository.save(reservation);
            log.info("Expired reservation cancelled: {}", reservation.getId());
        }

        log.info("Cleanup completed. {} reservations cancelled", expiredReservations.size());
    }

    /**
     * 사용자별 예약 통계 조회 (QueryDSL 사용)
     */
    @Transactional(readOnly = true)
    public List<ReservationStatsDto> getUserReservationStats(Long userId) {
        log.info("Getting reservation stats for user: {}", userId);
        return reservationRepository.getReservationStatsByUser(userId);
    }

    /**
     * 객실별 예약 현황 조회 (QueryDSL 사용)
     */
    @Transactional(readOnly = true)
    public List<ReservationStatusDto> getRoomReservationStatus(Long roomId, LocalDate startDate, LocalDate endDate) {
        log.info("Getting reservation status for room: {} from {} to {}", roomId, startDate, endDate);
        return reservationRepository.getReservationStatusByRoom(roomId, startDate, endDate);
    }

    /**
     * 예약 요청 유효성 검증
     */
    private void validateReservationRequest(ReservationDto.Request request) {
        // 체크인 날짜가 체크아웃 날짜보다 이전인지 확인
        if (!request.getCheckInDate().isBefore(request.getCheckOutDate())) {
            throw new IllegalArgumentException("체크아웃 날짜는 체크인 날짜보다 이후여야 합니다.");
        }

        // 체크인 날짜가 오늘 이후인지 확인
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("체크인 날짜는 오늘 이후여야 합니다.");
        }

        // 최대 숙박 기간 확인 (30일)
        long days = request.getCheckInDate().until(request.getCheckOutDate()).getDays();
        if (days > 30) {
            throw new IllegalArgumentException("최대 숙박 기간은 30일입니다.");
        }
    }

    /**
     * 객실 예약 가능 여부 확인 (QueryDSL 사용)
     */
    private void validateRoomAvailability(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                roomId, checkIn, checkOut
        );

        if (!overlappingReservations.isEmpty()) {
            throw new IllegalStateException("해당 날짜에 이미 예약된 객실입니다.");
        }
    }

    /**
     * 객실 옵션 유효성 확인
     */
    private void validateRoomOption(Long roomId, Long optionId) {
        RoomOption roomOption = roomOptionRepository.findById(optionId)
                .orElseThrow(() -> new EntityNotFoundException("객실 옵션을 찾을 수 없습니다: " + optionId));

        // 해당 객실에 해당 옵션이 있는지 확인하는 로직 추가 필요
        // (RoomOptionMapping을 통해 확인)
    }

    /**
     * 예약 상태 변경 가능 여부 확인
     */
    private void validateStatusChange(Reservation reservation, ReservationStatus newStatus) {
        ReservationStatus currentStatus = reservation.getStatus();

        // 비즈니스 규칙에 따른 상태 변경 검증
        switch (newStatus) {
            case CONFIRMED:
                if (currentStatus != ReservationStatus.PENDING) {
                    throw new IllegalStateException("PENDING 상태에서만 확정할 수 있습니다.");
                }
                break;
            case CANCELLED:
                if (!reservation.canCancel()) {
                    throw new IllegalStateException("취소할 수 없는 예약입니다.");
                }
                break;
            case COMPLETED:
                if (currentStatus != ReservationStatus.CONFIRMED) {
                    throw new IllegalStateException("확정된 예약만 완료 처리할 수 있습니다.");
                }
                break;
            case NO_SHOW:
                if (currentStatus != ReservationStatus.CONFIRMED) {
                    throw new IllegalStateException("확정된 예약만 노쇼 처리할 수 있습니다.");
                }
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 상태 변경입니다: " + newStatus);
        }
    }
} 