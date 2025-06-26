package com.yanolja.areas.accommodation.controller;

import com.yanolja.common.response.ApiResponse;
import com.yanolja.common.config.FileStorageProperties;
import io.swagger.v3.oas.annotations.Operation;
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
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/amenities")
@Tag(name = "편의시설 아이콘 API", description = "편의시설 아이콘 관련 API")
public class AmenityImageController {

    private final FileStorageProperties fileStorageProperties;

    @Operation(summary = "편의시설 아이콘 업로드", description = "편의시설 아이콘을 업로드합니다.")
    @PostMapping(value = "/icons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadAmenityIcon(@RequestParam("file") MultipartFile file) throws IOException {
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("파일명이 올바르지 않습니다.");
        }

        String fileExtension = getFileExtension(originalFilename);
        if (!isValidImageExtension(fileExtension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (png, jpg, jpeg, svg만 지원)");
        }

        // 고유한 파일명 생성
        String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;

        // 저장 경로 생성
        Path uploadPath = Paths.get(fileStorageProperties.getFullAmenityIconDir());
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일 저장
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 접근 가능한 URL 생성
        String iconUrl = "/api/amenities/icons/" + uniqueFilename;
        
        log.info("편의시설 아이콘 업로드 완료: {}", iconUrl);
        
        return ApiResponse.success(iconUrl);
    }

    @Operation(summary = "편의시설 아이콘 파일 제공", description = "편의시설 아이콘 파일을 제공합니다.")
    @GetMapping("/icons/{filename:.+}")
    public ResponseEntity<Resource> getAmenityIconFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(fileStorageProperties.getFullAmenityIconDir()).resolve(filename).normalize();
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
        } catch (Exception e) {
            log.error("아이콘 파일 조회 실패: {}", filename, e);
            return ResponseEntity.badRequest().build();
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    private boolean isValidImageExtension(String extension) {
        return extension.equals("png") || 
               extension.equals("jpg") || 
               extension.equals("jpeg") || 
               extension.equals("svg");
    }
} 