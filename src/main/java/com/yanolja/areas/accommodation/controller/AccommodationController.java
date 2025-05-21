package com.yanolja.areas.accommodation.controller;

import com.yanolja.areas.accommodation.dto.AccommodationDto;
import com.yanolja.areas.accommodation.service.AccommodationService;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/accommodations")
@RequiredArgsConstructor
@Tag(name = "숙소 관리", description = "숙소 관리 API")
public class AccommodationController {

    private final AccommodationService accommodationService;

    @PostMapping
    @Operation(summary = "숙소 등록", description = "새로운 숙소를 등록합니다.")
    public ApiResponse<AccommodationDto.Response> createAccommodation(
            @Valid @RequestBody AccommodationDto.Request request) {
        AccommodationDto.Response response = accommodationService.createAccommodation(request);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "숙소 목록 조회", description = "등록된 모든 숙소의 목록을 조회합니다.")
    public ApiResponse<List<AccommodationDto.ListResponse>> getAllAccommodations() {
        List<AccommodationDto.ListResponse> responses = accommodationService.getAllAccommodations();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "숙소 상세 조회", description = "특정 숙소의 상세 정보를 조회합니다.")
    public ApiResponse<AccommodationDto.Response> getAccommodation(@PathVariable Long id) {
        AccommodationDto.Response response = accommodationService.getAccommodationById(id);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "숙소 정보 수정", description = "특정 숙소의 정보를 수정합니다.")
    public ApiResponse<AccommodationDto.Response> updateAccommodation(
            @PathVariable Long id,
            @Valid @RequestBody AccommodationDto.Request request) {
        AccommodationDto.Response response = accommodationService.updateAccommodation(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "숙소 삭제", description = "특정 숙소를 소프트 삭제 처리합니다.")
    public ApiResponse<Void> deleteAccommodation(@PathVariable Long id) {
        accommodationService.deleteAccommodation(id);
        return ApiResponse.success();
    }
} 