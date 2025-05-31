package com.yanolja.areas.room.service;

import com.yanolja.areas.room.dto.PortalRoomDto;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.areas.room.repository.RoomRepository;
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
    
    @InjectMocks
    private PortalRoomService portalRoomService;
    
    // 테스트 데이터
    private Room room1;
    private Room room2;
    private Room detailRoom;
    
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
        
        // 기본 모킹 설정
        lenient().when(roomRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(detailRoom));
        lenient().when(roomRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());
        lenient().when(roomRepository.findByAccommodationIdAndNotDeleted(1L)).thenReturn(Arrays.asList(room1, room2));
    }

    @Test
    @DisplayName("객실 검색 - 키워드 검색 성공")
    void searchRooms_WithKeyword_ShouldReturnMatchingRooms() {
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
        assertThat(result.getContent().get(0).getCapacity()).isEqualTo(2);
        assertThat(result.getContent().get(0).getPricePerNight()).isEqualTo(new BigDecimal("120000"));
        
        verify(roomRepository, times(1)).searchRooms(
                eq(keyword), 
                eq(minPrice), 
                eq(maxPrice),
                eq(minCapacity),
                eq(sortBy), 
                any(Pageable.class)
        );
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
        
        verify(roomRepository).findByIdAndNotDeleted(1L);
    }
    
    @Test
    @DisplayName("객실 상세 조회 - 존재하지 않는 객실 ID")
    void getRoomDetail_WithNonExistingId_ShouldThrowException() {
        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            portalRoomService.getRoomDetail(999L);
        });
        
        assertThat(exception.getMessage()).contains("999");
        verify(roomRepository).findByIdAndNotDeleted(999L);
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
        
        verify(roomRepository).findByAccommodationIdAndNotDeleted(1L);
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
        ReflectionTestUtils.setField(room, "deletedYn", "N");
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
        ReflectionTestUtils.setField(room, "deletedYn", "N");
        ReflectionTestUtils.setField(room, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(room, "updatedAt", LocalDateTime.now());
        
        return room;
    }
} 