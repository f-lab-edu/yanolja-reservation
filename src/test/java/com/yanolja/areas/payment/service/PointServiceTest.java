package com.yanolja.areas.payment.service;

import com.yanolja.areas.payment.dto.PointDto;
import com.yanolja.areas.payment.entity.Point;
import com.yanolja.areas.payment.entity.PointStatus;
import com.yanolja.areas.payment.entity.PointTransactionType;
import com.yanolja.areas.payment.repository.PointRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointService 단위 테스트")
class PointServiceTest {

    @Mock
    private PointRepository pointRepository;
    
    @Mock
    private DistributedLockService distributedLockService;

    @InjectMocks
    private PointService pointService;

    private Point mockEarnedPoint;
    private Point mockUsedPoint;

    @BeforeEach
    void setUp() {
        mockEarnedPoint = Point.createEarnPoint(
                1L,
                1000,
                1000,
                "주문 적립",
                123L,
                LocalDateTime.now().plusYears(2)
        );

        mockUsedPoint = Point.createUsePoint(
                1L,
                500,
                500,
                "주문 사용",
                456L
        );
    }

    @Test
    @DisplayName("포인트 적립 성공")
    void earnPoints_Success() {
        // given
        Long userId = 1L;
        int amount = 1000;
        Long sourceId = 123L;
        String description = "주문 적립";

        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("lockValue");
        given(pointRepository.save(any(Point.class))).willReturn(mockEarnedPoint);

        // when
        Point result = pointService.earnPoints(userId, amount, sourceId, description);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAmount()).isEqualTo(amount);
        assertThat(result.getTransactionType()).isEqualTo(PointTransactionType.EARN);
        assertThat(result.getDescription()).isEqualTo(description);
        
        verify(pointRepository).save(any(Point.class));
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void usePoints_Success() {
        // given
        Long userId = 1L;
        Integer amount = 500;
        Long sourceId = 456L;
        String description = "주문 사용";

        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("lockValue");
        given(pointRepository.getCurrentPointBalance(userId, PointStatus.ACTIVE)).willReturn(1000);
        given(pointRepository.save(any(Point.class))).willReturn(mockUsedPoint);

        // when
        Point result = pointService.usePoints(userId, amount, sourceId, description);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getAmount()).isEqualTo(-amount); // 사용은 음수로 저장
        assertThat(result.getTransactionType()).isEqualTo(PointTransactionType.USE);
        assertThat(result.getDescription()).isEqualTo(description);
        
