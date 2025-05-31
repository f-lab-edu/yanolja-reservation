package com.yanolja.areas.room.controller;

import com.yanolja.areas.room.dto.RoomDto;
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
        return ApiResponse.success(response, "객실 등록이 성공적으로 완료되었습니다.");
    }

    @GetMapping
    @Operation(summary = "객실 목록 조회", description = "등록된 모든 객실의 목록을 조회합니다.")
    public ApiResponse<List<RoomDto.ListResponse>> getAllRooms() {
        List<RoomDto.ListResponse> responses = roomService.getAllRooms();
        return ApiResponse.success(responses, "객실 목록 조회 성공");
    }

    @GetMapping("/{id}")
    @Operation(summary = "객실 상세 조회", description = "특정 객실의 상세 정보를 조회합니다.")
    public ApiResponse<RoomDto.Response> getRoom(@PathVariable Long id) {
        RoomDto.Response response = roomService.getRoomById(id);
        return ApiResponse.success(response, "객실 조회 성공");
    }

    @GetMapping("/accommodation/{accommodationId}")
    @Operation(summary = "숙소별 객실 목록 조회", description = "특정 숙소의 모든 객실 목록을 조회합니다.")
    public ApiResponse<List<RoomDto.ListResponse>> getRoomsByAccommodation(
            @PathVariable Long accommodationId) {
        List<RoomDto.ListResponse> responses = roomService.getRoomsByAccommodationId(accommodationId);
        return ApiResponse.success(responses, "숙소별 객실 목록 조회 성공");
    }

    @PutMapping("/{id}")
    @Operation(summary = "객실 정보 수정", description = "특정 객실의 정보를 수정합니다.")
    public ApiResponse<RoomDto.Response> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomDto.Request request) {
        RoomDto.Response response = roomService.updateRoom(id, request);
        return ApiResponse.success(response, "객실 정보 수정 성공");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "객실 삭제", description = "특정 객실을 소프트 삭제 처리합니다.")
    public ApiResponse<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ApiResponse.success("객실 소프트 삭제 성공");
    }
} 