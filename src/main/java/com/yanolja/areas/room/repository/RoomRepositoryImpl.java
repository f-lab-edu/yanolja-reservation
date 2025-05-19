package com.yanolja.areas.room.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.room.entity.QRoom;
import com.yanolja.areas.room.entity.Room;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoomRepositoryImpl implements RoomRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<Room> findAllNotDeleted() {
        QRoom room = QRoom.room;
        
        return queryFactory
                .selectFrom(room)
                .where(room.deletedYn.eq("N"))
                .fetch();
    }
    
    @Override
    public Optional<Room> findByIdAndNotDeleted(Long id) {
        QRoom room = QRoom.room;
        
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(room)
                        .where(
                                room.id.eq(id),
                                room.deletedYn.eq("N")
                        )
                        .fetchOne()
        );
    }
    
    @Override
    public List<Room> findByAccommodationIdAndNotDeleted(Long accommodationId) {
        QRoom room = QRoom.room;
        
        return queryFactory
                .selectFrom(room)
                .where(
                        room.accommodationId.eq(accommodationId),
                        room.deletedYn.eq("N")
                )
                .fetch();
    }
    
    @Override
    public Page<Room> searchRooms(
            String keyword, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            Integer minCapacity,
            String sortBy, 
            Pageable pageable) {
        
        QRoom room = QRoom.room;
        
        // 기본 조건 : 삭제되지 않은 객실
        BooleanExpression conditions = room.deletedYn.eq("N");
        
        // 키워드 검색 조건
        if (StringUtils.hasText(keyword)) {
            conditions = conditions.and(
                    room.name.containsIgnoreCase(keyword)
                            .or(room.description.containsIgnoreCase(keyword))
            );
        }
        
        // 가격 범위 조건
        if (minPrice != null) {
            conditions = conditions.and(room.pricePerNight.goe(minPrice));
        }
        
        if (maxPrice != null) {
            conditions = conditions.and(room.pricePerNight.loe(maxPrice));
        }
        
        // 수용 인원 조건
        if (minCapacity != null) {
            conditions = conditions.and(room.capacity.goe(minCapacity));
        }
        
        // 쿼리 생성
        JPAQuery<Room> query = queryFactory
                .selectFrom(room)
                .where(conditions)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        
        // 정렬 조건
        if (StringUtils.hasText(sortBy)) {
            OrderSpecifier<?> orderSpecifier = getOrderSpecifier(sortBy, room);
            query.orderBy(orderSpecifier);
        } else {
            query.orderBy(room.id.asc());
        }
        
        List<Room> content = query.fetch();
        
        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(room.count())
                .from(room)
                .where(conditions);
        
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
    
    private OrderSpecifier<?> getOrderSpecifier(String sortBy, QRoom room) {
        switch (sortBy) {
            case "price_asc":
                return new OrderSpecifier<>(Order.ASC, room.pricePerNight);
            case "price_desc":
                return new OrderSpecifier<>(Order.DESC, room.pricePerNight);
            case "capacity_asc":
                return new OrderSpecifier<>(Order.ASC, room.capacity);
            case "capacity_desc":
                return new OrderSpecifier<>(Order.DESC, room.capacity);
            default:
                return new OrderSpecifier<>(Order.ASC, room.id);
        }
    }
} 