package com.yanolja.areas.accommodation.controller;

import com.yanolja.areas.accommodation.dto.PortalAccommodationDto;
import com.yanolja.areas.accommodation.service.PortalAccommodationService;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portal/accommodations")
@RequiredArgsConstructor
@Tag(name = "숙소 포털 API", description = "일반 사용자용 숙소 검색 및 조회 API")
public class PortalAccommodationController {

    private final PortalAccommodationService portalAccommodationService;

    @PostMapping("/search")
    @Operation(summary = "숙소 검색", description = "조건에 맞는 숙소를 검색합니다. (이름, 주소, 가격 범위 등)")
    public ApiResponse<Page<PortalAccommodationDto.ListResponse>> searchAccommodations(
            @RequestBody PortalAccommodationDto.SearchRequest searchRequest) {
        
        Page<PortalAccommodationDto.ListResponse> responses = 
                portalAccommodationService.searchAccommodations(searchRequest);
                
        return ApiResponse.success(responses, "숙소 검색 결과입니다.");
    }

    @GetMapping("/{id}")
    @Operation(summary = "숙소 상세 조회", description = "숙소의 상세 정보를 조회합니다.")
    public ApiResponse<PortalAccommodationDto.DetailResponse> getAccommodationDetail(@PathVariable Long id) {
        PortalAccommodationDto.DetailResponse response = portalAccommodationService.getAccommodationDetail(id);
        return ApiResponse.success(response, "숙소 상세 정보입니다.");
    }
} 