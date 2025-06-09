package com.yanolja.areas.room.service;

import com.yanolja.areas.room.dto.PortalRoomDto;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.areas.room.entity.RoomImage;
import com.yanolja.areas.room.entity.RoomOptionMapping;
import com.yanolja.areas.room.repository.RoomImageRepository;
import com.yanolja.areas.room.repository.RoomRepository;
import com.yanolja.areas.room.repository.RoomOptionMappingRepository;
import com.yanolja.common.dto.PageRequestDto;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortalRoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    
    @Mock
    private RoomImageRepository roomImageRepository;
    
    @Mock
    private RoomImageService roomImageService;
    
    @Mock
    private RoomOptionMappingRepository roomOptionMappingRepository;
    
    @InjectMocks
    private PortalRoomService portalRoomService;
    
    // 테스트 데이터
    private Room room1;
    private Room room2;
    private Room detailRoom;
    private RoomImage roomImage1;
    private RoomImage roomImage2;
    private final String MAIN_IMAGE_URL = "/api/rooms/images/1/main.jpg";
    
    @BeforeEach
    void setUp() {
        // 기본 객실 데이터 설정
        room1 = createMockRoom(1L, 1L, "디럭스 더블룸", "편안한 디럭스 더블룸", 2, new BigDecimal("120000"));
        room2 = createMockRoom(2L, 1L, "스위트 룸", "고급스러운 스위트 룸", 4, new BigDecimal("200000"));
        
        // 상세 조회용 객실 데이터
        detailRoom = createDetailMockRoom(
                1L, 
                1L,
                "디럭스 더블룸", 
                "편안하고 아늑한 디럭스 더블룸입니다. 커플에게 적합합니다.", 
                2,
                new BigDecimal("120000")
        );
        
        // 객실 이미지 데이터 설정
        roomImage1 = createMockRoomImage(1L, detailRoom, "/api/rooms/images/1/main.jpg", true);
        roomImage2 = createMockRoomImage(2L, detailRoom, "/api/rooms/images/1/sub.jpg", false);
        
        // 기본 모킹 설정
        lenient().when(roomRepository.findById(1L)).thenReturn(Optional.of(detailRoom));
        lenient().when(roomRepository.findById(999L)).thenReturn(Optional.empty());
        lenient().when(roomRepository.findByAccommodationId(1L)).thenReturn(Arrays.asList(room1, room2));
        lenient().when(roomImageRepository.findByRoomId(1L)).thenReturn(Arrays.asList(roomImage1, roomImage2));
        lenient().when(roomImageService.getMainImageUrl(1L)).thenReturn(MAIN_IMAGE_URL);
        lenient().when(roomImageService.getMainImageUrl(2L)).thenReturn("/api/rooms/images/2/main.jpg");
    }

    @Test
    @DisplayName("객실 검색 - 키워드 검색 성공 (이미지 포함)")
    void searchRooms_WithKeyword_ShouldReturnRoomsWithMainImage() {
        // Given
        String keyword = "디럭스";
        BigDecimal minPrice = new BigDecimal("100000");
        BigDecimal maxPrice = new BigDecimal("150000");
        Integer minCapacity = 2;
        
        PortalRoomDto.SearchCondition condition = PortalRoomDto.SearchCondition.builder()
                .keyword(keyword)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minCapacity(minCapacity)
                .build();
        
        PageRequestDto pageRequest = PageRequestDto.builder()
                .page(0)
                .size(10)
                .sortColumn("price")
                .sortDirection("asc")
                .build();
        
        PortalRoomDto.SearchRequest request = PortalRoomDto.SearchRequest.builder()
                .condition(condition)
                .pageRequest(pageRequest)
                .build();
        
        Pageable pageable = pageRequest.toPageable();
        String sortBy = "price_asc";
                
        Page<Room> mockPage = new PageImpl<>(
                List.of(room1), 
                pageable, 
                1
        );
        
        when(roomRepository.searchRooms(
                eq(keyword), 
                eq(minPrice), 
                eq(maxPrice),
                eq(minCapacity),
                eq(sortBy), 
                any(Pageable.class)
        )).thenReturn(mockPage);
        
        // When
        Page<PortalRoomDto.ListResponse> result = portalRoomService.searchRooms(request);
        
        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("디럭스 더블룸");
        assertThat(result.getContent().get(0).getMainImageUrl()).isEqualTo(MAIN_IMAGE_URL);
        
        verify(roomRepository).searchRooms(
                eq(keyword), 
                eq(minPrice), 
                eq(maxPrice),
                eq(minCapacity),
                eq(sortBy), 
                any(Pageable.class)
        );
        verify(roomImageService).getMainImageUrl(1L);
    }
    
    @Test
    @DisplayName("객실 검색 - 검색 결과 없음")
    void searchRooms_WithNoResults_ShouldReturnEmptyPage() {
        // Given
        String keyword = "존재하지 않는 객실";
        
        PortalRoomDto.SearchCondition condition = PortalRoomDto.SearchCondition.builder()
                .keyword(keyword)
                .build();
        
        PageRequestDto pageRequest = PageRequestDto.builder()
                .page(0)
                .size(10)
                .build();
        
        PortalRoomDto.SearchRequest request = PortalRoomDto.SearchRequest.builder()
                .condition(condition)
                .pageRequest(pageRequest)
                .build();
        
        Pageable pageable = pageRequest.toPageable();
                
        Page<Room> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        
        when(roomRepository.searchRooms(
                eq(keyword), 
                any(), 
                any(),
                any(),
                any(), 
                any(Pageable.class)
        )).thenReturn(emptyPage);
        
        // When
        Page<PortalRoomDto.ListResponse> result = portalRoomService.searchRooms(request);
        
        // Then
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }
    
    @Test
    @DisplayName("객실 상세 조회 - 존재하는 객실 ID")
    void getRoomDetail_WithExistingId_ShouldReturnRoomDetail() {
        // When
        PortalRoomDto.DetailResponse result = portalRoomService.getRoomDetail(1L);
        
        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("디럭스 더블룸");
        assertThat(result.getDescription()).isEqualTo("편안하고 아늑한 디럭스 더블룸입니다. 커플에게 적합합니다.");
        assertThat(result.getCapacity()).isEqualTo(2);
        assertThat(result.getPricePerNight()).isEqualTo(new BigDecimal("120000"));
        
        verify(roomRepository).findById(1L);
    }
    
    @Test
    @DisplayName("객실 상세 조회 - 존재하지 않는 객실 ID")
    void getRoomDetail_WithNonExistingId_ShouldThrowException() {
        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            portalRoomService.getRoomDetail(999L);
        });
        
        assertThat(exception.getMessage()).contains("999");
        verify(roomRepository).findById(999L);
    }
    
    @Test
    @DisplayName("숙소별 객실 목록 조회 - 성공")
    void getRoomsByAccommodation_ShouldReturnRoomList() {
        // When
        List<PortalRoomDto.ListResponse> result = portalRoomService.getRoomsByAccommodation(1L);
        
        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("디럭스 더블룸");
        assertThat(result.get(1).getName()).isEqualTo("스위트 룸");
        
        verify(roomRepository).findByAccommodationId(1L);
    }
    
    @Test
    @DisplayName("객실 상세 조회 - 이미지 URL 포함")
    void getRoomDetail_WithExistingId_ShouldIncludeImageUrls() {
        // When
        PortalRoomDto.DetailResponse result = portalRoomService.getRoomDetail(1L);
        
        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("디럭스 더블룸");
        
        // 이미지 URL 목록 검증
        assertThat(result.getImageUrls()).isNotNull();
        assertThat(result.getImageUrls()).hasSize(2);
        assertThat(result.getImageUrls()).contains("/api/rooms/images/1/main.jpg", "/api/rooms/images/1/sub.jpg");
        
        verify(roomRepository).findById(1L);
        verify(roomImageRepository).findByRoomId(1L);
    }
    
    @Test
    @DisplayName("숙소별 객실 목록 조회 - 메인 이미지 URL 포함")
    void getRoomsByAccommodation_ShouldIncludeMainImageUrl() {
        // When
        List<PortalRoomDto.ListResponse> results = portalRoomService.getRoomsByAccommodation(1L);
        
        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getMainImageUrl()).isEqualTo(MAIN_IMAGE_URL);
        assertThat(results.get(1).getMainImageUrl()).isEqualTo("/api/rooms/images/2/main.jpg");
        
        verify(roomRepository).findByAccommodationId(1L);
        verify(roomImageService).getMainImageUrl(1L);
        verify(roomImageService).getMainImageUrl(2L);
    }
    
    @Test
    @DisplayName("특정 옵션을 사용하는 객실 목록 조회 (포털용) - 성공")
    void getRoomsByOptionId_ShouldReturnRoomsWithOption() {
        // Given
        RoomOptionMapping mapping1 = mock(RoomOptionMapping.class);
        RoomOptionMapping mapping2 = mock(RoomOptionMapping.class);
        
        when(mapping1.getRoom()).thenReturn(room1);
        when(mapping2.getRoom()).thenReturn(room2);
        
        when(roomOptionMappingRepository.findByRoomOptionIdAndRoomNotDeleted(1L))
                .thenReturn(Arrays.asList(mapping1, mapping2));
        
        // When
        List<PortalRoomDto.ListResponse> results = portalRoomService.getRoomsByOptionId(1L);
        
        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("디럭스 더블룸");
        assertThat(results.get(1).getName()).isEqualTo("스위트 룸");
        assertThat(results.get(0).getMainImageUrl()).isEqualTo(MAIN_IMAGE_URL);
        
        verify(roomOptionMappingRepository).findByRoomOptionIdAndRoomNotDeleted(1L);
    }

    @Test
    @DisplayName("존재하지 않는 옵션으로 객실 조회 - 빈 목록 반환")
    void getRoomsByOptionId_WithNonExistingOption_ShouldReturnEmptyList() {
        // Given
        when(roomOptionMappingRepository.findByRoomOptionIdAndRoomNotDeleted(999L))
                .thenReturn(Arrays.asList());
        
        // When
        List<PortalRoomDto.ListResponse> results = portalRoomService.getRoomsByOptionId(999L);
        
        // Then
        assertThat(results).isEmpty();
        
        verify(roomOptionMappingRepository).findByRoomOptionIdAndRoomNotDeleted(999L);
    }
    
    // 테스트 데이터 생성을 위한 도우미 메서드
    private Room createMockRoom(Long id, Long accommodationId, String name, String description, 
                               Integer capacity, BigDecimal pricePerNight) {
        Room room = Room.createRoom(
                accommodationId,
                name,
                description,
                capacity,
                pricePerNight
        );
                
        ReflectionTestUtils.setField(room, "id", id);
        ReflectionTestUtils.setField(room, "deletedYn", false);
        return room;
    }
    
    private Room createDetailMockRoom(Long id, Long accommodationId, String name, String description, 
                                     Integer capacity, BigDecimal pricePerNight) {
        Room room = Room.createRoom(
                accommodationId,
                name,
                description,
                capacity,
                pricePerNight
        );
                
        ReflectionTestUtils.setField(room, "id", id);
        ReflectionTestUtils.setField(room, "deletedYn", false);
        ReflectionTestUtils.setField(room, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(room, "updatedAt", LocalDateTime.now());
        
        return room;
    }
    
    /**
     * 테스트용 RoomImage 객체 생성
     */
    private RoomImage createMockRoomImage(Long id, Room room, String imageUrl, boolean isMain) {
        RoomImage image = RoomImage.builder()
                .room(room)
                .imageUrl(imageUrl)
                .isMain(isMain)
                .build();
        
        ReflectionTestUtils.setField(image, "id", id);
        return image;
    }
} 