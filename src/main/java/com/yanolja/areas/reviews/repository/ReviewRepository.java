package com.yanolja.areas.reviews.repository;

import com.yanolja.areas.reviews.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

    /**
     * 숙소별 리뷰 조회 (페이징)
     * @param accommodationId 숙소 ID
     * @param pageable 페이징 정보
     * @return 리뷰 페이지
     */
    Page<Review> findByAccommodationIdOrderByCreatedAtDesc(Long accommodationId, Pageable pageable);

    /**
     * 사용자별 리뷰 조회 (페이징)
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 리뷰 페이지
     */
    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 예약 ID로 리뷰 조회
     * @param reservationId 예약 ID
     * @return 리뷰 (Optional)
     */
    Optional<Review> findByReservationId(Long reservationId);

    /**
     * 사용자가 특정 숙소에 작성한 리뷰 조회
     * @param userId 사용자 ID
     * @param accommodationId 숙소 ID
     * @return 리뷰 목록
     */
    List<Review> findByUserIdAndAccommodationId(Long userId, Long accommodationId);

    /**
     * 숙소의 평균 평점 조회
     * @param accommodationId 숙소 ID
     * @return 평균 평점
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.accommodationId = :accommodationId")
    Double findAverageRatingByAccommodationId(@Param("accommodationId") Long accommodationId);

    /**
     * 숙소의 리뷰 개수 조회
     * @param accommodationId 숙소 ID
     * @return 리뷰 개수
     */
    int countByAccommodationId(Long accommodationId);

    /**
     * 특정 평점 이상의 리뷰 조회
     * @param accommodationId 숙소 ID
     * @param minRating 최소 평점
     * @param pageable 페이징 정보
     * @return 리뷰 페이지
     */
    Page<Review> findByAccommodationIdAndRatingGreaterThanEqualOrderByCreatedAtDesc(
            Long accommodationId, Integer minRating, Pageable pageable);

    /**
     * 예약 ID 존재 여부 확인
     * @param reservationId 예약 ID
     * @return 존재 여부
     */
    boolean existsByReservationId(Long reservationId);
} 