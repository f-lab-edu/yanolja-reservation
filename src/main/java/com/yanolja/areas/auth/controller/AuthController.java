package com.yanolja.areas.auth.controller;

import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.auth.dto.LoginRequest;
import com.yanolja.areas.auth.dto.LogoutRequest;
import com.yanolja.areas.auth.dto.RegisterRequest;
import com.yanolja.areas.auth.dto.TokenRefreshRequest;
import com.yanolja.areas.auth.dto.TokenResponse;
import com.yanolja.areas.auth.service.AuthService;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "사용자 회원가입", description = "requestDto[RegisterRequest], responseDto[User]", tags = {"인증"})
    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = authService.registerUser(registerRequest);
        return ApiResponse.success(user, "회원가입이 성공적으로 완료되었습니다.");
    }

    @Operation(summary = "사용자 로그인", description = "requestDto[LoginRequest], responseDto[TokenResponse]", tags = {"인증"})
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = authService.login(loginRequest);
        return ApiResponse.success(tokenResponse, "로그인이 성공적으로 완료되었습니다.");
    }
    
    @Operation(summary = "사용자 로그아웃", description = "requestDto[LogoutRequest], responseDto[Map<String, Object>]", tags = {"인증"})
    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        boolean result = authService.logout(logoutRequest);
        Map<String, Object> data = new HashMap<>();
        data.put("success", result);
        String message = result ? "로그아웃 되었습니다." : "로그아웃 처리 중 오류가 발생했습니다.";
        return ApiResponse.success(data, message);
    }
    
    @Operation(summary = "토큰 갱신", description = "requestDto[TokenRefreshRequest], responseDto[TokenResponse]", tags = {"인증"})
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest refreshRequest) {
        TokenResponse tokenResponse = authService.refreshToken(refreshRequest);
        return ApiResponse.success(tokenResponse, "토큰이 성공적으로 갱신되었습니다.");
    }
} 