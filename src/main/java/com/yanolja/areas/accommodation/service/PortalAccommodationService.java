package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.AmenityDto;
import com.yanolja.areas.accommodation.dto.PortalAccommodationDto;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.AccommodationImage;
import com.yanolja.areas.accommodation.repository.AccommodationImageRepository;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalAccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationImageRepository accommodationImageRepository;
    private final AccommodationImageService accommodationImageService;
    private final AmenityService amenityService;

    /**
     * 검색 조건에 따른 숙소 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<PortalAccommodationDto.ListResponse> searchAccommodations(
            PortalAccommodationDto.SearchRequest request) {
        
        // 검색 조건 추출
        PortalAccommodationDto.SearchCondition condition = request.getCondition();
        String keyword = condition != null ? condition.getKeyword() : null;
        BigDecimal minPrice = condition != null ? condition.getMinPrice() : null;
        BigDecimal maxPrice = condition != null ? condition.getMaxPrice() : null;
        
        // 페이징 정보 변환
        Pageable pageable = request.getPageRequest() != null ? 
                request.getPageRequest().toPageable(PortalAccommodationDto::mapSortColumn) : 
                org.springframework.data.domain.PageRequest.of(0, 10);
        
        // 정렬 방향과 컬럼명을 기준으로 sortBy 생성
        String sortBy = null;
        if (request.getPageRequest() != null && 
            request.getPageRequest().getSortColumn() != null && 
            request.getPageRequest().getSortDirection() != null) {
            sortBy = request.getPageRequest().getSortColumn() + "_" + request.getPageRequest().getSortDirection();
        }
        
        // Repository 호출
        Page<Accommodation> accommodations = accommodationRepository.searchAccommodations(
                keyword,
                minPrice,
                maxPrice,
                sortBy,
                pageable
        );
        
        // 각 숙소에 대한 메인 이미지 URL을 조회하여 DTO로 변환
        return accommodations.map(accommodation -> {
            String mainImageUrl = accommodationImageService.getMainImageUrl(accommodation.getId());
            return PortalAccommodationDto.ListResponse.fromEntityWithMainImage(accommodation, mainImageUrl);
        });
    }

    /**
     * 숙소 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public PortalAccommodationDto.DetailResponse getAccommodationDetail(Long id) {
        Accommodation accommodation = accommodationRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + id + "인 숙소를 찾을 수 없습니다."));
        
        // 이미지 목록 조회
        List<AccommodationImage> images = accommodationImageRepository.findByAccommodationId(id);
        
        // 편의시설 목록 조회
        List<AmenityDto.Response> amenities = amenityService.getAmenitiesByAccommodationId(id);
        
        // DTO 생성
        PortalAccommodationDto.DetailResponse response = PortalAccommodationDto.DetailResponse.fromEntityWithImages(accommodation, images);
        
        // 편의시설 정보 목록 설정 (이름과 아이콘 URL 포함)
        List<PortalAccommodationDto.AmenityInfo> amenityInfos = amenities.stream()
                .map(amenity -> PortalAccommodationDto.AmenityInfo.builder()
                        .name(amenity.getName())
                        .iconUrl(amenity.getIconUrl())
                        .build())
                .collect(Collectors.toList());
        
        response.setAmenityInfos(amenityInfos);
        
        return response;
    }
} 