package com.yanolja.areas.payment.service;

import com.yanolja.areas.payment.dto.OrderDto;
import com.yanolja.areas.payment.dto.OrderCouponDto;
import com.yanolja.areas.payment.entity.*;
import com.yanolja.areas.payment.repository.OrderCouponRepository;
import com.yanolja.areas.payment.repository.OrderRepository;
import com.yanolja.areas.payment.repository.PointRepository;
import com.yanolja.areas.payment.repository.UserCouponRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserCouponRepository userCouponRepository;
    private final OrderCouponRepository orderCouponRepository;
    private final PointRepository pointRepository;
    private final CouponService couponService;
    private final PointService pointService;
    private final DistributedLockService distributedLockService;

    /**
     * 주문 생성 (동시성 안전)
     */
    @Transactional
    public OrderDto.Response createOrder(OrderDto.CreateRequest request) {
        log.info("주문 생성 시작 - userId: {}, reservationId: {}", request.getUserId(), request.getReservationId());

        String lockKey = "order:create:" + request.getUserId() + ":" + request.getReservationId();
        String lockValue = distributedLockService.tryLock(lockKey, 5, 30);
        
        if (lockValue == null) {
            throw new IllegalStateException("주문 생성 중입니다. 잠시 후 다시 시도해주세요.");
        }

        try {
            // 1. 중복 주문 확인
            if (orderRepository.existsByUserIdAndReservationId(request.getUserId(), request.getReservationId())) {
                throw new IllegalStateException("이미 해당 예약에 대한 주문이 존재합니다.");
            }

            // 2. 쿠폰 검증 및 할인 금액 계산
            BigDecimal discountAmount = BigDecimal.ZERO;
            List<UserCoupon> validCoupons = validateAndGetUserCoupons(
                request.getUserId(), 
                request.getCouponIds(), 
                request.getOriginalAmount()
            );

            for (UserCoupon userCoupon : validCoupons) {
                BigDecimal couponDiscount = calculateCouponDiscount(userCoupon.getCoupon(), request.getOriginalAmount());
                discountAmount = discountAmount.add(couponDiscount);
            }

            // 3. 포인트 사용 검증
            Integer pointsToUse = request.getPointsUsed() != null ? request.getPointsUsed() : 0;
            if (pointsToUse > 0) {
                validatePointUsage(request.getUserId(), pointsToUse, request.getOriginalAmount().subtract(discountAmount));
            }

            // 4. 최종 결제 금액 계산
            BigDecimal finalAmount = request.getOriginalAmount()
                .subtract(discountAmount)
                .subtract(BigDecimal.valueOf(pointsToUse));

            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            // 5. 주문 번호 생성
            String orderNumber = generateOrderNumber();

            // 6. 주문 생성
            Order order = Order.createOrder(
                orderNumber,
                request.getUserId(),
                request.getReservationId(),
                request.getOriginalAmount(),
                discountAmount,
                pointsToUse,
                finalAmount
            );

            order = orderRepository.save(order);

            // 7. 쿠폰 사용 처리 (동시성 안전)
            for (int i = 0; i < validCoupons.size(); i++) {
                UserCoupon userCoupon = validCoupons.get(i);
                BigDecimal couponDiscount = calculateCouponDiscount(userCoupon.getCoupon(), request.getOriginalAmount());
                
                // 사용자 쿠폰 사용 처리
                boolean success = userCoupon.tryUse();
                if (!success) {
                    throw new IllegalStateException("쿠폰 사용에 실패했습니다.");
                }
                userCouponRepository.save(userCoupon);

                // 주문-쿠폰 연결 정보 저장
                OrderCoupon orderCoupon = OrderCoupon.create(order.getId(), userCoupon.getId(), couponDiscount);
                orderCouponRepository.save(orderCoupon);
            }

            // 8. 포인트 사용 처리
            if (pointsToUse > 0) {
                pointService.usePoints(request.getUserId(), pointsToUse, order.getId(), "주문 결제");
            }

            log.info("주문 생성 완료 - orderNumber: {}, finalAmount: {}", orderNumber, finalAmount);
            return OrderDto.Response.fromEntity(order);
            
        } finally {
            distributedLockService.unlock(lockKey, lockValue);
        }
    }

    /**
     * 주문 상세 조회
     */
    @Transactional(readOnly = true)
    public OrderDto.Response getOrder(String orderNumber) {
        Order order = findOrderByNumber(orderNumber);
        OrderDto.Response response = OrderDto.Response.fromEntity(order);

        // 사용된 쿠폰 정보 조회
        List<OrderCoupon> orderCoupons = orderCouponRepository.findByOrderId(order.getId());
        List<OrderCouponDto.Response> couponResponses = orderCoupons.stream()
            .map(OrderCouponDto.Response::fromEntity)
            .collect(Collectors.toList());
        
        response.setUsedCoupons(couponResponses);
        return response;
    }

    /**
     * 사용자별 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<OrderDto.ListResponse> getUserOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return orders.map(OrderDto.ListResponse::fromEntity);
    }

    /**
     * 주문 확정 (동시성 안전)
     */
    @Transactional
    public void confirmOrder(String orderNumber) {
        Order order = findOrderByNumber(orderNumber);
        boolean success = order.tryConfirm();
        if (!success) {
            throw new IllegalStateException("주문 확정에 실패했습니다. 현재 상태: " + order.getStatus());
        }
        orderRepository.save(order);
        log.info("주문 확정 완료 - orderNumber: {}", orderNumber);
    }

    /**
     * 주문 취소 (동시성 안전)
     */
    @Transactional
    public void cancelOrder(String orderNumber, String reason) {
        Order order = findOrderByNumber(orderNumber);
        
        boolean success = order.tryCancel();
        if (!success) {
            throw new IllegalStateException("취소할 수 없는 주문 상태입니다: " + order.getStatus());
        }

        orderRepository.save(order);

        // 사용된 쿠폰 복원
        restoreUsedCoupons(order.getId());
        
        // 사용된 포인트 복원
        if (order.getPointsUsed() > 0) {
            pointService.refundPoints(order.getUserId(), order.getPointsUsed(), order.getId(), reason);
        }

        log.info("주문 취소 완료 - orderNumber: {}, reason: {}", orderNumber, reason);
    }

    /**
     * 주문 완료 처리 (동시성 안전)
     */
    @Transactional
    public void completeOrder(String orderNumber) {
        Order order = findOrderByNumber(orderNumber);
        boolean success = order.tryComplete();
        if (!success) {
            throw new IllegalStateException("주문 완료 처리에 실패했습니다. 현재 상태: " + order.getStatus());
        }
        orderRepository.save(order);

        // 포인트 적립 (결제 금액의 1% 기본 적립)
        if (order.getFinalAmount().compareTo(BigDecimal.ZERO) > 0) {
            Integer earnPoints = order.getFinalAmount()
                .multiply(BigDecimal.valueOf(0.01))
                .setScale(0, RoundingMode.DOWN)
                .intValue();
            
            if (earnPoints > 0) {
                pointService.earnPoints(order.getUserId(), earnPoints, order.getId(), "주문 완료 적립");
            }
        }

        log.info("주문 완료 처리 - orderNumber: {}", orderNumber);
    }

    /**
     * 만료된 주문 처리 (동시성 안전)
     */
    @Transactional
    public void expireOrders() {
        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(LocalDateTime.now());
        
        for (Order order : expiredOrders) {
            boolean success = order.tryExpire();
            if (success) {
                orderRepository.save(order);
                
                // 사용된 쿠폰 복원
                restoreUsedCoupons(order.getId());
                
                // 사용된 포인트 복원
                if (order.getPointsUsed() > 0) {
                    pointService.refundPoints(order.getUserId(), order.getPointsUsed(), 
                                            order.getId(), "주문 만료");
                }
                
                log.info("주문 만료 처리 - orderNumber: {}", order.getOrderNumber());
            }
        }
    }

    // === Private Methods ===

    private List<UserCoupon> validateAndGetUserCoupons(Long userId, List<Long> couponIds, BigDecimal orderAmount) {
        if (couponIds == null || couponIds.isEmpty()) {
            return List.of();
        }

        List<UserCoupon> userCoupons = userCouponRepository.findUsableCouponsForAmount(
            userId, orderAmount.longValue(), LocalDateTime.now()
        );

        List<UserCoupon> selectedCoupons = userCoupons.stream()
            .filter(uc -> couponIds.contains(uc.getCoupon().getId()))
            .collect(Collectors.toList());

        if (selectedCoupons.size() != couponIds.size()) {
            throw new IllegalArgumentException("사용할 수 없는 쿠폰이 포함되어 있습니다.");
        }

        return selectedCoupons;
    }

    private BigDecimal calculateCouponDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount;
        
        if (coupon.getDiscountType() == DiscountType.FIXED) {
            discount = coupon.getDiscountValue();
        } else {
            discount = orderAmount.multiply(coupon.getDiscountValue().divide(BigDecimal.valueOf(100)));
        }

        // 최대 할인 금액 적용
        if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
            discount = coupon.getMaxDiscountAmount();
        }

        return discount;
    }

    private void validatePointUsage(Long userId, Integer pointsToUse, BigDecimal payableAmount) {
        Integer currentBalance = pointService.getCurrentBalance(userId);
        
        if (currentBalance < pointsToUse) {
            throw new IllegalArgumentException("보유 포인트가 부족합니다.");
        }

        // 결제 금액의 50%까지만 사용 가능
        BigDecimal maxUsablePoints = payableAmount.multiply(BigDecimal.valueOf(0.5));
        if (BigDecimal.valueOf(pointsToUse).compareTo(maxUsablePoints) > 0) {
            throw new IllegalArgumentException("포인트는 결제 금액의 50%까지만 사용할 수 있습니다.");
        }

        // 최소 사용 금액 1,000원
        if (pointsToUse < 1000) {
            throw new IllegalArgumentException("포인트는 최소 1,000포인트부터 사용할 수 있습니다.");
        }
    }

    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis();
    }

    private Order findOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다: " + orderNumber));
    }

    private void restoreUsedCoupons(Long orderId) {
        List<OrderCoupon> orderCoupons = orderCouponRepository.findByOrderId(orderId);
        
        for (OrderCoupon orderCoupon : orderCoupons) {
            UserCoupon userCoupon = userCouponRepository.findById(orderCoupon.getUserCouponId())
                .orElseThrow(() -> new EntityNotFoundException("사용자 쿠폰을 찾을 수 없습니다."));
            
            userCoupon.restore();
            userCouponRepository.save(userCoupon);
        }
    }
} 