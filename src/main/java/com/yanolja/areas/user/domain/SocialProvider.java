package com.yanolja.areas.user.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SocialProvider {
    @Schema(description = "구글 로그인")
    GOOGLE("구글"),
    
    @Schema(description = "카카오 로그인")
    KAKAO("카카오");

    private String label;


    public String getLabel() {
        return this.label;
    }
} 