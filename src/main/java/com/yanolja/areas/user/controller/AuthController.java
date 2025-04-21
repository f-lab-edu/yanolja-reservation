package com.yanolja.areas.user.controller;

import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.dto.LoginRequest;
import com.yanolja.areas.user.dto.LogoutRequest;
import com.yanolja.areas.user.dto.RegisterRequest;
import com.yanolja.areas.user.dto.TokenRefreshRequest;
import com.yanolja.areas.user.dto.TokenResponse;
import com.yanolja.areas.user.service.UserService;
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

    private final UserService userService;

    @Operation(summary = "사용자 회원가입", description = "requestDto[RegisterRequest]", tags = {"인증"})
    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = userService.registerUser(registerRequest);
        return ApiResponse.success(user, "회원가입이 성공적으로 완료되었습니다.");
    }

    @Operation(summary = "사용자 로그인", description = "requestDto[LoginRequest]", tags = {"인증"})
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = userService.login(loginRequest);
        return ApiResponse.success(tokenResponse, "로그인이 성공적으로 완료되었습니다.");
    }
    
    @Operation(summary = "사용자 로그아웃", description = "requestDto[LogoutRequest]", tags = {"인증"})
    @PostMapping("/logout")
    public ApiResponse<Map<String, Object>> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        boolean result = userService.logout(logoutRequest);
        Map<String, Object> data = new HashMap<>();
        data.put("success", result);
        String message = result ? "로그아웃 되었습니다." : "로그아웃 처리 중 오류가 발생했습니다.";
        return ApiResponse.success(data, message);
    }
    
    @Operation(summary = "토큰 갱신", description = "requestDto[TokenRefreshRequest]", tags = {"인증"})
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest refreshRequest) {
        TokenResponse tokenResponse = userService.refreshToken(refreshRequest);
        return ApiResponse.success(tokenResponse, "토큰이 성공적으로 갱신되었습니다.");
    }
} 