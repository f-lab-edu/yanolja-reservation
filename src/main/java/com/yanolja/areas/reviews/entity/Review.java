package com.yanolja.areas.reviews.entity;

import com.yanolja.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("리뷰 ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private Long userId;

    @Column(name = "accommodation_id", nullable = false)
    @Comment("숙소 ID")
    private Long accommodationId;

    @Column(name = "reservation_id", nullable = false)
    @Comment("예약 ID")
    private Long reservationId;

    @Column(name = "rating", nullable = false)
    @Comment("평점 (1-5)")
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    @Comment("리뷰 내용")
    private String comment;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("리뷰 이미지 목록")
    private List<ReviewImage> reviewImages = new ArrayList<>();

    @Builder
    private Review(Long userId, Long accommodationId, Long reservationId, 
                  Integer rating, String comment) {
        this.userId = userId;
        this.accommodationId = accommodationId;
        this.reservationId = reservationId;
        this.rating = rating;
        this.comment = comment;
    }

    /**
     * 리뷰 생성
     * @param userId 사용자 ID
     * @param accommodationId 숙소 ID
     * @param reservationId 예약 ID
     * @param rating 평점
     * @param comment 리뷰 내용
     * @return 생성된 Review 객체
     */
    public static Review createReview(Long userId, Long accommodationId, Long reservationId,
                                    Integer rating, String comment) {
        return Review.builder()
                .userId(userId)
                .accommodationId(accommodationId)
                .reservationId(reservationId)
                .rating(rating)
                .comment(comment)
                .build();
    }

    /**
     * 리뷰 수정
     * @param rating 평점
     * @param comment 리뷰 내용
     */
    public void updateReview(Integer rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    /**
     * 리뷰 이미지 추가
     * @param reviewImage 리뷰 이미지
     */
    public void addReviewImage(ReviewImage reviewImage) {
        this.reviewImages.add(reviewImage);
        reviewImage.setReview(this);
    }

    /**
     * 리뷰 이미지 제거
     * @param reviewImage 리뷰 이미지
     */
    public void removeReviewImage(ReviewImage reviewImage) {
        this.reviewImages.remove(reviewImage);
        reviewImage.setReview(null);
    }

    /**
     * 평점 유효성 검증
     * @param rating 평점
     * @return 유효한 평점인지 여부
     */
    public static boolean isValidRating(Integer rating) {
        return rating != null && rating >= 1 && rating <= 5;
    }
} 