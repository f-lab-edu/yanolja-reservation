package com.yanolja.areas.payment.repository;

import com.yanolja.areas.payment.entity.Coupon;
import com.yanolja.areas.payment.entity.CouponIssueType;
import com.yanolja.areas.payment.entity.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponRepositoryCustom {

    /**
     * 쿠폰 코드로 쿠폰 조회
     */
    Optional<Coupon> findByCode(String code);

    /**
     * 활성 상태 쿠폰 목록 조회
     */
    List<Coupon> findByStatusOrderByCreatedAtDesc(CouponStatus status);

    /**
     * 발급 타입별 쿠폰 조회
     */
    List<Coupon> findByIssueType(CouponIssueType issueType);

    /**
     * 쿠폰 코드 존재 여부 확인
     */
    boolean existsByCode(String code);

    /**
     * 관리자용 쿠폰 목록 조회 (페이징)
     */
    Page<Coupon> findAllByOrderByCreatedAtDesc(Pageable pageable);
} 