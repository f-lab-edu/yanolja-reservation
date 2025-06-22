package com.yanolja.areas.reviews.repository;

import com.yanolja.areas.reviews.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    /**
     * 리뷰 ID로 이미지 목록 조회 (생성일시 순으로 정렬) - JPA Query Method 사용
     */
    List<ReviewImage> findByReviewIdOrderByCreatedAtAsc(Long reviewId);

    /**
     * 리뷰 ID로 모든 이미지 삭제 - JPA Query Method 사용
     */
    void deleteByReviewId(Long reviewId);
} 