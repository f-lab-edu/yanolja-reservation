package com.yanolja.areas.room.repository;

import com.yanolja.areas.room.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RoomRepositoryCustom {
    
    /**
     * 삭제되지 않은 모든 객실 목록 조회
     */
    List<Room> findAllNotDeleted();
    
    /**
     * ID로 객실 상세 조회 (삭제되지 않은 객실만)
     */
    Optional<Room> findByIdAndNotDeleted(Long id);
    
    /**
     * 숙소별 객실 목록 조회 (삭제되지 않은 객실만)
     */
    List<Room> findByAccommodationIdAndNotDeleted(Long accommodationId);
    
    /**
     * 검색 조건에 맞는 객실 목록을 페이징하여 조회
     * 
     * @param keyword 검색 키워드 (객실명, 설명)
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param minCapacity 최소 수용 인원
     * @param sortBy 정렬 기준 (price_asc, price_desc, capacity_asc, capacity_desc)
     * @param pageable 페이징 정보
     * @return 객실 목록 페이지
     */
    Page<Room> searchRooms(
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minCapacity,
            String sortBy,
            Pageable pageable
    );
} 