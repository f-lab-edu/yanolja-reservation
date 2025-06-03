package com.yanolja.areas.reservation.scheduler;

import com.yanolja.areas.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 예약 관련 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    /**
     * 만료된 PENDING 예약 정리 (5분마다 실행)
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    public void cleanupExpiredReservations() {
        try {
            log.info("Starting cleanup of expired reservations");
            reservationService.cleanupExpiredReservations();
            log.info("Finished cleanup of expired reservations");
        } catch (Exception e) {
            log.error("Error occurred during reservation cleanup", e);
        }
    }
} 