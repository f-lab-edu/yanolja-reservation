package com.yanolja.areas.accommodation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.yanolja.common.auditing.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "amenities")
@Getter
@NoArgsConstructor
public class Amenity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("편의시설 ID")
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    @Comment("편의시설 이름")
    private String name;
    
    @Column(name = "icon_url")
    @Comment("아이콘 URL")
    private String iconUrl;
    
    @Builder
    public Amenity(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }
    
    /**
     * 새로운 편의시설 생성
     * @param name 시설 이름
     * @param iconUrl 아이콘 URL
     * @return 생성된 Amenity 객체
     */
    public static Amenity createAmenity(String name, String iconUrl) {
        return Amenity.builder()
                .name(name)
                .iconUrl(iconUrl)
                .build();
    }
    
    /**
     * 편의시설 정보 업데이트
     * @param name 시설 이름
     * @param iconUrl 아이콘 URL
     */
    public void updateInfo(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }
} 