package com.yanolja.areas.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshRequest {
    
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;
} 