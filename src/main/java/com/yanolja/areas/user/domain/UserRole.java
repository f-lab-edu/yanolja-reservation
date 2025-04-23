package com.yanolja.areas.user.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserRole {
    @Schema(description = "유저 권한")
    USER("USER", "일반 사용자"),
    ADMIN("ADMIN", "관리자");

    private String type;
    private String name;

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.name;
    }

} 