package com.yanolja.areas.room.controller;

import com.yanolja.areas.room.dto.RoomOptionDto;
import com.yanolja.areas.room.service.RoomOptionService;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin/room-options")
@RequiredArgsConstructor
@Tag(name = "관리자 - 객실 옵션 관리", description = "관리자용 객실 옵션 관리 API")
public class RoomOptionController {

    private final RoomOptionService roomOptionService;

    @PostMapping
    @Operation(summary = "객실 옵션 등록", description = "새로운 객실 옵션을 등록합니다.")
    public ApiResponse<RoomOptionDto.Response> createRoomOption(
            @Valid @RequestBody RoomOptionDto.Request request) {
        RoomOptionDto.Response response = roomOptionService.createRoomOption(request);
        return ApiResponse.success(response);
    }

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

    @PutMapping("/{id}")
    @Operation(summary = "객실 옵션 정보 수정", description = "특정 객실 옵션의 정보를 수정합니다.")
    public ApiResponse<RoomOptionDto.Response> updateRoomOption(
            @PathVariable Long id,
            @Valid @RequestBody RoomOptionDto.Request request) {
        RoomOptionDto.Response response = roomOptionService.updateRoomOption(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "객실 옵션 삭제", description = "특정 객실 옵션을 삭제합니다.")
    public ApiResponse<Void> deleteRoomOption(@PathVariable Long id) {
        roomOptionService.deleteRoomOption(id);
        return ApiResponse.success();
    }
} 