package com.yanolja.areas.accommodation.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AccommodationStatus {
    @Schema(description = "활성화 상태")
    ACTIVE("활성화"),
    INACTIVE("비활성화");

    private String label;
    
    public String getLabel() {
        return this.label;
    }
} 