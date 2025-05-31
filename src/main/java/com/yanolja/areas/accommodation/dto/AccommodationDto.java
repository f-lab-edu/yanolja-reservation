package com.yanolja.areas.accommodation.dto;

import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.AccommodationImage;
import com.yanolja.areas.room.dto.RoomDto;
import com.yanolja.areas.accommodation.entity.AccommodationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AccommodationDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "숙소 등록/수정 요청 DTO")
    public static class Request {

        @NotBlank(message = "숙소 이름은 필수입니다.")
        @Schema(description = "숙소 이름")
        private String name;

        @Schema(description = "숙소 설명")
        private String description;

        @NotBlank(message = "주소는 필수입니다.")
        @Schema(description = "숙소 주소")
        private String address;

        @Schema(description = "위도")
        private BigDecimal latitude;
       
        @Schema(description = "경도")
        private BigDecimal longitude;

        @NotNull(message = "1박 가격은 필수입니다.")
        @PositiveOrZero(message = "1박 가격은 0 이상이어야 합니다.")
        @Schema(description = "1박 기준 가격")
        private BigDecimal pricePerNight;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "숙소 상세 응답 DTO")
    public static class Response {
      
        @Schema(description = "숙소 ID")
        private Long id;
      
        @Schema(description = "숙소 이름")
        private String name;
      
        @Schema(description = "숙소 설명")
        private String description;
      
        @Schema(description = "숙소 주소")
        private String address;
      
        @Schema(description = "위도")
        private BigDecimal latitude;
      
        @Schema(description = "경도")
        private BigDecimal longitude;
      
        @Schema(description = "1박 기준 가격")
        private BigDecimal pricePerNight;
      
        @Schema(description = "평점")
        private BigDecimal rating;
      
        @Schema(description = "리뷰 수")
        private Integer reviewCount;
      
        @Schema(description = "숙소 상태")
        private AccommodationStatus status;
        
        @Schema(description = "숙소 이미지 목록")
        private List<AccommodationImageDto.Response> images;
        
        @Schema(description = "편의시설 목록")
        private List<AmenityDto.Response> amenities;
        
        @Schema(description = "객실 목록")
        private List<RoomDto.ListResponse> rooms;

        public static Response fromEntity(Accommodation accommodation) {
            return Response.builder()
                    .id(accommodation.getId())
                    .name(accommodation.getName())
                    .description(accommodation.getDescription())
                    .address(accommodation.getAddress())
                    .latitude(accommodation.getLatitude())
                    .longitude(accommodation.getLongitude())
                    .pricePerNight(accommodation.getPricePerNight())
                    .rating(accommodation.getRating())
                    .reviewCount(accommodation.getReviewCount())
                    .status(accommodation.getStatus())
                    .images(new ArrayList<>())
                    .amenities(new ArrayList<>())
                    .rooms(new ArrayList<>())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "숙소 목록 조회 응답 DTO")
    public static class ListResponse {

        @Schema(description = "숙소 ID")
        private Long id;
        
        @Schema(description = "숙소 이름")
        private String name;
        
        @Schema(description = "숙소 주소")
        private String address;

        @Schema(description = "1박 기준 가격")
        private BigDecimal pricePerNight;
        
        @Schema(description = "평점")
        private BigDecimal rating;
        
        @Schema(description = "리뷰 수")
        private Integer reviewCount;
        
        @Schema(description = "대표 이미지 URL")
        private String mainImageUrl;

        public static ListResponse fromEntity(Accommodation accommodation) {
            return ListResponse.builder()
                    .id(accommodation.getId())
                    .name(accommodation.getName())
                    .address(accommodation.getAddress())
                    .pricePerNight(accommodation.getPricePerNight())
                    .rating(accommodation.getRating())
                    .reviewCount(accommodation.getReviewCount())
                    .build();
        }
        
        public static ListResponse fromEntityWithMainImage(Accommodation accommodation, String mainImageUrl) {
            return ListResponse.builder()
                    .id(accommodation.getId())
                    .name(accommodation.getName())
                    .address(accommodation.getAddress())
                    .pricePerNight(accommodation.getPricePerNight())
                    .rating(accommodation.getRating())
                    .reviewCount(accommodation.getReviewCount())
                    .mainImageUrl(mainImageUrl)
                    .build();
        }
    }
} 