package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.AccommodationDto;
import com.yanolja.areas.accommodation.entity.*;
import com.yanolja.areas.accommodation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;

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
        List<Accommodation> accommodations = accommodationRepository.findAll();
        return accommodations.stream()
                .map(AccommodationDto.ListResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccommodationDto.Response getAccommodationById(Long id) {
        Accommodation accommodation = findAccommodationById(id);
        return AccommodationDto.Response.fromEntity(accommodation);
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

        accommodation = accommodationRepository.save(accommodation);
        
        return AccommodationDto.Response.fromEntity(accommodation);
    }

    @Transactional
    public void deleteAccommodation(Long id) {
        Accommodation accommodation = findAccommodationById(id);
        accommodation.markAsDeleted();
        accommodationRepository.save(accommodation);
    }
    
    private Accommodation findAccommodationById(Long id) {
        return accommodationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("ID가 " + id + "인 숙소를 찾을 수 없습니다."));
    }
} 