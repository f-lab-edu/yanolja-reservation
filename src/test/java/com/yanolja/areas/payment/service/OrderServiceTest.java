package com.yanolja.areas.payment.service;

import com.yanolja.areas.payment.dto.OrderDto;
import com.yanolja.areas.payment.entity.*;
import com.yanolja.areas.payment.repository.*;
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
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private UserCouponRepository userCouponRepository;
    
    @Mock
    private OrderCouponRepository orderCouponRepository;
    
    @Mock
    private PointRepository pointRepository;
    
    @Mock
    private CouponService couponService;
    
    @Mock
    private PointService pointService;
    
    @Mock
    private DistributedLockService distributedLockService;

    @InjectMocks
    private OrderService orderService;

    private OrderDto.CreateRequest createRequest;
    private Order mockOrder;
    private UserCoupon mockUserCoupon;
    private Coupon mockCoupon;

    @BeforeEach
    void setUp() {
        createRequest = OrderDto.CreateRequest.builder()
                .userId(1L)
                .reservationId(1L)
                .originalAmount(BigDecimal.valueOf(100000))
                .couponIds(List.of(1L))
                .pointsUsed(5000)
                .build();

        mockOrder = Order.createOrder(
                "ORD123456789",
                1L,
                1L,
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(10000),
                5000,
                BigDecimal.valueOf(85000)
        );
        // Reflection을 사용하여 ID와 기타 필드 설정
        try {
            java.lang.reflect.Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockOrder, 1L);
            
            java.lang.reflect.Field createdAtField = mockOrder.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(mockOrder, LocalDateTime.now());
        } catch (Exception e) {
            // ignore
        }

        mockCoupon = Coupon.createCoupon(
                "WELCOME10",
                "신규 가입 쿠폰",
                "신규 가입자 10% 할인",
                DiscountType.PERCENTAGE,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(50000),
                100,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                CouponIssueType.SIGNUP
        );

        mockUserCoupon = UserCoupon.issueTo(1L, mockCoupon);
        
        // UserCoupon과 Coupon ID 설정
        try {
            // UserCoupon ID 설정
            java.lang.reflect.Field userCouponIdField = UserCoupon.class.getDeclaredField("id");
            userCouponIdField.setAccessible(true);
            userCouponIdField.set(mockUserCoupon, 1L);
            
            // Coupon ID 설정
            java.lang.reflect.Field couponIdField = Coupon.class.getDeclaredField("id");
            couponIdField.setAccessible(true);
            couponIdField.set(mockCoupon, 1L);
        } catch (Exception e) {
            // ignore
        }
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_Success() {
        // given
        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("mock-lock-token");
        given(distributedLockService.unlock(anyString(), anyString())).willReturn(true);
        given(orderRepository.existsByUserIdAndReservationId(1L, 1L)).willReturn(false);
        given(userCouponRepository.findUsableCouponsForAmount(eq(1L), eq(100000L), any(LocalDateTime.class)))
                .willReturn(List.of(mockUserCoupon));
        given(pointService.getCurrentBalance(1L)).willReturn(10000);
        given(orderRepository.save(any(Order.class))).willReturn(mockOrder);
        given(userCouponRepository.save(any(UserCoupon.class))).willReturn(mockUserCoupon);
        OrderCoupon mockOrderCoupon = OrderCoupon.builder()
                .order(mockOrder)
                .userCouponId(1L)
                .discountAmount(BigDecimal.valueOf(10000))
                .build();
        given(orderCouponRepository.save(any(OrderCoupon.class))).willReturn(mockOrderCoupon);

        // when
        OrderDto.Response result = orderService.createOrder(createRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getReservationId()).isEqualTo(1L);
        assertThat(result.getOriginalAmount()).isEqualTo(BigDecimal.valueOf(100000));
        
        verify(orderRepository).save(any(Order.class));
        verify(pointService).usePoints(eq(1L), eq(5000), any(Long.class), eq("주문 결제"));
    }

    @Test
    @DisplayName("중복 주문 생성 실패")
    void createOrder_DuplicateOrder_ThrowsException() {
        // given
        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("mock-lock-token");
        given(distributedLockService.unlock(anyString(), anyString())).willReturn(true);
        given(orderRepository.existsByUserIdAndReservationId(1L, 1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 해당 예약에 대한 주문이 존재합니다.");
    }

    @Test
    @DisplayName("보유 포인트 부족으로 주문 생성 실패")
    void createOrder_InsufficientPoints_ThrowsException() {
        // given
        OrderDto.CreateRequest requestWithoutCoupons = OrderDto.CreateRequest.builder()
                .userId(1L)
                .reservationId(1L)
                .originalAmount(BigDecimal.valueOf(100000))
                .pointsUsed(5000) // 쿠폰 없이 포인트만 사용
                .build();
        
        lenient().when(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).thenReturn("mock-lock-token");
        lenient().when(distributedLockService.unlock(anyString(), anyString())).thenReturn(true);
        lenient().when(orderRepository.existsByUserIdAndReservationId(1L, 1L)).thenReturn(false);
        lenient().when(userCouponRepository.findUsableCouponsForAmount(eq(1L), eq(100000L), any(LocalDateTime.class)))
                .thenReturn(List.of());
        lenient().when(pointService.getCurrentBalance(1L)).thenReturn(3000); // 사용하려는 5000포인트보다 적음

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(requestWithoutCoupons))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("보유 포인트가 부족합니다.");
        
        verify(distributedLockService).unlock(anyString(), anyString());
    }

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrder_Success() {
        // given
        String orderNumber = "ORD123456789";
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(mockOrder));
        given(orderCouponRepository.findByOrder_Id(mockOrder.getId())).willReturn(List.of());

        // when
        OrderDto.Response result = orderService.getOrder(orderNumber);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo(orderNumber);
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 실패")
    void getOrder_NotFound_ThrowsException() {
        // given
        String orderNumber = "INVALID_ORDER";
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(orderNumber))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다: " + orderNumber);
    }

    @Test
    @DisplayName("사용자별 주문 목록 조회 성공")
    void getUserOrders_Success() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> mockPage = new PageImpl<>(List.of(mockOrder));
        
        given(orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).willReturn(mockPage);

        // when
        Page<OrderDto.ListResponse> result = orderService.getUserOrders(userId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Test
    @DisplayName("주문 확정 성공")
    void confirmOrder_Success() {
        // given
        String orderNumber = "ORD123456789";
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(mockOrder));
        given(orderRepository.save(any(Order.class))).willReturn(mockOrder);

        // when
        orderService.confirmOrder(orderNumber);

        // then
        verify(orderRepository).save(mockOrder);
        assertThat(mockOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("주문 취소 성공")
    void cancelOrder_Success() {
        // given
        String orderNumber = "ORD123456789";
        String reason = "고객 변심";
        
        // 주문을 PENDING 상태로 설정
        Order pendingOrder = Order.createOrder(
                orderNumber, 1L, 1L, BigDecimal.valueOf(100000),
                BigDecimal.valueOf(10000), 5000, BigDecimal.valueOf(85000)
        );
        // ID 설정
        try {
            java.lang.reflect.Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pendingOrder, 2L);
        } catch (Exception e) {
            // ignore
        }
        
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(pendingOrder));
        given(orderRepository.save(any(Order.class))).willReturn(pendingOrder);
        given(orderCouponRepository.findByOrder_Id(2L)).willReturn(List.of());

        // when
        orderService.cancelOrder(orderNumber, reason);

        // then
        verify(orderRepository).save(pendingOrder);
        verify(pointService).refundPoints(eq(1L), eq(5000), any(Long.class), eq(reason));
        assertThat(pendingOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("취소 불가능한 주문 상태에서 취소 실패")
    void cancelOrder_InvalidStatus_ThrowsException() {
        // given
        String orderNumber = "ORD123456789";
        String reason = "고객 변심";
        
        // 주문을 COMPLETED 상태로 설정
        Order completedOrder = Order.createOrder(
                orderNumber, 1L, 1L, BigDecimal.valueOf(100000),
                BigDecimal.valueOf(10000), 5000, BigDecimal.valueOf(85000)
        );
        // 먼저 CONFIRMED 상태로 변경한 후 COMPLETED로 변경
        completedOrder.tryConfirm();
        completedOrder.tryComplete();
        
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(completedOrder));

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(orderNumber, reason))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("취소할 수 없는 주문 상태입니다: " + completedOrder.getStatus());
    }

    @Test
    @DisplayName("주문 완료 처리 성공")
    void completeOrder_Success() {
        // given
        String orderNumber = "ORD123456789";
        Order confirmedOrder = Order.createOrder(
                orderNumber, 1L, 1L, BigDecimal.valueOf(100000),
                BigDecimal.valueOf(10000), 5000, BigDecimal.valueOf(85000)
        );
        confirmedOrder.tryConfirm(); // CONFIRMED 상태로 변경
        // ID 설정
        try {
            java.lang.reflect.Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(confirmedOrder, 3L);
        } catch (Exception e) {
            // ignore
        }
        
        given(orderRepository.findByOrderNumber(orderNumber)).willReturn(Optional.of(confirmedOrder));
        given(orderRepository.save(any(Order.class))).willReturn(confirmedOrder);

        // when
        orderService.completeOrder(orderNumber);

        // then
        verify(orderRepository).save(confirmedOrder);
        verify(pointService).earnPoints(eq(1L), eq(850), any(Long.class), eq("주문 완료 적립"));
        assertThat(confirmedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("만료된 주문 처리 성공")
    void expireOrders_Success() {
        // given
        List<Order> expiredOrders = List.of(mockOrder);
        given(orderRepository.findExpiredPendingOrders(any(LocalDateTime.class)))
                .willReturn(expiredOrders);
        given(orderRepository.save(any(Order.class))).willReturn(mockOrder);
        given(orderCouponRepository.findByOrder_Id(mockOrder.getId())).willReturn(List.of());

        // when
        orderService.expireOrders();

        // then
        verify(orderRepository).save(mockOrder);
        verify(pointService).refundPoints(eq(1L), eq(5000), any(Long.class), eq("주문 만료"));
        assertThat(mockOrder.getStatus()).isEqualTo(OrderStatus.EXPIRED);
    }

    @Test
    @DisplayName("포인트 사용 검증 - 최소 사용 금액 미달")
    void validatePointUsage_BelowMinimum_ThrowsException() {
        // given
        OrderDto.CreateRequest invalidRequest = OrderDto.CreateRequest.builder()
                .userId(1L)
                .reservationId(1L)
                .originalAmount(BigDecimal.valueOf(100000))
                .pointsUsed(500) // 최소 1000포인트 미달
                .build();

        lenient().when(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).thenReturn("mock-lock-token");
        lenient().when(distributedLockService.unlock(anyString(), anyString())).thenReturn(true);
        lenient().when(orderRepository.existsByUserIdAndReservationId(1L, 1L)).thenReturn(false);
        lenient().when(userCouponRepository.findUsableCouponsForAmount(eq(1L), eq(100000L), any(LocalDateTime.class)))
                .thenReturn(List.of());
        lenient().when(pointService.getCurrentBalance(1L)).thenReturn(10000);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트는 최소 1,000포인트부터 사용할 수 있습니다.");
    }

    @Test
    @DisplayName("포인트 사용 검증 - 50% 초과 사용")
    void validatePointUsage_ExceedsLimit_ThrowsException() {
        // given
        OrderDto.CreateRequest invalidRequest = OrderDto.CreateRequest.builder()
                .userId(1L)
                .reservationId(1L)
                .originalAmount(BigDecimal.valueOf(100000))
                .pointsUsed(60000) // 50% 초과
                .build();

        lenient().when(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).thenReturn("mock-lock-token");
        lenient().when(distributedLockService.unlock(anyString(), anyString())).thenReturn(true);
        lenient().when(orderRepository.existsByUserIdAndReservationId(1L, 1L)).thenReturn(false);
        lenient().when(userCouponRepository.findUsableCouponsForAmount(eq(1L), eq(100000L), any(LocalDateTime.class)))
                .thenReturn(List.of());
        lenient().when(pointService.getCurrentBalance(1L)).thenReturn(70000);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트는 결제 금액의 50%까지만 사용할 수 있습니다.");
    }
} 