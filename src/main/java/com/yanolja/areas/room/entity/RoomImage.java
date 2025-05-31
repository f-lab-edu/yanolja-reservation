package com.yanolja.areas.room.entity;

import com.yanolja.common.auditing.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "room_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("이미지 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "image_url", nullable = false)
    @Comment("이미지 URL")
    private String imageUrl;

    @Column(name = "is_main")
    @Comment("대표 이미지 여부")
    private Boolean isMain;

    @Builder
    public RoomImage(Room room, String imageUrl, Boolean isMain) {
        this.room = room;
        this.imageUrl = imageUrl;
        this.isMain = isMain != null ? isMain : false;
    }

    /**
     * 이미지를 대표 이미지로 설정
     * 호출 시 이 메소드는 다른 모든 해당 객실의 이미지의 isMain 값을 false로 설정할 수 있도록
     * 서비스 레이어에서 다른 이미지들을 모두 처리해야 합니다.
     */
    public void makeMainImage() {
        this.isMain = true;
    }
    
    /**
     * 대표 이미지 해제
     */
    public void unsetAsMain() {
        this.isMain = false;
    }

    /**
     * 이미지 URL 업데이트
     * @param imageUrl 새 이미지 URL
     */
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 