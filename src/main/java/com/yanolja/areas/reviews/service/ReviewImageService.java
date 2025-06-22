package com.yanolja.areas.reviews.service;

import com.yanolja.areas.reviews.dto.ReviewImageDto;
import com.yanolja.areas.reviews.entity.Review;
import com.yanolja.areas.reviews.entity.ReviewImage;
import com.yanolja.areas.reviews.repository.ReviewImageRepository;
import com.yanolja.areas.reviews.repository.ReviewRepository;
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
public class ReviewImageService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final FileStorageProperties fileStorageProperties;

    /**
     * 리뷰 이미지 목록 조회
     */
    @Transactional(readOnly = true)
    public ReviewImageDto.ListResponse getImagesByReviewId(Long reviewId) {
        List<ReviewImage> images = reviewImageRepository.findByReviewIdOrderByCreatedAtAsc(reviewId);
        return ReviewImageDto.ListResponse.fromEntities(images);
    }

    /**
     * 리뷰 이미지 저장
     */
    @Transactional
    public List<ReviewImageDto.Response> saveImages(Long reviewId, MultipartFile[] files) throws IOException {
        log.info("Start saving images for review ID: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + reviewId + "인 리뷰를 찾을 수 없습니다."));

        List<ReviewImageDto.Response> responses = new ArrayList<>();

        // 폴더 생성
        Path uploadPath = Paths.get(fileStorageProperties.getFullReviewImageDir(), reviewId.toString());
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

            // 파일 확장자 검증
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isValidImageFile(originalFilename)) {
                throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif만 지원)");
            }

            // 파일 저장
            String filename = UUID.randomUUID() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(filename);

            try {
                Files.copy(file.getInputStream(), filePath);
                log.debug("Saved file to: {}", filePath);
            } catch (IOException e) {
                log.error("Failed to save file: {}", filePath, e);
                throw new IOException("이미지 파일 저장에 실패했습니다.", e);
            }

            // 이미지 엔티티 생성 및 저장
            ReviewImage image = ReviewImage.createReviewImage(
                    review,
                    "/api/reviews/images/" + reviewId + "/" + filename
            );

            ReviewImage savedImage = reviewImageRepository.save(image);
            responses.add(ReviewImageDto.Response.fromEntity(savedImage));
            log.debug("Saved image entity with ID: {}", savedImage.getId());
        }

        log.info("Completed saving {} images for review ID: {}", files.length, reviewId);
        return responses;
    }

    /**
     * 리뷰 이미지 삭제
     */
    @Transactional
    public void deleteImage(Long imageId) {
        log.info("Deleting image ID: {}", imageId);

        ReviewImage image = reviewImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 " + imageId + "인 이미지를 찾을 수 없습니다."));

        Long reviewId = image.getReview().getId();

        try {
            // 실제 파일 삭제
            String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
            Path filePath = Paths.get(fileStorageProperties.getFullReviewImageDir(),
                    reviewId.toString(), filename);

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
        reviewImageRepository.delete(image);
        log.info("Deleted image ID: {} from database", imageId);
    }

    /**
     * 리뷰의 모든 이미지 삭제
     */
    @Transactional
    public void deleteAllImagesByReviewId(Long reviewId) {
        log.info("Deleting all images for review ID: {}", reviewId);

        List<ReviewImage> images = reviewImageRepository.findByReviewIdOrderByCreatedAtAsc(reviewId);
        
        for (ReviewImage image : images) {
            try {
                // 실제 파일 삭제
                String filename = image.getImageUrl().substring(image.getImageUrl().lastIndexOf("/") + 1);
                Path filePath = Paths.get(fileStorageProperties.getFullReviewImageDir(),
                        reviewId.toString(), filename);

                if (Files.deleteIfExists(filePath)) {
                    log.debug("Deleted file: {}", filePath);
                }
            } catch (IOException e) {
                log.error("Failed to delete file for image ID: {}", image.getId(), e);
            }
        }

        // DB에서 모든 이미지 삭제
        reviewImageRepository.deleteByReviewId(reviewId);
        
        // 디렉토리 삭제 시도
        try {
            Path reviewDir = Paths.get(fileStorageProperties.getFullReviewImageDir(), reviewId.toString());
            if (Files.exists(reviewDir)) {
                Files.delete(reviewDir);
                log.debug("Deleted directory: {}", reviewDir);
            }
        } catch (IOException e) {
            log.warn("Failed to delete directory for review ID: {}", reviewId, e);
        }

        log.info("Deleted all images for review ID: {}", reviewId);
    }

    /**
     * 유효한 이미지 파일인지 확인
     */
    private boolean isValidImageFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif");
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
} 