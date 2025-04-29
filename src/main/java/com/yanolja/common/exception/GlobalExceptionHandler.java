package com.yanolja.common.exception;

import com.yanolja.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    @ResponseStatus(HttpStatus.OK) // HTTP 상태 코드는 항상 200으로 응답하고 ApiResponse로 실제 상태 전달
    @ResponseBody
    public ApiResponse<Void> handleUserException(UserException e) {
        log.error("사용자 예외: {}", e.getMessage(), e);
        ErrorCode errorCode = e.getErrorCode();
        return ApiResponse.error(errorCode);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        log.error("유효성 검증 예외: {}", e.getMessage(), e);
        return ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ApiResponse<Void> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("인증 실패: {}", ex.getMessage(), ex);
        return ApiResponse.error("A001", "이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ApiResponse<Void> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.error("사용자 없음: {}", ex.getMessage(), ex);
        return ApiResponse.error("A002", "등록되지 않은 사용자입니다.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("서버 오류: {}", ex.getMessage(), ex);
        return ApiResponse.error("S001", "서버 내부 오류가 발생했습니다.");
    }
} 