package com.yanolja.areas.accommodation.controller;

import java.util.List;

import com.yanolja.areas.accommodation.dto.AmenityDto;
import com.yanolja.areas.accommodation.service.AmenityService;
import com.yanolja.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AmenityController {
    
    private final AmenityService amenityService;
    
    /**
     * 새로운 편의시설 등록
     * @param requestDto 편의시설 요청 DTO
     * @return ApiResponse<AmenityDto.Response> 등록된 편의시설 정보
     */
    @PostMapping("/amenities")
    public ResponseEntity<ApiResponse<AmenityDto.Response>> createAmenity(
            @Valid @RequestBody AmenityDto.Request requestDto) {
        
        log.info("편의시설 등록 요청: {}", requestDto.getName());
        AmenityDto.Response responseDto = amenityService.createAmenity(requestDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(responseDto));
    }
    
    /**
     * 숙소에 편의시설 연결
     * @param accommodationId 숙소 ID
     * @param requestDto 편의시설 ID 목록 요청 DTO
     * @return ApiResponse<List<AmenityDto.Response>> 연결된 편의시설 목록
     */
    @PostMapping("/accommodations/{id}/amenities")
    public ResponseEntity<ApiResponse<List<AmenityDto.Response>>> connectAmenitiesToAccommodation(
            @PathVariable("id") Long accommodationId,
            @Valid @RequestBody AmenityDto.ConnectionRequest requestDto) {
        
        log.info("숙소 ID: {}에 편의시설 연결 요청, 편의시설 개수: {}", accommodationId, requestDto.getAmenityIds().size());
        List<AmenityDto.Response> responseDtos = amenityService.connectAmenitiesToAccommodation(accommodationId, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success(responseDtos));
    }
    
    /**
     * 숙소에 연결된 편의시설 목록 조회
     * @param accommodationId 숙소 ID
     * @return ApiResponse<List<AmenityDto.Response>> 편의시설 목록
     */
    @GetMapping("/accommodations/{id}/amenities")
    public ResponseEntity<ApiResponse<List<AmenityDto.Response>>> getAmenitiesByAccommodationId(
            @PathVariable("id") Long accommodationId) {
        
        log.info("숙소 ID: {}의 편의시설 목록 조회 요청", accommodationId);
        List<AmenityDto.Response> responseDtos = amenityService.getAmenitiesByAccommodationId(accommodationId);
        
        return ResponseEntity.ok(ApiResponse.success(responseDtos));
    }
    
    /**
     * 편의시설 삭제
     * @param amenityId 삭제할 편의시설 ID
     * @return ApiResponse 삭제 결과
     */
    @DeleteMapping("/amenities/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAmenity(@PathVariable("id") Long amenityId) {
        log.info("편의시설 삭제 요청, ID: {}", amenityId);
        amenityService.deleteAmenity(amenityId);
        
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 숙소에서 특정 편의시설 연결 해제
     * @param accommodationId 숙소 ID
     * @param amenityId 편의시설 ID
     * @return ApiResponse 연결 해제 결과
     */
    @DeleteMapping("/accommodations/{accommodationId}/amenities/{amenityId}")
    public ResponseEntity<ApiResponse<Void>> removeAmenityFromAccommodation(
            @PathVariable Long accommodationId,
            @PathVariable Long amenityId) {
        
        log.info("숙소 ID: {}에서 편의시설 ID: {} 연결 해제 요청", accommodationId, amenityId);
        amenityService.removeAmenityFromAccommodation(accommodationId, amenityId);
        
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 편의시설 수정
     * @param amenityId 수정할 편의시설 ID
     * @param requestDto 편의시설 수정 요청 DTO
     * @return ApiResponse<AmenityDto.Response> 수정된 편의시설 정보
     */
    @PutMapping("/amenities/{id}")
    public ResponseEntity<ApiResponse<AmenityDto.Response>> updateAmenity(
            @PathVariable("id") Long amenityId,
            @Valid @RequestBody AmenityDto.Request requestDto) {
        
        log.info("편의시설 수정 요청, ID: {}, 이름: {}", amenityId, requestDto.getName());
        AmenityDto.Response responseDto = amenityService.updateAmenity(amenityId, requestDto);
        
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }
} 