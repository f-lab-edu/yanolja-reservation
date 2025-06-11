package com.yanolja.areas.payment.service;

import com.yanolja.areas.payment.dto.PaymentStatisticsDto;
import com.yanolja.areas.payment.entity.PointStatus;
import com.yanolja.areas.payment.entity.PointTransactionType;
import com.yanolja.areas.payment.entity.PaymentStatus;
import com.yanolja.areas.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제 시스템 스케줄링 서비스
 * 주문 만료, 포인트 만료, 쿠폰 만료 등의 정기 작업 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSchedulerService {

    private final OrderService orderService;
    private final PointService pointService;
    private final CouponService couponService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    /**
     * 만료된 주문 처리 (매 10분마다 실행)
     */
    @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    public void processExpiredOrders() {
        try {
            log.info("만료된 주문 처리 시작");
            orderService.expireOrders();
            log.info("만료된 주문 처리 완료");
        } catch (Exception e) {
            log.error("만료된 주문 처리 중 오류 발생", e);
        }
    }

    /**
     * 만료된 포인트 처리 (매일 오전 2시 실행)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void processExpiredPoints() {
        try {
            log.info("만료된 포인트 처리 시작");
            pointService.expirePoints();
            log.info("만료된 포인트 처리 완료");
        } catch (Exception e) {
            log.error("만료된 포인트 처리 중 오류 발생", e);
        }
    }

    /**
     * 만료된 쿠폰 처리 (매일 오전 3시 실행)
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void processExpiredCoupons() {
        try {
            log.info("만료된 쿠폰 처리 시작");
            couponService.expireCoupons();
            log.info("만료된 쿠폰 처리 완료");
        } catch (Exception e) {
            log.error("만료된 쿠폰 처리 중 오류 발생", e);
        }
    }

    /**
     * 실패한 결제 재시도 (매 30분마다 실행)
     */
    @Scheduled(fixedRate = 1800000) // 30분 = 1,800,000ms
    public void retryFailedPayments() {
        try {
            log.info("실패한 결제 재시도 시작");
            paymentService.retryFailedPayments();
            log.info("실패한 결제 재시도 완료");
        } catch (Exception e) {
            log.error("실패한 결제 재시도 중 오류 발생", e);
        }
    }



} 