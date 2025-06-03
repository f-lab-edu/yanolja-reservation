package com.yanolja.areas.reservation.service;

import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import com.yanolja.areas.reservation.dto.ReservationDto;
import com.yanolja.areas.reservation.dto.ReservationOptionDto;
import com.yanolja.areas.reservation.entity.Reservation;
import com.yanolja.areas.reservation.entity.ReservationOption;
import com.yanolja.areas.reservation.entity.ReservationStatus;
import com.yanolja.areas.reservation.entity.PaymentStatus;
import com.yanolja.areas.reservation.repository.ReservationOptionRepository;
import com.yanolja.areas.reservation.repository.ReservationRepository;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.areas.room.entity.RoomOption;
import com.yanolja.areas.room.repository.RoomOptionRepository;
import com.yanolja.areas.room.repository.RoomRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    
    @Mock
    private ReservationOptionRepository reservationOptionRepository;
    
    @Mock
    private RoomRepository roomRepository;
    
    @Mock
    private RoomOptionRepository roomOptionRepository;
    
    @Mock
    private AccommodationRepository accommodationRepository;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationDto.Request reservationRequest;
    private Reservation reservation;
    private ReservationOption reservationOption;
    private Room room;
    private RoomOption roomOption;
    private Accommodation accommodation;
    private ReservationDto.StatusUpdateRequest statusUpdateRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 예약 요청 DTO 생성
        ReservationOptionDto.Request optionRequest = ReservationOptionDto.Request.builder()
                .optionId(1L)
                .quantity(2)
                .price(new BigDecimal("10000"))
                .build();
        
        reservationRequest = ReservationDto.Request.builder()
                .roomId(1L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(2))
                .totalPrice(new BigDecimal("89000"))
                .options(Arrays.asList(optionRequest))
                .build();

        // 테스트용 숙소 엔티티 생성
        accommodation = Accommodation.builder()
                .name("서울 시티 호텔")
                .address("서울시 강남구 테헤란로 123")
                .description("편안한 숙박을 위한 호텔입니다.")
                .build();
        ReflectionTestUtils.setField(accommodation, "id", 1L);

        // 테스트용 객실 엔티티 생성
        room = Room.createRoom(
                1L,
                "스탠다드 더블룸",
                "편안한 더블룸입니다.",
                2,
                new BigDecimal("79000")
        );
        ReflectionTestUtils.setField(room, "id", 1L);

        // 테스트용 객실 옵션 엔티티 생성
        roomOption = RoomOption.builder()
                .name("조식")
                .price(new BigDecimal("10000"))
                .build();
        ReflectionTestUtils.setField(roomOption, "id", 1L);

        // 테스트용 예약 엔티티 생성
        reservation = Reservation.createReservation(
                1L,
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                new BigDecimal("89000")
        );
        ReflectionTestUtils.setField(reservation, "id", 1L);
        ReflectionTestUtils.setField(reservation, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(reservation, "updatedAt", LocalDateTime.now());

        // 테스트용 예약 옵션 엔티티 생성
        reservationOption = ReservationOption.createReservationOption(
                1L,
                2,
                new BigDecimal("10000")
        );
        ReflectionTestUtils.setField(reservationOption, "id", 1L);
        reservation.addOption(reservationOption);

        // 테스트용 상태 변경 요청 DTO
        statusUpdateRequest = ReservationDto.StatusUpdateRequest.builder()
                .status(ReservationStatus.CONFIRMED)
                .reason("결제 완료")
                .build();
    }

    @Test
    @DisplayName("예약 생성 성공 테스트")
    void createReservationSuccess() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.findOverlappingReservations(any(), any(), any())).thenReturn(Arrays.asList());
        when(roomOptionRepository.findById(1L)).thenReturn(Optional.of(roomOption));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // When
        ReservationDto.Response response = reservationService.createReservation(1L, reservationRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(1L, response.getRoomId());
        assertEquals(LocalDate.now().plusDays(1), response.getCheckInDate());
        assertEquals(LocalDate.now().plusDays(2), response.getCheckOutDate());
        assertEquals(new BigDecimal("89000"), response.getTotalPrice());
        assertEquals(ReservationStatus.PENDING, response.getStatus());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
        assertEquals(1, response.getNights());
        assertTrue(response.getCanCancel());
        assertTrue(response.getCanModify());
        
        verify(roomRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).findOverlappingReservations(any(), any(), any());
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 생성 실패 테스트 - 존재하지 않는 객실")
    void createReservationFailRoomNotFound() {
        // Given
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());
        
        ReservationDto.Request invalidRequest = ReservationDto.Request.builder()
                .roomId(999L)
                .checkInDate(LocalDate.now().plusDays(1))
                .checkOutDate(LocalDate.now().plusDays(2))
                .totalPrice(new BigDecimal("89000"))
                .build();

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            reservationService.createReservation(1L, invalidRequest);
        });
        
        verify(roomRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("예약 생성 실패 테스트 - 중복 예약")
    void createReservationFailOverlapping() {
        // Given
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepository.findOverlappingReservations(any(), any(), any()))
                .thenReturn(Arrays.asList(reservation));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            reservationService.createReservation(1L, reservationRequest);
        });
        
        verify(roomRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).findOverlappingReservations(any(), any(), any());
    }

    @Test
    @DisplayName("예약 생성 실패 테스트 - 잘못된 날짜")
    void createReservationFailInvalidDates() {
        // Given
        ReservationDto.Request invalidRequest = ReservationDto.Request.builder()
                .roomId(1L)
                .checkInDate(LocalDate.now().plusDays(2))
                .checkOutDate(LocalDate.now().plusDays(1)) // 체크아웃이 체크인보다 빠름
                .totalPrice(new BigDecimal("89000"))
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            reservationService.createReservation(1L, invalidRequest);
        });
    }

    @Test
    @DisplayName("예약 상세 조회 성공 테스트")
    void getReservationSuccess() {
        // Given
        when(reservationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(reservation));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));

        // When
        ReservationDto.Response response = reservationService.getReservation(1L, 1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(1L, response.getRoomId());
        assertEquals("스탠다드 더블룸", response.getRoomName());
        assertEquals("서울 시티 호텔", response.getAccommodationName());
        assertEquals("서울시 강남구 테헤란로 123", response.getAccommodationAddress());
        
        verify(reservationRepository, times(1)).findByIdAndUserId(1L, 1L);
        verify(roomRepository, times(1)).findById(1L);
        verify(accommodationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("예약 상세 조회 실패 테스트 - 존재하지 않는 예약")
    void getReservationFailNotFound() {
        // Given
        when(reservationRepository.findByIdAndUserId(999L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            reservationService.getReservation(999L, 1L);
        });
        
        verify(reservationRepository, times(1)).findByIdAndUserId(999L, 1L);
    }

    @Test
    @DisplayName("사용자별 예약 목록 조회 성공 테스트")
    void getUserReservationsSuccess() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> reservationPage = new PageImpl<>(Arrays.asList(reservation));
        
        when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(reservationPage);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));

        // When
        Page<ReservationDto.ListResponse> response = reservationService.getUserReservations(1L, null, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
        
        ReservationDto.ListResponse listItem = response.getContent().get(0);
        assertEquals(1L, listItem.getId());
        assertEquals("스탠다드 더블룸", listItem.getRoomName());
        assertEquals("서울 시티 호텔", listItem.getAccommodationName());
        
        verify(reservationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L, pageable);
    }

    @Test
    @DisplayName("예약 상태 변경 성공 테스트")
    void updateReservationStatusSuccess() {
        // Given
        when(reservationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // When
        ReservationDto.Response response = reservationService.updateReservationStatus(1L, 1L, statusUpdateRequest);

        // Then
        assertNotNull(response);
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
        
        verify(reservationRepository, times(1)).findByIdAndUserId(1L, 1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 취소 성공 테스트")
    void cancelReservationSuccess() {
        // Given
        when(reservationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // When
        ReservationDto.Response response = reservationService.cancelReservation(1L, 1L);

        // Then
        assertNotNull(response);
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertEquals(PaymentStatus.CANCELLED, reservation.getPaymentStatus());
        assertFalse(reservation.canCancel());
        
        verify(reservationRepository, times(1)).findByIdAndUserId(1L, 1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 확정 성공 테스트")
    void confirmReservationSuccess() {
        // Given
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // When
        ReservationDto.Response response = reservationService.confirmReservation(1L);

        // Then
        assertNotNull(response);
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(PaymentStatus.COMPLETED, reservation.getPaymentStatus());
        
        verify(reservationRepository, times(1)).findById(1L);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 확정 실패 테스트 - 잘못된 상태")
    void confirmReservationFailInvalidStatus() {
        // Given
        reservation.updateStatus(ReservationStatus.CONFIRMED); // 이미 확정된 상태로 변경
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            reservationService.confirmReservation(1L);
        });
        
        verify(reservationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("만료된 예약 정리 성공 테스트")
    void cleanupExpiredReservationsSuccess() {
        // Given
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(10);
        List<Reservation> expiredReservations = Arrays.asList(reservation);
        
        when(reservationRepository.findExpiredPendingReservations(any(LocalDateTime.class)))
                .thenReturn(expiredReservations);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // When
        reservationService.cleanupExpiredReservations();

        // Then
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        
        verify(reservationRepository, times(1)).findExpiredPendingReservations(any(LocalDateTime.class));
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("고급 검색 테스트")
    void searchReservationsSuccess() {
        // Given
        ReservationDto.SearchCondition condition = ReservationDto.SearchCondition.builder()
                .userId(1L)
                .statuses(Arrays.asList(ReservationStatus.CONFIRMED))
                .checkInDateFrom(LocalDate.now())
                .checkInDateTo(LocalDate.now().plusDays(30))
                .build();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Reservation> reservationPage = new PageImpl<>(Arrays.asList(reservation));
        
        when(reservationRepository.findReservationsWithComplexConditions(
                any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(reservationPage);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));

        // When
        Page<ReservationDto.ListResponse> response = reservationService.searchReservations(condition, pageable);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        
        verify(reservationRepository, times(1)).findReservationsWithComplexConditions(
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("사용자별 예약 통계 조회 테스트")
    void getUserReservationStatsSuccess() {
        // Given
        List<Object[]> mockStats = Arrays.asList(
                new Object[]{ReservationStatus.CONFIRMED, 5L, new BigDecimal("450000")},
                new Object[]{ReservationStatus.COMPLETED, 3L, new BigDecimal("270000")}
        );
        
        when(reservationRepository.getReservationStatsByUser(1L)).thenReturn(mockStats);

        // When
        List<Object[]> response = reservationService.getUserReservationStats(1L);

        // Then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(ReservationStatus.CONFIRMED, response.get(0)[0]);
        assertEquals(5L, response.get(0)[1]);
        assertEquals(new BigDecimal("450000"), response.get(0)[2]);
        
        verify(reservationRepository, times(1)).getReservationStatsByUser(1L);
    }

    @Test
    @DisplayName("객실별 예약 현황 조회 테스트")
    void getRoomReservationStatusSuccess() {
        // Given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        
        List<Object[]> mockStatus = Arrays.asList(
                new Object[]{startDate, startDate.plusDays(1), ReservationStatus.CONFIRMED, 1L},
                new Object[]{startDate.plusDays(5), startDate.plusDays(6), ReservationStatus.COMPLETED, 1L}
        );
        
        when(reservationRepository.getReservationStatusByRoom(1L, startDate, endDate))
                .thenReturn(mockStatus);

        // When
        List<Object[]> response = reservationService.getRoomReservationStatus(1L, startDate, endDate);

        // Then
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(startDate, response.get(0)[0]);
        assertEquals(ReservationStatus.CONFIRMED, response.get(0)[2]);
        
        verify(reservationRepository, times(1)).getReservationStatusByRoom(1L, startDate, endDate);
    }
} 