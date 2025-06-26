package com.yanolja.areas.reservation.dto;

import com.yanolja.areas.reservation.entity.PaymentStatus;
import com.yanolja.areas.reservation.entity.Reservation;
import com.yanolja.areas.reservation.entity.ReservationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationDto {

    /**
     * 예약 생성 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    public static class Request {
        @NotNull(message = "객실 ID는 필수입니다.")
        private Long roomId;

        @NotNull(message = "체크인 날짜는 필수입니다.")
        @Future(message = "체크인 날짜는 현재 날짜 이후여야 합니다.")
        private LocalDate checkInDate;

        @NotNull(message = "체크아웃 날짜는 필수입니다.")
        @Future(message = "체크아웃 날짜는 현재 날짜 이후여야 합니다.")
        private LocalDate checkOutDate;

        @NotNull(message = "총 가격은 필수입니다.")
        @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다.")
        private BigDecimal totalPrice;

        private List<ReservationOptionDto.Request> options;

        @Builder
        public Request(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, 
                      BigDecimal totalPrice, List<ReservationOptionDto.Request> options) {
            this.roomId = roomId;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.totalPrice = totalPrice;
            this.options = options;
        }
    }

    /**
     * 예약 상세 응답 DTO
     */
    @Getter
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private Long roomId;
        private String roomName;
        private String accommodationName;
        private String accommodationAddress;
        private String accommodationImage;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer nights;
        private BigDecimal totalPrice;
        private ReservationStatus status;
        private PaymentStatus paymentStatus;
        private Boolean canCancel;
        private Boolean canModify;
        private List<ReservationOptionDto.Response> options;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(Reservation reservation) {
            return Response.builder()
                    .id(reservation.getId())
                    .userId(reservation.getUserId())
                    .roomId(reservation.getRoomId())
                    .checkInDate(reservation.getCheckInDate())
                    .checkOutDate(reservation.getCheckOutDate())
                    .nights(reservation.getNights())
                    .totalPrice(reservation.getTotalPrice())
                    .status(reservation.getStatus())
                    .paymentStatus(reservation.getPaymentStatus())
                    .canCancel(reservation.canCancel())
                    .canModify(reservation.canModify())
                    .options(reservation.getReservationOptions().stream()
                            .map(ReservationOptionDto.Response::fromEntity)
                            .collect(Collectors.toList()))
                    .createdAt(reservation.getCreatedAt())
                    .updatedAt(reservation.getUpdatedAt())
                    .build();
        }

        public static Response fromEntityWithDetails(Reservation reservation, String roomName, 
                                                   String accommodationName, String accommodationAddress, 
                                                   String accommodationImage) {
            return Response.builder()
                    .id(reservation.getId())
                    .userId(reservation.getUserId())
                    .roomId(reservation.getRoomId())
                    .roomName(roomName)
                    .accommodationName(accommodationName)
                    .accommodationAddress(accommodationAddress)
                    .accommodationImage(accommodationImage)
                    .checkInDate(reservation.getCheckInDate())
                    .checkOutDate(reservation.getCheckOutDate())
                    .nights(reservation.getNights())
                    .totalPrice(reservation.getTotalPrice())
                    .status(reservation.getStatus())
                    .paymentStatus(reservation.getPaymentStatus())
                    .canCancel(reservation.canCancel())
                    .canModify(reservation.canModify())
                    .options(reservation.getReservationOptions().stream()
                            .map(ReservationOptionDto.Response::fromEntity)
                            .collect(Collectors.toList()))
                    .createdAt(reservation.getCreatedAt())
                    .updatedAt(reservation.getUpdatedAt())
                    .build();
        }
    }

    /**
     * 예약 목록 응답 DTO
     */
    @Getter
    @Builder
    public static class ListResponse {
        private Long id;
        private Long roomId;
        private Long accommodationId;
        private String roomName;
        private String accommodationName;
        private String accommodationImage;
        private LocalDate checkInDate;
        private LocalDate checkOutDate;
        private Integer nights;
        private BigDecimal totalPrice;
        private ReservationStatus status;
        private PaymentStatus paymentStatus;
        private Boolean canCancel;
        private Boolean canModify;
        private LocalDateTime createdAt;

        public static ListResponse fromEntity(Reservation reservation) {
            return ListResponse.builder()
                    .id(reservation.getId())
                    .roomId(reservation.getRoomId())
                    .accommodationId(null)
                    .checkInDate(reservation.getCheckInDate())
                    .checkOutDate(reservation.getCheckOutDate())
                    .nights(reservation.getNights())
                    .totalPrice(reservation.getTotalPrice())
                    .status(reservation.getStatus())
                    .paymentStatus(reservation.getPaymentStatus())
                    .canCancel(reservation.canCancel())
                    .canModify(reservation.canModify())
                    .createdAt(reservation.getCreatedAt())
                    .build();
        }

        public static ListResponse fromEntityWithDetails(Reservation reservation, String roomName, 
                                                       String accommodationName, String accommodationImage) {
            return ListResponse.builder()
                    .id(reservation.getId())
                    .roomId(reservation.getRoomId())
                    .accommodationId(null)
                    .roomName(roomName)
                    .accommodationName(accommodationName)
                    .accommodationImage(accommodationImage)
                    .checkInDate(reservation.getCheckInDate())
                    .checkOutDate(reservation.getCheckOutDate())
                    .nights(reservation.getNights())
                    .totalPrice(reservation.getTotalPrice())
                    .status(reservation.getStatus())
                    .paymentStatus(reservation.getPaymentStatus())
                    .canCancel(reservation.canCancel())
                    .canModify(reservation.canModify())
                    .createdAt(reservation.getCreatedAt())
                    .build();
        }
        
        public static ListResponse fromEntityWithDetailsAndAccommodationId(Reservation reservation, String roomName, 
                                                       String accommodationName, String accommodationImage, Long accommodationId) {
            return ListResponse.builder()
                    .id(reservation.getId())
                    .roomId(reservation.getRoomId())
                    .accommodationId(accommodationId)
                    .roomName(roomName)
                    .accommodationName(accommodationName)
                    .accommodationImage(accommodationImage)
                    .checkInDate(reservation.getCheckInDate())
                    .checkOutDate(reservation.getCheckOutDate())
                    .nights(reservation.getNights())
                    .totalPrice(reservation.getTotalPrice())
                    .status(reservation.getStatus())
                    .paymentStatus(reservation.getPaymentStatus())
                    .canCancel(reservation.canCancel())
                    .canModify(reservation.canModify())
                    .createdAt(reservation.getCreatedAt())
                    .build();
        }
    }

    /**
     * 예약 상태 변경 요청 DTO
     */
    @Getter
    @NoArgsConstructor
    public static class StatusUpdateRequest {
        @NotNull(message = "예약 상태는 필수입니다.")
        private ReservationStatus status;

        private String reason;

        @Builder
        public StatusUpdateRequest(ReservationStatus status, String reason) {
            this.status = status;
            this.reason = reason;
        }
    }

    /**
     * 예약 검색 조건 DTO
     */
    @Getter
    @NoArgsConstructor
    public static class SearchCondition {
        private Long userId;
        private ReservationStatus status;
        private List<ReservationStatus> statuses;
        private LocalDate checkInDateFrom;
        private LocalDate checkInDateTo;
        private LocalDate checkOutDateFrom;
        private LocalDate checkOutDateTo;

        @Builder
        public SearchCondition(Long userId, ReservationStatus status, List<ReservationStatus> statuses,
                             LocalDate checkInDateFrom, LocalDate checkInDateTo,
                             LocalDate checkOutDateFrom, LocalDate checkOutDateTo) {
            this.userId = userId;
            this.status = status;
            this.statuses = statuses;
            this.checkInDateFrom = checkInDateFrom;
            this.checkInDateTo = checkInDateTo;
            this.checkOutDateFrom = checkOutDateFrom;
            this.checkOutDateTo = checkOutDateTo;
        }
    }
} 