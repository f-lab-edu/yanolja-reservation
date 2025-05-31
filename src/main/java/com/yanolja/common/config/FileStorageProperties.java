package com.yanolja.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file.upload")
@Getter
@Setter
public class FileStorageProperties {
    
    /**
     * 숙소 이미지 업로드 경로
     */
    private String accommodationImageDir = "uploads/accommodations";
    
    /**
     * 최대 파일 크기 (바이트 단위)
     */
    private long maxSize = 10485760; // 10MB
} 