package com.yanolja.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버 오류가 발생했습니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청한 리소스를 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C004", "접근 권한이 없습니다."),
    
    // Authentication
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증되지 않은 요청입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
    
    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 등록된 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U003", "잘못된 비밀번호입니다."),
    
    // OAuth2
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "O001", "지원하지 않는 소셜 로그인 제공자입니다."),
    MISSING_EMAIL(HttpStatus.BAD_REQUEST, "O002", "소셜 계정의 이메일 정보를 가져올 수 없습니다."),
    ACCOUNT_ALREADY_LINKED(HttpStatus.CONFLICT, "O003", "이미 다른 소셜 계정과 연결된 이메일입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
} 