package com.yanolja.areas.accommodation.repository;

import com.yanolja.areas.accommodation.entity.AccommodationImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccommodationImageRepository extends JpaRepository<AccommodationImage, Long> {
    
    /**
     * 숙소 ID로 이미지 목록 조회
     * @param accommodationId 숙소 ID
     * @return 숙소 이미지 목록
     */
    List<AccommodationImage> findByAccommodationId(Long accommodationId);
    
    /**
     * 숙소 ID로 대표 이미지 조회
     * @param accommodationId 숙소 ID
     * @return 대표 이미지
     */
    AccommodationImage findByAccommodationIdAndIsMainTrue(Long accommodationId);
} 