package com.yanolja.areas.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 방별 옵션 개수 통계 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "방별 옵션 개수 통계 응답")
public class RoomOptionCountStatisticsDto {
    
    @Schema(description = "방 ID", example = "1")
    private Long roomId;
    
    @Schema(description = "방 이름", example = "디럭스 더블룸")
    private String roomName;
    
    @Schema(description = "옵션 개수", example = "5")
    private Long optionCount;
} 