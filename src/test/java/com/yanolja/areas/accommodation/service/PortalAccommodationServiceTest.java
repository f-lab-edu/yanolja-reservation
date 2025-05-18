package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.AmenityDto;
import com.yanolja.areas.accommodation.dto.PortalAccommodationDto;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.AccommodationImage;
import com.yanolja.areas.accommodation.repository.AccommodationImageRepository;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import com.yanolja.common.dto.PageRequestDto;
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
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortalAccommodationServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;
    
    @Mock
    private AccommodationImageRepository accommodationImageRepository;
    
    @Mock
    private AccommodationImageService accommodationImageService;
    
    @Mock
    private AmenityService amenityService;

    @InjectMocks
    private PortalAccommodationService portalAccommodationService;
    
    // 테스트 데이터
    private Accommodation accommodation1;
    private Accommodation accommodation2;
    private Accommodation detailAccommodation;
    private AccommodationImage mainImage;
    private AccommodationImage subImage;
    private List<AccommodationImage> imageList;
    private List<AmenityDto.Response> amenityList;
    private String mainImageUrl;
    private String subImageUrl;
    
    @BeforeEach
    void setUp() {
        // 기본 숙소 데이터 설정
        accommodation1 = createMockAccommodation(1L, "서울 호텔", "서울시 중구", new BigDecimal("100000"));
        accommodation2 = createMockAccommodation(2L, "서울 리조트", "서울시 강남구", new BigDecimal("120000"));
        
        // 상세 조회용 숙소 데이터
        detailAccommodation = createDetailMockAccommodation(
                1L, 
                "서울 그랜드 호텔", 
                "서울시 중구 명동", 
                "서울 중심부에 위치한 5성급 호텔", 
                new BigDecimal("150000")
        );
        
        // 이미지 URL 설정
        mainImageUrl = "/images/accommodations/1/main.jpg";
        subImageUrl = "/images/accommodations/1/room.jpg";
        
        // 이미지 데이터 설정
        mainImage = createMockAccommodationImage(1L, detailAccommodation, mainImageUrl, true);
        subImage = createMockAccommodationImage(2L, detailAccommodation, subImageUrl, false);
        imageList = Arrays.asList(mainImage, subImage);
        
        // 편의시설 데이터 설정
        amenityList = Arrays.asList(
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
        
        // 기본 모킹 설정 - lenient() 추가하여 불필요한 stubbing 경고 방지
        lenient().when(accommodationImageService.getMainImageUrl(1L)).thenReturn(mainImageUrl);
        lenient().when(accommodationImageService.getMainImageUrl(2L)).thenReturn("/images/accommodations/2/main.jpg");
        lenient().when(accommodationImageRepository.findByAccommodationId(1L)).thenReturn(imageList);
        lenient().when(accommodationRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(detailAccommodation));
        lenient().when(accommodationRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());
        lenient().when(amenityService.getAmenitiesByAccommodationId(1L)).thenReturn(amenityList);
    }

    @Test
    @DisplayName("숙소 검색 - 키워드 검색 성공 (이미지 포함)")
    void searchAccommodations_WithKeyword_ShouldReturnMatchingAccommodations() {
        // Given
        String keyword = "서울";
        BigDecimal minPrice = new BigDecimal("50000");
        BigDecimal maxPrice = new BigDecimal("150000");
        
        PortalAccommodationDto.SearchCondition condition = PortalAccommodationDto.SearchCondition.builder()
                .keyword(keyword)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();
        
        PageRequestDto pageRequest = PageRequestDto.builder()
                .page(0)
                .size(10)
                .sortColumn("price")
                .sortDirection("asc")
                .build();
        
        PortalAccommodationDto.SearchRequest request = PortalAccommodationDto.SearchRequest.builder()
                .condition(condition)
                .pageRequest(pageRequest)
                .build();
        
        Pageable pageable = pageRequest.toPageable(PortalAccommodationDto::mapSortColumn);
        String sortBy = "price_asc";
                
        Page<Accommodation> mockPage = new PageImpl<>(
                List.of(accommodation1, accommodation2), 
                pageable, 
                2
        );
        
        when(accommodationRepository.searchAccommodations(
                eq(keyword), 
                eq(minPrice), 
                eq(maxPrice), 
                eq(sortBy), 
                any(Pageable.class)
        )).thenReturn(mockPage);
        
        // AccommodationImageService mock setup for verification
        when(accommodationImageService.getMainImageUrl(anyLong())).thenReturn(mainImageUrl);
        
        // When
        Page<PortalAccommodationDto.ListResponse> result = portalAccommodationService.searchAccommodations(request);
        
        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("서울 호텔");
        assertThat(result.getContent().get(1).getName()).isEqualTo("서울 리조트");
        
        // 메인 이미지 검증
        assertThat(result.getContent().get(0).getMainImageUrl()).isEqualTo(mainImageUrl);
        assertThat(result.getContent().get(1).getMainImageUrl()).isEqualTo(mainImageUrl);
        
        // AccommodationImageService 호출 검증
        verify(accommodationImageService, times(1)).getMainImageUrl(1L);
        verify(accommodationImageService, times(1)).getMainImageUrl(2L);
    }
    
    @Test
    @DisplayName("숙소 검색 - 검색 결과 없음")
    void searchAccommodations_WithNoResults_ShouldReturnEmptyPage() {
        // Given
        String keyword = "존재하지 않는 지역";
        
        PortalAccommodationDto.SearchCondition condition = PortalAccommodationDto.SearchCondition.builder()
                .keyword(keyword)
                .build();
        
        PageRequestDto pageRequest = PageRequestDto.builder()
                .page(0)
                .size(10)
                .build();
        
        PortalAccommodationDto.SearchRequest request = PortalAccommodationDto.SearchRequest.builder()
                .condition(condition)
                .pageRequest(pageRequest)
                .build();
        
        Pageable pageable = pageRequest.toPageable();
                
        Page<Accommodation> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        
        when(accommodationRepository.searchAccommodations(
                eq(keyword), 
                any(), 
                any(), 
                any(), 
                any(Pageable.class)
        )).thenReturn(emptyPage);
        
        // When
        Page<PortalAccommodationDto.ListResponse> result = portalAccommodationService.searchAccommodations(request);
        
        // Then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }
    
    @Test
    @DisplayName("숙소 상세 조회 - 존재하는 숙소 ID (이미지와 편의시설 포함)")
    void getAccommodationDetail_WithExistingId_ShouldReturnAccommodationDetail() {
        // When
        // Setup specific image list for this test
        when(accommodationImageRepository.findByAccommodationId(1L)).thenReturn(imageList);
        when(accommodationRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(detailAccommodation));
        when(amenityService.getAmenitiesByAccommodationId(1L)).thenReturn(amenityList);
        
        PortalAccommodationDto.DetailResponse result = portalAccommodationService.getAccommodationDetail(1L);
        
        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("서울 그랜드 호텔");
        assertThat(result.getDescription()).isEqualTo("서울 중심부에 위치한 5성급 호텔");
        assertThat(result.getPricePerNight()).isEqualTo(new BigDecimal("150000"));
        
        // 이미지 URL 목록 검증
        assertThat(result.getImageUrls()).isNotNull();
        assertThat(result.getImageUrls()).hasSize(2);
        assertThat(result.getImageUrls()).contains(mainImageUrl, subImageUrl);
        
        // 편의시설 목록 검증
        assertThat(result.getAmenities()).isNotNull();
        assertThat(result.getAmenities()).hasSize(2);
        
        // 편의시설 정보 상세 검증 (이름과 아이콘 URL)
        assertThat(result.getAmenities().get(0).getName()).isEqualTo("와이파이");
        assertThat(result.getAmenities().get(0).getIconUrl()).isEqualTo("/icons/wifi.png");
        assertThat(result.getAmenities().get(1).getName()).isEqualTo("수영장");
        assertThat(result.getAmenities().get(1).getIconUrl()).isEqualTo("/icons/pool.png");
        
        // 호출 검증
        verify(accommodationRepository).findByIdAndNotDeleted(1L);
        verify(accommodationImageRepository).findByAccommodationId(1L);
        verify(amenityService).getAmenitiesByAccommodationId(1L);
    }
    
    @Test
    @DisplayName("숙소 상세 조회 - 존재하지 않는 숙소 ID")
    void getAccommodationDetail_WithNonExistingId_ShouldThrowException() {
        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            portalAccommodationService.getAccommodationDetail(999L);
        });
        
        assertThat(exception.getMessage()).contains("999");
        verify(accommodationRepository).findByIdAndNotDeleted(999L);
    }
    
    // 테스트 데이터 생성을 위한 도우미 메서드
    private Accommodation createMockAccommodation(Long id, String name, String address, BigDecimal price) {
        Accommodation accommodation = Accommodation.builder()
                .name(name)
                .address(address)
                .pricePerNight(price)
                .rating(new BigDecimal("4.5"))
                .reviewCount(10)
                .status("ACTIVE")
                .deletedYn("N")
                .build();
                
        // JPA에서 일반적으로 설정하는 ID를 리플렉션을 통해 설정
        try {
            java.lang.reflect.Field idField = Accommodation.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(accommodation, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return accommodation;
    }
    
    private Accommodation createDetailMockAccommodation(Long id, String name, String address, String description, BigDecimal price) {
        Accommodation accommodation = Accommodation.builder()
                .name(name)
                .description(description)
                .address(address)
                .pricePerNight(price)
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .rating(new BigDecimal("4.5"))
                .reviewCount(10)
                .status("ACTIVE")
                .deletedYn("N")
                .build();
                
        // JPA에서 일반적으로 설정하는 ID를 리플렉션을 통해 설정
        try {
            java.lang.reflect.Field idField = Accommodation.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(accommodation, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return accommodation;
    }
    
    /**
     * 테스트용 AccommodationImage 객체 생성
     */
    private AccommodationImage createMockAccommodationImage(Long id, Accommodation accommodation, String imageUrl, boolean isMain) {
        AccommodationImage image = AccommodationImage.builder()
                .accommodation(accommodation)
                .imageUrl(imageUrl)
                .isMain(isMain)
                .build();
        
        ReflectionTestUtils.setField(image, "id", id);
        return image;
    }
} 