package com.yanolja.areas.reviews.entity;

import com.yanolja.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "review_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("리뷰 이미지 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @Comment("리뷰")
    private Review review;

    @Column(name = "image_url", nullable = false, length = 500)
    @Comment("이미지 URL")
    private String imageUrl;

    @Builder
    public ReviewImage(Review review, String imageUrl) {
        this.review = review;
        this.imageUrl = imageUrl;
    }

    /**
     * 리뷰 이미지 생성
     * @param review 리뷰
     * @param imageUrl 이미지 URL
     * @return 생성된 ReviewImage 객체
     */
    public static ReviewImage createReviewImage(Review review, String imageUrl) {
        return ReviewImage.builder()
                .review(review)
                .imageUrl(imageUrl)
                .build();
    }

    /**
     * 리뷰 설정 (연관관계 편의 메서드)
     * @param review 리뷰
     */
    public void setReview(Review review) {
        this.review = review;
    }

    /**
     * 이미지 URL 업데이트
     * @param imageUrl 새로운 이미지 URL
     */
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 