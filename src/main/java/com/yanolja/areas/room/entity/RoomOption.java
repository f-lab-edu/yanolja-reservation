package com.yanolja.areas.room.entity;

import com.yanolja.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Table(name = "room_options")
@Getter
@NoArgsConstructor
public class RoomOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("객실 옵션 ID")
    private Long id;

    @Column(nullable = false, length = 100)
    @Comment("옵션 이름")
    private String name;

    @Column(nullable = false)
    @Comment("옵션 가격")
    private BigDecimal price;

    /**
     * 객실 옵션 생성자
     * @param name 옵션 이름
     * @param price 옵션 가격
     */
    @Builder
    public RoomOption(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }
    
    /**
     * 객실 옵션 생성
     * @param name 옵션 이름
     * @param price 옵션 가격
     * @return 생성된 RoomOption 객체
     */
    public static RoomOption createRoomOption(String name, BigDecimal price) {
        return RoomOption.builder()
                .name(name)
                .price(price)
                .build();
    }

    /**
     * 객실 옵션 정보 업데이트
     * @param name 옵션 이름
     * @param price 옵션 가격
     */
    public void update(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }
} 