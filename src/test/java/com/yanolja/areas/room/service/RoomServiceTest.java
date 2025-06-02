package com.yanolja.areas.room.service;

import com.yanolja.areas.room.dto.RoomDto;
import com.yanolja.areas.room.dto.RoomImageDto;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.areas.room.entity.RoomImage;
import com.yanolja.areas.room.repository.RoomRepository;
import com.yanolja.areas.room.repository.RoomOptionMappingRepository;
import com.yanolja.areas.room.repository.RoomOptionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    
    @Mock
    private RoomImageService roomImageService;

    @Mock
    private RoomOptionMappingRepository roomOptionMappingRepository;

    @Mock
    private RoomOptionRepository roomOptionRepository;

    @InjectMocks
    private RoomService roomService;

    private RoomDto.Request roomRequest;
    private Room room;
    private RoomImage roomImage;
    private MockMultipartFile mockImage1;
    private MockMultipartFile mockImage2;
    private List<RoomImageDto.Response> roomImageResponses;

    @BeforeEach
    void setUp() {
        // 테스트용 객실 요청 DTO 생성
        roomRequest = new RoomDto.Request();
        roomRequest.setAccommodationId(1L);
        roomRequest.setName("디럭스 더블룸");
        roomRequest.setDescription("편안한 디럭스 더블룸입니다.");
        roomRequest.setCapacity(2);
        roomRequest.setPricePerNight(new BigDecimal("120000"));

        // 테스트용 엔티티 생성
        room = Room.createRoom(
                1L,
                "디럭스 더블룸",
                "편안한 디럭스 더블룸입니다.",
                2,
                new BigDecimal("120000")
        );
        ReflectionTestUtils.setField(room, "id", 1L);
        ReflectionTestUtils.setField(room, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(room, "updatedAt", LocalDateTime.now());
        ReflectionTestUtils.setField(room, "createdBy", "test");
        ReflectionTestUtils.setField(room, "updatedBy", "test");
        
        // 테스트용 객실 이미지 생성
        roomImage = RoomImage.builder()
                .room(room)
                .imageUrl("/api/rooms/images/1/main.jpg")
                .isMain(true)
                .build();
        ReflectionTestUtils.setField(roomImage, "id", 1L);
        
        // 테스트용 이미지 파일
        mockImage1 = new MockMultipartFile(
                "image1", 
                "test1.jpg", 
                "image/jpeg", 
                "test image content 1".getBytes()
        );
        
        mockImage2 = new MockMultipartFile(
                "image2", 
                "test2.jpg", 
                "image/jpeg", 
                "test image content 2".getBytes()
        );
        
        // 테스트용 이미지 응답 목록
        RoomImageDto.Response imageResponse1 = RoomImageDto.Response.builder()
                .id(1L)
                .roomId(1L)
                .imageUrl("/api/rooms/images/1/test1.jpg")
                .isMain(true)
                .build();
        
        RoomImageDto.Response imageResponse2 = RoomImageDto.Response.builder()
                .id(2L)
                .roomId(1L)
                .imageUrl("/api/rooms/images/1/test2.jpg")
                .isMain(false)
                .build();
        
        roomImageResponses = Arrays.asList(imageResponse1, imageResponse2);
        
        // RoomImageService의 기본 모킹 설정
        RoomImageDto.ListResponse listResponse = RoomImageDto.ListResponse.builder()
                .images(roomImageResponses)
                .build();
        lenient().when(roomImageService.getImagesByRoomId(1L)).thenReturn(listResponse);
        lenient().when(roomImageService.getMainImageUrl(1L)).thenReturn("/api/rooms/images/1/main.jpg");
    }

    @Test
    @DisplayName("객실 생성 성공 테스트")
    void createRoomSuccess() {
        // Given
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(room));
        when(roomOptionMappingRepository.findByRoomIdAndRoomNotDeleted(1L)).thenReturn(Arrays.asList());

        // When
        RoomDto.Response response = roomService.createRoom(roomRequest);

        // Then
        assertNotNull(response);
        assertEquals("디럭스 더블룸", response.getName());
        assertEquals("편안한 디럭스 더블룸입니다.", response.getDescription());
        assertEquals(2, response.getCapacity());
        assertEquals(new BigDecimal("120000"), response.getPricePerNight());
        assertEquals("AVAILABLE", response.getStatus());
        
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    @DisplayName("객실 목록 조회 성공 테스트 - 이미지 포함")
    void getAllRoomsSuccess() {
        // Given
        List<Room> rooms = Arrays.asList(room);
        when(roomRepository.findAllNotDeleted()).thenReturn(rooms);

        // When
        List<RoomDto.ListResponse> responses = roomService.getAllRooms();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("디럭스 더블룸", responses.get(0).getName());
        assertEquals("편안한 디럭스 더블룸입니다.", responses.get(0).getDescription());
        assertEquals(2, responses.get(0).getCapacity());
        assertEquals(new BigDecimal("120000"), responses.get(0).getPricePerNight());
        assertEquals("AVAILABLE", responses.get(0).getStatus());
        assertEquals("/api/rooms/images/1/main.jpg", responses.get(0).getMainImageUrl());
        
        verify(roomRepository, times(1)).findAllNotDeleted();
        verify(roomImageService, times(1)).getMainImageUrl(1L);
    }

    @Test
    @DisplayName("객실 상세 조회 성공 테스트 - 이미지 포함")
    void getRoomByIdSuccess() {
        // Given
        when(roomRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(room));
        when(roomOptionMappingRepository.findByRoomIdAndRoomNotDeleted(1L)).thenReturn(Arrays.asList());

        // When
        RoomDto.Response response = roomService.getRoomById(1L);

        // Then
        assertNotNull(response);
        assertEquals("디럭스 더블룸", response.getName());
        assertEquals("편안한 디럭스 더블룸입니다.", response.getDescription());
        assertEquals(2, response.getCapacity());
        assertEquals(new BigDecimal("120000"), response.getPricePerNight());
        assertEquals("AVAILABLE", response.getStatus());
        
        // 이미지 목록 검증
        assertNotNull(response.getImages());
        assertEquals(2, response.getImages().size());
        assertEquals("/api/rooms/images/1/test1.jpg", response.getImages().get(0).getImageUrl());
        assertEquals("/api/rooms/images/1/test2.jpg", response.getImages().get(1).getImageUrl());
        
        verify(roomRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(roomImageService, times(1)).getImagesByRoomId(1L);
    }

    @Test
    @DisplayName("객실 상세 조회 실패 테스트 - 존재하지 않는 ID")
    void getRoomByIdFailNotFound() {
        // Given
        when(roomRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            roomService.getRoomById(999L);
        });
        
        verify(roomRepository, times(1)).findByIdAndNotDeleted(999L);
    }

    @Test
    @DisplayName("숙소별 객실 목록 조회 성공 테스트 - 이미지 포함")
    void getRoomsByAccommodationIdSuccess() {
        // Given
        List<Room> rooms = Arrays.asList(room);
        when(roomRepository.findByAccommodationIdAndNotDeleted(1L)).thenReturn(rooms);

        // When
        List<RoomDto.ListResponse> responses = roomService.getRoomsByAccommodationId(1L);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("디럭스 더블룸", responses.get(0).getName());
        assertEquals("편안한 디럭스 더블룸입니다.", responses.get(0).getDescription());
        assertEquals(1L, responses.get(0).getAccommodationId());
        assertEquals("/api/rooms/images/1/main.jpg", responses.get(0).getMainImageUrl());
        
        verify(roomRepository, times(1)).findByAccommodationIdAndNotDeleted(1L);
        verify(roomImageService, times(1)).getMainImageUrl(1L);
    }

    @Test
    @DisplayName("객실 수정 성공 테스트")
    void updateRoomSuccess() {
        // Given
        when(roomRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(room));
        when(roomOptionMappingRepository.findByRoomIdAndRoomNotDeleted(1L)).thenReturn(Arrays.asList());

        // 수정할 객실 정보
        RoomDto.Request updateRequest = new RoomDto.Request();
        updateRequest.setAccommodationId(1L);
        updateRequest.setName("수정된 디럭스 더블룸");
        updateRequest.setDescription("수정된 설명입니다.");
        updateRequest.setCapacity(3);
        updateRequest.setPricePerNight(new BigDecimal("150000"));
        updateRequest.setOptionIds(Arrays.asList()); // 빈 리스트로 설정하여 기존 옵션 제거

        // When
        RoomDto.Response response = roomService.updateRoom(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("수정된 디럭스 더블룸", response.getName());
        assertEquals("수정된 설명입니다.", response.getDescription());
        assertEquals(3, response.getCapacity());
        assertEquals(new BigDecimal("150000"), response.getPricePerNight());
        
        verify(roomRepository, times(2)).findByIdAndNotDeleted(1L); // updateRoom 내부에서 2번 호출됨
        verify(roomOptionMappingRepository, times(1)).deleteByRoomId(1L);
    }

    @Test
    @DisplayName("객실 삭제 성공 테스트")
    void deleteRoomSuccess() {
        // Given
        when(roomRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(room));
        
        // When
        roomService.deleteRoom(1L);

        // Then
        assertEquals("Y", room.getDeletedYn());
        verify(roomRepository, times(1)).findByIdAndNotDeleted(1L);
        verify(roomOptionMappingRepository, times(1)).deleteByRoomId(1L);
    }

    @Test
    @DisplayName("객실 삭제 실패 테스트 - 존재하지 않는 ID")
    void deleteRoomFailNotFound() {
        // Given
        when(roomRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            roomService.deleteRoom(999L);
        });
        
        verify(roomRepository, times(1)).findByIdAndNotDeleted(999L);
    }

} 