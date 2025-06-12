package com.yanolja.areas.reviews.service;

import com.yanolja.areas.reviews.dto.ReviewDto;
import com.yanolja.areas.reviews.entity.Review;
import com.yanolja.areas.reviews.entity.ReviewImage;
import com.yanolja.areas.reviews.repository.ReviewImageRepository;
import com.yanolja.areas.reviews.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService 테스트")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review review;
    private ReviewImage reviewImage;
    private ReviewDto.CreateRequest createRequest;
    private ReviewDto.UpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Test data setup
        review = Review.createReview(1L, 1L, 1L, 5, "정말 좋은 숙소였습니다.");
        reviewImage = ReviewImage.createReviewImage(review, "https://example.com/image1.jpg");
        
        createRequest = ReviewDto.CreateRequest.builder()
                .accommodationId(1L)
                .reservationId(1L)
                .rating(5)
                .comment("정말 좋은 숙소였습니다.")
                .imageUrls(Arrays.asList("https://example.com/image1.jpg"))
                .build();

        updateRequest = ReviewDto.UpdateRequest.builder()
                .rating(4)
                .comment("수정된 리뷰 내용입니다.")
                .imageUrls(Arrays.asList("https://example.com/image2.jpg"))
                .build();
    }

    @Test
    @DisplayName("리뷰 생성 성공")
    void createReview_Success() {
        // Given
        Long userId = 1L;
        when(reviewRepository.existsByReservationId(1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewImageRepository.saveAll(anyList())).thenReturn(Collections.singletonList(reviewImage));

        // When
        ReviewDto.Response response = reviewService.createReview(userId, createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getAccommodationId()).isEqualTo(1L);
        assertThat(response.getReservationId()).isEqualTo(1L);
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("정말 좋은 숙소였습니다.");

        verify(reviewRepository).existsByReservationId(1L);
        verify(reviewRepository).save(any(Review.class));
        verify(reviewImageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 중복 리뷰")
    void createReview_Fail_DuplicateReview() {
        // Given
        Long userId = 1L;
        when(reviewRepository.existsByReservationId(1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(userId, createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 해당 예약에 대한 리뷰가 존재합니다.");

        verify(reviewRepository).existsByReservationId(1L);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 생성 실패 - 잘못된 평점")
    void createReview_Fail_InvalidRating() {
        // Given
        Long userId = 1L;
        ReviewDto.CreateRequest invalidRequest = ReviewDto.CreateRequest.builder()
                .accommodationId(1L)
                .reservationId(1L)
                .rating(6) // 잘못된 평점
                .comment("정말 좋은 숙소였습니다.")
                .build();

        when(reviewRepository.existsByReservationId(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(userId, invalidRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("평점은 1~5 사이의 값이어야 합니다.");

        verify(reviewRepository).existsByReservationId(1L);
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_Success() {
        // Given
        Long userId = 1L;
        Long reviewId = 1L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // When
        ReviewDto.Response response = reviewService.updateReview(userId, reviewId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(4);
        assertThat(response.getComment()).isEqualTo("수정된 리뷰 내용입니다.");

        verify(reviewRepository).findById(reviewId);
        verify(reviewImageRepository).deleteByReviewId(reviewId);
        verify(reviewImageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재하지 않는 리뷰")
    void updateReview_Fail_ReviewNotFound() {
        // Given
        Long userId = 1L;
        Long reviewId = 1L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.updateReview(userId, reviewId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 리뷰입니다.");

        verify(reviewRepository).findById(reviewId);
        verify(reviewImageRepository, never()).deleteByReviewId(anyLong());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 권한 없음")
    void updateReview_Fail_NoPermission() {
        // Given
        Long userId = 2L; // 다른 사용자
        Long reviewId = 1L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // When & Then
        assertThatThrownBy(() -> reviewService.updateReview(userId, reviewId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 수정 권한이 없습니다.");

        verify(reviewRepository).findById(reviewId);
        verify(reviewImageRepository, never()).deleteByReviewId(anyLong());
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_Success() {
        // Given
        Long userId = 1L;
        Long reviewId = 1L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // When
        reviewService.deleteReview(userId, reviewId);

        // Then
        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 존재하지 않는 리뷰")
    void deleteReview_Fail_ReviewNotFound() {
        // Given
        Long userId = 1L;
        Long reviewId = 1L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.deleteReview(userId, reviewId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 리뷰입니다.");

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 권한 없음")
    void deleteReview_Fail_NoPermission() {
        // Given
        Long userId = 2L; // 다른 사용자
        Long reviewId = 1L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // When & Then
        assertThatThrownBy(() -> reviewService.deleteReview(userId, reviewId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 삭제 권한이 없습니다.");

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository, never()).delete(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 상세 조회 성공")
    void getReview_Success() {
        // Given
        Long reviewId = 1L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // When
        ReviewDto.Response response = reviewService.getReview(reviewId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNull(); // review 엔티티에 id가 설정되지 않음
        assertThat(response.getUserId()).isEqualTo(1L);

        verify(reviewRepository).findById(reviewId);
    }

    @Test
    @DisplayName("리뷰 상세 조회 실패 - 존재하지 않는 리뷰")
    void getReview_Fail_ReviewNotFound() {
        // Given
        Long reviewId = 1L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.getReview(reviewId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 리뷰입니다.");

        verify(reviewRepository).findById(reviewId);
    }

    @Test
    @DisplayName("숙소별 리뷰 조회 성공")
    void getReviewsByAccommodation_Success() {
        // Given
        Long accommodationId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review));
        
        when(reviewRepository.findByAccommodationIdOrderByCreatedAtDesc(accommodationId, pageable))
                .thenReturn(reviewPage);

        // When
        Page<ReviewDto.ListResponse> response = reviewService.getReviewsByAccommodation(accommodationId, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getUserId()).isEqualTo(1L);

        verify(reviewRepository).findByAccommodationIdOrderByCreatedAtDesc(accommodationId, pageable);
    }

    @Test
    @DisplayName("사용자별 리뷰 조회 성공")
    void getReviewsByUser_Success() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review));
        
        when(reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                .thenReturn(reviewPage);

        // When
        Page<ReviewDto.ListResponse> response = reviewService.getReviewsByUser(userId, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getUserId()).isEqualTo(1L);

        verify(reviewRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Test
    @DisplayName("리뷰 검색 성공")
    void searchReviews_Success() {
        // Given
        ReviewDto.SearchRequest searchRequest = ReviewDto.SearchRequest.builder()
                .accommodationId(1L)
                .minRating(4)
                .page(0)
                .size(10)
                .build();
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review));
        
        when(reviewRepository.searchReviews(searchRequest, pageable)).thenReturn(reviewPage);

        // When
        Page<ReviewDto.ListResponse> response = reviewService.searchReviews(searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);

        verify(reviewRepository).searchReviews(eq(searchRequest), any(Pageable.class));
    }

    @Test
    @DisplayName("리뷰 통계 조회 성공")
    void getReviewStatistics_Success() {
        // Given
        Long accommodationId = 1L;
        when(reviewRepository.countByAccommodationId(accommodationId)).thenReturn(100);
        when(reviewRepository.findAverageRatingByAccommodationId(accommodationId)).thenReturn(4.5);
        when(reviewRepository.getRatingStatistics(accommodationId))
                .thenReturn(Arrays.asList(5, 10, 15, 30, 40));

        // When
        ReviewDto.StatisticsResponse response = reviewService.getReviewStatistics(accommodationId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalReviews()).isEqualTo(100);
        assertThat(response.getAverageRating()).isEqualTo(4.5);
        assertThat(response.getRatingCounts()).hasSize(5);

        verify(reviewRepository).countByAccommodationId(accommodationId);
        verify(reviewRepository).findAverageRatingByAccommodationId(accommodationId);
        verify(reviewRepository).getRatingStatistics(accommodationId);
    }

    @Test
    @DisplayName("최신 리뷰 조회 성공")
    void getRecentReviews_Success() {
        // Given
        Long accommodationId = 1L;
        int limit = 5;
        List<Review> reviews = Collections.singletonList(review);
        
        when(reviewRepository.findRecentReviewsByAccommodationId(accommodationId, limit))
                .thenReturn(reviews);

        // When
        List<ReviewDto.ListResponse> response = reviewService.getRecentReviews(accommodationId, limit);

        // Then
        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getUserId()).isEqualTo(1L);

        verify(reviewRepository).findRecentReviewsByAccommodationId(accommodationId, limit);
    }

    @Test
    @DisplayName("예약 리뷰 존재 여부 확인")
    void hasReviewForReservation_Success() {
        // Given
        Long reservationId = 1L;
        when(reviewRepository.existsByReservationId(reservationId)).thenReturn(true);

        // When
        boolean result = reviewService.hasReviewForReservation(reservationId);

        // Then
        assertThat(result).isTrue();

        verify(reviewRepository).existsByReservationId(reservationId);
    }
} 