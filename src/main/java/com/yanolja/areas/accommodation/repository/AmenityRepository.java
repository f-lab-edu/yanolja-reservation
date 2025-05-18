package com.yanolja.areas.accommodation.repository;

import java.util.Optional;

import com.yanolja.areas.accommodation.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    
    /**
     * 이름으로 편의시설 조회
     * @param name 시설 이름
     * @return 해당 이름의 편의시설
     */
    Optional<Amenity> findByName(String name);
    
    /**
     * 이름으로 편의시설 존재 여부 확인
     * @param name 시설 이름
     * @return 존재 여부
     */
    boolean existsByName(String name);
} 