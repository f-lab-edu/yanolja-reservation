package com.yanolja.areas.reviews.dto;

import com.yanolja.areas.reviews.entity.ReviewImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewImageDto {

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String fileName;
        private String filePath;
        private Long fileSize;
        private LocalDateTime createdAt;

        public static Response fromEntity(ReviewImage reviewImage) {
            String imageUrl = reviewImage.getImageUrl();
            String fileName = extractFileName(imageUrl);
            
            return Response.builder()
                    .id(reviewImage.getId())
                    .fileName(fileName)
                    .filePath(imageUrl) // imageUrl을 filePath로 사용
                    .fileSize(0L) // 파일 크기 정보가 없으므로 0으로 설정
                    .createdAt(reviewImage.getCreatedAt())
                    .build();
        }
        
        private static String extractFileName(String imageUrl) {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return "unknown.jpg";
            }
            
            // URL에서 파일명 추출
            int lastSlashIndex = imageUrl.lastIndexOf('/');
            if (lastSlashIndex >= 0 && lastSlashIndex < imageUrl.length() - 1) {
                return imageUrl.substring(lastSlashIndex + 1);
            }
            
            return "image.jpg";
        }
    }

    @Getter
    @Builder
    public static class ListResponse {
        private List<Response> images;
        private int totalCount;

        public static ListResponse fromEntities(List<ReviewImage> reviewImages) {
            List<Response> imageResponses = reviewImages.stream()
                    .map(Response::fromEntity)
                    .collect(Collectors.toList());

            return ListResponse.builder()
                    .images(imageResponses)
                    .totalCount(imageResponses.size())
                    .build();
        }
    }
} 