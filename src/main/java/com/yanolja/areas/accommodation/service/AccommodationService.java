package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.AccommodationDto;
import com.yanolja.areas.accommodation.dto.AccommodationImageDto;
import com.yanolja.areas.accommodation.dto.AmenityDto;
import com.yanolja.areas.accommodation.entity.*;
import com.yanolja.areas.accommodation.repository.*;
import com.yanolja.areas.room.dto.RoomDto;
import com.yanolja.areas.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationImageRepository accommodationImageRepository;
    private final AccommodationImageService accommodationImageService;
    private final AmenityService amenityService;
    private final RoomService roomService;

    @Transactional
    public AccommodationDto.Response createAccommodation(AccommodationDto.Request request) {
        // 1. 숙소 기본 정보 생성
        Accommodation accommodation = Accommodation.createAccommodation(
            request.getName(),
            request.getDescription(),
            request.getAddress(),
            request.getLatitude(),
            request.getLongitude(),
            request.getPricePerNight()
        );

        accommodation = accommodationRepository.save(accommodation);
        
        return AccommodationDto.Response.fromEntity(accommodation);
    }

    @Transactional(readOnly = true)
    public List<AccommodationDto.ListResponse> getAllAccommodations() {
        List<Accommodation> accommodations = accommodationRepository.findAllActive();
        
        return accommodations.stream()
            .map(accommodation -> {
                String mainImageUrl = accommodationImageService.getMainImageUrl(accommodation.getId());
                return AccommodationDto.ListResponse.fromEntityWithMainImage(accommodation, mainImageUrl);
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccommodationDto.Response getAccommodationById(Long id) {
        Accommodation accommodation = findAccommodationById(id);
        
        // 숙소 이미지 목록 조회
        List<AccommodationImage> images = accommodationImageRepository.findByAccommodationId(id);
        
        // 편의시설 목록 조회
        List<AmenityDto.Response> amenities = amenityService.getAmenitiesByAccommodationId(id);
        
        // 객실 목록 조회
        List<RoomDto.ListResponse> rooms = roomService.getRoomsByAccommodationId(id);
        
        // DTO 변환 및 반환
        AccommodationDto.Response responseDto = AccommodationDto.Response.fromEntity(accommodation);
        
        // 이미지와 편의시설 목록 설정
        responseDto.setImages(
            images.stream()
                .map(AccommodationImageDto.Response::fromEntity)
                .collect(Collectors.toList())
        );
        
        responseDto.setAmenities(amenities);
        
        // 객실 목록 설정
        responseDto.setRooms(rooms);
        
        return responseDto;
    }
    
    @Transactional
    public AccommodationDto.Response updateAccommodation(Long id, AccommodationDto.Request request) {
        Accommodation accommodation = findAccommodationById(id);
        
        accommodation.updateInfo(
            request.getName(),
            request.getDescription(),
            request.getAddress(),
            request.getLatitude(),
            request.getLongitude(),
            request.getPricePerNight()
        );
        
        return AccommodationDto.Response.fromEntity(accommodation);
    }
    
    @Transactional
    public void deleteAccommodation(Long id) {
        Accommodation accommodation = findAccommodationById(id);
        accommodation.markAsDeleted();
    }
    
    private Accommodation findAccommodationById(Long id) {
        return accommodationRepository.findByIdAndNotDeleted(id)
            .orElseThrow(() -> new EntityNotFoundException("ID가 " + id + "인 숙소를 찾을 수 없습니다."));
    }
} 