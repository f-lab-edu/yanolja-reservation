package com.yanolja.areas.reviews.controller;

import com.yanolja.areas.reviews.dto.ReviewDto;
import com.yanolja.areas.reviews.service.ReviewService;
import com.yanolja.areas.user.domain.UserDetail;
import com.yanolja.areas.user.domain.UserRole;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")  // 컨트롤러 전체에 관리자 권한 체크 적용
@Tag(name = "리뷰 관리", description = "관리자용 리뷰 관리 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 상세 조회", description = "리뷰 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewDto.Response> getReview(
            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId) {
        
        ReviewDto.Response response = reviewService.getReview(reviewId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "숙소별 리뷰 조회", description = "특정 숙소의 리뷰 목록을 조회합니다.")
    @GetMapping("/accommodations/{accommodationId}")
    public ApiResponse<Page<ReviewDto.ListResponse>> getReviewsByAccommodation(
            @Parameter(description = "숙소 ID", example = "1")
            @PathVariable Long accommodationId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ReviewDto.ListResponse> responses = reviewService.getReviewsByAccommodation(accommodationId, pageable);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "사용자별 리뷰 조회", description = "특정 사용자의 리뷰 목록을 조회합니다.")
    @GetMapping("/users/{userId}")
    public ApiResponse<Page<ReviewDto.ListResponse>> getReviewsByUser(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ReviewDto.ListResponse> responses = reviewService.getReviewsByUser(userId, pageable);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "리뷰 검색", description = "다양한 조건으로 리뷰를 검색합니다.")
    @PostMapping("/search")
    public ApiResponse<Page<ReviewDto.ListResponse>> searchReviews(
            @RequestBody @Valid ReviewDto.SearchRequest searchRequest) {
        
        Page<ReviewDto.ListResponse> responses = reviewService.searchReviews(searchRequest);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "숙소 리뷰 통계", description = "숙소의 리뷰 통계 정보를 조회합니다.")
    @GetMapping("/accommodations/{accommodationId}/statistics")
    public ApiResponse<ReviewDto.StatisticsResponse> getReviewStatistics(
            @Parameter(description = "숙소 ID", example = "1")
            @PathVariable Long accommodationId) {
        
        ReviewDto.StatisticsResponse response = reviewService.getReviewStatistics(accommodationId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "최신 리뷰 조회", description = "숙소의 최신 리뷰 목록을 조회합니다.")
    @GetMapping("/accommodations/{accommodationId}/recent")
    public ApiResponse<List<ReviewDto.ListResponse>> getRecentReviews(
            @Parameter(description = "숙소 ID", example = "1")
            @PathVariable Long accommodationId,
            @Parameter(description = "조회할 개수", example = "5")
            @RequestParam(defaultValue = "5") int limit) {
        
        List<ReviewDto.ListResponse> responses = reviewService.getRecentReviews(accommodationId, limit);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "리뷰 삭제", description = "관리자가 부적절한 리뷰를 삭제합니다.")
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @Parameter(description = "리뷰 ID", example = "1")
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetail userDetail) {
        
        reviewService.deleteReview(null, reviewId);
        return ApiResponse.success();
    }

    @Operation(summary = "예약 리뷰 존재 확인", description = "특정 예약에 대한 리뷰 존재 여부를 확인합니다.")
    @GetMapping("/reservations/{reservationId}/exists")
    public ApiResponse<Boolean> hasReviewForReservation(
            @Parameter(description = "예약 ID", example = "1")
            @PathVariable Long reservationId) {
        
        boolean exists = reviewService.hasReviewForReservation(reservationId);
        return ApiResponse.success(exists);
    }
} 