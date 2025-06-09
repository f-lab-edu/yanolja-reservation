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

@Repository
@RequiredArgsConstructor
public class RoomRepositoryImpl implements RoomRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public Page<Room> searchRooms(
            String keyword, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            Integer minCapacity,
            String sortBy, 
            Pageable pageable) {
        
        QRoom room = QRoom.room;
        
        // 기본 조건 : @Where 어노테이션에 의해 자동으로 삭제되지 않은 객실만 조회됨
        // 추가 조건만 작성
        BooleanExpression conditions = null;
        
        // 키워드 검색 조건
        if (StringUtils.hasText(keyword)) {
            conditions = room.name.containsIgnoreCase(keyword)
                            .or(room.description.containsIgnoreCase(keyword));
        }
        
        // 가격 범위 조건
        if (minPrice != null) {
            BooleanExpression minPriceCondition = room.pricePerNight.goe(minPrice);
            conditions = conditions != null ? conditions.and(minPriceCondition) : minPriceCondition;
        }
        
        if (maxPrice != null) {
            BooleanExpression maxPriceCondition = room.pricePerNight.loe(maxPrice);
            conditions = conditions != null ? conditions.and(maxPriceCondition) : maxPriceCondition;
        }
        
        // 수용 인원 조건
        if (minCapacity != null) {
            BooleanExpression capacityCondition = room.capacity.goe(minCapacity);
            conditions = conditions != null ? conditions.and(capacityCondition) : capacityCondition;
        }
        
        // 쿼리 생성
        JPAQuery<Room> query = queryFactory
                .selectFrom(room)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());
        
        // 조건 추가 (조건이 있는 경우에만)
        if (conditions != null) {
            query.where(conditions);
        }
        
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
                .from(room);
        
        if (conditions != null) {
            countQuery.where(conditions);
        }
        
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