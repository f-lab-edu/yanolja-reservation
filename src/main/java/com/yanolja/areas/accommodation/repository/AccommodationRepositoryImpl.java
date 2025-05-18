package com.yanolja.areas.accommodation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yanolja.areas.accommodation.entity.Accommodation;
import com.yanolja.areas.accommodation.entity.QAccommodation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
} 