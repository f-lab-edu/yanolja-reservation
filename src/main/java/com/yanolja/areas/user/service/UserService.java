package com.yanolja.areas.user.service;

import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.dto.LoginRequest;
import com.yanolja.areas.user.dto.LogoutRequest;
import com.yanolja.areas.user.dto.RegisterRequest;
import com.yanolja.areas.user.dto.TokenResponse;

/**
 * 사용자 인증 및 관리를 위한 서비스 인터페이스
 */
public interface UserService {

    /**
     * 사용자 회원가입
     * @param request 회원가입 요청 정보
     * @return 등록된 사용자
     */
    User registerUser(RegisterRequest request);

    /**
     * 사용자 로그인 처리
     * @param request 로그인 요청 정보
     * @return 발급된 토큰 정보
     */
    TokenResponse login(LoginRequest request);

    
    /**
     * 사용자 로그아웃 처리
     * @param request 로그아웃 요청 정보
     * @return 로그아웃 성공 여부
     */
    boolean logout(LogoutRequest request);
} 