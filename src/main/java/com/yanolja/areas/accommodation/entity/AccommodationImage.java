package com.yanolja.areas.accommodation.entity;

import com.yanolja.common.auditing.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "accommodation_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccommodationImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("이미지 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @Column(name = "image_url", nullable = false)
    @Comment("이미지 URL")
    private String imageUrl;

    @Column(name = "is_main")
    @Comment("대표 이미지 여부")
    private Boolean isMain;

    @Builder
    public AccommodationImage(Accommodation accommodation, String imageUrl, Boolean isMain) {
        this.accommodation = accommodation;
        this.imageUrl = imageUrl;
        this.isMain = isMain != null ? isMain : false;
    }

    /**
     * 대표 이미지 설정
     * @param isMain 대표 이미지 여부
     */
    public void setAsMain(Boolean isMain) {
        this.isMain = isMain;
    }

    /**
     * 이미지 URL 업데이트
     * @param imageUrl 새 이미지 URL
     */
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 