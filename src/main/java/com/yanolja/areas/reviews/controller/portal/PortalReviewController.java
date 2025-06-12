package com.yanolja.areas.reviews.controller.portal;

import com.yanolja.areas.reviews.dto.ReviewDto;
import com.yanolja.areas.reviews.service.ReviewService;
import com.yanolja.areas.user.domain.UserDetail;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/portal/reviews")
@RequiredArgsConstructor
@Tag(name = "포털 리뷰", description = "일반 사용자용 리뷰 API")
public class PortalReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "숙박 완료 후 리뷰를 작성합니다.")
    @PostMapping
    public ApiResponse<ReviewDto.Response> createReview(
            @AuthenticationPrincipal UserDetail userDetail,
            @RequestBody @Valid ReviewDto.CreateRequest request) {
        
        ReviewDto.Response response = reviewService.createReview(userDetail.getId(), request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "내 리뷰 수정", description = "작성한 리뷰를 수정합니다.")
    @PutMapping("/{reviewId}")
    public ApiResponse<ReviewDto.Response> updateReview(
            @AuthenticationPrincipal UserDetail userDetail,
            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewDto.UpdateRequest request) {
        
        ReviewDto.Response response = reviewService.updateReview(userDetail.getId(), reviewId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "내 리뷰 삭제", description = "작성한 리뷰를 삭제합니다.")
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @AuthenticationPrincipal UserDetail userDetail,
            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId) {
        
        reviewService.deleteReview(userDetail.getId(), reviewId);
        return ApiResponse.success();
    }

    @Operation(summary = "리뷰 상세 조회", description = "리뷰의 상세 정보를 조회합니다.")
    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewDto.Response> getReview(
            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId) {
        
        ReviewDto.Response response = reviewService.getReview(reviewId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "내 리뷰 목록", description = "로그인한 사용자가 작성한 리뷰 목록을 조회합니다.")
    @GetMapping("/my")
    public ApiResponse<Page<ReviewDto.ListResponse>> getMyReviews(
            @AuthenticationPrincipal UserDetail userDetail,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ReviewDto.ListResponse> responses = reviewService.getReviewsByUser(userDetail.getId(), pageable);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "숙소 리뷰 목록", description = "특정 숙소의 리뷰 목록을 조회합니다.")
    @GetMapping("/accommodations/{accommodationId}")
    public ApiResponse<Page<ReviewDto.ListResponse>> getAccommodationReviews(
            @Parameter(description = "숙소 ID", example = "1")
            @PathVariable Long accommodationId,
            @Parameter(description = "최소 평점 필터", example = "4")
            @RequestParam(required = false) Integer minRating,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        if (minRating != null) {
            // 평점 필터가 있는 경우 검색 API 사용
            ReviewDto.SearchRequest searchRequest = ReviewDto.SearchRequest.builder()
                    .accommodationId(accommodationId)
                    .minRating(minRating)
                    .page(pageable.getPageNumber())
                    .size(pageable.getPageSize())
                    .sortBy("createdAt")
                    .sortDirection("desc")
                    .build();
            
            Page<ReviewDto.ListResponse> responses = reviewService.searchReviews(searchRequest);
            return ApiResponse.success(responses);
        } else {
            Page<ReviewDto.ListResponse> responses = reviewService.getReviewsByAccommodation(accommodationId, pageable);
            return ApiResponse.success(responses);
        }
    }

    @Operation(summary = "숙소 리뷰 통계", description = "숙소의 리뷰 통계 정보를 조회합니다.")
    @GetMapping("/accommodations/{accommodationId}/statistics")
    public ApiResponse<ReviewDto.StatisticsResponse> getAccommodationReviewStatistics(
            @Parameter(description = "숙소 ID", example = "1")
            @PathVariable Long accommodationId) {
        
        ReviewDto.StatisticsResponse response = reviewService.getReviewStatistics(accommodationId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "숙소 최신 리뷰", description = "숙소의 최신 리뷰를 조회합니다.")
    @GetMapping("/accommodations/{accommodationId}/recent")
    public ApiResponse<List<ReviewDto.ListResponse>> getAccommodationRecentReviews(
            @Parameter(description = "숙소 ID", example = "1")
            @PathVariable Long accommodationId,
            @Parameter(description = "조회할 개수", example = "3")
            @RequestParam(defaultValue = "3") int limit) {
        
        List<ReviewDto.ListResponse> responses = reviewService.getRecentReviews(accommodationId, limit);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "리뷰 작성 가능 여부 확인", description = "특정 예약에 대해 리뷰 작성이 가능한지 확인합니다.")
    @GetMapping("/reservations/{reservationId}/can-review")
    public ApiResponse<Boolean> canWriteReview(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long reservationId) {
        
        boolean hasReview = reviewService.hasReviewForReservation(reservationId);
        boolean canReview = !hasReview; // 리뷰가 없으면 작성 가능
        
        return ApiResponse.success(canReview);
    }

    @Operation(summary = "리뷰 검색", description = "다양한 조건으로 리뷰를 검색합니다.")
    @PostMapping("/search")
    public ApiResponse<Page<ReviewDto.ListResponse>> searchReviews(
            @RequestBody @Valid ReviewDto.SearchRequest searchRequest) {
        
        Page<ReviewDto.ListResponse> responses = reviewService.searchReviews(searchRequest);
        return ApiResponse.success(responses);
    }
} 