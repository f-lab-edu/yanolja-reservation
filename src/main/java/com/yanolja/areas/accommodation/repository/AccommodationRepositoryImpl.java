package com.yanolja.areas.accommodation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.QAccommodation;
import com.yanolja.common.dto.PageRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccommodationRepositoryImpl implements AccommodationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    
    @Override
    public Page<Accommodation> searchAccommodations(
            String keyword, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            PageRequestDto pageRequestDto,
            Pageable pageable) {
        
        QAccommodation accommodation = QAccommodation.accommodation;
        
        // 조건절 생성
        BooleanBuilder whereBuilder = new BooleanBuilder();
        
        // 활성 상태이고 삭제되지 않은 숙소만 포함
        whereBuilder.and(accommodation.status.eq("ACTIVE"));
        whereBuilder.and(accommodation.deletedYn.eq("N"));
        
        // 키워드 검색 조건 추가
        if (StringUtils.hasText(keyword)) {
            whereBuilder.and(
                accommodation.name.containsIgnoreCase(keyword)
                .or(accommodation.address.containsIgnoreCase(keyword))
            );
        }
        
        // 가격 범위 조건 추가
        if (minPrice != null) {
            whereBuilder.and(accommodation.pricePerNight.goe(minPrice));
        }
        
        if (maxPrice != null) {
            whereBuilder.and(accommodation.pricePerNight.loe(maxPrice));
        }
        
        // 기본 쿼리 생성
        JPAQuery<Accommodation> query = queryFactory
                .selectFrom(accommodation)
                .where(whereBuilder);
        
        // 정렬 적용
        OrderSpecifier<?>[] orderSpecifiers = pageRequestDto.toOrderSpecifier(column -> {
                boolean isAsc = !"desc".equalsIgnoreCase(pageRequestDto.getSortDirection());
                
                switch (column) {
                    case "price":
                        return isAsc ? accommodation.pricePerNight.asc() : accommodation.pricePerNight.desc();
                    case "rating":
                        return isAsc 
                            ? accommodation.rating.asc().nullsLast() 
                            : accommodation.rating.desc().nullsLast();
                    case "review":
                        return isAsc 
                            ? accommodation.reviewCount.asc().nullsLast() 
                            : accommodation.reviewCount.desc().nullsLast();
                    default:
                        return accommodation.id.desc(); // 기본값
                }
            });

        if (orderSpecifiers.length > 0) {
            query.orderBy(orderSpecifiers);
        } else {
            // 기본 정렬 (ID 내림차순)
            query.orderBy(accommodation.id.desc());
        }
        
        // 전체 개수 조회
        long total = query.fetchCount();
        
        // 페이징 적용
        List<Accommodation> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        
        return new PageImpl<>(results, pageable, total);
    }
    
} 