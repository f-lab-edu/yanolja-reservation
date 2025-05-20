package com.yanolja.areas.room.service;

import com.yanolja.areas.room.dto.RoomImageDto;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.areas.room.entity.RoomImage;
import com.yanolja.areas.room.repository.RoomImageRepository;
import com.yanolja.areas.room.repository.RoomRepository;
import com.yanolja.common.config.FileStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomImageServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomImageRepository roomImageRepository;
    
    @Mock
    private FileStorageProperties fileStorageProperties;

    @InjectMocks
    private RoomImageService roomImageService;

    private Room room;
    private MockMultipartFile mockImage1;
    private MockMultipartFile mockImage2;
    private final Long ROOM_ID = 1L;
    private final Long ACCOMMODATION_ID = 1L;
    private final String TEST_UPLOAD_DIR = "test-uploads/rooms";

    @BeforeEach
    void setUp() {
        // 객실 정보 설정
        room = Room.createRoom(
                ACCOMMODATION_ID,
                "디럭스 더블룸",
                "편안한 더블룸입니다.",
                2,
                new BigDecimal("120000")
        );
        ReflectionTestUtils.setField(room, "id", ROOM_ID);

        // 테스트용 이미지 파일 생성
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
        
        // FileStorageProperties 설정 - 모든 테스트에서 사용하는 것은 아니므로 lenient로 설정
        lenient().when(fileStorageProperties.getRoomImageDir()).thenReturn(TEST_UPLOAD_DIR);
        lenient().when(fileStorageProperties.getMaxSize()).thenReturn(10485760L); // 10MB
    }

    @Test
    void saveImages_WithValidFiles_ShouldReturnSavedImageResponses() throws IOException {
        // given
        MultipartFile[] files = {mockImage1, mockImage2};
        Integer mainImageIndex = 1; // 두 번째 이미지를 대표 이미지로 설정
        
        when(roomRepository.findByIdAndNotDeleted(ROOM_ID)).thenReturn(Optional.of(room));
        
        doAnswer(invocation -> {
            RoomImage image = invocation.getArgument(0);
            ReflectionTestUtils.setField(image, "id", 1L);
            return image;
        }).when(roomImageRepository).save(any(RoomImage.class));

        // 실제 파일 시스템 호출 방지를 위한 모킹
        Path mockPath = mock(Path.class);
        Path mockResolved = mock(Path.class);
        
        try {
            // 파일 저장 디렉토리를 생성하고 삭제하는 코드 작성
            Path testDir = Paths.get(TEST_UPLOAD_DIR, ROOM_ID.toString());
            Files.createDirectories(testDir);
            
            // 테스트 완료 후 삭제할 수 있도록 deleteOnExit 설정
            testDir.toFile().deleteOnExit();
        } catch (IOException e) {
            // 테스트 환경에서는 무시
        }

        // when
        List<RoomImageDto.Response> results = roomImageService.saveImages(ROOM_ID, files, mainImageIndex);

        // then
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // 두 번째 이미지가 대표 이미지로 설정되었는지 확인
        assertFalse(results.get(0).getIsMain());
        assertTrue(results.get(1).getIsMain());
        
        verify(roomRepository).findByIdAndNotDeleted(ROOM_ID);
        verify(roomImageRepository, times(2)).save(any(RoomImage.class));
    }
    
    @Test
    void saveImages_WithOversizedFile_ShouldThrowException() {
        // given
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "oversized",
                "oversized.jpg",
                "image/jpeg",
                "oversize content".getBytes()
        );
        
        when(roomRepository.findByIdAndNotDeleted(ROOM_ID)).thenReturn(Optional.of(room));
        // 파일 사이즈 검사를 위한 설정 (실제 설정은 10MB보다 작은 크기로 설정)
        when(fileStorageProperties.getMaxSize()).thenReturn(10L); // 10바이트로 제한
        
        // when & then
        MultipartFile[] files = {oversizedFile};
        assertThrows(IllegalArgumentException.class, () -> {
            roomImageService.saveImages(ROOM_ID, files, 0);
        });
    }

    @Test
    void saveImages_WithNonExistingRoom_ShouldThrowEntityNotFoundException() {
        // given
        MultipartFile[] files = {mockImage1, mockImage2};
        when(roomRepository.findByIdAndNotDeleted(ROOM_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> {
            roomImageService.saveImages(ROOM_ID, files, 0);
        });
        
        verify(roomRepository).findByIdAndNotDeleted(ROOM_ID);
        verify(roomImageRepository, never()).save(any(RoomImage.class));
    }

    @Test
    void getImagesByRoomId_ShouldReturnImageList() {
        // given
        RoomImage image1 = createMockRoomImage(1L, room, "/test/path1.jpg", false);
        RoomImage image2 = createMockRoomImage(2L, room, "/test/path2.jpg", true);
        List<RoomImage> mockImages = Arrays.asList(image1, image2);
        
        when(roomImageRepository.findByRoomId(ROOM_ID)).thenReturn(mockImages);
        
        // when
        RoomImageDto.ListResponse result = roomImageService.getImagesByRoomId(ROOM_ID);
        
        // then
        assertNotNull(result);
        assertEquals(2, result.getImages().size());
        assertEquals("/test/path1.jpg", result.getImages().get(0).getImageUrl());
        assertEquals("/test/path2.jpg", result.getImages().get(1).getImageUrl());
        assertFalse(result.getImages().get(0).getIsMain());
        assertTrue(result.getImages().get(1).getIsMain());
        
        verify(roomImageRepository).findByRoomId(ROOM_ID);
    }

    @Test
    void getMainImageUrl_ShouldReturnMainImageUrl() {
        // given
        RoomImage mainImage = createMockRoomImage(1L, room, "/test/main.jpg", true);
        
        when(roomImageRepository.findByRoomIdAndIsMainTrue(ROOM_ID)).thenReturn(mainImage);
        
        // when
        String result = roomImageService.getMainImageUrl(ROOM_ID);
        
        // then
        assertEquals("/test/main.jpg", result);
        verify(roomImageRepository).findByRoomIdAndIsMainTrue(ROOM_ID);
    }
    
    @Test
    void getMainImageUrl_WithNoMainImage_ShouldReturnNull() {
        // given
        when(roomImageRepository.findByRoomIdAndIsMainTrue(ROOM_ID)).thenReturn(null);
        
        // when
        String result = roomImageService.getMainImageUrl(ROOM_ID);
        
        // then
        assertNull(result);
        verify(roomImageRepository).findByRoomIdAndIsMainTrue(ROOM_ID);
    }

    @Test
    void setAsMainImage_ShouldUpdateMainImageFlag() {
        // given
        Long imageId = 1L;
        RoomImage image = createMockRoomImage(imageId, room, "/test/path1.jpg", false);
        List<RoomImage> roomImages = Arrays.asList(
            image,
            createMockRoomImage(2L, room, "/test/path2.jpg", true)
        );
        
        when(roomImageRepository.findById(imageId)).thenReturn(Optional.of(image));
        when(roomImageRepository.findByRoomId(ROOM_ID)).thenReturn(roomImages);
        when(roomImageRepository.save(any(RoomImage.class))).thenAnswer(i -> i.getArgument(0));
        
        // when
        RoomImageDto.Response result = roomImageService.setAsMainImage(imageId);
        
        // then
        assertNotNull(result);
        assertTrue(result.getIsMain());
        assertEquals("/test/path1.jpg", result.getImageUrl());
        
        verify(roomImageRepository).findById(imageId);
        verify(roomImageRepository).findByRoomId(ROOM_ID);
        verify(roomImageRepository, times(3)).save(any(RoomImage.class)); // 기존 메인 이미지 해제 + 새 메인 이미지 설정
    }

    @Test
    void deleteImage_ShouldRemoveImageFromDatabase() throws IOException {
        // given
        Long imageId = 1L;
        RoomImage image = createMockRoomImage(imageId, room, "/api/rooms/images/1/test1.jpg", false);
        
        when(roomImageRepository.findById(imageId)).thenReturn(Optional.of(image));
        
        // when
        roomImageService.deleteImage(imageId);
        
        // then
        verify(roomImageRepository).findById(imageId);
        verify(roomImageRepository).delete(image);
    }
    
    @Test
    void deleteImage_WithNonExistingImage_ShouldThrowEntityNotFoundException() {
        // given
        Long imageId = 999L;
        when(roomImageRepository.findById(imageId)).thenReturn(Optional.empty());
        
        // when & then
        assertThrows(EntityNotFoundException.class, () -> {
            roomImageService.deleteImage(imageId);
        });
        
        verify(roomImageRepository).findById(imageId);
        verify(roomImageRepository, never()).delete(any(RoomImage.class));
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