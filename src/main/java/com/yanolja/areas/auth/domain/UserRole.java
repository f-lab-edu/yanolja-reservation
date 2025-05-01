package com.yanolja.areas.auth.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserRole {
    @Schema(description = "유저 권한")
    USER("일반 사용자"),
    ADMIN("관리자");

    private String label;
    
    public String getLabel() {
        return this.label;
    }
} 