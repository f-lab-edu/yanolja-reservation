package com.yanolja.areas.reviews.repository;

import com.yanolja.areas.reviews.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {


    /**
     * 리뷰별 이미지 삭제
     * @param reviewId 리뷰 ID
     */
    @Modifying
    @Query("DELETE FROM ReviewImage ri WHERE ri.review.id = :reviewId")
    void deleteByReviewId(@Param("reviewId") Long reviewId);


} 