package com.yanolja.areas.room.service;

import com.yanolja.areas.room.dto.PortalRoomDto;
import com.yanolja.areas.room.dto.RoomImageDto;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.areas.room.entity.RoomImage;
import com.yanolja.areas.room.repository.RoomImageRepository;
import com.yanolja.areas.room.repository.RoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortalRoomService {
    
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomImageService roomImageService;
    
    /**
     * 객실 검색
     * @param request 검색 요청
     * @return 검색 결과 페이지
     */
    public Page<PortalRoomDto.ListResponse> searchRooms(PortalRoomDto.SearchRequest request) {
        PortalRoomDto.SearchCondition condition = request.getCondition();
        Pageable pageable = request.getPageRequest().toPageable(this::mapSortColumn);
        
        String sortBy = request.getPageRequest().getSortDirection() != null ?
                (request.getPageRequest().getSortColumn() + "_" + request.getPageRequest().getSortDirection().toLowerCase()) :
                null;
                
        Page<Room> rooms = roomRepository.searchRooms(
                condition.getKeyword(),
                condition.getMinPrice(),
                condition.getMaxPrice(),
                condition.getMinCapacity(),
                sortBy,
                pageable
        );
        
        return rooms.map(room -> {
            String mainImageUrl = roomImageService.getMainImageUrl(room.getId());
            return PortalRoomDto.ListResponse.fromEntityWithMainImage(room, mainImageUrl);
        });
    }
    
    /**
     * 객실 상세 조회
     * @param id 객실 ID
     * @return 객실 상세 정보
     */
    public PortalRoomDto.DetailResponse getRoomDetail(Long id) {
        Room room = roomRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + id + "인 객실을 찾을 수 없습니다."));
        
        // 이미지 정보 조회
        List<RoomImage> roomImages = roomImageRepository.findByRoomId(id);
        List<String> imageUrls = roomImages.stream()
                .map(RoomImage::getImageUrl)
                .collect(Collectors.toList());
        
        PortalRoomDto.DetailResponse response = PortalRoomDto.DetailResponse.fromEntity(room);
        response.setImageUrls(imageUrls);
        
        return response;
    }
    
    /**
     * 숙소별 객실 목록 조회
     * @param accommodationId 숙소 ID
     * @return 객실 목록
     */
    public List<PortalRoomDto.ListResponse> getRoomsByAccommodation(Long accommodationId) {
        List<Room> rooms = roomRepository.findByAccommodationIdAndNotDeleted(accommodationId);
        
        return rooms.stream()
                .map(room -> {
                    String mainImageUrl = roomImageService.getMainImageUrl(room.getId());
                    return PortalRoomDto.ListResponse.fromEntityWithMainImage(room, mainImageUrl);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 정렬 컬럼명 매핑 (DTO -> DB)
     */
    private String mapSortColumn(String original) {
        switch (original) {
            case "price":
                return "pricePerNight";
            case "capacity":
                return "capacity";
            default:
                return "id";
        }
    }
} 