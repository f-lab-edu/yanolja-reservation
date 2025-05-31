package com.yanolja.areas.accommodation.dto;

import com.yanolja.areas.accommodation.entity.AccommodationImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

public class AccommodationImageDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "이미지 정보 요청 DTO")
    public static class Request {
        
        @Schema(description = "대표 이미지 여부")
        private Boolean isMain;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "이미지 응답 DTO")
    public static class Response {
        
        @Schema(description = "이미지 ID")
        private Long id;
        
        @Schema(description = "숙소 ID")
        private Long accommodationId;
        
        @Schema(description = "이미지 URL")
        private String imageUrl;
        
        @Schema(description = "대표 이미지 여부")
        private Boolean isMain;
        
        public static Response fromEntity(AccommodationImage image) {
            return Response.builder()
                    .id(image.getId())
                    .accommodationId(image.getAccommodation().getId())
                    .imageUrl(image.getImageUrl())
                    .isMain(image.getIsMain())
                    .build();
        }
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "이미지 목록 응답 DTO")
    public static class ListResponse {
        
        @Schema(description = "이미지 목록")
        private List<Response> images;
        
        public static ListResponse fromEntities(List<AccommodationImage> images) {
            List<Response> responses = images.stream()
                    .map(Response::fromEntity)
                    .collect(Collectors.toList());
            
            return ListResponse.builder()
                    .images(responses)
                    .build();
        }
    }
} 