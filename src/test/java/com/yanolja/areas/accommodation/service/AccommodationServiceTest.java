package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.AccommodationDto;
import com.yanolja.areas.accommodation.dto.AccommodationImageDto;
import com.yanolja.areas.accommodation.dto.AmenityDto;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.AccommodationImage;
import com.yanolja.areas.accommodation.entity.AccommodationStatus;
import com.yanolja.areas.accommodation.repository.AccommodationImageRepository;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import com.yanolja.areas.room.dto.RoomDto;
import com.yanolja.areas.room.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
import org.springframework.dao.OptimisticLockingFailureException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccommodationService 테스트")
public class AccommodationServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;
    
    @Mock
    private AccommodationImageRepository accommodationImageRepository;
    
    @Mock
    private AccommodationImageService accommodationImageService;
    
    @Mock
    private AmenityService amenityService;
    
    @Mock
    private RoomService roomService;

    @InjectMocks
    private AccommodationService accommodationService;

    private AccommodationDto.Request accommodationRequest;
    private Accommodation accommodation;
    private AccommodationImage accommodationMainImage;
    private AccommodationImage accommodationImage;
    private List<AmenityDto.Response> amenities;
    private List<RoomDto.ListResponse> rooms;
    private MockMultipartFile mockImage1;
    private MockMultipartFile mockImage2;

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
        accommodation = Accommodation.builder()
                .name("호텔 테스트")
                .description("테스트용 호텔입니다.")
                .address("서울시 강남구 테헤란로 123")
                .rating(BigDecimal.valueOf(4.5))
                .reviewCount(10)
                .build();
        ReflectionTestUtils.setField(accommodation, "id", 1L);
        
        // 테스트용 이미지 엔티티 생성
        accommodationMainImage = AccommodationImage.builder()
                .accommodation(accommodation)
                .imageUrl("/images/accommodations/1/main.jpg")
                .isMain(true)
                .build();
        ReflectionTestUtils.setField(accommodationMainImage, "id", 1L);
        
        accommodationImage = AccommodationImage.builder()
                .accommodation(accommodation)
                .imageUrl("/images/accommodations/1/room.jpg")
                .isMain(false)
                .build();
        ReflectionTestUtils.setField(accommodationImage, "id", 2L);
        
        // 테스트용 편의시설 목록 생성
        amenities = Arrays.asList(
            AmenityDto.Response.builder()
                .id(1L)
                .name("와이파이")
                .iconUrl("/icons/wifi.png")
                .build(),
            AmenityDto.Response.builder()
                .id(2L)
                .name("수영장")
                .iconUrl("/icons/pool.png")
                .build()
        );
        
        // 테스트용 객실 목록 생성
        rooms = Arrays.asList(
            RoomDto.ListResponse.builder()
                .id(1L)
                .accommodationId(1L)
                .name("디럭스 더블룸")
                .description("편안한 더블룸입니다.")
                .capacity(2)
                .pricePerNight(new BigDecimal("120000"))
                .status("AVAILABLE")
                .mainImageUrl("/images/rooms/1/main.jpg")
                .build(),
            RoomDto.ListResponse.builder()
                .id(2L)
                .accommodationId(1L)
                .name("스위트룸")
                .description("넓은 스위트룸입니다.")
                .capacity(4)
                .pricePerNight(new BigDecimal("200000"))
                .status("AVAILABLE")
                .mainImageUrl("/images/rooms/2/main.jpg")
                .build()
        );
        
        // 테스트용 이미지 파일 생성
        mockImage1 = new MockMultipartFile(
                "image1",
                "test1.jpg",
                "image/jpeg",
                "test image content 1".getBytes()
        );

        mockImage2 = new MockMultipartFile(
                "image2",
                "test2.jpg",
                "image/jpeg",
                "test image content 2".getBytes()
        );
    }

    @Test
    @DisplayName("숙소 생성 성공 테스트")
    void createAccommodationSuccess() {
        // Given
        when(accommodationRepository.save(any(Accommodation.class))).thenAnswer(invocation -> {
            Accommodation savedAccommodation = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedAccommodation, "id", 1L);
            return savedAccommodation;
        });

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
    @DisplayName("숙소 목록 조회 성공 테스트 - 메인 이미지 포함")
    void getAllAccommodationsSuccessWithMainImage() {
        // Given
        Accommodation accommodation = Accommodation.builder()
                .name("호텔 테스트")
                .description("테스트용 호텔입니다.")
                .address("서울시 강남구 테헤란로 123")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .pricePerNight(new BigDecimal("100000"))
                .rating(BigDecimal.valueOf(4.5))
                .reviewCount(10)
                .status(AccommodationStatus.ACTIVE)
                .deletedYn(false)
                .build();
        ReflectionTestUtils.setField(accommodation, "id", 1L);

        when(accommodationRepository.findAll()).thenReturn(List.of(accommodation));
        when(accommodationImageService.getMainImageUrl(1L)).thenReturn("/images/accommodations/1/main.jpg");

        // When
        List<AccommodationDto.ListResponse> responses = accommodationService.getAllAccommodations();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        AccommodationDto.ListResponse response = responses.get(0);
        assertEquals("호텔 테스트", response.getName());
        assertEquals("서울시 강남구 테헤란로 123", response.getAddress());
        assertEquals(new BigDecimal("100000"), response.getPricePerNight());
        assertEquals(BigDecimal.valueOf(4.5), response.getRating());
        assertEquals(10, response.getReviewCount());
        assertEquals("/images/accommodations/1/main.jpg", response.getMainImageUrl());

        verify(accommodationRepository, times(1)).findAll();
        verify(accommodationImageService, times(1)).getMainImageUrl(1L);
    }

    @Test
    @DisplayName("숙소 상세 조회 성공 테스트 - 이미지 목록, 편의시설, 객실 포함")
    void getAccommodationByIdSuccessWithImagesAndRooms() {
        // Given
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        when(accommodationImageRepository.findByAccommodationId(1L)).thenReturn(
                Arrays.asList(accommodationMainImage, accommodationImage)
        );
        when(amenityService.getAmenitiesByAccommodationId(1L)).thenReturn(amenities);
        when(roomService.getRoomsByAccommodationId(1L)).thenReturn(rooms);

        // When
        AccommodationDto.Response response = accommodationService.getAccommodationById(1L);

        // Then
        assertNotNull(response);
        assertEquals("호텔 테스트", response.getName());
        assertEquals("테스트용 호텔입니다.", response.getDescription());
        assertEquals("서울시 강남구 테헤란로 123", response.getAddress());
        
        // 이미지 검증
        assertNotNull(response.getImages());
        assertEquals(2, response.getImages().size());
        
        // 첫 번째 이미지가 메인 이미지인지 확인
        assertTrue(response.getImages().get(0).getIsMain());
        assertEquals("/images/accommodations/1/main.jpg", response.getImages().get(0).getImageUrl());
        
        // 두 번째 이미지 확인
        assertFalse(response.getImages().get(1).getIsMain());
        assertEquals("/images/accommodations/1/room.jpg", response.getImages().get(1).getImageUrl());
        
        // 편의시설 검증
        assertNotNull(response.getAmenities());
        assertEquals(2, response.getAmenities().size());
        assertEquals("와이파이", response.getAmenities().get(0).getName());
        assertEquals("/icons/wifi.png", response.getAmenities().get(0).getIconUrl());
        assertEquals("수영장", response.getAmenities().get(1).getName());
        assertEquals("/icons/pool.png", response.getAmenities().get(1).getIconUrl());
        
        // 객실 검증
        assertNotNull(response.getRooms());
        assertEquals(2, response.getRooms().size());
        assertEquals("디럭스 더블룸", response.getRooms().get(0).getName());
        assertEquals(new BigDecimal("120000"), response.getRooms().get(0).getPricePerNight());
        assertEquals("스위트룸", response.getRooms().get(1).getName());
        assertEquals(new BigDecimal("200000"), response.getRooms().get(1).getPricePerNight());
        
        verify(accommodationRepository, times(1)).findById(1L);
        verify(accommodationImageRepository, times(1)).findByAccommodationId(1L);
        verify(amenityService, times(1)).getAmenitiesByAccommodationId(1L);
        verify(roomService, times(1)).getRoomsByAccommodationId(1L);
    }

    @Test
    @DisplayName("숙소 상세 조회 실패 테스트 - 존재하지 않는 ID")
    void getAccommodationByIdFailNotFound() {
        // Given
        when(accommodationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            accommodationService.getAccommodationById(999L);
        });
        
        verify(accommodationRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("숙소 수정 성공 테스트")
    void updateAccommodationSuccess() {
        // Given
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));

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
        
        verify(accommodationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("숙소 삭제 성공 테스트")
    void deleteAccommodationSuccess() {
        // Given
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));
        
        // When
        accommodationService.deleteAccommodation(1L);

        // Then
        verify(accommodationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("숙소 삭제 실패 테스트 - 존재하지 않는 ID")
    void deleteAccommodationFailNotFound() {
        // Given
        when(accommodationRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            accommodationService.deleteAccommodation(999L);
        });
        
        verify(accommodationRepository, times(1)).findById(999L);
        verify(accommodationRepository, never()).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("리뷰 수 증가 성공")
    void incrementReviewCount_Success() {
        // Given
        Long accommodationId = 1L;
        Accommodation accommodation = Accommodation.builder()
                .name("호텔 테스트")
                .description("테스트용 호텔입니다.")
                .address("서울시 강남구 테헤란로 123")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .pricePerNight(new BigDecimal("100000"))
                .reviewCount(10)
                .build();
        ReflectionTestUtils.setField(accommodation, "id", accommodationId);
        
        when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.of(accommodation));
        when(accommodationRepository.saveAndFlush(any(Accommodation.class)))
                .thenReturn(accommodation);

        // When
        accommodationService.incrementReviewCount(accommodationId);

        // Then
        assertEquals(11, accommodation.getReviewCount());
        verify(accommodationRepository, times(1)).findById(accommodationId);
        verify(accommodationRepository, times(1)).saveAndFlush(accommodation);
    }

    @Test
    @DisplayName("리뷰 수 증가 실패 - 존재하지 않는 숙소")
    void incrementReviewCount_Fail_AccommodationNotFound() {
        // Given
        Long accommodationId = 1L;
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accommodationService.incrementReviewCount(accommodationId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("ID가 1인 숙소를 찾을 수 없습니다.");

        verify(accommodationRepository).findById(accommodationId);
        verify(accommodationRepository, never()).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("리뷰 수 증가 재시도 성공")
    void incrementReviewCountWithRetry_Success() {
        // Given
        Long accommodationId = 1L;
        Accommodation accommodation = Accommodation.builder()
                .name("호텔 테스트")
                .description("테스트용 호텔입니다.")
                .address("서울시 강남구 테헤란로 123")
                .reviewCount(10)
                .build();
        ReflectionTestUtils.setField(accommodation, "id", accommodationId);
        ReflectionTestUtils.setField(accommodation, "version", 0L);

        when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.of(accommodation));
        when(accommodationRepository.saveAndFlush(any(Accommodation.class)))
                .thenReturn(accommodation);

        // When
        accommodationService.incrementReviewCountWithRetry(accommodationId, 3);

        // Then
        assertEquals(11, accommodation.getReviewCount());
        verify(accommodationRepository, times(1)).findById(accommodationId);
        verify(accommodationRepository, times(1)).saveAndFlush(accommodation);
    }

    @Test
    @DisplayName("리뷰 수 감소 성공")
    void decrementReviewCount_Success() {
        // Given
        Long accommodationId = 1L;
        Accommodation accommodation = Accommodation.builder()
                .name("호텔 테스트")
                .description("테스트용 호텔입니다.")
                .address("서울시 강남구 테헤란로 123")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .pricePerNight(new BigDecimal("100000"))
                .reviewCount(10)
                .build();
        ReflectionTestUtils.setField(accommodation, "id", accommodationId);
        
        when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.of(accommodation));
        when(accommodationRepository.saveAndFlush(any(Accommodation.class)))
                .thenReturn(accommodation);

        // When
        accommodationService.decrementReviewCount(accommodationId);

        // Then
        assertEquals(9, accommodation.getReviewCount());
        verify(accommodationRepository, times(1)).findById(accommodationId);
        verify(accommodationRepository, times(1)).saveAndFlush(accommodation);
    }

    @Test
    @DisplayName("리뷰 수 감소 실패 - 존재하지 않는 숙소")
    void decrementReviewCount_Fail_AccommodationNotFound() {
        // Given
        Long accommodationId = 1L;
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accommodationService.decrementReviewCount(accommodationId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("ID가 1인 숙소를 찾을 수 없습니다.");

        verify(accommodationRepository).findById(accommodationId);
        verify(accommodationRepository, never()).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("평점 업데이트 성공")
    void updateRating_Success() {
        // Given
        Long accommodationId = 1L;
        BigDecimal newRating = BigDecimal.valueOf(4.8);
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(accommodation));
        when(accommodationRepository.save(any(Accommodation.class))).thenReturn(accommodation);

        // When
        accommodationService.updateRating(accommodationId, newRating);

        // Then
        assertThat(accommodation.getRating()).isEqualTo(newRating);
        verify(accommodationRepository).findById(accommodationId);
        verify(accommodationRepository).save(accommodation);
    }

    @Test
    @DisplayName("평점 업데이트 실패 - 존재하지 않는 숙소")
    void updateRating_Fail_AccommodationNotFound() {
        // Given
        Long accommodationId = 1L;
        BigDecimal newRating = BigDecimal.valueOf(4.8);
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accommodationService.updateRating(accommodationId, newRating))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("ID가 1인 숙소를 찾을 수 없습니다.");

        verify(accommodationRepository).findById(accommodationId);
        verify(accommodationRepository, never()).save(any(Accommodation.class));
    }

    @Test
    @DisplayName("평점 업데이트 재시도 성공")
    void updateRatingWithRetry_Success() {
        // Given
        Long accommodationId = 1L;
        BigDecimal newRating = BigDecimal.valueOf(4.8);
        when(accommodationRepository.findById(accommodationId))
                .thenReturn(Optional.of(accommodation));
        when(accommodationRepository.save(any(Accommodation.class)))
                .thenThrow(OptimisticLockingFailureException.class)
                .thenReturn(accommodation);

        // When
        accommodationService.updateRatingWithRetry(accommodationId, newRating, 3);

        // Then
        assertThat(accommodation.getRating()).isEqualTo(newRating);
        verify(accommodationRepository, times(2)).findById(accommodationId);
        verify(accommodationRepository, times(2)).save(accommodation);
    }
} 