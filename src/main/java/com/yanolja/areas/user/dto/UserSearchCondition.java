package com.yanolja.areas.user.dto;

import com.yanolja.areas.user.domain.UserRole;
import com.yanolja.areas.user.domain.SocialProvider;
import lombok.*;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchCondition {
    @Schema(description = "이름")
    private String name;
    
    @Schema(description = "이메일")
    private String email;
    
    @Schema(description = "전화번호")
    private String phone;
    
    @Schema(description = "가입일자 시작일")
    private LocalDateTime createdAtFrom;
    
    @Schema(description = "가입일자 종료일")
    private LocalDateTime createdAtTo;

    @Schema(description = "회원 유형")
    private UserRole role;
    
    @Schema(description = "소셜 로그인 유형")
    private SocialProvider socialProvider;
}