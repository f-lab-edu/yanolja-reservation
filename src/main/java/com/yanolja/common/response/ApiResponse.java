package com.yanolja.common.response;

import com.yanolja.common.enumcode.MessageEnum;
import com.yanolja.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    // 코드 상수
    public static final class ResponseCode {
        public static final String SUCCESS = "S000";
        public static final String GENERAL_ERROR = "E000";
        public static final String PENDING = "P000";
    }

    private boolean success;
    private T data;
    private String code;
    private String message;

    // 성공 응답 생성 (데이터 있음)
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .code(ResponseCode.SUCCESS)
                .build();
    }

    // 성공 응답 생성 (데이터 있음, 메시지 포함)
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .code(ResponseCode.SUCCESS)
                .build();
    }
    
    // 성공 응답 생성 (데이터 있음, MessageEnum 사용)
    public static <T> ApiResponse<T> success(T data, MessageEnum messageEnum) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(messageEnum.getMessage())
                .code(ResponseCode.SUCCESS)
                .build();
    }

    // 성공 응답 생성 (데이터 없음, 메시지만)
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .code(ResponseCode.SUCCESS)
                .build();
    }
    
    // 성공 응답 생성 (데이터 없음, MessageEnum 사용)
    public static ApiResponse<Void> success(MessageEnum messageEnum) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(messageEnum.getMessage())
                .code(ResponseCode.SUCCESS)
                .build();
    }

    // 에러 응답 생성 (ErrorCode 사용)
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    // 에러 응답 생성 (커스텀 코드와 메시지)
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }

    // 에러 응답 생성 (메시지만)
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(ResponseCode.GENERAL_ERROR)
                .message(message)
                .build();
    }
    
    // 에러 응답 생성 (MessageEnum 사용)
    public static <T> ApiResponse<T> error(MessageEnum messageEnum) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(ResponseCode.GENERAL_ERROR)
                .message(messageEnum.getMessage())
                .build();
    }
    
    // 에러 응답 생성 (커스텀 코드와 MessageEnum 사용)
    public static <T> ApiResponse<T> error(String code, MessageEnum messageEnum) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(messageEnum.getMessage())
                .build();
    }
   
    // 커스텀 응답 생성 (모든 필드 직접 지정)
    public static <T> ApiResponse<T> custom(boolean success, String code, String message, T data) {
        return ApiResponse.<T>builder()
                .success(success)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }
    
    // 커스텀 응답 생성 (MessageEnum 사용)
    public static <T> ApiResponse<T> custom(boolean success, String code, MessageEnum messageEnum, T data) {
        return ApiResponse.<T>builder()
                .success(success)
                .code(code)
                .message(messageEnum.getMessage())
                .data(data)
                .build();
    }
} 