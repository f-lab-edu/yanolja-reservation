package com.yanolja.areas.reviews.dto;

import com.yanolja.areas.reviews.entity.Review;
import com.yanolja.areas.reviews.entity.ReviewImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ReviewDto {

    /**
     * 리뷰 생성 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "리뷰 생성 요청 DTO")
    public static class CreateRequest {

        @NotNull(message = "숙소 ID는 필수입니다.")
        @Schema(description = "숙소 ID", example = "1")
        private Long accommodationId;

        @NotNull(message = "예약 ID는 필수입니다.")
        @Schema(description = "예약 ID", example = "1")
        private Long reservationId;

        @NotNull(message = "평점은 필수입니다.")
        @Min(value = 1, message = "평점은 1 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5 이하여야 합니다.")
        @Schema(description = "평점 (1-5)", example = "5")
        private Integer rating;

        @Size(max = 1000, message = "리뷰 내용은 1000자 이하여야 합니다.")
        @Schema(description = "리뷰 내용", example = "정말 좋은 숙소였습니다.")
        private String comment;

        @Size(max = 5, message = "이미지는 최대 5개까지 등록 가능합니다.")
        @Schema(description = "이미지 URL 목록")
        private List<String> imageUrls;
    }

    /**
     * 리뷰 수정 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "리뷰 수정 요청 DTO")
    public static class UpdateRequest {

        @NotNull(message = "평점은 필수입니다.")
        @Min(value = 1, message = "평점은 1 이상이어야 합니다.")
        @Max(value = 5, message = "평점은 5 이하여야 합니다.")
        @Schema(description = "평점 (1-5)", example = "4")
        private Integer rating;

        @Size(max = 1000, message = "리뷰 내용은 1000자 이하여야 합니다.")
        @Schema(description = "리뷰 내용", example = "수정된 리뷰 내용입니다.")
        private String comment;

        @Size(max = 5, message = "이미지는 최대 5개까지 등록 가능합니다.")
        @Schema(description = "이미지 URL 목록")
        private List<String> imageUrls;
    }

    /**
     * 리뷰 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "리뷰 응답 DTO")
    public static class Response {

        @Schema(description = "리뷰 ID", example = "1")
        private Long id;

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "숙소 ID", example = "1")
        private Long accommodationId;

        @Schema(description = "예약 ID", example = "1")
        private Long reservationId;

        @Schema(description = "평점 (1-5)", example = "5")
        private Integer rating;

        @Schema(description = "리뷰 내용", example = "정말 좋은 숙소였습니다.")
        private String comment;

        @Schema(description = "이미지 URL 목록")
        private List<String> imageUrls;

        @Schema(description = "작성일시", example = "2024-03-01T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "수정일시", example = "2024-03-01T10:00:00")
        private LocalDateTime updatedAt;

        @Schema(description = "작성자", example = "user@example.com")
        private String createdBy;

        /**
         * Review 엔티티로부터 DTO 생성
         */
        public static Response from(Review review) {
            List<String> imageUrls = review.getReviewImages().stream()
                    .map(ReviewImage::getImageUrl)
                    .collect(Collectors.toList());

            return Response.builder()
                    .id(review.getId())
                    .userId(review.getUserId())
                    .accommodationId(review.getAccommodationId())
                    .reservationId(review.getReservationId())
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .imageUrls(imageUrls)
                    .createdAt(review.getCreatedAt())
                    .updatedAt(review.getUpdatedAt())
                    .createdBy(review.getCreatedBy())
                    .build();
        }
    }

    /**
     * 리뷰 목록 응답 DTO (간단 정보)
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "리뷰 목록 응답 DTO")
    public static class ListResponse {

        @Schema(description = "리뷰 ID", example = "1")
        private Long id;

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "평점 (1-5)", example = "5")
        private Integer rating;

        @Schema(description = "리뷰 내용", example = "정말 좋은 숙소였습니다.")
        private String comment;

        @Schema(description = "이미지 개수", example = "3")
        private Integer imageCount;

        @Schema(description = "작성일시", example = "2024-03-01T10:00:00")
        private LocalDateTime createdAt;

        @Schema(description = "작성자", example = "user@example.com")
        private String createdBy;

        /**
         * Review 엔티티로부터 DTO 생성
         */
        public static ListResponse from(Review review) {
            return ListResponse.builder()
                    .id(review.getId())
                    .userId(review.getUserId())
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .imageCount(review.getReviewImages().size())
                    .createdAt(review.getCreatedAt())
                    .createdBy(review.getCreatedBy())
                    .build();
        }
    }

    /**
     * 리뷰 검색 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "리뷰 검색 요청 DTO")
    public static class SearchRequest {

        @Schema(description = "숙소 ID", example = "1")
        private Long accommodationId;

        @Schema(description = "사용자 ID", example = "1")
        private Long userId;

        @Schema(description = "최소 평점", example = "4")
        @Min(value = 1, message = "최소 평점은 1 이상이어야 합니다.")
        @Max(value = 5, message = "최소 평점은 5 이하여야 합니다.")
        private Integer minRating;

        @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
        @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
        @Builder.Default
        private Integer page = 0;

        @Schema(description = "페이지 크기", example = "10")
        @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
        @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
        @Builder.Default
        private Integer size = 10;

        @Schema(description = "정렬 기준 (createdAt, rating)", example = "createdAt")
        @Builder.Default
        private String sortBy = "createdAt";

        @Schema(description = "정렬 방향 (asc, desc)", example = "desc")
        @Builder.Default
        private String sortDirection = "desc";
    }

    /**
     * 숙소 리뷰 통계 응답 DTO
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "숙소 리뷰 통계 응답 DTO")
    public static class StatisticsResponse {

        @Schema(description = "총 리뷰 개수", example = "150")
        private Integer totalReviews;

        @Schema(description = "평균 평점", example = "4.5")
        private Double averageRating;

        @Schema(description = "평점별 개수 (1점부터 5점까지)", example = "[2, 5, 15, 45, 83]")
        private List<Integer> ratingCounts;
    }
} 