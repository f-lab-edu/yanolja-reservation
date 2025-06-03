package com.yanolja.areas.reservation.entity;

import com.yanolja.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;

@Entity
@Table(name = "reservation_options")
@Getter
@NoArgsConstructor
public class ReservationOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("예약 옵션 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    @Comment("예약")
    private Reservation reservation;

    @Column(name = "option_id", nullable = false)
    @Comment("객실 옵션 ID")
    private Long optionId;

    @Column(nullable = false)
    @Comment("수량")
    private Integer quantity;

    @Column(nullable = false)
    @Comment("가격")
    private BigDecimal price;

    /**
     * 예약 옵션 생성자
     */
    @Builder
    public ReservationOption(Reservation reservation, Long optionId, Integer quantity, BigDecimal price) {
        this.reservation = reservation;
        this.optionId = optionId;
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * 예약 옵션 생성
     */
    public static ReservationOption createReservationOption(Long optionId, Integer quantity, BigDecimal price) {
        return ReservationOption.builder()
                .optionId(optionId)
                .quantity(quantity)
                .price(price)
                .build();
    }

    /**
     * 예약 설정 (양방향 연관관계 설정)
     */
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    /**
     * 수량 변경
     */
    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * 가격 변경
     */
    public void updatePrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * 총 옵션 가격 계산
     */
    public BigDecimal getTotalPrice() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
} 