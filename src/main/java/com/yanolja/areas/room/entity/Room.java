package com.yanolja.areas.room.entity;

import com.yanolja.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@DynamicUpdate
@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor
@Where(clause = "deleted_yn = 0")
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("객실 ID")
    private Long id;

    @Column(name = "accommodation_id", nullable = false)
    @Comment("숙소 ID")
    private Long accommodationId;

    @Column(nullable = false)
    @Comment("객실 이름")
    private String name;

    @Column(columnDefinition = "TEXT")
    @Comment("객실 설명")
    private String description;

    @Column(nullable = false)
    @Comment("수용 인원")
    private Integer capacity;

    @Column(name = "price_per_night", nullable = false)
    @Comment("1박 가격")
    private BigDecimal pricePerNight;

    @Column(nullable = false)
    @Comment("상태 (AVAILABLE, UNAVAILABLE)")
    private String status;
    
    @Column(name = "deleted_yn", columnDefinition = "TINYINT(1) DEFAULT 0")
    @Comment("삭제 여부 (1: 삭제, 0: 미삭제)")
    private Boolean deletedYn;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("객실 옵션 매핑 목록")
    private List<RoomOptionMapping> roomOptionMappings = new ArrayList<>();

    /**
     * 객실 생성자
     * @param accommodationId 숙소 ID
     * @param name 객실 이름
     * @param description 객실 설명
     * @param capacity 수용 인원
     * @param pricePerNight 1박 가격
     * @param status 상태
     */
    @Builder
    public Room(Long accommodationId, String name, String description,
                Integer capacity, BigDecimal pricePerNight, String status) {
        this.accommodationId = accommodationId;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
        this.status = status;
        this.deletedYn = false;
        this.roomOptionMappings = new ArrayList<>();
    }
    
    /**
     * 객실 생성
     * @param accommodationId 숙소 ID
     * @param name 객실 이름
     * @param description 객실 설명
     * @param capacity 수용 인원
     * @param pricePerNight 1박 가격
     * @return 생성된 Room 객체
     */
    public static Room createRoom(Long accommodationId, String name, String description, 
                              Integer capacity, BigDecimal pricePerNight) {
        return Room.builder()
                .accommodationId(accommodationId)
                .name(name)
                .description(description)
                .capacity(capacity)
                .pricePerNight(pricePerNight)
                .status("AVAILABLE")
                .build();
    }

    /**
     * 객실 정보 업데이트
     * @param name 객실 이름
     * @param description 객실 설명
     * @param capacity 수용 인원
     * @param pricePerNight 1박 가격
     */
    public void update(String name, String description,
                      Integer capacity, BigDecimal pricePerNight) {
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
    }

    /**
     * 객실 옵션 추가
     * @param roomOption 추가할 객실 옵션
     */
    public void addRoomOption(RoomOption roomOption) {
        RoomOptionMapping mapping = RoomOptionMapping.createMapping(this, roomOption);
        this.roomOptionMappings.add(mapping);
    }

    /**
     * 객실 옵션 제거
     * @param roomOption 제거할 객실 옵션
     */
    public void removeRoomOption(RoomOption roomOption) {
        this.roomOptionMappings.removeIf(mapping -> 
                mapping.getRoomOption().getId().equals(roomOption.getId()));
    }

    /**
     * 모든 객실 옵션 제거
     */
    public void clearRoomOptions() {
        this.roomOptionMappings.clear();
    }

    /**
     * 특정 옵션이 이미 추가되어 있는지 확인
     * @param roomOptionId 확인할 옵션 ID
     * @return 존재하면 true, 없으면 false
     */
    public boolean hasRoomOption(Long roomOptionId) {
        return this.roomOptionMappings.stream()
                .anyMatch(mapping -> mapping.getRoomOption().getId().equals(roomOptionId));
    }
    
    /**
     * 소프트 삭제 처리
     */
    public void delete() {
        this.deletedYn = true;
    }
} 