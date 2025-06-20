package com.yanolja.areas.payment.service;

import com.yanolja.areas.payment.dto.CouponDto;
import com.yanolja.areas.payment.entity.*;
import com.yanolja.areas.payment.repository.CouponRepository;
import com.yanolja.areas.payment.repository.UserCouponRepository;
import com.yanolja.common.service.DistributedLockService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService 단위 테스트")
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;
    
    @Mock
    private UserCouponRepository userCouponRepository;
    
    @Mock
    private DistributedLockService distributedLockService;

    @InjectMocks
    private CouponService couponService;

    private CouponDto.CreateCouponRequest createRequest;
    private Coupon mockCoupon;
    private UserCoupon mockUserCoupon;

    @BeforeEach
    void setUp() {
        createRequest = CouponDto.CreateCouponRequest.builder()
                .code("WELCOME10")
                .name("신규 가입 쿠폰")
                .description("신규 가입자 10% 할인")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(BigDecimal.valueOf(10))
                .maxDiscountAmount(BigDecimal.valueOf(10000))
                .minOrderAmount(BigDecimal.valueOf(50000))
                .issueCount(100)
                .issueStartAt(LocalDateTime.now())
                .issueEndAt(LocalDateTime.now().plusDays(30))
                .validFrom(LocalDateTime.now())
                .validUntil(LocalDateTime.now().plusDays(60))
                .issueType(CouponIssueType.SIGNUP)
                .build();

        mockCoupon = Coupon.createCoupon(
                "WELCOME10",
                "신규 가입 쿠폰",
                "신규 가입자 10% 할인",
                DiscountType.PERCENTAGE,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(50000),
                100,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(30),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(60),
                CouponIssueType.SIGNUP
        );

        mockUserCoupon = UserCoupon.issueTo(1L, mockCoupon);
    }

    @Test
    @DisplayName("쿠폰 생성 성공")
    void createCoupon_Success() {
        // given
        given(couponRepository.existsByCode("WELCOME10")).willReturn(false);
        given(couponRepository.save(any(Coupon.class))).willReturn(mockCoupon);

        // when
        Coupon result = couponService.createCoupon(createRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("WELCOME10");
        assertThat(result.getName()).isEqualTo("신규 가입 쿠폰");
        assertThat(result.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
        assertThat(result.getDiscountValue()).isEqualTo(BigDecimal.valueOf(10));
        
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    @DisplayName("중복 쿠폰 코드로 생성 실패")
    void createCoupon_DuplicateCode_ThrowsException() {
        // given
        given(couponRepository.existsByCode("WELCOME10")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> couponService.createCoupon(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 쿠폰 코드입니다: WELCOME10");
    }

    @Test
    @DisplayName("사용자에게 쿠폰 발급 성공")
    void issueCouponToUser_Success() {
        // given
        Long userId = 1L;
        String couponCode = "WELCOME10";
        
        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("lockValue");
        given(couponRepository.findByCode(couponCode)).willReturn(Optional.of(mockCoupon));
        given(userCouponRepository.existsByUserIdAndCouponId(userId, mockCoupon.getId())).willReturn(false);
        given(couponRepository.save(any(Coupon.class))).willReturn(mockCoupon);
        given(userCouponRepository.save(any(UserCoupon.class))).willReturn(mockUserCoupon);

        // when
        UserCoupon result = couponService.issueCouponToUser(userId, couponCode);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getCoupon()).isEqualTo(mockCoupon);
        
        verify(couponRepository).save(mockCoupon); // 발급 수량 증가
        verify(userCouponRepository).save(any(UserCoupon.class));
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 코드로 발급 실패")
    void issueCouponToUser_CouponNotFound_ThrowsException() {
        // given
        Long userId = 1L;
        String invalidCouponCode = "INVALID_CODE";
        
        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("lockValue");
        given(couponRepository.findByCode(invalidCouponCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.issueCouponToUser(userId, invalidCouponCode))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("쿠폰을 찾을 수 없습니다: " + invalidCouponCode);
    }

    @Test
    @DisplayName("이미 발급받은 쿠폰 재발급 실패")
    void issueCouponToUser_AlreadyIssued_ThrowsException() {
        // given
        Long userId = 1L;
        String couponCode = "WELCOME10";
        
        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("lockValue");
        given(couponRepository.findByCode(couponCode)).willReturn(Optional.of(mockCoupon));
        given(userCouponRepository.existsByUserIdAndCouponId(userId, mockCoupon.getId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> couponService.issueCouponToUser(userId, couponCode))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 발급받은 쿠폰입니다.");
    }

    @Test
    @DisplayName("발급 수량 소진된 쿠폰 발급 실패")
    void issueCouponToUser_SoldOut_ThrowsException() {
        // given
        Long userId = 1L;
        String couponCode = "WELCOME10";
        
        // 발급 수량이 모두 소진된 쿠폰 생성
        Coupon soldOutCoupon = Coupon.createCoupon(
                "WELCOME10", "신규 가입 쿠폰", "신규 가입자 10% 할인",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), BigDecimal.valueOf(10000),
                BigDecimal.valueOf(50000), 100,
                LocalDateTime.now(), LocalDateTime.now().plusDays(30),
                LocalDateTime.now(), LocalDateTime.now().plusDays(60),
                CouponIssueType.SIGNUP
        );
        
        // 발급 수량을 최대치로 설정
        for (int i = 0; i < 100; i++) {
            soldOutCoupon.tryIncreaseUsedCount();
        }
        
        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("lockValue");
        given(couponRepository.findByCode(couponCode)).willReturn(Optional.of(soldOutCoupon));
        given(userCouponRepository.existsByUserIdAndCouponId(userId, soldOutCoupon.getId())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> couponService.issueCouponToUser(userId, couponCode))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("쿠폰 발급 수량이 소진되었습니다.");
    }

    @Test
    @DisplayName("발급 기간이 아닌 쿠폰 발급 실패")
    void issueCouponToUser_OutOfIssueTime_ThrowsException() {
        // given
        Long userId = 1L;
        String couponCode = "WELCOME10";
        
        // 발급 기간이 지난 쿠폰 생성
        Coupon expiredIssueCoupon = Coupon.createCoupon(
                "WELCOME10", "신규 가입 쿠폰", "신규 가입자 10% 할인",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), BigDecimal.valueOf(10000),
                BigDecimal.valueOf(50000), 100,
                LocalDateTime.now().minusDays(30), // 과거 시작일
                LocalDateTime.now().minusDays(1),  // 과거 종료일
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                CouponIssueType.SIGNUP
        );
        
        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("lockValue");
        given(couponRepository.findByCode(couponCode)).willReturn(Optional.of(expiredIssueCoupon));
        given(userCouponRepository.existsByUserIdAndCouponId(userId, expiredIssueCoupon.getId())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> couponService.issueCouponToUser(userId, couponCode))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("쿠폰 발급 기간이 아닙니다.");
    }

    @Test
    @DisplayName("자동 쿠폰 발급 성공")
    void issueAutomaticCoupons_Success() {
        // given
        Long userId = 1L;
        CouponIssueType issueType = CouponIssueType.SIGNUP;
        
        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("lockValue");
        given(couponRepository.findAvailableForIssue(eq(CouponStatus.ACTIVE), any(LocalDateTime.class)))
                .willReturn(List.of(mockCoupon));
        given(userCouponRepository.existsByUserIdAndCouponId(userId, mockCoupon.getId())).willReturn(false);
        given(couponRepository.findByCode(mockCoupon.getCode())).willReturn(Optional.of(mockCoupon));
        given(couponRepository.save(any(Coupon.class))).willReturn(mockCoupon);
        given(userCouponRepository.save(any(UserCoupon.class))).willReturn(mockUserCoupon);

        // when
        couponService.issueAutomaticCoupons(userId, issueType);

        // then
        verify(couponRepository).save(mockCoupon);
        verify(userCouponRepository).save(any(UserCoupon.class));
    }

    @Test
    @DisplayName("사용자의 사용 가능한 쿠폰 목록 조회 성공")
    void getUserAvailableCoupons_Success() {
        // given
        Long userId = 1L;
        
        given(userCouponRepository.findAvailableUserCoupons(eq(userId), eq(UserCouponStatus.AVAILABLE), any(LocalDateTime.class)))
                .willReturn(List.of(mockUserCoupon));

        // when
        List<UserCoupon> result = couponService.getUserAvailableCoupons(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(mockUserCoupon);
        
        verify(userCouponRepository).findAvailableUserCoupons(eq(userId), eq(UserCouponStatus.AVAILABLE), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("특정 주문 금액에 사용 가능한 쿠폰 조회 성공")
    void getUsableCouponsForOrder_Success() {
        // given
        Long userId = 1L;
        BigDecimal orderAmount = BigDecimal.valueOf(100000);
        
        given(userCouponRepository.findUsableCouponsForAmount(eq(userId), eq(orderAmount.longValue()), any(LocalDateTime.class)))
                .willReturn(List.of(mockUserCoupon));

        // when
        List<UserCoupon> result = couponService.getUsableCouponsForOrder(userId, orderAmount);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(mockUserCoupon);
        
        verify(userCouponRepository).findUsableCouponsForAmount(eq(userId), eq(orderAmount.longValue()), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("쿠폰 목록 조회 성공")
    void getAllCoupons_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Coupon> mockPage = new PageImpl<>(List.of(mockCoupon));
        
        given(couponRepository.findAllByOrderByCreatedAtDesc(pageable)).willReturn(mockPage);

        // when
        Page<Coupon> result = couponService.getAllCoupons(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(mockCoupon);
        
        verify(couponRepository).findAllByOrderByCreatedAtDesc(pageable);
    }

    @Test
    @DisplayName("쿠폰 검색 성공")
    void searchCoupons_Success() {
        // given
        String keyword = "신규";
        
        given(couponRepository.searchCoupons(keyword)).willReturn(List.of(mockCoupon));

        // when
        List<Coupon> result = couponService.searchCoupons(keyword);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(mockCoupon);
        
        verify(couponRepository).searchCoupons(keyword);
    }

    @Test
    @DisplayName("쿠폰 상태 변경 성공")
    void updateCouponStatus_Success() {
        // given
        Long couponId = 1L;
        CouponStatus newStatus = CouponStatus.INACTIVE;
        
        given(couponRepository.findById(couponId)).willReturn(Optional.of(mockCoupon));
        given(couponRepository.save(any(Coupon.class))).willReturn(mockCoupon);

        // when
        couponService.updateCouponStatus(couponId, newStatus);

        // then
        verify(couponRepository).save(mockCoupon);
        assertThat(mockCoupon.getStatus()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰 상태 변경 실패")
    void updateCouponStatus_CouponNotFound_ThrowsException() {
        // given
        Long invalidCouponId = 999L;
        CouponStatus newStatus = CouponStatus.INACTIVE;
        
        given(couponRepository.findById(invalidCouponId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.updateCouponStatus(invalidCouponId, newStatus))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("쿠폰을 찾을 수 없습니다: " + invalidCouponId);
    }

    @Test
    @DisplayName("만료된 쿠폰 처리 성공")
    void expireCoupons_Success() {
        // given
        List<UserCoupon> expiredUserCoupons = List.of(mockUserCoupon);
        
        given(userCouponRepository.findExpiringUserCoupons(eq(UserCouponStatus.AVAILABLE), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(expiredUserCoupons);
        given(userCouponRepository.save(any(UserCoupon.class))).willReturn(mockUserCoupon);

        // when
        couponService.expireCoupons();

        // then
        verify(userCouponRepository).save(mockUserCoupon);
    }

    @Test
    @DisplayName("쿠폰 통계 조회 성공")
    void getCouponStatistics_Success() {
        // given
        Long couponId = 1L;
        given(couponRepository.findById(couponId)).willReturn(Optional.of(mockCoupon));
        given(userCouponRepository.countByUserIdAndStatus(null, UserCouponStatus.AVAILABLE)).willReturn(50L);
        given(userCouponRepository.countByUserIdAndStatus(null, UserCouponStatus.USED)).willReturn(30L);
        given(userCouponRepository.countByUserIdAndStatus(null, UserCouponStatus.EXPIRED)).willReturn(20L);

        // when
        CouponDto.CouponStatistics result = couponService.getCouponStatistics(couponId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCouponId()).isEqualTo(couponId);
        assertThat(result.getAvailableCount()).isEqualTo(50L);
        assertThat(result.getUsedCount()).isEqualTo(30L);
        assertThat(result.getExpiredCount()).isEqualTo(20L);
    }

    @Test
    @DisplayName("쿠폰 할인 금액 계산 - 정액 할인")
    void calculateDiscount_FixedAmount_Success() {
        // given
        Coupon fixedCoupon = Coupon.createCoupon(
                "FIXED5000", "정액 할인 쿠폰", "5000원 할인",
                DiscountType.FIXED, BigDecimal.valueOf(5000), null,
                BigDecimal.valueOf(30000), 100,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30),
                CouponIssueType.EVENT
        );
        
        BigDecimal orderAmount = BigDecimal.valueOf(100000);

        // when
        BigDecimal discount = fixedCoupon.calculateDiscountAmount(orderAmount);

        // then
        assertThat(discount).isEqualTo(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("쿠폰 할인 금액 계산 - 정률 할인")
    void calculateDiscount_PercentageAmount_Success() {
        // given
        BigDecimal orderAmount = BigDecimal.valueOf(100000);

        // when
        BigDecimal discount = mockCoupon.calculateDiscountAmount(orderAmount);

        // then
        // 10% 할인이지만 최대 할인 금액 10,000원으로 제한
        assertThat(discount.compareTo(BigDecimal.valueOf(10000))).isEqualTo(0);
    }

    @Test
    @DisplayName("쿠폰 할인 금액 계산 - 최소 주문 금액 미달")
    void calculateDiscount_BelowMinimumOrder_ReturnsZero() {
        // given
        BigDecimal orderAmount = BigDecimal.valueOf(30000); // 최소 주문 금액 50,000원 미달

        // when
        BigDecimal discount = mockCoupon.calculateDiscountAmount(orderAmount);

        // then
        assertThat(discount).isEqualTo(BigDecimal.ZERO);
    }
} 