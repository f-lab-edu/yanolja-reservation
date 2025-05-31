package com.yanolja.areas.room.repository;

import com.yanolja.areas.room.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    
    /**
     * 객실 ID로 이미지 목록 조회
     * @param roomId 객실 ID
     * @return 객실 이미지 목록
     */
    List<RoomImage> findByRoomId(Long roomId);
    
    /**
     * 객실 ID로 대표 이미지 조회
     * @param roomId 객실 ID
     * @return 대표 이미지
     */
    RoomImage findByRoomIdAndIsMainTrue(Long roomId);
} 