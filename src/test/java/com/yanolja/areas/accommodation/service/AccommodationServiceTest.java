package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.AccommodationDto;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccommodationServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @InjectMocks
    private AccommodationService accommodationService;

    private AccommodationDto.Request accommodationRequest;
    private Accommodation accommodation;

    @BeforeEach
    void setUp() {
        // 테스트용 숙소 요청 DTO 생성
        accommodationRequest = AccommodationDto.Request.builder()
                .name("호텔 테스트")
                .description("테스트용 호텔입니다.")
                .address("서울시 강남구 테헤란로 123")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .pricePerNight(new BigDecimal("100000"))
                .build();

        // 테스트용 엔티티 생성
        accommodation = Accommodation.createAccommodation(
                "호텔 테스트",
                "테스트용 호텔입니다.",
                "서울시 강남구 테헤란로 123",
                new BigDecimal("37.5665"),
                new BigDecimal("126.9780"),
                new BigDecimal("100000")
        );
        ReflectionTestUtils.setField(accommodation, "id", 1L);
    }

    @Test
    @DisplayName("숙소 생성 성공 테스트")
    void createAccommodationSuccess() {
        // Given
        when(accommodationRepository.save(any(Accommodation.class))).thenReturn(accommodation);

        // When
        AccommodationDto.Response response = accommodationService.createAccommodation(accommodationRequest);

        // Then
        assertNotNull(response);
        assertEquals("호텔 테스트", response.getName());
        assertEquals("테스트용 호텔입니다.", response.getDescription());
        assertEquals("서울시 강남구 테헤란로 123", response.getAddress());
        assertEquals(new BigDecimal("37.5665"), response.getLatitude());
        assertEquals(new BigDecimal("126.9780"), response.getLongitude());
        assertEquals(new BigDecimal("100000"), response.getPricePerNight());
        
        verify(accommodationRepository, times(1)).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("숙소 목록 조회 성공 테스트")
    void getAllAccommodationsSuccess() {
        // Given
        List<Accommodation> accommodations = Arrays.asList(accommodation);
        when(accommodationRepository.findByDeletedYn("N")).thenReturn(accommodations);

        // When
        List<AccommodationDto.ListResponse> responses = accommodationService.getAllAccommodations();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("호텔 테스트", responses.get(0).getName());
        assertEquals("서울시 강남구 테헤란로 123", responses.get(0).getAddress());
        assertEquals(new BigDecimal("100000"), responses.get(0).getPricePerNight());
        
        verify(accommodationRepository, times(1)).findByDeletedYn("N");
    }

    @Test
    @DisplayName("숙소 상세 조회 성공 테스트")
    void getAccommodationByIdSuccess() {
        // Given
        when(accommodationRepository.findByIdAndDeletedYn(1L,"N")).thenReturn(Optional.of(accommodation));

        // When
        AccommodationDto.Response response = accommodationService.getAccommodationById(1L);

        // Then
        assertNotNull(response);
        assertEquals("호텔 테스트", response.getName());
        assertEquals("테스트용 호텔입니다.", response.getDescription());
        assertEquals("서울시 강남구 테헤란로 123", response.getAddress());
        
        verify(accommodationRepository, times(1)).findByIdAndDeletedYn(1L,"N");
    }

    @Test
    @DisplayName("숙소 상세 조회 실패 테스트 - 존재하지 않는 ID")
    void getAccommodationByIdFailNotFound() {
        // Given
        when(accommodationRepository.findByIdAndDeletedYn(999L,"N")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            accommodationService.getAccommodationById(999L);
        });
        
        verify(accommodationRepository, times(1)).findByIdAndDeletedYn(999L,"N");
    }

    @Test
    @DisplayName("숙소 수정 성공 테스트")
    void updateAccommodationSuccess() {
        // Given
        when(accommodationRepository.findByIdAndDeletedYn(1L,"N")).thenReturn(Optional.of(accommodation));
        when(accommodationRepository.save(any(Accommodation.class))).thenReturn(accommodation);

        // 수정할 숙소 정보
        AccommodationDto.Request updateRequest = AccommodationDto.Request.builder()
                .name("수정된 호텔 테스트")
                .description("수정된 설명입니다.")
                .address("서울시 강남구 테헤란로 456")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .pricePerNight(new BigDecimal("120000"))
                .build();

        // When
        AccommodationDto.Response response = accommodationService.updateAccommodation(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("수정된 호텔 테스트", response.getName());
        assertEquals("수정된 설명입니다.", response.getDescription());
        assertEquals("서울시 강남구 테헤란로 456", response.getAddress());
        assertEquals(new BigDecimal("120000"), response.getPricePerNight());
        
        verify(accommodationRepository, times(1)).findByIdAndDeletedYn(1L,"N");
        verify(accommodationRepository, times(1)).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("숙소 삭제 성공 테스트")
    void deleteAccommodationSuccess() {
        // Given
        when(accommodationRepository.findByIdAndDeletedYn(1L,"N")).thenReturn(Optional.of(accommodation));
        
        // When
        accommodationService.deleteAccommodation(1L);

        // Then
        verify(accommodationRepository, times(1)).findByIdAndDeletedYn(1L,"N");
        verify(accommodationRepository, times(1)).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("숙소 삭제 실패 테스트 - 존재하지 않는 ID")
    void deleteAccommodationFailNotFound() {
        // Given
        when(accommodationRepository.findByIdAndDeletedYn(999L,"N")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            accommodationService.deleteAccommodation(999L);
        });
        
        verify(accommodationRepository, times(1)).findByIdAndDeletedYn(999L,"N");
        verify(accommodationRepository, never()).save(any(Accommodation.class));
    }
} 