package com.yanolja.areas.room.controller;

import com.yanolja.areas.room.dto.RoomImageDto;
import com.yanolja.areas.room.service.RoomImageService;
import com.yanolja.common.response.ApiResponse;
import com.yanolja.common.config.FileStorageProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Tag(name = "객실 이미지 API", description = "객실 이미지 관련 API 모음")
public class RoomImageController {

    private final RoomImageService roomImageService;
    private final FileStorageProperties fileStorageProperties;

    @Operation(summary = "객실 이미지 업로드", description = "객실에 이미지를 업로드합니다.")
    @PostMapping(value = "/{roomId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<RoomImageDto.Response>> uploadImages(
            @Parameter(description = "객실 ID", required = true) @PathVariable Long roomId,
            @Parameter(description = "이미지 파일 목록", required = true) @RequestParam("files") MultipartFile[] files,
            @Parameter(description = "대표 이미지 인덱스 (첫 번째 이미지 = 0)") @RequestParam(required = false) Integer mainImageIndex
    ) throws IOException {
        log.info("Uploading {} images for room ID: {}", files.length, roomId);
        List<RoomImageDto.Response> results = roomImageService.saveImages(roomId, files, mainImageIndex);
        return ApiResponse.success(results);
    }

    @Operation(summary = "객실 이미지 목록 조회", description = "객실의 이미지 목록을 조회합니다.")
    @GetMapping("/{roomId}/images")
    public ApiResponse<RoomImageDto.ListResponse> getImages(
            @Parameter(description = "객실 ID", required = true) @PathVariable Long roomId
    ) {
        log.info("Getting images for room ID: {}", roomId);
        RoomImageDto.ListResponse result = roomImageService.getImagesByRoomId(roomId);
        return ApiResponse.success(result);
    }

    @Operation(summary = "객실 대표 이미지 설정", description = "특정 이미지를 객실의 대표 이미지로 설정합니다.")
    @PutMapping("/images/{imageId}/main")
    public ApiResponse<RoomImageDto.Response> setMainImage(
            @Parameter(description = "이미지 ID", required = true) @PathVariable Long imageId
    ) {
        log.info("Setting image ID: {} as main", imageId);
        RoomImageDto.Response result = roomImageService.setAsMainImage(imageId);
        return ApiResponse.success(result);
    }

    @Operation(summary = "객실 이미지 삭제", description = "객실 이미지를 삭제합니다.")
    @DeleteMapping("/images/{imageId}")
    public ApiResponse<Void> deleteImage(
            @Parameter(description = "이미지 ID", required = true) @PathVariable Long imageId
    ) {
        log.info("Deleting image ID: {}", imageId);
        roomImageService.deleteImage(imageId);
        return ApiResponse.success();
    }

    @Operation(summary = "객실 이미지 파일 제공", description = "객실 이미지 파일을 제공합니다.")
    @GetMapping("/images/{roomId}/{filename:.+}")
    public ResponseEntity<Resource> getRoomImageFile(
            @Parameter(description = "객실 ID", required = true) @PathVariable Long roomId,
            @Parameter(description = "파일명", required = true) @PathVariable String filename) {
        try {
            Path filePath = Paths.get(fileStorageProperties.getRoomImageDir())
                    .resolve(roomId.toString())
                    .resolve(filename)
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            log.error("Error serving room image file: {}/{}", roomId, filename, e);
            return ResponseEntity.badRequest().build();
        }
    }
} 