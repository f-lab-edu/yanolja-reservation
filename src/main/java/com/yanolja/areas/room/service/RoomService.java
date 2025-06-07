package com.yanolja.areas.room.service;

import com.yanolja.areas.room.dto.OptionUsageStatisticsDto;
import com.yanolja.areas.room.dto.RoomDto;
import com.yanolja.areas.room.dto.RoomImageDto;
import com.yanolja.areas.room.dto.RoomOptionCountStatisticsDto;
import com.yanolja.areas.room.dto.RoomOptionDto;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.areas.room.entity.RoomOption;
import com.yanolja.areas.room.entity.RoomOptionMapping;
import com.yanolja.areas.room.repository.RoomRepository;
import com.yanolja.areas.room.repository.RoomImageRepository;
import com.yanolja.areas.room.repository.RoomOptionMappingRepository;
import com.yanolja.areas.room.repository.RoomOptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomImageService roomImageService;
    private final RoomOptionMappingRepository roomOptionMappingRepository;
    private final RoomOptionRepository roomOptionRepository;

    @Transactional
    public RoomDto.Response createRoom(RoomDto.Request request) {
        Room room = Room.createRoom(
                request.getAccommodationId(),
                request.getName(),
                request.getDescription(),
                request.getCapacity(),
                request.getPricePerNight()
        );

        Room savedRoom = roomRepository.save(room);

        // 객실 옵션 추가
        if (request.getOptionIds() != null && !request.getOptionIds().isEmpty()) {
            addRoomOptions(savedRoom.getId(), request.getOptionIds());
        }

        return getRoomById(savedRoom.getId());
    }

    public List<RoomDto.ListResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(room -> {
                    String mainImageUrl = roomImageService.getMainImageUrl(room.getId());
                    return RoomDto.ListResponse.fromEntityWithMainImage(room, mainImageUrl);
                })
                .collect(Collectors.toList());
    }

    public RoomDto.Response getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("객실을 찾을 수 없습니다."));
                
        // 이미지 정보 추가
        RoomImageDto.ListResponse imageList = roomImageService.getImagesByRoomId(id);
        
        // 옵션 정보 추가
        List<RoomOptionDto.ListResponse> options = getRoomOptions(id);
        
        return RoomDto.Response.fromEntityWithImagesAndOptions(room, imageList.getImages(), options);
    }

    public List<RoomDto.ListResponse> getRoomsByAccommodationId(Long accommodationId) {
        return roomRepository.findByAccommodationId(accommodationId).stream()
                .map(room -> {
                    String mainImageUrl = roomImageService.getMainImageUrl(room.getId());
                    return RoomDto.ListResponse.fromEntityWithMainImage(room, mainImageUrl);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDto.Response updateRoom(Long id, RoomDto.Request request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("객실을 찾을 수 없습니다."));

        room.update(
                request.getName(),
                request.getDescription(),
                request.getCapacity(),
                request.getPricePerNight()
        );

        // 기존 옵션 매핑 제거 후 새로운 옵션 추가
        if (request.getOptionIds() != null) {
            removeAllRoomOptions(id);
            if (!request.getOptionIds().isEmpty()) {
                addRoomOptions(id, request.getOptionIds());
            }
        }

        return getRoomById(id);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("객실을 찾을 수 없습니다."));
        
        // 객실 옵션 매핑 제거
        removeAllRoomOptions(id);
        
        room.delete();
    }

    /**
     * 객실에 옵션 추가
     * @param roomId 객실 ID
     * @param optionIds 추가할 옵션 ID 목록
     */
    @Transactional
    public void addRoomOptions(Long roomId, List<Long> optionIds) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("객실을 찾을 수 없습니다."));

        for (Long optionId : optionIds) {
            // 이미 존재하는 매핑인지 확인
            boolean exists = roomOptionMappingRepository
                    .existsByRoomIdAndRoomOptionId(roomId, optionId);
            
            if (!exists) {
                RoomOption roomOption = roomOptionRepository.findById(optionId)
                        .orElseThrow(() -> new EntityNotFoundException("옵션을 찾을 수 없습니다."));
                room.addRoomOption(roomOption);
            }
        }
    }

    /**
     * 객실에서 특정 옵션 제거
     * @param roomId 객실 ID
     * @param optionId 제거할 옵션 ID
     */
    @Transactional
    public void removeRoomOption(Long roomId, Long optionId) {
        roomOptionMappingRepository.deleteByRoomIdAndRoomOptionId(roomId, optionId);
    }

    /**
     * 객실의 모든 옵션 제거
     * @param roomId 객실 ID
     */
    @Transactional
    public void removeAllRoomOptions(Long roomId) {
        roomOptionMappingRepository.deleteByRoomId(roomId);
    }

    /**
     * 객실의 옵션 목록 조회
     * @param roomId 객실 ID
     * @return 옵션 목록
     */
    public List<RoomOptionDto.ListResponse> getRoomOptions(Long roomId) {
        List<RoomOptionMapping> mappings = roomOptionMappingRepository.findByRoomIdAndRoomNotDeleted(roomId);
        return mappings.stream()
                .map(mapping -> RoomOptionDto.ListResponse.fromEntity(mapping.getRoomOption()))
                .collect(Collectors.toList());
    }

    /**
     * 특정 옵션을 사용하는 객실 목록 조회
     * @param optionId 옵션 ID
     * @return 객실 목록
     */
    public List<RoomDto.ListResponse> getRoomsByOptionId(Long optionId) {
        List<RoomOptionMapping> mappings = roomOptionMappingRepository.findByRoomOptionIdAndRoomNotDeleted(optionId);
        return mappings.stream()
                .map(mapping -> {
                    Room room = mapping.getRoom();
                    String mainImageUrl = roomImageService.getMainImageUrl(room.getId());
                    return RoomDto.ListResponse.fromEntityWithMainImage(room, mainImageUrl);
                })
                .collect(Collectors.toList());
    }

    /**
     * 여러 옵션을 모두 가진 객실 목록 조회
     * @param optionIds 옵션 ID 목록
     * @return 모든 옵션을 가진 객실 목록
     */
    public List<RoomDto.ListResponse> getRoomsWithAllOptions(List<Long> optionIds) {
        List<RoomOptionMapping> mappings = roomOptionMappingRepository.findRoomsWithAllOptions(optionIds);
        return mappings.stream()
                .map(RoomOptionMapping::getRoom)
                .distinct()
                .map(room -> {
                    String mainImageUrl = roomImageService.getMainImageUrl(room.getId());
                    return RoomDto.ListResponse.fromEntityWithMainImage(room, mainImageUrl);
                })
                .collect(Collectors.toList());
    }

    /**
     * 여러 옵션 중 하나 이상을 가진 객실 목록 조회
     * @param optionIds 옵션 ID 목록
     * @return 옵션 중 하나 이상을 가진 객실 목록
     */
    public List<RoomDto.ListResponse> getRoomsWithAnyOptions(List<Long> optionIds) {
        List<RoomOptionMapping> mappings = roomOptionMappingRepository.findRoomsWithAnyOptions(optionIds);
        return mappings.stream()
                .map(RoomOptionMapping::getRoom)
                .distinct()
                .map(room -> {
                    String mainImageUrl = roomImageService.getMainImageUrl(room.getId());
                    return RoomDto.ListResponse.fromEntityWithMainImage(room, mainImageUrl);
                })
                .collect(Collectors.toList());
    }

    /**
     * 숙소별 객실-옵션 매핑 조회
     * @param accommodationId 숙소 ID
     * @return 객실 목록
     */
    public List<RoomDto.ListResponse> getRoomsByAccommodationIdWithOptions(Long accommodationId) {
        List<RoomOptionMapping> mappings = roomOptionMappingRepository.findByAccommodationId(accommodationId);
        return mappings.stream()
                .map(RoomOptionMapping::getRoom)
                .distinct()
                .map(room -> {
                    String mainImageUrl = roomImageService.getMainImageUrl(room.getId());
                    return RoomDto.ListResponse.fromEntityWithMainImage(room, mainImageUrl);
                })
                .collect(Collectors.toList());
    }

    /**
     * 옵션별 사용 통계 조회
     * @return 옵션 사용 통계
     */
    public List<OptionUsageStatisticsDto> getOptionUsageStatistics() {
        return roomOptionMappingRepository.getOptionUsageStatistics();
    }

    /**
     * 객실별 옵션 개수 통계 조회
     * @return 객실별 옵션 개수 통계
     */
    public List<RoomOptionCountStatisticsDto> getRoomOptionCountStatistics() {
        return roomOptionMappingRepository.getRoomOptionCountStatistics();
    }
} 