package com.yanolja.areas.room.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.room.dto.OptionUsageStatisticsDto;
import com.yanolja.areas.room.dto.RoomOptionCountStatisticsDto;
import com.yanolja.areas.room.entity.QRoom;
import com.yanolja.areas.room.entity.QRoomOption;
import com.yanolja.areas.room.entity.QRoomOptionMapping;
import com.yanolja.areas.room.entity.RoomOptionMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RoomOptionMappingRepositoryImpl implements RoomOptionMappingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RoomOptionMapping> findByAccommodationId(Long accommodationId) {
        QRoomOptionMapping mapping = QRoomOptionMapping.roomOptionMapping;
        QRoom room = QRoom.room;

        return queryFactory
                .selectFrom(mapping)
                .join(mapping.room, room).fetchJoin()
                .where(
                        room.accommodationId.eq(accommodationId),
                        room.deletedYn.eq("N")
                )
                .fetch();
    }

    @Override
    public List<RoomOptionMapping> findByRoomOptionIdAndRoomNotDeleted(Long optionId) {
        QRoomOptionMapping mapping = QRoomOptionMapping.roomOptionMapping;
        QRoom room = QRoom.room;

        return queryFactory
                .selectFrom(mapping)
                .join(mapping.room, room).fetchJoin()
                .where(
                        mapping.roomOption.id.eq(optionId),
                        room.deletedYn.eq("N")
                )
                .fetch();
    }

    @Override
    public List<RoomOptionMapping> findByRoomIdAndRoomNotDeleted(Long roomId) {
        QRoomOptionMapping mapping = QRoomOptionMapping.roomOptionMapping;
        QRoom room = QRoom.room;
        QRoomOption roomOption = QRoomOption.roomOption;

        return queryFactory
                .selectFrom(mapping)
                .join(mapping.room, room).fetchJoin()
                .join(mapping.roomOption, roomOption).fetchJoin()
                .where(
                        room.id.eq(roomId),
                        room.deletedYn.eq("N")
                )
                .fetch();
    }

    @Override
    public List<RoomOptionMapping> findRoomsWithAllOptions(List<Long> optionIds) {
        if (optionIds == null || optionIds.isEmpty()) {
            return List.of();
        }

        QRoomOptionMapping mapping = QRoomOptionMapping.roomOptionMapping;
        QRoom room = QRoom.room;

        // 모든 옵션을 가진 객실 ID 조회
        List<Long> roomIds = queryFactory
                .select(mapping.room.id)
                .from(mapping)
                .join(mapping.room, room)
                .where(
                        mapping.roomOption.id.in(optionIds),
                        room.deletedYn.eq("N")
                )
                .groupBy(mapping.room.id)
                .having(mapping.roomOption.id.countDistinct().eq((long) optionIds.size()))
                .fetch();

        if (roomIds.isEmpty()) {
            return List.of();
        }

        // 해당 객실들의 모든 매핑 조회
        return queryFactory
                .selectFrom(mapping)
                .join(mapping.room, room).fetchJoin()
                .join(mapping.roomOption).fetchJoin()
                .where(
                        mapping.room.id.in(roomIds),
                        room.deletedYn.eq("N")
                )
                .fetch();
    }

    @Override
    public List<RoomOptionMapping> findRoomsWithAnyOptions(List<Long> optionIds) {
        if (optionIds == null || optionIds.isEmpty()) {
            return List.of();
        }

        QRoomOptionMapping mapping = QRoomOptionMapping.roomOptionMapping;
        QRoom room = QRoom.room;

        return queryFactory
                .selectFrom(mapping)
                .join(mapping.room, room).fetchJoin()
                .join(mapping.roomOption).fetchJoin()
                .where(
                        mapping.roomOption.id.in(optionIds),
                        room.deletedYn.eq("N")
                )
                .distinct()
                .fetch();
    }

    @Override
    public List<OptionUsageStatisticsDto> getOptionUsageStatistics() {
        QRoomOptionMapping mapping = QRoomOptionMapping.roomOptionMapping;
        QRoom room = QRoom.room;
        QRoomOption roomOption = QRoomOption.roomOption;

        return queryFactory
                .select(Projections.constructor(OptionUsageStatisticsDto.class,
                        roomOption.id,
                        roomOption.name,
                        mapping.count()
                ))
                .from(mapping)
                .join(mapping.room, room)
                .join(mapping.roomOption, roomOption)
                .where(room.deletedYn.eq("N"))
                .groupBy(roomOption.id, roomOption.name)
                .orderBy(mapping.count().desc())
                .fetch();
    }

    @Override
    public List<RoomOptionCountStatisticsDto> getRoomOptionCountStatistics() {
        QRoomOptionMapping mapping = QRoomOptionMapping.roomOptionMapping;
        QRoom room = QRoom.room;

        return queryFactory
                .select(Projections.constructor(RoomOptionCountStatisticsDto.class,
                        room.id,
                        room.name,
                        mapping.count()
                ))
                .from(mapping)
                .join(mapping.room, room)
                .where(room.deletedYn.eq("N"))
                .groupBy(room.id, room.name)
                .orderBy(mapping.count().desc())
                .fetch();
    }

} 