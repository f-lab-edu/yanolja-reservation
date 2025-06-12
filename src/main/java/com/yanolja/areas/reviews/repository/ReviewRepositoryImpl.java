package com.yanolja.areas.reviews.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.reviews.dto.ReviewDto;
import com.yanolja.areas.reviews.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.yanolja.areas.reviews.entity.QReview.review;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Review> searchReviews(ReviewDto.SearchRequest searchRequest, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        // 숙소 ID 조건
        if (searchRequest.getAccommodationId() != null) {
            builder.and(review.accommodationId.eq(searchRequest.getAccommodationId()));
        }

        // 사용자 ID 조건
        if (searchRequest.getUserId() != null) {
            builder.and(review.userId.eq(searchRequest.getUserId()));
        }

        // 최소 평점 조건
        if (searchRequest.getMinRating() != null) {
            builder.and(review.rating.goe(searchRequest.getMinRating()));
        }

        // 정렬 조건
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(
                searchRequest.getSortBy(), 
                searchRequest.getSortDirection()
        );

        // 쿼리 실행
        List<Review> reviews = queryFactory
                .selectFrom(review)
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(review.count())
                .from(review)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(reviews, pageable, total != null ? total : 0);
    }

    @Override
    public List<Integer> getRatingStatistics(Long accommodationId) {
        List<Integer> ratingCounts = new ArrayList<>();

        for (int rating = 1; rating <= 5; rating++) {
            Long count = queryFactory
                    .select(review.count())
                    .from(review)
                    .where(
                            review.accommodationId.eq(accommodationId),
                            review.rating.eq(rating)
                    )
                    .fetchOne();
            ratingCounts.add(count != null ? count.intValue() : 0);
        }

        return ratingCounts;
    }

    @Override
    public Page<Review> findReviewsByUserId(Long userId, Pageable pageable) {
        List<Review> reviews = queryFactory
                .selectFrom(review)
                .where(review.userId.eq(userId))
                .orderBy(review.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(review.count())
                .from(review)
                .where(review.userId.eq(userId))
                .fetchOne();

        return new PageImpl<>(reviews, pageable, total != null ? total : 0);
    }

    @Override
    public List<Review> findRecentReviewsByAccommodationId(Long accommodationId, int limit) {
        return queryFactory
                .selectFrom(review)
                .where(review.accommodationId.eq(accommodationId))
                .orderBy(review.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    /**
     * 정렬 조건 생성
     * @param sortBy 정렬 기준
     * @param sortDirection 정렬 방향
     * @return OrderSpecifier
     */
    private OrderSpecifier<?> getOrderSpecifier(String sortBy, String sortDirection) {
        boolean isAsc = "asc".equalsIgnoreCase(sortDirection);

        return switch (sortBy) {
            case "rating" -> isAsc ? review.rating.asc() : review.rating.desc();
            case "createdAt" -> isAsc ? review.createdAt.asc() : review.createdAt.desc();
            case "updatedAt" -> isAsc ? review.updatedAt.asc() : review.updatedAt.desc();
            default -> review.createdAt.desc(); // 기본값
        };
    }
} 