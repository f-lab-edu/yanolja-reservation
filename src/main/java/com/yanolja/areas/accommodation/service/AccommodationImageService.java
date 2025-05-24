package com.yanolja.areas.accommodation.service;

import com.yanolja.areas.accommodation.dto.AccommodationImageDto;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.AccommodationImage;
import com.yanolja.areas.accommodation.repository.AccommodationImageRepository;
import com.yanolja.areas.accommodation.repository.AccommodationRepository;
import com.yanolja.common.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationImageService {

    private final AccommodationRepository accommodationRepository;
    private final AccommodationImageRepository accommodationImageRepository;
    private final FileStorageProperties fileStorageProperties;
    
    /**
     * 숙소 이미지 목록 조회
     * @param accommodationId 숙소 ID
     * @return 이미지 목록 응답 DTO
     */
    @Transactional(readOnly = true)
    public AccommodationImageDto.ListResponse getImagesByAccommodationId(Long accommodationId) {
        List<AccommodationImage> images = accommodationImageRepository.findByAccommodationId(accommodationId);
        return AccommodationImageDto.ListResponse.fromEntities(images);
    }
    
    /**
     * 대표 이미지 URL 조회
     * @param accommodationId 숙소 ID
     * @return 대표 이미지 URL (없으면 null)
     */
    @Transactional(readOnly = true)
    public String getMainImageUrl(Long accommodationId) {
        AccommodationImage mainImage = accommodationImageRepository.findByAccommodationIdAndIsMainTrue(accommodationId);
        return mainImage != null ? mainImage.getImageUrl() : null;
    }
    
    /**
     * 숙소 이미지 저장
     * @param accommodationId 숙소 ID
     * @param files 이미지 파일 배열
     * @param mainImageIndex 대표 이미지 인덱스 (null인 경우 첫 번째 이미지가 대표 이미지)
     * @return 저장된 이미지 목록
     */
    @Transactional
    public List<AccommodationImageDto.Response> saveImages(Long accommodationId, MultipartFile[] files, Integer mainImageIndex) throws IOException {
        log.info("Start saving images for accommodation ID: {}", accommodationId);
        
        Accommodation accommodation = accommodationRepository.findByIdAndDeletedYn(accommodationId,"N")
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + accommodationId + "인 숙소를 찾을 수 없습니다."));
        
        List<AccommodationImageDto.Response> responses = new ArrayList<>();
        
        // 기존 대표 이미지 플래그 초기화
        List<AccommodationImage> existingImages = accommodationImageRepository.findByAccommodationId(accommodationId);
        for (AccommodationImage existingImage : existingImages) {
            existingImage.unsetAsMain();
            accommodationImageRepository.save(existingImage);
        }
        
        // 폴더 생성
        Path uploadPath = Paths.get(fileStorageProperties.getAccommodationImageDir(), accommodationId.toString());
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
                log.info("Created directory: {}", uploadPath);
            } catch (IOException e) {
                log.error("Failed to create directory: {}", uploadPath, e);
                throw new IOException("이미지 저장을 위한 디렉토리 생성에 실패했습니다.", e);
            }
        }
        
        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            log.debug("Processing file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
            
            // 파일 크기 검증
            if (file.getSize() > fileStorageProperties.getMaxSize()) {
                log.warn("File too large: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
                throw new IllegalArgumentException("파일 크기가 최대 허용 크기(" + 
                        fileStorageProperties.getMaxSize() / 1024 / 1024 + "MB)를 초과합니다.");
            }
            
            // 파일 저장
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            
            try {
                Files.copy(file.getInputStream(), filePath);
                log.debug("Saved file to: {}", filePath);
            } catch (IOException e) {
                log.error("Failed to save file: {}", filePath, e);
                throw new IOException("이미지 파일 저장에 실패했습니다.", e);
            }
            
            // 이미지 엔티티 생성 및 저장
            boolean isMain = (mainImageIndex != null) ? i == mainImageIndex : i == 0;
            
            AccommodationImage image = AccommodationImage.builder()
                    .accommodation(accommodation)
                    .imageUrl("/api/accommodations/images/" + accommodationId + "/" + filename)
                    .isMain(isMain)
                    .build();
            
            AccommodationImage savedImage = accommodationImageRepository.save(image);
            responses.add(AccommodationImageDto.Response.fromEntity(savedImage));
            log.debug("Saved image entity with ID: {}, isMain: {}", savedImage.getId(), isMain);
        }
        
        log.info("Completed saving {} images for accommodation ID: {}", files.length, accommodationId);
        return responses;
    }
    
    /**
     * 이미지를 대표 이미지로 설정
     * @param imageId 이미지 ID
     * @return 업데이트된 이미지 응답
     */
    @Transactional
    public AccommodationImageDto.Response setAsMainImage(Long imageId) {
        log.info("Setting image ID: {} as main image", imageId);
        
        AccommodationImage image = accommodationImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + imageId + "인 이미지를 찾을 수 없습니다."));
        
        // 기존 대표 이미지 플래그 초기화
        List<AccommodationImage> images = accommodationImageRepository.findByAccommodationId(image.getAccommodation().getId());
        for (AccommodationImage existingImage : images) {
            existingImage.unsetAsMain();
            accommodationImageRepository.save(existingImage);
        }
        
        // 새 대표 이미지 설정
        image.makeMainImage();
        AccommodationImage savedImage = accommodationImageRepository.save(image);
        
        log.info("Set image ID: {} as main image for accommodation ID: {}", imageId, image.getAccommodation().getId());
        return AccommodationImageDto.Response.fromEntity(savedImage);
    }
    
    /**
     * 이미지 삭제
     * @param imageId 이미지 ID
     */
    @Transactional
    public void deleteImage(Long imageId) {
        log.info("Deleting image ID: {}", imageId);
        
        AccommodationImage image = accommodationImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + imageId + "인 이미지를 찾을 수 없습니다."));
        
        Long accommodationId = image.getAccommodation().getId();
        
        try {
            // 실제 파일 삭제
            String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
            Path filePath = Paths.get(fileStorageProperties.getAccommodationImageDir(), 
                    accommodationId.toString(), filename);
            
            if (Files.deleteIfExists(filePath)) {
                log.debug("Deleted file: {}", filePath);
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete file for image ID: {}", imageId, e);
            // 파일 삭제 실패 시에도 DB에서는 삭제 진행
        }
        
        // DB에서 이미지 삭제
        accommodationImageRepository.delete(image);
        log.info("Deleted image ID: {} from database", imageId);
    }
} 