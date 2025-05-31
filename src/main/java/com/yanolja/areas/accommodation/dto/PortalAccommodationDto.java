package com.yanolja.areas.accommodation.dto;

import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.common.dto.PageRequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

public class PortalAccommodationDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "숙소 검색 요청 DTO")
    public static class SearchRequest {
        
        @Schema(description = "검색 조건")
        private SearchCondition condition;
        
        @Schema(description = "페이지 요청 정보")
        private PageRequestDto pageRequest;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "검색 조건 DTO")
    public static class SearchCondition {
        
        @Schema(description = "검색 키워드 (숙소 이름, 주소 검색)")
        private String keyword;
        
        @Schema(description = "최소 가격")
        private BigDecimal minPrice;
        
        @Schema(description = "최대 가격")
        private BigDecimal maxPrice;
    }
    
    /**
     * 숙소 정렬 컬럼 매핑 함수
     */
    public static String mapSortColumn(String sortColumn) {
        return switch (sortColumn != null ? sortColumn.toLowerCase() : "") {
            case "price" -> "pricePerNight";
            case "rating" -> "rating";
            case "reviewcount" -> "reviewCount";
            default -> "id";
        };
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
                    .mainImageUrl(null) // 실제 이미지 URL 로직 구현 필요
                    .build();
        }
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "숙소 상세 조회 응답 DTO")
    public static class DetailResponse {
        
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
        
        @Schema(description = "이미지 URL 목록")
        private List<String> imageUrls;
        
        @Schema(description = "편의 시설 정보")
        private List<String> amenities;
        
        public static DetailResponse fromEntity(Accommodation accommodation) {
            return DetailResponse.builder()
                    .id(accommodation.getId())
                    .name(accommodation.getName())
                    .description(accommodation.getDescription())
                    .address(accommodation.getAddress())
                    .latitude(accommodation.getLatitude())
                    .longitude(accommodation.getLongitude())
                    .pricePerNight(accommodation.getPricePerNight())
                    .rating(accommodation.getRating())
                    .reviewCount(accommodation.getReviewCount())
                    .imageUrls(List.of()) // 실제 이미지 URL 로직 구현 필요
                    .amenities(List.of()) // 실제 편의시설 정보 로직 구현 필요
                    .build();
        }
    }
} 