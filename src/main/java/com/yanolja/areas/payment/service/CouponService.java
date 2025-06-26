package com.yanolja.areas.payment.service;

import com.yanolja.areas.payment.dto.CouponDto;
import com.yanolja.areas.payment.entity.*;
import com.yanolja.areas.payment.repository.CouponRepository;
import com.yanolja.areas.payment.repository.UserCouponRepository;
import com.yanolja.common.service.DistributedLockService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final DistributedLockService distributedLockService;

    /**
     * 쿠폰 생성
     */
    @Transactional
    public Coupon createCoupon(CouponDto.CreateCouponRequest request) {
        log.info("쿠폰 생성 시작 - name: {}, discountType: {}", request.getName(), request.getDiscountType());

        // 쿠폰 코드 중복 확인
        if (couponRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("이미 존재하는 쿠폰 코드입니다: " + request.getCode());
        }

        Coupon coupon = Coupon.createCoupon(
            request.getCode(),
            request.getName(),
            request.getDescription(),
            request.getDiscountType(),
            request.getDiscountValue(),
            request.getMaxDiscountAmount(),
            request.getMinOrderAmount(),
            request.getIssueCount(),
            request.getIssueStartAt(),
            request.getIssueEndAt(),
            request.getValidFrom(),
            request.getValidUntil(),
            request.getIssueType()
        );

        coupon = couponRepository.save(coupon);
        log.info("쿠폰 생성 완료 - id: {}, code: {}", coupon.getId(), coupon.getCode());
        
        return coupon;
    }

    /**
     * 사용자에게 쿠폰 발급 (동시성 안전)
     */
    @Transactional
    public UserCoupon issueCouponToUser(Long userId, String couponCode) {
        log.info("쿠폰 발급 시작 - userId: {}, couponCode: {}", userId, couponCode);

        String lockKey = "coupon:issue:" + couponCode;
        String lockValue = distributedLockService.tryLock(lockKey, 5, 30);
        
        if (lockValue == null) {
            throw new IllegalStateException("쿠폰 발급 처리 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            Coupon coupon = findCouponByCode(couponCode);
            
            // 발급 가능 여부 검증
            validateCouponIssuable(coupon, userId);

            // 발급 수량 증가 (동시성 안전)
            boolean success = coupon.tryIncreaseUsedCount();
            if (!success) {
                throw new IllegalStateException("쿠폰 재고가 부족합니다.");
            }
            couponRepository.save(coupon);

            // 사용자 쿠폰 생성
            UserCoupon userCoupon = UserCoupon.issueTo(userId, coupon);
            userCoupon = userCouponRepository.save(userCoupon);

            log.info("쿠폰 발급 완료 - userCouponId: {}", userCoupon.getId());
            return userCoupon;
            
        } finally {
            distributedLockService.unlock(lockKey, lockValue);
        }
    }

    /**
     * 특정 타입의 쿠폰을 사용자에게 자동 발급 (동시성 안전)
     */
    @Transactional
    public void issueAutomaticCoupons(Long userId, CouponIssueType issueType) {
        log.info("자동 쿠폰 발급 시작 - userId: {}, issueType: {}", userId, issueType);

        List<Coupon> availableCoupons = couponRepository.findAvailableForIssue(
            CouponStatus.ACTIVE,
            LocalDateTime.now()
        ).stream()
        .filter(coupon -> coupon.getIssueType() == issueType)
        .filter(coupon -> !userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId()))
        .toList();

        int issuedCount = 0;
        for (Coupon coupon : availableCoupons) {
            try {
                issueCouponToUser(userId, coupon.getCode());
                issuedCount++;
            } catch (Exception e) {
                log.warn("자동 쿠폰 발급 실패 - userId: {}, couponId: {}", userId, coupon.getId(), e);
            }
        }

        log.info("자동 쿠폰 발급 완료 - userId: {}, 발급된 쿠폰 수: {}", userId, issuedCount);
    }

    /**
     * 사용자의 사용 가능한 쿠폰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UserCoupon> getUserAvailableCoupons(Long userId) {
        return userCouponRepository.findAvailableUserCoupons(
            userId, 
            UserCouponStatus.AVAILABLE, 
            LocalDateTime.now()
        );
    }

    /**
     * 특정 주문 금액에 사용 가능한 쿠폰 조회
     */
    @Transactional(readOnly = true)
    public List<UserCoupon> getUsableCouponsForOrder(Long userId, BigDecimal orderAmount) {
        return userCouponRepository.findUsableCouponsForAmount(
            userId, 
            orderAmount.longValue(), 
            LocalDateTime.now()
        );
    }

    /**
     * 쿠폰 목록 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public Page<Coupon> getAllCoupons(Pageable pageable) {
        return couponRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * 쿠폰 검색
     */
    @Transactional(readOnly = true)
    public List<Coupon> searchCoupons(String keyword) {
        return couponRepository.searchCoupons(keyword);
    }

    /**
     * 쿠폰 상태 변경
     */
    @Transactional
    public void updateCouponStatus(Long couponId, CouponStatus status) {
        Coupon coupon = findCouponById(couponId);
        coupon.updateStatus(status);
        couponRepository.save(coupon);
        
        log.info("쿠폰 상태 변경 - couponId: {}, status: {}", couponId, status);
    }

    /**
     * 만료된 쿠폰 처리 (동시성 안전)
     */
    @Transactional
    public void expireCoupons() {
        LocalDateTime now = LocalDateTime.now();
        List<UserCoupon> expiredCoupons = userCouponRepository.findExpiringUserCoupons(
            UserCouponStatus.AVAILABLE, 
            now.minusDays(1), 
            now
        );
        
        int expiredCount = 0;
        for (UserCoupon userCoupon : expiredCoupons) {
            boolean success = userCoupon.tryExpire();
            if (success) {
                userCouponRepository.save(userCoupon);
                expiredCount++;
                log.debug("쿠폰 만료 처리 - userCouponId: {}", userCoupon.getId());
            }
        }
        
        log.info("쿠폰 만료 처리 완료 - 대상 수: {}, 처리된 수: {}", expiredCoupons.size(), expiredCount);
    }

    /**
     * 쿠폰 발급 통계
     */
    @Transactional(readOnly = true)
    public CouponDto.CouponStatistics getCouponStatistics(Long couponId) {
        Coupon coupon = findCouponById(couponId);
        
        Long availableCount = userCouponRepository.countByUserIdAndStatus(null, UserCouponStatus.AVAILABLE);
        Long usedCount = userCouponRepository.countByUserIdAndStatus(null, UserCouponStatus.USED);
        Long expiredCount = userCouponRepository.countByUserIdAndStatus(null, UserCouponStatus.EXPIRED);

        return CouponDto.CouponStatistics.builder()
            .couponId(couponId)
            .totalIssued(coupon.getUsedCount())
            .remainingIssue(coupon.getIssueCount() - coupon.getUsedCount())
            .availableCount(availableCount)
            .usedCount(usedCount)
            .expiredCount(expiredCount)
            .build();
    }

    /**
     * 쿠폰 할인 금액 계산
     */
    @Transactional(readOnly = true)
    public CouponDto.DiscountCalculateResponse calculateDiscount(Long couponId, BigDecimal orderAmount) {
        // 쿠폰 조회
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다: " + couponId));
        
        // 쿠폰 사용 가능 여부 확인
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new IllegalArgumentException("사용할 수 없는 쿠폰입니다.");
        }
        
        // 최소 주문 금액 확인
        if (coupon.getMinOrderAmount() != null && 
            orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException("최소 주문 금액이 부족합니다.");
        }
        
        BigDecimal discountAmount;
        BigDecimal discountRate = BigDecimal.ZERO;
        boolean isMaxDiscountApplied = false;
        
        // 할인 금액 계산
        if (coupon.getDiscountType() == DiscountType.FIXED) {
            // 정액 할인
            discountAmount = coupon.getDiscountValue();
        } else {
            // 정률 할인
            discountRate = coupon.getDiscountValue();
            discountAmount = orderAmount.multiply(discountRate).divide(BigDecimal.valueOf(100));
        }
        
        // 최대 할인 금액 제한 적용
        if (coupon.getMaxDiscountAmount() != null && 
            discountAmount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discountAmount = coupon.getMaxDiscountAmount();
            isMaxDiscountApplied = true;
        }
        
        // 주문 금액을 초과할 수 없음
        if (discountAmount.compareTo(orderAmount) > 0) {
            discountAmount = orderAmount;
        }
        
        return CouponDto.DiscountCalculateResponse.builder()
                .discountAmount(discountAmount)
                .isMaxDiscountApplied(isMaxDiscountApplied)
                .discountRate(discountRate)
                .build();
    }

    /**
     * 사용자 쿠폰 목록 조회 (페이징) - DTO 반환
     */
    @Transactional(readOnly = true)
    public Page<CouponDto.UserCouponResponse> getUserCouponsDto(Long userId, Pageable pageable) {
        Page<UserCoupon> userCoupons = userCouponRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return userCoupons.map(CouponDto.UserCouponResponse::from);
    }

    /**
     * 특정 주문 금액에 사용 가능한 쿠폰 목록 조회 - DTO 반환
     */
    @Transactional(readOnly = true)
    public List<CouponDto.UserCouponResponse> getUsableCouponsDto(Long userId, BigDecimal orderAmount) {
        List<UserCoupon> userCoupons = userCouponRepository.findUsableCouponsForAmount(
            userId, 
            orderAmount.longValue(), 
            LocalDateTime.now()
        );
        return userCoupons.stream()
                .map(CouponDto.UserCouponResponse::from)
                .toList();
    }

    /**
     * 쿠폰 목록 조회 (관리자용) - DTO 반환
     */
    @Transactional(readOnly = true)
    public Page<CouponDto.CouponResponse> getAllCouponsDto(Pageable pageable) {
        Page<Coupon> coupons = couponRepository.findAllByOrderByCreatedAtDesc(pageable);
        return coupons.map(CouponDto.CouponResponse::from);
    }

    /**
     * 쿠폰 발급 - DTO 반환
     */
    @Transactional
    public CouponDto.UserCouponResponse issueCouponToUserDto(Long userId, String couponCode) {
        UserCoupon userCoupon = issueCouponToUser(userId, couponCode);
        return CouponDto.UserCouponResponse.from(userCoupon);
    }

    /**
     * 쿠폰 검색 - DTO 반환
     */
    @Transactional(readOnly = true)
    public List<CouponDto.CouponResponse> searchCouponsDto(String keyword) {
        List<Coupon> coupons = couponRepository.searchCoupons(keyword);
        return coupons.stream()
                .map(CouponDto.CouponResponse::from)
                .toList();
    }

    /**
     * 쿠폰 생성 - DTO 반환
     */
    @Transactional
    public CouponDto.CouponResponse createCouponDto(CouponDto.CreateCouponRequest request) {
        Coupon coupon = createCoupon(request);
        return CouponDto.CouponResponse.from(coupon);
    }

    /**
     * 사용자 쿠폰 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<UserCoupon> getUserCoupons(Long userId, Pageable pageable) {
        return userCouponRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 특정 주문 금액에 사용 가능한 쿠폰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<UserCoupon> getUsableCoupons(Long userId, BigDecimal orderAmount) {
        return userCouponRepository.findUsableCouponsForAmount(
            userId, 
            orderAmount.longValue(), 
            LocalDateTime.now()
        );
    }

    // === Private Methods ===

    private Coupon findCouponById(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다: " + couponId));
    }

    private Coupon findCouponByCode(String couponCode) {
        return couponRepository.findByCode(couponCode)
                .orElseThrow(() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다: " + couponCode));
    }

    private void validateCouponIssuable(Coupon coupon, Long userId) {
        // 이미 발급받은 쿠폰인지 확인
        if (userCouponRepository.existsByUserIdAndCouponId(userId, coupon.getId())) {
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }

        // 발급 수량 확인
        if (coupon.getUsedCount() >= coupon.getIssueCount()) {
            throw new IllegalStateException("쿠폰 발급 수량이 소진되었습니다.");
        }

        // 발급 기간 확인
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getIssueStartAt()) || now.isAfter(coupon.getIssueEndAt())) {
            throw new IllegalStateException("쿠폰 발급 기간이 아닙니다.");
        }

        // 쿠폰 상태 확인
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            throw new IllegalStateException("발급할 수 없는 쿠폰 상태입니다: " + coupon.getStatus());
        }
    }
} 