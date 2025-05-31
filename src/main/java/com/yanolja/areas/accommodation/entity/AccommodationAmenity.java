package com.yanolja.areas.accommodation.entity;

import com.yanolja.common.auditing.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "accommodation_amenities")
@Getter
@NoArgsConstructor
public class AccommodationAmenity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("숙소 편의시설 매핑 ID")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "accommodation_id")
    @Comment("숙소 ID")
    private Accommodation accommodation;
    
    @ManyToOne
    @JoinColumn(name = "amenity_id")
    @Comment("편의시설 ID")
    private Amenity amenity;
    
    @Builder
    public AccommodationAmenity(Accommodation accommodation, Amenity amenity) {
        this.accommodation = accommodation;
        this.amenity = amenity;
    }
    
    /**
     * 숙소-편의시설 매핑 생성
     * @param accommodation 숙소
     * @param amenity 편의시설
     * @return 생성된 AccommodationAmenity 객체
     */
    public static AccommodationAmenity createAccommodationAmenity(Accommodation accommodation, Amenity amenity) {
        return AccommodationAmenity.builder()
                .accommodation(accommodation)
                .amenity(amenity)
                .build();
    }
} 