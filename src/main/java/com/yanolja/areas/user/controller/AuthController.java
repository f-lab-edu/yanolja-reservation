package com.yanolja.areas.user.controller;

import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.dto.LoginRequest;
import com.yanolja.areas.user.dto.LogoutRequest;
import com.yanolja.areas.user.dto.RegisterRequest;
import com.yanolja.areas.user.dto.TokenRefreshRequest;
import com.yanolja.areas.user.dto.TokenResponse;
import com.yanolja.areas.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "사용자 회원가입", description = "requestDto[RegisterRequest] responseDto[User]", tags = {"인증"})
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = User.class)))
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = userService.registerUser(registerRequest);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "사용자 로그인", description = "requestDto[LoginRequest] responseDto[TokenResponse]", tags = {"인증"})
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TokenResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        TokenResponse tokenResponse = userService.login(loginRequest);
        return ResponseEntity.ok(tokenResponse);
    }
    
    @Operation(summary = "사용자 로그아웃", description = "requestDto[LogoutRequest] responseDto[Map]", tags = {"인증"})
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Map.class)))
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        boolean result = userService.logout(logoutRequest);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", result ? "로그아웃 되었습니다." : "로그아웃 처리 중 오류가 발생했습니다.");
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "토큰 갱신", description = "requestDto[TokenRefreshRequest] responseDto[TokenResponse]", tags = {"인증"})
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = TokenResponse.class)))
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest refreshRequest) {
        TokenResponse tokenResponse = userService.refreshToken(refreshRequest);
        return ResponseEntity.ok(tokenResponse);
    }
} 