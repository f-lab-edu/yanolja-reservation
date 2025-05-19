package com.yanolja.areas.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "객실 옵션 정보")
public class RoomOptionDto {
    @Schema(description = "옵션 ID", example = "1")
    private Long id;
    
    @Schema(description = "옵션 이름", example = "조식 서비스")
    private String name;
    
    @Schema(description = "옵션 가격", example = "25000")
    private BigDecimal price;
} 