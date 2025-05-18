package com.yanolja.areas.accommodation.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.QAccommodation;
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
    public List<Accommodation> findAllActive() {
        QAccommodation accommodation = QAccommodation.accommodation;
        
        return queryFactory
                .selectFrom(accommodation)
                .where(accommodation.deletedYn.eq("N"))
                .fetch();
    }
    
    @Override
    public Optional<Accommodation> findByIdAndNotDeleted(Long id) {
        QAccommodation accommodation = QAccommodation.accommodation;
        
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(accommodation)
                        .where(
                                accommodation.id.eq(id),
                                accommodation.deletedYn.eq("N")
                        )
                        .fetchOne()
        );
    }
    
    @Override
    public Page<Accommodation> searchAccommodations(
            String keyword, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            String sortBy,
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
        if (StringUtils.hasText(sortBy)) {
            switch (sortBy) {
                case "price_asc":
                    query.orderBy(accommodation.pricePerNight.asc());
                    break;
                case "price_desc":
                    query.orderBy(accommodation.pricePerNight.desc());
                    break;
                case "rating_desc":
                    query.orderBy(accommodation.rating.desc().nullsLast());
                    break;
                case "review_desc":
                    query.orderBy(accommodation.reviewCount.desc().nullsLast());
                    break;
                default:
                    // 기본 ID 기준 정렬
                    query.orderBy(accommodation.id.desc());
            }
        } else {
            // 기본 ID 기준 정렬
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