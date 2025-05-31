package com.yanolja.areas.accommodation.controller;

import com.yanolja.areas.accommodation.dto.AccommodationImageDto;
import com.yanolja.areas.accommodation.service.AccommodationImageService;
import com.yanolja.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations")
@Tag(name = "숙소 이미지 API", description = "숙소 이미지 관련 API")
public class AccommodationImageController {

    private final AccommodationImageService accommodationImageService;

    @Operation(summary = "숙소 이미지 목록 조회", description = "특정 숙소의 이미지 목록을 조회합니다.")
    @GetMapping("/{accommodationId}/images")
    public ApiResponse<AccommodationImageDto.ListResponse> getAccommodationImages(@PathVariable Long accommodationId) {
        AccommodationImageDto.ListResponse response = accommodationImageService.getImagesByAccommodationId(accommodationId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "숙소 이미지 업로드", description = "특정 숙소에 이미지를 업로드합니다.")
    @PostMapping(value = "/{accommodationId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<AccommodationImageDto.Response>> uploadAccommodationImages(
            @PathVariable Long accommodationId,
            @RequestParam(value = "files") MultipartFile[] files,
            @RequestParam(value = "mainImageIndex", required = false) Integer mainImageIndex) throws IOException {
        List<AccommodationImageDto.Response> responses = accommodationImageService.saveImages(accommodationId, files, mainImageIndex);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "대표 이미지 설정", description = "특정 이미지를 숙소의 대표 이미지로 설정합니다.")
    @PutMapping("/images/{imageId}/main")
    public ApiResponse<AccommodationImageDto.Response> setMainImage(@PathVariable Long imageId) {
        AccommodationImageDto.Response response = accommodationImageService.setAsMainImage(imageId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "숙소 이미지 삭제", description = "특정 숙소 이미지를 삭제합니다.")
    @DeleteMapping("/images/{imageId}")
    public ApiResponse<Void> deleteAccommodationImage(@PathVariable Long imageId) {
        accommodationImageService.deleteImage(imageId);
        return ApiResponse.success();
    }
} 