package com.yanolja.areas.user.controller;

import com.yanolja.areas.auth.dto.RegisterRequest;
import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.domain.UserDetail;
import com.yanolja.areas.user.dto.UserInfoResponse;
import com.yanolja.areas.user.dto.UserUpdateRequest;
import com.yanolja.areas.user.dto.UserSearchCondition;
import com.yanolja.areas.user.service.UserService;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자", description = "사용자 정보 관리 API")
public class UserController {
    private final UserService userService;

    @Operation(summary = "사용자 회원가입", description = "requestDto[RegisterRequest], responseDto[User]", tags = {"인증"})
    @PostMapping("/register")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = userService.registerUser(registerRequest);
        return ApiResponse.success(user);
    }

    @Operation(summary = "현재 로그인한 사용자 정보 조회", description = "JWT 토큰 기반으로 현재 사용자 정보 조회")
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getCurrentUser(@AuthenticationPrincipal UserDetail userDetail) {
        UserInfoResponse user = userService.getUserById(userDetail.getId());
        return ApiResponse.success(user);
    }

    @Operation(summary = "사용자 단건 조회", description = "userId로 사용자 정보 조회")
    @GetMapping("/{id}")
    public ApiResponse<UserInfoResponse> getUser(@PathVariable Long id) {
        UserInfoResponse user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    @Operation(summary = "사용자 전체 조회", description = "전체 사용자 목록 조회")
    @GetMapping("")
    public ApiResponse<List<UserInfoResponse>> searchUsers(@ModelAttribute UserSearchCondition condition) {
        List<UserInfoResponse> users = userService.searchUsers(condition);
        return ApiResponse.success(users);
    }

    @Operation(summary = "사용자 정보 수정", description = "userId로 사용자 정보 수정")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ApiResponse.success();
    }

    @Operation(summary = "회원 탈퇴", description = "userId로 회원 탈퇴 처리")
    @PostMapping("/{id}/withdraw")
    public ApiResponse<Void> withdrawUser(@PathVariable Long id) {
        userService.withdrawUser(id);
        return ApiResponse.success();
    }

    @Operation(summary = "이메일 중복 확인", description = "이메일 중복 여부 확인")
    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = userService.checkEmailDuplicate(email);
        return ApiResponse.success(isDuplicate);
    }
} 