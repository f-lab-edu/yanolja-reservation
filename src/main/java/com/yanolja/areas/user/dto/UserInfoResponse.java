package com.yanolja.areas.user.dto;

import com.yanolja.areas.user.domain.SocialProvider;
import com.yanolja.areas.user.domain.User;
import com.yanolja.areas.user.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 응답")
public class UserInfoResponse {

    @Schema(description = "사용자 ID")
    private Long id;

    @Schema(description = "사용자 이름")
    private String name;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "전화번호")
    private String phone;

    @Schema(description = "사용자 역할")
    private UserRole role;

    @Schema(description = "소셜 로그인 제공자")
    private SocialProvider socialProvider;

    @Schema(description = "프로필 이미지 URL")
    private String profileImageUrl;

    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .socialProvider(user.getSocialProvider() != null ? user.getSocialProvider() : null)
                .profileImageUrl(user.getProfileImageUrl() != null ? user.getProfileImageUrl() : null)
                .build();
    }
} 