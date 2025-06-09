package com.yanolja.areas.reservation.exception;

/**
 * 예약 관련 비즈니스 예외
 */
public class ReservationException extends RuntimeException {
    
    private final String errorCode;
    
    public ReservationException(String message) {
        super(message);
        this.errorCode = "RESERVATION_ERROR";
    }
    
    public ReservationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ReservationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "RESERVATION_ERROR";
    }
    
    public String getErrorCode() {
        return errorCode;
    }
} 