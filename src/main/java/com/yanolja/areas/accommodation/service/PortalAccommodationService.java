package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.PortalAccommodationDto;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PortalAccommodationService {

    private final AccommodationRepository accommodationRepository;

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
        
        return accommodations.map(PortalAccommodationDto.ListResponse::fromEntity);
    }

    /**
     * 숙소 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public PortalAccommodationDto.DetailResponse getAccommodationDetail(Long id) {
        Accommodation accommodation = accommodationRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + id + "인 숙소를 찾을 수 없습니다."));
        
        return PortalAccommodationDto.DetailResponse.fromEntity(accommodation);
    }
} 