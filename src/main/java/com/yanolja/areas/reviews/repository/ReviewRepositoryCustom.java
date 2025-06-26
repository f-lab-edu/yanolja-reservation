package com.yanolja.areas.reviews.repository;

import com.yanolja.areas.reviews.dto.ReviewDto;
import com.yanolja.areas.reviews.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewRepositoryCustom {

    /**
     * 복합 조건으로 리뷰 검색
     * @param searchRequest 검색 조건
     * @param pageable 페이징 정보
     * @return 리뷰 페이지
     */
    Page<Review> searchReviews(ReviewDto.SearchRequest searchRequest, Pageable pageable);

    /**
     * 숙소별 평점 통계 조회
     * @param accommodationId 숙소 ID
     * @return 평점별 개수 (1점부터 5점까지)
     */
    List<Integer> getRatingStatistics(Long accommodationId);

    /**
     * 사용자가 작성한 리뷰 목록 (페이징)
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 리뷰 페이지
     */
    Page<Review> findReviewsByUserId(Long userId, Pageable pageable);

    /**
     * 숙소의 최신 리뷰 목록 조회
     * @param accommodationId 숙소 ID
     * @param limit 조회할 개수
     * @return 최신 리뷰 목록
     */
    List<Review> findRecentReviewsByAccommodationId(Long accommodationId, int limit);
} 