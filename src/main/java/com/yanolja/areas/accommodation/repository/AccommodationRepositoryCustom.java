package com.yanolja.areas.accommodation.repository;

import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.common.dto.PageRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccommodationRepositoryCustom {
    
    Optional<Accommodation> findByIdAndNotDeleted(Long id);
    
    /**
     * 사용자 숙소 검색
     */
    Page<Accommodation> searchAccommodations(
            String keyword, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            PageRequestDto pageRequestDto,
            Pageable pageable);
} 