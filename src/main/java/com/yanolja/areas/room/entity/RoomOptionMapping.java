package com.yanolja.areas.room.entity;

import com.yanolja.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "room_option_mappings")
@Getter
@NoArgsConstructor
public class RoomOptionMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("객실-옵션 매핑 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @Comment("객실 ID")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    @Comment("옵션 ID")
    private RoomOption roomOption;

    /**
     * 객실-옵션 매핑 생성자
     * @param room 객실
     * @param roomOption 객실 옵션
     */
    @Builder
    public RoomOptionMapping(Room room, RoomOption roomOption) {
        this.room = room;
        this.roomOption = roomOption;
    }

    /**
     * 객실-옵션 매핑 생성
     * @param room 객실
     * @param roomOption 객실 옵션
     * @return 생성된 RoomOptionMapping 객체
     */
    public static RoomOptionMapping createMapping(Room room, RoomOption roomOption) {
        return RoomOptionMapping.builder()
                .room(room)
                .roomOption(roomOption)
                .build();
    }
} 