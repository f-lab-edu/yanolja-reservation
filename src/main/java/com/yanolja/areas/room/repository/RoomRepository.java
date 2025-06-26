package com.yanolja.areas.room.repository;

import com.yanolja.areas.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long>, RoomRepositoryCustom {
    
    /**
     * 숙소별 객실 목록 조회 (삭제되지 않은 객실만)
     * @Where 어노테이션에 의해 자동으로 삭제되지 않은 데이터만 조회됨
     */
    List<Room> findByAccommodationId(Long accommodationId);
} 