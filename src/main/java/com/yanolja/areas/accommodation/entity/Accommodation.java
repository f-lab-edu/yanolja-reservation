package com.yanolja.areas.accommodation.entity;

import com.yanolja.common.auditing.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import org.hibernate.annotations.Comment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accommodations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Accommodation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("숙소 ID")
    private Long id;

    @Column(nullable = false)
    @Comment("숙소 이름")
    private String name;

    @Column(columnDefinition = "TEXT")
    @Comment("숙소 설명")
    private String description;

    @Column(nullable = false)
    @Comment("숙소 주소")
    private String address;

    @Comment("위도")
    private BigDecimal latitude;
    
    @Comment("경도")
    private BigDecimal longitude;

    @Column(nullable = false)
    @Comment("1박 가격")
    private BigDecimal pricePerNight;

    @Comment("평점")
    private BigDecimal rating;

    @Column(name = "review_count")
    @Comment("리뷰 수")
    private Integer reviewCount;

    @Column(name = "status")
    @Comment("상태 (ACTIVE, INACTIVE)")
    private String status;

    @Column(name = "deleted_yn")
    @Comment("삭제 여부 (Y, N)")
    private String deletedYn;

    @OneToMany(mappedBy = "accommodation")
    private List<AccommodationImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "accommodation")
    private List<Room> rooms = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "accommodation_amenities",
        joinColumns = @JoinColumn(name = "accommodation_id"),
        inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private List<Amenity> amenities = new ArrayList<>();
    
    /**
     * 숙소 생성
     * @param name 숙소 이름
     * @param description 숙소 설명
     * @param address 숙소 주소
     * @param latitude 위도
     * @param longitude 경도
     * @param pricePerNight 1박 가격
     * @return 생성된 Accommodation 객체
     */
    public static Accommodation createAccommodation(String name, String description, String address, 
                                             BigDecimal latitude, BigDecimal longitude, BigDecimal pricePerNight) {
        return Accommodation.builder()
                .name(name)
                .description(description)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .pricePerNight(pricePerNight)
                .status("ACTIVE")
                .reviewCount(0)
                .build();
    }
    
    /**
     * 이미지 추가
     * @param images 추가할 이미지 리스트
     */
    public void addImages(List<AccommodationImage> images) {
        this.images.addAll(images);
        images.forEach(image -> image.setAccommodation(this));
    }
    
    /**
     * 객실 추가
     * @param rooms 추가할 객실 리스트
     */
    public void addRooms(List<Room> rooms) {
        this.rooms.addAll(rooms);
        rooms.forEach(room -> room.setAccommodation(this));
    }
    
    /**
     * 편의시설 설정
     * @param amenities 설정할 편의시설 리스트
     */
    public void setAmenitiesList(List<Amenity> amenities) {
        this.amenities.clear();
        this.amenities.addAll(amenities);
    }
    
    /**
     * 숙소 정보 업데이트
     * @param name 숙소 이름
     * @param description 숙소 설명
     * @param address 숙소 주소
     * @param latitude 위도
     * @param longitude 경도
     * @param pricePerNight 1박 가격
     */
    public void updateInfo(String name, String description, String address, 
                           BigDecimal latitude, BigDecimal longitude, BigDecimal pricePerNight) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pricePerNight = pricePerNight;
    }
    
    /**
     * 상태 변경
     * @param status 변경할 상태
     */
    public void changeStatus(String status) {
        this.status = status;
    }

    /**
     * 평점 업데이트
     * @param rating 평점
     */
    public void updateRating(BigDecimal rating) {
        this.rating = rating;
    }

    /**
     * 리뷰 수 증가
     */
    public void incrementReviewCount() {
        this.reviewCount = (this.reviewCount != null ? this.reviewCount : 0) + 1;
    }

    /**
     * 리뷰 수 감소
     */
    public void decrementReviewCount() {
        if (this.reviewCount != null && this.reviewCount > 0) {
            this.reviewCount -= 1;
        }
    }
} 