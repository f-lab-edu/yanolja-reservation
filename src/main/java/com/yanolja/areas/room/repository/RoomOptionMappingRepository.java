package com.yanolja.areas.room.repository;

import com.yanolja.areas.room.entity.RoomOptionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomOptionMappingRepository extends JpaRepository<RoomOptionMapping, Long>, RoomOptionMappingRepositoryCustom {

    /**
     * 특정 객실의 옵션 매핑 삭제 (QueryMethod)
     * @param roomId 객실 ID
     */
    @Modifying
    void deleteByRoomId(Long roomId);

    /**
     * 특정 객실의 특정 옵션 매핑 삭제 (QueryMethod)
     * @param roomId 객실 ID
     * @param roomOptionId 옵션 ID
     */
    @Modifying
    void deleteByRoomIdAndRoomOptionId(Long roomId, Long roomOptionId);

    /**
     * 특정 객실-옵션 매핑 존재 여부 확인 (QueryMethod)
     * @param roomId 객실 ID
     * @param roomOptionId 옵션 ID
     * @return 존재 여부
     */
    boolean existsByRoomIdAndRoomOptionId(Long roomId, Long roomOptionId);
} 