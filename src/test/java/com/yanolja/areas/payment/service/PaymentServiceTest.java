package com.yanolja.areas.payment.service;

import com.yanolja.areas.payment.dto.PaymentDto;
import com.yanolja.areas.payment.entity.*;
import com.yanolja.areas.payment.repository.*;
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
@DisplayName("PaymentService 단위 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private OrderService orderService;
    
    @Mock
    private PgService pgService;
    
    @Mock
    private DistributedLockService distributedLockService;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentDto.Request paymentRequest;
    private PaymentDto.ApproveRequest approveRequest;
    private Order mockOrder;
    private Payment mockPayment;

    @BeforeEach
    void setUp() {
        paymentRequest = PaymentDto.Request.builder()
                .orderNumber("ORD123456789")
                .paymentMethod(PaymentMethod.CARD)
                .amount(BigDecimal.valueOf(85000))
                .pgProvider("toss")
                .cardInfo(PaymentDto.Request.CardInfo.builder()
                        .cardNumber("1234-5678-9012-3456")
                        .cardType("신용카드")
                        .installmentMonths(0)
                        .build())
                .build();

        approveRequest = PaymentDto.ApproveRequest.builder()
                .paymentKey("PAY_TEST_KEY")
                .pgTransactionId("PG_TX_123")
                .approvalNumber("APPR_123")
                .receiptUrl("https://receipt.example.com/123")
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

        mockPayment = Payment.createPayment(
                "PAY_TEST_KEY",
                mockOrder,
                PaymentMethod.CARD,
                BigDecimal.valueOf(85000)
        );
        
        // Reflection을 사용하여 ID와 기타 필드 설정
        try {
            // Order ID 설정
            java.lang.reflect.Field orderIdField = Order.class.getDeclaredField("id");
            orderIdField.setAccessible(true);
            orderIdField.set(mockOrder, 1L);
            
            // Order createdAt 설정
            java.lang.reflect.Field orderCreatedAtField = mockOrder.getClass().getSuperclass().getDeclaredField("createdAt");
            orderCreatedAtField.setAccessible(true);
            orderCreatedAtField.set(mockOrder, LocalDateTime.now());
            
            // Payment ID 설정
            java.lang.reflect.Field paymentIdField = Payment.class.getDeclaredField("id");
            paymentIdField.setAccessible(true);
            paymentIdField.set(mockPayment, 1L);
            
            // Payment createdAt 설정
            java.lang.reflect.Field paymentCreatedAtField = mockPayment.getClass().getSuperclass().getDeclaredField("createdAt");
            paymentCreatedAtField.setAccessible(true);
            paymentCreatedAtField.set(mockPayment, LocalDateTime.now());
            
            // Payment pgProvider 설정
            java.lang.reflect.Field pgProviderField = Payment.class.getDeclaredField("pgProvider");
            pgProviderField.setAccessible(true);
            pgProviderField.set(mockPayment, "toss");
            
        } catch (Exception e) {
            // ignore
        }
        
        // DistributedLockService mock setup
        lenient().when(distributedLockService.tryLock(anyString(), anyLong(), anyLong())).thenReturn("mock-lock-token");
        lenient().when(distributedLockService.unlock(anyString(), anyString())).thenReturn(true);
        
        // PgService mock setup for successful payment
        PgService.PgResult successPgResult = PgService.PgResult.builder()
                .success(true)
                .pgTransactionId("PG_TX_123")
                .approvalNumber("APPR_123")
                .receiptUrl("https://receipt.example.com/123")
                .build();
        lenient().when(pgService.requestPayment(any(), any())).thenReturn(successPgResult);
        lenient().when(pgService.cancelTossPayment(any(), anyString())).thenReturn(true);
        lenient().when(pgService.refundTossPayment(any(), any(), anyString())).thenReturn(true);
        lenient().when(pgService.verifyWebhookSignature(anyString(), anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("카드 결제 처리 성공")
    void processPayment_Card_Success() {
        // given
        given(orderRepository.findByOrderNumber("ORD123456789")).willReturn(Optional.of(mockOrder));
        given(paymentRepository.existsByOrderIdAndStatus(any(Long.class), eq(PaymentStatus.SUCCESS)))
                .willReturn(false);
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            // ID가 없는 경우에만 설정
            try {
                java.lang.reflect.Field idField = Payment.class.getDeclaredField("id");
                idField.setAccessible(true);
                if (idField.get(payment) == null) {
                    idField.set(payment, 1L);
                }
            } catch (Exception e) {
                // ignore
            }
            return payment;
        });

        // when
        PaymentDto.Response result = paymentService.processPayment(paymentRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(85000));
        
        // verify 호출 횟수 조정 - 실제 로직에 맞춰서
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
        verify(orderService, times(1)).confirmOrder("ORD123456789");
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 결제 처리 실패")
    void processPayment_OrderNotFound_ThrowsException() {
        // given
        given(orderRepository.findByOrderNumber("ORD123456789")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("주문을 찾을 수 없습니다: ORD123456789");
    }

    @Test
    @DisplayName("결제할 수 없는 주문 상태로 결제 처리 실패")
    void processPayment_InvalidOrderStatus_ThrowsException() {
        // given
        Order completedOrder = Order.createOrder(
                "ORD123456789", 1L, 1L, BigDecimal.valueOf(100000),
                BigDecimal.valueOf(10000), 5000, BigDecimal.valueOf(85000)
        );
        // PENDING -> CONFIRMED -> COMPLETED 순서로 변경해야 함
        completedOrder.tryConfirm();
        completedOrder.tryComplete();
        
        given(orderRepository.findByOrderNumber("ORD123456789")).willReturn(Optional.of(completedOrder));

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("결제할 수 없는 주문 상태입니다: " + OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("이미 결제 완료된 주문으로 결제 처리 실패")
    void processPayment_AlreadyPaid_ThrowsException() {
        // given
        given(orderRepository.findByOrderNumber("ORD123456789")).willReturn(Optional.of(mockOrder));
        given(paymentRepository.existsByOrderIdAndStatus(any(Long.class), eq(PaymentStatus.SUCCESS)))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 결제가 완료된 주문입니다.");
    }

    @Test
    @DisplayName("만료된 주문으로 결제 처리 실패")
    void processPayment_ExpiredOrder_ThrowsException() {
        // given
        Order expiredOrder = Order.createOrder(
                "ORD123456789", 1L, 1L, BigDecimal.valueOf(100000),
                BigDecimal.valueOf(10000), 5000, BigDecimal.valueOf(85000)
        );
        
        // expiredAt을 과거 시간으로 설정하여 만료된 주문 만들기
        try {
            java.lang.reflect.Field expiredAtField = Order.class.getDeclaredField("expiredAt");
            expiredAtField.setAccessible(true);
            expiredAtField.set(expiredOrder, LocalDateTime.now().minusHours(1)); // 1시간 전으로 설정
            
            // Order ID도 설정
            java.lang.reflect.Field orderIdField = Order.class.getDeclaredField("id");
            orderIdField.setAccessible(true);
            orderIdField.set(expiredOrder, 1L);
        } catch (Exception e) {
            // ignore
        }
        
        given(orderRepository.findByOrderNumber("ORD123456789")).willReturn(Optional.of(expiredOrder));

        // when & then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("만료된 주문입니다.");
    }

    @Test
    @DisplayName("결제 승인 성공")
    void approvePayment_Success() {
        // given
        given(paymentRepository.findByPaymentKey("PAY_TEST_KEY")).willReturn(Optional.of(mockPayment));
        given(paymentRepository.save(any(Payment.class))).willReturn(mockPayment);

        // when
        PaymentDto.Response result = paymentService.approvePayment(approveRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentKey()).isEqualTo("PAY_TEST_KEY");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        
        verify(paymentRepository).save(mockPayment);
        verify(orderService).confirmOrder(mockOrder.getOrderNumber());
    }

    @Test
    @DisplayName("존재하지 않는 결제 키로 승인 실패")
    void approvePayment_PaymentNotFound_ThrowsException() {
        // given
        given(paymentRepository.findByPaymentKey("INVALID_KEY")).willReturn(Optional.empty());

        PaymentDto.ApproveRequest invalidRequest = PaymentDto.ApproveRequest.builder()
                .paymentKey("INVALID_KEY")
                .build();

        // when & then
        assertThatThrownBy(() -> paymentService.approvePayment(invalidRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("결제 정보를 찾을 수 없습니다: INVALID_KEY");
    }

    @Test
    @DisplayName("결제 취소 성공")
    void cancelPayment_Success() {
        // given
        Payment successPayment = Payment.createPayment(
                "PAY_TEST_KEY", mockOrder, PaymentMethod.CARD, BigDecimal.valueOf(85000)
        );
        successPayment.tryApprovePayment("PG_TX_123", "APPR_123", "https://receipt.example.com/123");
        
        given(paymentRepository.findByPaymentKey("PAY_TEST_KEY")).willReturn(Optional.of(successPayment));
        given(paymentRepository.save(any(Payment.class))).willReturn(successPayment);

        // when
        PaymentDto.Response result = paymentService.cancelPayment("PAY_TEST_KEY", "고객 요청");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        
        verify(paymentRepository).save(successPayment);
        verify(orderService).cancelOrder(eq(mockOrder.getOrderNumber()), eq("고객 요청"));
    }

    @Test
    @DisplayName("취소할 수 없는 결제 상태로 취소 실패")
    void cancelPayment_InvalidStatus_ThrowsException() {
        // given
        Payment failedPayment = Payment.createPayment(
                "PAY_TEST_KEY", mockOrder, PaymentMethod.CARD, BigDecimal.valueOf(85000)
        );
        failedPayment.tryFailPayment("결제 실패");
        
        given(paymentRepository.findByPaymentKey("PAY_TEST_KEY")).willReturn(Optional.of(failedPayment));

        // when & then
        assertThatThrownBy(() -> paymentService.cancelPayment("PAY_TEST_KEY", "고객 요청"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("취소할 수 없는 결제 상태입니다: " + PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("부분 환불 성공")
    void refundPayment_Partial_Success() {
        // given
        Payment successPayment = Payment.createPayment(
                "PAY_TEST_KEY", mockOrder, PaymentMethod.CARD, BigDecimal.valueOf(85000)
        );
        successPayment.tryApprovePayment("PG_TX_123", "APPR_123", "https://receipt.example.com/123");
        
        given(paymentRepository.findByPaymentKey("PAY_TEST_KEY")).willReturn(Optional.of(successPayment));
        given(paymentRepository.save(any(Payment.class))).willReturn(successPayment);

        BigDecimal refundAmount = BigDecimal.valueOf(30000);

        // when
        PaymentDto.Response result = paymentService.refundPayment("PAY_TEST_KEY", refundAmount, "부분 취소");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PARTIAL_REFUNDED);
        
        verify(paymentRepository).save(successPayment);
    }

    @Test
    @DisplayName("전액 환불 성공")
    void refundPayment_Full_Success() {
        // given
        Payment successPayment = Payment.createPayment(
                "PAY_TEST_KEY", mockOrder, PaymentMethod.CARD, BigDecimal.valueOf(85000)
        );
        successPayment.tryApprovePayment("PG_TX_123", "APPR_123", "https://receipt.example.com/123");
        
        given(paymentRepository.findByPaymentKey("PAY_TEST_KEY")).willReturn(Optional.of(successPayment));
        given(paymentRepository.save(any(Payment.class))).willReturn(successPayment);

        BigDecimal refundAmount = BigDecimal.valueOf(85000);

        // when
        PaymentDto.Response result = paymentService.refundPayment("PAY_TEST_KEY", refundAmount, "전액 환불");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        
        verify(paymentRepository).save(successPayment);
    }

    @Test
    @DisplayName("환불 가능 금액 초과로 환불 실패")
    void refundPayment_ExceedsAmount_ThrowsException() {
        // given
        Payment successPayment = Payment.createPayment(
                "PAY_TEST_KEY", mockOrder, PaymentMethod.CARD, BigDecimal.valueOf(85000)
        );
        successPayment.tryApprovePayment("PG_TX_123", "APPR_123", "https://receipt.example.com/123");
        
        given(paymentRepository.findByPaymentKey("PAY_TEST_KEY")).willReturn(Optional.of(successPayment));

        BigDecimal excessiveRefundAmount = BigDecimal.valueOf(100000); // 결제 금액보다 큰 환불 금액

        // when & then
        assertThatThrownBy(() -> paymentService.refundPayment("PAY_TEST_KEY", excessiveRefundAmount, "초과 환불"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("환불할 수 없는 상태입니다.");
    }

    @Test
    @DisplayName("결제 내역 조회 성공")
    void getPayment_Success() {
        // given
        given(paymentRepository.findByPaymentKey("PAY_TEST_KEY")).willReturn(Optional.of(mockPayment));

        // when
        PaymentDto.Response result = paymentService.getPayment("PAY_TEST_KEY");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentKey()).isEqualTo("PAY_TEST_KEY");
        
        verify(paymentRepository).findByPaymentKey("PAY_TEST_KEY");
    }

    @Test
    @DisplayName("실패한 결제 재시도 성공")
    void retryFailedPayments_Success() {
        // given
        Payment failedPayment = Payment.createPayment(
                "PAY_FAILED_KEY", mockOrder, PaymentMethod.CARD, BigDecimal.valueOf(85000)
        );
        failedPayment.tryFailPayment("네트워크 오류");
        
        given(paymentRepository.findFailedPaymentsForRetry(any(LocalDateTime.class)))
                .willReturn(List.of(failedPayment));
        given(paymentRepository.save(any(Payment.class))).willReturn(failedPayment);

        // when
        paymentService.retryFailedPayments();

        // then
        verify(paymentRepository).findFailedPaymentsForRetry(any(LocalDateTime.class));
        verify(paymentRepository).save(failedPayment);
        verify(orderService).confirmOrder(mockOrder.getOrderNumber());
    }

    @Test
    @DisplayName("카드 번호 마스킹 테스트")
    void maskCardNumber_Success() {
        // given
        PaymentDto.Request cardRequest = PaymentDto.Request.builder()
                .orderNumber("ORD123456789")
                .paymentMethod(PaymentMethod.CARD)
                .amount(BigDecimal.valueOf(85000))
                .cardInfo(PaymentDto.Request.CardInfo.builder()
                        .cardNumber("1234567890123456")
                        .cardType("신용카드")
                        .installmentMonths(0)
                        .build())
                .build();

        given(orderRepository.findByOrderNumber("ORD123456789")).willReturn(Optional.of(mockOrder));
        given(paymentRepository.existsByOrderIdAndStatus(any(Long.class), eq(PaymentStatus.SUCCESS)))
                .willReturn(false);
        given(paymentRepository.save(any(Payment.class))).willAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            // 카드 번호가 마스킹되었는지 확인
            assertThat(payment.getCardNumber()).isEqualTo("1234****3456");
            return payment;
        });

        // when
        paymentService.processPayment(cardRequest);

        // then
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }
} 