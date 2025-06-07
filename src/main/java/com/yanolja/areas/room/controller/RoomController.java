package com.yanolja.areas.room.controller;

import com.yanolja.areas.room.dto.OptionUsageStatisticsDto;
import com.yanolja.areas.room.dto.RoomDto;
import com.yanolja.areas.room.dto.RoomOptionCountStatisticsDto;
import com.yanolja.areas.room.dto.RoomOptionDto;
import com.yanolja.areas.room.service.RoomService;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "객실 관리", description = "객실 관리 API")
public class RoomController {

    private final RoomService roomService;

    @PostMapping
    @Operation(summary = "객실 등록", description = "새로운 객실을 등록합니다.")
    public ApiResponse<RoomDto.Response> createRoom(
            @Valid @RequestBody RoomDto.Request request) {
        RoomDto.Response response = roomService.createRoom(request);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "객실 목록 조회", description = "등록된 모든 객실의 목록을 조회합니다.")
    public ApiResponse<List<RoomDto.ListResponse>> getAllRooms() {
        List<RoomDto.ListResponse> responses = roomService.getAllRooms();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "객실 상세 조회", description = "특정 객실의 상세 정보를 조회합니다.")
    public ApiResponse<RoomDto.Response> getRoom(@PathVariable Long id) {
        RoomDto.Response response = roomService.getRoomById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/accommodation/{accommodationId}")
    @Operation(summary = "숙소별 객실 목록 조회", description = "특정 숙소의 모든 객실 목록을 조회합니다.")
    public ApiResponse<List<RoomDto.ListResponse>> getRoomsByAccommodation(
            @PathVariable Long accommodationId) {
        List<RoomDto.ListResponse> responses = roomService.getRoomsByAccommodationId(accommodationId);
        return ApiResponse.success(responses);
    }

    @PutMapping("/{id}")
    @Operation(summary = "객실 정보 수정", description = "특정 객실의 정보를 수정합니다.")
    public ApiResponse<RoomDto.Response> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomDto.Request request) {
        RoomDto.Response response = roomService.updateRoom(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "객실 삭제", description = "특정 객실을 소프트 삭제 처리합니다.")
    public ApiResponse<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ApiResponse.success();
    }

    // 객실 옵션 관리 API

    @GetMapping("/{roomId}/options")
    @Operation(summary = "객실 옵션 목록 조회", description = "특정 객실에 연결된 모든 옵션을 조회합니다.")
    public ApiResponse<List<RoomOptionDto.ListResponse>> getRoomOptions(@PathVariable Long roomId) {
        List<RoomOptionDto.ListResponse> responses = roomService.getRoomOptions(roomId);
        return ApiResponse.success(responses);
    }

    @PostMapping("/{roomId}/options")
    @Operation(summary = "객실 옵션 추가", description = "특정 객실에 옵션들을 추가합니다.")
    public ApiResponse<Void> addRoomOptions(
            @PathVariable Long roomId,
            @RequestBody List<Long> optionIds) {
        roomService.addRoomOptions(roomId, optionIds);
        return ApiResponse.success();
    }

    @DeleteMapping("/{roomId}/options/{optionId}")
    @Operation(summary = "객실 옵션 제거", description = "특정 객실에서 특정 옵션을 제거합니다.")
    public ApiResponse<Void> removeRoomOption(
            @PathVariable Long roomId,
            @PathVariable Long optionId) {
        roomService.removeRoomOption(roomId, optionId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{roomId}/options")
    @Operation(summary = "객실 모든 옵션 제거", description = "특정 객실의 모든 옵션을 제거합니다.")
    public ApiResponse<Void> removeAllRoomOptions(@PathVariable Long roomId) {
        roomService.removeAllRoomOptions(roomId);
        return ApiResponse.success();
    }

    @GetMapping("/by-option/{optionId}")
    @Operation(summary = "옵션별 객실 목록 조회", description = "특정 옵션을 사용하는 모든 객실을 조회합니다.")
    public ApiResponse<List<RoomDto.ListResponse>> getRoomsByOption(@PathVariable Long optionId) {
        List<RoomDto.ListResponse> responses = roomService.getRoomsByOptionId(optionId);
        return ApiResponse.success(responses);
    }

    @PostMapping("/by-all-options")
    @Operation(summary = "모든 옵션을 가진 객실 조회", description = "지정된 모든 옵션을 가진 객실들을 조회합니다.")
    public ApiResponse<List<RoomDto.ListResponse>> getRoomsWithAllOptions(
            @RequestBody List<Long> optionIds) {
        List<RoomDto.ListResponse> responses = roomService.getRoomsWithAllOptions(optionIds);
        return ApiResponse.success(responses);
    }

    @PostMapping("/by-any-options")
    @Operation(summary = "옵션 중 하나 이상을 가진 객실 조회", description = "지정된 옵션 중 하나 이상을 가진 객실들을 조회합니다.")
    public ApiResponse<List<RoomDto.ListResponse>> getRoomsWithAnyOptions(
            @RequestBody List<Long> optionIds) {
        List<RoomDto.ListResponse> responses = roomService.getRoomsWithAnyOptions(optionIds);
        return ApiResponse.success(responses);
    }

    @GetMapping("/accommodation/{accommodationId}/with-options")
    @Operation(summary = "숙소별 옵션 매핑된 객실 목록 조회", description = "특정 숙소의 옵션이 매핑된 객실 목록을 조회합니다.")
    public ApiResponse<List<RoomDto.ListResponse>> getRoomsByAccommodationWithOptions(
            @PathVariable Long accommodationId) {
        List<RoomDto.ListResponse> responses = roomService.getRoomsByAccommodationIdWithOptions(accommodationId);
        return ApiResponse.success(responses);
    }

    @GetMapping("/statistics/option-usage")
    @Operation(summary = "옵션별 사용 통계 조회", description = "각 옵션이 몇 개의 객실에서 사용되고 있는지 통계를 조회합니다.")
    public ApiResponse<List<OptionUsageStatisticsDto>> getOptionUsageStatistics() {
        List<OptionUsageStatisticsDto> statistics = roomService.getOptionUsageStatistics();
        return ApiResponse.success(statistics);
    }

    @GetMapping("/statistics/room-option-count")
    @Operation(summary = "객실별 옵션 개수 통계 조회", description = "각 객실이 몇 개의 옵션을 가지고 있는지 통계를 조회합니다.")
    public ApiResponse<List<RoomOptionCountStatisticsDto>> getRoomOptionCountStatistics() {
        List<RoomOptionCountStatisticsDto> statistics = roomService.getRoomOptionCountStatistics();
        return ApiResponse.success(statistics);
    }
} 