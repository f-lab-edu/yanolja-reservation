package com.yanolja.areas.room.service;

import com.yanolja.areas.room.dto.RoomImageDto;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.areas.room.entity.RoomImage;
import com.yanolja.areas.room.repository.RoomImageRepository;
import com.yanolja.areas.room.repository.RoomRepository;
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
public class RoomImageService {

    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final FileStorageProperties fileStorageProperties;
    
    /**
     * 객실 이미지 목록 조회
     * @param roomId 객실 ID
     * @return 이미지 목록 응답 DTO
     */
    @Transactional(readOnly = true)
    public RoomImageDto.ListResponse getImagesByRoomId(Long roomId) {
        List<RoomImage> images = roomImageRepository.findByRoomId(roomId);
        return RoomImageDto.ListResponse.fromEntities(images);
    }
    
    /**
     * 대표 이미지 URL 조회
     * @param roomId 객실 ID
     * @return 대표 이미지 URL (없으면 null)
     */
    @Transactional(readOnly = true)
    public String getMainImageUrl(Long roomId) {
        RoomImage mainImage = roomImageRepository.findByRoomIdAndIsMainTrue(roomId);
        return mainImage != null ? mainImage.getImageUrl() : null;
    }
    
    /**
     * 객실 이미지 저장
     * @param roomId 객실 ID
     * @param files 이미지 파일 배열
     * @param mainImageIndex 대표 이미지 인덱스 (null인 경우 첫 번째 이미지가 대표 이미지)
     * @return 저장된 이미지 목록
     */
    @Transactional
    public List<RoomImageDto.Response> saveImages(Long roomId, MultipartFile[] files, Integer mainImageIndex) throws IOException {
        log.info("Start saving images for room ID: {}", roomId);
        
        Room room = roomRepository.findByIdAndNotDeleted(roomId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + roomId + "인 객실을 찾을 수 없습니다."));
        
        List<RoomImageDto.Response> responses = new ArrayList<>();
        
        // 기존 대표 이미지 플래그 초기화
        List<RoomImage> existingImages = roomImageRepository.findByRoomId(roomId);
        for (RoomImage existingImage : existingImages) {
            existingImage.unsetAsMain();
            roomImageRepository.save(existingImage);
        }
        
        // 폴더 생성
        Path uploadPath = Paths.get(fileStorageProperties.getRoomImageDir(), roomId.toString());
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
            
            RoomImage image = RoomImage.builder()
                    .room(room)
                    .imageUrl("/api/rooms/images/" + roomId + "/" + filename)
                    .isMain(isMain)
                    .build();
            
            RoomImage savedImage = roomImageRepository.save(image);
            responses.add(RoomImageDto.Response.fromEntity(savedImage));
            log.debug("Saved image entity with ID: {}, isMain: {}", savedImage.getId(), isMain);
        }
        
        log.info("Completed saving {} images for room ID: {}", files.length, roomId);
        return responses;
    }
    
    /**
     * 이미지를 대표 이미지로 설정
     * @param imageId 이미지 ID
     * @return 업데이트된 이미지 응답
     */
    @Transactional
    public RoomImageDto.Response setAsMainImage(Long imageId) {
        log.info("Setting image ID: {} as main image", imageId);
        
        RoomImage image = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + imageId + "인 이미지를 찾을 수 없습니다."));
        
        // 기존 대표 이미지 플래그 초기화
        List<RoomImage> images = roomImageRepository.findByRoomId(image.getRoom().getId());
        for (RoomImage existingImage : images) {
            existingImage.unsetAsMain();
            roomImageRepository.save(existingImage);
        }
        
        // 새 대표 이미지 설정
        image.makeMainImage();
        RoomImage savedImage = roomImageRepository.save(image);
        
        log.info("Set image ID: {} as main image for room ID: {}", imageId, image.getRoom().getId());
        return RoomImageDto.Response.fromEntity(savedImage);
    }
    
    /**
     * 이미지 삭제
     * @param imageId 이미지 ID
     */
    @Transactional
    public void deleteImage(Long imageId) {
        log.info("Deleting image ID: {}", imageId);
        
        RoomImage image = roomImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + imageId + "인 이미지를 찾을 수 없습니다."));
        
        Long roomId = image.getRoom().getId();
        
        try {
            // 실제 파일 삭제
            String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
            Path filePath = Paths.get(fileStorageProperties.getRoomImageDir(), 
                    roomId.toString(), filename);
            
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
        roomImageRepository.delete(image);
        log.info("Deleted image ID: {} from database", imageId);
    }
} 