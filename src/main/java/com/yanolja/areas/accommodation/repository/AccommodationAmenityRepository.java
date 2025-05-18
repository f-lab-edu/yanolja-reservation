package com.yanolja.areas.accommodation.repository;

import java.util.List;
import java.util.Optional;

import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.AccommodationAmenity;
import com.yanolja.areas.accommodation.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccommodationAmenityRepository extends JpaRepository<AccommodationAmenity, Long> {
    
    /**
     * 숙소에 연결된 모든 편의시설-숙소 매핑 조회
     * @param accommodation 숙소
     * @return 해당 숙소에 연결된 모든 편의시설 매핑
     */
    List<AccommodationAmenity> findByAccommodation(Accommodation accommodation);
    
    /**
     * 편의시설에 연결된 모든 숙소-편의시설 매핑 조회
     * @param amenity 편의시설
     * @return 해당 편의시설에 연결된 모든 숙소 매핑
     */
    List<AccommodationAmenity> findByAmenity(Amenity amenity);
    
    /**
     * 숙소와 편의시설로 매핑 검색
     * @param accommodation 숙소
     * @param amenity 편의시설
     * @return 해당 숙소와 편의시설의 매핑
     */
    Optional<AccommodationAmenity> findByAccommodationAndAmenity(Accommodation accommodation, Amenity amenity);
    
    /**
     * 숙소와 편의시설로 매핑 존재 여부 확인
     * @param accommodation 숙소
     * @param amenity 편의시설
     * @return 존재 여부
     */
    boolean existsByAccommodationAndAmenity(Accommodation accommodation, Amenity amenity);
    
    /**
     * 숙소ID로 모든 편의시설-숙소 매핑 삭제
     * @param accommodation 숙소
     */
    void deleteByAccommodation(Accommodation accommodation);
} 