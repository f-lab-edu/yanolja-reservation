package com.yanolja.areas.room.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yanolja.areas.room.entity.RoomOption;
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

public class RoomOptionDto {

    @Getter
    @Setter
    @Schema(description = "객실 옵션 등록/수정 요청")
    public static class Request {
        @Schema(description = "옵션 이름", example = "조식 서비스")
        @NotBlank(message = "옵션 이름은 필수입니다.")
        private String name;

        @Schema(description = "옵션 가격", example = "25000")
        @NotNull(message = "옵션 가격은 필수입니다.")
        @Min(value = 0, message = "옵션 가격은 0원 이상이어야 합니다.")
        private BigDecimal price;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "객실 옵션 상세 응답")
    public static class Response {
        @Schema(description = "옵션 ID", example = "1")
        private Long id;
        
        @Schema(description = "옵션 이름", example = "조식 서비스")
        private String name;
        
        @Schema(description = "옵션 가격", example = "25000")
        private BigDecimal price;
        
        @Schema(description = "생성일시")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        @Schema(description = "수정일시")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
        
        public static Response fromEntity(RoomOption roomOption) {
            return Response.builder()
                    .id(roomOption.getId())
                    .name(roomOption.getName())
                    .price(roomOption.getPrice())
                    .createdAt(roomOption.getCreatedAt())
                    .updatedAt(roomOption.getUpdatedAt())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "객실 옵션 목록 응답")
    public static class ListResponse {
        @Schema(description = "옵션 ID", example = "1")
        private Long id;
        
        @Schema(description = "옵션 이름", example = "조식 서비스")
        private String name;
        
        @Schema(description = "옵션 가격", example = "25000")
        private BigDecimal price;
        
        public static ListResponse fromEntity(RoomOption roomOption) {
            return ListResponse.builder()
                    .id(roomOption.getId())
                    .name(roomOption.getName())
                    .price(roomOption.getPrice())
                    .build();
        }
    }
} 