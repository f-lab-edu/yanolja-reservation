package com.yanolja.areas.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 옵션 사용 통계 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "옵션 사용 통계 응답")
public class OptionUsageStatisticsDto {
    
    @Schema(description = "옵션 ID", example = "1")
    private Long optionId;
    
    @Schema(description = "옵션 이름", example = "와이파이")
    private String optionName;
    
    @Schema(description = "사용 횟수", example = "25")
    private Long usageCount;
} 