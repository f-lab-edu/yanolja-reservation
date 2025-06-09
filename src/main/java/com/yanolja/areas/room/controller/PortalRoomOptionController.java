package com.yanolja.areas.room.controller;

import com.yanolja.areas.room.dto.RoomOptionDto;
import com.yanolja.areas.room.service.RoomOptionService;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portal/room-options")
@RequiredArgsConstructor
@Tag(name = "포털 - 객실 옵션", description = "포털용 객실 옵션 조회 API")
public class PortalRoomOptionController {

    private final RoomOptionService roomOptionService;

    @GetMapping
    @Operation(summary = "객실 옵션 목록 조회", description = "등록된 모든 객실 옵션의 목록을 조회합니다.")
    public ApiResponse<List<RoomOptionDto.ListResponse>> getAllRoomOptions() {
        List<RoomOptionDto.ListResponse> responses = roomOptionService.getAllRoomOptions();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "객실 옵션 상세 조회", description = "특정 객실 옵션의 상세 정보를 조회합니다.")
    public ApiResponse<RoomOptionDto.Response> getRoomOption(@PathVariable Long id) {
        RoomOptionDto.Response response = roomOptionService.getRoomOptionById(id);
        return ApiResponse.success(response);
    }

    @PostMapping("/by-ids")
    @Operation(summary = "ID 목록으로 객실 옵션 조회", description = "특정 ID 목록에 해당하는 객실 옵션들을 조회합니다.")
    public ApiResponse<List<RoomOptionDto.ListResponse>> getRoomOptionsByIds(
            @RequestBody List<Long> ids) {
        List<RoomOptionDto.ListResponse> responses = roomOptionService.getRoomOptionsByIds(ids);
        return ApiResponse.success(responses);
    }
} 