package com.yanolja.areas.user.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserRole {
    @Schema(description = "유저 권한")
    USER("일반 사용자"),
    ADMIN("관리자");

    private String label;
    
    public String getLabel() {
        return this.label;
    }
} 