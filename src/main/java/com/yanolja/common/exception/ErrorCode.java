package com.yanolja.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // User 관련 에러
    USER_NOT_FOUND(404, "U001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(400, "U002", "이미 존재하는 사용자입니다."),
    INVALID_PASSWORD(400, "U003", "비밀번호가 일치하지 않습니다."),
    INVALID_USERNAME(400, "U004", "유효하지 않은 사용자명입니다."),
    INVALID_EMAIL(400, "U005", "유효하지 않은 이메일입니다."),
    
    // Auth 관련 에러
    INVALID_TOKEN(401, "A001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "A002", "만료된 토큰입니다."),
    UNAUTHORIZED(401, "A003", "인증이 필요합니다."),
    FORBIDDEN(403, "A004", "접근 권한이 없습니다."),

    // Validation 관련 에러
    INVALID_INPUT(400, "V001", "입력값이 유효하지 않습니다."),

    // Server 관련 에러
    INTERNAL_SERVER_ERROR(500, "S001", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
} 