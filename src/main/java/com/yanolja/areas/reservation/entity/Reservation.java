package com.yanolja.areas.reservation.entity;

import com.yanolja.common.auditing.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("예약 ID")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private Long userId;

    @Column(name = "room_id", nullable = false)
    @Comment("객실 ID")
    private Long roomId;

    @Column(name = "check_in_date", nullable = false)
    @Comment("체크인 날짜")
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    @Comment("체크아웃 날짜")
    private LocalDate checkOutDate;

    @Column(name = "total_price", nullable = false)
    @Comment("총 가격")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("예약 상태")
    private ReservationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Comment("결제 상태")
    private PaymentStatus paymentStatus;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Comment("예약 옵션 목록")
    private List<ReservationOption> reservationOptions = new ArrayList<>();

    /**
     * 예약 생성자
     */
    @Builder
    public Reservation(Long userId, Long roomId, LocalDate checkInDate, LocalDate checkOutDate,
                      BigDecimal totalPrice, ReservationStatus status, PaymentStatus paymentStatus) {
        this.userId = userId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalPrice = totalPrice;
        this.status = status != null ? status : ReservationStatus.PENDING;
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.PENDING;
        this.reservationOptions = new ArrayList<>();
    }

    /**
     * 예약 생성
     */
    public static Reservation createReservation(Long userId, Long roomId, LocalDate checkInDate,
                                              LocalDate checkOutDate, BigDecimal totalPrice) {
        return Reservation.builder()
                .userId(userId)
                .roomId(roomId)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .totalPrice(totalPrice)
                .status(ReservationStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }

    /**
     * 예약 상태 변경
     */
    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }

    /**
     * 결제 상태 변경
     */
    public void updatePaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    /**
     * 예약 확정
     */
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
        this.paymentStatus = PaymentStatus.COMPLETED;
    }

    /**
     * 예약 취소
     */
    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
        this.paymentStatus = PaymentStatus.CANCELLED;
    }

    /**
     * 예약 완료
     */
    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }

    /**
     * 노쇼 처리
     */
    public void noShow() {
        this.status = ReservationStatus.NO_SHOW;
    }

    /**
     * 예약 옵션 추가
     */
    public void addOption(ReservationOption option) {
        this.reservationOptions.add(option);
        option.setReservation(this);
    }

    /**
     * 예약 옵션 제거
     */
    public void removeOption(ReservationOption option) {
        this.reservationOptions.remove(option);
        option.setReservation(null);
    }

    /**
     * 숙박 일수 계산
     */
    public int getNights() {
        return (int) checkInDate.until(checkOutDate).getDays();
    }

    /**
     * 예약 취소 가능 여부 확인
     */
    public boolean canCancel() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;
    }

    /**
     * 예약 변경 가능 여부 확인
     */
    public boolean canModify() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;
    }
} 