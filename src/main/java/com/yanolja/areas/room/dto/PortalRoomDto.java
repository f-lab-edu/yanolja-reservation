package com.yanolja.areas.room.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yanolja.areas.room.entity.Room;
import com.yanolja.common.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PortalRoomDto {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "객실 목록 조회 응답")
    public static class ListResponse {
        @Schema(description = "객실 ID", example = "1")
        private Long id;

        @Schema(description = "숙소 ID", example = "1")
        private Long accommodationId;

        @Schema(description = "객실 이름", example = "디럭스 더블룸")
        private String name;

        @Schema(description = "객실 설명", example = "아늑한 분위기의 디럭스 더블룸입니다.")
        private String description;

        @Schema(description = "수용 인원", example = "2")
        private Integer capacity;

        @Schema(description = "1박 가격", example = "120000")
        private BigDecimal pricePerNight;

        @Schema(description = "객실 상태", example = "AVAILABLE")
        private String status;

        @Schema(description = "메인 이미지 URL", example = "https://example.com/images/room1.jpg")
        private String mainImageUrl;
        
        public static ListResponse fromEntity(Room room) {
            return ListResponse.builder()
                    .id(room.getId())
                    .accommodationId(room.getAccommodationId())
                    .name(room.getName())
                    .description(room.getDescription())
                    .capacity(room.getCapacity())
                    .pricePerNight(room.getPricePerNight())
                    .status(room.getStatus())
                    .build();
        }
        
        public static ListResponse fromEntityWithMainImage(Room room, String mainImageUrl) {
            return ListResponse.builder()
                    .id(room.getId())
                    .accommodationId(room.getAccommodationId())
                    .name(room.getName())
                    .description(room.getDescription())
                    .capacity(room.getCapacity())
                    .pricePerNight(room.getPricePerNight())
                    .status(room.getStatus())
                    .mainImageUrl(mainImageUrl)
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "객실 상세 조회 응답")
    public static class DetailResponse {
        @Schema(description = "객실 ID", example = "1")
        private Long id;

        @Schema(description = "숙소 ID", example = "1")
        private Long accommodationId;

        @Schema(description = "객실 이름", example = "디럭스 더블룸")
        private String name;

        @Schema(description = "객실 설명", example = "아늑한 분위기의 디럭스 더블룸입니다.")
        private String description;

        @Schema(description = "수용 인원", example = "2")
        private Integer capacity;

        @Schema(description = "1박 가격", example = "120000")
        private BigDecimal pricePerNight;

        @Schema(description = "객실 상태", example = "AVAILABLE")
        private String status;

        @Schema(description = "이미지 URL 목록")
        private List<String> imageUrls;

        @Schema(description = "객실 옵션 목록")
        private List<RoomOptionDto> options;
        
        @Schema(description = "생성일시")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        @Schema(description = "수정일시")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
        
        public static DetailResponse fromEntity(Room room) {
            return DetailResponse.builder()
                    .id(room.getId())
                    .accommodationId(room.getAccommodationId())
                    .name(room.getName())
                    .description(room.getDescription())
                    .capacity(room.getCapacity())
                    .pricePerNight(room.getPricePerNight())
                    .status(room.getStatus())
                    .imageUrls(new ArrayList<>())
                    .options(new ArrayList<>())
                    .createdAt(room.getCreatedAt())
                    .updatedAt(room.getUpdatedAt())
                    .build();
        }
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "객실 검색 조건")
    public static class SearchCondition {
        @Schema(description = "검색 키워드 (객실 이름, 설명)", example = "디럭스")
        private String keyword;

        @Schema(description = "최소 가격", example = "50000")
        private BigDecimal minPrice;

        @Schema(description = "최대 가격", example = "150000")
        private BigDecimal maxPrice;
        
        @Schema(description = "최소 수용 인원", example = "2")
        private Integer minCapacity;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "객실 검색 요청")
    public static class SearchRequest {
        @Schema(description = "검색 조건")
        private SearchCondition condition;

        @Schema(description = "페이지 요청 정보")
        private PageRequestDto pageRequest;
    }
} 