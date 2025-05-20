package com.yanolja.areas.room.service;

import com.yanolja.areas.room.dto.RoomDto;
import com.yanolja.areas.room.dto.RoomImageDto;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.areas.room.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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
        return RoomDto.Response.fromEntity(savedRoom);
    }

    public List<RoomDto.ListResponse> getAllRooms() {
        return roomRepository.findAllNotDeleted().stream()
                .map(room -> {
                    String mainImageUrl = roomImageService.getMainImageUrl(room.getId());
                    return RoomDto.ListResponse.fromEntityWithMainImage(room, mainImageUrl);
                })
                .collect(Collectors.toList());
    }

    public RoomDto.Response getRoomById(Long id) {
        Room room = roomRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("객실을 찾을 수 없습니다."));
                
        RoomDto.Response response = RoomDto.Response.fromEntity(room);
        
        // 이미지 정보 추가
        RoomImageDto.ListResponse imageList = roomImageService.getImagesByRoomId(id);
        response.setImages(imageList.getImages());
        
        return response;
    }

    public List<RoomDto.ListResponse> getRoomsByAccommodationId(Long accommodationId) {
        return roomRepository.findByAccommodationIdAndNotDeleted(accommodationId).stream()
                .map(room -> {
                    String mainImageUrl = roomImageService.getMainImageUrl(room.getId());
                    return RoomDto.ListResponse.fromEntityWithMainImage(room, mainImageUrl);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDto.Response updateRoom(Long id, RoomDto.Request request) {
        Room room = roomRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("객실을 찾을 수 없습니다."));

        room.update(
                request.getName(),
                request.getDescription(),
                request.getCapacity(),
                request.getPricePerNight()
        );

        return RoomDto.Response.fromEntity(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("객실을 찾을 수 없습니다."));
        room.delete();
    }

} 