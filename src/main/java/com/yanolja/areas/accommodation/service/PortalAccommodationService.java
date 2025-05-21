package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.PortalAccommodationDto;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.AccommodationImage;
import com.yanolja.areas.accommodation.repository.AccommodationImageRepository;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortalAccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationImageRepository accommodationImageRepository;
    private final AccommodationImageService accommodationImageService;

    /**
     * 검색 조건에 따른 숙소 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<PortalAccommodationDto.ListResponse> searchAccommodations(
            PortalAccommodationDto.SearchRequest request) {
        
        // 검색 조건 추출
        PortalAccommodationDto.SearchCondition condition = request.getCondition();
        String keyword = null;
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        
        if (condition != null) {
            keyword = condition.getKeyword();
            minPrice = condition.getMinPrice();
            maxPrice = condition.getMaxPrice();
        }
        // 페이징 정보 변환
        Pageable pageable = request.getPageRequest() != null ? 
                request.getPageRequest().toPageable(PortalAccommodationDto::mapSortColumn) : PageRequest.of(0, 10);
        
        // Repository 호출
        Page<Accommodation> accommodations = accommodationRepository.searchAccommodations(
                keyword,
                minPrice,
                maxPrice,
                request.getPageRequest(),
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
        Accommodation accommodation = accommodationRepository.findByIdAndDeletedYn(id,"N")
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + id + "인 숙소를 찾을 수 없습니다."));
        
        // 이미지 목록을 조회하여 상세 DTO 생성
        List<AccommodationImage> images = accommodationImageRepository.findByAccommodationId(id);
        return PortalAccommodationDto.DetailResponse.fromEntityWithImages(accommodation, images);
    }
} 