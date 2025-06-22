package com.yanolja.areas.reviews.service;

import com.yanolja.areas.reviews.dto.ReviewDto;
import com.yanolja.areas.reviews.entity.Review;
import com.yanolja.areas.reviews.entity.ReviewImage;
import com.yanolja.areas.reviews.repository.ReviewImageRepository;
import com.yanolja.areas.reviews.repository.ReviewRepository;
import com.yanolja.areas.accommodation.service.AccommodationService;
import com.yanolja.common.service.DistributedLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final AccommodationService accommodationService;
    private final DistributedLockService distributedLockService;
    private final ReviewImageService reviewImageService;
    
    private static final int MAX_RETRIES = 3;
    private static final long LOCK_TIMEOUT = 10L; // 10초
    private static final long LOCK_LEASE_TIME = 30L; // 30초

    /**
     * 리뷰 생성 (이미지 파일 포함)
     * @param userId 사용자 ID
     * @param request 리뷰 생성 요청
     * @param imageFiles 이미지 파일 배열
     * @return 생성된 리뷰 응답
     */
    @Transactional
    public ReviewDto.Response createReviewWithImages(Long userId, ReviewDto.CreateRequest request, MultipartFile[] imageFiles) throws IOException {
        log.info("리뷰 생성 시작 (이미지 포함) - userId: {}, accommodationId: {}, reservationId: {}, imageCount: {}", 
                userId, request.getAccommodationId(), request.getReservationId(), 
                imageFiles != null ? imageFiles.length : 0);

        // 이미지 개수 제한 확인
        if (imageFiles != null && imageFiles.length > 5) {
            throw new IllegalArgumentException("리뷰 이미지는 최대 5개까지 업로드 가능합니다.");
        }

        // 이미 작성된 리뷰가 있는지 확인
        if (reviewRepository.existsByReservationId(request.getReservationId())) {
            throw new IllegalArgumentException("이미 해당 예약에 대한 리뷰가 존재합니다.");
        }

        // 평점 유효성 검증
        if (!Review.isValidRating(request.getRating())) {
            throw new IllegalArgumentException("평점은 1~5 사이의 값이어야 합니다.");
        }

        // 분산 락을 사용하여 리뷰 생성
        String lockKey = "review:create:accommodation:" + request.getAccommodationId();
        
        return distributedLockService.executeWithLock(lockKey, LOCK_TIMEOUT, LOCK_LEASE_TIME, () -> {
            try {
                // 리뷰 생성
                Review review = Review.createReview(
                        userId,
                        request.getAccommodationId(),
                        request.getReservationId(),
                        request.getRating(),
                        request.getComment()
                );

                Review savedReview = reviewRepository.save(review);

                // 이미지 파일이 있으면 업로드
                if (imageFiles != null && imageFiles.length > 0) {
                    reviewImageService.saveImages(savedReview.getId(), imageFiles);
                }

                // 숙소의 리뷰 수와 평점 업데이트
                accommodationService.incrementReviewCountWithRetry(request.getAccommodationId(), MAX_RETRIES);
                updateAccommodationRating(request.getAccommodationId());

                log.info("리뷰 생성 완료 - reviewId: {}", savedReview.getId());
                return ReviewDto.Response.from(savedReview);
            } catch (IOException e) {
                log.error("리뷰 이미지 업로드 실패 - userId: {}, accommodationId: {}", userId, request.getAccommodationId(), e);
                throw new RuntimeException("리뷰 이미지 업로드에 실패했습니다.", e);
            }
        });
    }

    /**
     * 리뷰 생성 (기존 메서드 - 하위 호환성 유지)
     * @param userId 사용자 ID
     * @param request 리뷰 생성 요청
     * @return 생성된 리뷰 응답
     */
    @Transactional
    public ReviewDto.Response createReview(Long userId, ReviewDto.CreateRequest request) {
        log.info("리뷰 생성 시작 - userId: {}, accommodationId: {}, reservationId: {}", 
                userId, request.getAccommodationId(), request.getReservationId());

        // 이미 작성된 리뷰가 있는지 확인
        if (reviewRepository.existsByReservationId(request.getReservationId())) {
            throw new IllegalArgumentException("이미 해당 예약에 대한 리뷰가 존재합니다.");
        }

        // 평점 유효성 검증
        if (!Review.isValidRating(request.getRating())) {
            throw new IllegalArgumentException("평점은 1~5 사이의 값이어야 합니다.");
        }

        // 분산 락을 사용하여 리뷰 생성
        String lockKey = "review:create:accommodation:" + request.getAccommodationId();
        
        return distributedLockService.executeWithLock(lockKey, LOCK_TIMEOUT, LOCK_LEASE_TIME, () -> {
            // 리뷰 생성
            Review review = Review.createReview(
                    userId,
                    request.getAccommodationId(),
                    request.getReservationId(),
                    request.getRating(),
                    request.getComment()
            );

            Review savedReview = reviewRepository.save(review);

            // 이미지 URL이 있으면 이미지 저장
            if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
                List<ReviewImage> reviewImages = request.getImageUrls().stream()
                        .map(imageUrl -> ReviewImage.createReviewImage(savedReview, imageUrl))
                        .collect(Collectors.toList());
                reviewImageRepository.saveAll(reviewImages);
            }

            // 숙소의 리뷰 수와 평점 업데이트
            accommodationService.incrementReviewCountWithRetry(request.getAccommodationId(), MAX_RETRIES);
            updateAccommodationRating(request.getAccommodationId());

            log.info("리뷰 생성 완료 - reviewId: {}", savedReview.getId());
            return ReviewDto.Response.from(savedReview);
        });
    }

    /**
     * 리뷰 수정
     * @param userId 사용자 ID
     * @param reviewId 리뷰 ID
     * @param request 리뷰 수정 요청
     * @return 수정된 리뷰 응답
     */
    @Transactional
    public ReviewDto.Response updateReview(Long userId, Long reviewId, ReviewDto.UpdateRequest request) {
        log.info("리뷰 수정 시작 - userId: {}, reviewId: {}", userId, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 작성자 확인
        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("리뷰 수정 권한이 없습니다.");
        }

        // 평점 유효성 검증
        if (!Review.isValidRating(request.getRating())) {
            throw new IllegalArgumentException("평점은 1~5 사이의 값이어야 합니다.");
        }

        // 분산 락을 사용하여 리뷰 수정
        String lockKey = "review:update:accommodation:" + review.getAccommodationId();
        
        return distributedLockService.executeWithLock(lockKey, LOCK_TIMEOUT, LOCK_LEASE_TIME, () -> {
            // 리뷰 정보 수정
            review.updateReview(request.getRating(), request.getComment());

            // 기존 이미지 삭제 후 새 이미지 저장
            if (request.getImageUrls() != null) {
                reviewImageRepository.deleteByReviewId(reviewId);
                
                if (!request.getImageUrls().isEmpty()) {
                    List<ReviewImage> reviewImages = request.getImageUrls().stream()
                            .map(imageUrl -> ReviewImage.createReviewImage(review, imageUrl))
                            .collect(Collectors.toList());
                    reviewImageRepository.saveAll(reviewImages);
                }
            }

            // 숙소의 평점 업데이트
            updateAccommodationRating(review.getAccommodationId());

            log.info("리뷰 수정 완료 - reviewId: {}", reviewId);
            return ReviewDto.Response.from(review);
        });
    }

    /**
     * 리뷰 삭제
     * @param userId 사용자 ID
     * @param reviewId 리뷰 ID
     */
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        log.info("리뷰 삭제 시작 - userId: {}, reviewId: {}", userId, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        // 작성자 확인 (관리자는 userId가 null이므로 체크하지 않음)
        if (userId != null && !review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("리뷰 삭제 권한이 없습니다.");
        }

        // 분산 락을 사용하여 리뷰 삭제
        String lockKey = "review:delete:accommodation:" + review.getAccommodationId();
        Long accommodationId = review.getAccommodationId();
        
        distributedLockService.executeWithLock(lockKey, LOCK_TIMEOUT, LOCK_LEASE_TIME, () -> {
            reviewRepository.delete(review);

            // 숙소의 리뷰 수와 평점 업데이트
            accommodationService.decrementReviewCount(accommodationId);
            updateAccommodationRating(accommodationId);

            log.info("리뷰 삭제 완료 - reviewId: {}", reviewId);
        });
    }

    /**
     * 숙소의 평균 평점을 계산하고 업데이트
     * @param accommodationId 숙소 ID
     */
    private void updateAccommodationRating(Long accommodationId) {
        Double averageRating = reviewRepository.findAverageRatingByAccommodationId(accommodationId);
        if (averageRating != null) {
            BigDecimal rating = BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP);
            accommodationService.updateRatingWithRetry(accommodationId, rating, MAX_RETRIES);
        }
    }

    /**
     * 리뷰 상세 조회
     * @param reviewId 리뷰 ID
     * @return 리뷰 응답
     */
    public ReviewDto.Response getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        return ReviewDto.Response.from(review);
    }

    /**
     * 숙소별 리뷰 조회
     * @param accommodationId 숙소 ID
     * @param pageable 페이징 정보
     * @return 리뷰 페이지
     */
    public Page<ReviewDto.ListResponse> getReviewsByAccommodation(Long accommodationId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByAccommodationIdWithImages(accommodationId, pageable);
        return reviews.map(ReviewDto.ListResponse::from);
    }

    /**
     * 사용자별 리뷰 조회
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 리뷰 페이지
     */
    public Page<ReviewDto.ListResponse> getReviewsByUser(Long userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByUserIdWithImages(userId, pageable);
        return reviews.map(ReviewDto.ListResponse::from);
    }

    /**
     * 리뷰 검색
     * @param searchRequest 검색 조건
     * @return 리뷰 페이지
     */
    public Page<ReviewDto.ListResponse> searchReviews(ReviewDto.SearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize()
        );

        Page<Review> reviews = reviewRepository.searchReviews(searchRequest, pageable);
        return reviews.map(ReviewDto.ListResponse::from);
    }

    /**
     * 숙소 리뷰 통계 조회
     * @param accommodationId 숙소 ID
     * @return 리뷰 통계
     */
    public ReviewDto.StatisticsResponse getReviewStatistics(Long accommodationId) {
        // 총 리뷰 개수
        int totalReviews = reviewRepository.countByAccommodationId(accommodationId);

        // 평균 평점
        Double averageRating = reviewRepository.findAverageRatingByAccommodationId(accommodationId);

        // 평점별 개수
        List<Integer> ratingCounts = reviewRepository.getRatingStatistics(accommodationId);

        return ReviewDto.StatisticsResponse.builder()
                .totalReviews(totalReviews)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .ratingCounts(ratingCounts)
                .build();
    }

    /**
     * 숙소의 최신 리뷰 조회
     * @param accommodationId 숙소 ID
     * @param limit 조회할 개수
     * @return 최신 리뷰 목록
     */
    public List<ReviewDto.ListResponse> getRecentReviews(Long accommodationId, int limit) {
        List<Review> reviews = reviewRepository.findRecentReviewsByAccommodationId(accommodationId, limit);
        return reviews.stream()
                .map(ReviewDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 예약에 대한 리뷰 존재 여부 확인
     * @param reservationId 예약 ID
     * @return 리뷰 존재 여부
     */
    public boolean hasReviewForReservation(Long reservationId) {
        return reviewRepository.existsByReservationId(reservationId);
    }
} 