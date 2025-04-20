package com.yanolja.areas.user.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yanolja.common.enumcode.EntityEnumerable;
import com.yanolja.common.enumcode.EntityEnumerableConverter;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserRole implements EntityEnumerable {
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

    @jakarta.persistence.Converter
    public static class Converter extends EntityEnumerableConverter<UserRole> {
        public Converter() {
            super(UserRole.class);
        }
    }
} 