package com.yanolja.areas.room.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yanolja.areas.room.entity.Room;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RoomDto {

    @Getter
    @Setter
    @Schema(description = "객실 등록/수정 요청")
    public static class Request {
        @Schema(description = "숙소 ID", example = "1")
        @NotNull(message = "숙소 ID는 필수입니다.")
        private Long accommodationId;

        @Schema(description = "객실 이름", example = "디럭스 더블룸")
        @NotBlank(message = "객실 이름은 필수입니다.")
        private String name;

        @Schema(description = "객실 설명", example = "아늑한 분위기의 디럭스 더블룸입니다.")
        private String description;

        @Schema(description = "수용 인원", example = "2")
        @NotNull(message = "수용 인원은 필수입니다.")
        @Min(value = 1, message = "수용 인원은 1명 이상이어야 합니다.")
        private Integer capacity;

        @Schema(description = "1박 가격", example = "120000")
        @NotNull(message = "객실 가격은 필수입니다.")
        @Min(value = 0, message = "객실 가격은 0원 이상이어야 합니다.")
        private BigDecimal pricePerNight;

        @Schema(description = "객실 이미지 URL 목록")
        private List<String> imageUrls;

        @Schema(description = "객실 옵션 ID 목록")
        private List<Long> optionIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "객실 상세 응답")
    public static class Response {
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

        @Schema(description = "객실 이미지 목록")
        private List<RoomImageDto.Response> images;

        @Schema(description = "객실 옵션 목록")
        private List<RoomOptionDto> options;
        
        @Schema(description = "생성일시")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        @Schema(description = "수정일시")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
        
        public static Response fromEntity(Room room) {
            return Response.builder()
                    .id(room.getId())
                    .accommodationId(room.getAccommodationId())
                    .name(room.getName())
                    .description(room.getDescription())
                    .capacity(room.getCapacity())
                    .pricePerNight(room.getPricePerNight())
                    .status(room.getStatus())
                    .images(new ArrayList<>())
                    .options(new ArrayList<>())
                    .createdAt(room.getCreatedAt())
                    .updatedAt(room.getUpdatedAt())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "객실 목록 응답")
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
} 