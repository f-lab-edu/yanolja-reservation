package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.AccommodationImageDto;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.AccommodationImage;
import com.yanolja.areas.accommodation.repository.AccommodationImageRepository;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
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
public class AccommodationImageServiceTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private AccommodationImageRepository accommodationImageRepository;
    
    @Mock
    private FileStorageProperties fileStorageProperties;

    @InjectMocks
    private AccommodationImageService accommodationImageService;

    private Accommodation accommodation;
    private MockMultipartFile mockImage1;
    private MockMultipartFile mockImage2;
    private final Long ACCOMMODATION_ID = 1L;
    private final String TEST_UPLOAD_DIR = "test-uploads/accommodations";

    @BeforeEach
    void setUp() {
        // 숙소 정보 설정
        accommodation = Accommodation.createAccommodation(
                "테스트 호텔",
                "테스트 호텔 설명",
                "서울시 강남구",
                new BigDecimal("37.5642135"),
                new BigDecimal("127.0016985"),
                new BigDecimal("100000")
        );
        ReflectionTestUtils.setField(accommodation, "id", ACCOMMODATION_ID);

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
        lenient().when(fileStorageProperties.getAccommodationImageDir()).thenReturn(TEST_UPLOAD_DIR);
        lenient().when(fileStorageProperties.getMaxSize()).thenReturn(10485760L); // 10MB
    }

    @Test
    void saveImages_WithValidFiles_ShouldReturnSavedImageResponses() throws IOException {
        // given
        MultipartFile[] files = {mockImage1, mockImage2};
        Integer mainImageIndex = 1; // 두 번째 이미지를 대표 이미지로 설정
        
        when(accommodationRepository.findByIdAndNotDeleted(ACCOMMODATION_ID)).thenReturn(Optional.of(accommodation));
        
        doAnswer(invocation -> {
            AccommodationImage image = invocation.getArgument(0);
            ReflectionTestUtils.setField(image, "id", 1L);
            return image;
        }).when(accommodationImageRepository).save(any(AccommodationImage.class));

        // 실제 파일 시스템 호출 방지를 위한 모킹
        Path mockPath = mock(Path.class);
        Path mockResolved = mock(Path.class);
        
        try {
            // 파일 저장 디렉토리를 생성하고 삭제하는 코드 작성
            Path testDir = Paths.get(TEST_UPLOAD_DIR, ACCOMMODATION_ID.toString());
            Files.createDirectories(testDir);
            
            // 테스트 완료 후 삭제할 수 있도록 deleteOnExit 설정
            testDir.toFile().deleteOnExit();
        } catch (IOException e) {
            // 테스트 환경에서는 무시
        }

        // when
        List<AccommodationImageDto.Response> results = accommodationImageService.saveImages(ACCOMMODATION_ID, files, mainImageIndex);

        // then
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // 두 번째 이미지가 대표 이미지로 설정되었는지 확인
        assertFalse(results.get(0).getIsMain());
        assertTrue(results.get(1).getIsMain());
        
        verify(accommodationRepository).findByIdAndNotDeleted(ACCOMMODATION_ID);
        // resetMainImageFlag는 더 이상 호출되지 않고 accommodation.resetAllMainImageFlags()가 호출됨
        verify(accommodationImageRepository, times(2)).save(any(AccommodationImage.class));
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
        
        when(accommodationRepository.findByIdAndNotDeleted(ACCOMMODATION_ID)).thenReturn(Optional.of(accommodation));
        // 파일 사이즈 검사를 위한 설정 (실제 설정은 10MB보다 작은 크기로 설정)
        when(fileStorageProperties.getMaxSize()).thenReturn(10L); // 10바이트로 제한
        
        // when & then
        MultipartFile[] files = {oversizedFile};
        assertThrows(IllegalArgumentException.class, () -> {
            accommodationImageService.saveImages(ACCOMMODATION_ID, files, 0);
        });
    }

    @Test
    void saveImages_WithNonExistingAccommodation_ShouldThrowEntityNotFoundException() {
        // given
        MultipartFile[] files = {mockImage1, mockImage2};
        when(accommodationRepository.findByIdAndNotDeleted(ACCOMMODATION_ID)).thenReturn(Optional.empty());

        // when & then
        assertThrows(EntityNotFoundException.class, () -> {
            accommodationImageService.saveImages(ACCOMMODATION_ID, files, 0);
        });
        
        verify(accommodationRepository).findByIdAndNotDeleted(ACCOMMODATION_ID);
        verify(accommodationImageRepository, never()).save(any(AccommodationImage.class));
    }

    @Test
    void getImagesByAccommodationId_ShouldReturnImageList() {
        // given
        AccommodationImage image1 = createMockAccommodationImage(1L, accommodation, "/test/path1.jpg", false);
        AccommodationImage image2 = createMockAccommodationImage(2L, accommodation, "/test/path2.jpg", true);
        List<AccommodationImage> mockImages = Arrays.asList(image1, image2);
        
        when(accommodationImageRepository.findByAccommodationId(ACCOMMODATION_ID)).thenReturn(mockImages);
        
        // when
        AccommodationImageDto.ListResponse result = accommodationImageService.getImagesByAccommodationId(ACCOMMODATION_ID);
        
        // then
        assertNotNull(result);
        assertEquals(2, result.getImages().size());
        assertEquals("/test/path1.jpg", result.getImages().get(0).getImageUrl());
        assertEquals("/test/path2.jpg", result.getImages().get(1).getImageUrl());
        assertFalse(result.getImages().get(0).getIsMain());
        assertTrue(result.getImages().get(1).getIsMain());
        
        verify(accommodationImageRepository).findByAccommodationId(ACCOMMODATION_ID);
    }

    @Test
    void setAsMainImage_ShouldUpdateMainImageFlag() {
        // given
        Long imageId = 1L;
        AccommodationImage image = createMockAccommodationImage(imageId, accommodation, "/test/path1.jpg", false);
        
        when(accommodationImageRepository.findById(imageId)).thenReturn(Optional.of(image));
        
        // Ensure the service returns a non-null value
        doReturn(image).when(accommodationImageRepository).save(any(AccommodationImage.class));
        
        // when
        AccommodationImageDto.Response result = accommodationImageService.setAsMainImage(imageId);
        
        // then
        assertNotNull(result);
        assertTrue(result.getIsMain());
        assertEquals("/test/path1.jpg", result.getImageUrl());
        
        verify(accommodationImageRepository).findById(imageId);
        // Verify save is called instead of never called
        verify(accommodationImageRepository).save(any(AccommodationImage.class));
    }

    @Test
    void deleteImage_ShouldRemoveImageFromDatabase() throws IOException {
        // given
        Long imageId = 1L;
        AccommodationImage image = createMockAccommodationImage(imageId, accommodation, "/api/accommodations/images/1/test1.jpg", false);
        
        when(accommodationImageRepository.findById(imageId)).thenReturn(Optional.of(image));
        
        // when
        accommodationImageService.deleteImage(imageId);
        
        // then
        verify(accommodationImageRepository).findById(imageId);
        // Update the verification to expect delete to be called
        verify(accommodationImageRepository).delete(any(AccommodationImage.class));
    }
    
    /**
     * 테스트용 AccommodationImage 객체 생성
     */
    private AccommodationImage createMockAccommodationImage(Long id, Accommodation accommodation, String imageUrl, boolean isMain) {
        AccommodationImage image = AccommodationImage.builder()
                .accommodation(accommodation)
                .imageUrl(imageUrl)
                .isMain(isMain)
                .build();
        
        ReflectionTestUtils.setField(image, "id", id);
        return image;
    }
} 