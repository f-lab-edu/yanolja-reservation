package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.AmenityDto;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.AccommodationAmenity;
import com.yanolja.areas.accommodation.entity.Amenity;
import com.yanolja.areas.accommodation.repository.AccommodationAmenityRepository;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import com.yanolja.areas.accommodation.repository.AmenityRepository;
import com.yanolja.common.exception.UserException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AmenityServiceTest {

    @Mock
    private AmenityRepository amenityRepository;
    
    @Mock
    private AccommodationRepository accommodationRepository;
    
    @Mock
    private AccommodationAmenityRepository accommodationAmenityRepository;
    
    @InjectMocks
    private AmenityService amenityService;
    
    private Amenity testAmenity;
    private Accommodation testAccommodation;
    private AccommodationAmenity testAccommodationAmenity;
    
    @BeforeEach
    void setUp() {
        // 테스트용 편의시설 생성
        testAmenity = Amenity.builder()
                .name("와이파이")
                .iconUrl("https://example.com/icons/wifi.png")
                .build();
        // ID 설정
        ReflectionTestUtils.setField(testAmenity, "id", 1L);
        
        // 테스트용 숙소 생성
        testAccommodation = Accommodation.builder()
                .name("테스트 숙소")
                .description("테스트 숙소 설명")
                .address("서울시 강남구")
                .pricePerNight(new BigDecimal("100000"))
                .build();
        // ID 설정
        ReflectionTestUtils.setField(testAccommodation, "id", 1L);
        
        // 테스트용 숙소-편의시설 매핑 생성
        testAccommodationAmenity = AccommodationAmenity.builder()
                .accommodation(testAccommodation)
                .amenity(testAmenity)
                .build();
    }
    
    @Test
    @DisplayName("편의시설 생성 성공 테스트")
    void createAmenity_Success() {
        // Given
        AmenityDto.Request requestDto = AmenityDto.Request.builder()
                .name("와이파이")
                .iconUrl("https://example.com/icons/wifi.png")
                .build();
        
        when(amenityRepository.existsByName(anyString())).thenReturn(false);
        when(amenityRepository.save(any(Amenity.class))).thenReturn(testAmenity);
        
        // When
        AmenityDto.Response responseDto = amenityService.createAmenity(requestDto);
        
        // Then
        assertNotNull(responseDto);
        assertEquals(1L, responseDto.getId());
        assertEquals("와이파이", responseDto.getName());
        assertEquals("https://example.com/icons/wifi.png", responseDto.getIconUrl());
        
        verify(amenityRepository, times(1)).existsByName("와이파이");
        verify(amenityRepository, times(1)).save(any(Amenity.class));
    }
    
    @Test
    @DisplayName("중복 이름으로 편의시설 생성 실패 테스트")
    void createAmenity_DuplicateName_Fail() {
        // Given
        AmenityDto.Request requestDto = AmenityDto.Request.builder()
                .name("와이파이")
                .iconUrl("https://example.com/icons/wifi.png")
                .build();
        
        when(amenityRepository.existsByName(anyString())).thenReturn(true);
        
        // When & Then
        assertThrows(UserException.class, () -> amenityService.createAmenity(requestDto));
        
        verify(amenityRepository, times(1)).existsByName("와이파이");
        verify(amenityRepository, never()).save(any(Amenity.class));
    }
    
    @Test
    @DisplayName("숙소에 편의시설 연결 성공 테스트")
    void connectAmenitiesToAccommodation_Success() {
        // Given
        Long accommodationId = 1L;
        Long amenityId = 1L;
        AmenityDto.ConnectionRequest requestDto = AmenityDto.ConnectionRequest.builder()
                .amenityIds(List.of(amenityId))
                .build();
        
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(testAmenity));
        when(accommodationAmenityRepository.existsByAccommodationAndAmenity(testAccommodation, testAmenity)).thenReturn(false);
        when(accommodationAmenityRepository.save(any(AccommodationAmenity.class))).thenReturn(testAccommodationAmenity);
        when(accommodationAmenityRepository.findByAccommodation(testAccommodation)).thenReturn(List.of(testAccommodationAmenity));
        
        // When
        List<AmenityDto.Response> responseDtos = amenityService.connectAmenitiesToAccommodation(accommodationId, requestDto);
        
        // Then
        assertNotNull(responseDtos);
        assertEquals(1, responseDtos.size());
        assertEquals(1L, responseDtos.get(0).getId());
        assertEquals("와이파이", responseDtos.get(0).getName());
        
        verify(accommodationRepository, times(2)).findById(accommodationId);
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(accommodationAmenityRepository, times(1)).existsByAccommodationAndAmenity(testAccommodation, testAmenity);
        verify(accommodationAmenityRepository, times(1)).save(any(AccommodationAmenity.class));
        verify(accommodationAmenityRepository, times(1)).findByAccommodation(testAccommodation);
    }
    
    @Test
    @DisplayName("존재하지 않는 숙소에 편의시설 연결 실패 테스트")
    void connectAmenitiesToAccommodation_AccommodationNotFound_Fail() {
        // Given
        Long accommodationId = 1L;
        Long amenityId = 1L;
        AmenityDto.ConnectionRequest requestDto = AmenityDto.ConnectionRequest.builder()
                .amenityIds(List.of(amenityId))
                .build();
        
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> amenityService.connectAmenitiesToAccommodation(accommodationId, requestDto));
        
        verify(accommodationRepository, times(1)).findById(accommodationId);
        verify(amenityRepository, never()).findById(anyLong());
        verify(accommodationAmenityRepository, never()).existsByAccommodationAndAmenity(any(), any());
        verify(accommodationAmenityRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("이미 연결된 편의시설 중복 연결 테스트")
    void connectAmenitiesToAccommodation_AlreadyConnected_Success() {
        // Given
        Long accommodationId = 1L;
        Long amenityId = 1L;
        AmenityDto.ConnectionRequest requestDto = AmenityDto.ConnectionRequest.builder()
                .amenityIds(List.of(amenityId))
                .build();
        
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(testAmenity));
        when(accommodationAmenityRepository.existsByAccommodationAndAmenity(testAccommodation, testAmenity)).thenReturn(true);
        when(accommodationAmenityRepository.findByAccommodation(testAccommodation)).thenReturn(List.of(testAccommodationAmenity));
        
        // When
        List<AmenityDto.Response> responseDtos = amenityService.connectAmenitiesToAccommodation(accommodationId, requestDto);
        
        // Then
        assertNotNull(responseDtos);
        assertEquals(1, responseDtos.size());
        
        verify(accommodationRepository, times(2)).findById(accommodationId);
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(accommodationAmenityRepository, times(1)).existsByAccommodationAndAmenity(testAccommodation, testAmenity);
        verify(accommodationAmenityRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("숙소에 연결된 편의시설 목록 조회 성공 테스트")
    void getAmenitiesByAccommodationId_Success() {
        // Given
        Long accommodationId = 1L;
        
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
        when(accommodationAmenityRepository.findByAccommodation(testAccommodation)).thenReturn(List.of(testAccommodationAmenity));
        
        // When
        List<AmenityDto.Response> responseDtos = amenityService.getAmenitiesByAccommodationId(accommodationId);
        
        // Then
        assertNotNull(responseDtos);
        assertEquals(1, responseDtos.size());
        assertEquals(1L, responseDtos.get(0).getId());
        assertEquals("와이파이", responseDtos.get(0).getName());
        
        verify(accommodationRepository, times(1)).findById(accommodationId);
        verify(accommodationAmenityRepository, times(1)).findByAccommodation(testAccommodation);
    }
    
    @Test
    @DisplayName("존재하지 않는 숙소의 편의시설 목록 조회 실패 테스트")
    void getAmenitiesByAccommodationId_AccommodationNotFound_Fail() {
        // Given
        Long accommodationId = 1L;
        
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> amenityService.getAmenitiesByAccommodationId(accommodationId));
        
        verify(accommodationRepository, times(1)).findById(accommodationId);
        verify(accommodationAmenityRepository, never()).findByAccommodation(any());
    }
    
    @Test
    @DisplayName("편의시설 삭제 성공 테스트")
    void deleteAmenity_Success() {
        // Given
        Long amenityId = 1L;
        
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(testAmenity));
        when(accommodationAmenityRepository.findByAmenity(testAmenity)).thenReturn(
                Collections.singletonList(testAccommodationAmenity));
        
        // When
        amenityService.deleteAmenity(amenityId);
        
        // Then
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(accommodationAmenityRepository, times(1)).findByAmenity(testAmenity);
        verify(accommodationAmenityRepository, times(1)).deleteAll(any());
        verify(amenityRepository, times(1)).delete(testAmenity);
    }
    
    @Test
    @DisplayName("존재하지 않는 편의시설 삭제 실패 테스트")
    void deleteAmenity_NotFound_Fail() {
        // Given
        Long amenityId = 999L;
        
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> amenityService.deleteAmenity(amenityId));
        
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(accommodationAmenityRepository, never()).findByAmenity(any());
        verify(accommodationAmenityRepository, never()).deleteAll(any());
        verify(amenityRepository, never()).delete(any());
    }
    
    @Test
    @DisplayName("숙소-편의시설 연결 해제 성공 테스트")
    void removeAmenityFromAccommodation_Success() {
        // Given
        Long accommodationId = 1L;
        Long amenityId = 1L;
        
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(testAmenity));
        when(accommodationAmenityRepository.findByAccommodationAndAmenity(testAccommodation, testAmenity))
                .thenReturn(Optional.of(testAccommodationAmenity));
        
        // When
        amenityService.removeAmenityFromAccommodation(accommodationId, amenityId);
        
        // Then
        verify(accommodationRepository, times(1)).findById(accommodationId);
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(accommodationAmenityRepository, times(1)).findByAccommodationAndAmenity(testAccommodation, testAmenity);
        verify(accommodationAmenityRepository, times(1)).delete(testAccommodationAmenity);
    }
    
    @Test
    @DisplayName("존재하지 않는 숙소-편의시설 연결 해제 실패 테스트")
    void removeAmenityFromAccommodation_MappingNotFound_Fail() {
        // Given
        Long accommodationId = 1L;
        Long amenityId = 1L;
        
        when(accommodationRepository.findById(accommodationId)).thenReturn(Optional.of(testAccommodation));
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(testAmenity));
        when(accommodationAmenityRepository.findByAccommodationAndAmenity(testAccommodation, testAmenity))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, 
                () -> amenityService.removeAmenityFromAccommodation(accommodationId, amenityId));
        
        verify(accommodationRepository, times(1)).findById(accommodationId);
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(accommodationAmenityRepository, times(1)).findByAccommodationAndAmenity(testAccommodation, testAmenity);
        verify(accommodationAmenityRepository, never()).delete(any());
    }
    
    @Test
    @DisplayName("편의시설 수정 성공 테스트")
    void updateAmenity_Success() {
        // Given
        Long amenityId = 1L;
        AmenityDto.Request requestDto = AmenityDto.Request.builder()
                .name("수정된 와이파이")
                .iconUrl("https://example.com/icons/wifi_updated.png")
                .build();
        
        Amenity updatedAmenity = Amenity.builder()
                .name("수정된 와이파이")
                .iconUrl("https://example.com/icons/wifi_updated.png")
                .build();
        ReflectionTestUtils.setField(updatedAmenity, "id", 1L);
        
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(testAmenity));
        when(amenityRepository.existsByName(anyString())).thenReturn(false);
        
        // When
        AmenityDto.Response responseDto = amenityService.updateAmenity(amenityId, requestDto);
        
        // Then
        assertNotNull(responseDto);
        assertEquals(1L, responseDto.getId());
        assertEquals("수정된 와이파이", responseDto.getName());
        assertEquals("https://example.com/icons/wifi_updated.png", responseDto.getIconUrl());
        
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(amenityRepository, times(1)).existsByName("수정된 와이파이");
    }
    
    @Test
    @DisplayName("편의시설 수정 시 중복 이름 실패 테스트")
    void updateAmenity_DuplicateName_Fail() {
        // Given
        Long amenityId = 1L;
        AmenityDto.Request requestDto = AmenityDto.Request.builder()
                .name("중복된 이름")
                .iconUrl("https://example.com/icons/wifi_updated.png")
                .build();
        
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(testAmenity));
        when(amenityRepository.existsByName("중복된 이름")).thenReturn(true);
        
        // When & Then
        assertThrows(UserException.class, () -> amenityService.updateAmenity(amenityId, requestDto));
        
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(amenityRepository, times(1)).existsByName("중복된 이름");
    }
    
    @Test
    @DisplayName("존재하지 않는 편의시설 수정 실패 테스트")
    void updateAmenity_NotFound_Fail() {
        // Given
        Long amenityId = 999L;
        AmenityDto.Request requestDto = AmenityDto.Request.builder()
                .name("수정된 와이파이")
                .iconUrl("https://example.com/icons/wifi_updated.png")
                .build();
        
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> amenityService.updateAmenity(amenityId, requestDto));
        
        verify(amenityRepository, times(1)).findById(amenityId);
        verify(amenityRepository, never()).existsByName(anyString());
    }
    
    @Test
    @DisplayName("이름 변경 없는 편의시설 수정 성공 테스트")
    void updateAmenity_SameName_Success() {
        // Given
        Long amenityId = 1L;
        AmenityDto.Request requestDto = AmenityDto.Request.builder()
                .name("와이파이") // 기존과 동일한 이름
                .iconUrl("https://example.com/icons/wifi_updated.png")
                .build();
        
        when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(testAmenity));
        
        // When
        AmenityDto.Response responseDto = amenityService.updateAmenity(amenityId, requestDto);
        
        // Then
        assertNotNull(responseDto);
        assertEquals(1L, responseDto.getId());
        assertEquals("와이파이", responseDto.getName());
        assertEquals("https://example.com/icons/wifi_updated.png", responseDto.getIconUrl());
        
        verify(amenityRepository, times(1)).findById(amenityId);
        // 이름이 변경되지 않으면 중복 체크가 실행되지 않음
        verify(amenityRepository, never()).existsByName(anyString());
    }
} 