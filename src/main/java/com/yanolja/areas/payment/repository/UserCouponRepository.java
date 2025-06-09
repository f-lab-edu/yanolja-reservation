package com.yanolja.areas.payment.repository;

import com.yanolja.areas.payment.entity.UserCoupon;
import com.yanolja.areas.payment.entity.UserCouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long>, UserCouponRepositoryCustom {

    /**
     * 사용자별 쿠폰 목록 조회
     */
    Page<UserCoupon> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자별 특정 상태 쿠폰 조회
     */
    List<UserCoupon> findByUserIdAndStatus(Long userId, UserCouponStatus status);

    /**
     * 사용자-쿠폰 중복 발급 확인
     */
    boolean existsByUserIdAndCouponId(Long userId, Long couponId);

    /**
     * 특정 쿠폰의 사용자별 보유 여부 확인
     */
    Optional<UserCoupon> findByUserIdAndCouponIdAndStatus(Long userId, Long couponId, UserCouponStatus status);
} 