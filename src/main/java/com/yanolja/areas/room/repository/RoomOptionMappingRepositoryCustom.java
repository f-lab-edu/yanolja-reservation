package com.yanolja.areas.room.repository;

import com.yanolja.areas.room.dto.OptionUsageStatisticsDto;
import com.yanolja.areas.room.dto.RoomOptionCountStatisticsDto;
import com.yanolja.areas.room.entity.RoomOptionMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomOptionMappingRepositoryCustom {

    /**
     * 특정 숙소의 모든 객실-옵션 매핑 조회 (QueryDSL)
     * @param accommodationId 숙소 ID
     * @return 옵션 매핑 목록
     */
    List<RoomOptionMapping> findByAccommodationId(Long accommodationId);

    /**
     * 특정 옵션을 사용하는 객실들 중 삭제되지 않은 객실의 매핑만 조회 (QueryDSL)
     * @param optionId 옵션 ID
     * @return 옵션 매핑 목록
     */
    List<RoomOptionMapping> findByRoomOptionIdAndRoomNotDeleted(Long optionId);

    /**
     * 특정 객실의 옵션 매핑 조회 (삭제되지 않은 객실만) (QueryDSL)
     * @param roomId 객실 ID
     * @return 옵션 매핑 목록
     */
    List<RoomOptionMapping> findByRoomIdAndRoomNotDeleted(Long roomId);

    /**
     * 여러 옵션 ID를 모두 가진 객실 조회 (QueryDSL)
     * @param optionIds 옵션 ID 목록
     * @return 모든 옵션을 가진 객실의 매핑 목록
     */
    List<RoomOptionMapping> findRoomsWithAllOptions(List<Long> optionIds);

    /**
     * 여러 옵션 ID 중 하나 이상을 가진 객실 조회 (QueryDSL)
     * @param optionIds 옵션 ID 목록
     * @return 옵션 중 하나 이상을 가진 객실의 매핑 목록
     */
    List<RoomOptionMapping> findRoomsWithAnyOptions(List<Long> optionIds);

    /**
     * 옵션별 사용중인 객실 수 통계 (QueryDSL)
     * @return 옵션 사용 통계 DTO 목록
     */
    List<OptionUsageStatisticsDto> getOptionUsageStatistics();

    /**
     * 객실별 연결된 옵션 수 통계 (QueryDSL)
     * @return 객실별 옵션 개수 통계 DTO 목록
     */
    List<RoomOptionCountStatisticsDto> getRoomOptionCountStatistics();

} 