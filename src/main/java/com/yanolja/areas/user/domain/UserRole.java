package com.yanolja.areas.user.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserRole {
    @Schema(description = "유저 권한")
    USER("USER", "일반 사용자"),
    ADMIN("ADMIN", "관리자");

    private String code;
    private String label;

    public String getCode() {
        return this.code;
    }

    public String getLabel() {
        return this.label;
    }

} 