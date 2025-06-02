package com.yanolja.areas.room.controller;

import com.yanolja.areas.room.dto.PortalRoomDto;
import com.yanolja.areas.room.dto.RoomOptionDto;
import com.yanolja.areas.room.service.PortalRoomService;
import com.yanolja.areas.room.service.RoomService;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portal/rooms")
@RequiredArgsConstructor
@Tag(name = "객실 포털 API", description = "일반 사용자용 객실 검색 및 조회 API")
public class PortalRoomController {

    private final PortalRoomService portalRoomService;
    private final RoomService roomService;

    @PostMapping("/search")
    @Operation(summary = "객실 검색", description = "조건에 맞는 객실을 검색합니다. (이름, 가격 범위, 수용 인원 등)")
    public ApiResponse<Page<PortalRoomDto.ListResponse>> searchRooms(
            @RequestBody PortalRoomDto.SearchRequest searchRequest) {
        
        Page<PortalRoomDto.ListResponse> responses = 
                portalRoomService.searchRooms(searchRequest);
                
        return ApiResponse.success(responses, "객실 검색 결과입니다.");
    }

    @GetMapping("/{id}")
    @Operation(summary = "객실 상세 조회", description = "객실의 상세 정보를 조회합니다.")
    public ApiResponse<PortalRoomDto.DetailResponse> getRoomDetail(@PathVariable Long id) {
        PortalRoomDto.DetailResponse response = portalRoomService.getRoomDetail(id);
        return ApiResponse.success(response, "객실 상세 정보입니다.");
    }
    
    @GetMapping("/accommodation/{accommodationId}")
    @Operation(summary = "숙소별 객실 목록 조회", description = "특정 숙소의 모든 객실 목록을 조회합니다.")
    public ApiResponse<List<PortalRoomDto.ListResponse>> getRoomsByAccommodation(
            @PathVariable Long accommodationId) {
        List<PortalRoomDto.ListResponse> responses = portalRoomService.getRoomsByAccommodation(accommodationId);
        return ApiResponse.success(responses, "숙소별 객실 목록 조회 성공");
    }

    @GetMapping("/{roomId}/options")
    @Operation(summary = "객실 옵션 목록 조회 (포털용)", description = "특정 객실에 연결된 모든 옵션을 조회합니다.")
    public ApiResponse<List<RoomOptionDto.ListResponse>> getRoomOptions(@PathVariable Long roomId) {
        List<RoomOptionDto.ListResponse> responses = roomService.getRoomOptions(roomId);
        return ApiResponse.success(responses, "객실 옵션 목록 조회 성공");
    }

    @GetMapping("/by-option/{optionId}")
    @Operation(summary = "옵션별 객실 목록 조회 (포털용)", description = "특정 옵션을 사용하는 모든 객실을 조회합니다.")
    public ApiResponse<List<PortalRoomDto.ListResponse>> getRoomsByOption(@PathVariable Long optionId) {
        
        List<PortalRoomDto.ListResponse> responses = portalRoomService.getRoomsByOptionId(optionId);
        return ApiResponse.success(responses, "옵션별 객실 목록 조회 성공");
    }
} 