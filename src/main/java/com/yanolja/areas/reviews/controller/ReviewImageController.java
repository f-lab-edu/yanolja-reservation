package com.yanolja.areas.reviews.controller;

import com.yanolja.areas.reviews.dto.ReviewImageDto;
import com.yanolja.areas.reviews.service.ReviewImageService;
import com.yanolja.common.config.FileStorageProperties;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
@Tag(name = "리뷰 이미지 API", description = "리뷰 이미지 관련 API")
public class ReviewImageController {

    private final ReviewImageService reviewImageService;
    private final FileStorageProperties fileStorageProperties;

    @Operation(summary = "리뷰 이미지 업로드", description = "리뷰에 이미지를 업로드합니다.")
    @PostMapping(value = "/{reviewId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<ReviewImageDto.Response>> uploadReviewImages(
            @PathVariable Long reviewId,
            @RequestParam(value = "files") MultipartFile[] files) throws IOException {
        
        if (files.length > 5) {
            throw new IllegalArgumentException("리뷰 이미지는 최대 5개까지 업로드 가능합니다.");
        }
        
        List<ReviewImageDto.Response> responses = reviewImageService.saveImages(reviewId, files);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "리뷰 이미지 목록 조회", description = "특정 리뷰의 이미지 목록을 조회합니다.")
    @GetMapping("/{reviewId}/images")
    public ApiResponse<ReviewImageDto.ListResponse> getReviewImages(@PathVariable Long reviewId) {
        ReviewImageDto.ListResponse response = reviewImageService.getImagesByReviewId(reviewId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "리뷰 이미지 삭제", description = "특정 리뷰 이미지를 삭제합니다.")
    @DeleteMapping("/images/{imageId}")
    public ApiResponse<Void> deleteReviewImage(@PathVariable Long imageId) {
        reviewImageService.deleteImage(imageId);
        return ApiResponse.success();
    }

    @Operation(summary = "리뷰 이미지 파일 제공", description = "리뷰 이미지 파일을 제공합니다.")
    @GetMapping("/images/{reviewId}/{filename:.+}")
    public ResponseEntity<Resource> getReviewImageFile(
            @PathVariable Long reviewId,
            @PathVariable String filename) {
        try {
            Path filePath = Paths.get(fileStorageProperties.getFullReviewImageDir())
                    .resolve(reviewId.toString())
                    .resolve(filename)
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            log.error("Error serving review image file: {}/{}", reviewId, filename, e);
            return ResponseEntity.badRequest().build();
        }
    }
} 