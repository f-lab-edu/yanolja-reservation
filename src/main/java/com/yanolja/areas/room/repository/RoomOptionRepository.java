package com.yanolja.areas.room.repository;

import com.yanolja.areas.room.entity.RoomOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomOptionRepository extends JpaRepository<RoomOption, Long> {
    
    /**
     * 이름으로 객실 옵션 중복 확인
     * @param name 옵션 이름
     * @return 중복 여부
     */
    boolean existsByName(String name);
    
    /**
     * 이름으로 객실 옵션 중복 확인 (수정 시 자신 제외)
     * @param name 옵션 이름
     * @param id 제외할 옵션 ID
     * @return 중복 여부
     */
    boolean existsByNameAndIdNot(String name, Long id);
    
    /**
     * 모든 객실 옵션 조회 (생성일시 역순 정렬)
     * @return 객실 옵션 목록
     */
    List<RoomOption> findAllByOrderByCreatedAtDesc();
} 