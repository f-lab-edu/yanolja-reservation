package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.PortalAccommodationDto;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import com.yanolja.common.dto.PageRequestDto;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortalAccommodationServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @InjectMocks
    private PortalAccommodationService portalAccommodationService;

    @Test
    @DisplayName("숙소 검색 - 키워드 검색 성공")
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
                
        Accommodation accommodation1 = createMockAccommodation(1L, "서울 호텔", "서울시 중구", new BigDecimal("100000"));
        Accommodation accommodation2 = createMockAccommodation(2L, "서울 리조트", "서울시 강남구", new BigDecimal("120000"));
        
        Page<Accommodation> mockPage = new PageImpl<>(
                List.of(accommodation1, accommodation2), 
                pageable, 
                2
        );
        
        when(accommodationRepository.searchAccommodations(
                eq(keyword), 
                eq(minPrice), 
                eq(maxPrice), 
                eq(pageRequest), 
                any(Pageable.class)
        )).thenReturn(mockPage);
        
        // When
        Page<PortalAccommodationDto.ListResponse> result = portalAccommodationService.searchAccommodations(request);
        
        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("서울 호텔");
        assertThat(result.getContent().get(1).getName()).isEqualTo("서울 리조트");
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
                eq(pageRequest), 
                any(Pageable.class)
        )).thenReturn(emptyPage);
        
        // When
        Page<PortalAccommodationDto.ListResponse> result = portalAccommodationService.searchAccommodations(request);
        
        // Then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }
    
    @Test
    @DisplayName("숙소 상세 조회 - 존재하는 숙소 ID")
    void getAccommodationDetail_WithExistingId_ShouldReturnAccommodationDetail() {
        // Given
        Long id = 1L;
        Accommodation accommodation = createDetailMockAccommodation(
                id, 
                "서울 그랜드 호텔", 
                "서울시 중구 명동", 
                "서울 중심부에 위치한 5성급 호텔", 
                new BigDecimal("150000")
        );
        
        when(accommodationRepository.findById(id)).thenReturn(Optional.of(accommodation));
        
        // When
        PortalAccommodationDto.DetailResponse result = portalAccommodationService.getAccommodationDetail(id);
        
        // Then
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("서울 그랜드 호텔");
        assertThat(result.getDescription()).isEqualTo("서울 중심부에 위치한 5성급 호텔");
        assertThat(result.getPricePerNight()).isEqualTo(new BigDecimal("150000"));
    }
    
    @Test
    @DisplayName("숙소 상세 조회 - 존재하지 않는 숙소 ID")
    void getAccommodationDetail_WithNonExistingId_ShouldThrowException() {
        // Given
        Long id = 999L;
        when(accommodationRepository.findById(id)).thenReturn(Optional.empty());
        
        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            portalAccommodationService.getAccommodationDetail(id);
        });
        
        assertThat(exception.getMessage()).contains(id.toString());
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
} 