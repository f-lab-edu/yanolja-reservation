package com.yanolja.areas.accommodation.dto;

import com.yanolja.areas.accommodation.entity.Amenity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class AmenityDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "편의시설 등록/수정 요청 DTO")
    public static class Request {
        
        @NotBlank(message = "편의시설 이름은 필수입니다")
        @Size(max = 100, message = "편의시설 이름은 100자 이내로 입력해주세요")
        @Schema(description = "편의시설 이름")
        private String name;
        
        @Schema(description = "아이콘 URL")
        private String iconUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "편의시설 응답 DTO")
    public static class Response {
        
        @Schema(description = "편의시설 ID")
        private Long id;
        
        @Schema(description = "편의시설 이름")
        private String name;
        
        @Schema(description = "아이콘 URL")
        private String iconUrl;
        
        public static Response fromEntity(Amenity amenity) {
            return Response.builder()
                    .id(amenity.getId())
                    .name(amenity.getName())
                    .iconUrl(amenity.getIconUrl())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "숙소-편의시설 연결 요청 DTO")
    public static class ConnectionRequest {
        
        @NotEmpty(message = "편의시설 ID 목록은 필수입니다")
        @Schema(description = "연결할 편의시설 ID 목록")
        private List<Long> amenityIds;
    }
} 