        verify(pointRepository).save(any(Point.class));
    }

    @Test
    @DisplayName("보유 포인트 부족으로 사용 실패")
    void usePoints_InsufficientBalance_ThrowsException() {
        // given
        Long userId = 1L;
        Integer amount = 1500; // 보유량보다 많은 포인트
        Long sourceId = 456L;
        String description = "주문 사용";

        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("lockValue");
        given(pointRepository.getCurrentPointBalance(userId, PointStatus.ACTIVE)).willReturn(1000);

        // when & then
        assertThatThrownBy(() -> pointService.usePoints(userId, amount, sourceId, description))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용 가능한 포인트가 부족합니다.");
    }

    @Test
    @DisplayName("현재 포인트 잔액 조회 성공")
    void getCurrentBalance_Success() {
        // given
        Long userId = 1L;
        given(pointRepository.getCurrentPointBalance(userId, PointStatus.ACTIVE)).willReturn(1500);

        // when
        Integer balance = pointService.getCurrentBalance(userId);

        // then
        assertThat(balance).isEqualTo(1500);
        verify(pointRepository).getCurrentPointBalance(userId, PointStatus.ACTIVE);
    }

    @Test
    @DisplayName("포인트 히스토리 조회 성공")
    void getUserPointHistory_Success() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Point> points = List.of(mockEarnedPoint, mockUsedPoint);
        Page<Point> mockPage = new PageImpl<>(points);

        given(pointRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).willReturn(mockPage);

        // when
        Page<Point> result = pointService.getUserPointHistory(userId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).containsExactly(mockEarnedPoint, mockUsedPoint);
        
        verify(pointRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Test
    @DisplayName("기간별 포인트 히스토리 조회 성공")
    void getPointHistoryByDateRange_Success() {
        // given
        Long userId = 1L;
        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();
        PointTransactionType transactionType = PointTransactionType.EARN;
        List<Point> points = List.of(mockEarnedPoint);

        given(pointRepository.findPointHistoryByDateRange(userId, transactionType, from, to))
                .willReturn(points);

        // when
        List<Point> result = pointService.getPointHistoryByDateRange(userId, transactionType, from, to);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(mockEarnedPoint);
        
        verify(pointRepository).findPointHistoryByDateRange(userId, transactionType, from, to);
    }

    @Test
    @DisplayName("만료 예정 포인트 조회 성공")
    void getExpiringPoints_Success() {
        // given
        Long userId = 1L;
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().plusDays(30);
        List<Point> expiringPoints = List.of(mockEarnedPoint);

        given(pointRepository.findUserExpiringPoints(userId, startTime, endTime)).willReturn(expiringPoints);

        // when
        List<Point> result = pointService.getExpiringPoints(userId, startTime, endTime);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result).containsExactly(mockEarnedPoint);
        
        verify(pointRepository).findUserExpiringPoints(userId, startTime, endTime);
    }

    @Test
    @DisplayName("만료된 포인트 처리 성공")
    void expirePoints_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        List<Point> expiredPoints = List.of(mockEarnedPoint);

        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("lockValue");
        given(pointRepository.findExpiringPoints(
                eq(PointStatus.ACTIVE),
                eq(PointTransactionType.EARN),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).willReturn(expiredPoints);
        given(pointRepository.save(any(Point.class))).willReturn(mockEarnedPoint);

        // when
        pointService.expirePoints();

        // then
        verify(pointRepository).findExpiringPoints(
                eq(PointStatus.ACTIVE),
                eq(PointTransactionType.EARN),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
        verify(pointRepository, times(expiredPoints.size())).save(any(Point.class));
    }

    @Test
    @DisplayName("포인트 환불 성공")
    void refundPoints_Success() {
        // given
        Long userId = 1L;
        Integer amount = 500;
        Long orderId = 789L;
        String description = "주문 취소로 인한 환불";

        // 환불용 Mock Point 객체 생성
        Point mockRefundPoint = Point.createEarnPoint(
                userId,
                amount,
                1500, // newBalance
                description,
                orderId,
                LocalDateTime.now().plusYears(2)
        );

        given(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).willReturn("lockValue");
        given(pointRepository.getCurrentPointBalance(userId, PointStatus.ACTIVE)).willReturn(1000);
        given(pointRepository.save(any(Point.class))).willReturn(mockRefundPoint);

        // when
        Point result = pointService.refundPoints(userId, amount, orderId, description);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTransactionType()).isEqualTo(PointTransactionType.EARN);
        assertThat(result.getDescription()).contains("환불");
        
        verify(pointRepository).save(any(Point.class));
    }

    @Test
    @DisplayName("총 적립 포인트 조회 성공")
    void getTotalEarnedPoints_Success() {
        // given
        Long userId = 1L;
        given(pointRepository.getTotalEarnedPoints(userId, PointTransactionType.EARN, PointStatus.ACTIVE))
                .willReturn(50000);

        // when
        Integer totalEarned = pointService.getTotalEarnedPoints(userId);

        // then
        assertThat(totalEarned).isEqualTo(50000);
        verify(pointRepository).getTotalEarnedPoints(userId, PointTransactionType.EARN, PointStatus.ACTIVE);
    }

    @Test
    @DisplayName("주문별 포인트 내역 조회 성공")
    void getPointsByOrder_Success() {
        // given
        Long orderId = 123L;
        List<Point> orderPoints = List.of(mockEarnedPoint, mockUsedPoint);

        given(pointRepository.findByOrderId(orderId)).willReturn(orderPoints);

        // when
        List<Point> result = pointService.getPointsByOrder(orderId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(mockEarnedPoint, mockUsedPoint);
        
        verify(pointRepository).findByOrderId(orderId);
    }

    @Test
    @DisplayName("포인트 통계 조회 성공")
    void getPointStatistics_Success() {
        // given
        Long userId = 1L;
        
        // Mock 통계 데이터
        PointDto.PointStatistics mockStatistics = PointDto.PointStatistics.builder()
                .userId(userId)
                .currentBalance(5000)
                .totalEarned(10000)
                .monthlyEarned(2000)
                .expiringAmount(1000)
                .build();

        given(pointRepository.getCurrentPointBalance(userId, PointStatus.ACTIVE)).willReturn(5000);
        given(pointRepository.getTotalEarnedPoints(userId, PointTransactionType.EARN, PointStatus.ACTIVE)).willReturn(10000);

        // when
        PointDto.PointStatistics result = pointService.getPointStatistics(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getCurrentBalance()).isEqualTo(5000);
        assertThat(result.getTotalEarned()).isEqualTo(10000);
        
        verify(pointRepository).getCurrentPointBalance(userId, PointStatus.ACTIVE);
        verify(pointRepository).getTotalEarnedPoints(userId, PointTransactionType.EARN, PointStatus.ACTIVE);
    }
}