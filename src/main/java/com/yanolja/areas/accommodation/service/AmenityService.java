package com.yanolja.areas.accommodation.service;

import java.util.List;
import java.util.stream.Collectors;

import com.yanolja.areas.accommodation.dto.AmenityDto;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.AccommodationAmenity;
import com.yanolja.areas.accommodation.entity.Amenity;
import com.yanolja.areas.accommodation.repository.AccommodationAmenityRepository;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import com.yanolja.areas.accommodation.repository.AmenityRepository;
import com.yanolja.common.exception.ErrorCode;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AmenityService {
    
    private final AmenityRepository amenityRepository;
    private final AccommodationRepository accommodationRepository;
    private final AccommodationAmenityRepository accommodationAmenityRepository;
    
    /**
     * 새로운 편의시설 등록
     * @param requestDto 편의시설 요청 DTO
     * @return 등록된 편의시설 응답 DTO
     */
    @Transactional
    public AmenityDto.Response createAmenity(AmenityDto.Request requestDto) {
        // 중복 이름 검사
        if (amenityRepository.existsByName(requestDto.getName())) {
            throw new EntityExistsException("이미 존재하는 편의시설 이름입니다: " + requestDto.getName());
        }
        
        // 새 편의시설 생성 및 저장
        Amenity amenity = Amenity.createAmenity(requestDto.getName(), requestDto.getIconUrl());
        Amenity savedAmenity = amenityRepository.save(amenity);
        
        log.info("새로운 편의시설이 등록되었습니다. ID: {}, 이름: {}", savedAmenity.getId(), savedAmenity.getName());
        
        return AmenityDto.Response.fromEntity(savedAmenity);
    }
    
    /**
     * 숙소에 편의시설 연결
     * @param accommodationId 숙소 ID
     * @param requestDto 편의시설 ID 목록 요청 DTO
     * @return 연결된 편의시설 응답 DTO 목록
     */
    @Transactional
    public List<AmenityDto.Response> connectAmenitiesToAccommodation(Long accommodationId, AmenityDto.ConnectionRequest requestDto) {
        // 숙소 조회
        Accommodation accommodation = findAccommodationById(accommodationId);
        
        // 편의시설 ID 목록을 순회하며 연결
        for (Long amenityId : requestDto.getAmenityIds()) {
            Amenity amenity = findAmenityById(amenityId);
            
            // 이미 연결되어 있는지 확인
            if (!accommodationAmenityRepository.existsByAccommodationAndAmenity(accommodation, amenity)) {
                // 새로운 연결 생성 및 저장
                AccommodationAmenity accommodationAmenity = AccommodationAmenity.createAccommodationAmenity(accommodation, amenity);
                accommodationAmenityRepository.save(accommodationAmenity);
                log.debug("숙소 ID: {}에 편의시설 ID: {}가 연결되었습니다.", accommodationId, amenityId);
            } else {
                log.debug("숙소 ID: {}에 편의시설 ID: {}가 이미 연결되어 있습니다.", accommodationId, amenityId);
            }
        }
        
        log.info("숙소 ID: {}에 {} 개의 편의시설이 연결되었습니다.", accommodationId, requestDto.getAmenityIds().size());
        
        // 연결된 편의시설 목록 조회 및 반환
        return getAmenitiesByAccommodationId(accommodationId);
    }
    
    /**
     * 숙소에 연결된 편의시설 목록 조회
     * @param accommodationId 숙소 ID
     * @return 편의시설 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<AmenityDto.Response> getAmenitiesByAccommodationId(Long accommodationId) {
        // 숙소 조회
        Accommodation accommodation = findAccommodationById(accommodationId);
        
        // 숙소에 연결된 편의시설 매핑 목록 조회
        List<AccommodationAmenity> accommodationAmenities = accommodationAmenityRepository.findByAccommodation(accommodation);
        
        // 편의시설 엔티티를 DTO로 변환하여 반환
        return accommodationAmenities.stream()
                .map(accommodationAmenity -> AmenityDto.Response.fromEntity(accommodationAmenity.getAmenity()))
                .collect(Collectors.toList());
    }
    
    /**
     * 편의시설 삭제
     * @param amenityId 삭제할 편의시설 ID
     */
    @Transactional
    public void deleteAmenity(Long amenityId) {
        Amenity amenity = findAmenityById(amenityId);
        
        // 해당 편의시설과 연결된 모든 숙소-편의시설 매핑을 먼저 삭제
        List<AccommodationAmenity> relatedMappings = accommodationAmenityRepository.findByAmenity(amenity);
        if (!relatedMappings.isEmpty()) {
            accommodationAmenityRepository.deleteAll(relatedMappings);
            log.debug("편의시설 ID: {}와 연결된 {} 개의 숙소-편의시설 매핑이 삭제되었습니다.", amenityId, relatedMappings.size());
        }
        
        // 편의시설 삭제
        amenityRepository.delete(amenity);
        log.info("편의시설이 삭제되었습니다. ID: {}, 이름: {}", amenity.getId(), amenity.getName());
    }
    
    /**
     * 숙소에서 특정 편의시설 연결 해제
     * @param accommodationId 숙소 ID
     * @param amenityId 편의시설 ID
     */
    @Transactional
    public void removeAmenityFromAccommodation(Long accommodationId, Long amenityId) {
        Accommodation accommodation = findAccommodationById(accommodationId);
        Amenity amenity = findAmenityById(amenityId);
        
        // 숙소-편의시설 매핑 조회
        AccommodationAmenity accommodationAmenity = accommodationAmenityRepository
                .findByAccommodationAndAmenity(accommodation, amenity)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("숙소 ID: %d와 편의시설 ID: %d의 연결을 찾을 수 없습니다.", accommodationId, amenityId)));
        
        // 매핑 삭제
        accommodationAmenityRepository.delete(accommodationAmenity);
        log.info("숙소 ID: {}에서 편의시설 ID: {}의 연결이 해제되었습니다.", accommodationId, amenityId);
    }
    
    /**
     * 편의시설 수정
     * @param amenityId 수정할 편의시설 ID
     * @param requestDto 편의시설 수정 요청 DTO
     * @return 수정된 편의시설 응답 DTO
     */
    @Transactional
    public AmenityDto.Response updateAmenity(Long amenityId, AmenityDto.Request requestDto) {
        Amenity amenity = findAmenityById(amenityId);
        
        // 다른 편의시설과 이름 중복 체크 (자기 자신 제외)
        if (!amenity.getName().equals(requestDto.getName()) && 
            amenityRepository.existsByName(requestDto.getName())) {
            throw new EntityExistsException("이미 존재하는 편의시설 이름입니다: " + requestDto.getName());
        }
        
        // 편의시설 정보 업데이트
        amenity.updateInfo(requestDto.getName(), requestDto.getIconUrl());
        
        log.info("편의시설이 수정되었습니다. ID: {}, 이름: {}", amenity.getId(), amenity.getName());
        
        return AmenityDto.Response.fromEntity(amenity);
    }
    
    /**
     * 전체 편의시설 목록 조회
     * @return 편의시설 응답 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<AmenityDto.Response> getAllAmenities() {
        List<Amenity> amenities = amenityRepository.findAll();
        
        return amenities.stream()
                .map(AmenityDto.Response::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * ID로 숙소 조회
     * @param id 숙소 ID
     * @return 숙소 엔티티
     * @throws EntityNotFoundException 숙소를 찾을 수 없는 경우
     */
    private Accommodation findAccommodationById(Long id) {
        return accommodationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + id + "인 숙소를 찾을 수 없습니다."));
    }
    
    /**
     * ID로 편의시설 조회
     * @param id 편의시설 ID
     * @return 편의시설 엔티티
     * @throws EntityNotFoundException 편의시설을 찾을 수 없는 경우
     */
    private Amenity findAmenityById(Long id) {
        return amenityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + id + "인 편의시설을 찾을 수 없습니다."));
    }
} 