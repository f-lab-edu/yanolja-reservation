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
     * 기본 업로드 루트 경로
     */
    private String baseDir = System.getProperty("java.io.tmpdir") + "/yanolja-uploads";
    
    /**
     * 숙소 이미지 업로드 경로
     */
    private String accommodationImageDir = "accommodations";
    
    /**
     * 객실 이미지 업로드 경로
     */
    private String roomImageDir = "rooms";
    
    /**
     * 편의시설 아이콘 업로드 경로
     */
    private String amenityIconDir = "amenities";
    
    /**
     * 최대 파일 크기 (바이트 단위)
     */
    private long maxSize = 10485760; // 10MB
    
    /**
     * 전체 경로 반환 메서드들
     */
    public String getFullAccommodationImageDir() {
        return baseDir + "/" + accommodationImageDir;
    }
    
    public String getFullRoomImageDir() {
        return baseDir + "/" + roomImageDir;
    }
    
    public String getFullAmenityIconDir() {
        return baseDir + "/" + amenityIconDir;
    }
} 